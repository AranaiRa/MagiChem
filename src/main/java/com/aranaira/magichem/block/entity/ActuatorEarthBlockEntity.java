package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.gui.ActuatorEarthMenu;
import com.aranaira.magichem.gui.ActuatorEarthScreen;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ActuatorEarthBlockEntity extends AbstractDirectionalPluginBlockEntity implements MenuProvider, IPluginDevice, IEldrinConsumerTile, IShlorpReceiver, IMateriaProvisionRequester {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 15, 30, 50, 75, 105, 140, 180, 225, 275, 335, 410, 500},
            SAND_PER_OPERATION = {0, 45, 50, 55, 60, 70, 80, 90, 105, 120, 135, 155, 175, 200},
            GRIME_REDUCTION = {0, 34, 37, 40, 43, 46, 49, 52, 55, 58, 61, 64, 67, 70};
    public static final int
            MAX_POWER_LEVEL = 13,
            SLOT_COUNT = 5, SLOT_SAND = 0, SLOT_WASTE = 1, SLOT_RAREFIED_WASTE = 2, SLOT_ESSENTIA_INSERTION = 3, SLOT_BOTTLES = 4,
            DATA_COUNT = 6, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1, DATA_FLAGS = 2, DATA_SAND = 3, DATA_GRIME = 4, DATA_RAREFIED_GRIME = 5,
            FLAG_IS_SATISFIED = 1, FLAG_IS_PAUSED = 2,
            STAMPER_ANIMATION_PERIOD = 10;
    private int
            remainingSand = 0,
            currentGrime = 0,
            currentRarefiedGrime = 0,
            flags;
    public float
            stamperDepth = 0,
            stamperDepthNextTick = 0;
    protected ContainerData data;
    private static final MateriaItem ESSENTIA_EARTH = ItemRegistry.getEssentiaMap(false, false).get("earth");

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ActuatorEarthBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.flags = 0;
    }

    public ActuatorEarthBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_EARTH_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return 0;
            }

            @Override
            public void set(int pIndex, int pValue) {

            }

            @Override
            public int getCount() {
                return 0;
            }
        };

        this.flags = 0;

        itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_SAND)
                    return stack.getItem().equals(Items.SAND) || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                else if(slot == SLOT_ESSENTIA_INSERTION) {
                    if(stack.getItem() == ItemRegistry.DEBUG_ORB.get())
                        return true;
                    else if(stack.getItem() instanceof MateriaItem mi) {
                        return mi == ESSENTIA_EARTH;
                    }
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if(slot == SLOT_ESSENTIA_INSERTION) {
                    if(InventoryHelper.isMateriaUnbottled(itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION)))
                        return ItemStack.EMPTY;
                }

                return super.extractItem(slot, amount, simulate);
            }
        };
    }

    public int getEldrinPowerUsage() {
        return ELDRIN_POWER_USAGE[this.powerLevel];
    }

    public static int getEldrinPowerUsage(int pPowerLevel) {
        return ELDRIN_POWER_USAGE[pPowerLevel];
    }

    public int getSandPerOperation() {
        return SAND_PER_OPERATION[this.powerLevel];
    }

    public static int getSandPerOperation(int pPowerLevel) {
        return SAND_PER_OPERATION[pPowerLevel];
    }

    public int getGrimeReductionRate() {
        return GRIME_REDUCTION[this.powerLevel];
    }

    public static int getGrimeReductionRate(int pPowerLevel) {
        return GRIME_REDUCTION[pPowerLevel];
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewEssentiaThisCycle", drewEssentiaThisCycle);
        nbt.putInt("remainingSand", remainingSand);
        nbt.putInt("currentGrime", currentGrime);
        nbt.putInt("currentRarefiedGrime", currentRarefiedGrime);
        nbt.putInt("flags", flags);
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.remainingCycleTime = nbt.getInt("remainingCycleTime");
        this.powerLevel = nbt.getInt("powerLevel");
        this.storedMateria = nbt.getInt("storedMateria");
        this.drewEldrinThisCycle = nbt.getBoolean("drewEldrinThisCycle");
        this.drewEssentiaThisCycle = nbt.getBoolean("drewEssentiaThisCycle");
        this.remainingSand = nbt.getInt("remainingSand");
        this.currentGrime = nbt.getInt("currentGrime");
        this.currentRarefiedGrime = nbt.getInt("currentRarefiedGrime");
        this.flags = nbt.getInt("flags");

        if(nbt.contains("owner"))
            ownerUUID = nbt.getUUID("owner");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewEssentiaThisCycle", drewEssentiaThisCycle);
        nbt.putInt("remainingSand", remainingSand);
        nbt.putInt("currentGrime", currentGrime);
        nbt.putInt("currentRarefiedGrime", currentRarefiedGrime);
        nbt.putInt("flags", flags);
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void processCompletedOperation(int pCyclesCompleted) {
        syncAndSave();
    }

    public static void setPaused(ActuatorEarthBlockEntity entity, boolean pauseState) {
        if(pauseState) {
            entity.flags = entity.flags | FLAG_IS_PAUSED;
        } else {
            entity.flags = entity.flags & ~FLAG_IS_PAUSED;
        }
        entity.syncAndSave();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        boolean changed = AbstractDirectionalPluginBlockEntity.tick(level, pos, blockState, t, ActuatorEarthBlockEntity::getValue);

        if(t instanceof ActuatorEarthBlockEntity entity) {
            if(changed && !level.isClientSide())
                entity.syncAndSave();

            if (level.isClientSide()) {
                entity.handleAnimationDrivers();
            }

            if (!entity.getPaused()) {
                //Fill the internal sand buffer
                if(entity.itemHandler.getStackInSlot(SLOT_SAND).getItem() == ItemRegistry.DEBUG_ORB.get()) {
                    entity.remainingSand = Config.quakeRefinerySandCapacity;
                }
                else if (Config.quakeRefinerySandCapacity - entity.remainingSand >= 1000) {
                    ItemStack sandStack = entity.itemHandler.getStackInSlot(SLOT_SAND);
                    if (!sandStack.isEmpty()) {
                        sandStack.shrink(1);
                        entity.itemHandler.setStackInSlot(SLOT_SAND, sandStack);
                        entity.remainingSand += 1000;
                        entity.syncAndSave();
                    }
                }

                //Dump grime to waste
                if (entity.currentGrime > Config.grimePerWaste) {
                    ItemStack wasteStack = entity.itemHandler.getStackInSlot(SLOT_WASTE);
                    int maxWasteToAdd = entity.currentGrime / Config.grimePerWaste;
                    int actualWasteToAdd;
                    if (wasteStack == ItemStack.EMPTY) {
                        actualWasteToAdd = Math.min(maxWasteToAdd, 64);
                        wasteStack = new ItemStack(ItemRegistry.ALCHEMICAL_WASTE.get(), actualWasteToAdd);
                    } else {
                        actualWasteToAdd = Math.min(64 - wasteStack.getCount(), maxWasteToAdd);
                        wasteStack = new ItemStack(ItemRegistry.ALCHEMICAL_WASTE.get(), wasteStack.getCount() + actualWasteToAdd);
                    }
                    entity.itemHandler.setStackInSlot(SLOT_WASTE, wasteStack);
                    entity.currentGrime -= actualWasteToAdd * Config.grimePerWaste;
                    entity.syncAndSave();
                }

                //Dump rarefied
                if (entity.currentRarefiedGrime > Config.grimePerWaste) {
                    ItemStack wasteStack = entity.itemHandler.getStackInSlot(SLOT_RAREFIED_WASTE);
                    int maxWasteToAdd = entity.currentRarefiedGrime / Config.grimePerWaste;
                    int actualWasteToAdd;
                    if (wasteStack == ItemStack.EMPTY) {
                        actualWasteToAdd = Math.min(maxWasteToAdd, 64);
                        wasteStack = new ItemStack(ItemRegistry.RAREFIED_WASTE.get(), actualWasteToAdd);
                    } else {
                        actualWasteToAdd = Math.min(64 - wasteStack.getCount(), maxWasteToAdd);
                        wasteStack = new ItemStack(ItemRegistry.RAREFIED_WASTE.get(), wasteStack.getCount() + actualWasteToAdd);
                    }
                    entity.itemHandler.setStackInSlot(SLOT_RAREFIED_WASTE, wasteStack);
                    entity.currentRarefiedGrime -= actualWasteToAdd * Config.grimePerWaste;
                    entity.syncAndSave();
                }
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorEarthBlockEntity entity) {
        boolean changed = AbstractDirectionalPluginBlockEntity.delegatedTick(level, pos, state, entity,
                ActuatorEarthBlockEntity::getValue,
                ActuatorEarthBlockEntity::getAffinity,
                ActuatorEarthBlockEntity::getPowerDraw,
                ActuatorEarthBlockEntity::handleAuxiliaryRequirements);

        if(changed) entity.syncAndSave();
    }

    public void handleAnimationDrivers() {
        boolean doDriverUpdate = true;

        if((!getIsSatisfied() || (remainingSand < getSandPerOperation()))) {
            if(stamperDepth == 0) {
                doDriverUpdate = false;
                stamperDepthNextTick = 0;
            }
        }

        if(doDriverUpdate) {
            //Stamper motion
            float loopingTime = (level.getGameTime() % STAMPER_ANIMATION_PERIOD) / (float) STAMPER_ANIMATION_PERIOD;
            stamperDepth = 1 - (float) Math.sin(loopingTime * Math.PI);

            loopingTime = ((level.getGameTime() + 1) % STAMPER_ANIMATION_PERIOD) / (float) STAMPER_ANIMATION_PERIOD;
            stamperDepthNextTick = 1 - (float) Math.sin(loopingTime * Math.PI);

            //Particles
            if(stamperDepth == 1) {
                spawnSandParticles();
            }
        }
    }

    public void spawnSandParticles() {
        Vector3f mid = new Vector3f(0.5f, 0.75f, 0.5f);

        int particleCount = 14;
        for(int i=0; i<particleCount; i++) {
            double theta = (i / (float)particleCount) * Math.PI * 2;
            float radius = 0.125f;
            double pLSpeed = 0.075f;
            Vector3f offset = new Vector3f((float)Math.cos(theta), 0.0f, (float)Math.sin(theta));

            Vector3 pos = new Vector3(getBlockPos().getX() + mid.x + offset.x * radius,
                                      getBlockPos().getY() + mid.y + offset.y * radius,
                                      getBlockPos().getZ() + mid.z + offset.z * radius);
            Vector3 speed = new Vector3(offset.x * pLSpeed,
                                        offset.y + 0.05,
                                        offset.z * pLSpeed);

            level.addParticle(new MAParticleType(ParticleInit.DUST.get())
                            .setPhysics(true).setScale(0.10f).setGravity(0.02f)
                            .setMover(new ParticleVelocityMover(speed.x, speed.y, speed.z, true))
                            .setColor(213, 196, 150, 48),
                    pos.x, pos.y, pos.z,
                    0, 0, 0);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyItemHandler.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ActuatorEarthMenu(i, inventory, this, this.data);
    }

    public int addGrimeToBuffer(int pGrimeToAdd) {
        int insertion = 0;
        int reduction;
        int rarefied = 0;
        int overflow = pGrimeToAdd;

        //Determine how much grime is being added
        if(getIsSatisfied()) {
            float rate = Math.min(1, (float)pGrimeToAdd / 1000f);
            int sandConsumption = (int)Math.ceil((float)getSandPerOperation() * rate);

            if (remainingSand >= sandConsumption) {
                remainingSand -= sandConsumption;

                //Reduce insertion and generate rarefied grime
                reduction = Math.round((float) pGrimeToAdd * ((float) getGrimeReductionRate()) / 100.0f);
                insertion = pGrimeToAdd - reduction;
                rarefied = Math.round(((float) Config.quakeRefineryRarefiedRate / 100.0f) * (float) reduction);
                if (currentGrime + insertion > Config.quakeRefineryGrimeCapacity) {
                    overflow = insertion - (Config.quakeRefineryGrimeCapacity - currentGrime);
                    insertion = Config.quakeRefineryGrimeCapacity - currentGrime;
                } else {
                    overflow = 0;
                }
            }
        }

        //Final application
        currentGrime += insertion;
        currentRarefiedGrime = Math.min(Config.quakeRefineryGrimeCapacity, currentRarefiedGrime + rarefied);
        syncAndSave();
        return overflow;
    }

    public int getSandInTank() {
        return remainingSand;
    }

    public static float getSandPercent(int pSandAmount) {
        return (float)pSandAmount * 100f / Config.quakeRefinerySandCapacity;
    }

    public float getSandPercent() {
        return (float)remainingSand * 100f / Config.quakeRefinerySandCapacity;
    }

    public static int getScaledSand(int pSandAmount) {
        return pSandAmount * ActuatorEarthScreen.FLUID_GAUGE_H / Config.quakeRefinerySandCapacity;
    }

    public int getGrimeInTank() {
        return currentGrime;
    }

    public int getRarefiedGrimeInTank() {
        return currentRarefiedGrime;
    }

    public static float getGrimePercent(int pGrimeAmount) {
        return (float)pGrimeAmount * 100f / Config.quakeRefineryGrimeCapacity;
    }

    public static float getRarefiedGrimePercent(int pRarefiedGrimeAmount) {
        return (float)pRarefiedGrimeAmount * 100f / Config.quakeRefineryGrimeCapacity;
    }

    public static int getScaledGrime(int pGrimeAmount) {
        return pGrimeAmount * ActuatorEarthScreen.FLUID_GAUGE_H / Config.quakeRefineryGrimeCapacity;
    }

    public static int getScaledRarefiedGrime(int pRarefiedGrimeAmount) {
        return pRarefiedGrimeAmount * ActuatorEarthScreen.FLUID_GAUGE_H / Config.quakeRefineryGrimeCapacity;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,2,1));
    }

    public static Affinity getAffinity(Void v) {
        return Affinity.EARTH;
    }

    ////////////////////
    // PROVISIONING AND SHLORPS
    ////////////////////

    private final NonNullList<MateriaItem> activeProvisionRequests = NonNullList.create();

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        if(activeProvisionRequests.size() > 0)
            return false;
        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION);
        if(InventoryHelper.isMateriaUnbottled(insertionStack)) {
            return insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION);
        }
        return insertionStack.isEmpty();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION);

        if(insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION) / 2) {
            result.put(ESSENTIA_EARTH, itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION) - insertionStack.getCount());
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(pMateriaItem == ESSENTIA_EARTH)
            activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        if(pStack.getItem() == ESSENTIA_EARTH) {
            ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION);

            if(insertionStack.isEmpty()) {
                insertionStack = pStack.copy();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("CustomModelData", 1);
                insertionStack.setTag(nbt);
            } else {
                insertionStack.grow(pStack.getCount());
            }
            itemHandler.setStackInSlot(SLOT_ESSENTIA_INSERTION, insertionStack);

            syncAndSave();

            activeProvisionRequests.remove((MateriaItem)pStack.getItem());
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() == ESSENTIA_EARTH) {
            return 0;
        }
        return pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);
        return 0;
    }

    ////////////////////
    // STATIC RETRIEVAL
    ////////////////////

    public static int getValue(IDs id) {
        return switch (id) {
            case SLOT_COUNT -> SLOT_COUNT;
            case SLOT_ESSENTIA_INSERTION -> SLOT_ESSENTIA_INSERTION;
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case MAX_POWER_LEVEL -> MAX_POWER_LEVEL;
        };
    }

    public static int getPowerDraw(AbstractDirectionalPluginBlockEntity entity) {
        if(entity == null)
            return 1;
        return ELDRIN_POWER_USAGE[entity.getPowerLevel()];
    }

    public static boolean handleAuxiliaryRequirements(AbstractDirectionalPluginBlockEntity entity) {
        entity.satisfyAuxiliaryRequirements();
        return true;
    }
}
