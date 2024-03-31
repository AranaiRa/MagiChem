package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.base.INoCreativeTab;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseActuatorRouterBlockEntity extends BlockEntity implements MenuProvider, INoCreativeTab {
    private BlockPos masterPos;
    private BlockEntity master;
    private Direction facing;
    protected Player owner;

    public BaseActuatorRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.BASE_ACTUATOR_ROUTER_BE.get(), pPos, pBlockState);
    }

    public Direction getFacing() {
        return this.facing;
    }

    public void configure(BlockPos pMasterPos, Direction pFacing) {
        this.masterPos = pMasterPos;
        this.facing = pFacing;
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    public BlockEntity getMaster(){
        if(master == null) {
            master = getLevel().getBlockEntity(masterPos);
        }
        return master;
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getMaster().getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putLong("masterPos", masterPos.asLong());
        nbt.putInt("facing", mapFacingToBitpack(facing));
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        masterPos = BlockPos.of(nbt.getLong("masterPos"));
        facing = unmapFacingFromBitpack(nbt.getInt("facing"));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();

        nbt.putLong("masterPos", masterPos.asLong());
        nbt.putInt("facing", mapFacingToBitpack(facing));

        return nbt;
    }

    private int mapFacingToBitpack(Direction pFacing) {
        if(pFacing == null)
            return 0;

        return switch(pFacing) {
            case NORTH -> 1;
            case SOUTH -> 2;
            case EAST -> 3;
            case WEST -> 4;
            default -> 0;
        };
    }

    private Direction unmapFacingFromBitpack(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> Direction.NORTH;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.EAST;
            case 4 -> Direction.WEST;
            default -> null;
        };
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if(getMaster() instanceof MenuProvider mp)
            return mp.createMenu(pContainerId, pPlayerInventory, pPlayer);
        return null;
    }

    @Override
    public Component getDisplayName() {
        return null;
    }
}
