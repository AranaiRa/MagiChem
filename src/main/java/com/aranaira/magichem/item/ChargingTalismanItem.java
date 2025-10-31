package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.gui.ChargingTalismanMenu;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import java.util.List;

public class ChargingTalismanItem extends Item implements ICurioItem {
    public ChargingTalismanItem(Properties pProperties) {
        super(pProperties);
    }

    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, @Nonnull InteractionHand pHand) {
        if (!pLevel.isClientSide && pHand == InteractionHand.MAIN_HAND) {
            NetworkHooks.openScreen((ServerPlayer)pPlayer, new SimpleMenuProvider(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pInternalPlayer) {
                    ItemStack itemInHand = pInternalPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                    int slot = pPlayerInventory.findSlotMatchingItem(itemInHand);

                    ContainerData data = new SimpleContainerData(1);
                    data.set(0, slot);

                    return new ChargingTalismanMenu(pContainerId, pPlayerInventory, data);
                }
            }, Component.empty()));
        }

        return InteractionResultHolder.success(pPlayer.getItemInHand(pHand));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.charging_talisman")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        ICurioItem.super.curioTick(slotContext, stack);

        if(!slotContext.entity().level().isClientSide() && stack.hasTag() && stack.getTag().contains("magichem.powerspike.targetpos")) {
            BlockPos posQuery = BlockPos.of(stack.getTag().getLong("magichem.powerspike.targetpos"));
            BlockEntity entityQuery = slotContext.entity().level().getBlockEntity(posQuery);

            if(entityQuery instanceof CirclePowerBlockEntity circle) {

                if (slotContext.entity() instanceof Player player) {
                    int remainingPower = tryChargeSlot(circle, player.getItemInHand(InteractionHand.MAIN_HAND));
                    if (remainingPower > 0) tryChargeSlot(circle, player.getItemInHand(InteractionHand.OFF_HAND));
                    for (ItemStack armorSlot : player.getArmorSlots()) {
                        if (remainingPower > 0) tryChargeSlot(circle, armorSlot);
                        if (remainingPower <= 0) break;
                    }
                }
            }
        }
    }

    private int tryChargeSlot(CirclePowerBlockEntity circle, ItemStack itemQuery) {
        MutableInt out = new MutableInt(-1);
        if (!itemQuery.isEmpty()) {
            itemQuery.getCapability(ForgeCapabilities.ENERGY).ifPresent(itemCap -> {
                circle.getCapability(ForgeCapabilities.ENERGY).ifPresent(circleCap -> {
                    int extractQuery = circleCap.extractEnergy(Integer.MAX_VALUE, true);
                    int actualInsertion = itemCap.receiveEnergy(extractQuery, false);
                    circleCap.extractEnergy(actualInsertion, false);
                    out.setValue(circleCap.getEnergyStored());
                });
            });
        }
        return out.intValue();
    }
}
