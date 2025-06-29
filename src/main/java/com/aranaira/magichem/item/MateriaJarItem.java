package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.MateriaJarBlockEntity;
import com.aranaira.magichem.block.entity.MateriaJarQuadBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageSingleTypeBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.item.renderer.MateriaJarItemRenderer;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class MateriaJarItem extends BlockItem {
    public MateriaJarItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(() -> new MateriaJarItemRenderer(
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
                    if (entity instanceof MateriaJarBlockEntity mvbe && tag != null) {
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
            ItemStack itemInHand = pContext.getItemInHand();
            boolean spawnNew = itemInHand.getCount() > 1;
            CompoundTag itemTag = itemInHand.getOrCreateTag();

            if (be instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
                if (itemTag.contains("type")) {
                    String itemTypeString = itemTag.getString("type");
                    int itemAmount = itemTag.getInt("amount");
                    MateriaItem mi = AbstractMateriaStorageMultiTypeBlockEntity.materiaMap.getOrDefault(itemTypeString, null);

                    final MateriaItem typeInContainer = single.getMateriaType();

                    if (mi != null && typeInContainer == mi) {
                        final int amountInContainer = Math.max(0,single.getCurrentStock());
                        final int limitInContainer = single.getStorageLimit();

                        int remainingSpace = limitInContainer - amountInContainer;

                        int inserted = Math.min(remainingSpace, itemAmount);
                        int remaining = itemAmount - inserted;

                        if (remaining == 0) {
                            if(spawnNew) {
                                ItemStack is = new ItemStack(BlockRegistry.MATERIA_JAR.get().asItem());
                                ItemEntity ie = new ItemEntity(
                                        pContext.getLevel(),
                                        pContext.getPlayer().getX(),
                                        pContext.getPlayer().getY(),
                                        pContext.getPlayer().getZ(),
                                        is
                                );
                                itemInHand.shrink(1);
                                pContext.getPlayer().setItemInHand(pContext.getHand(), itemInHand);
                                pContext.getLevel().addFreshEntity(ie);
                            } else {
                                itemInHand.removeTagKey("type");
                                itemInHand.removeTagKey("amount");
                            }
                        } else {
                            if(spawnNew) {
                                ItemStack is = new ItemStack(BlockRegistry.MATERIA_JAR.get().asItem());

                                if(remaining > 0) {
                                    CompoundTag nbt = new CompoundTag();
                                    nbt.putString("type", mi.getMateriaName());
                                    nbt.putInt("amount", remaining);
                                    is.setTag(nbt);
                                }

                                ItemEntity ie = new ItemEntity(
                                        pContext.getLevel(),
                                        pContext.getPlayer().getX(),
                                        pContext.getPlayer().getY(),
                                        pContext.getPlayer().getZ(),
                                        is
                                );
                                itemInHand.shrink(1);
                                pContext.getPlayer().setItemInHand(pContext.getHand(), itemInHand);
                                pContext.getLevel().addFreshEntity(ie);
                            } else {
                                itemTag.putInt("amount", remaining);
                                itemInHand.setTag(itemTag);
                            }
                        }
                        single.setContents(typeInContainer, amountInContainer + inserted);
                    } else if(mi != null && typeInContainer == null) {
                        final int amountInContainer = Math.max(0, single.getCurrentStock());
                        final int limitInContainer = single.getStorageLimit();

                        int remainingSpace = limitInContainer - amountInContainer;

                        int inserted = Math.min(remainingSpace, itemAmount);
                        int remaining = itemAmount - inserted;

                        if (spawnNew) {
                            ItemStack is = new ItemStack(BlockRegistry.MATERIA_JAR.get().asItem());

                            if(remaining > 0) {
                                CompoundTag nbt = new CompoundTag();
                                nbt.putString("type", mi.getMateriaName());
                                nbt.putInt("amount", remaining);
                                is.setTag(nbt);
                            }
                            ItemEntity ie = new ItemEntity(
                                    pContext.getLevel(),
                                    pContext.getPlayer().getX(),
                                    pContext.getPlayer().getY(),
                                    pContext.getPlayer().getZ(),
                                    is
                            );
                            itemInHand.shrink(1);
                            pContext.getPlayer().setItemInHand(pContext.getHand(), itemInHand);
                            pContext.getLevel().addFreshEntity(ie);
                        } else {
                            if(remaining > 0) {
                                itemTag.putInt("amount", remaining);
                                itemInHand.setTag(itemTag);
                                pContext.getPlayer().setItemInHand(pContext.getHand(), itemInHand);
                            } else {
                                itemInHand.removeTagKey("type");
                                itemInHand.removeTagKey("amount");
                            }
                        }
                        single.setContents(mi, inserted);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            else if (be instanceof MateriaJarQuadBlockEntity quad) {
                int slot = quad.getSlotFromWorldCoord(pContext.getClickLocation());

                if (itemTag.contains("type")) {
                    String itemTypeString = itemTag.getString("type");
                    int itemAmount = itemTag.getInt("amount");
                    MateriaItem mi = AbstractMateriaStorageMultiTypeBlockEntity.materiaMap.getOrDefault(itemTypeString, null);

                    final MateriaItem typeInSlot = quad.getMateriaTypeInSlot(slot);

                    if (mi != null && typeInSlot != null) {
                        final int amountInSlot = Math.max(0,quad.getMateriaAmountInSlot(slot));
                        final int limitInSlot = quad.getStorageLimitIgnoreStoredTypes(typeInSlot);

                        int remainingSpace = limitInSlot - amountInSlot;

                        int inserted = Math.min(remainingSpace, itemAmount);
                        int remaining = itemAmount - inserted;
                        if(remaining == 0) {
                            if(spawnNew) {
                                ItemStack is = new ItemStack(BlockRegistry.MATERIA_JAR.get().asItem());
                                ItemEntity ie = new ItemEntity(
                                        pContext.getLevel(),
                                        pContext.getPlayer().getX(),
                                        pContext.getPlayer().getY(),
                                        pContext.getPlayer().getZ(),
                                        is
                                );
                                itemInHand.shrink(1);
                                pContext.getPlayer().setItemInHand(pContext.getHand(), itemInHand);
                                pContext.getLevel().addFreshEntity(ie);
                            } else {
                                itemInHand.removeTagKey("type");
                                itemInHand.removeTagKey("amount");
                            }
                        } else {
                            if(spawnNew) {
                                ItemStack is = new ItemStack(BlockRegistry.MATERIA_JAR.get().asItem());

                                if(remaining > 0) {
                                    CompoundTag nbt = new CompoundTag();
                                    nbt.putString("type", mi.getMateriaName());
                                    nbt.putInt("amount", remaining);
                                    is.setTag(nbt);
                                }

                                ItemEntity ie = new ItemEntity(
                                        pContext.getLevel(),
                                        pContext.getPlayer().getX(),
                                        pContext.getPlayer().getY(),
                                        pContext.getPlayer().getZ(),
                                        is
                                );
                                itemInHand.shrink(1);
                                pContext.getPlayer().setItemInHand(pContext.getHand(), itemInHand);
                                pContext.getLevel().addFreshEntity(ie);
                            } else {
                                itemTag.putInt("amount", remaining);
                                itemInHand.setTag(itemTag);
                            }
                        }
                        quad.setContents(slot, typeInSlot, amountInSlot + inserted);
                    } else if(mi != null) {
                        final int limitInSlot = quad.getStorageLimitIgnoreStoredTypes(mi);

                        int inserted = Math.min(limitInSlot, itemAmount);
                        int remaining = itemAmount - inserted;

                        if(spawnNew) {
                            ItemStack is = new ItemStack(BlockRegistry.MATERIA_JAR.get().asItem());

                            if(remaining > 0) {
                                CompoundTag nbt = new CompoundTag();
                                nbt.putString("type", mi.getMateriaName());
                                nbt.putInt("amount", remaining);
                                is.setTag(nbt);
                            }

                            ItemEntity ie = new ItemEntity(
                                    pContext.getLevel(),
                                    pContext.getPlayer().getX(),
                                    pContext.getPlayer().getY(),
                                    pContext.getPlayer().getZ(),
                                    is
                            );
                            pContext.getLevel().addFreshEntity(ie);

                            itemTag.putInt("amount", remaining);
                            itemInHand.setTag(itemTag);
                            pContext.getPlayer().setItemInHand(pContext.getHand(), itemInHand);
                        } else {
                            itemInHand.removeTagKey("type");
                            itemInHand.removeTagKey("amount");
                        }
                        quad.setContents(slot, mi, inserted);
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
                Component.translatable("tooltip.magichem.materia_jar")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }
}
