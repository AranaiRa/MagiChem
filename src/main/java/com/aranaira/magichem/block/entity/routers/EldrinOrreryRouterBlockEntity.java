package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.EldrinOrreryBlock;
import com.aranaira.magichem.block.EldrinOrreryRouterBlock;
import com.aranaira.magichem.block.entity.EldrinOrreryBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.IDestroysMasterOnDestruction;
import com.aranaira.magichem.foundation.IMateriaSortingRequester;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.EldrinOrreryRouterType;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_ELDRIN_ORRERY;

public class EldrinOrreryRouterBlockEntity extends BlockEntity implements MenuProvider, INoCreativeTab, IDestroysMasterOnDestruction {
    private BlockPos masterPos;
    private EldrinOrreryBlockEntity master;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;
    private int packedData;

    public EldrinOrreryRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ELDRIN_ORRERY_ROUTER_BE.get(), pPos, pBlockState);
    }

    public Direction getFacing() {
        return getBlockState().getValue(FACING);
    }

    public EldrinOrreryRouterType getRouterType() {

        int routerType = getBlockState().getValue(ROUTER_TYPE_ELDRIN_ORRERY);
        if(routerType == 1) return EldrinOrreryRouterType.NORTH;
        else if(routerType == 2) return EldrinOrreryRouterType.NORTH_EAST;
        else if(routerType == 3) return EldrinOrreryRouterType.EAST;
        else if(routerType == 4) return EldrinOrreryRouterType.SOUTH_EAST;
        else if(routerType == 5) return EldrinOrreryRouterType.SOUTH;
        else if(routerType == 6) return EldrinOrreryRouterType.SOUTH_WEST;
        else if(routerType == 7) return EldrinOrreryRouterType.WEST;
        else if(routerType == 8) return EldrinOrreryRouterType.NORTH_EAST;
        else if(routerType == 9) return EldrinOrreryRouterType.ABOVE;
        else if(routerType == 10) return EldrinOrreryRouterType.DOUBLE_ABOVE;


        return EldrinOrreryRouterType.NONE;
    }

    public void configure(BlockPos pMasterPos) {
        this.masterPos = pMasterPos;
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    public EldrinOrreryBlockEntity getMaster(){
        if(master == null || masterPos == null) {
            final EldrinOrreryRouterType routerType = EldrinOrreryBlock.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_ELDRIN_ORRERY));
            for (Pair<BlockPos, EldrinOrreryRouterType> query : EldrinOrreryBlock.getRouterOffsets()) {
                if(routerType == query.getSecond()) {
                    BlockPos offset = query.getFirst().multiply(-1);
                    BlockPos target = getBlockPos().offset(offset);
                    BlockEntity be = level.getBlockEntity(target);
                    if(be instanceof EldrinOrreryBlockEntity orrery) {
                        masterPos = orrery.getBlockPos();
                        master = orrery;
                        return master;
                    }
                }
            }
        }

        return master;
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.EldrinOrrery");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getMaster() == null ? LazyOptional.empty() : getMaster().getCapability(cap, side);
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
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
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

        nbt.putLong("masterPos", masterPos.asLong());

        return nbt;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return getMaster().createMenu(pContainerId, pPlayerInventory, pPlayer);
    }

    @Override
    public void destroyMaster() {
        getLevel().destroyBlock(getMasterPos(), true);
        EldrinOrreryBlock.destroyRouters(getLevel(), getMasterPos());
    }
}
