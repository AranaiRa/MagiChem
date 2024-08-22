package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractFixationBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.foundation.ISortFromContainerBlacklist;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.tools.InventoryUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class ConstructSortMateriaFromContainer extends ConstructAITask<ConstructSortMateriaFromContainer> {

    private static final ConstructCapability[] requiredCaps;
    private BlockPos takeFromTarget, bottleReturnTarget, jarTarget;
    private AbstractMateriaStorageBlockEntity jarTargetEntity;
    private AABB area;
    private boolean voidExcess;
    private int waitTimer;
    private ETaskPhase phase = ETaskPhase.SETUP;

    public ConstructSortMateriaFromContainer(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        takeFromTarget = null;
        bottleReturnTarget = null;
        jarTarget = null;
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            switch(this.phase) {
                case SETUP -> {
                    this.setMoveTarget(takeFromTarget);
                    this.phase = ETaskPhase.MOVE_TO_CONTAINER;
                }
                case MOVE_TO_CONTAINER -> {
                    if (doMove(2.0F)) {
                        this.waitTimer = 21;

                        BlockEntity be = construct.asEntity().level().getBlockEntity(this.takeFromTarget);
                        if(be != null) {
                            if(!(be instanceof ISortFromContainerBlacklist))
                            be.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(cap -> {
                                for(int i=0; i<cap.getSlots(); i++) {
                                    ItemStack stack = cap.getStackInSlot(i);

                                    if(!stack.isEmpty())
                                        continue;

                                    if(stack.getItem() instanceof MateriaItem) {

                                        break;
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.SORT_MATERIA_FROM_CONTAINER);
    }

    @Override
    public ConstructSortMateriaFromContainer duplicate() {
        return new ConstructSortMateriaFromContainer(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructSortMateriaFromContainer copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructSortMateriaFromContainer task) {
            this.takeFromTarget = task.takeFromTarget;
            this.area = task.area;
            this.voidExcess = task.voidExcess;
            this.bottleReturnTarget = task.bottleReturnTarget;
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
        parameters.add(new ConstructTaskPointParameter("sort_materia_from_container.point1"));
        parameters.add(new ConstructTaskAreaParameter("sort_materia_from_container.area"));
        parameters.add(new ConstructTaskBooleanParameter("sort_materia_from_container.boolean"));
        parameters.add(new ConstructTaskPointParameter("sort_materia_from_container.point2"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("sort_materia_from_container.point1").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.takeFromTarget = pointParam.getPosition();
            }
        });

        this.getParameter("sort_materia_from_container.area").ifPresent((param) -> {
            if (param instanceof ConstructTaskAreaParameter areaParam) {
                this.area = null;

                if(areaParam.getPoints() != null) {
                    if(areaParam.getArea() != null)
                        this.area = areaParam.getArea();
                }
            }
        });

        this.getParameter("sort_materia_from_container.boolean").ifPresent((param) -> {
            if(param instanceof ConstructTaskBooleanParameter boolParam) {
                this.voidExcess = boolParam.getValue();
            }
        });

        this.getParameter("sort_materia_from_container.point2").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.bottleReturnTarget = pointParam.getPosition();
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.area != null && this.takeFromTarget != null && this.bottleReturnTarget != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    }

    enum ETaskPhase {
        SETUP,
        MOVE_TO_CONTAINER,
        WAIT_AT_CONTAINER,
        MOVE_TO_VESSEL,
        WAIT_AT_VESSEL,
        MOVE_TO_BOTTLE_RETURN,
        WAIT_AT_BOTTLE_RETURN,
        WAIT_TO_FAIL
    }
}
