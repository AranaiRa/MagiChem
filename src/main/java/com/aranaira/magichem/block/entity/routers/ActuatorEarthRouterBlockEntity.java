package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class ActuatorEarthRouterBlockEntity extends BaseActuatorRouterBlockEntity implements IShlorpReceiver, IMateriaProvisionRequester {

    public ActuatorEarthRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState);
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return ((ActuatorEarthBlockEntity)getMaster()).allowIncreasedDeliverySize();
    }

    @Override
    public boolean needsProvisioning() {
        return ((ActuatorEarthBlockEntity)getMaster()).needsProvisioning();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return ((ActuatorEarthBlockEntity)getMaster()).getProvisioningNeeds();
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorEarthBlockEntity)getMaster()).setProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorEarthBlockEntity)getMaster()).cancelProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        ((ActuatorEarthBlockEntity)getMaster()).provide(pStack);
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return ((ActuatorEarthBlockEntity)getMaster()).canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return ((ActuatorEarthBlockEntity)getMaster()).insertStackFromShlorp(pStack);
    }
}
