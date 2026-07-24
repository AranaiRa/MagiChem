package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.registry.MateriaRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Random;

import static com.aranaira.magichem.registry.MateriaRegistry.NIGREDO;
import static com.aranaira.magichem.registry.MateriaRegistry.ALBEDO;
import static com.aranaira.magichem.registry.MateriaRegistry.CITRINITAS;
import static com.aranaira.magichem.registry.MateriaRegistry.RUBEDO;

public abstract class AbstractBlockEntityWithEfficiency extends BlockEntity {
    protected int efficiencyMod;
    protected float operationTimeMod;
    protected boolean isStalled = false;
    protected static final Random r = new Random();

    public AbstractBlockEntityWithEfficiency(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public static Triplet<Integer, NonNullList<ItemStack>, Integer> applyEfficiencyToCraftingResult(NonNullList<ItemStack> query, int efficiency, float outputRate, int grimeSuccess, int grimeFail) {
        int grime = 0;
        int crafted = 0;
        if(efficiency < 100 || outputRate < 1.0f) {
            ArrayList<ItemStack> modifiableQuery = new ArrayList<>();
            for (ItemStack stack : query) {
                modifiableQuery.add(new ItemStack(stack.getItem(), stack.getCount()));
            }

            NonNullList<ItemStack> output = NonNullList.create();
            for (ItemStack stack : modifiableQuery) {
                int count = stack.getCount();
                for (int i = 0; i < count; i++) {
                    boolean doShrink = false;
                    int adjustedEfficiency = efficiency;
                    if(stack.getItem() == NIGREDO.get() || stack.getItem() == ALBEDO.get() || stack.getItem() == CITRINITAS.get() || stack.getItem() == RUBEDO.get())
                        adjustedEfficiency = efficiency + Math.min(100, Math.round((100 - efficiency) * (float) ServerConfig.houseOfAlchemyDistillationEfficiencyBonus / 100f));

                    if(adjustedEfficiency < 100 || outputRate < 1.0f) {
                        if (r.nextInt(100) > adjustedEfficiency)
                            doShrink = true;
                        else if (outputRate < 1.0) {
                            if (r.nextFloat() > outputRate)
                                doShrink = true;
                        }
                    }

                    if(doShrink) {
                        stack.shrink(1);
                        grime += grimeFail;
                    } else {
                        crafted++;
                        grime += grimeSuccess;
                    }
                }
            }

            for(ItemStack stack : modifiableQuery) {
                if(stack.getCount() > 0)
                    output.add(stack);
            }
            return new Triplet<>(grime, output, crafted);
        }
        else {
            int count = 0;
            for(ItemStack stack : query) {
                count += stack.getCount();
            }
            return new Triplet<>(count*grimeSuccess, query, count);
        }
    }

    public abstract int getMaximumGrime();

    public abstract int clean();
}
