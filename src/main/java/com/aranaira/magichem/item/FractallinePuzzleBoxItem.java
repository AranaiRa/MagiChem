package com.aranaira.magichem.item;

import com.mna.api.items.IRelic;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class FractallinePuzzleBoxItem extends Item implements IRelic {
    private static final Random r = new Random();

    public FractallinePuzzleBoxItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(
                Component.empty().withStyle(ChatFormatting.GREEN)
                        .append(Component.translatable("tooltip.magichem.event.study.part1"))
                        .append(Component.literal("" + getExperienceGain(pStack)))
                        .append(Component.translatable("tooltip.magichem.event.study.part2.xp"))
                        .append(Component.translatable("tooltip.magichem.event.study.part3.unlimited"))
        );
        pTooltipComponents.add(
                Component.empty().withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("tooltip.magichem.fractalline_puzzle_box"))
        );
    }

    public static int getExperienceGain(ItemStack pStack) {
        int out = 4;

        if(pStack.getTag() != null && pStack.getTag().contains("solvedPuzzleLayers")) {
            out += pStack.getTag().getInt("solvedPuzzleLayers");
        }

        return out;
    }

    public static boolean trySolvePuzzle(ItemStack pStack) {
        int solvedPuzzles = 0;
        if(pStack.getTag() != null && pStack.getTag().contains("solvedPuzzleLayers")) {
            solvedPuzzles = pStack.getTag().getInt("solvedPuzzleLayers") + 1;
        }

        //Chance to solve a puzzle starts at 100%, reaches 50% at 4 solves, and 1% at 16 solves
        double chance = 1 - (Math.tanh(solvedPuzzles / 6d));
        if(r.nextFloat() <= chance) {
            CompoundTag nbt = new CompoundTag();
            if(pStack.hasTag()) nbt = pStack.getTag();

            nbt.putInt("solvedPuzzleLayers", solvedPuzzles);
            pStack.setTag(nbt);

            return true;
        }
        return false;
    }
}
