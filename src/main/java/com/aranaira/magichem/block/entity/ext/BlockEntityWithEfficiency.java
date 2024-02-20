package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.Config;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Random;

public abstract class BlockEntityWithEfficiency extends BlockEntity {
    public static int baseEfficiency;
    protected int efficiencyMod, grime;
    protected boolean isStalled = false;
    protected static final Random r = new Random();

    public BlockEntityWithEfficiency(BlockEntityType<?> blockEntityType, BlockPos blockPos, int efficiency, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        baseEfficiency = efficiency;
    }

    public static Pair<Integer, NonNullList<ItemStack>> applyEfficiencyToCraftingResult(NonNullList<ItemStack> query, int efficiency, float outputRate, int grimeSuccess, int grimeFail) {
        int grime = 0;
        if(efficiency < 100) {
            ArrayList<ItemStack> modifiableQuery = new ArrayList<>();
            for (ItemStack stack : query) {
                modifiableQuery.add(new ItemStack(stack.getItem(), stack.getCount()));
            }

            NonNullList<ItemStack> output = NonNullList.create();
            for (ItemStack stack : modifiableQuery) {
                int count = stack.getCount();
                for (int i = 0; i < count; i++) {
                    boolean doShrink = false;
                    if (r.nextInt(100) > efficiency)
                        doShrink = true;
                    else if (outputRate < 1.0) {
                        if(r.nextFloat() > outputRate)
                            doShrink = true;
                    }

                    if(doShrink) {
                        stack.shrink(1);
                        grime += grimeFail;
                    } else {
                        grime += grimeSuccess;
                    }
                }
            }

            for(ItemStack stack : modifiableQuery) {
                if(stack.getCount() > 0)
                    output.add(stack);
            }
            return new Pair<>(grime, output);
        }
        else {
            int count = 0;
            for(ItemStack stack : query) {
                count += stack.getCount();
            }
            return new Pair<>(count*grimeSuccess, query);
        }
    }

    public abstract int getGrimeFromData();

    public int getGrime() {
        return grime;
    }

    public abstract int getMaximumGrime();

    public abstract int clean();
}
