package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class ActuatorFireRouterBlockEntity extends BaseActuatorRouterBlockEntity implements IShlorpReceiver, IMateriaProvisionRequester {

    public ActuatorFireRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState);
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return ((ActuatorFireBlockEntity)getMaster()).allowIncreasedDeliverySize();
    }

    @Override
    public boolean needsProvisioning() {
        return ((ActuatorFireBlockEntity)getMaster()).needsProvisioning();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return ((ActuatorFireBlockEntity)getMaster()).getProvisioningNeeds();
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorFireBlockEntity)getMaster()).setProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        ((ActuatorFireBlockEntity)getMaster()).cancelProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        ((ActuatorFireBlockEntity)getMaster()).provide(pStack);
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return ((ActuatorFireBlockEntity)getMaster()).canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return ((ActuatorFireBlockEntity)getMaster()).insertStackFromShlorp(pStack);
    }
}
