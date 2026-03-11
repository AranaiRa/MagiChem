package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.Animations;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;
import java.util.Optional;

public class ConstructCreateExperienceOrb extends ConstructAITask<ConstructCreateExperienceOrb> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos targetPos;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer;

    public ConstructCreateExperienceOrb(IConstruct<?> construct, ResourceLocation guiIcon) {
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
                    final FluidStack fluidInTank = construct.getFluidInTank(0);
                    if(!fluidInTank.isEmpty() && fluidInTank.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get()) {
                        if(fluidInTank.getAmount() < ServerConfig.fluidPerXPPoint) {
                            this.pushDiagnosticMessage("I don't have enough Academic Slurry to make an orb. Sorry, boss!", false);
                            this.forceFail();
                        } else {
                            this.setMoveTarget(targetPos);
                            this.phase = ETaskPhase.MOVE_TO_TARGET;
                        }
                    }
                    else {
                        this.forceFail();
                    }
                    break;
                }
                case MOVE_TO_TARGET: {
                    if(doMove(4f)) {
                        final Optional<InteractionHand> handWithCapability = construct.getHandWithCapability(ConstructCapability.FLUID_DISPENSE);

                        construct.forceAnimation(handWithCapability.get() == InteractionHand.MAIN_HAND ? Animations.SHOOT_LEFT : Animations.SHOOT_RIGHT, true);
                        this.phase = ETaskPhase.WAIT_TO_CREATE;
                        this.waitTimer = 30;
                    }
                    break;
                }
                case WAIT_TO_CREATE: {
                    if(waitTimer <= 0) {
                        this.phase = ETaskPhase.CREATE_ORB;
                    } else {
                        this.waitTimer--;
                    }
                    break;
                }
                case CREATE_ORB: {
                    if(doMove(4f)) {
                        int points = construct.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE).getAmount() / ServerConfig.fluidPerXPPoint;

                        ExperienceOrb orb = new ExperienceOrb(construct.asEntity().level(), targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, points);
                        construct.asEntity().level().addFreshEntity(orb);

                        this.pushDiagnosticMessage("I turned my slurry into an experience orb, boss. Look at it roll around!", false);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.COLLECT_EXPERIENCE);
    }

    @Override
    public ConstructCreateExperienceOrb duplicate() {
        return new ConstructCreateExperienceOrb(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructCreateExperienceOrb copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructCreateExperienceOrb task) {
            this.targetPos = task.targetPos;
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
        parameters.add(new ConstructTaskPointParameter("create_experience_orb.point"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("create_experience_orb.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                if(pointParam.getPoint() != null) {
                    this.targetPos = pointParam.getPosition();
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
        return this.targetPos != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_DISPENSE, ConstructCapability.FLUID_STORE};
    }

    enum ETaskPhase {
        SETUP,
        MOVE_TO_TARGET,
        WAIT_TO_CREATE,
        CREATE_ORB
    }

}