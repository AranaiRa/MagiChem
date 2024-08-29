package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.FuseryBlock;
import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.IDestroysMasterOnDestruction;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.FuseryRouterType;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.block.FuseryRouterBlock.*;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class FuseryRouterBlockEntity extends AbstractBlockEntityWithEfficiency implements MenuProvider, INoCreativeTab, ICanTakePlugins, IRouterBlockEntity, IDestroysMasterOnDestruction {
    private BlockPos masterPos;
    private FuseryBlockEntity master;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;
    private int packedData;

    public FuseryRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.FUSERY_ROUTER_BE.get(), pPos, pBlockState);
    }

    public Direction getFacing() {
        return getBlockState().getValue(FACING);
    }

    public FuseryRouterType getRouterType() {
        int routerType = getBlockState().getValue(ROUTER_TYPE_FUSERY);
        if(routerType == ROUTER_TYPE_PLUG_LEFT) {
            return FuseryRouterType.PLUG_LEFT;
        } else if(routerType == ROUTER_TYPE_PLUG_RIGHT) {
            return FuseryRouterType.PLUG_RIGHT;
        } else if(routerType == ROUTER_TYPE_COG) {
            return FuseryRouterType.COG;
        }else if(routerType == ROUTER_TYPE_TANK_RIGHT) {
            return FuseryRouterType.TANK_RIGHT;
        } else if(routerType == ROUTER_TYPE_TANK_ACROSS) {
            return FuseryRouterType.TANK_ACROSS;
        }
        return FuseryRouterType.NONE;
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

    public void configure(BlockPos pMasterPos, DevicePlugDirection pPlugDirection) {
        this.masterPos = pMasterPos;
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

    public FuseryBlockEntity getMaster(){
        if(master == null) {
            if(masterPos != null)
                master = (FuseryBlockEntity) getLevel().getBlockEntity(masterPos);

            //if master is still null we've got a problem and the router needs to be deleted
            if(master == null) {
                level.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        return master;
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.fusery");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(getMasterPos() == null)
            return LazyOptional.empty();
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
        nbt.putInt("typeAndFacing", mapPlugDirToBitpack(plugDirection) << 8);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        masterPos = BlockPos.of(nbt.getLong("masterPos"));
        int typeAndFacing = nbt.getInt("typeAndFacing");
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
        nbt.putInt("typeAndFacing", mapPlugDirToBitpack(plugDirection) << 8);

        return nbt;
    }

    private int mapTypeToBitpack(FuseryRouterType pRouterType) {
        if(pRouterType == null)
            return 0;

        return switch(pRouterType) {
            case PLUG_LEFT -> 1;
            case PLUG_RIGHT -> 2;
            case COG -> 3;
            case TANK_RIGHT -> 4;
            case TANK_ACROSS -> 5;
            default -> 0;
        };
    }

    private FuseryRouterType unmapTypeFromBitpack(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> FuseryRouterType.PLUG_LEFT;
            case 2 -> FuseryRouterType.PLUG_RIGHT;
            case 3 -> FuseryRouterType.COG;
            case 4 -> FuseryRouterType.TANK_RIGHT;
            case 5 -> FuseryRouterType.TANK_ACROSS;
            default -> FuseryRouterType.NONE;
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

    @Override
    public void destroyMaster() {
        getLevel().destroyBlock(getMasterPos(), true);
        FuseryBlock.destroyRouters(getLevel(), getMasterPos(), getFacing());
    }
}
