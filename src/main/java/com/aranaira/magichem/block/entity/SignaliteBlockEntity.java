package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.SignaliteBlock;
import com.aranaira.magichem.block.SignaliteBlock.SignaliteBlockType;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.aranaira.magichem.block.SignaliteBlock.SignaliteBlockType.STANDARD;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.VERTICAL_CRYSTAL_SHAPE_TYPE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER;

public class SignaliteBlockEntity extends BlockEntity {
    private static final int
        FLAG_NORTH = 1, FLAG_SOUTH = 2, FLAG_EAST = 4, FLAG_WEST = 8, FLAG_UP = 16, FLAG_DOWN = 32,
        FLAG_NORTH_SPECIAL = 64, FLAG_SOUTH_SPECIAL = 128, FLAG_EAST_SPECIAL = 256,
        FLAG_WEST_SPECIAL = 512, FLAG_UP_SPECIAL = 1024, FLAG_DOWN_SPECIAL = 2048;
    public boolean
        connectedNorth = true, connectedSouth = true,
        connectedEast = true, connectedWest = true,
        connectedUp = true, connectedDown = true,
        specialNorth = false, specialSouth = false,
        specialEast = false, specialWest = false,
        specialUp = false, specialDown = false,
        locked = false, hidden = false;
    public int specialSignalStrength = 0;
    public int specialSignalTarget = 0;
    private final int[] lastInputSignals = new int[6];

    public SignaliteBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.SIGNALITE_BE.get(), pPos, pBlockState);
    }

    @Override
    public void setLevel(Level pLevel) {
        super.setLevel(pLevel);
    }

    public void toggle(Direction pDir) {
        if(getBlockState().getBlock() instanceof SignaliteBlock sb) {

            if (sb.getType() == STANDARD) {
                if (pDir == Direction.NORTH) connectedNorth = !connectedNorth;
                else if (pDir == Direction.SOUTH) connectedSouth = !connectedSouth;
                else if (pDir == Direction.EAST) connectedEast = !connectedEast;
                else if (pDir == Direction.WEST) connectedWest = !connectedWest;
                else if (pDir == Direction.DOWN) connectedUp = !connectedUp;
                else if (pDir == Direction.UP) connectedDown = !connectedDown;
                syncAndSave();
            }
            else {
                if (pDir == Direction.NORTH) {
                    if(!connectedNorth && !specialNorth) connectedNorth = true;
                    else if(specialNorth) specialNorth = false;
                    else {
                        specialNorth = true;
                        connectedNorth = false;
                    }
                }
                else if (pDir == Direction.SOUTH) {
                    if(!connectedSouth && !specialSouth) connectedSouth = true;
                    else if(specialSouth) specialSouth = false;
                    else {
                        specialSouth = true;
                        connectedSouth = false;
                    }
                }
                else if (pDir == Direction.EAST) {
                    if(!connectedEast && !specialEast) connectedEast = true;
                    else if(specialEast) specialEast = false;
                    else {
                        specialEast = true;
                        connectedEast = false;
                    }
                }
                else if (pDir == Direction.WEST) {
                    if(!connectedWest && !specialWest) connectedWest = true;
                    else if(specialWest) specialWest = false;
                    else {
                        specialWest = true;
                        connectedWest = false;
                    }
                }
                else if (pDir == Direction.DOWN) {
                    if(!connectedUp && !specialUp) connectedUp = true;
                    else if(specialUp) specialUp = false;
                    else {
                        specialUp = true;
                        connectedUp = false;
                    }
                }
                else if (pDir == Direction.UP) {
                    if(!connectedDown && !specialDown) connectedDown = true;
                    else if(specialDown) specialDown = false;
                    else {
                        specialDown = true;
                        connectedDown = false;
                    }
                }
                syncAndSave();
            }
        }

        if(level != null) {
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
    }

    public List<Direction> getTransmittingDirections() {
        List<Direction> out = new ArrayList<>();

        if(connectedNorth || specialNorth) out.add(Direction.NORTH);
        if(connectedSouth || specialSouth) out.add(Direction.SOUTH);
        if(connectedEast || specialEast) out.add(Direction.EAST);
        if(connectedWest || specialWest) out.add(Direction.WEST);
        if(connectedUp || specialUp) out.add(Direction.UP);
        if(connectedDown || specialDown) out.add(Direction.DOWN);

        return out;
    }

    public boolean isTransmittingDirectionOneWay(Direction pDir) {
        if(pDir == Direction.NORTH && specialNorth) return true;
        else if(pDir == Direction.SOUTH && specialSouth) return true;
        else if(pDir == Direction.EAST && specialEast) return true;
        else if(pDir == Direction.WEST && specialWest) return true;
        else if(pDir == Direction.UP && specialUp) return true;
        else if(pDir == Direction.DOWN && specialDown) return true;
        return false;
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
        nbt.putByte("signalTarget", (byte)(specialSignalTarget & 0x11111111));
        nbt.putBoolean("locked", locked);
        nbt.putBoolean("hidden", hidden);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        unpackConnectionFromInt(nbt.getInt("connectionFlags"));
        specialSignalTarget = nbt.getByte("signalTarget");
        locked = nbt.getBoolean("locked");
        hidden = nbt.getBoolean("hidden");
        super.load(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("connectionFlags", packConnectionsToInt());
        nbt.putByte("signalTarget", (byte)(specialSignalTarget & 0x11111111));
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
        if(specialNorth)
            out = out | FLAG_NORTH_SPECIAL;
        if(specialSouth)
            out = out | FLAG_SOUTH_SPECIAL;
        if(specialEast)
            out = out | FLAG_EAST_SPECIAL;
        if(specialWest)
            out = out | FLAG_WEST_SPECIAL;
        if(specialUp)
            out = out | FLAG_UP_SPECIAL;
        if(specialDown)
            out = out | FLAG_DOWN_SPECIAL;
        return out;
    }

    private void unpackConnectionFromInt(int pPackedBooleans) {
        connectedNorth = (pPackedBooleans & FLAG_NORTH) == FLAG_NORTH;
        connectedSouth = (pPackedBooleans & FLAG_SOUTH) == FLAG_SOUTH;
        connectedEast = (pPackedBooleans & FLAG_EAST) == FLAG_EAST;
        connectedWest = (pPackedBooleans & FLAG_WEST) == FLAG_WEST;
        connectedUp = (pPackedBooleans & FLAG_UP) == FLAG_UP;
        connectedDown = (pPackedBooleans & FLAG_DOWN) == FLAG_DOWN;

        specialNorth = (pPackedBooleans & FLAG_NORTH_SPECIAL) == FLAG_NORTH_SPECIAL;
        specialSouth = (pPackedBooleans & FLAG_SOUTH_SPECIAL) == FLAG_SOUTH_SPECIAL;
        specialEast = (pPackedBooleans & FLAG_EAST_SPECIAL) == FLAG_EAST_SPECIAL;
        specialWest = (pPackedBooleans & FLAG_WEST_SPECIAL) == FLAG_WEST_SPECIAL;
        specialUp = (pPackedBooleans & FLAG_UP_SPECIAL) == FLAG_UP_SPECIAL;
        specialDown = (pPackedBooleans & FLAG_DOWN_SPECIAL) == FLAG_DOWN_SPECIAL;
    }

    public void clearLastInputSignals() {
        Arrays.fill(lastInputSignals, 0);
    }

    public void setLastInputByDirection(Direction pDir, int pVal) {
        if(pDir == Direction.NORTH) lastInputSignals[0] = pVal;
        else if(pDir == Direction.SOUTH) lastInputSignals[1] = pVal;
        else if(pDir == Direction.EAST) lastInputSignals[2] = pVal;
        else if(pDir == Direction.WEST) lastInputSignals[3] = pVal;
        else if(pDir == Direction.UP) lastInputSignals[4] = pVal;
        else if(pDir == Direction.DOWN) lastInputSignals[5] = pVal;
    }

    public int getLastInputAverage() {
        float out = 0;

        for (int signal : lastInputSignals) {
            out += signal;
        }

        float count = 0;
        count += connectedNorth ? 1 : 0;
        count += connectedSouth ? 1 : 0;
        count += connectedEast ? 1 : 0;
        count += connectedWest ? 1 : 0;
        count += connectedUp ? 1 : 0;
        count += connectedDown ? 1 : 0;

        return count == 0 ? 0 : Math.round(out / count);
    }

    public int getLastInputSum() {
        int out = 0;

        for (int signal : lastInputSignals) {
            out += signal;
        }

        return Math.min(out, 15);
    }

    public void incrementSpecialSignalSetting() {
        specialSignalTarget = specialSignalTarget == 15 ? 0 : specialSignalTarget + 1;
        syncAndSave();
    }

    public void decrementSpecialSignalSetting() {
        specialSignalTarget = specialSignalTarget == 0 ? 15 : specialSignalTarget - 1;
        syncAndSave();
    }
}
