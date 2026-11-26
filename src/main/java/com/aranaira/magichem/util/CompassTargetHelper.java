package com.aranaira.magichem.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CompassTargetHelper {
    // The point a compass shows: the true target MnA Proposed Fixes (mnapfixes) records inside LodestonePos,
    // when it is complete and understood, otherwise MnA's own X/Y/Z. hasHeight: y means something (a structure
    // target with an exact or approximate Y). approximate: that Y is an estimate.

    public record CompassPoint(int x, int y, int z, boolean hasHeight, boolean approximate) {}

    @Nullable 
    public static CompassPoint getPoint(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("LodestonePos", Tag.TAG_COMPOUND)) return null;
        CompoundTag lodestone = tag.getCompound("LodestonePos");

        if (lodestone.contains("mnapfixes_target", Tag.TAG_COMPOUND)) {
            CompoundTag target = lodestone.getCompound("mnapfixes_target");
            String yQuality = target.getString("YQuality");
            String source = target.getString("Source");

            boolean knownYQuality = yQuality.equals("exact") || yQuality.equals("approximate") || yQuality.equals("unknown");
            boolean knownSource = source.equals("structure_start") || source.equals("biome");
            boolean complete = target.contains("X", Tag.TAG_INT) && target.contains("Z", Tag.TAG_INT)
                    && (yQuality.equals("unknown") || target.contains("Y", Tag.TAG_INT));

            if (knownYQuality && knownSource && complete) {
                boolean hasHeight = source.equals("structure_start") && !yQuality.equals("unknown");
                return new CompassPoint(target.getInt("X"), target.getInt("Y"), target.getInt("Z"),
                        hasHeight, yQuality.equals("approximate"));
            }
        }
    
        return new CompassPoint(lodestone.getInt("X"), lodestone.getInt("Y"), lodestone.getInt("Z"), false, false);
    }    
}
