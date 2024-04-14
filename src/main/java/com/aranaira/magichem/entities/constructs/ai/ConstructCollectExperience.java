package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.events.CommonEventHelper;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
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
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

public class ConstructCollectExperience extends ConstructAITask<ConstructCollectExperience> {
    private static final ConstructCapability[] requiredCaps;
    private AABB area;
    private ExperienceOrb targetOrb;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer;

    public ConstructCollectExperience(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            switch (this.phase) {
                case SETUP : {
                    List<ExperienceOrb> allOrbs = construct.asEntity().level().getEntitiesOfClass(ExperienceOrb.class, area);
                    if(allOrbs.size() > 0) {
                        targetOrb = allOrbs.get(0);
                        this.setMoveTarget(targetOrb.getOnPos());
                        this.phase = ETaskPhase.ABSORB_ORB;
                    } else {
                        this.forceFail();
                    }
                    break;
                }
                case ABSORB_ORB: {
                    if(doMove(4f)) {
                        if(targetOrb != null) {
                            int points = targetOrb.value;
                            FluidStack attempt = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), points * Config.fluidPerXPPoint);
                            if (construct.isFluidValid(1, attempt)) {
                                construct.fill(attempt, IFluidHandler.FluidAction.EXECUTE);
                                targetOrb.kill();
                                this.waitTimer = 3;
                                this.phase = ETaskPhase.WAIT;
                                this.pushDiagnosticMessage("I found an experience orb on the ground. Come here, you little...!", false);
                                this.setSuccessCode();
                            }
                        } else {
                            this.forceFail();
                        }
                    }
                    break;
                }
                case WAIT: {
                    this.waitTimer--;
                    if(this.waitTimer == 0) {
                        this.phase = ETaskPhase.SETUP;
                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.COLLECT_EXPERIENCE);
    }

    @Override
    public ConstructCollectExperience duplicate() {
        return new ConstructCollectExperience(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructCollectExperience copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructCollectExperience task) {
            this.area = task.area;
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
        parameters.add(new ConstructTaskAreaParameter("collect_experience.area"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("collect_experience.area").ifPresent((param) -> {
            if (param instanceof ConstructTaskAreaParameter areaParam) {
                if(areaParam.getPoints() != null) {
                    if (areaParam.getArea() != null)
                        this.area = areaParam.getArea();
                }
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.area != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_DISPENSE, ConstructCapability.FLUID_STORE};
    }

    enum ETaskPhase {
        SETUP,
        ABSORB_ORB,
        WAIT,
        WAIT_TO_FAIL
    }

}