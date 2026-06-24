package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.block.CentrifugeBlock;
import com.aranaira.magichem.block.entity.ext.AbstractSeparationBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.gui.CentrifugeMenu;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CentrifugeBlockEntity extends AbstractSeparationBlockEntity implements MenuProvider, IRequiresRouterCleanupOnDestruction, IShlorpReceiver, IMateriaProvisionRequester, IMateriaSortingRequester, IHasDeviceRecipeSlot, IKeepsInventoryOnBreak {

    public static final int
        SLOT_COUNT = 14,
        SLOT_BOTTLES = 13, SLOT_BOTTLES_OUTPUT = 0,
        SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 3,
        SLOT_OUTPUT_START = 4, SLOT_OUTPUT_COUNT  = 9,
        GRIME_BAR_WIDTH = 50, PROGRESS_BAR_WIDTH = 24,
        DATA_COUNT = 7, DATA_PROGRESS = 0, DATA_GRIME = 1, DATA_TORQUE = 2, DATA_ANIMUS = 3, DATA_EFFICIENCY_MOD = 4, DATA_OPERATION_TIME_MOD = 5, DATA_BATCH_SIZE = 6,
        NO_TORQUE_GRACE_PERIOD = 20, TORQUE_GAIN_ON_COG_ACTIVATION = 36, ANIMUS_GAIN_ON_DUSTING = 12000;
    public static final float
        WHEEL_ACCELERATION_RATE = 0.375f, WHEEL_DECELERATION_RATE = 0.625f, WHEEL_TOP_SPEED = 20.0f,
        COG_ACCELERATION_RATE = 0.5f, COG_DECELERATION_RATE = 0.375f, COG_TOP_SPEED = 10.0f;

    public float
            wheelAngle, wheelSpeed, cogAngle, cogSpeed;
    private int
            analogSignalLastTick = 0;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CENTRIFUGE_BE.get(), pos, state);

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
                if(slot == SLOT_BOTTLES)
                    return stack.getItem() == Items.GLASS_BOTTLE || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                if(slot == SLOT_BOTTLES_OUTPUT)
                    return false;
                if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    return stack.getItem() instanceof AdmixtureItem;
                }
                if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                    return false;

                return super.isItemValid(slot, stack);
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        return CentrifugeBlockEntity.this.progress;
                    }
                    case DATA_TORQUE: {
                        return CentrifugeBlockEntity.this.remainingTorque;
                    }
                    case DATA_ANIMUS: {
                        return CentrifugeBlockEntity.this.remainingAnimus;
                    }
                    case DATA_EFFICIENCY_MOD: {
                        return CentrifugeBlockEntity.this.efficiencyMod;
                    }
                    case DATA_OPERATION_TIME_MOD: {
                        return Math.round(CentrifugeBlockEntity.this.operationTimeMod * 100);
                    }
                    case DATA_BATCH_SIZE: {
                        return CentrifugeBlockEntity.this.batchSize;
                    }
                    default: return -1;
                }
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        CentrifugeBlockEntity.this.progress = pValue;
                        break;
                    }
                    case DATA_TORQUE: {
                        remainingTorque = pValue;
                        break;
                    }
                    case DATA_ANIMUS: {
                        remainingAnimus = pValue;
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
    }

    ////////////////////
    // BOILERPLATE CODE
    ////////////////////

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CentrifugeMenu(id, inventory, this, this.data);
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
        nbt.putInt("remainingTorque", this.remainingTorque);
        nbt.putInt("remainingAnimus", this.remainingAnimus);
        nbt.putInt("batchSize", this.batchSize);
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
        remainingTorque = nbt.getInt("remainingTorque");
        remainingAnimus = nbt.getInt("remainingAnimus");
        batchSize = nbt.getInt("batchSize");
        clearRecipeAfterNextProcess = nbt.getBoolean("clearRecipeAfterNextProcess");

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
        nbt.putInt("remainingTorque", this.remainingTorque);
        nbt.putInt("remainingAnimus", this.remainingAnimus);
        nbt.putInt("batchSize", this.batchSize);
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
        ItemStack stack = new ItemStack(BlockRegistry.CENTRIFUGE.get());
        IGrimeCapability grimeCap = GrimeProvider.getCapability(CentrifugeBlockEntity.this);

        CompoundTag nbt = new CompoundTag();
        nbt.putInt("grime", grimeCap.getGrime());
        nbt.put("inventory", itemHandler.serializeNBT());

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {
        if(pNBT.contains("inventory")){
            CompoundTag inventoryTag = pNBT.getCompound("inventory");
            int size = inventoryTag.getInt("Size");
            if (size == SLOT_COUNT) {
                itemHandler.deserializeNBT(inventoryTag);
            } else if (getLevel() != null && getLevel().isClientSide()) {
                final LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    MutableComponent msg = Component.translatable("feedback.warning.inventory_size_mismatch.part1")
                            .append(Component.translatable("block.magichem.centrifuge").withStyle(ChatFormatting.GOLD))
                            .append(Component.translatable("feedback.warning.inventory_size_mismatch.part2"));
                    player.displayClientMessage(msg, false);
                }
            }
            doDeferredRecipeCheck = true;
        }
        if (pNBT.contains("grime")) {
            GrimeProvider.getCapability(this).setGrime(pNBT.getInt("grime"));
        }
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    @Override
    public int getMaximumGrime() {
        return getVar(IDs.CONFIG_MAX_GRIME);
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
        return (GRIME_BAR_WIDTH * grime) / ServerConfig.centrifugeMaximumGrime;
    }

    public int getTorque() {
        return remainingTorque;
    }

    @Override
    protected void pushData() {
        this.data.set(DATA_PROGRESS, progress);
        this.data.set(DATA_TORQUE, remainingTorque);
        this.data.set(DATA_ANIMUS, remainingAnimus);
        //todo: push op time mod
    }

    ////////////////////
    // OVERRIDES
    ////////////////////

    @Override
    public SimpleContainer getContentsOfInputSlots() {
        return getContentsOfInputSlots(CentrifugeBlockEntity::getVar);
    }

    @Override
    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(CentrifugeBlockEntity::getVar);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CentrifugeBlockEntity pEntity) {
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
        } else {
            int analogSignalThisTick = pState.getBlock().getAnalogOutputSignal(pState, pLevel, pPos);
            if(analogSignalThisTick != pEntity.analogSignalLastTick) {
                pEntity.setChanged();
                for (Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> offset : CentrifugeBlock.getRouterOffsets(pState.getValue(MagiChemBlockStateProperties.FACING))) {
                    BlockEntity be = pLevel.getBlockEntity(pPos.offset(offset.getFirst()));
                    if(be != null) be.setChanged();
                }
            }
            pEntity.analogSignalLastTick = analogSignalThisTick;
        }

        AbstractSeparationBlockEntity.tick(pLevel, pPos, pState, pEntity, CentrifugeBlockEntity::getVar, pEntity::getPoweredOperationTime);
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
            case DATA_TORQUE -> DATA_TORQUE;
            case DATA_ANIMUS -> DATA_ANIMUS;
            case DATA_EFFICIENCY_MOD -> DATA_EFFICIENCY_MOD;
            case DATA_OPERATION_TIME_MOD -> DATA_OPERATION_TIME_MOD;

            case MODE_USES_RF -> 0;

            case GUI_PROGRESS_BAR_WIDTH -> PROGRESS_BAR_WIDTH;
            case GUI_GRIME_BAR_WIDTH -> GRIME_BAR_WIDTH;

            case CONFIG_BASE_EFFICIENCY -> ServerConfig.centrifugeEfficiency;
            case CONFIG_MAX_GRIME -> ServerConfig.centrifugeMaximumGrime;
            case CONFIG_GRIME_ON_SUCCESS -> ServerConfig.centrifugeGrimeOnSuccess;
            case CONFIG_GRIME_ON_FAILURE -> ServerConfig.centrifugeGrimeOnFailure;
            case CONFIG_OPERATION_TIME -> ServerConfig.centrifugeOperationTime;
            case CONFIG_TORQUE_GAIN_ON_ACTIVATION -> TORQUE_GAIN_ON_COG_ACTIVATION;
            case CONFIG_ANIMUS_GAIN_ON_DUSTING -> ANIMUS_GAIN_ON_DUSTING;
            case CONFIG_NO_TORQUE_GRACE_PERIOD -> NO_TORQUE_GRACE_PERIOD;

            default -> -1;
        };
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();

        List<BlockEntity> query = new ArrayList<>();
        for(Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> posAndType : CentrifugeBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof CentrifugeRouterBlockEntity crbe) {
                BlockEntity pe = crbe.getPlugEntity();
                if(pe instanceof AbstractDirectionalPluginBlockEntity dpbe) {
                    final ICanTakePlugins targetMachine = dpbe.getTargetMachine();
                    if(targetMachine instanceof CentrifugeRouterBlockEntity router) {
                        CentrifugeBlockEntity master = router.getMaster();
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

    private void handleAnimationDrivers() {
        if(remainingTorque + remainingAnimus > 0) {
            if(wheelSpeed == 0) wheelSpeed += WHEEL_ACCELERATION_RATE * 4;
            wheelSpeed = Math.min(wheelSpeed + WHEEL_ACCELERATION_RATE, WHEEL_TOP_SPEED);
            cogSpeed = Math.min(cogSpeed + COG_ACCELERATION_RATE, COG_TOP_SPEED);
        } else {
            wheelSpeed = Math.max(wheelSpeed - WHEEL_DECELERATION_RATE, 0f);
            cogSpeed = Math.max(cogSpeed - COG_DECELERATION_RATE, 0f);
        }
        wheelAngle = (wheelAngle + wheelSpeed) % 360.0f;
        cogAngle = (cogAngle + cogSpeed) % 360.0f;
    }

    public void activateCog() {
        activateCog(false);
    }

    public void activateCog(boolean isFakePlayer){
        if(remainingAnimus < TORQUE_GAIN_ON_COG_ACTIVATION) {
            int torqueMultiplier = isFakePlayer ? 3 : 1;
            remainingTorque = Math.max(remainingTorque, TORQUE_GAIN_ON_COG_ACTIVATION * torqueMultiplier);
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void dustCog() {
        remainingAnimus += ANIMUS_GAIN_ON_DUSTING;
        setChanged();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-2, 0, -2), getBlockPos().offset(2,1,2));
    }

    @Override
    public void destroyRouters() {
        CentrifugeBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
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
//                                    materiaToVent = materiaToVent | (1 << i);
                                itemHandler.setStackInSlot(SLOT_INPUT_START + i, ItemStack.EMPTY.copy());
                                continue;
                            }
                        }
                    } else {
                        ItemStack query = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
                        if(InventoryHelper.hasCustomModelData(query) && query.getItem() != componentMateria[i/2].getItem()) {
//                                materiaToVent = materiaToVent | (1 << i);
                            itemHandler.setStackInSlot(SLOT_INPUT_START + i, ItemStack.EMPTY.copy());
                            continue;
                        }
                    }
//                        materiaToVent = materiaToVent & ~(1 << i);
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
            final SimpleContainer inputs = getContentsOfInputSlots(CentrifugeBlockEntity::getVar);
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
