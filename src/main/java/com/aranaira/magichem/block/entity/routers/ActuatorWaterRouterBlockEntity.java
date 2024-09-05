package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class ActuatorWaterRouterBlockEntity extends BaseActuatorRouterBlockEntity implements IShlorpReceiver, IMateriaProvisionRequester {

    public ActuatorWaterRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState);
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return ((ActuatorWaterBlockEntity)getMaster()).allowIncreasedDeliverySize();
    }

    @Override
    public boolean needsProvisioning() {
        return ((ActuatorWaterBlockEntity)getMaster()).needsProvisioning();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return ((ActuatorWaterBlockEntity)getMaster()).getProvisioningNeeds();
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorWaterBlockEntity)getMaster()).setProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorWaterBlockEntity)getMaster()).cancelProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        ((ActuatorWaterBlockEntity)getMaster()).provide(pStack);
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return ((ActuatorWaterBlockEntity)getMaster()).canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return ((ActuatorWaterBlockEntity)getMaster()).insertStackFromShlorp(pStack);
    }
}
