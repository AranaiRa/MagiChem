package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.BlockEntityWithEfficiency;
import com.aranaira.magichem.block.entity.interfaces.IMateriaProcessingDevice;
import com.aranaira.magichem.gui.AlembicMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
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

public class AlembicBlockEntity extends BlockEntityWithEfficiency implements MenuProvider, IMateriaProcessingDevice {
    public static final int
        SLOT_COUNT = 14,
        SLOT_BOTTLES = 0,
        SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 3,
        SLOT_PROCESSING = 4,
        SLOT_OUTPUT_START = 5, SLOT_OUTPUT_COUNT  = 9,
        PROGRESS_BAR_WIDTH = 22,
        GRIME_BAR_WIDTH = 50,
        DATA_COUNT = 2, DATA_PROGRESS = 0, DATA_GRIME = 1;


    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected ContainerData data;
    private int progress = 0;

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
                return !(stack.getItem() instanceof MateriaItem);
            if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                return false;

            return super.isItemValid(slot, stack);
        }
    };

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.ALEMBIC_BE.get(), pos, Config.alembicEfficiency, state);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch(pIndex) {
                    case DATA_PROGRESS -> AlembicBlockEntity.this.progress;
                    case DATA_GRIME -> AlembicBlockEntity.this.grime;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch(pIndex) {
                    case DATA_PROGRESS -> AlembicBlockEntity.this.progress = pValue;
                    case DATA_GRIME -> AlembicBlockEntity.this.grime = pValue;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    //////////
    // BOILERPLATE CODE
    //////////

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.alembic");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AlembicMenu(id, inventory, this, this.data);
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
        nbt.putInt("grime", this.grime);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        grime = nbt.getInt("grime");
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



    public static void tick(Level level, BlockPos pos, BlockState state, AlembicBlockEntity entity) {
        ItemStack processingItem = entity.itemHandler.getStackInSlot(SLOT_PROCESSING);
        if(processingItem == ItemStack.EMPTY) {
            //Move an item into the processing slot
            int targetSlot = nextInputSlotWithItem(entity);
            if (targetSlot != -1) {
                CompoundTag nbt = entity.itemHandler.getStackInSlot(targetSlot).getTag();
                ItemStack stack = entity.itemHandler.extractItem(targetSlot, 1, false);
                if(nbt != null) stack.setTag(nbt);
                entity.itemHandler.insertItem(SLOT_PROCESSING, stack, false);
            }
        }

        if(entity.getGrime() >= Config.alembicMaximumGrime)
            return;

        AlchemicalCompositionRecipe recipe = getRecipeInSlot(entity);
        if(processingItem != ItemStack.EMPTY && recipe != null) {
            if(canCraftItem(entity, recipe)) {
                if (entity.progress > getOperationTicks(entity.getGrime())) {
                    if (!level.isClientSide()) {
                        craftItem(entity, recipe);
                        entity.pushData();
                    }
                    if (!entity.isStalled)
                        entity.resetProgress();
                } else
                    entity.incrementProgress();
            }
        }
        else if(processingItem == ItemStack.EMPTY)
            entity.resetProgress();
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    public static int getOperationTicks(int grime) {
        return Math.round(Config.alembicOperationTime * getTimeScalar(grime));
    }

    public int getProgress() {
        return data.get(DATA_PROGRESS);
    }

    public static int getScaledProgress(int progress, int grime) {
        return PROGRESS_BAR_WIDTH * progress / getOperationTicks(grime);
    }

    @Override
    public int getGrime() {
        return data.get(DATA_GRIME);
    }

    @Override
    public int clean() {
        int grimeDetected = getGrime();
        grime = 0;
        data.set(DATA_GRIME, grime);
        return grimeDetected / Config.grimePerWaste;
    }

    public static int getScaledGrime(int grime) {
        return (GRIME_BAR_WIDTH * grime) / Config.alembicMaximumGrime;
    }

    public static float getGrimePercent(int grime) {
        return (float)grime / (float)Config.alembicMaximumGrime;
    }

    public static int getActualEfficiency(int grime) {
        float grimeScalar = 1f - Math.min(Math.max(Math.min(Math.max(getGrimePercent(grime) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return Math.round(baseEfficiency * grimeScalar);
    }

    public static float getTimeScalar(int grime) {
        float grimeScalar = Math.min(Math.max(Math.min(Math.max(getGrimePercent(grime) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return 1f + grimeScalar * 3f;
    }

    private void pushData() {
        this.data.set(DATA_PROGRESS, progress);
        this.data.set(DATA_GRIME, grime);
    }

    ////////////////////
    // RECIPE HANDLING
    ////////////////////

    private static NonNullList<ItemStack> getRecipeComponents(AlembicBlockEntity entity) {
        Level level = entity.level;
        NonNullList<ItemStack> result = NonNullList.create();

        AlchemicalCompositionRecipe recipe = AlchemicalCompositionRecipe.getDistillingRecipe(level, entity.itemHandler.getStackInSlot(SLOT_PROCESSING));

        if(recipe != null) {
            recipe.getComponentMateria().forEach(item -> {
                result.add(new ItemStack(item.getItem(), item.getCount()));
            });
        }

        return result;
    }

    private static AlchemicalCompositionRecipe getRecipeInSlot(AlembicBlockEntity entity) {
        Level level = entity.level;

        AlchemicalCompositionRecipe recipe = AlchemicalCompositionRecipe.getDistillingRecipe(level, entity.itemHandler.getStackInSlot(SLOT_PROCESSING));

        if(recipe != null) {
            return recipe;
        }

        return null;
    }

    private static boolean canCraftItem(AlembicBlockEntity entity, AlchemicalCompositionRecipe recipe) {
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

    private static void craftItem(AlembicBlockEntity entity, AlchemicalCompositionRecipe recipe) {
        SimpleContainer outputSlots = new SimpleContainer(9);
        for(int i=0; i<SLOT_OUTPUT_COUNT; i++) {
            outputSlots.setItem(i, entity.itemHandler.getStackInSlot(SLOT_OUTPUT_START+i));
        }

        Pair<Integer, NonNullList<ItemStack>> pair = applyEfficiencyToCraftingResult(recipe.getComponentMateria(), AlembicBlockEntity.getActualEfficiency(entity.getGrime()), recipe.getOutputRate(), Config.alembicGrimeOnSuccess, Config.alembicGrimeOnFailure);
        int grimeToAdd = Math.round(pair.getFirst() * recipe.getOutputRate());
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
            ItemStack processingSlotContents = entity.itemHandler.getStackInSlot(SLOT_PROCESSING);
            processingSlotContents.shrink(1);
            if(processingSlotContents.getCount() == 0)
                entity.itemHandler.setStackInSlot(SLOT_PROCESSING, ItemStack.EMPTY);
        }

        entity.grime = Math.min(Math.max(entity.grime + grimeToAdd, 0), Config.alembicMaximumGrime);
    }

    private static int nextInputSlotWithItem(AlembicBlockEntity entity) {
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
    public SimpleContainer getContentsOfOutputSlots() {
        SimpleContainer output = new SimpleContainer(SLOT_OUTPUT_COUNT);

        for(int i=SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            output.setItem(i-SLOT_OUTPUT_START, itemHandler.getStackInSlot(i));
        }

        return output;
    }

    @Override
    public void setContentsOfOutputSlots(SimpleContainer replacementInventory) {
        for(int i=SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            itemHandler.setStackInSlot(i, replacementInventory.getItem(i-SLOT_OUTPUT_START));
        }
    }
}
