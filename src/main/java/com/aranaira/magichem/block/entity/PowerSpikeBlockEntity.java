package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.apache.commons.lang3.mutable.MutableInt;

public class PowerSpikeBlockEntity extends BlockEntity {

    public PowerSpikeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.POWER_SPIKE_BE.get(), pos, state);
    }

    private BlockPos
            powerDrawPos, powerTransferPos;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putLong("magichem.powerspike.powerDrawPos", this.powerDrawPos.asLong());
        nbt.putLong("magichem.powerspike.powerTransferPos", this.powerTransferPos.asLong());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        powerDrawPos = BlockPos.of(nbt.getLong("magichem.powerspike.powerDrawPos"));
        powerTransferPos = BlockPos.of(nbt.getLong("magichem.powerspike.powerTransferPos"));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PowerSpikeBlockEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        if(entity.powerTransferPos != null && entity.powerDrawPos != null) {

            MutableInt powerAvailable = new MutableInt(0);
            MutableInt powerToTransfer = new MutableInt(0);

            BlockEntity drawEntity = level.getBlockEntity(entity.powerDrawPos);
            BlockEntity transferEntity = level.getBlockEntity(entity.powerTransferPos);

            if(drawEntity != null && transferEntity != null) {
                drawEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(drawCap -> {
                    transferEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(transferCap -> {
                        powerAvailable.setValue(drawCap.getEnergyStored());

                        powerToTransfer.setValue(transferCap.receiveEnergy(powerAvailable.intValue(), true));

                        drawCap.extractEnergy(powerToTransfer.intValue(), false);

                        transferCap.receiveEnergy(powerToTransfer.intValue(), false);
                    });
                });
            }
        }
        else if(entity.powerTransferPos == null){
            updatePowerTransferPos(entity);
        }
    }

    private static void updatePowerTransferPos(PowerSpikeBlockEntity entity) {
        BlockPos thisPos = entity.getBlockPos();
        entity.powerTransferPos = thisPos.offset(entity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite().getNormal());
    }

    public void setPowerDrawPos(BlockPos drawPos) {
        this.powerDrawPos = drawPos;
    }
}
