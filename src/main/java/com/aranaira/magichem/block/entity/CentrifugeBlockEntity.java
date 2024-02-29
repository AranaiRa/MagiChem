package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.BlockEntityWithEfficiency;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.gui.CentrifugeMenu;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeBlockEntity extends BlockEntityWithEfficiency implements MenuProvider {
    public static final int
        SLOT_COUNT = 14,
        SLOT_BOTTLES = 0, SLOT_BOTTLES_OUTPUT = 13,
        SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 3,
        SLOT_OUTPUT_START = 4, SLOT_OUTPUT_COUNT  = 9,
        GRIME_BAR_WIDTH = 50, PROGRESS_BAR_WIDTH = 24,
        DATA_COUNT = 2, DATA_PROGRESS = 0, DATA_GRIME = 1;

    public int remainingTorque = 0;
    public float wheelAngle, cogAngle;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_BOTTLES)
                return stack.getItem() == Items.GLASS_BOTTLE;
            if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT)
                return stack.getItem() instanceof AdmixtureItem;
            if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                return false;

            return super.isItemValid(slot, stack);
        }
    };

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CENTRIFUGE_BE.get(), pos, Config.centrifugeEfficiency, state);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        return CentrifugeBlockEntity.this.progress;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(CentrifugeBlockEntity.this);
                        return grime.getGrime();
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
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(CentrifugeBlockEntity.this);
                        grime.setGrime(pValue);
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

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private int progress = 0;
    protected ContainerData data;

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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
    }

    public static int getScaledProgress(CentrifugeBlockEntity entity) {
        return PROGRESS_BAR_WIDTH * entity.progress / Config.centrifugeOperationTime;
    }

    public void dropInventoryToWorld() {
        //Drop items in input slots, bottle slot, and processing slot as-is
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i < SLOT_INPUT_COUNT + 1; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);


        //Convert items in the output slots to alchemical waste
        SimpleContainer waste = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i < SLOT_OUTPUT_COUNT; i++) {
            ItemStack stack = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
            waste.setItem(i, new ItemStack(ItemRegistry.ALCHEMICAL_WASTE.get(), stack.getCount()));
        }

        Containers.dropContents(this.level, this.worldPosition, waste);
    }

    private static FixationSeparationRecipe getRecipeInSlot(CentrifugeBlockEntity entity, int slot) {
        Level level = entity.level;

        FixationSeparationRecipe recipe = FixationSeparationRecipe.getSeparatingRecipe(level, entity.itemHandler.getStackInSlot(slot));

        if(recipe != null) {
            return recipe;
        }

        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity entity) {
        entity.wheelAngle += 10.0f;
        entity.cogAngle += 2.5f;

        //skip all of this if grime is full
        if(GrimeProvider.getCapability(entity).getGrime() >= Config.centrifugeMaximumGrime)
            return;

        //figure out what slot and stack to target
        Pair<Integer, ItemStack> processing = getProcessingItem(entity);
        int processingSlot = processing.getFirst();
        ItemStack processingItem = processing.getSecond();

        FixationSeparationRecipe recipe = getRecipeInSlot(entity, processingSlot);
        if(processingItem != ItemStack.EMPTY && recipe != null) {
            if(canCraftItem(entity, recipe)) {
                if (entity.progress > Config.centrifugeOperationTime) {
                    if (!level.isClientSide())
                        craftItem(entity, recipe, processingSlot);
                    if (!entity.isStalled)
                        entity.resetProgress();
                } else
                    entity.incrementProgress();
            }
        }
        else if(processingItem == ItemStack.EMPTY)
            entity.resetProgress();
    }

    private static Pair<Integer, ItemStack> getProcessingItem(CentrifugeBlockEntity entity) {
        int processingSlot = SLOT_INPUT_START+SLOT_INPUT_COUNT-1;
        ItemStack processingItem = entity.itemHandler.getStackInSlot(processingSlot);

        if(processingItem == ItemStack.EMPTY) {
            processingSlot--;
            processingItem = entity.itemHandler.getStackInSlot(processingSlot);
        }

        if(processingItem == ItemStack.EMPTY) {
            processingSlot--;
            processingItem = entity.itemHandler.getStackInSlot(processingSlot);
        }

        return new Pair<>(processingSlot, processingItem);
    }

    private static boolean canCraftItem(CentrifugeBlockEntity entity, FixationSeparationRecipe recipe) {
        if(entity.itemHandler.getStackInSlot(CentrifugeBlockEntity.SLOT_BOTTLES_OUTPUT).getCount() == 64)
            return false;

        SimpleContainer cont = new SimpleContainer(SLOT_OUTPUT_COUNT);
        for(int i=SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            cont.setItem(i-SLOT_OUTPUT_START, entity.itemHandler.getStackInSlot(i).copy());
        }

        for(int i=0; i<recipe.getComponentMateria().size(); i++) {
            if(!cont.canAddItem(recipe.getComponentMateria().get(i).copy()))
                return false;
            cont.addItem(recipe.getComponentMateria().get(i).copy());
        }

        return true;
    }

    private static void craftItem(CentrifugeBlockEntity entity, FixationSeparationRecipe recipe, int processingSlot) {
        SimpleContainer outputSlots = new SimpleContainer(9);
        for(int i=0; i<SLOT_OUTPUT_COUNT; i++) {
            outputSlots.setItem(i, entity.itemHandler.getStackInSlot(SLOT_OUTPUT_START+i));
        }

        Pair<Integer, NonNullList<ItemStack>> pair = applyEfficiencyToCraftingResult(recipe.getComponentMateria(), CentrifugeBlockEntity.getActualEfficiency(GrimeProvider.getCapability(entity).getGrime()), 1.0f, Config.centrifugeGrimeOnSuccess, Config.centrifugeGrimeOnFailure);
        int grimeToAdd = Math.round(pair.getFirst());
        NonNullList<ItemStack> componentMateria = pair.getSecond();

        for(ItemStack item : componentMateria) {
            if(outputSlots.canAddItem(item)) {
                outputSlots.addItem(item);
            }
            else {
                entity.isStalled = true;
                break;
            }
        }

        if(!entity.isStalled) {
            for(int i=0; i<9; i++) {
                entity.itemHandler.setStackInSlot(SLOT_OUTPUT_START + i, outputSlots.getItem(i));
            }
            ItemStack processingSlotContents = entity.itemHandler.getStackInSlot(processingSlot);
            processingSlotContents.shrink(1);
            if(processingSlotContents.getCount() == 0)
                entity.itemHandler.setStackInSlot(processingSlot, ItemStack.EMPTY);
        }

        IGrimeCapability grimeCapability = GrimeProvider.getCapability(entity);
        grimeCapability.setGrime(Math.min(Math.max(grimeCapability.getGrime() + grimeToAdd, 0), Config.centrifugeMaximumGrime));
    }

    private static int nextInputSlotWithItem(CentrifugeBlockEntity entity) {
        //Select items bottom-first
        if(!entity.itemHandler.getStackInSlot(SLOT_INPUT_START + 2).isEmpty())
            return SLOT_INPUT_START + 2;
        else if(!entity.itemHandler.getStackInSlot(SLOT_INPUT_START + 1).isEmpty())
            return SLOT_INPUT_START + 1;
        else if(!entity.itemHandler.getStackInSlot(SLOT_INPUT_START).isEmpty())
            return SLOT_INPUT_START;
        else
            return -1;
    }

    private void resetProgress() {
        progress = 0;
    }

    private void incrementProgress() {
        progress++;
    }

    @Override
    public int getGrimeFromData() {
        return 0;
    }

    @Override
    public int getMaximumGrime() {
        return 0;
    }

    public static int getOperationTicks(int grime) {
        return Math.round(Config.centrifugeOperationTime * getTimeScalar(grime));
    }

    public static int getScaledProgress(int progress, int grime) {
        return PROGRESS_BAR_WIDTH * progress / getOperationTicks(grime);
    }

    @Override
    public int clean() {
        return 0;
    }

    public static int getScaledGrime(int grime) {
        return (GRIME_BAR_WIDTH * grime) / Config.centrifugeMaximumGrime;
    }

    public static float getGrimePercent(int grime) {
        return (float)grime / (float)Config.centrifugeMaximumGrime;
    }

    public static int getActualEfficiency(int grime) {
        float grimeScalar = 1f - Math.min(Math.max(Math.min(Math.max(getGrimePercent(grime) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return Math.round(baseEfficiency * grimeScalar);
    }

    public static float getTimeScalar(int grime) {
        float grimeScalar = Math.min(Math.max(Math.min(Math.max(getGrimePercent(grime) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return 1f + grimeScalar * 3f;
    }
}
