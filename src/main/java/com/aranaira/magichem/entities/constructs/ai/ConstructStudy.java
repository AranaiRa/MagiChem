package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Random;

public class ConstructStudy extends ConstructAITask<ConstructStudy> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos deskPos;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer;
    private static final Random random = new Random();

    public ConstructStudy(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        deskPos = null;
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
//            switch (this.phase) {
//            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.COLLECT_EXPERIENCE);
    }

    @Override
    public ConstructStudy duplicate() {
        return new ConstructStudy(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructStudy copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructStudy task) {
            this.deskPos = task.deskPos;
        }

        return this;
    }

    @Override
    public void readNBT(CompoundTag compoundTag) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag compoundTag) {
        return compoundTag;
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskAreaParameter("study.point"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("study.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.deskPos = pointParam.getPoint().getPosition();
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.deskPos != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    }

    enum ETaskPhase {
        SETUP,
        MOVE_TO_DESK,
        RANDOM_MOODLET,
        WAIT_TO_FAIL
    }

}