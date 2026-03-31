package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.ext.*;
import com.aranaira.magichem.block.entity.routers.IRouterBlockEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.enums.ShlorpParticleMode;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.construct.*;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConstructSortMateriaFromDevice extends ConstructAITask<ConstructSortMateriaFromDevice> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos takeFromTarget, jarTargetPos;
    private BlockEntity jarTargetEntity;
    private AABB area;
    private boolean voidExcess;
    private MateriaItem filter;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer;
    private static final Random r = new Random();
    public static final float[] SHLORP_SPEEDS = new float[]{0.01f,0.026f,0.035f,0.053f,0.079f,0.121f};
    public static final float[] SHLORP_DELAY_MULT = new float[]{1.9f, 1.7f, 1.5f, 1.2f, 0.9f, 0.6f};
    public static final int[] SHLORP_DELAY_STATIC = new int[]{34, 27, 22, 18, 14, 11};

    public ConstructSortMateriaFromDevice(IConstruct<?> construct, ResourceLocation guiIcon) {
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
                    this.filter = null;

                    this.setMoveTarget(takeFromTarget);
                    this.phase = ETaskPhase.MOVE_TO_MIDPOINT;
                }
                case WAIT_TO_FAIL -> {
                    this.waitTimer--;
                    if(this.waitTimer <= 0) {
                        construct.clearForcedAnimation();
                        this.forceFail();
                    }
                }
                //advanced mode
                case MOVE_TO_MIDPOINT -> {
                    if(doMove(5.0F)) {
                        boolean foundTargetAndSource = this.selectMateriaStackFromSource();
                        CompoundTag persistentData = construct.asEntity().getPersistentData();
                        if(persistentData.contains("transitMateria")) {
                            if(!persistentData.getCompound("transitMateria").getString("id").equals("minecraft:air")) {
                                this.filter = (MateriaItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation(persistentData.getCompound("transitMateria").getString("id")));
                            }
                        }

                        if(this.filter != null) {
                            if(foundTargetAndSource) {
                                this.phase = ETaskPhase.CREATE_SHLORP;
                                this.setTargetVessel(this.filter);
                            }
                        } else {
                            this.pushDiagnosticMessage("The device I'm monitoring is empty right now. I'll just wait for a bit!", false);
                            this.phase = ETaskPhase.WAIT_TO_FAIL;
                            construct.clearForcedAnimation();
                        }
                    }
                }
                case CREATE_SHLORP -> {

                    int amount = doShlorpCreation();
                    this.waitTimer = Math.round(amount * SHLORP_DELAY_MULT[construct.getEquivalentTier()]) + SHLORP_DELAY_STATIC[construct.getEquivalentTier()];
                    if(amount > 0) {

                        InteractionHand interactionHand = construct.getHandWithCapability(ConstructCapability.CAST_SPELL).get();
                        if(interactionHand == InteractionHand.MAIN_HAND)
                            construct.forceAnimation(Animations.CHANNEL_LEFT, true);
                        else
                            construct.forceAnimation(Animations.CHANNEL_RIGHT, true);

                        this.pushDiagnosticMessage("I moved " + amount + " " + getTranslatedNameFromItem(this.filter) + " to a vessel, boss. Bloop!", true);
                        this.phase = ETaskPhase.WAIT_AT_VESSEL;

                    } else {

                        this.pushDiagnosticMessage("I couldn't find a jar to put the " + getTranslatedNameFromItem(this.filter) + " in. Sorry, boss!", true);
                        this.phase = ETaskPhase.WAIT_TO_FAIL;
                    }
                    this.swingHandWithCapability(ConstructCapability.FLUID_DISPENSE);
                }
                case WAIT_AT_VESSEL -> {
                    this.waitTimer--;
                    if(this.waitTimer <= 0) {
                        construct.clearForcedAnimation();
                        this.setSuccessCode();
                        this.setMoveTarget(takeFromTarget);
                        this.phase = ETaskPhase.MOVE_TO_MIDPOINT;
                    }
                }
            }
        }
    }

    private boolean selectMateriaStackFromSource() {
        BlockEntity be = construct.asEntity().level().getBlockEntity(this.takeFromTarget);
        SimpleContainer contents = new SimpleContainer(1);

        if(be instanceof AbstractDistillationBlockEntity distillation) {
            contents = distillation.getContentsOfOutputSlots();
        } else if(be instanceof AbstractSeparationBlockEntity separation) {
            contents = separation.getContentsOfOutputSlots();
            if(contents.isEmpty() && separation.getRecipeItem().isEmpty()) {
                contents = separation.getContentsOfInputSlots();
            }
        } else if(be instanceof AbstractFixationBlockEntity fixation) {
            contents = fixation.getContentsOfOutputSlots();
            if(contents.isEmpty() && fixation.getRecipeItem().isEmpty()) {
                contents = fixation.getContentsOfInputSlots();
            }
        } else if(be instanceof AbstractFabricationBlockEntity fabrication) {
            if(fabrication.getRecipeItem().isEmpty()) {
                contents = fabrication.getContentsOfInputSlots();
            }
        } else if(be instanceof IRouterBlockEntity router) {
            BlockEntity mbe = router.getMaster();
            if(mbe instanceof AbstractDistillationBlockEntity distillation) {
                contents = distillation.getContentsOfOutputSlots();
            } else if(mbe instanceof AbstractSeparationBlockEntity separation) {
                contents = separation.getContentsOfOutputSlots();
                if(contents.isEmpty() && separation.getRecipeItem().isEmpty()) {
                    if(separation instanceof CentrifugeBlockEntity centrifuge) {
                        contents = centrifuge.getContentsOfInputSlots(CentrifugeBlockEntity::getVar);
                    }
                    else if(separation instanceof GrandCentrifugeBlockEntity grandCentrifuge) {
                        contents = grandCentrifuge.getContentsOfInputSlots(GrandCentrifugeBlockEntity::getVar);
                    }
                }
            } else if(mbe instanceof AbstractFixationBlockEntity fixation) {
                contents = fixation.getContentsOfOutputSlots();
                if(contents.isEmpty() && fixation.getRecipeItem().isEmpty()) {
                    if(fixation instanceof FuseryBlockEntity fusery) {
                        contents = fusery.getContentsOfInputSlots(FuseryBlockEntity::getVar);
                    }
                    else if(fixation instanceof GrandFuseryBlockEntity grandFusery) {
                        contents = grandFusery.getContentsOfInputSlots(GrandFuseryBlockEntity::getVar);
                    }
                }
            } else if(mbe instanceof AbstractFabricationBlockEntity fabrication) {
                if(fabrication.getRecipeItem().isEmpty()) {
                    if(fabrication instanceof CircleFabricationBlockEntity circle) {
                        contents = circle.getContentsOfInputSlots(CircleFabricationBlockEntity::getVar);
                    }
                    else if(fabrication instanceof GrandCircleFabricationBlockEntity grandCircle) {
                        contents = grandCircle.getContentsOfInputSlots(GrandCircleFabricationBlockEntity::getVar);
                    }
                }
            }
        }

        if(!contents.isEmpty()) {
            int largestStackSize = -1;
            ItemStack stack = null;
            for(int i=0; i< contents.getContainerSize(); i++) {
                if(contents.getItem(i) != ItemStack.EMPTY && contents.getItem(i).getItem() instanceof MateriaItem mi && InventoryHelper.hasCustomModelData(contents.getItem(i))) {
                    if(contents.getItem(i).getCount() > largestStackSize) {
                        stack = contents.getItem(i);
                        largestStackSize = stack.getCount();
                    }
                }
            }

            if(stack != ItemStack.EMPTY) {
                HashMap<MateriaItem, List<BlockEntity>> allStorage = getMateriaStorageInRegion();

                boolean hasDestination = false;
                for(MateriaItem materiaStorageQuery : allStorage.keySet()) {
                    for(int i=0; i<allStorage.get(materiaStorageQuery).size(); i++) {
                        BlockEntity storageQuery = allStorage.get(materiaStorageQuery).get(i);
                        if(storageQuery instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
                            if (single.getMateriaType() == (MateriaItem) stack.getItem()) {
                                hasDestination = true;
                                break;
                            }
                        }
                        else if(storageQuery instanceof AbstractMateriaStorageMultiTypeBlockEntity multi) {
                            for(MateriaItem blockMateriaQuery : multi.getMateriaTypes()) {
                                if (blockMateriaQuery == (MateriaItem) stack.getItem()) {
                                    hasDestination = true;
                                    break;
                                }
                            }
                            if(hasDestination) break;
                        }
                    }
                    if(hasDestination) break;
                }
                if(!hasDestination) {
                    for (MateriaItem materiaStorageQuery : allStorage.keySet()) {
                        if (materiaStorageQuery == null) {
                            hasDestination = true;
                            break;
                        }
                    }
                }

                this.filter = (MateriaItem) stack.getItem();

                if(hasDestination) {

                    int stackLimit = getCollectionLimit();
                    int amountToShrink = Math.min(stackLimit, stack.getCount());
                    stack.shrink(amountToShrink);

                    ItemStack transitMateria = new ItemStack(filter, amountToShrink);
                    CompoundTag nbt = construct.asEntity().getPersistentData();
                    nbt.put("transitMateria", transitMateria.serializeNBT());

                    construct.asEntity().addAdditionalSaveData(nbt);
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
        return filter != null ? Component.translatable("item.magichem." + prefix + filter.getMateriaName()).getString() : Component.literal("(Not Found)").getString();
    }

    private int doMateriaTransfer() {
        ItemStack transitMateria = ItemStack.EMPTY;
        CompoundTag constructNBT = construct.asEntity().getPersistentData();
        if(constructNBT.contains("transitMateria")) {
            CompoundTag itemTag = constructNBT.getCompound("transitMateria");
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemTag.getString("id")));
            int count = itemTag.getByte("Count");
            transitMateria = new ItemStack(item, count);
        }
        int transferredAmount = 0;

        if(this.jarTargetEntity instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
            if (single.getMateriaType() == null) {
                single.setContents(filter, transitMateria.getCount());
                transferredAmount = transitMateria.getCount();
            } else {
                transferredAmount = single.fill(transitMateria.getCount(), this.voidExcess);
            }
        }
        else if(this.jarTargetEntity instanceof AbstractMateriaStorageMultiTypeBlockEntity multi) {
            boolean didTransfer = false;
            for(MateriaItem materiaBlockQuery : multi.getMateriaTypes()) {
                if (materiaBlockQuery == filter) {
                    transferredAmount = multi.fill(materiaBlockQuery, transitMateria.getCount(), this.voidExcess);
                    didTransfer = true;
                    break;
                }
            }
            if(!didTransfer) {
                multi.setContents(filter, transitMateria.getCount());
                transferredAmount = transitMateria.getCount();
            }
        }

        if(transferredAmount == transitMateria.getCount()) {
            constructNBT.remove("transitMateria");
            construct.asEntity().addAdditionalSaveData(constructNBT);
        } else {
            transitMateria.setCount(transitMateria.getCount() - transferredAmount);
            constructNBT.getCompound("transitMateria").putInt("Count", transferredAmount);
        }
        return transferredAmount;
    }

    private int doShlorpCreation() {
        ItemStack transitMateria = ItemStack.EMPTY;
        CompoundTag constructNBT = construct.asEntity().getPersistentData();
        int transferredAmount = 0;
        float speedFactor = SHLORP_SPEEDS[construct.getEquivalentTier()];

        if(constructNBT.contains("transitMateria")) {
            CompoundTag itemTag = constructNBT.getCompound("transitMateria");
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemTag.getString("id")));
            int count = itemTag.getByte("Count");
            transitMateria = new ItemStack(item, count);
            transferredAmount = count;
        }

        if(jarTargetEntity == null) {
            //Nothing we can do if the filter is also fucked
            if(filter == null)
                return 0;

            //This is a problem; see if we can reset the target
            setTargetVessel(filter);

            //If we're still null, gotta abort process
            if(jarTargetEntity == null)
                return 0;
        }

        //force the type on the destination container to prevent voiding a ton of materia types
        if(jarTargetEntity instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
            if (single.getMateriaType() == null)
                single.setContents((MateriaItem) transitMateria.getItem(), 0);
        }
        else if(jarTargetEntity instanceof AbstractMateriaStorageMultiTypeBlockEntity multi) {
            if(!multi.containsMateriaType(filter))
                multi.setContents((MateriaItem) transitMateria.getItem(), 0);
        }

        //create shlorp here
        if(!transitMateria.isEmpty()) {
            Level level = construct.asEntity().level();
            BlockEntity startpoint = level.getBlockEntity(takeFromTarget);
            BlockEntity endpoint = level.getBlockEntity(jarTargetPos);

            if (endpoint != null && startpoint != null) {
                Vector3 sP = new Vector3(startpoint.getBlockPos().getX(), startpoint.getBlockPos().getY(), startpoint.getBlockPos().getZ());
                Vector3 eP = Vector3.zero(), eO = Vector3.zero(), eT = Vector3.up().scale(6);

                if (endpoint instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
                    eP = new Vector3(endpoint.getBlockPos().getX(), endpoint.getBlockPos().getY(), endpoint.getBlockPos().getZ());

                    Pair<Vector3, Vector3> defaultOriginAndTangent = single.getDefaultOriginAndTangent();
                    eO = defaultOriginAndTangent.getFirst();
                    eT = defaultOriginAndTangent.getSecond().scale(6);
                }
                else if (endpoint instanceof AbstractMateriaStorageMultiTypeBlockEntity multi) {
                    eP = new Vector3(endpoint.getBlockPos().getX(), endpoint.getBlockPos().getY(), endpoint.getBlockPos().getZ());

                    Pair<Vector3, Vector3> defaultOriginAndTangent = multi.getDefaultOriginAndTangent(filter);
                    eO = defaultOriginAndTangent.getFirst();
                    eT = defaultOriginAndTangent.getSecond().scale(6);
                }

                ShlorpEntity shlorp = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), level);
                shlorp.setPos(new Vec3(sP.x, sP.y, sP.z));
                shlorp.configure(
                        sP, new Vector3(0.5, 0.5, 0.5), Vector3.up().scale(r.nextFloat() * 2.5f + 1f),
                        eP, eO, eT,
                        speedFactor, 0.0625f,
                        4 + transferredAmount,
                        (MateriaItem) transitMateria.getItem(),
                        transitMateria.getCount(),
                        ShlorpParticleMode.NONE);
                shlorp.setFallback(endpoint.getBlockPos());
                level.addFreshEntity(shlorp);
            }
        }

        constructNBT.put("transitMateria", ItemStack.EMPTY.serializeNBT());
        construct.asEntity().addAdditionalSaveData(constructNBT);
        return Math.min(1,Math.round(transferredAmount * (0.035f / speedFactor)));
    }

    private HashMap<MateriaItem, List<BlockEntity>> getMateriaStorageInRegion() {
        Level level = construct.asEntity().level();
        return InventoryHelper.getAllMateriaStorageInZone(level,
                (int)area.minX, (int)area.minY, (int)area.minZ,
                (int)area.maxX, (int)area.maxY, (int)area.maxZ);
    }

    private void setTargetVessel(MateriaItem filter) {
        BlockEntity firstEmpty = null;
        BlockPos firstEmptyPos = null;
        HashMap<MateriaItem, List<BlockEntity>> allStorage = getMateriaStorageInRegion();
        boolean foundFilter = false;

        for(MateriaItem materiaStorageQuery : allStorage.keySet()) {
            for (int i=0; i<allStorage.get(materiaStorageQuery).size(); i++) {
                BlockEntity beQuery = allStorage.get(materiaStorageQuery).get(i);

                if(beQuery instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
                    if (single.getMateriaType() == null) {
                        if (firstEmpty == null) {
                            firstEmpty = single;
                            firstEmptyPos = single.getBlockPos();
                        }
                    } else if (single.getMateriaType() == filter) {
                        jarTargetEntity = single;
                        jarTargetPos = single.getBlockPos();
                        foundFilter = true;
                        if (voidExcess)
                            break;
                    }
                }
                else if(beQuery instanceof AbstractMateriaStorageMultiTypeBlockEntity multi) {
                    final Collection<MateriaItem> materiaBlockTypes = multi.getMateriaTypes();
                    for(MateriaItem materiaBlockQuery : materiaBlockTypes) {
                        if (materiaBlockQuery == filter) {
                            jarTargetEntity = multi;
                            jarTargetPos = multi.getBlockPos();
                            foundFilter = true;
                            if (voidExcess) {
                                break;
                            }
                        }
                    }
                    if(foundFilter) break;
                    else if(multi.isBelowTypeLimit()) {
                        firstEmpty = multi;
                        firstEmptyPos = multi.getBlockPos();
                    }
                }

                if (foundFilter && firstEmpty != null)
                    break;
            }

            if (foundFilter && firstEmpty != null)
                break;
        }

        if(!voidExcess) {
            if(foundFilter) {
                if(jarTargetEntity instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
                    if (single.getCurrentStock() >= single.getStorageLimit()) {
                        jarTargetEntity = firstEmpty;
                        jarTargetPos = firstEmptyPos;
                    }
                }
                else if(jarTargetEntity instanceof AbstractMateriaStorageMultiTypeBlockEntity multi) {
                    if (multi.getCurrentStock(filter) >= multi.getStorageLimit(filter)) {
                        jarTargetEntity = firstEmpty;
                        jarTargetPos = firstEmptyPos;
                    }
                }
            } else {
                jarTargetEntity = firstEmpty;
                jarTargetPos = firstEmptyPos;
            }
        }
        else if(!foundFilter) {
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
        requiredCaps = new ConstructCapability[]{ConstructCapability.CAST_SPELL};
    }

    enum ETaskPhase {
        SETUP,
        WAIT_AT_VESSEL,
        WAIT_TO_FAIL,
        MOVE_TO_MIDPOINT,
        CREATE_SHLORP
    }
}