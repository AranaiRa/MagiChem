package com.aranaira.magichem.events.compat;

import net.joefoxe.hexerei.item.ModItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class HexereiEventHelper {
    public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        BlockState targetState = event.getLevel().getBlockState(event.getPos());
        BlockEntity targetEntity = event.getLevel().getBlockEntity(event.getPos());

        if(targetEntity != null) {
            targetEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(cap -> {
                final FluidStack tank = cap.getFluidInTank(0);
                if(stack.getItem() == Items.GLASS_BOTTLE && tank.getFluid() == ForgeMod.MILK.get()) {
                    if(tank.getAmount() >= 250) {
                        cap.drain(250, IFluidHandler.FluidAction.EXECUTE);
                        stack.shrink(1);
                        ItemEntity ie = new ItemEntity(event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(ModItems.MILK_BOTTLE.get()));
                        event.getLevel().addFreshEntity(ie);
                        event.setCanceled(true);
                    }
                } else if(stack.getItem() == ModItems.MILK_BOTTLE.get()) {
                    if(tank.isEmpty() || tank.getFluid() == ForgeMod.MILK.get()) {
                        if(tank.getAmount() <= cap.getTankCapacity(0) - 250) {
                            cap.fill(new FluidStack(ForgeMod.MILK.get(), 250), IFluidHandler.FluidAction.EXECUTE);
                            stack.shrink(1);
                            ItemEntity ie = new ItemEntity(event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(Items.GLASS_BOTTLE));
                            event.getLevel().addFreshEntity(ie);
                            event.setCanceled(true);
                        }
                    }
                }
            });
        }
    }
}
