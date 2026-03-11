package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageSingleTypeBlockEntity;
import com.aranaira.magichem.entities.ItemShlorpEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.IItemProvisionRequester;
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
import com.mna.api.entities.construct.Animations;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.*;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.aranaira.magichem.entities.constructs.ai.ConstructSortMateriaFromDevice.*;

public class ConstructProvideItems extends ConstructAITask<ConstructProvideItems> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos takeFromTarget, deviceTargetPos;
    private BlockEntity targetInventoryEntity;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer, craftCount;
    private boolean leaveOneInContainer = false;
    private static final Random r = new Random();

    public ConstructProvideItems(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        targetInventoryEntity = null;
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            switch (this.phase) {
                case SETUP -> {
                    this.setMoveTarget(this.deviceTargetPos);
                    BlockEntity be = construct.asEntity().level().getBlockEntity(deviceTargetPos);
                    if (be instanceof IItemProvisionRequester iipr && iipr.needsItemProvisioning()) {
                        this.phase = ETaskPhase.MOVE_TO_MIDPOINT;
                    } else {
                        this.phase = ETaskPhase.WAIT_TO_FAIL;
                    }
                }
                case MOVE_TO_MIDPOINT -> {
                    if(doMove(5.0F)) {
                        BlockEntity be = construct.asEntity().level().getBlockEntity(deviceTargetPos);
                        BlockEntity tbe = construct.asEntity().level().getBlockEntity(takeFromTarget);
                        if(be instanceof IItemProvisionRequester iipr && tbe != null) {
                            if(iipr.needsItemProvisioning()) {
                                final NonNullList<ItemStack> needs = iipr.getItemProvisioningNeeds();

                                MutableBoolean hasAllItems = new MutableBoolean(true);

                                tbe.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(cap -> {
                                    //need to use a simple container copy of the handler because the same item type can be in multiple slots
                                    SimpleContainer sim = new SimpleContainer(cap.getSlots());
                                    for(int i=0; i<cap.getSlots(); i++) {
                                        sim.setItem(i, cap.extractItem(i, Integer.MAX_VALUE, true));
                                    }

                                    for (ItemStack need : needs) {
                                        int remaining = need.getCount();
                                        for(int i=0; i<cap.getSlots(); i++) {
                                            if(cap.getStackInSlot(i).getItem() == need.getItem()) {
                                                ItemStack removalQuery = sim.removeItemType(need.getItem(), need.getCount());
                                                if (!removalQuery.isEmpty()) {
                                                    remaining -= removalQuery.getCount();
                                                    if(remaining <= 0)
                                                        break;
                                                }
                                            }
                                        }
                                        if(remaining > 0) {
                                            hasAllItems.setValue(false);
                                            break;
                                        }
                                    }
                                });

                                if(hasAllItems.booleanValue()) {
                                    this.phase = ETaskPhase.CREATE_SHLORP;
                                } else {
                                    this.pushDiagnosticMessage("I can't find all of the items the device needs. I'll just wait for a bit!", false);
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
                    BlockEntity tbe = construct.asEntity().level().getBlockEntity(takeFromTarget);
                    if(be instanceof IItemProvisionRequester iipr && tbe != null) {
                        if (iipr.needsItemProvisioning()) {
                            final NonNullList<ItemStack> needs = iipr.getItemProvisioningNeeds();
                            final NonNullList<ItemStack> payload = NonNullList.create();

                            tbe.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(cap -> {
                                for (ItemStack need : needs) {
                                    int remaining = need.getCount();
                                    for(int i=0; i<cap.getSlots(); i++) {
                                        if(cap.getStackInSlot(i).getItem() == need.getItem()) {
                                            ItemStack removalQuery = cap.extractItem(i, need.getCount(), false);
                                            if (!removalQuery.isEmpty()) {
                                                remaining -= removalQuery.getCount();
                                                if(remaining <= 0)
                                                    break;
                                            }
                                        }
                                    }
                                }
                            });

                            //create shlorp
                            {
                                iipr.setItemProvisioningInProgress();

                                Level level = construct.asEntity().level();
                                BlockEntity startPoint = level.getBlockEntity(takeFromTarget);
                                BlockEntity endPoint = level.getBlockEntity(deviceTargetPos);

                                if (endPoint != null && startPoint != null) {
                                    float speedFactor = SHLORP_SPEEDS[construct.getEquivalentTier()];

                                    Vector3 sP, sO, sT;
                                    Vector3 eP = new Vector3(endPoint.getBlockPos().getX(), endPoint.getBlockPos().getY(), endPoint.getBlockPos().getZ());

                                    sP = new Vector3(startPoint.getBlockPos().getX(), startPoint.getBlockPos().getY(), startPoint.getBlockPos().getZ());

                                    sO = new Vector3(0.5,0.5,0.5);
                                    sT = Vector3.up().scale(r.nextFloat() * 2.0f + 1.5f);

                                    for(int i=0; i<needs.size(); i++) {
                                        payload.add(i, needs.get(i).copy());
                                    }

                                    ItemShlorpEntity shlorp = new ItemShlorpEntity(EntitiesRegistry.ITEM_SHLORP_ENTITY.get(), level);
                                    shlorp.setPos(new Vec3(sP.x, sP.y, sP.z));
                                    shlorp.configure(
                                            sP, sO, sT,
                                            eP, new Vector3(0.5, 0.5, 0.5), Vector3.up().scale(r.nextFloat() * 2.0f + 1.5f),
                                            speedFactor, payload);
                                    level.addFreshEntity(shlorp);
                                }
                            }

                            InteractionHand interactionHand = construct.getHandWithCapability(ConstructCapability.CAST_SPELL).get();
                            if (interactionHand == InteractionHand.MAIN_HAND)
                                construct.forceAnimation(Animations.CHANNEL_LEFT, true);
                            else
                                construct.forceAnimation(Animations.CHANNEL_RIGHT, true);

                            this.pushDiagnosticMessage("I moved a clump of stuff into the device, boss. I like watching them fly!", true);

                            this.phase = ETaskPhase.WAIT_AT_DEVICE;
                            this.waitTimer = 121;
                        }
                    }
                }
                case WAIT_AT_DEVICE -> {
                    this.waitTimer--;
                    if(this.waitTimer <= 0) {
                        construct.clearForcedAnimation();
                        this.setSuccessCode();
                        this.phase = ETaskPhase.WAIT_TO_FAIL;
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

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.PROVIDE_ITEMS);
    }

    @Override
    public ConstructProvideItems duplicate() {
        return new ConstructProvideItems(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructProvideItems copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructProvideItems task) {
            this.takeFromTarget = task.takeFromTarget;
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
        parameters.add(new ConstructTaskPointParameter("provide_items.point"));
        parameters.add(new ConstructTaskPointParameter("provide_items.point2"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("provide_items.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.deviceTargetPos = pointParam.getPosition();
            }
        });

        this.getParameter("provide_items.point2").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.takeFromTarget = pointParam.getPosition();
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.takeFromTarget != null && this.deviceTargetPos != null;
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