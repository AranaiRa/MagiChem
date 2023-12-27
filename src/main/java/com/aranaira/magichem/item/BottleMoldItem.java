package com.aranaira.magichem.item;

import com.mna.items.runes.ItemRunePattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class BottleMoldItem extends ItemRunePattern {
    private static final int max_uses = 257;

    @Override
    public int getBarColor(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("uses")) {
            float f = Math.max(0.0F, ((float)max_uses - (float)stack.getTag().getInt("uses")) / (float)max_uses);
            return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
        } else {
            return Mth.hsvToRgb(0.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("uses") ? 13 - Math.round(13.0F * ((float)stack.getTag().getInt("uses") / (float)max_uses)) : 0;
    }

    public static boolean incrementDamage(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        int uses = nbt.getInt("uses") + 1;
        nbt.putInt("uses", uses);
        return uses == max_uses;
    }
}
