package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.*;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ConstructCheckVessel extends ConstructConditional<ConstructCheckVessel> {
    private BlockPos targetVesselPos = null;
    private float targetPercentage = 1;

    public ConstructCheckVessel(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        if(targetVesselPos == null)
            return false;

        BlockEntity be = construct.asEntity().level().getBlockEntity(targetVesselPos);
        if(be instanceof AbstractMateriaStorageBlockEntity targetVessel) {
            float stock = (float) targetVessel.getCurrentStock();
            float limit = (float) targetVessel.getStorageLimit();
            return (stock / limit) >= targetPercentage;
        }

        return false;
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.QUERY_CHECK_VESSEL);
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("query_check_vessel.point"));
        parameters.add(new ConstructTaskIntegerParameter("query_check_vessel.int", 1, 100));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("query_check_vessel.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                targetVesselPos = pointParam.getPosition();
            }
        });

        this.getParameter("query_check_vessel.int").ifPresent((param) -> {
            if (param instanceof ConstructTaskIntegerParameter integerParam) {
                targetPercentage = (float)integerParam.getValue() / 100.0f;
            }
        });
    }

    @Override
    public boolean isFullyConfigured() {
        return targetVesselPos != null;
    }

    @Override
    public ConstructCheckVessel copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructCheckVessel task) {
            this.targetVesselPos = task.targetVesselPos;
            this.targetPercentage = task.targetPercentage;
        }

        return this;
    }

    @Override
    public ConstructCheckVessel duplicate() {
        return new ConstructCheckVessel(this.construct, this.guiIcon).copyFrom(this);
    }
}
