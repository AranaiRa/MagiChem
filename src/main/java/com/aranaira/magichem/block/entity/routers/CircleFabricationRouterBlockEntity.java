package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.CircleFabricationBlock;
import com.aranaira.magichem.block.CirclePowerBlock;
import com.aranaira.magichem.block.GrandDistilleryRouterBlock;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.GrandDistilleryRouterType;
import com.aranaira.magichem.item.MateriaItem;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_CIRCLE_FABRICATION;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_CIRCLE_POWER;

public class CircleFabricationRouterBlockEntity extends BlockEntity implements MenuProvider, INoCreativeTab, IDestroysMasterOnDestruction, IHasDeviceRecipeSlot, IMateriaProvisionRequester, IMateriaSortingRequester, ICanHaveUnbottledMateriaInInputTray {
    private BlockPos masterPos;
    private CircleFabricationBlockEntity master;

    public CircleFabricationRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.CIRCLE_FABRICATION_ROUTER_BE.get(), pPos, pBlockState);
    }

    public void configure(BlockPos pMasterPos) {
        this.masterPos = pMasterPos;
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    public CircleFabricationBlockEntity getMaster(){
        if(master == null) {
            if(masterPos != null) {
                master = (CircleFabricationBlockEntity) getLevel().getBlockEntity(masterPos);
            } else {
                final int routerType = getBlockState().getValue(ROUTER_TYPE_CIRCLE_FABRICATION);
                for (Pair<BlockPos, Integer> posAndType : CircleFabricationBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
                    if (routerType == posAndType.getSecond()) {
                        BlockEntity query = getLevel().getBlockEntity(getBlockPos().offset(posAndType.getFirst().multiply(-1)));
                        if (query instanceof CircleFabricationBlockEntity resolved) {
                            master = resolved;
                        }
                    }
                }
            }

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
        return Component.translatable("block.magichem.circle_fabrication");
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
        CirclePowerBlock.destroyRouters(getLevel(), getMasterPos(), null);
    }

    @Override
    public byte setRecipe(ItemStack pStack, Player player) {
        if(masterPos == null)
            return ERROR_CODE_NO_BLOCK_ENTITY;

        return getMaster().setRecipe(pStack, player);
    }

    @Override
    public ItemStack getRecipeItem() {
        if(masterPos == null)
            return null;

        return getMaster().getRecipeItem();
    }

    @Override
    public ItemStack getRecipeItem(boolean pMakeCopy) {
        if(masterPos == null)
            return null;

        return getMaster().getRecipeItem(pMakeCopy);
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        return false;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return null;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void provide(ItemStack pStack) {

    }

    @Override
    public boolean needsSorting() {
        return false;
    }

    @Override
    public ItemStack tryExtractUnbottled(ItemStack pBottlesInHand) {
        if(masterPos == null)
            return ItemStack.EMPTY;

        return getMaster().tryExtractUnbottled(pBottlesInHand);
    }

    @Override
    public boolean isClogged() {
        if(masterPos == null)
            return false;

        return getMaster().isClogged();
    }
}
