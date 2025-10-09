package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.AcidBasinBlock;
import com.aranaira.magichem.block.AcidBasinRouterBlock;
import com.aranaira.magichem.block.entity.AcidBasinBlockEntity;
import com.aranaira.magichem.foundation.IDestroysMasterOnDestruction;
import com.aranaira.magichem.foundation.enums.AcidBasinRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_ACID_BASIN;

public class AcidBasinRouterBlockEntity extends BlockEntity implements IRouterBlockEntity, IDestroysMasterOnDestruction {
    private BlockPos masterPos = null;
    private AcidBasinBlockEntity master = null;

    public AcidBasinRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACID_BASIN_ROUTER_BE.get(), pPos, pBlockState);
    }

    public void configure(BlockPos pPos) {
        masterPos = pPos;
        syncAndSave();
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public AcidBasinBlockEntity getMaster() {
        if(master == null) {
            if(masterPos != null)
                master = (AcidBasinBlockEntity) getLevel().getBlockEntity(masterPos);

            //if master is still null we've got a problem and the router needs to be deleted
            if(master == null) {
                level.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        return master;
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(FACING);
    }

    public AcidBasinRouterType getRouterType() {
        int routerType = getBlockState().getValue(ROUTER_TYPE_ACID_BASIN);
        if(routerType == AcidBasinRouterBlock.ROUTER_TYPE_MAIN_TANK) {
            return AcidBasinRouterType.MAIN_TANK;
        } else if(routerType == AcidBasinRouterBlock.ROUTER_TYPE_MAIN_TANK_ABOVE) {
            return AcidBasinRouterType.MAIN_TANK_ABOVE;
        } else if(routerType == AcidBasinRouterBlock.ROUTER_TYPE_OUTPUT_TANK) {
            return AcidBasinRouterType.OUTPUT_TANK;
        } else if(routerType == AcidBasinRouterBlock.ROUTER_TYPE_OUTPUT_TANK_ABOVE) {
            return AcidBasinRouterType.OUTPUT_TANK_ABOVE;
        }
        return AcidBasinRouterType.NONE;
    }

    @Override
    public DevicePlugDirection getPlugDirection() {
        return DevicePlugDirection.NONE;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getMaster() == null ? LazyOptional.empty() : getMaster().getCapability(cap, side);
    }

    @Override
    public void destroyMaster() {
        getLevel().destroyBlock(getMasterPos(), true);
        AcidBasinBlock.destroyRouters(getLevel(), getMasterPos(), getFacing());
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        if(masterPos != null)
            nbt.putLong("masterPos", masterPos.asLong());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if(nbt.contains("masterPos"))
            masterPos = BlockPos.of(nbt.getLong("masterPos"));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        if(masterPos != null)
            nbt.putLong("masterPos", masterPos.asLong());
        return nbt;
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
}
