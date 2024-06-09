package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaProcessorBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.AlchemicalNexusAnimSpec;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.gui.AlchemicalNexusMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.AlchemicalInfusionRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.items.ItemInit;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AlchemicalNexusBlockEntity extends AbstractMateriaProcessorBlockEntity implements MenuProvider, ICanTakePlugins, IFluidHandler {

    protected ItemStackHandler itemHandler;
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.of(() -> this);

    protected FluidStack containedSlurry = FluidStack.EMPTY;
    protected AlchemicalInfusionRecipe currentRecipe;
    protected ContainerData data;
    protected AlchemicalNexusAnimSpec cachedSpec;
    protected int
        progress = 0, pluginLinkageCountdown = 3, animStage = 0, craftingStage = 0, powerLevel = 1, shlorpIndex = 0;
    protected boolean isStalled = false;

    public static final int
            FLUID_BAR_HEIGHT = 88,
            SLOT_COUNT = 17,
            SLOT_MARKS = 0, SLOT_PROCESSING = 1, SLOT_RECIPE = 2,
            SLOT_INPUT_START = 3, SLOT_INPUT_COUNT = 5, SLOT_OUTPUT_START = 8, SLOT_OUTPUT_COUNT = 9,
            DATA_COUNT = 4,
            DATA_PROGRESS = 0, DATA_ANIM_STAGE = 1, DATA_CRAFTING_STAGE = 2, DATA_POWER_LEVEL = 3,
            ANIM_STAGE_IDLE = 0, ANIM_STAGE_RAMP_SPEEDUP = 1, ANIM_STAGE_RAMP_CANCEL = 2, ANIM_STAGE_RAMP_BEAM = 3, ANIM_STAGE_RAMP_CIRCLE = 4,
            ANIM_STAGE_SHLORPS = 5, ANIM_STAGE_CRAFTING = 6, ANIM_STAGE_BETWEEN_CIRCLE_BUILD = 6, ANIM_STAGE_DENOUEMENT = 7;

    public static final float
            CRYSTAL_SPEED_MIN = 0.75f, CRYSTAL_SPEED_MAX = 20.0f,
            CRYSTAL_BOB_HEIGHT_MAX = 0.125f, CRYSTAL_BOB_PERIOD = 90f,
            ITEM_SPEED_MIN = 0.0375f, ITEM_SPEED_MAX = 1.5f, ITEM_SCALE_START = 6f, ITEM_SCALE_END = 0f;
    public float
            crystalAngle = 0f, crystalRotSpeed = CRYSTAL_SPEED_MIN,
            itemAngle = 0f, itemRotSpeed = ITEM_SPEED_MIN, itemScale = 7f;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public AlchemicalNexusBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ALCHEMICAL_NEXUS_BE.get(), pPos, pBlockState);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                if(slot == SLOT_RECIPE)
                    currentRecipe = AlchemicalInfusionRecipe.getInfusionRecipe(level, getStackInSlot(SLOT_RECIPE));
                setChanged();
                if((slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)) {
                    isStalled = false;
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_MARKS)
                    return stack.getItem() == ItemInit.RUNE_MARKING.get() || stack.getItem() == ItemInit.BOOK_MARKS.get();
                else if(slot == SLOT_RECIPE || slot == SLOT_PROCESSING || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT))
                    return false;

                return super.isItemValid(slot, stack);
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch(pIndex) {
                    case DATA_PROGRESS -> progress;
                    case DATA_ANIM_STAGE -> animStage;
                    case DATA_CRAFTING_STAGE -> craftingStage;
                    case DATA_POWER_LEVEL -> powerLevel;
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_PROGRESS -> AlchemicalNexusBlockEntity.this.progress = pValue;
                    case DATA_ANIM_STAGE -> AlchemicalNexusBlockEntity.this.animStage = pValue;
                    case DATA_CRAFTING_STAGE -> AlchemicalNexusBlockEntity.this.craftingStage = pValue;
                    case DATA_POWER_LEVEL -> AlchemicalNexusBlockEntity.this.powerLevel = pValue;
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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        } else if(cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> this);
        currentRecipe = AlchemicalInfusionRecipe.getInfusionRecipe(level, itemHandler.getStackInSlot(SLOT_RECIPE));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("animationStage", this.animStage);
        nbt.putInt("fluidContents", 0);
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
        animStage = nbt.getInt("animationStage");
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
        nbt.putInt("animationStage", this.animStage);
        if(containedSlurry.isEmpty())
            nbt.putInt("fluidContents", 0);
        else
            nbt.putInt("fluidContents", containedSlurry.getAmount());
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.alchemical_nexus");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AlchemicalNexusMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    ////////////////////
    // CRAFTING HANDLERS
    ////////////////////

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, E e) {
        if(e instanceof AlchemicalNexusBlockEntity anbe) {
            if(pLevel.isClientSide()) {
                anbe.handleAnimationDrivers();
                anbe.spawnCandleParticles();
            }

            //Temporary recipe setter
            if(anbe.currentRecipe == null) {
                anbe.currentRecipe = AlchemicalInfusionRecipe.getInfusionRecipe(pLevel, AlchemicalInfusionRecipe.getAllOutputs().get(0));
                anbe.itemHandler.setStackInSlot(SLOT_RECIPE, anbe.currentRecipe.getAlchemyObject());
                anbe.syncAndSave();
            }

            if(anbe.animStage == ANIM_STAGE_IDLE) {
                //Check if all the items are present
                if(anbe.hasAllRecipeItemsForCurrentStage()) {
                    anbe.animStage = ANIM_STAGE_RAMP_SPEEDUP;
                    anbe.cacheAnimSpec();
                    anbe.syncAndSave();
                }
            }

            else if(anbe.animStage == ANIM_STAGE_RAMP_SPEEDUP) {
                anbe.incrementProgress();

                if(!anbe.hasAllRecipeItemsForCurrentStage()) {
                    anbe.animStage = ANIM_STAGE_RAMP_CANCEL;
                }

                if(anbe.progress > anbe.cachedSpec.ticksInRampSpeedup) {
                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_RAMP_BEAM;
                    anbe.syncAndSave();
                }
            }

            else if(anbe.animStage == ANIM_STAGE_RAMP_CANCEL) {
                anbe.incrementProgress();

                if(anbe.progress > anbe.cachedSpec.ticksInRampCancel) {
                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_IDLE;
                    anbe.syncAndSave();
                }
            }

            else if(anbe.animStage == ANIM_STAGE_RAMP_BEAM) {
                anbe.incrementProgress();

                if(!anbe.hasAllRecipeItemsForCurrentStage()) {
                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_RAMP_CANCEL;
                    anbe.syncAndSave();
                }

                if(anbe.progress > anbe.cachedSpec.ticksInRampBeam) {
                    //remove ingredient items at this point

                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_RAMP_CIRCLE;
                    anbe.syncAndSave();
                }
            }

            else if(anbe.animStage == ANIM_STAGE_RAMP_CIRCLE) {
                anbe.incrementProgress();

                //check for container item in the processing slot

                if(anbe.progress > anbe.cachedSpec.ticksInRampCircle) {
                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_SHLORPS;
                    anbe.syncAndSave();
                }
            }

            else if(anbe.animStage == ANIM_STAGE_SHLORPS) {
                if(pLevel.getGameTime() % anbe.cachedSpec.ticksBetweenShlorpPulls == 0) {
                    NonNullList<Pair<AbstractMateriaStorageBlockEntity, BlockPos>> marks = anbe.getMarkedEntitiesAndLocations();
                    NonNullList<MateriaItem> outstanding = anbe.getDemandedMateriaNotInTransit();

                    Pair<AbstractMateriaStorageBlockEntity, BlockPos> pair = marks.get(anbe.shlorpIndex);
                    MateriaItem type = pair.getFirst().getMateriaType();
                    if(type != null) {
                        for(MateriaItem mi : outstanding) {
                            if(type == mi) {
                                anbe.markInTransit(mi);
                                Pair<Vector3, Vector3> ot = pair.getFirst().getDefaultOriginAndTangent();
                                Vector3 spawnPos = new Vector3(pair.getSecond());
                                Vector3 origin = ot.getFirst();
                                Vector3 tangent = ot.getSecond().scale(4.0f);

                                int amount = 0;
                                for(ItemStack is : anbe.currentRecipe.getStages(false).get(anbe.craftingStage).componentMateria) {
                                    if(is.getItem() == type) {
                                        amount = Math.min(pair.getFirst().getCurrentStock(), is.getCount());
                                        break;
                                    }
                                }

                                if(amount == 0) break;

                                //create shlorp
                                ShlorpEntity se = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), pLevel);
                                se.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

                                se.configure(
                                        spawnPos,
                                        origin, tangent,
                                        new Vector3(anbe.getBlockPos()),
                                        new Vector3(0.5, 0.5, 0.5), new Vector3(0, 4, 0),
                                        anbe.cachedSpec.shlorpSpeed, 0.0625f, amount,
                                        mi, amount
                                );

                                pLevel.addFreshEntity(se);
                                se.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

                                //don't need to keep iterating at this point
                                break;
                            }
                        }
                    }

                    anbe.shlorpIndex = anbe.shlorpIndex == marks.size()-1 ? 0 : anbe.shlorpIndex + 1;
                }

                if(anbe.isFullySatisfied()) {
                    anbe.animStage = ANIM_STAGE_CRAFTING;
                    anbe.syncAndSave();
                }
            }

            else if(anbe.animStage == ANIM_STAGE_CRAFTING) {
                anbe.animStage = ANIM_STAGE_RAMP_CANCEL;
                anbe.syncAndSave();
            }
        }
    }

    private void cacheAnimSpec() {
        this.cachedSpec = getAnimSpec(this.powerLevel);

        InfusionStage currentStage = this.currentRecipe.getStages(false).get(this.craftingStage);

        setSatisfactionDemands(currentStage.componentMateria);
    }

    public NonNullList<Pair<AbstractMateriaStorageBlockEntity, BlockPos>> getMarkedEntitiesAndLocations() {
        NonNullList<Pair<AbstractMateriaStorageBlockEntity, BlockPos>> markedPairs = NonNullList.create();
        ItemStack stackInSlot = itemHandler.getStackInSlot(SLOT_MARKS);

        if(stackInSlot.getItem() == ItemInit.RUNE_MARKING.get()) {
            if(stackInSlot.hasTag()) {
                CompoundTag nbt = stackInSlot.getTag();

                if(nbt.contains("mark")) {
                    CompoundTag nbtMark = nbt.getCompound("mark");
                    BlockPos markedPos = new BlockPos(nbtMark.getInt("x"), nbtMark.getInt("y"), nbtMark.getInt("z"));

                    BlockEntity be = level.getBlockEntity(markedPos);
                    if(be != null) {
                        if(be instanceof AbstractMateriaStorageBlockEntity amsbe) {
                            markedPairs.add(new Pair<>(amsbe, markedPos));
                        }
                    }
                }
            }
        }
        else if(stackInSlot.getItem() == ItemInit.BOOK_MARKS.get()) {
            if(stackInSlot.hasTag()) {
                CompoundTag nbt = stackInSlot.getTag();

                if(nbt.contains("Items")) {
                    CompoundTag nbtItems = nbt.getCompound("Items");

                    for(int i=0; i<16; i++) {
                        if(nbtItems.contains(""+i)) {
                            CompoundTag nbtItem = nbtItems.getCompound(""+i).getCompound("item");
                            if(nbtItem.contains("tag")) {
                                CompoundTag nbtMark = nbtItem.getCompound("tag").getCompound("mark");

                                BlockPos markedPos = new BlockPos(nbtMark.getInt("x"), nbtMark.getInt("y"), nbtMark.getInt("z"));

                                BlockEntity be = level.getBlockEntity(markedPos);
                                if(be != null) {
                                    if(be instanceof AbstractMateriaStorageBlockEntity amsbe) {
                                        markedPairs.add(new Pair<>(amsbe, markedPos));
                                    }
                                }
                            }
                        }
                    }

                    BlockPos markedPos = new BlockPos(nbtItems.getInt("x"), nbtItems.getInt("y"), nbtItems.getInt("z"));

                    BlockEntity be = level.getBlockEntity(markedPos);
                    if(be != null) {
                        if(be instanceof AbstractMateriaStorageBlockEntity amsbe) {
                            markedPairs.add(new Pair<>(amsbe, markedPos));
                        }
                    }
                }
            }
        }

        return markedPairs;
    }

    public static AlchemicalNexusAnimSpec getAnimSpec(int pPowerLevel) {
        return new AlchemicalNexusAnimSpec(130, 70, 36, 30, 30, 0.03125f);
    }

    public boolean hasAllRecipeItemsForCurrentStage() {
        int i = 0;
        for(ItemStack is : currentRecipe.getStages(false).get(craftingStage).componentItems) {
            if(itemHandler.getStackInSlot(SLOT_INPUT_START + i).getItem() != is.getItem())
                return false;
            i++;
        }
        return true;
    }

    public SimpleContainer getContentsOfOutputSlots() {
        SimpleContainer output = new SimpleContainer(SLOT_OUTPUT_COUNT);

        for(int i = SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            output.setItem(i-SLOT_OUTPUT_START, itemHandler.getStackInSlot(i).copy());
        }

        return output;
    }

    public SimpleContainer getContentsOfInputSlots() {
        SimpleContainer input = new SimpleContainer(SLOT_INPUT_COUNT);

        for(int i = SLOT_INPUT_START; i<SLOT_INPUT_START+SLOT_INPUT_COUNT; i++) {
            input.setItem(i-SLOT_INPUT_START, itemHandler.getStackInSlot(i).copy());
        }

        return input;
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected void incrementProgress() {
        progress++;
    }

    ////////////////////
    // RECIPE HANDLING
    ////////////////////

    public List<ItemStack> getInputItems() {
        List<ItemStack> items = new ArrayList<>();

        for(int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++){
            if(!itemHandler.getStackInSlot(i).isEmpty())
                items.add(itemHandler.getStackInSlot(i));
        }

        return items;
    }

    ////////////////////
    // FLUID HANDLING
    ////////////////////

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return containedSlurry;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Config.alchemicalNexusTankCapacity;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack fluidAction) {
        return fluidAction.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get();
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction action) {
        if(action.execute()) {
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();

            //Hit capacity
            if(incomingAmount + extantAmount > getTankCapacity(0)) {
                int actualTransfer = getTankCapacity(0) - extantAmount;
                if(action == FluidAction.EXECUTE)
                    this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), getTankCapacity(0));
                return actualTransfer;
            } else {
                if(action == FluidAction.EXECUTE)
                    this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), extantAmount + incomingAmount);
                return incomingAmount;
            }
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        if(fluidAction.execute()) {
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();
            if(extantAmount >= incomingAmount) {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry.shrink(incomingAmount);
                return new FluidStack(fluid, incomingAmount);
            } else {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry = FluidStack.EMPTY;
                return new FluidStack(fluid, extantAmount);
            }
        }
        return fluidStack;
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        return drain(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), i), fluidAction);
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    public AlchemicalInfusionRecipe getCurrentRecipe() {
        return this.currentRecipe;
    }

    public ItemStack getMarkItem() {
        return itemHandler.getStackInSlot(SLOT_MARKS);
    }

    public static int getScaledSlurry(int pSlurry) {
        return (FLUID_BAR_HEIGHT * pSlurry) / Config.alchemicalNexusTankCapacity;
    }

    ////////////////////
    // VFX HANDLING
    ////////////////////

    public void handleAnimationDrivers() {
        crystalAngle = (crystalAngle + crystalRotSpeed);// % (float)(Math.PI * 2);
        itemAngle = (itemAngle + itemRotSpeed);// % (float)(Math.PI * 2);

        if(animStage == ANIM_STAGE_IDLE) {
            crystalRotSpeed = CRYSTAL_SPEED_MIN;
            itemRotSpeed = ITEM_SPEED_MIN;
            itemScale = ITEM_SCALE_START;
        }
        else if(animStage == ANIM_STAGE_RAMP_SPEEDUP) {
            float timeInStage = (float)progress / (float)cachedSpec.ticksInRampSpeedup;

            crystalRotSpeed = MathUtils.lerpf(CRYSTAL_SPEED_MIN, CRYSTAL_SPEED_MAX, timeInStage);
            itemRotSpeed = MathUtils.lerpf(ITEM_SPEED_MIN, ITEM_SPEED_MAX, timeInStage);
            itemScale = MathUtils.lerpf(ITEM_SCALE_START, ITEM_SCALE_END, timeInStage);
        }
        else if(animStage == ANIM_STAGE_RAMP_CANCEL) {
            float timeInStage = (float)progress / (float)cachedSpec.ticksInRampCancel;

            crystalRotSpeed = MathUtils.lerpf(CRYSTAL_SPEED_MAX, CRYSTAL_SPEED_MIN, timeInStage);
            itemRotSpeed = MathUtils.lerpf(ITEM_SPEED_MAX, ITEM_SPEED_MIN, timeInStage);
            itemScale = MathUtils.lerpf(ITEM_SCALE_END, ITEM_SCALE_START, timeInStage);
        }
    }

    public static final Vector3[] CANDLE_PARTICLE_ORIGINS = {
            new Vector3( 0.092,0.975,-1.483), new Vector3( 0.907,0.975,-1.483),
            new Vector3( 1.483,0.975,-0.907), new Vector3( 1.483,0.975,-0.092),
            new Vector3( 0.907,0.975, 0.483), new Vector3( 0.092,0.975, 0.483),
            new Vector3(-0.483,0.975,-0.092), new Vector3(-0.483,0.975,-0.907)
    };
    public void spawnCandleParticles() {
        BlockPos bp = getBlockPos();
        Vector3 origin = new Vector3(bp.getX(), bp.getY(), bp.getZ()).add(new Vector3(0,0,1));

        if(animStage == 0) {
            for (int i = 0; i < CANDLE_PARTICLE_ORIGINS.length; i++) {
                if ((level.getGameTime() % 6 == 0 && i % 2 == 0) || (level.getGameTime() % 6 == 3 && i % 2 == 1)) {
                    Vector3 pos = origin.add(CANDLE_PARTICLE_ORIGINS[i]);

                    level.addParticle(new MAParticleType(ParticleInit.BLUE_FLAME.get())
                            .setScale(0.05f).setMaxAge(30),
                            pos.x, pos.y, pos.z,
                            0, 0.04f, 0);
                }
            }
        }
        else {
            for (int i = 0; i < CANDLE_PARTICLE_ORIGINS.length; i++) {
                if (level.getGameTime() % 2 == 0) {
                    Vector3 pos = origin.add(CANDLE_PARTICLE_ORIGINS[i]);

                    level.addParticle(new MAParticleType(ParticleInit.ARCANE_MAGELIGHT.get())
                            .setScale(0.03f).setMaxAge(30).setColor(15, 150, 135, 255),
                            pos.x, pos.y, pos.z,
                            0, 0.005, 0);
                }
            }
        }
    }

    ////////////////////
    // ACTUATOR HANDLING
    ////////////////////

    @Override
    public void linkPlugins() {

    }

    @Override
    public void removePlugin(DirectionalPluginBlockEntity pPlugin) {

    }

    @Override
    public void linkPluginsDeferred() {

    }
}
