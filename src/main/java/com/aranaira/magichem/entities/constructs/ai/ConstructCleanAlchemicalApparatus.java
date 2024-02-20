package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.entity.ext.BlockEntityWithEfficiency;
import com.aranaira.magichem.block.entity.interfaces.IMateriaProcessingDevice;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class ConstructCleanAlchemicalApparatus extends ConstructAITask<ConstructCleanAlchemicalApparatus> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos targetApparatusPos;
    private BlockEntityWithEfficiency targetApparatus = null;
    private ECleanTaskPhase phase = ECleanTaskPhase.SETUP;
    private int waitTimer;

    public ConstructCleanAlchemicalApparatus(IConstruct<?> construct, ResourceLocation guiIcon) {
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
                case SETUP -> {
                    this.setMoveTarget(targetApparatusPos);
                    this.phase = ECleanTaskPhase.MOVE_TO_DEVICE;
                }
                case MOVE_TO_DEVICE -> {
                    if (doMove(2.0F)) {
                        this.phase = ECleanTaskPhase.WAIT_AT_DEVICE;
                        this.waitTimer = 21;
                        if(construct.getFluidInTank(0).getFluid() == Fluids.WATER) {
                            if(construct.getStoredFluidAmount() > 100) {
                                this.pushDiagnosticMessage("Sploosh! That's one squeaky clean apparatus, boss!", false);
                                this.swingHandWithCapability(ConstructCapability.FLUID_DISPENSE);
                                //construct.fluid
                                this.targetApparatus.clean();
                            } else {
                                this.phase = ECleanTaskPhase.WAIT_TO_FAIL;
                                this.pushDiagnosticMessage("I don't have enough water to clean the apparatus. Sorry, boss!", false);
                            }
                        } else {
                            this.phase = ECleanTaskPhase.WAIT_TO_FAIL;
                            this.pushDiagnosticMessage("I don't have any water. I need some to clean the apparatus, boss!", false);
                        }
                    }
                }
                case WAIT_AT_DEVICE -> {
                    this.waitTimer--;
                    if(this.waitTimer == 0) {
                        this.setSuccessCode();
                    }
                }
                case WAIT_TO_FAIL -> {
                    this.waitTimer--;
                    if(this.waitTimer == 0) {
                        this.forceFail();
                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.CLEAN_ALCHEMICAL_APPARATUS);
    }

    @Override
    public ConstructCleanAlchemicalApparatus duplicate() {
        return new ConstructCleanAlchemicalApparatus(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructCleanAlchemicalApparatus copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructCleanAlchemicalApparatus task) {
            this.targetApparatusPos = task.targetApparatusPos;
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
        parameters.add(new ConstructTaskPointParameter("clean_alchemical_apparatus.point"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("clean_alchemical_apparatus.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.targetApparatusPos = pointParam.getPosition();
                BlockEntity be = construct.asEntity().level().getBlockEntity(this.targetApparatusPos);
                if(be instanceof BlockEntityWithEfficiency bewe)
                    this.targetApparatus = bewe;
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.targetApparatus != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_DISPENSE, ConstructCapability.FLUID_STORE};
    }
}

enum ECleanTaskPhase {
    SETUP,
    MOVE_TO_DEVICE,
    WAIT_AT_DEVICE,
    WAIT_TO_FAIL
}
