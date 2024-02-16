package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.BlockEntityWithEfficiency;
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
        SLOT_COUNT = 15,
        SLOT_BOTTLES = 0, SLOT_BOTTLES_OUTPUT = 14,
        SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 3,
        SLOT_PROCESSING = 4,
        SLOT_OUTPUT_START = 5, SLOT_OUTPUT_COUNT  = 9,
        PROGRESS_BAR_WIDTH = 22;

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
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private int progress = 0;

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CentrifugeMenu(id, inventory, this);
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

    private static FixationSeparationRecipe getRecipeInSlot(CentrifugeBlockEntity entity) {
        Level level = entity.level;

        FixationSeparationRecipe recipe = FixationSeparationRecipe.getSeparatingRecipe(level, entity.itemHandler.getStackInSlot(SLOT_PROCESSING));

        if(recipe != null) {
            return recipe;
        }

        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity entity) {
        /*if(level.isClientSide()) {
            return;
        }*/

        ItemStack processingItem = entity.itemHandler.getStackInSlot(SLOT_PROCESSING);
        if(processingItem == ItemStack.EMPTY) {
            //Move an item into the processing slot
            int targetSlot = nextInputSlotWithItem(entity);
            if (targetSlot != -1) {
                ItemStack stack = entity.itemHandler.extractItem(targetSlot, 1, false);
                entity.itemHandler.insertItem(SLOT_PROCESSING, stack, false);
            }
        }

        FixationSeparationRecipe recipe = getRecipeInSlot(entity);
        if(processingItem != ItemStack.EMPTY && recipe != null) {
            if(canCraftItem(entity, recipe)) {
                if (entity.progress > Config.centrifugeOperationTime) {
                    if (!level.isClientSide())
                        craftItem(entity, recipe);
                    if (!entity.isStalled)
                        entity.resetProgress();
                } else
                    entity.incrementProgress();
            }
        }
        else if(processingItem == ItemStack.EMPTY)
            entity.resetProgress();
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

    private static void craftItem(CentrifugeBlockEntity entity, FixationSeparationRecipe recipe) {
        SimpleContainer outputSlots = new SimpleContainer(9);
        for(int i=0; i<SLOT_OUTPUT_COUNT; i++) {
            outputSlots.setItem(i, entity.itemHandler.getStackInSlot(SLOT_OUTPUT_START+i));
        }

        Pair<Integer, NonNullList<ItemStack>> pair = applyEfficiencyToCraftingResult(recipe.getComponentMateria(), entity.getActualEfficiency(), 1.0f, Config.alembicGrimeOnSuccess, Config.alembicGrimeOnFailure);
        int grimeToAdd = pair.getFirst();
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

            ItemStack outputBottles = entity.itemHandler.getStackInSlot(CentrifugeBlockEntity.SLOT_BOTTLES_OUTPUT);
            if(outputBottles.getItem() == Items.GLASS_BOTTLE) {
                outputBottles.grow(1);
            } else {
                outputBottles = new ItemStack(Items.GLASS_BOTTLE, 1);
            }
            entity.itemHandler.setStackInSlot(CentrifugeBlockEntity.SLOT_BOTTLES_OUTPUT, outputBottles);
        }
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
}
