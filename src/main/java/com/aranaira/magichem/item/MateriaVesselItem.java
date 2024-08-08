package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.item.renderer.MateriaVesselItemRenderer;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
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

            if (be instanceof AbstractMateriaStorageBlockEntity amsbe) {
                return InteractionResult.SUCCESS;
            }
        } else {
            BlockEntity be = pContext.getLevel().getBlockEntity(pContext.getClickedPos());

            if (be instanceof AbstractMateriaStorageBlockEntity amsbe) {
                ItemStack itemInHand = pContext.getItemInHand();
                CompoundTag itemTag = itemInHand.getOrCreateTag();

                if (itemTag.contains("type")) {
                    String itemTypeString = itemTag.getString("type");
                    int itemAmount = itemTag.getInt("amount");

                    if (amsbe.getMateriaType() != null) {
                        String targetTypeString = amsbe.getMateriaType().getMateriaName();
                        if (targetTypeString.equals(itemTypeString)) {
                            int inserted = amsbe.insertMateria(itemAmount);
                            int remaining = itemAmount - inserted;
                            if(remaining == 0) {
                                itemInHand.removeTagKey("type");
                                itemInHand.removeTagKey("amount");
                            } else {
                                itemTag.putInt("amount", remaining);
                                itemInHand.setTag(itemTag);
                            }
                        }
                    } else {
                        MateriaItem itemType = ItemRegistry.getMateriaMap(false, false).get(itemTypeString);

                        if(itemType != null) {
                            amsbe.insertMateria(new ItemStack(itemType, itemAmount));
                            itemInHand.removeTagKey("type");
                            itemInHand.removeTagKey("amount");
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
