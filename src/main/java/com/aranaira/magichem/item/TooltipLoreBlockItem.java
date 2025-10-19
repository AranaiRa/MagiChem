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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipLoreBlockItem extends BlockItem {

    public TooltipLoreBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(pStack.getItem() == BlockRegistry.ACTUATOR_WATER.get().asItem()) {
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
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_ENDER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.ender")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_NEUTRAL.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.neutral")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_AGGREGATING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.variant")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.aggregating")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_BURNISHING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.variant")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.burnishing")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_CHAOTIC.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.variant")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.chaotic")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_DEVOURING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.variant")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.devouring")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_EQUATING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.variant")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.equating")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_GATEKEEPING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.variant")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.gatekeeping")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_METICULOUS.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.variant")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.meticulous")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_NEGATING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.variant")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.negating")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_SEER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.device")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.seer.line1")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.seer.line2")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_SINGING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.device")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.singing.line1")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.singing.line2")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.SIGNALITE_LISTENING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.device")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.listening.line1")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.listening.line2")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_WOOD.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_WOOD.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_SILVER.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_SILVER.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_ELECTRUM.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_ELECTRUM.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_GOLD.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_GOLD.get().asItem()
        ) {
            if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_WOOD.get().asItem() ||
               pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_WOOD.get().asItem()) {
                pTooltipComponents.add(
                        Component.translatable("tooltip.magichem.alchemically_treated_glass.wood")
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
            else if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_SILVER.get().asItem() ||
               pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_SILVER.get().asItem()) {
                pTooltipComponents.add(
                        Component.translatable("tooltip.magichem.alchemically_treated_glass.silver")
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
            else if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_ELECTRUM.get().asItem() ||
               pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_ELECTRUM.get().asItem()) {
                pTooltipComponents.add(
                        Component.translatable("tooltip.magichem.alchemically_treated_glass.electrum")
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
            else if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_GOLD.get().asItem() ||
               pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_GOLD.get().asItem()) {
                pTooltipComponents.add(
                        Component.translatable("tooltip.magichem.alchemically_treated_glass.gold")
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.alchemically_treated_glass")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.MAGIC_MIRROR.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.magic_mirror.line1")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.magic_mirror.line2")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.MAGICHEMICAL_MECHANISM.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.magichemical_mechanism")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.magichemical_mechanism.ext")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem."+this.toString())
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
                        if (nbt.contains("powerLevel")) {
                            anbe.setPowerUsageSetting(nbt.getInt("powerLevel"));
                        }
                        if (nbt.contains("slurry")) {
                            anbe.unpackSlurryFromNBT(nbt);
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
                } else if (cachedItem == BlockRegistry.GRAND_FUSERY.get().asItem()) {
                    GrandFuseryBlockEntity gfbe = (GrandFuseryBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (gfbe != null) {
                        if (nbt.contains("inventory")) {
                            gfbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("grime")) {
                            GrimeProvider.getCapability(gfbe).setGrime(nbt.getInt("grime"));
                        }
                        if (nbt.contains("slurry")) {
                            gfbe.setSlurryLevel(nbt.getInt("slurry"));
                        }
                        if (nbt.contains("powerUsageSetting")) {
                            gfbe.setPowerUsageSetting(nbt.getInt("powerUsageSetting"));
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
                } else if (cachedItem == BlockRegistry.MIRROR_LABYRINTH.get().asItem()) {
                    MirrorLabyrinthBlockEntity mlbe = (MirrorLabyrinthBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (mlbe != null) {
                        if (nbt.contains("inventory")) {
                            mlbe.unpackInventoryFromNBT((CompoundTag) nbt.get("inventory"));
                        }
                        if (nbt.contains("powerUsageSetting")) {
                            mlbe.setPowerUsageSetting(nbt.getInt("powerUsageSetting"));
                        }
                        if (nbt.contains("materiaStorage")) {
                            mlbe.unpackMateriaStorageFromTag(nbt.getCompound("materiaStorage"));
                        }
                    }
                } else if (cachedItem == BlockRegistry.ACID_BASIN.get().asItem()) {
                    AcidBasinBlockEntity abbe = (AcidBasinBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (abbe != null) {
                        if(nbt.contains("inventory"))
                            abbe.load(nbt);
                    }
                } else if (cachedItem == BlockRegistry.ELDRIN_ORRERY.get().asItem()) {
                    EldrinOrreryBlockEntity eobe = (EldrinOrreryBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (eobe != null) {
                        if(nbt.contains("inventory"))
                            eobe.unpackInventoryFromNBT(nbt);
                    }
                } else if (cachedItem == BlockRegistry.SKYWRATH_CONDENSER.get().asItem()) {
                    SkywrathCondenserBlockEntity condenser = (SkywrathCondenserBlockEntity) pContext.getLevel().getBlockEntity(pContext.getClickedPos());
                    if (condenser != null) {
                        condenser.unpackInventoryFromNBT(nbt);
                    }
                }
            }
            return result;
        }
        return super.place(pContext);
    }
}
