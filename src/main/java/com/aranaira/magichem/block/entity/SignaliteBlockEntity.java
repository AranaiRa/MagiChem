package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.SignaliteBlock;
import com.aranaira.magichem.block.SignaliteBlock.SignaliteBlockType;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.block.SignaliteBlock.SignaliteBlockType.STANDARD;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.VERTICAL_CRYSTAL_SHAPE_TYPE;

public class SignaliteBlockEntity extends BlockEntity {
    private static final int
        FLAG_NORTH = 1, FLAG_SOUTH = 2, FLAG_EAST = 4, FLAG_WEST = 8, FLAG_UP = 16, FLAG_DOWN = 32;
    public boolean
        connectedNorth = true, connectedSouth = true,
        connectedEast = true, connectedWest = true,
        connectedUp = true, connectedDown = true;

    public SignaliteBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.SIGNALITE_BE.get(), pPos, pBlockState);
    }

    @Override
    public void setLevel(Level pLevel) {
        super.setLevel(pLevel);
    }

    public void toggle(Direction pDir) {
        if(pDir == Direction.NORTH) connectedNorth = !connectedNorth;
        else if(pDir == Direction.SOUTH) connectedSouth = !connectedSouth;
        else if(pDir == Direction.EAST) connectedEast = !connectedEast;
        else if(pDir == Direction.WEST) connectedWest = !connectedWest;
        else if(pDir == Direction.UP) connectedUp = !connectedUp;
        else if(pDir == Direction.DOWN) connectedDown = !connectedDown;
        syncAndSave();

        if(level != null) {
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
    }

    public List<Direction> getTransmittingDirections() {
        List<Direction> out = new ArrayList<>();

        if(connectedNorth) out.add(Direction.NORTH);
        if(connectedSouth) out.add(Direction.SOUTH);
        if(connectedEast) out.add(Direction.EAST);
        if(connectedWest) out.add(Direction.WEST);
        if(connectedUp) out.add(Direction.UP);
        if(connectedDown) out.add(Direction.DOWN);

        return out;
    }

    public boolean acceptsSignalFromDirection(Direction pDir) {
        if(pDir == Direction.NORTH) return connectedSouth;
        else if(pDir == Direction.SOUTH) return connectedNorth;
        else if(pDir == Direction.EAST) return connectedWest;
        else if(pDir == Direction.WEST) return connectedEast;
        else if(pDir == Direction.UP) return connectedDown;
        else if(pDir == Direction.DOWN) return connectedUp;
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putInt("connectionFlags", packConnectionsToInt());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        unpackConnectionFromInt(nbt.getInt("connectionFlags"));
        super.load(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("connectionFlags", packConnectionsToInt());
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    private int packConnectionsToInt() {
        int out = 0;
        if(connectedNorth)
            out = out | FLAG_NORTH;
        if(connectedSouth)
            out = out | FLAG_SOUTH;
        if(connectedEast)
            out = out | FLAG_EAST;
        if(connectedWest)
            out = out | FLAG_WEST;
        if(connectedUp)
            out = out | FLAG_UP;
        if(connectedDown)
            out = out | FLAG_DOWN;
        return out;
    }

    private void unpackConnectionFromInt(int pPackedBooleans) {
        connectedNorth = (pPackedBooleans & FLAG_NORTH) == FLAG_NORTH;
        connectedSouth = (pPackedBooleans & FLAG_SOUTH) == FLAG_SOUTH;
        connectedEast = (pPackedBooleans & FLAG_EAST) == FLAG_EAST;
        connectedWest = (pPackedBooleans & FLAG_WEST) == FLAG_WEST;
        connectedUp = (pPackedBooleans & FLAG_UP) == FLAG_UP;
        connectedDown = (pPackedBooleans & FLAG_DOWN) == FLAG_DOWN;
    }
}
