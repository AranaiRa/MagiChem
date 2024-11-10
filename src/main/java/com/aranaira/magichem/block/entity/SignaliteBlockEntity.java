package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SignaliteBlockEntity extends BlockEntity {
    public boolean
        north = true, south = true,
        east = true, west = true,
        up = true, down = true;

    public SignaliteBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.SIGNALITE_BE.get(), pPos, pBlockState);
    }

    public void toggle(Direction pDir) {
        if(pDir == Direction.NORTH) north = !north;
        else if(pDir == Direction.SOUTH) south = !south;
        else if(pDir == Direction.EAST) east = !east;
        else if(pDir == Direction.WEST) west = !west;
        else if(pDir == Direction.UP) up = !up;
        else if(pDir == Direction.DOWN) down = !down;
    }
}
