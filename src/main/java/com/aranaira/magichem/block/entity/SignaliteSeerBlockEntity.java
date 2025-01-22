package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.SignaliteBlock;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignaliteSeerBlockEntity extends BlockEntity {
    private static final int
        FLAG_NORTH = 1, FLAG_SOUTH = 2, FLAG_EAST = 4, FLAG_WEST = 8, FLAG_UP = 16, FLAG_DOWN = 32,
        FLAG_NORTH_SPECIAL = 64, FLAG_SOUTH_SPECIAL = 128, FLAG_EAST_SPECIAL = 256,
        FLAG_WEST_SPECIAL = 512, FLAG_UP_SPECIAL = 1024, FLAG_DOWN_SPECIAL = 2048;
    public boolean
        locked = false, hidden = false;
    public BlockPos remoteMonitoringPos = null;

    public SignaliteSeerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.SIGNALITE_SEER_BE.get(), pPos, pBlockState);
    }

    @Override
    public void setLevel(Level pLevel) {
        super.setLevel(pLevel);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void setMonitoringTarget(BlockPos pNewPos) {
        remoteMonitoringPos = pNewPos;

        updateSignalStrength();

        syncAndSave();
    }

    private void updateSignalStrength() {
        if(level != null && remoteMonitoringPos != null) {
            BlockState stateQuery = level.getBlockState(remoteMonitoringPos);
            if(stateQuery.getBlock().hasAnalogOutputSignal(stateQuery)) {
                int signal = stateQuery.getBlock().getAnalogOutputSignal(stateQuery, level, remoteMonitoringPos);
                if(signal != getBlockState().getValue(BlockStateProperties.POWER)) {
                    BlockState newState = getBlockState().setValue(BlockStateProperties.POWER, signal);
                    level.setBlock(getBlockPos(), newState, 3);
                }
            } else {
                if(0 != getBlockState().getValue(BlockStateProperties.POWER)) {
                    BlockState newState = getBlockState().setValue(BlockStateProperties.POWER, 0);
                    level.setBlock(getBlockPos(), newState, 3);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        if(remoteMonitoringPos != null)
            nbt.putLong("remoteMonitoringPos", remoteMonitoringPos.asLong());
        nbt.putBoolean("locked", locked);
        nbt.putBoolean("hidden", hidden);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        if(nbt.contains("remoteMonitoringPos"))
            remoteMonitoringPos = BlockPos.of(nbt.getLong("remoteMonitoringPos"));
        else
            remoteMonitoringPos = null;
        locked = nbt.getBoolean("locked");
        hidden = nbt.getBoolean("hidden");
        super.load(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        if(remoteMonitoringPos != null)
            nbt.putLong("remoteMonitoringPos", remoteMonitoringPos.asLong());
        nbt.putBoolean("locked", locked);
        nbt.putBoolean("hidden", hidden);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
    }

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, SignaliteSeerBlockEntity pEntity) {
        if(pEntity.remoteMonitoringPos != null) {
            pEntity.updateSignalStrength();
        }
    }
}
