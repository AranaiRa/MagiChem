package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.CentrifugeBlock;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.block.GrandCentrifugeBlock;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractSeparationBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandCentrifugeRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.GrandCentrifugeRouterType;
import com.aranaira.magichem.gui.GrandCentrifugeMenu;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.HAS_LABORATORY_UPGRADE;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.IS_EMITTING_LIGHT;
import static com.aranaira.magichem.util.render.ColorUtils.SIX_STEP_PARTICLE_COLORS;

public class GrandCentrifugeBlockEntity extends AbstractSeparationBlockEntity implements MenuProvider, ICanTakePlugins, IPoweredAlchemyDevice, IRequiresRouterCleanupOnDestruction, IShlorpReceiver, IMateriaProvisionRequester, IMateriaSortingRequester, IHasDeviceRecipeSlot, IKeepsInventoryOnBreak, ICanAcceptLaboratoryCharm {
    public static final int
        SLOT_COUNT = 23,
        SLOT_BOTTLES = 0, SLOT_BOTTLES_OUTPUT = 1,
        SLOT_INPUT_START = 2, SLOT_INPUT_COUNT = 6,
        SLOT_OUTPUT_START = 8, SLOT_OUTPUT_COUNT  = 15,
        GUI_PROGRESS_BAR_WIDTH = 24, GUI_GRIME_BAR_WIDTH = 67, GUI_HEAT_GAUGE_HEIGHT = 16,
        DATA_COUNT = 6, DATA_PROGRESS = 0, DATA_GRIME = 1, DATA_POWER_SUFFICIENCY = 2, DATA_EFFICIENCY_MOD = 3, DATA_OPERATION_TIME_MOD = 4, DATA_BATCH_SIZE = 5;
    public static final float
            CIRCLE_FILL_RATE = 0.025f, PARTICLE_PERCENT_RATE = 0.05f,
            WHEEL_TOP_SPEED = 0.375f, WHEEL_ACCELERATION_RATE = .003125f;
    private int powerUsageSetting = 1;
    private boolean
            hasSufficientPower = false, redstonePaused = false;

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();
    private static final int[] POWER_DRAW = { //TODO: Convert this to config
            60, 75, 95, 120, 155, 195, 250, 320, 410, 525,
            670, 860, 1100, 1410, 1805, 2315, 2970, 3805, 4875, 6250,
            8015, 10275, 13175, 16890, 21655, 27760, 35590, 45630, 58500, 75000
    };
    private static final int[] OPERATION_TICKS = { //TODO: Convert this to config
            150, 134, 119, 106, 94, 84, 75, 67, 59, 52,
            46, 41, 36, 32, 28, 25, 22, 19, 16, 14,
            12, 10, 8, 7, 6, 5, 4, 3, 2, 1
    };

    public float
            circlePercent = 0f, particlePercent = 0f, wheelAngle = 0f, wheelSpeed = 0f;
    private int analogSignalLastTick = 0;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public GrandCentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.GRAND_CENTRIFUGE_BE.get(), pos, state);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    if(InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(slot)))
                        return ItemStack.EMPTY;
                }
                if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT) {
                    ItemStack bottleStack = getStackInSlot(SLOT_BOTTLES);
                    boolean isOrb = bottleStack.getItem() == ItemRegistry.DEBUG_ORB.get();
                    int bottleLimit =
                            bottleStack.isEmpty() ? 0 :
                                    bottleStack.getItem() == ItemRegistry.DEBUG_ORB.get() ? Integer.MAX_VALUE : bottleStack.getCount();

                    if(bottleLimit == 0) return ItemStack.EMPTY.copy();

                    ItemStack item = super.extractItem(slot, Math.min(amount, bottleLimit), simulate);
                    item.removeTagKey("CustomModelData");

                    if(!simulate && !isOrb) bottleStack.shrink(Math.min(amount, bottleLimit));

                    return item;
                }

                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if((slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)) {
                    isStalled = false;
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == SLOT_BOTTLES)
                    return stack.getItem() == Items.GLASS_BOTTLE || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                if (slot == SLOT_BOTTLES_OUTPUT)
                    return stack.getItem() == Items.GLASS_BOTTLE;
                if (slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    return stack.getItem() instanceof AdmixtureItem;
                }
                if (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                    return false;

                return super.isItemValid(slot, stack);
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        return GrandCentrifugeBlockEntity.this.progress;
                    }
                    case DATA_POWER_SUFFICIENCY: {
                        return GrandCentrifugeBlockEntity.this.hasSufficientPower ? 1 : 0;
                    }
                    case DATA_EFFICIENCY_MOD: {
                        return GrandCentrifugeBlockEntity.this.efficiencyMod;
                    }
                    case DATA_OPERATION_TIME_MOD: {
                        return Math.round(GrandCentrifugeBlockEntity.this.operationTimeMod * 100);
                    }
                    case DATA_BATCH_SIZE: {
                        return GrandCentrifugeBlockEntity.this.batchSize;
                    }
                    default: return -1;
                }
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        GrandCentrifugeBlockEntity.this.progress = pValue;
                        break;
                    }
                    case DATA_POWER_SUFFICIENCY: {
                        GrandCentrifugeBlockEntity.this.hasSufficientPower = pValue == 1;
                        break;
                    }
                    case DATA_EFFICIENCY_MOD: {
                        efficiencyMod = pValue;
                        break;
                    }
                    case DATA_OPERATION_TIME_MOD: {
                        operationTimeMod = pValue / 100f;
                        break;
                    }
                    case DATA_BATCH_SIZE: {
                        batchSize = pValue;
                        break;
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };

        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    //////////
    // BOILERPLATE CODE
    //////////

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.grand_centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GrandCentrifugeMenu(id, inventory, this, this.data);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        linkPlugins();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putBoolean("hasSufficientPower", this.hasSufficientPower);
        nbt.putInt("batchSize", this.batchSize);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putBoolean("redstonePaused", this.redstonePaused);
        nbt.putBoolean("clearRecipeAfterNextProcess", this.clearRecipeAfterNextProcess);
        nbt.putLong("grime", GrimeProvider.getCapability(this).getGrime());

        if(currentRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.ITEMS.getKey(currentRecipe.getResultAdmixture().getItem());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
        }

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        unpackDataFromNBT(nbt);
        progress = nbt.getInt("craftingProgress");
        hasSufficientPower = nbt.getBoolean("hasSufficientPower");
        batchSize = nbt.getInt("batchSize");
        powerUsageSetting = nbt.getInt("powerUsageSetting");
        redstonePaused = nbt.getBoolean("redstonePaused");
        clearRecipeAfterNextProcess = nbt.getBoolean("clearRecipeAfterNextProcess");
        GrimeProvider.getCapability(this).setGrime((int)nbt.getLong("grime"));

        if(nbt.contains("recipe"))
            deferredRecipeQuery = new ResourceLocation(nbt.getString("recipe"));
        else
            deferredRecipeQuery = null;
        doDeferredRecipeCheck = true;

        updateActuatorValues(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putBoolean("hasSufficientPower", this.hasSufficientPower);
        nbt.putInt("batchSize", this.batchSize);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putBoolean("redstonePaused", this.redstonePaused);
        nbt.putBoolean("clearRecipeAfterNextProcess", this.clearRecipeAfterNextProcess);
        nbt.putLong("grime", GrimeProvider.getCapability(this).getGrime());

        if(currentRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.ITEMS.getKey(currentRecipe.getResultAdmixture().getItem());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
        }

        return nbt;
    }

    @Override
    public void packDataToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.GRAND_CENTRIFUGE.get());
        IGrimeCapability grimeCap = GrimeProvider.getCapability(GrandCentrifugeBlockEntity.this);

        CompoundTag nbt = new CompoundTag();
        nbt.putInt("grime", grimeCap.getGrime());
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.put("inventory", itemHandler.serializeNBT());

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {
        if(pNBT.contains("inventory")) {
            CompoundTag inventoryTag = pNBT.getCompound("inventory");
            int size = inventoryTag.getInt("Size");
            if (size == SLOT_COUNT) {
                itemHandler.deserializeNBT(inventoryTag);
            } else if (getLevel() != null && getLevel().isClientSide()) {
                final LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    MutableComponent msg = Component.translatable("feedback.warning.inventory_size_mismatch.part1")
                            .append(Component.translatable("block.magichem.grand_centrifuge").withStyle(ChatFormatting.GOLD))
                            .append(Component.translatable("feedback.warning.inventory_size_mismatch.part2"));
                    player.displayClientMessage(msg, false);
                }
            }
            doDeferredRecipeCheck = true;
        }
        if (pNBT.contains("grime")) {
            GrimeProvider.getCapability(this).setGrime(pNBT.getInt("grime"));
        }
        if (pNBT.contains("powerUsageSetting")) {
            setPowerUsageSetting(pNBT.getInt("powerUsageSetting"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    @Override
    public int getMaximumGrime() {
        return ServerConfig.grandCentrifugeMaximumGrime;
    }

    public boolean getPowerSufficiency() {
        return hasSufficientPower;
    }

    public void checkPaused() {
        boolean shouldPause = false;
        BlockPos myPos = getBlockPos();

        for (Triplet<BlockPos, GrandCentrifugeRouterType, DevicePlugDirection> query : GrandCentrifugeBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            for(Direction dir : Direction.values()) {
                BlockPos posQuery = myPos.offset(query.getFirst()).offset(dir.getNormal());
                BlockState stateQuery = getLevel().getBlockState(posQuery);
                if(stateQuery.getBlock() != BlockRegistry.GRAND_CENTRIFUGE.get() && stateQuery.getBlock() != BlockRegistry.GRAND_CENTRIFUGE_ROUTER.get()) {
                    int signal = getLevel().getBlockState(posQuery).getSignal(getLevel(), posQuery, dir);
                    if (signal > 0) {
                        shouldPause = true;
                        break;
                    }
                }
            }
            if(shouldPause) break;
        }

        setPaused(shouldPause);
    }

    public void setPaused(boolean pNewPauseState) {
        redstonePaused = pNewPauseState;
        for (AbstractDirectionalPluginBlockEntity pluginDevice : pluginDevices) {
            pluginDevice.setDevicePaused(pNewPauseState);
        }
        syncAndSave();
    }

    @Override
    public int clean() {
        int grimeDetected = GrimeProvider.getCapability(this).getGrime();
        IGrimeCapability grimeCapability = GrimeProvider.getCapability(this);
        grimeCapability.setGrime(0);
        syncAndSave();
        return grimeDetected / ServerConfig.grimePerWaste;
    }

    public static int getScaledGrime(int grime) {
        return Math.min(ServerConfig.grandCentrifugeMaximumGrime, (GUI_GRIME_BAR_WIDTH * grime) / ServerConfig.grandCentrifugeMaximumGrime);
    }

    @Override
    protected void pushData() {
        this.data.set(DATA_PROGRESS, progress);
        this.data.set(DATA_POWER_SUFFICIENCY, hasSufficientPower ? 1 : 0);
        //TODO: push op time mod
    }

    ////////////////////
    // ACTUATOR HANDLERS
    ////////////////////

    public DevicePlugDirection getPlugDirection() {
        return DevicePlugDirection.NONE;
    }

    public BlockEntity getPlugEntity() {
        BlockPos target = getBlockPos();

        if(getPlugDirection() == DevicePlugDirection.NORTH) target = target.north();
        else if(getPlugDirection() == DevicePlugDirection.EAST) target = target.east();
        else if(getPlugDirection() == DevicePlugDirection.SOUTH) target = target.south();
        else if(getPlugDirection() == DevicePlugDirection.WEST) target = target.west();

        return getLevel().getBlockEntity(target);
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();

        //Start by grabbing the actuator plugged into the main block
        if(getPlugEntity() instanceof AbstractDirectionalPluginBlockEntity dpbe)
            pluginDevices.add(dpbe);

        List<BlockEntity> query = new ArrayList<>();
        for(Triplet<BlockPos, GrandCentrifugeRouterType, DevicePlugDirection> posAndType : GrandCentrifugeBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof GrandCentrifugeRouterBlockEntity gcrbe) {
                BlockEntity pe = gcrbe.getPlugEntity();
                if(pe instanceof AbstractDirectionalPluginBlockEntity dpbe) {
                    final ICanTakePlugins targetMachine = dpbe.getTargetMachine();
                    if(targetMachine instanceof GrandCentrifugeRouterBlockEntity router) {
                        GrandCentrifugeBlockEntity master = router.getMaster();
                        if(master == this) pluginDevices.add(dpbe);
                    } else if (targetMachine == this) {
                        pluginDevices.add(dpbe);
                    }
                }
            }
        }
    }

    @Override
    public List<AbstractDirectionalPluginBlockEntity> getPlugins() {
        return pluginDevices;
    }

    ////////////////////
    // INTERACTION AND VFX
    ////////////////////

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3,3,3));
    }

    public void handleAnimationDrivers() {
        if(particlePercent == 1) {
            circlePercent = Math.min(1, circlePercent + CIRCLE_FILL_RATE);
        } else if(particlePercent == 0) {
            circlePercent = Math.max(0, circlePercent - CIRCLE_FILL_RATE);
        }

        if(hasSufficientPower && !redstonePaused) {
            particlePercent = Math.min(1, particlePercent + PARTICLE_PERCENT_RATE);
            if(circlePercent >= 1)
                wheelSpeed = Math.min(wheelSpeed + WHEEL_ACCELERATION_RATE, WHEEL_TOP_SPEED);
        } else {
            particlePercent = Math.max(0, particlePercent - PARTICLE_PERCENT_RATE);
            wheelSpeed = Math.max(wheelSpeed - WHEEL_ACCELERATION_RATE * 0.5f, 0f);
        }

        wheelAngle = (wheelAngle + wheelSpeed) % 360f;
    }

    public void setRecipeByOutput(ItemStack pRecipeOutput) {
        FixationSeparationRecipe fsr = FixationSeparationRecipe.getSeparatingRecipe(level, pRecipeOutput);

        if(fsr != null) {
            this.currentRecipe = fsr;
            this.syncAndSave();
        }

        if(!level.isClientSide()) {
            if (currentRecipe != null) {
                ItemStack[] componentMateria = new ItemStack[5];
                currentRecipe.getComponentMateria().toArray(componentMateria);

                for(int i=0; i<SLOT_INPUT_COUNT; i++) {
                    if(componentMateria[i/2] != null) {
                        if (componentMateria[i / 2].getItem() instanceof MateriaItem mi) {
                            ItemStack query = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
                            if(InventoryHelper.hasCustomModelData(query) && query.getItem() != componentMateria[i/2].getItem()) {
                                itemHandler.setStackInSlot(SLOT_INPUT_START + i, ItemStack.EMPTY.copy());
                            }
                        }
                    } else {
                        ItemStack query = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
                        if(InventoryHelper.hasCustomModelData(query) && query.getItem() != componentMateria[i/2].getItem()) {
                            itemHandler.setStackInSlot(SLOT_INPUT_START + i, ItemStack.EMPTY.copy());
                        }
                    }
                }
            }
        }
        syncAndSave();
    }

    @Nullable
    public FixationSeparationRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public byte setRecipe(ItemStack pStack, Player player) {
        if(pStack.getItem() instanceof AdmixtureItem ai) {
            currentRecipe = FixationSeparationRecipe.getSeparatingRecipe(player.level(), ai);
            syncAndSave();
            return ERROR_CODE_SUCCESS;
        }
        else if(pStack.isEmpty()) {
            currentRecipe = null;
            syncAndSave();
            return ERROR_CODE_SUCCESS;
        }
        return ERROR_CODE_MUST_BE_ADMIXTURE;
    }

    @Override
    public ItemStack getRecipeItem() {
        return currentRecipe == null ? ItemStack.EMPTY.copy() : currentRecipe.getResultItem().copy();
    }

    @Override
    public ItemStack getRecipeItem(boolean pMakeCopy) {
        return currentRecipe == null ? ItemStack.EMPTY.copy() : pMakeCopy ? currentRecipe.getResultItem().copy() : currentRecipe.getResultItem();
    }

    ////////////////////
    // POWER
    ////////////////////

    public int getPowerUsageSetting() {
        return powerUsageSetting;
    }

    public int getPowerDraw() {
        int baseDraw = POWER_DRAW[MathUtils.clamp(powerUsageSetting, 1, 30)-1];
        float fireActuatorModifier = 1 - (operationTimeMod / 100.0f);

        int out = Math.round((float)baseDraw * fireActuatorModifier);

        return Math.round((float)baseDraw * fireActuatorModifier);
    }

    public int getOperationTicks() {
        return OPERATION_TICKS[MathUtils.clamp(powerUsageSetting, 1, 30)-1];
    }

    public int setPowerUsageSetting(int pPowerUsageSetting) {
        this.powerUsageSetting = pPowerUsageSetting;
        this.resetProgress();
        if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * ServerConfig.circlePowerBuffer)
            ENERGY_STORAGE.setEnergy(getPowerDraw() * ServerConfig.circlePowerBuffer);
        return this.powerUsageSetting;
    }

    public int incrementPowerUsageSetting() {
        if(powerUsageSetting + 1 < 31) {
            this.powerUsageSetting++;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * ServerConfig.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * ServerConfig.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    public int decrementPowerUsageSetting() {
        if(powerUsageSetting - 1 > 0) {
            this.powerUsageSetting--;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * ServerConfig.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * ServerConfig.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {

            int powerToLimit = Math.max(0, (getPowerDraw() * ServerConfig.circlePowerBuffer) - getEnergyStored());
            int actualReceive = Math.min(maxReceive, powerToLimit);

            return super.receiveEnergy(actualReceive, simulate);
        }
    };

    ////////////////////
    // OVERRIDES
    ////////////////////

    public SimpleContainer getContentsOfInputSlots() {
        return getContentsOfInputSlots(GrandCentrifugeBlockEntity::getVar);
    }

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(GrandCentrifugeBlockEntity::getVar);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, GrandCentrifugeBlockEntity pEntity) {
        if(pEntity.doDeferredRecipeCheck) {
            boolean changed = false;
            Item itemQuery = ForgeRegistries.ITEMS.getValue(pEntity.deferredRecipeQuery);
            FixationSeparationRecipe recipeQuery = FixationSeparationRecipe.getSeparatingRecipe(pLevel, itemQuery);

            if(recipeQuery != null) {
                changed = pEntity.currentRecipe != recipeQuery;
                pEntity.currentRecipe = recipeQuery;
            } else {
                changed = pEntity.currentRecipe != null;
                pEntity.currentRecipe = null;
            }
            pEntity.doDeferredRecipeCheck = false;
            if(changed)
                pEntity.syncAndSave();
        }

        if(pLevel.isClientSide()) {
            pEntity.handleAnimationDrivers();
        }

        if(!pEntity.getLevel().isClientSide() && !pEntity.redstonePaused) {
            int powerDraw = pEntity.getPowerDraw();
            boolean sufficientThisTick = pEntity.ENERGY_STORAGE.getEnergyStored() >= powerDraw;

            if(sufficientThisTick != pEntity.hasSufficientPower) {
                pEntity.hasSufficientPower = sufficientThisTick;
                pEntity.syncAndSave();
            }
            if(sufficientThisTick) {
                pEntity.remainingTorque = 36;
            } else {
                pEntity.progress = Math.max(0, pEntity.progress - 2);
            }
            pEntity.ENERGY_STORAGE.extractEnergy(powerDraw, false);

            Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockPos daisPos = pPos;

            if(facing == Direction.NORTH) daisPos = daisPos.south();
            if(facing == Direction.SOUTH) daisPos = daisPos.north();
            if(facing == Direction.EAST) daisPos = daisPos.west();
            if(facing == Direction.WEST) daisPos = daisPos.east();

            BlockState daisState = pLevel.getBlockState(daisPos);
            boolean isDaisEmittingLight = daisState.getValue(IS_EMITTING_LIGHT);

            if((isDaisEmittingLight && pEntity.particlePercent == 0) || (!isDaisEmittingLight && pEntity.particlePercent == 1)) {
                BlockState newDaisState = pLevel.getBlockState(daisPos).setValue(IS_EMITTING_LIGHT, sufficientThisTick);

                pLevel.setBlock(daisPos, newDaisState, 3);
                pLevel.sendBlockUpdated(daisPos, daisState, newDaisState, 3);
            }
        } else if(pEntity.redstonePaused) {
            pEntity.remainingTorque = 0;
        }

        //particle stuff
        if(pEntity.getLevel().isClientSide() && pEntity.particlePercent > 0) {
            Vector3 center = Vector3.zero();
            Direction facing = pEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (facing == Direction.NORTH)
                center = new Vector3(pPos.getX() + 0.5, pPos.getY() + 1.375, pPos.getZ() + 1.5);
            else if (facing == Direction.EAST)
                center = new Vector3(pPos.getX() - 0.5, pPos.getY() + 1.375, pPos.getZ() + 0.5);
            else if (facing == Direction.SOUTH)
                center = new Vector3(pPos.getX() + 0.5, pPos.getY() + 1.375, pPos.getZ() - 0.5);
            else if (facing == Direction.WEST)
                center = new Vector3(pPos.getX() + 1.5, pPos.getY() + 1.375, pPos.getZ() + 0.5);

            int colorIndex = r.nextInt(6);
            if (pEntity.getLevel().getGameTime() % 8 == 0) {
                pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setColor(SIX_STEP_PARTICLE_COLORS[colorIndex][0], SIX_STEP_PARTICLE_COLORS[colorIndex][1], SIX_STEP_PARTICLE_COLORS[colorIndex][2])
                                .setScale(0.4f * pEntity.particlePercent).setMaxAge(80),
                        center.x, center.y, center.z,
                        0, 0, 0);
            }
            pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                            .setColor(255, 255, 255).setScale(0.2f * pEntity.particlePercent),
                    center.x, center.y, center.z,
                    0, 0, 0);

            if(pEntity.particlePercent == 1) {
                for (int i = 0; i < 2; i++) {
                    Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                    pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                    .setColor(SIX_STEP_PARTICLE_COLORS[colorIndex][0], SIX_STEP_PARTICLE_COLORS[colorIndex][1], SIX_STEP_PARTICLE_COLORS[colorIndex][2], 128)
                                    .setScale(0.09f).setMaxAge(16)
                                    .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                            center.x + offset.x, center.y + offset.y, center.z + offset.z,
                            0, 0, 0);

                    pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                    .setScale(0.015f).setMaxAge(16)
                                    .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                            center.x + offset.x, center.y + offset.y, center.z + offset.z,
                            0, 0, 0);
                }
            }
        }

        if(!pLevel.isClientSide()) {
            int analogSignalThisTick = pState.getBlock().getAnalogOutputSignal(pState, pLevel, pPos);
            if(analogSignalThisTick != pEntity.analogSignalLastTick) {
                pEntity.setChanged();
                for (Triplet<BlockPos, GrandCentrifugeRouterType, DevicePlugDirection> offset : GrandCentrifugeBlock.getRouterOffsets(pState.getValue(MagiChemBlockStateProperties.FACING))) {
                    BlockEntity be = pLevel.getBlockEntity(pPos.offset(offset.getFirst()));
                    if(be != null) be.setChanged();
                }
            }
            pEntity.analogSignalLastTick = analogSignalThisTick;
        }

        if(!pEntity.redstonePaused)
            AbstractSeparationBlockEntity.tick(pLevel, pPos, pState, pEntity, GrandCentrifugeBlockEntity::getVar, pEntity::getPoweredOperationTime);
    }

    public Integer getPoweredOperationTime(Void unused) {
        return OPERATION_TICKS[this.powerUsageSetting-1];
    }

    public void applyLaboratoryCharm() {
        BlockPos rootPos = getBlockPos();
        BlockState rootState = getBlockState();
        BlockState newRootState = getBlockState().setValue(HAS_LABORATORY_UPGRADE, true);

        getLevel().setBlock(rootPos, newRootState, 3);
        getLevel().sendBlockUpdated(rootPos, rootState, newRootState, 2);

        for(int z=-1; z<=1; z++) {
            for(int y=0; y<=2; y++) {
                for (int x=-1; x <=1; x++) {
                    BlockPos routerPos = rootPos.offset(x, y, z);
                    BlockState routerState = getLevel().getBlockState(routerPos);

                    if(routerState.getBlock() == BlockRegistry.GRAND_CENTRIFUGE_ROUTER.get()) {
                        BlockState newRouterState = routerState.setValue(HAS_LABORATORY_UPGRADE, true);

                        getLevel().setBlock(routerPos, newRouterState, 3);
                        getLevel().sendBlockUpdated(routerPos, routerState, newRouterState, 2);
                    }
                }
            }
        }
    }

    @Override
    public boolean needsSorting() {
        boolean materiaInOutput = false;
        for(int i=SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            if(!itemHandler.getStackInSlot(i).isEmpty()) {
                materiaInOutput = true;
                break;
            }
        }
        boolean materiaInInput = false;
        if(!materiaInOutput){
            for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
                if (!itemHandler.getStackInSlot(i).isEmpty()) {
                    materiaInInput = true;
                    break;
                }
            }
        }
        return materiaInOutput || materiaInInput;
    }

    public static int getVar(IDs pID) {
        return switch(pID) {
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case SLOT_BOTTLES_OUTPUT -> SLOT_BOTTLES_OUTPUT;
            case SLOT_INPUT_START -> SLOT_INPUT_START;
            case SLOT_INPUT_COUNT -> SLOT_INPUT_COUNT;
            case SLOT_OUTPUT_START -> SLOT_OUTPUT_START;
            case SLOT_OUTPUT_COUNT -> SLOT_OUTPUT_COUNT;

            case DATA_PROGRESS -> DATA_PROGRESS;
            case DATA_GRIME -> DATA_GRIME;
            case DATA_EFFICIENCY_MOD -> DATA_EFFICIENCY_MOD;
            case DATA_OPERATION_TIME_MOD -> DATA_OPERATION_TIME_MOD;

            case MODE_USES_RF -> 1;

            case GUI_PROGRESS_BAR_WIDTH -> GUI_PROGRESS_BAR_WIDTH;
            case GUI_GRIME_BAR_WIDTH -> GUI_GRIME_BAR_WIDTH;

            case CONFIG_BASE_EFFICIENCY -> ServerConfig.grandCentrifugeEfficiency;
            case CONFIG_MAX_GRIME -> ServerConfig.grandCentrifugeMaximumGrime;
            case CONFIG_GRIME_ON_SUCCESS -> ServerConfig.grandCentrifugeGrimeOnSuccess;
            case CONFIG_GRIME_ON_FAILURE -> ServerConfig.grandCentrifugeGrimeOnFailure;

            default -> -1;
        };
    }

    @Override
    public void destroyRouters() {
        if(getBlockState().getValue(HAS_LABORATORY_UPGRADE)) {
            ItemStack charmStack = new ItemStack(ItemRegistry.LABORATORY_CHARM.get());
            ItemEntity ie = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), charmStack);
            getLevel().addFreshEntity(ie);
        }

        GrandCentrifugeBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    ////////////////////
    // PROVISIONING AND SHLORPS
    ////////////////////

    private final NonNullList<MateriaItem> activeProvisionRequests = NonNullList.create();

    @Override
    public boolean allowIncreasedDeliverySize() {
        return true;
    }

    @Override
    public boolean needsProvisioning() {
        if(currentRecipe == null)
            return false;

        //make sure there's space to PUT the provision
        int openSlots = 0;
        for(int i=SLOT_INPUT_START; i<SLOT_INPUT_START+SLOT_INPUT_COUNT; i++) {
            if(itemHandler.getStackInSlot(i).isEmpty()) {
                openSlots++;
            }
        }

        //make sure there aren't enough stacks on the way
        return openSlots - activeProvisionRequests.size() > 0;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        if(currentRecipe != null) {
            boolean requestInTransit = activeProvisionRequests.contains((MateriaItem) currentRecipe.getResultAdmixture().getItem());

            boolean itemInInputs = false;
            final SimpleContainer inputs = getContentsOfInputSlots(GrandCentrifugeBlockEntity::getVar);
            for(int i=0; i<inputs.getContainerSize(); i++) {
                if(inputs.getItem(i).getItem() == currentRecipe.getResultAdmixture().getItem()) {
                    itemInInputs = true;
                    break;
                }
            }

            if (!requestInTransit && !itemInInputs) {
                result.put((MateriaItem) currentRecipe.getResultAdmixture().getItem(), batchSize);
            }
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("CustomModelData", 1);
        pStack.setTag(nbt);

        SimpleContainer inputSlots = new SimpleContainer(SLOT_INPUT_COUNT);
        for(int i=SLOT_INPUT_START; i<SLOT_INPUT_START+SLOT_INPUT_COUNT; i++) {
            inputSlots.setItem(i-SLOT_INPUT_START, itemHandler.getStackInSlot(i));
        }
        for(int i=SLOT_INPUT_START; i<SLOT_INPUT_START+SLOT_INPUT_COUNT; i++) {
            if (itemHandler.getStackInSlot(i).isEmpty()) {
                inputSlots.setItem(i-SLOT_INPUT_START, pStack);
                break;
            } else if(itemHandler.getStackInSlot(i).getItem() == pStack.getItem()) {
                if(InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(i))) {
                    inputSlots.getItem(i-SLOT_INPUT_START).grow(pStack.getCount());
                    break;
                }
            }
        }

        for(int i=0; i<SLOT_INPUT_COUNT; i++) {
            itemHandler.setStackInSlot(SLOT_INPUT_START+i, inputSlots.getItem(i));
        }

        cancelProvisioningInProgress((MateriaItem)pStack.getItem());

        syncAndSave();
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return needsProvisioning() ? 0 : pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);

        return 0;
    }

    @Override
    public ItemStack tryExtractUnbottled(ItemStack pBottlesInHand) {
        int limit = pBottlesInHand.getCount();
        ItemStack extractQuery = null;

        for(int i=SLOT_INPUT_START;i<SLOT_INPUT_START+SLOT_INPUT_COUNT;i++) {
            if(!itemHandler.getStackInSlot(i).isEmpty() && InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(i))) {
                extractQuery = itemHandler.getStackInSlot(i);
                break;
            }
        }

        if(extractQuery != null) {
            int extracted = Math.min(limit, extractQuery.getCount());
            pBottlesInHand.shrink(extracted);
            ItemStack output = new ItemStack(extractQuery.getItem(), extracted);
            extractQuery.shrink(extracted);
            return output;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean isClogged() {
        if (currentRecipe == null) {
            for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
                if (!itemHandler.getStackInSlot(i).isEmpty() && InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}