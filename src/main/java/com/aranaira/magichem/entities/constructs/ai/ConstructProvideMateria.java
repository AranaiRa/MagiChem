package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageSingleTypeBlockEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
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
import com.mna.api.entities.construct.ai.parameter.*;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.aranaira.magichem.entities.constructs.ai.ConstructSortMateriaFromDevice.*;

public class ConstructProvideMateria extends ConstructAITask<ConstructProvideMateria> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos takeFromTarget, deviceTargetPos;
    private BlockEntity jarTargetEntity;
    private AABB area;
    private MateriaItem filter;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer, craftCount;
    private boolean leaveOneInContainer = false;
    private static final Random r = new Random();

    public ConstructProvideMateria(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        filter = null;
        jarTargetEntity = null;
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            switch (this.phase) {
                case SETUP -> {
                    this.filter = null;

                    this.setMoveTarget(this.deviceTargetPos);
                    BlockEntity be = construct.asEntity().level().getBlockEntity(deviceTargetPos);
                    if (be instanceof IMateriaProvisionRequester impr && impr.needsProvisioning()) {
                        this.phase = ETaskPhase.MOVE_TO_MIDPOINT;
                    } else {
                        this.phase = ETaskPhase.WAIT_TO_FAIL;
                    }
                }
                case MOVE_TO_MIDPOINT -> {
                    if(doMove(5.0F)) {
                        BlockEntity be = construct.asEntity().level().getBlockEntity(deviceTargetPos);
                        if(be instanceof IMateriaProvisionRequester impr) {
                            if(impr.needsProvisioning()) {
                                final HashMap<MateriaItem, List<BlockEntity>> allStorage = getMateriaStorageInRegion();
                                allStorage.remove(null);

                                boolean foundTarget = false;
                                for (MateriaItem materiaProvisionQuery : impr.getProvisioningNeeds().keySet()) {
                                    for (MateriaItem materiaStorageQuery : allStorage.keySet()) {
                                        if(materiaProvisionQuery != materiaStorageQuery) continue;



                                        for(int i=0; i<allStorage.get(materiaStorageQuery).size(); i++) {
                                            this.filter = materiaProvisionQuery;
                                            this.jarTargetEntity = allStorage.get(materiaStorageQuery).get(i);

                                            int stock = 0;
                                            if(jarTargetEntity instanceof AbstractMateriaStorageSingleTypeBlockEntity single) stock = single.getCurrentStock();
                                            else if(jarTargetEntity instanceof AbstractMateriaStorageMultiTypeBlockEntity multi) stock = multi.getCurrentStock(filter);

                                            boolean leaveOneMode = leaveOneInContainer && materiaStorageQuery == materiaProvisionQuery && stock > 1;
                                            boolean leaveNoneMode = !leaveOneInContainer && materiaStorageQuery == materiaProvisionQuery;

                                            if(leaveOneMode || leaveNoneMode) {
                                                foundTarget = true;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if(foundTarget) {
                                    this.phase = ETaskPhase.CREATE_SHLORP;
                                } else {
                                    this.pushDiagnosticMessage("I can't find any of the materia the device needs. I'll just wait for a bit!", false);
                                    this.waitTimer = 41;
                                    this.phase = ETaskPhase.WAIT_TO_FAIL;
                                    construct.clearForcedAnimation();
                                }
                            } else {
                                this.pushDiagnosticMessage("The device I'm monitoring doesn't need any materia provided right now. I'll just wait for a bit!", false);
                                this.waitTimer = 41;
                                this.phase = ETaskPhase.WAIT_TO_FAIL;
                                construct.clearForcedAnimation();
                            }
                        }
                    }
                }
                case CREATE_SHLORP -> {
                    BlockEntity be = construct.asEntity().level().getBlockEntity(deviceTargetPos);
                    if(be instanceof IMateriaProvisionRequester impr) {
                        if (impr.needsProvisioning()) {
                            if (impr.getProvisioningNeeds().containsKey(filter)) {
                                final int required = impr.getProvisioningNeeds().get(filter);

                                int collectionLimit = Math.min(required * (impr.allowIncreasedDeliverySize() ? craftCount : 1), getCollectionLimit());

                                if(jarTargetEntity instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
                                    if (single.getMateriaType() == filter) {
                                        ItemStack extracted = single.extractMateria(collectionLimit, leaveOneInContainer);
                                        this.waitTimer = Math.round(extracted.getCount() * SHLORP_DELAY_MULT[construct.getEquivalentTier()]) + SHLORP_DELAY_STATIC[construct.getEquivalentTier()];
                                        float speedFactor = SHLORP_SPEEDS[construct.getEquivalentTier()];

                                        if (extracted.getCount() > 0) {
                                            //create shlorp
                                            {
                                                if (!extracted.isEmpty()) {
                                                    impr.setProvisioningInProgress(filter);

                                                    Level level = construct.asEntity().level();
                                                    BlockEntity startpoint = jarTargetEntity;
                                                    BlockEntity endpoint = level.getBlockEntity(deviceTargetPos);

                                                    if (endpoint != null && startpoint != null) {
                                                        Vector3 sP, sO, sT;
                                                        Vector3 eP = new Vector3(endpoint.getBlockPos().getX(), endpoint.getBlockPos().getY(), endpoint.getBlockPos().getZ());

                                                        sP = new Vector3(startpoint.getBlockPos().getX(), startpoint.getBlockPos().getY(), startpoint.getBlockPos().getZ());

                                                        Pair<Vector3, Vector3> defaultOriginAndTangent = single.getDefaultOriginAndTangent();
                                                        sO = defaultOriginAndTangent.getFirst();
                                                        sT = defaultOriginAndTangent.getSecond().scale(6);

                                                        ShlorpEntity shlorp = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), level);
                                                        shlorp.setPos(new Vec3(sP.x, sP.y, sP.z));
                                                        shlorp.configure(
                                                                sP, sO, sT,
                                                                eP, new Vector3(0.5, 0.5, 0.5), Vector3.up().scale(r.nextFloat() * 3.0f + 3f),
                                                                speedFactor, 0.125f,
                                                                4 + extracted.getCount(),
                                                                (MateriaItem) extracted.getItem(),
                                                                extracted.getCount(),
                                                                ShlorpParticleMode.DESTINATION_TANGENT);
                                                        level.addFreshEntity(shlorp);
                                                    }
                                                }
                                            }

                                            InteractionHand interactionHand = construct.getHandWithCapability(ConstructCapability.CAST_SPELL).get();
                                            if (interactionHand == InteractionHand.MAIN_HAND)
                                                construct.forceAnimation(Animations.CHANNEL_LEFT, true);
                                            else
                                                construct.forceAnimation(Animations.CHANNEL_RIGHT, true);

                                            this.pushDiagnosticMessage("I moved " + extracted.getCount() + " " + getTranslatedNameFromItem(this.filter) + " to the device, boss. Shloop!", true);
                                        }

                                        this.phase = ETaskPhase.WAIT_AT_DEVICE;
                                    }
                                }
                                else if(jarTargetEntity instanceof AbstractMateriaStorageMultiTypeBlockEntity multi) {
                                    for(MateriaItem materiaStorageQuery : multi.getMateriaTypes()) {
                                        if (materiaStorageQuery == filter) {
                                            ItemStack extracted = new ItemStack(filter, multi.drain(filter, collectionLimit, leaveOneInContainer));
                                            this.waitTimer = Math.round(extracted.getCount() * 1.5f) + 22;

                                            if (extracted.getCount() > 0) {
                                                //create shlorp
                                                {
                                                    if (!extracted.isEmpty()) {
                                                        impr.setProvisioningInProgress(filter);

                                                        Level level = construct.asEntity().level();
                                                        BlockEntity startpoint = jarTargetEntity;
                                                        BlockEntity endpoint = level.getBlockEntity(deviceTargetPos);

                                                        if (endpoint != null && startpoint != null) {
                                                            Vector3 sP, sO, sT;
                                                            Vector3 eP = new Vector3(endpoint.getBlockPos().getX(), endpoint.getBlockPos().getY(), endpoint.getBlockPos().getZ());

                                                            sP = new Vector3(startpoint.getBlockPos().getX(), startpoint.getBlockPos().getY(), startpoint.getBlockPos().getZ());

                                                            Pair<Vector3, Vector3> defaultOriginAndTangent = multi.getDefaultOriginAndTangent(filter);
                                                            sO = defaultOriginAndTangent.getFirst();
                                                            sT = defaultOriginAndTangent.getSecond().scale(6);

                                                            ShlorpEntity shlorp = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), level);
                                                            shlorp.setPos(new Vec3(sP.x, sP.y, sP.z));
                                                            shlorp.configure(
                                                                    sP, sO, sT,
                                                                    eP, new Vector3(0.5, 0.5, 0.5), Vector3.up().scale(r.nextFloat() * 3.0f + 3f),
                                                                    0.035f, 0.125f,
                                                                    4 + extracted.getCount(),
                                                                    (MateriaItem) extracted.getItem(),
                                                                    extracted.getCount(),
                                                                    ShlorpParticleMode.DESTINATION_TANGENT);
                                                            level.addFreshEntity(shlorp);
                                                        }
                                                    }
                                                }

                                                InteractionHand interactionHand = construct.getHandWithCapability(ConstructCapability.CAST_SPELL).get();
                                                if (interactionHand == InteractionHand.MAIN_HAND)
                                                    construct.forceAnimation(Animations.CHANNEL_LEFT, true);
                                                else
                                                    construct.forceAnimation(Animations.CHANNEL_RIGHT, true);

                                                this.pushDiagnosticMessage("I moved " + extracted.getCount() + " " + getTranslatedNameFromItem(this.filter) + " to the device, boss. Shloop!", true);
                                            }

                                            this.phase = ETaskPhase.WAIT_AT_DEVICE;
                                        }
                                    }
                                }
                            }
                        }
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

    private HashMap<MateriaItem, List<BlockEntity>> getMateriaStorageInRegion() {
        Level level = construct.asEntity().level();
        return InventoryHelper.getAllMateriaStorageInZone(level,
                (int)area.minX, (int)area.minY, (int)area.minZ,
                (int)area.maxX, (int)area.maxY, (int)area.maxZ);
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
            this.leaveOneInContainer = task.leaveOneInContainer;
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
        parameters.add(new ConstructTaskBooleanParameter("provide_materia.boolean", true));
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
                this.leaveOneInContainer = booleanParam.getValue();
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
        requiredCaps = new ConstructCapability[]{ConstructCapability.CAST_SPELL};
    }

    enum ETaskPhase {
        SETUP,
        WAIT_AT_DEVICE,
        WAIT_TO_FAIL,
        MOVE_TO_MIDPOINT,
        CREATE_SHLORP
    }
}