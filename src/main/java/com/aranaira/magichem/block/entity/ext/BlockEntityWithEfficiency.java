package com.aranaira.magichem.block.entity.ext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class BlockEntityWithEfficiency extends BlockEntity {
    public static int baseEfficiency;
    protected int efficiencyMod, modTimeRemaining, modTimeUntilBoostable;
    protected boolean isStalled = false;
    protected static final Random r = new Random();

    public BlockEntityWithEfficiency(BlockEntityType<?> blockEntityType, BlockPos blockPos, int efficiency, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        baseEfficiency = efficiency;
    }

    public static NonNullList<ItemStack> applyEfficiencyToCraftingResult(NonNullList<ItemStack> query, int efficiency) {
        //TODO: roll this sucka
        return query;
    }
}
