package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.CentrifugeBlock;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.ext.BlockEntityWithEfficiency;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.base.INoCreativeTab;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeRouterBlockEntity extends BlockEntityWithEfficiency implements MenuProvider, INoCreativeTab, ICanTakePlugins {
    private BlockPos masterPos;
    private CentrifugeBlockEntity master;
    private CentrifugeRouterType routerType = CentrifugeRouterType.NONE;
    private Direction facing;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;
    private int packedData;

    public CentrifugeRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.CENTRIFUGE_ROUTER_BE.get(), pPos, CentrifugeRouterBlockEntity.baseEfficiency, pBlockState);
    }

    public Direction getFacing() {
        return this.facing;
    }

    public CentrifugeRouterType getRouterType() {
        return this.routerType;
    }

    public DevicePlugDirection getPlugDirection() {
        return this.plugDirection;
    }

    public BlockEntity getPlugEntity() {
        BlockPos target = getBlockPos();

        if(getPlugDirection() == DevicePlugDirection.NORTH) target = target.north();
        else if(getPlugDirection() == DevicePlugDirection.EAST) target = target.east();
        else if(getPlugDirection() == DevicePlugDirection.SOUTH) target = target.south();
        else if(getPlugDirection() == DevicePlugDirection.WEST) target = target.west();

        return getLevel().getBlockEntity(target);
    }

    public void configure(BlockPos pMasterPos, CentrifugeRouterType pRouterType, Direction pFacing, DevicePlugDirection pPlugDirection) {
        this.masterPos = pMasterPos;
        this.routerType = pRouterType;
        this.facing = pFacing;
        this.plugDirection = pPlugDirection;
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    @Override
    public void linkPlugins() {
        getMaster().linkPlugins();
    }

    @Override
    public void removePlugin(DirectionalPluginBlockEntity pPlugin) {
        getMaster().removePlugin(pPlugin);
    }

    @Override
    public void linkPluginsDeferred() {
        getMaster().linkPluginsDeferred();
    }

    public CentrifugeBlockEntity getMaster(){
        if(master == null) {
            master = (CentrifugeBlockEntity) getLevel().getBlockEntity(masterPos);
        }
        return master;
    }

    @Override
    public void setRemoved() {
        for(Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> posAndType : CentrifugeBlock.getRouterOffsets(facing)) {
            getLevel().destroyBlock(masterPos.offset(posAndType.getFirst()), true);
        }
        getLevel().destroyBlock(masterPos, true);
        super.setRemoved();
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.centrifuge");
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
        nbt.putInt("typeAndFacing", mapPlugDirToBitpack(plugDirection) << 8 | mapFacingToBitpack(facing) << 4 | mapTypeToBitpack(routerType));
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        masterPos = BlockPos.of(nbt.getLong("masterPos"));
        int typeAndFacing = nbt.getInt("typeAndFacing");
        routerType = unmapTypeFromBitpack(typeAndFacing & 15);
        facing = unmapFacingFromBitpack((typeAndFacing >> 4) & 15);
        plugDirection = unmapPlugDirFromBitpack(typeAndFacing >> 8);
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
        nbt.putInt("typeAndFacing", mapPlugDirToBitpack(plugDirection) << 8 | mapFacingToBitpack(facing) << 4 | mapTypeToBitpack(routerType));

        return nbt;
    }

    private int mapTypeToBitpack(CentrifugeRouterType pRouterType) {
        if(pRouterType == null)
            return 0;

        return switch(pRouterType) {
            case PLUG_LEFT -> 1;
            case PLUG_RIGHT -> 2;
            case COG -> 3;
            default -> 0;
        };
    }

    private CentrifugeRouterType unmapTypeFromBitpack(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> CentrifugeRouterType.PLUG_LEFT;
            case 2 -> CentrifugeRouterType.PLUG_RIGHT;
            case 3 -> CentrifugeRouterType.COG;
            default -> CentrifugeRouterType.NONE;
        };
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

    private int mapPlugDirToBitpack(DevicePlugDirection pPlugDirection) {
        if(pPlugDirection == null)
            return 0;

        return switch(pPlugDirection) {
            case NORTH -> 1;
            case SOUTH -> 2;
            case EAST -> 3;
            case WEST -> 4;
            default -> 0;
        };
    }

    private DevicePlugDirection unmapPlugDirFromBitpack(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> DevicePlugDirection.NORTH;
            case 2 -> DevicePlugDirection.SOUTH;
            case 3 -> DevicePlugDirection.EAST;
            case 4 -> DevicePlugDirection.WEST;
            default -> DevicePlugDirection.NONE;
        };
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return getMaster().createMenu(pContainerId, pPlayerInventory, pPlayer);
    }

    @Override
    public int getGrimeFromData() {
        return getMaster().getGrimeFromData();
    }

    @Override
    public int getMaximumGrime() {
        return getMaster().getMaximumGrime();
    }

    @Override
    public int clean() {
        return getMaster().clean();
    }
}
