package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipLoreBlockItem extends BlockItem {

    public TooltipLoreBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(pStack.getItem() == BlockRegistry.CIRCLE_POWER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.circlepower")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.CIRCLE_FABRICATION.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.circlefabrication")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ALEMBIC.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.alembic")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.DISTILLERY.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.distillery")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.CENTRIFUGE.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.centrifuge")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.FUSERY.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.fusery")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ALCHEMICAL_NEXUS.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.alchemicalnexus")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.GRAND_DISTILLERY.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.grand_distillery")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_WATER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.water")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_FIRE.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.fire")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_EARTH.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.earth")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_AIR.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.air")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_ARCANE.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.arcane")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.COLORING_CAULDRON.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.coloringcauldron")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.VARIEGATOR.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.variegator")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.EXPERIENCE_EXCHANGER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.experienceexchanger")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.OCCULTED_CINDER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.occultedcinder")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.MAGICHEMICAL_MECHANISM.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.magichemicalmechanism")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.magichemicalmechanism.ext")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        InteractionResult result = super.place(pContext);

        if(result != InteractionResult.FAIL) {
            if (stack.getItem() == BlockRegistry.ALEMBIC.get().asItem()) {
                CompoundTag nbt = stack.getOrCreateTag();
                AlembicBlockEntity abe = (AlembicBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                if(abe != null) {
                    if (nbt.contains("inventory")) {
                        abe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                    }
                    if (nbt.contains("grime")) {
                        GrimeProvider.getCapability(abe).setGrime(nbt.getInt("grime"));
                    }
                }
            }
            else if (stack.getItem() == BlockRegistry.CENTRIFUGE.get().asItem()) {
                CompoundTag nbt = stack.getOrCreateTag();
                CentrifugeBlockEntity cbe = (CentrifugeBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                if(cbe != null) {
                    if (nbt.contains("inventory")) {
                        cbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                    }
                    if (nbt.contains("grime")) {
                        GrimeProvider.getCapability(cbe).setGrime(nbt.getInt("grime"));
                    }
                }
            }
            else if (stack.getItem() == BlockRegistry.DISTILLERY.get().asItem()) {
                CompoundTag nbt = stack.getOrCreateTag();
                DistilleryBlockEntity dbe = (DistilleryBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                if(dbe != null) {
                    if (nbt.contains("inventory")) {
                        dbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                    }
                    if (nbt.contains("grime")) {
                        GrimeProvider.getCapability(dbe).setGrime(nbt.getInt("grime"));
                    }
                }
            }
            else if (stack.getItem() == BlockRegistry.FUSERY.get().asItem()) {
                CompoundTag nbt = stack.getOrCreateTag();
                FuseryBlockEntity abe = (FuseryBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                if(abe != null) {
                    if (nbt.contains("inventory")) {
                        abe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                    }
                    if (nbt.contains("grime")) {
                        GrimeProvider.getCapability(abe).setGrime(nbt.getInt("grime"));
                    }
                }
            }
            else if (stack.getItem() == BlockRegistry.ALCHEMICAL_NEXUS.get().asItem()) {
                CompoundTag nbt = stack.getOrCreateTag();
                AlchemicalNexusBlockEntity anbe = (AlchemicalNexusBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                if(anbe != null) {
                    if (nbt.contains("inventory")) {
                        anbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                    }
                }
            }
            else if (stack.getItem() == BlockRegistry.GRAND_DISTILLERY.get().asItem()) {
                CompoundTag nbt = stack.getOrCreateTag();
                GrandDistilleryBlockEntity gdbe = (GrandDistilleryBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                if(gdbe != null) {
                    if (nbt.contains("inventory")) {
                        gdbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                    }
                    if (nbt.contains("grime")) {
                        GrimeProvider.getCapability(gdbe).setGrime(nbt.getInt("grime"));
                    }
                }
            }
        }
        return result;
    }
}
