package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.MateriaJarBlock;
import com.aranaira.magichem.block.MateriaVesselBlock;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.construct.*;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConstructProvideMateria extends ConstructAITask<ConstructProvideMateria> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos takeFromTarget, deviceTargetPos;
    private AbstractMateriaStorageBlockEntity jarTargetEntity;
    private AABB area;
    private MateriaItem filter;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer, craftCount;
    private boolean isAdvancedMode = false, deliverPartialLoad = false;
    private static final Random r = new Random();

    public ConstructProvideMateria(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        filter = null;
        jarTargetEntity = null;

        //check to see if this construct can do shlorps
        IConstructConstruction constructData = construct.getConstructData();
        boolean correctMaterialTier = true;
        for (ConstructMaterial mat : constructData.getComposition()) {
            if(mat == ConstructMaterial.WICKERWOOD || mat == ConstructMaterial.WOOD || mat == ConstructMaterial.STONE) {
                correctMaterialTier = false;
                break;
            }
        }

        boolean smartHead = constructData.calculateIntelligence() > 8;

        boolean casterArm = constructData.isCapabilityEnabled(ConstructCapability.CAST_SPELL);

        isAdvancedMode = correctMaterialTier && smartHead && casterArm;
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            switch (this.phase) {
                case SETUP -> {
                    this.filter = null;

                    //TODO: should check to see if there's materia waiting in NBT

                    BlockEntity be = construct.asEntity().level().getBlockEntity(deviceTargetPos);
                    if(be instanceof IMateriaProvisionRequester impr) {
                        if(impr.needsProvisioning()) {
                            final Map<AbstractMateriaStorageBlockEntity, BlockPos> allVessels = getMateriaVesselsInRegion();

                            boolean foundTarget = false;
                            for (MateriaItem mi : impr.getProvisioningNeeds().keySet()) {
                                for(AbstractMateriaStorageBlockEntity amsbe : allVessels.keySet()) {
                                    if(amsbe.getMateriaType() == mi) {
                                        this.jarTargetEntity = amsbe;
                                        this.filter = mi;

                                        this.waitTimer = 21;
                                        this.phase = ETaskPhase.MOVE_TO_VESSEL;
                                        this.takeFromTarget = allVessels.get(amsbe);
                                        this.setMoveTarget(this.takeFromTarget);
                                        foundTarget = true;
                                        break;
                                    }
                                }
                            }
                            //diagnostic message that there isn't a jar with the right materia in zone

                            if(!foundTarget) {
                                this.waitTimer = 21;
                                this.phase = ETaskPhase.WAIT_TO_FAIL;
                            }
                        } else {
                            this.waitTimer = 21;
                            this.phase = ETaskPhase.WAIT_TO_FAIL;
                        }
                    }
                }
                case MOVE_TO_VESSEL -> {
                    if(doMove(2.0f)) {
                        BlockEntity be = construct.asEntity().level().getBlockEntity(deviceTargetPos);
                        if(be instanceof IMateriaProvisionRequester impr) {
                            if (impr.needsProvisioning()) {
                                if(impr.getProvisioningNeeds().containsKey(filter)) {
                                    final int required = impr.getProvisioningNeeds().get(filter);

                                    int collectionLimit = Math.min(required * craftCount, getCollectionLimit());
                                    if(jarTargetEntity.getMateriaType() == filter) {
                                        final ItemStack extracted = jarTargetEntity.extractMateria(collectionLimit);
                                        CompoundTag nbt = construct.asEntity().getPersistentData();
                                        nbt.put("transitMateria", extracted.serializeNBT());

                                        construct.asEntity().addAdditionalSaveData(nbt);
                                    }


                                    impr.setProvisioningInProgress(filter);

                                    this.waitTimer = 21;
                                    this.phase = ETaskPhase.WAIT_AT_VESSEL;
                                } else {
                                    this.waitTimer = 21;
                                    this.phase = ETaskPhase.WAIT_TO_FAIL;
                                }
                            }
                        }
                    }
                }
                case WAIT_AT_VESSEL -> {
                    this.waitTimer--;
                    if(this.waitTimer <= 0) {
                        this.phase = ETaskPhase.MOVE_TO_DEVICE;
                        this.setMoveTarget(this.deviceTargetPos);
                    }
                }
                case MOVE_TO_DEVICE -> {
                    if (doMove(2.0f)) {
                        BlockEntity be = construct.asEntity().level().getBlockEntity(deviceTargetPos);
                        if(be instanceof IMateriaProvisionRequester impr) {
                            ItemStack transitMateria = ItemStack.EMPTY;
                            CompoundTag constructNBT = construct.asEntity().getPersistentData();
                            if(constructNBT.contains("transitMateria")) {
                                CompoundTag itemTag = constructNBT.getCompound("transitMateria");
                                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemTag.getString("id")));
                                int count = itemTag.getByte("Count");
                                transitMateria = new ItemStack(item, count);

                                impr.provide(transitMateria);

                                this.waitTimer = 21;
                                this.phase = ETaskPhase.WAIT_AT_DEVICE;
                            }
                        }
                    }
                }
                case WAIT_AT_DEVICE -> {
                    this.waitTimer--;
                    if(this.waitTimer <= 0) {
                        this.setSuccessCode();
                    }
                }
                case WAIT_TO_FAIL -> {
                    this.waitTimer--;
                    if(this.waitTimer <= 0) {
                        this.forceFail();
                    }
                }
            }
        }
    }

    private int getCollectionLimit() {
        int stackLimit = Math.max(2, construct.getConstructData().getAffinityScore(Affinity.ARCANE) * 4);
        if (construct.getConstructData().calculateFluidCapacity() > 0) {
            FluidStack fluidInTank = construct.getFluidInTank(0);
            fluidInTank.getAmount();
            if (fluidInTank.isEmpty())
                stackLimit += 32;
        }
        return stackLimit;
    }

    @NotNull
    private String getTranslatedNameFromItem(MateriaItem pItem) {
        String prefix = "";
        if(pItem instanceof EssentiaItem) prefix = "essentia_";
        else if(pItem instanceof AdmixtureItem) prefix = "admixture_";
        return Component.translatable("item.magichem." + prefix + filter.getMateriaName()).getString();
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

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.PROVIDE_MATERIA);
    }

    @Override
    public ConstructProvideMateria duplicate() {
        return new ConstructProvideMateria(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructProvideMateria copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructProvideMateria task) {
            this.area = task.area;
            this.deviceTargetPos = task.deviceTargetPos;
            this.craftCount = task.craftCount;
            this.deliverPartialLoad = task.deliverPartialLoad;
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
        parameters.add(new ConstructTaskPointParameter("provide_materia.point"));
        parameters.add(new ConstructTaskAreaParameter("provide_materia.area"));
        parameters.add(new ConstructTaskIntegerParameter("provide_materia.int", 1, 10, 1, 1));
        parameters.add(new ConstructTaskBooleanParameter("provide_materia.boolean", false));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("provide_materia.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.deviceTargetPos = pointParam.getPosition();
            }
        });

        this.getParameter("provide_materia.area").ifPresent((param) -> {
            if (param instanceof ConstructTaskAreaParameter areaParam) {
                this.area = null;

                if(areaParam.getPoints() != null) {
                    if(areaParam.getArea() != null)
                        this.area = areaParam.getArea();
                }
            }
        });

        this.getParameter("provide_materia.int").ifPresent((param) -> {
            if(param instanceof ConstructTaskIntegerParameter intParam) {
                this.craftCount = intParam.getValue();
            }
        });

        this.getParameter("provide_materia.boolean").ifPresent((param) -> {
            if(param instanceof ConstructTaskBooleanParameter booleanParam) {
                this.deliverPartialLoad = booleanParam.getValue();
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.area != null && this.deviceTargetPos != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_DISPENSE};
    }

    enum ETaskPhase {
        SETUP,
        MOVE_TO_VESSEL,
        WAIT_AT_VESSEL,
        MOVE_TO_DEVICE,
        WAIT_AT_DEVICE,
        WAIT_TO_FAIL,
        MOVE_TO_MIDPOINT,
        CREATE_SHLORP
    }
}