package com.aranaira.magichem.foundation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.UUID;

public abstract class DirectionalPluginBlockEntity extends BlockEntity {
    protected Player owner;
    protected UUID ownerUUID;

    public DirectionalPluginBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
        this.saveAdditional(this.getUpdateTag());
    }

    public Player getOwner() {
        if(owner != null) return owner;
        else if(ownerUUID != null && getLevel() != null) {
            return getLevel().getPlayerByUUID(ownerUUID);
        }
        return null;
    }

    public ICanTakePlugins getTargetMachine() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(facing));
        if(be != null) {
            if (be instanceof ICanTakePlugins ictp) return ictp;
        }
        return null;
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public abstract void processCompletedOperation(int pCyclesCompleted);
}
