package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.IKeepsInventoryOnBreak;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.gui.NourishingCenserMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class NourishingCenserBlockEntity extends BlockEntity implements MenuProvider, IKeepsInventoryOnBreak, IMateriaProvisionRequester {
    private int droplets;

    public NourishingCenserBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.NOURISHING_CENSER_BE.get(), pPos, pBlockState);
    }

    public int getDroplets() {
        return droplets;
    }

    public float getDropletsPercent() {
        return (float)droplets / (float)(ServerConfig.nourishingCenserMateriaUnitsPerDram * 3);
    }

    @Override
    public void packDataToBlockItem() {

    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {

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
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new NourishingCenserMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(0));
    }
}
