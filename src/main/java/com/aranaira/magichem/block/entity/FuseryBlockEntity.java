package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.FuseryBlock;
import com.aranaira.magichem.block.entity.ext.AbstractFixationBlockEntity;
import com.aranaira.magichem.block.entity.routers.FuseryRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.IRequiresRouterCleanupOnDestruction;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.FuseryRouterType;
import com.aranaira.magichem.gui.FuseryMenu;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FuseryBlockEntity extends AbstractFixationBlockEntity implements MenuProvider, IRequiresRouterCleanupOnDestruction {
    public static final int
            SLOT_COUNT = 22,
            SLOT_BOTTLES = 20, SLOT_BOTTLES_OUTPUT = 0, SLOT_RECIPE = 21,
            SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 10,
            SLOT_OUTPUT_START = 11, SLOT_OUTPUT_COUNT  = 9,
            GRIME_BAR_WIDTH = 50, PROGRESS_BAR_WIDTH = 28, FLUID_BAR_HEIGHT = 88,
            DATA_COUNT = 8, DATA_PROGRESS = 0, DATA_GRIME = 1, DATA_TORQUE = 2, DATA_ANIMUS = 3, DATA_EFFICIENCY_MOD = 4, DATA_OPERATION_TIME_MOD = 5, DATA_BATCH_SIZE = 6, DATA_REDUCTION_RATE = 7,
            NO_TORQUE_GRACE_PERIOD = 20, TORQUE_GAIN_ON_COG_ACTIVATION = 36, ANIMUS_GAIN_ON_DUSTING = 12000;
    public static final float
            WHEEL_ACCELERATION_RATE = 0.375f, WHEEL_DECELERATION_RATE = 0.625f, WHEEL_TOP_SPEED = 20.0f,
            COG_ACCELERATION_RATE = 0.5f, COG_DECELERATION_RATE = 0.375f, COG_TOP_SPEED = 10.0f;

    public float
            wheelAngle, wheelSpeed, cogAngle, cogSpeed;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public FuseryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.FUSERY_BE.get(), pos, state);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT) {
                    ItemStack item = super.extractItem(slot, amount, simulate);
                    item.removeTagKey("CustomModelData");
                    return item;
                }

                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                if(slot == SLOT_RECIPE)
                    currentRecipe = FixationSeparationRecipe.getSeparatingRecipe(level, getStackInSlot(SLOT_RECIPE));
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
                    if(currentRecipe != null) {
                        if(((slot - SLOT_INPUT_START) / 2) >= currentRecipe.getComponentMateria().size())
                            return false;
                        ItemStack component = currentRecipe.getComponentMateria().get((slot - SLOT_INPUT_START) / 2);
                        return stack.getItem().equals(component.getItem());
                    } else {
                        return stack.getItem() instanceof AdmixtureItem;
                    }
                }
                if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                    return false;
                if(slot == SLOT_RECIPE)
                    return false;

                return super.isItemValid(slot, stack);
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        return FuseryBlockEntity.this.progress;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(FuseryBlockEntity.this);
                        return grime.getGrime();
                    }
                    case DATA_TORQUE: {
                        return FuseryBlockEntity.this.remainingTorque;
                    }
                    case DATA_ANIMUS: {
                        return FuseryBlockEntity.this.remainingAnimus;
                    }
                    case DATA_EFFICIENCY_MOD: {
                        return FuseryBlockEntity.this.efficiencyMod;
                    }
                    case DATA_OPERATION_TIME_MOD: {
                        return Math.round(FuseryBlockEntity.this.operationTimeMod * 100);
                    }
                    case DATA_BATCH_SIZE: {
                        return FuseryBlockEntity.this.batchSize;
                    }
                    case DATA_REDUCTION_RATE: {
                        return FuseryBlockEntity.this.reductionRate;
                    }
                    default: return -1;
                }
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        FuseryBlockEntity.this.progress = pValue;
                        break;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(FuseryBlockEntity.this);
                        grime.setGrime(pValue);
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
                    case DATA_REDUCTION_RATE: {
                        reductionRate = pValue;
                        break;
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
        this.lazyFluidHandler = LazyOptional.of(() -> this);
    }

    ////////////////////
    // BOILERPLATE CODE
    ////////////////////

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.fusery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FuseryMenu(id, inventory, this, this.data);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> this);
        currentRecipe = FixationSeparationRecipe.getSeparatingRecipe(level, itemHandler.getStackInSlot(SLOT_RECIPE));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("remainingTorque", this.remainingTorque);
        nbt.putInt("remainingAnimus", this.remainingAnimus);
        nbt.putInt("fluidContents", 0);
        nbt.putInt("batchSize", this.batchSize);
        lazyFluidHandler.ifPresent(cap -> {
            nbt.putInt("fluidContents", cap.getFluidInTank(0).getAmount());
        });
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        remainingTorque = nbt.getInt("remainingTorque");
        remainingAnimus = nbt.getInt("remainingAnimus");
        batchSize = nbt.getInt("batchSize");
        int fluidContents = nbt.getInt("fluidContents");
        if(fluidContents > 0)
            containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), fluidContents);
        else
            containedSlurry = FluidStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("remainingTorque", this.remainingTorque);
        nbt.putInt("remainingAnimus", this.remainingAnimus);
        nbt.putInt("batchSize", this.batchSize);
        if(containedSlurry.isEmpty())
            nbt.putInt("fluidContents", 0);
        else
            nbt.putInt("fluidContents", containedSlurry.getAmount());
        return nbt;
    }

    public void packInventoryToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.FUSERY.get());
        IGrimeCapability grimeCap = GrimeProvider.getCapability(FuseryBlockEntity.this);

        CompoundTag nbt = new CompoundTag();
        nbt.putInt("grime", grimeCap.getGrime());
        nbt.put("inventory", itemHandler.serializeNBT());

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        itemHandler.deserializeNBT(pInventoryTag);
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    @Override
    public int getGrimeFromData() {
        return data.get(DATA_GRIME);
    }

    @Override
    public int getMaximumGrime() {
        return getVar(IDs.CONFIG_MAX_GRIME);
    }

    @Override
    public int clean() {
        int grimeDetected = GrimeProvider.getCapability(this).getGrime();
        IGrimeCapability grimeCapability = GrimeProvider.getCapability(this);
        grimeCapability.setGrime(0);
        data.set(DATA_GRIME, 0);
        return grimeDetected / Config.grimePerWaste;
    }

    public static int getScaledGrime(int pGrime, Function<IDs, Integer> pVarFunc) {
        return (GRIME_BAR_WIDTH * pGrime) / pVarFunc.apply(IDs.CONFIG_MAX_GRIME);
    }

    public static int getScaledSlurry(int pSlurry, Function<IDs, Integer> pVarFunc) {
        return (FLUID_BAR_HEIGHT * pSlurry) / pVarFunc.apply(IDs.CONFIG_TANK_CAPACITY);
    }

    @Override
    protected void pushData() {
        this.data.set(DATA_PROGRESS, progress);
        this.data.set(DATA_GRIME, GrimeProvider.getCapability(this).getGrime());
        this.data.set(DATA_TORQUE, remainingTorque);
        this.data.set(DATA_ANIMUS, remainingAnimus);
    }

    ////////////////////
    // OVERRIDES
    ////////////////////

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(FuseryBlockEntity::getVar);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, FuseryBlockEntity pEntity) {
        if(pLevel.isClientSide()) {
            pEntity.handleAnimationDrivers();
        }

        //Particles
        generateCauldronSmokeParticles(pLevel, pPos, pEntity);

        AbstractFixationBlockEntity.tick(pLevel, pPos, pState, pEntity, FuseryBlockEntity::getVar);
    }

    public static int getVar(IDs pID) {
        return switch(pID) {
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case SLOT_BOTTLES_OUTPUT -> SLOT_BOTTLES_OUTPUT;
            case SLOT_INPUT_START -> SLOT_INPUT_START;
            case SLOT_INPUT_COUNT -> SLOT_INPUT_COUNT;
            case SLOT_OUTPUT_START -> SLOT_OUTPUT_START;
            case SLOT_OUTPUT_COUNT -> SLOT_OUTPUT_COUNT;
            case SLOT_RECIPE -> SLOT_RECIPE;

            case DATA_PROGRESS -> DATA_PROGRESS;
            case DATA_GRIME -> DATA_GRIME;
            case DATA_TORQUE -> DATA_TORQUE;
            case DATA_ANIMUS -> DATA_ANIMUS;
            case DATA_EFFICIENCY_MOD -> DATA_EFFICIENCY_MOD;
            case DATA_OPERATION_TIME_MOD -> DATA_OPERATION_TIME_MOD;

            case GUI_PROGRESS_BAR_WIDTH -> PROGRESS_BAR_WIDTH;
            case GUI_GRIME_BAR_WIDTH -> GRIME_BAR_WIDTH;

            case CONFIG_BASE_EFFICIENCY -> Config.fuseryEfficiency;
            case CONFIG_MAX_GRIME -> Config.fuseryMaximumGrime;
            case CONFIG_GRIME_ON_SUCCESS -> Config.fuseryGrimeOnSuccess;
            case CONFIG_GRIME_ON_FAILURE -> Config.fuseryGrimeOnFailure;
            case CONFIG_OPERATION_TIME -> Config.fuseryOperationTime;
            case CONFIG_TORQUE_GAIN_ON_ACTIVATION -> TORQUE_GAIN_ON_COG_ACTIVATION;
            case CONFIG_ANIMUS_GAIN_ON_DUSTING -> ANIMUS_GAIN_ON_DUSTING;
            case CONFIG_NO_TORQUE_GRACE_PERIOD -> NO_TORQUE_GRACE_PERIOD;
            case CONFIG_TANK_CAPACITY -> Config.fuseryTankCapacity;

            default -> -1;
        };
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();

        List<BlockEntity> query = new ArrayList<>();
        for(Triplet<BlockPos, FuseryRouterType, DevicePlugDirection> posAndType : FuseryBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof FuseryRouterBlockEntity frbe) {
                BlockEntity pe = frbe.getPlugEntity();
                if(pe instanceof DirectionalPluginBlockEntity dpbe) pluginDevices.add(dpbe);
            }
        }
    }

    ////////////////////
    // FLUID HANDLING
    ////////////////////

    @Override
    public int getTankCapacity(int tank) {
        return Config.fuseryTankCapacity;
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
            syncAndSave();
        }
    }

    public void dustCog() {
        remainingAnimus += ANIMUS_GAIN_ON_DUSTING;
        syncAndSave();
    }

    public void setRecipeByOutput(ItemStack pRecipeOutput) {
        itemHandler.setStackInSlot(SLOT_RECIPE, pRecipeOutput.copy());
        syncAndSave();
    }

    private static void generateCauldronSmokeParticles(Level pLevel, BlockPos pPos, FuseryBlockEntity pEntity) {
        if(pEntity.remainingTorque + pEntity.remainingAnimus > 0 && pEntity.containedSlurry.getAmount() > 0) {
            int loopingTime = (int)(pLevel.getGameTime() % 8);
            if(loopingTime % 2 == 0) {
                Vector3f mid = switch (pEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                    case NORTH -> new Vector3f(0.25f, 0.125f, 0.25f);
                    case EAST -> new Vector3f(0.75f, 0.125f, 0.25f);
                    case SOUTH -> new Vector3f(0.75f, 0.125f, 0.75f);
                    case WEST -> new Vector3f(0.25f, 0.125f, 0.75f);
                    default -> new Vector3f(0, 0, 0);
                };

                int i = loopingTime / 2;

                float shiftDist = 0.125f;
                Vector2f[] offsets = {
                        new Vector2f(-shiftDist, -shiftDist),
                        new Vector2f(-shiftDist, shiftDist),
                        new Vector2f(shiftDist, shiftDist),
                        new Vector2f(shiftDist, -shiftDist)
                };
                Vector3 pos = new Vector3(
                        pPos.getX() + mid.x + offsets[loopingTime / 2].x,
                        pPos.getY() + mid.y,
                        pPos.getZ() + mid.z + offsets[loopingTime / 2].y);

                pLevel.addParticle(new MAParticleType(ParticleInit.DUST_LERP.get())
                                .setScale(0.0875f).setMaxAge(72)
                                .setMover(new ParticleLerpMover(pos.x, pos.y, pos.z, pos.x, pos.y + 1.375, pos.z))
                                .setColor(40, 140, 240, 48),
                        pos.x, pos.y, pos.z,
                        0, 0, 0);
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-2, 0, -2), getBlockPos().offset(2,1,2));
    }

    @Override
    public void destroyRouters() {
        FuseryBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
}
