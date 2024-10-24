package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
        else if(pStack.getItem() == BlockRegistry.CIRCLE_TOIL.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.circletoil")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.GRAND_CIRCLE_FABRICATION.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.grandcirclefabrication")
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
                    Component.translatable("tooltip.magichem.granddistillery")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.GRAND_CENTRIFUGE.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.grandcentrifuge")
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
        else if(pStack.getItem() == BlockRegistry.CONJURER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.conjurer")
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
        else if(pStack.getItem() == BlockRegistry.CRYSTAL_CANDLE.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.crystalcandle")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        ItemStack stack = pContext.getItemInHand();

        if(stack.hasTag()) {
            CompoundTag nbt = stack.getOrCreateTag();
            Item cachedItem = stack.getItem();
            InteractionResult result = super.place(pContext);

            if (result != InteractionResult.FAIL) {
                if (cachedItem == BlockRegistry.ALEMBIC.get().asItem()) {
                    AlembicBlockEntity abe = (AlembicBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (abe != null) {
                        if (nbt.contains("inventory")) {
                            abe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("grime")) {
                            GrimeProvider.getCapability(abe).setGrime(nbt.getInt("grime"));
                        }
                    }
                } else if (cachedItem == BlockRegistry.CENTRIFUGE.get().asItem()) {
                    CentrifugeBlockEntity cbe = (CentrifugeBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (cbe != null) {
                        if (nbt.contains("inventory")) {
                            cbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("grime")) {
                            GrimeProvider.getCapability(cbe).setGrime(nbt.getInt("grime"));
                        }
                    }
                } else if (cachedItem == BlockRegistry.DISTILLERY.get().asItem()) {
                    DistilleryBlockEntity dbe = (DistilleryBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (dbe != null) {
                        if (nbt.contains("inventory")) {
                            dbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("grime")) {
                            GrimeProvider.getCapability(dbe).setGrime(nbt.getInt("grime"));
                        }
                    }
                } else if (cachedItem == BlockRegistry.FUSERY.get().asItem()) {
                    FuseryBlockEntity abe = (FuseryBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (abe != null) {
                        if (nbt.contains("inventory")) {
                            abe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("grime")) {
                            GrimeProvider.getCapability(abe).setGrime(nbt.getInt("grime"));
                        }
                    }
                } else if (cachedItem == BlockRegistry.ALCHEMICAL_NEXUS.get().asItem()) {
                    AlchemicalNexusBlockEntity anbe = (AlchemicalNexusBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (anbe != null) {
                        if (nbt.contains("inventory")) {
                            anbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                    }
                } else if (cachedItem == BlockRegistry.GRAND_DISTILLERY.get().asItem()) {
                    GrandDistilleryBlockEntity gdbe = (GrandDistilleryBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (gdbe != null) {
                        if (nbt.contains("inventory")) {
                            gdbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("grime")) {
                            GrimeProvider.getCapability(gdbe).setGrime(nbt.getInt("grime"));
                        }
                        if (nbt.contains("powerUsageSetting")) {
                            gdbe.setPowerUsageSetting(nbt.getInt("powerUsageSetting"));
                        }
                    }
                } else if (cachedItem == BlockRegistry.GRAND_CENTRIFUGE.get().asItem()) {
                    GrandCentrifugeBlockEntity gdbe = (GrandCentrifugeBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (gdbe != null) {
                        if (nbt.contains("inventory")) {
                            gdbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("grime")) {
                            GrimeProvider.getCapability(gdbe).setGrime(nbt.getInt("grime"));
                        }
                        if (nbt.contains("powerUsageSetting")) {
                            gdbe.setPowerUsageSetting(nbt.getInt("powerUsageSetting"));
                        }
                    }
                } else if (cachedItem == BlockRegistry.VARIEGATOR.get().asItem()) {
                    VariegatorBlockEntity vbe = (VariegatorBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (vbe != null) {
                        if (nbt.contains("inventory")) {
                            vbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("colors")) {
                            vbe.unpackColorsFromCompoundTag(nbt.getCompound("colors"));
                        }
                    }
                } else if (cachedItem == BlockRegistry.CONJURER.get().asItem()) {
                    ConjurerBlockEntity cbe = (ConjurerBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (cbe != null) {
                        cbe.unpackInventoryFromNBT(nbt);
                    }
                } else if (cachedItem == BlockRegistry.GRAND_CIRCLE_FABRICATION.get().asItem()) {
                    GrandCircleFabricationBlockEntity gcfbe = (GrandCircleFabricationBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (gcfbe != null) {
                        if (nbt.contains("inventory")) {
                            gcfbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("powerUsageSetting")) {
                            gcfbe.setPowerUsageSetting(nbt.getInt("powerUsageSetting"));
                        }
                    }
                }
            }
            return result;
        }
        return super.place(pContext);
    }
}
