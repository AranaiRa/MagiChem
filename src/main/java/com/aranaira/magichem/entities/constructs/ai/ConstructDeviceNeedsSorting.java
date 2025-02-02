package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IMateriaSortingRequester;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ConstructDeviceNeedsSorting extends ConstructConditional<ConstructDeviceNeedsSorting> {
    private BlockPos targetApparatus = null;

    public ConstructDeviceNeedsSorting(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        if(targetApparatus == null)
            return false;

        BlockEntity be = construct.asEntity().level().getBlockEntity(targetApparatus);

        if(be instanceof IMateriaSortingRequester imsr) {
            return imsr.needsSorting();
        }

        return false;
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.QUERY_HAS_GRIME_LEVEL);
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("query_device_needs_sorting.point"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("query_device_needs_sorting.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                BlockPos targetPos = pointParam.getPosition();
                if(targetPos != null) {
                    targetApparatus = targetPos;
                }
            }
        });
    }

    @Override
    public boolean isFullyConfigured() {
        return targetApparatus != null;
    }

    @Override
    public ConstructDeviceNeedsSorting copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructDeviceNeedsSorting task) {
            this.targetApparatus = task.targetApparatus;
        }

        return this;
    }

    @Override
    public ConstructDeviceNeedsSorting duplicate() {
        return new ConstructDeviceNeedsSorting(this.construct, this.guiIcon).copyFrom(this);
    }
}
