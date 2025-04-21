package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.MateriaJarQuadBlockEntity;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageSingleTypeBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.item.renderer.MateriaVesselItemRenderer;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class MateriaVesselItem extends BlockItem {
    public MateriaVesselItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(() -> new MateriaVesselItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()));

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer.get();
            }
        });
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        //We need to get the tag BEFORE the interaction result is generated.
        //If the jar was a single item stack, the tag won't exist to reference after the result is generated.
        CompoundTag tag = null;
        if (context.getItemInHand().hasTag()) {
            tag = context.getItemInHand().getTag();
        }
        InteractionResult result = super.place(context);

        if(result == InteractionResult.CONSUME) {
            if (!context.getLevel().isClientSide()) {
                BlockPos clickedPos = context.getClickedPos();
                BlockEntity entity = context.getLevel().getBlockEntity(clickedPos);

                if (entity != null) {
                    if (entity instanceof MateriaVesselBlockEntity mvbe && tag != null) {
                        if (tag.contains("type") && tag.contains("amount")) {
                            MateriaItem materia = ItemRegistry.getMateriaMap(false, false)
                                    .get(tag.getString("type"));

                            mvbe.setContents(materia, tag.getInt("amount"));
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if(pContext.getPlayer().isCrouching())
            return super.useOn(pContext);

        if(pContext.getLevel().isClientSide()) {
            BlockEntity be = pContext.getLevel().getBlockEntity(pContext.getClickedPos());

            if (be instanceof AbstractMateriaStorageSingleTypeBlockEntity || be instanceof AbstractMateriaStorageMultiTypeBlockEntity) {
                return InteractionResult.SUCCESS;
            }
        } else {
            BlockEntity be = pContext.getLevel().getBlockEntity(pContext.getClickedPos());

            if (be instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
                ItemStack itemInHand = pContext.getItemInHand();
                CompoundTag itemTag = itemInHand.getOrCreateTag();

                if (itemTag.contains("type")) {
                    String itemTypeString = itemTag.getString("type");
                    int itemAmount = itemTag.getInt("amount");

                    if (single.getMateriaType() != null) {
                        String targetTypeString = single.getMateriaType().getMateriaName();
                        if (targetTypeString.equals(itemTypeString)) {
                            int inserted = single.insertMateria(itemAmount);
                            int remaining = itemAmount - inserted;
                            if (remaining == 0) {
                                itemInHand.removeTagKey("type");
                                itemInHand.removeTagKey("amount");
                            } else {
                                itemTag.putInt("amount", remaining);
                                itemInHand.setTag(itemTag);
                            }
                        }
                    } else {
                        MateriaItem itemType = ItemRegistry.getMateriaMap(false, false).get(itemTypeString);

                        if (itemType != null) {
                            single.insertMateria(new ItemStack(itemType, itemAmount));
                            itemInHand.removeTagKey("type");
                            itemInHand.removeTagKey("amount");
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            } else if (be instanceof MateriaJarQuadBlockEntity quad) {
                ItemStack itemInHand = pContext.getItemInHand();
                CompoundTag itemTag = itemInHand.getOrCreateTag();
                int slot = quad.getSlotFromWorldCoord(pContext.getClickLocation());

                if (itemTag.contains("type")) {
                    String itemTypeString = itemTag.getString("type");
                    int itemAmount = itemTag.getInt("amount");
                    MateriaItem mi = AbstractMateriaStorageMultiTypeBlockEntity.materiaMap.getOrDefault(itemTypeString, null);

                    final MateriaItem typeInSlot = quad.getMateriaTypeInSlot(slot);

                    if (mi != null && typeInSlot != null) {
                        final int amountInSlot = Math.max(0,quad.getMateriaAmountInSlot(slot));
                        final int limitInSlot = quad.getStorageLimit(typeInSlot);

                        int remainingSpace = limitInSlot - amountInSlot;

                        int inserted = Math.min(remainingSpace, itemAmount);
                        int remaining = itemAmount - inserted;
                        if (remaining == 0) {
                            itemInHand.removeTagKey("type");
                            itemInHand.removeTagKey("amount");
                        } else {
                            itemTag.putInt("amount", remaining);
                            itemInHand.setTag(itemTag);
                        }
                        quad.setContents(slot, typeInSlot, amountInSlot + inserted);
                    } else if (mi != null && !quad.containsMateriaType(mi)) {
                        final int remainingSpace = (mi instanceof AdmixtureItem) ? ServerConfig.materiaJarAdmixtureCapacity : ServerConfig.materiaJarEssentiaCapacity;
                        int inserted = Math.min(remainingSpace, itemAmount);
                        if(inserted < itemAmount) {
                            itemTag.putInt("amount", itemAmount - inserted);
                            itemInHand.setTag(itemTag);
                            quad.setContents(slot, mi, Math.max(0,quad.getMateriaAmountInSlot(slot)) + inserted);
                        } else {
                            itemInHand.removeTagKey("type");
                            itemInHand.removeTagKey("amount");
                            quad.setContents(slot, mi, itemAmount);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.useOn(pContext);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(
                Component.translatable("tooltip.magichem.materiavessel")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }
}
