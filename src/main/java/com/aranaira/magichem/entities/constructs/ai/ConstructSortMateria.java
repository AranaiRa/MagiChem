package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.MateriaVesselBlock;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.block.entity.interfaces.IMateriaProcessingDevice;
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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstructSortMateria extends ConstructAITask<ConstructSortMateria> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos takeFromTarget, jarTargetPos;
    private MateriaVesselBlockEntity jarTargetEntity;
    private AABB area;
    private boolean voidExcess;
    private MateriaItem filter;
    private ESortTaskPhase phase = ESortTaskPhase.SETUP;
    private int waitTimer;

    public ConstructSortMateria(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        filter = null;
        jarTargetEntity = null;
        jarTargetPos = null;
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            switch (this.phase) {
                case SETUP -> {
                    this.setMoveTarget(takeFromTarget);
                    this.filter = chooseTargetJarFilter();
                    this.setTargetVessel(this.filter);
                    this.phase = ESortTaskPhase.MOVE_TO_DEVICE;
                }
                case MOVE_TO_DEVICE -> {
                    if (doMove(2.0F)) {
                        this.phase = ESortTaskPhase.WAIT_AT_DEVICE;
                        this.waitTimer = 21;
                        if(this.filter == null) {
                            this.pushDiagnosticMessage("The device I'm monitoring is empty right now. I'll just wait for a bit!", false);
                            this.phase = ESortTaskPhase.WAIT_TO_FAIL;
                            this.swingHandWithCapability(ConstructCapability.FLUID_DISPENSE);
                        }
                    }
                }
                case WAIT_AT_DEVICE -> {
                    this.waitTimer--;
                    if(this.waitTimer == 0) {
                        this.phase = ESortTaskPhase.MOVE_TO_VESSEL;
                        this.setMoveTarget(this.jarTargetPos);
                    }
                }
                case MOVE_TO_VESSEL -> {
                    if(doMove(2.0F)) {
                        int amount = doMateriaTransfer();
                        if(amount > 0) {
                            this.pushDiagnosticMessage("I moved some materia to a vessel, boss. Bloop!", true);
                            this.phase = ESortTaskPhase.WAIT_AT_VESSEL;
                        } else {
                            this.pushDiagnosticMessage("I couldn't find a jar to put the materia in. Sorry, boss!", true);
                            this.phase = ESortTaskPhase.WAIT_TO_FAIL;
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

    private MateriaItem chooseTargetJarFilter() {
        BlockEntity be = construct.asEntity().level().getBlockEntity(takeFromTarget);
        if (be instanceof IMateriaProcessingDevice mpd) {
            SimpleContainer contents = mpd.getContentsOfOutputSlots();
            if (!contents.isEmpty()) {
                int slot = 0;
                ItemStack stack;
                MateriaItem type = null;
                while (type == null) {
                    stack = contents.getItem(slot);
                    if (stack != ItemStack.EMPTY && stack.getItem() instanceof MateriaItem mi) {
                        type = mi;
                    }
                    slot++;
                }
                return type;
            }
        }
        return null;
    }

    private int doMateriaTransfer() {
        int transferredAmount = 0;
        BlockEntity be = construct.asEntity().level().getBlockEntity(this.takeFromTarget);
        if(be instanceof IMateriaProcessingDevice mpd) {
            SimpleContainer contents = mpd.getContentsOfOutputSlots();
            if(!contents.isEmpty()) {
                ItemStack stack = null;
                for(int i=0; i< contents.getContainerSize(); i++) {
                    if(contents.getItem(i) != ItemStack.EMPTY && contents.getItem(i).getItem() instanceof MateriaItem mi) {
                        if(mi == filter)
                            stack = contents.getItem(i);
                    }
                }

                if(stack != null) {

                    if (this.jarTargetEntity == null) {
                        this.pushDiagnosticMessage("I couldn't find a jar to put the materia in. Sorry, boss!", true);
                        this.forceFail();
                        this.stop();
                        return 0;
                    }

                    int transferLimit = Math.min(construct.getCarrySize(), stack.getCount());

                    stack.shrink(transferLimit);
                    if (this.jarTargetEntity.getMateriaType() == null) {
                        this.jarTargetEntity.setContents(filter, transferLimit);
                        transferredAmount = transferLimit;
                    } else {
                        transferredAmount = jarTargetEntity.fill(transferLimit, this.voidExcess);
                    }
                } else
                    stop();
            }
        }
        return transferredAmount;
    }

    private Map<MateriaVesselBlockEntity, BlockPos> getMateriaVesselsInRegion() {
        Map<MateriaVesselBlockEntity, BlockPos> output = new HashMap<>();

        Level level = construct.asEntity().level();

        for(int x=(int)area.minX; x<=(int)area.maxX; x++){
            for(int y=(int)area.minY; y<=(int)area.maxY; y++) {
                for (int z=(int)area.minZ; z<=(int)area.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if(level.getBlockState(pos).getBlock() instanceof MateriaVesselBlock) {
                        output.put((MateriaVesselBlockEntity) level.getBlockEntity(pos), pos);
                    }
                }
            }
        }

        return output;
    }

    private void setTargetVessel(MateriaItem filter) {
        MateriaVesselBlockEntity firstEmpty = null;
        BlockPos firstEmptyPos = null;
        Map<MateriaVesselBlockEntity, BlockPos> map = getMateriaVesselsInRegion();
        boolean foundFilter = false;
        for(MateriaVesselBlockEntity mvbe : map.keySet()) {
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
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.SORT_MATERIA);
    }

    @Override
    public ConstructSortMateria duplicate() {
        return new ConstructSortMateria(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructSortMateria copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructSortMateria task) {
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
}
enum ESortTaskPhase {
    SETUP,
    MOVE_TO_DEVICE,
    WAIT_AT_DEVICE,
    MOVE_TO_VESSEL,
    WAIT_AT_VESSEL,
    WAIT_TO_FAIL
}
