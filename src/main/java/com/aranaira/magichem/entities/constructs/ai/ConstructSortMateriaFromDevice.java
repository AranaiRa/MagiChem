package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.MateriaJarBlock;
import com.aranaira.magichem.block.MateriaVesselBlock;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractSeparationBlockEntity;
import com.aranaira.magichem.block.entity.routers.IRouterBlockEntity;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstructSortMateriaFromDevice extends ConstructAITask<ConstructSortMateriaFromDevice> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos takeFromTarget, jarTargetPos;
    private AbstractMateriaStorageBlockEntity jarTargetEntity;
    private AABB area;
    private boolean voidExcess;
    private MateriaItem filter;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer;
    private ItemStack materiaInTransit;

    public ConstructSortMateriaFromDevice(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        filter = null;
        jarTargetEntity = null;
        jarTargetPos = null;
        materiaInTransit = null;
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            switch (this.phase) {
                case SETUP -> {
                    this.setMoveTarget(takeFromTarget);
                    this.phase = ETaskPhase.MOVE_TO_DEVICE;
                }
                case MOVE_TO_DEVICE -> {
                    if (doMove(2.0F)) {
                        this.waitTimer = 21;
                        boolean foundTargetAndSource = this.selectMateriaStackFromSource();
                        if(this.filter != null) {
                            if(foundTargetAndSource) {
                                this.phase = ETaskPhase.WAIT_AT_DEVICE;
                                this.setTargetVessel(this.filter);
                            }
                        } else {
                            this.pushDiagnosticMessage("The device I'm monitoring is empty right now. I'll just wait for a bit!", false);
                            this.phase = ETaskPhase.WAIT_TO_FAIL;
                            this.swingHandWithCapability(ConstructCapability.FLUID_DISPENSE);
                        }
                    }
                }
                case WAIT_AT_DEVICE -> {
                    this.waitTimer--;
                    if(this.waitTimer == 0) {
                        this.phase = ETaskPhase.MOVE_TO_VESSEL;
                        this.setMoveTarget(this.jarTargetPos);
                    }
                }
                case MOVE_TO_VESSEL -> {
                    if(doMove(2.0F)) {
                        int amount = doMateriaTransfer();
                        if(amount > 0) {
                            this.pushDiagnosticMessage("I moved " + amount + " " + getTranslatedNameFromItem(this.filter) + " to a vessel, boss. Bloop!", true);
                            this.phase = ETaskPhase.WAIT_AT_VESSEL;
                        } else {
                            this.pushDiagnosticMessage("I couldn't find a jar to put the " + getTranslatedNameFromItem(this.filter) + " in. Sorry, boss!", true);
                            this.phase = ETaskPhase.WAIT_TO_FAIL;
                        }
                        this.waitTimer = 21;
                        this.swingHandWithCapability(ConstructCapability.FLUID_DISPENSE);
                    }
                }
                case WAIT_AT_VESSEL -> {
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

    private boolean selectMateriaStackFromSource() {
        BlockEntity be = construct.asEntity().level().getBlockEntity(this.takeFromTarget);
        SimpleContainer contents = new SimpleContainer(1);

        if(be instanceof AbstractDistillationBlockEntity adbe) {
            contents = adbe.getContentsOfOutputSlots();
        } else if(be instanceof AbstractSeparationBlockEntity asbe) {
            contents = asbe.getContentsOfOutputSlots();
        } else if(be instanceof IRouterBlockEntity irbe) {
            BlockEntity mbe = irbe.getMaster();
            if(mbe instanceof AbstractDistillationBlockEntity adbe) {
                contents = adbe.getContentsOfOutputSlots();
            } else if(mbe instanceof AbstractSeparationBlockEntity asbe) {
                contents = asbe.getContentsOfOutputSlots();
            }
        }

        if(!contents.isEmpty()) {
            int largestStackSize = -1;
            ItemStack stack = null;
            for(int i=0; i< contents.getContainerSize(); i++) {
                if(contents.getItem(i) != ItemStack.EMPTY && contents.getItem(i).getItem() instanceof MateriaItem mi) {
                    if(contents.getItem(i).getCount() > largestStackSize) {
                        stack = contents.getItem(i);
                        largestStackSize = stack.getCount();
                    }
                }
            }

            if(stack != ItemStack.EMPTY) {
                Map<AbstractMateriaStorageBlockEntity, BlockPos> materiaVesselsInRegion = getMateriaVesselsInRegion();

                boolean hasDestination = false;
                for(AbstractMateriaStorageBlockEntity amsbe : materiaVesselsInRegion.keySet()) {
                    if(amsbe.getMateriaType() == (MateriaItem) stack.getItem()) {
                        hasDestination = true;
                        break;
                    }
                }
                if(!hasDestination) {
                    for (AbstractMateriaStorageBlockEntity amsbe : materiaVesselsInRegion.keySet()) {
                        if (amsbe.getMateriaType() == null) {
                            hasDestination = true;
                            break;
                        }
                    }
                }

                this.filter = (MateriaItem) stack.getItem();

                if(hasDestination) {

                    int stackLimit = Math.max(2, construct.getConstructData().getAffinityScore(Affinity.ARCANE) * 4);
                    if (construct.getConstructData().calculateFluidCapacity() > 0) {
                        FluidStack fluidInTank = construct.getFluidInTank(0);
                        fluidInTank.getAmount();
                        if (fluidInTank.isEmpty())
                            stackLimit += 32;
                    }
                    int amountToShrink = Math.min(stackLimit, stack.getCount());
                    stack.shrink(amountToShrink);
                    this.materiaInTransit = new ItemStack(filter, amountToShrink);
                    return true;
                } else {
                    String targetMateria = getTranslatedNameFromItem(this.filter);
                    this.pushDiagnosticMessage("I couldn't find anywhere to put this " + targetMateria + ". Sorry, boss!", false);
                    return false;
                }
            } else {
                stop();
            }
        }
        return false;
    }

    @NotNull
    private String getTranslatedNameFromItem(MateriaItem pItem) {
        String prefix = "";
        if(pItem instanceof EssentiaItem) prefix = "essentia_";
        else if(pItem instanceof AdmixtureItem) prefix = "admixture_";
        return Component.translatable("item.magichem." + prefix + filter.getMateriaName()).getString();
    }

    private int doMateriaTransfer() {
        int transferredAmount;
        if (this.jarTargetEntity.getMateriaType() == null) {
            this.jarTargetEntity.setContents(filter, materiaInTransit.getCount());
            transferredAmount = materiaInTransit.getCount();
        } else {
            transferredAmount = jarTargetEntity.fill(materiaInTransit.getCount(), this.voidExcess);
        }
        materiaInTransit = ItemStack.EMPTY;
        return transferredAmount;
    }

    private Map<AbstractMateriaStorageBlockEntity, BlockPos> getMateriaVesselsInRegion() {
        Map<AbstractMateriaStorageBlockEntity, BlockPos> output = new HashMap<>();

        Level level = construct.asEntity().level();

        for(int x=(int)area.minX; x<=(int)area.maxX; x++){
            for(int y=(int)area.minY; y<=(int)area.maxY; y++) {
                for (int z=(int)area.minZ; z<=(int)area.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if(level.getBlockState(pos).getBlock() instanceof MateriaJarBlock || level.getBlockState(pos).getBlock() instanceof MateriaVesselBlock) {
                        output.put((AbstractMateriaStorageBlockEntity) level.getBlockEntity(pos), pos);
                    }
                }
            }
        }

        return output;
    }

    private void setTargetVessel(MateriaItem filter) {
        AbstractMateriaStorageBlockEntity firstEmpty = null;
        BlockPos firstEmptyPos = null;
        Map<AbstractMateriaStorageBlockEntity, BlockPos> map = getMateriaVesselsInRegion();
        boolean foundFilter = false;
        for(AbstractMateriaStorageBlockEntity mvbe : map.keySet()) {
            if (mvbe.getMateriaType() == null) {
                if (firstEmpty == null) {
                    firstEmpty = mvbe;
                    firstEmptyPos = map.get(mvbe);
                }
            } else if (mvbe.getMateriaType() == filter) {
                jarTargetEntity = mvbe;
                jarTargetPos = map.get(mvbe);
                foundFilter = true;
                break;
            }
        }

        if(!foundFilter) {
            jarTargetEntity = firstEmpty;
            jarTargetPos = firstEmptyPos;
        }
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.SORT_MATERIA_FROM_DEVICE);
    }

    @Override
    public ConstructSortMateriaFromDevice duplicate() {
        return new ConstructSortMateriaFromDevice(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructSortMateriaFromDevice copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructSortMateriaFromDevice task) {
            this.takeFromTarget = task.takeFromTarget;
            this.area = task.area;
            this.voidExcess = task.voidExcess;
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
        parameters.add(new ConstructTaskPointParameter("sort_materia.point"));
        parameters.add(new ConstructTaskAreaParameter("sort_materia.area"));
        parameters.add(new ConstructTaskBooleanParameter("sort_materia.boolean"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("sort_materia.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.takeFromTarget = pointParam.getPosition();
            }
        });

        this.getParameter("sort_materia.area").ifPresent((param) -> {
            if (param instanceof ConstructTaskAreaParameter areaParam) {
                this.area = null;

                if(areaParam.getPoints() != null) {
                    if(areaParam.getArea() != null)
                        this.area = areaParam.getArea();
                }
            }
        });

        this.getParameter("sort_materia.boolean").ifPresent((param) -> {
            if(param instanceof ConstructTaskBooleanParameter boolParam) {
                this.voidExcess = boolParam.getValue();
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.area != null && this.takeFromTarget != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_DISPENSE};
    }

    enum ETaskPhase {
        SETUP,
        MOVE_TO_DEVICE,
        WAIT_AT_DEVICE,
        MOVE_TO_VESSEL,
        WAIT_AT_VESSEL,
        WAIT_TO_FAIL
    }
}