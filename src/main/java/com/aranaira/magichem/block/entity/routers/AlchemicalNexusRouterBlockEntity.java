package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.AlchemicalNexusBlock;
import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.IDestroysMasterOnDestruction;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.enums.AlchemicalNexusRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemicalNexusRouterBlockEntity extends BlockEntity implements MenuProvider, INoCreativeTab, ICanTakePlugins, IRouterBlockEntity, IShlorpReceiver, IDestroysMasterOnDestruction {

    private BlockPos masterPos;
    private AlchemicalNexusBlockEntity master;
    private AlchemicalNexusRouterType routerType = AlchemicalNexusRouterType.NONE;
    private Direction facing;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;
    private int packedData;

    public AlchemicalNexusRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ALCHEMICAL_NEXUS_ROUTER_BE.get(), pPos, pBlockState);
    }

    public Direction getFacing() {
        return this.facing;
    }

    public AlchemicalNexusRouterType getRouterType() {
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

    public void configure(BlockPos pMasterPos, AlchemicalNexusRouterType pRouterType, Direction pFacing, DevicePlugDirection pPlugDirection) {
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
    public void removePlugin(AbstractDirectionalPluginBlockEntity pPlugin) {
        getMaster().removePlugin(pPlugin);
    }

    @Override
    public void linkPluginsDeferred() {
        getMaster().linkPluginsDeferred();
    }

    public AlchemicalNexusBlockEntity getMaster(){
        if(master == null) {
            if(masterPos != null)
                master = (AlchemicalNexusBlockEntity) getLevel().getBlockEntity(masterPos);

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
        return Component.translatable("block.magichem.alchemicalnexus");
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

    private int mapTypeToBitpack(AlchemicalNexusRouterType pRouterType) {
        if(pRouterType == null)
            return 0;

        return switch(pRouterType) {
            case PLUG_LEFT -> 1;
            case PLUG_RIGHT -> 2;
            case FRONT -> 3;
            case FRONT_LEFT -> 4;
            case FRONT_RIGHT -> 5;
            case BACK_TANK -> 6;
            case BACK_LEFT -> 7;
            case BACK_RIGHT -> 8;
            case TOP_CRYSTAL -> 9;
            default -> 0;
        };
    }

    private AlchemicalNexusRouterType unmapTypeFromBitpack(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> AlchemicalNexusRouterType.PLUG_LEFT;
            case 2 -> AlchemicalNexusRouterType.PLUG_RIGHT;
            case 3 -> AlchemicalNexusRouterType.FRONT;
            case 4 -> AlchemicalNexusRouterType.FRONT_LEFT;
            case 5 -> AlchemicalNexusRouterType.FRONT_RIGHT;
            case 6 -> AlchemicalNexusRouterType.BACK_TANK;
            case 7 -> AlchemicalNexusRouterType.BACK_LEFT;
            case 8 -> AlchemicalNexusRouterType.BACK_RIGHT;
            case 9 -> AlchemicalNexusRouterType.TOP_CRYSTAL;
            default -> AlchemicalNexusRouterType.NONE;
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
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return getMaster().canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return getMaster().insertStackFromShlorp(pStack);
    }

    @Override
    public void destroyMaster() {
        getLevel().destroyBlock(getMasterPos(), true);
        AlchemicalNexusBlock.destroyRouters(getLevel(), getMasterPos(), getFacing());
    }
}
