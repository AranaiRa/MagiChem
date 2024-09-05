package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class ActuatorAirRouterBlockEntity extends BaseActuatorRouterBlockEntity implements IShlorpReceiver, IMateriaProvisionRequester {

    public ActuatorAirRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState);
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return ((ActuatorAirBlockEntity)getMaster()).allowIncreasedDeliverySize();
    }

    @Override
    public boolean needsProvisioning() {
        return ((ActuatorAirBlockEntity)getMaster()).needsProvisioning();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return ((ActuatorAirBlockEntity)getMaster()).getProvisioningNeeds();
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorAirBlockEntity)getMaster()).setProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorAirBlockEntity)getMaster()).cancelProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        ((ActuatorAirBlockEntity)getMaster()).provide(pStack);
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return ((ActuatorAirBlockEntity)getMaster()).canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return ((ActuatorAirBlockEntity)getMaster()).insertStackFromShlorp(pStack);
    }
}
