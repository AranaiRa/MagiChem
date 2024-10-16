package com.aranaira.magichem.events;

import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CommonEventHelper {
    public static void generateWasteFromCleanedApparatus(Player player, Level level, AbstractBlockEntityWithEfficiency bewe, @Nullable ItemStack stackToDamage) {
        int wasteCount = bewe.clean();
        if(wasteCount > 0 && stackToDamage != null) {
            if(player != null) {
                if (!player.isCreative()) {
                    stackToDamage.setDamageValue(stackToDamage.getDamageValue() + 1);
                    if(stackToDamage.getDamageValue() >= stackToDamage.getMaxDamage()) {
                        stackToDamage.shrink(1);
                    }
                }
            }
        }

        int slots = (wasteCount / 64) + 1;
        SimpleContainer wasteItems = new SimpleContainer(slots);
        for (int i = 0; i < slots; i++) {
            int thisAmount = Math.min(wasteCount, 64);
            wasteItems.setItem(i, new ItemStack(ItemRegistry.ALCHEMICAL_WASTE.get(), thisAmount));
            wasteCount -= thisAmount;
            if(wasteCount <= 0)
                break;
        }

        Containers.dropContents(level, bewe.getBlockPos(), wasteItems);
    }

    public static boolean checkDirectionAndPos(DevicePlugDirection pPlugDir, BlockHitResult pBHR) {
        Direction dir = pBHR.getDirection();
        boolean plugCheck = false, extentsCheck = false;

        final Vec3 location = pBHR.getLocation().subtract(pBHR.getBlockPos().getX(), pBHR.getBlockPos().getY(), pBHR.getBlockPos().getZ());

        if(dir == Direction.NORTH) {
            plugCheck = pPlugDir == DevicePlugDirection.NORTH;
            extentsCheck = location.z == 0;
        }
        else if(dir == Direction.EAST) {
            plugCheck = pPlugDir == DevicePlugDirection.EAST;
            extentsCheck = location.x == 1;
        }
        else if(dir == Direction.SOUTH) {
            plugCheck = pPlugDir == DevicePlugDirection.SOUTH;
            extentsCheck = location.z == 1;
        }
        else if(dir == Direction.WEST) {
            plugCheck = pPlugDir == DevicePlugDirection.WEST;
            extentsCheck = location.x == 0;
        }

        return plugCheck && extentsCheck;
    }

    public static MutableComponent getTimeOfDayComponent(float pTime) {
        if(pTime >= 0.95 || pTime <= 0.05) return Component.translatable("gui.magichem.time.noon");
        else if(pTime < 0.20) return Component.translatable("gui.magichem.time.day");
        else if(pTime <= 0.30) return Component.translatable("gui.magichem.time.dusk");
        else if(pTime < 0.45) return Component.translatable("gui.magichem.time.evening");
        else if(pTime <= 0.55) return Component.translatable("gui.magichem.time.midnight");
        else if(pTime < 0.70) return Component.translatable("gui.magichem.time.night");
        else if(pTime <= 0.80) return Component.translatable("gui.magichem.time.dawn");
        else if(pTime < 0.95) return Component.translatable("gui.magichem.time.morning");
        else
            return Component.empty();
    }
}
