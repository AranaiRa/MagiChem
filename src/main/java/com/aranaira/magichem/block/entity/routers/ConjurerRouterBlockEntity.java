package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.entity.ConjurerBlockEntity;
import com.aranaira.magichem.foundation.IDestroysMasterOnDestruction;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.item.MateriaItem;
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

import java.util.Map;

import static com.aranaira.magichem.block.ConjurerRouterBlock.*;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_CONJURER;

public class ConjurerRouterBlockEntity extends BlockEntity implements MenuProvider, INoCreativeTab, IDestroysMasterOnDestruction, IShlorpReceiver, IMateriaProvisionRequester {
    private BlockPos masterPos;
    private ConjurerBlockEntity master;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;
    private int packedData;

    public ConjurerRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.CONJURER_ROUTER_BE.get(), pPos, pBlockState);
    }

    public DevicePlugDirection getPlugDirection() {
        return this.plugDirection;
    }

    public void configure(BlockPos pMasterPos) {
        this.masterPos = pMasterPos;
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    public ConjurerBlockEntity getMaster(){
        if(master == null) {
            if(masterPos != null)
                master = (ConjurerBlockEntity) getLevel().getBlockEntity(masterPos);

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
        return Component.translatable("block.magichem.conjurer");
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
        if(getBlockState().getValue(ROUTER_TYPE_CONJURER) == ROUTER_TYPE_MIDDLE) {
            getLevel().destroyBlock(getBlockPos().above(), true);
            getLevel().destroyBlock(getBlockPos().below(), true);
        } else if(getBlockState().getValue(ROUTER_TYPE_CONJURER) == ROUTER_TYPE_TOP) {
            getLevel().destroyBlock(getBlockPos().below(), true);
            getLevel().destroyBlock(getBlockPos().below().below(), true);
        }
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return getMaster().allowIncreasedDeliverySize();
    }

    @Override
    public boolean needsProvisioning() {
        return getMaster().needsProvisioning();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return getMaster().getProvisioningNeeds();
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        getMaster().setProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        getMaster().cancelProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        getMaster().provide(pStack);
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return getMaster().canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return getMaster().insertStackFromShlorp(pStack);
    }
}
