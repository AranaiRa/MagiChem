package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.blocks.tileentities.wizard_lab.EldrinFumeTile;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashMap;
import java.util.List;

public class ConstructHasEldrinInBuffer extends ConstructConditional<ConstructHasEldrinInBuffer> {
    private Affinity affinityToCheck = Affinity.UNKNOWN;
    private int amountToCheck = 0;

    public ConstructHasEldrinInBuffer(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        if(construct.getOwner() != null) {
            IWorldMagic worldMagic = (IWorldMagic)construct.getOwner().level().getCapability(WorldMagicProvider.MAGIC).orElse((IWorldMagic) null);
            if (worldMagic == null) {
                return false;
            } else {
                IWellspringNodeRegistry wellspringNetwork = worldMagic.getWellspringRegistry();
                HashMap<Affinity, Float> curAmt = wellspringNetwork.getNodeNetworkAmountFor(construct.getOwner().getUUID(), construct.getOwner().level());
                float contained = curAmt.getOrDefault(affinityToCheck, 0f);
                float resolvedPercent = (float)amountToCheck / 100f;

                return contained >= resolvedPercent * 1000f;
            }
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
        parameters.add(new ConstructTaskIntegerParameter("query_has_eldrin_in_buffer.int.selector", 1, 6));
        parameters.add(new ConstructTaskIntegerParameter("query_has_eldrin_in_buffer.int.amount", 1, 100));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("query_has_eldrin_in_buffer.int.selector").ifPresent((param) -> {
            if (param instanceof ConstructTaskIntegerParameter intParam) {
                int val = intParam.getValue();

                if(val == 1) affinityToCheck = Affinity.ENDER;
                else if(val == 2) affinityToCheck = Affinity.EARTH;
                else if(val == 3) affinityToCheck = Affinity.WATER;
                else if(val == 4) affinityToCheck = Affinity.WIND;
                else if(val == 5) affinityToCheck = Affinity.FIRE;
                else if(val == 6) affinityToCheck = Affinity.ARCANE;
                else affinityToCheck = Affinity.UNKNOWN;
            }
        });
        this.getParameter("query_has_eldrin_in_buffer.int.amount").ifPresent((param) -> {
            if (param instanceof ConstructTaskIntegerParameter intParam) {
                amountToCheck = intParam.getValue();
            }
        });
    }

    @Override
    public boolean isFullyConfigured() {
        return affinityToCheck != Affinity.UNKNOWN && amountToCheck != 0;
    }

    @Override
    public ConstructHasEldrinInBuffer copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructHasEldrinInBuffer task) {
            this.affinityToCheck = task.affinityToCheck;
            this.amountToCheck = task.amountToCheck;
        }

        return this;
    }

    @Override
    public ConstructHasEldrinInBuffer duplicate() {
        return new ConstructHasEldrinInBuffer(this.construct, this.guiIcon).copyFrom(this);
    }
}
