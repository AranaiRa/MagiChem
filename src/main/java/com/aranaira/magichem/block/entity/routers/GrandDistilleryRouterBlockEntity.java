package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.GrandDistilleryBlock;
import com.aranaira.magichem.block.GrandDistilleryRouterBlock;
import com.aranaira.magichem.block.entity.GrandDistilleryBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.GrandDistilleryRouterType;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GrandDistilleryRouterBlockEntity extends AbstractBlockEntityWithEfficiency implements MenuProvider, INoCreativeTab, ICanTakePlugins, IRouterBlockEntity {
    private BlockPos masterPos;
    private GrandDistilleryBlockEntity master;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;
    private int packedData;

    public GrandDistilleryRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.GRAND_DISTILLERY_ROUTER_BE.get(), pPos, pBlockState);
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public GrandDistilleryRouterType getRouterType() {
        return GrandDistilleryRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(GrandDistilleryRouterBlock.ROUTER_TYPE));
    }

    public DevicePlugDirection getPlugDirection() {
        GrandDistilleryRouterType type = GrandDistilleryRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(GrandDistilleryRouterBlock.ROUTER_TYPE));

        if(type == GrandDistilleryRouterType.PLUG_MID_LEFT || type == GrandDistilleryRouterType.PLUG_MID_RIGHT){
            if(getBlockState().getValue(GrandDistilleryBlock.HAS_LABORATORY_UPGRADE)) {
                return this.plugDirection;
            } else {
                return DevicePlugDirection.NONE;
            }
        } else {
            return this.plugDirection;
        }
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

    public GrandDistilleryBlockEntity getMaster(){
        if(master == null) {
            if(masterPos != null)
                master = (GrandDistilleryBlockEntity) getLevel().getBlockEntity(masterPos);

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
        return Component.translatable("block.magichem.grand_distillery");
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
        nbt.putInt("plugDirection", mapPlugDirToInt(plugDirection));
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        masterPos = BlockPos.of(nbt.getLong("masterPos"));
        plugDirection = unmapPlugDirFromInt(nbt.getInt("plugDirection"));
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
        nbt.putInt("plugDirection", mapPlugDirToInt(plugDirection));

        return nbt;
    }

    private int mapPlugDirToInt(DevicePlugDirection pPlugDirection) {
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

    private DevicePlugDirection unmapPlugDirFromInt(int pBitpack) {
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