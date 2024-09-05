package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.entity.ActuatorArcaneBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class ActuatorArcaneRouterBlockEntity extends BaseActuatorRouterBlockEntity implements IShlorpReceiver, IMateriaProvisionRequester {

    public ActuatorArcaneRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState);
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return ((ActuatorArcaneBlockEntity)getMaster()).allowIncreasedDeliverySize();
    }

    @Override
    public boolean needsProvisioning() {
        return ((ActuatorArcaneBlockEntity)getMaster()).needsProvisioning();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return ((ActuatorArcaneBlockEntity)getMaster()).getProvisioningNeeds();
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorArcaneBlockEntity)getMaster()).setProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorArcaneBlockEntity)getMaster()).cancelProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        ((ActuatorArcaneBlockEntity)getMaster()).provide(pStack);
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return ((ActuatorArcaneBlockEntity)getMaster()).canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return ((ActuatorArcaneBlockEntity)getMaster()).insertStackFromShlorp(pStack);
    }
}
