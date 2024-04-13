package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class ExperienceExchangerBlockEntity extends BlockEntity {
    private ItemStack containedCrystal = ItemStack.EMPTY;
    private boolean isPushMode = false;
    public float ringRotation, ringRotationNextTick, crystalRotation, crystalRotationNextTick, crystalBob, crystalBobNextTick;

    private static final int
        RING_ROTATION_PERIOD = 160, CRYSTAL_ROTATION_PERIOD = 300, CRYSTAL_BOB_PERIOD = 200;
    private static final float
        CRYSTAL_BOB_INTENSITY = 0.1f;

    public ExperienceExchangerBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public ExperienceExchangerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.EXPERIENCE_EXCHANGER_BE.get(), pPos, pBlockState);
    }

    public ItemStack setContainedStack(ItemStack pNewStack) {
        if(pNewStack.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get()) {
            containedCrystal = pNewStack;
            CompoundTag tag = pNewStack.getTag();
            if(tag != null) {
                if(tag.contains("mode")) {
                    int mode = tag.getInt("mode");
                    isPushMode = mode == 1;
                }
            } else {
                isPushMode = false;
            }
            return ItemStack.EMPTY;
        }
        return pNewStack;
    }

    public void ejectStack() {
        if(containedCrystal != ItemStack.EMPTY) {
            level.addFreshEntity(new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), containedCrystal));
            containedCrystal = ItemStack.EMPTY;
        }
    }

    public static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, T t) {
        if(t instanceof ExperienceExchangerBlockEntity eebe) {
            if(pLevel.isClientSide()) {
                handleAnimationDrivers(eebe);
            }

            if(eebe.containedCrystal == ItemStack.EMPTY) return;

            //make a connection with the block below
        }
    }

    private static void handleAnimationDrivers(ExperienceExchangerBlockEntity pBlockEntity) {
        long gameTime = pBlockEntity.level.getGameTime();

        pBlockEntity.ringRotation = ((float)(gameTime % RING_ROTATION_PERIOD) / RING_ROTATION_PERIOD) * (float)Math.PI * 2;
        pBlockEntity.ringRotationNextTick = ((float)((gameTime + 1) % RING_ROTATION_PERIOD) / RING_ROTATION_PERIOD) * (float)Math.PI * 2;
        if((gameTime + 1) % RING_ROTATION_PERIOD == 0)
            pBlockEntity.ringRotationNextTick += Math.PI * 2;

        pBlockEntity.crystalRotation = ((float)(gameTime % CRYSTAL_ROTATION_PERIOD) / CRYSTAL_ROTATION_PERIOD) * (float)Math.PI * 2;
        pBlockEntity.crystalRotationNextTick = ((float)((gameTime + 1) % CRYSTAL_ROTATION_PERIOD) / CRYSTAL_ROTATION_PERIOD) * (float)Math.PI * 2;
        if((gameTime + 1) % CRYSTAL_ROTATION_PERIOD == 0)
            pBlockEntity.crystalRotationNextTick += Math.PI * 2;

        pBlockEntity.crystalBob = (float)Math.sin(((float)(gameTime % CRYSTAL_BOB_PERIOD) / CRYSTAL_BOB_PERIOD) * (float)Math.PI * 2) * CRYSTAL_BOB_INTENSITY;
        pBlockEntity.crystalBobNextTick = (float)Math.sin(((float)((gameTime + 1) % CRYSTAL_BOB_PERIOD) / CRYSTAL_BOB_PERIOD) * (float)Math.PI * 2) * CRYSTAL_BOB_INTENSITY;
    }
}
