package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.foundation.IBlockWithPowerLevel;
import com.aranaira.magichem.gui.ActuatorWaterMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActuatorWaterBlockEntity extends BlockEntity implements MenuProvider, IBlockWithPowerLevel {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 67, 82, 100},
            WATER_PER_OPERATION = {0, 5, 15, 30, 50, 75, 105, 140, 180, 225, 275, 335, 410, 500},
            STEAM_PER_PROCESS = {0, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
            EFFICIENCY_INCREASE = {0, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40};
    public static final int
            TANK_ID_WATER = 0, TANK_ID_STEAM = 1,
            DATA_COUNT = 2, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1;
    private int
            powerLevel = 1,
            remainingEldrinTime;
    protected ContainerData data;
    private LazyOptional<IFluidHandler> fluidHandler;

    public ActuatorWaterBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public ActuatorWaterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_WATER_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch(pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorWaterBlockEntity.this.remainingEldrinTime;
                    case DATA_POWER_LEVEL -> ActuatorWaterBlockEntity.this.powerLevel;
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorWaterBlockEntity.this.remainingEldrinTime = pValue;
                    case DATA_POWER_LEVEL -> ActuatorWaterBlockEntity.this.powerLevel = pValue;
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    public int getEfficiencyIncrease() {
        return EFFICIENCY_INCREASE[this.powerLevel];
    }

    public static int getEfficiencyIncrease(int pPowerLevel) {
        return EFFICIENCY_INCREASE[pPowerLevel];
    }

    public int getEldrinPowerUsage() {
        return ELDRIN_POWER_USAGE[this.powerLevel];
    }

    public static int getEldrinPowerUsage(int pPowerLevel) {
        return ELDRIN_POWER_USAGE[pPowerLevel];
    }

    public int getWaterPerOperation() {
        return WATER_PER_OPERATION[this.powerLevel];
    }

    public static int getWaterPerOperation(int pPowerLevel) {
        return WATER_PER_OPERATION[pPowerLevel];
    }

    public int getSteamPerProcess() {
        return STEAM_PER_PROCESS[this.powerLevel];
    }

    public static int getSteamPerProcess(int pPowerLevel) {
        return STEAM_PER_PROCESS[pPowerLevel];
    }

    public int getPowerLevel() {
        return this.powerLevel;
    }

    public void increasePowerLevel() {
        this.powerLevel = Math.min(13, this.powerLevel + 1);
    }

    public void decreasePowerLevel() {
        this.powerLevel = Math.max(1, this.powerLevel - 1);
    }

    @Override
    public void setPowerLevel(int pPowerLevel) {
        this.powerLevel = pPowerLevel;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        remainingEldrinTime = nbt.getInt("remainingEldrinTime");
        powerLevel = nbt.getInt("powerLevel");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ActuatorWaterBlockEntity entity) {

    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ActuatorWaterMenu(i, inventory, this, this.data);
    }
}
