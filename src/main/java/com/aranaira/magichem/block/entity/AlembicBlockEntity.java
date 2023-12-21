package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.BlockEntityWithEfficiency;
import com.aranaira.magichem.gui.AlembicMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlembicBlockEntity extends BlockEntityWithEfficiency implements MenuProvider {
    public static final int
        SLOT_COUNT = 14,
        SLOT_BOTTLES = 0,
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
                return !(stack.getItem() instanceof MateriaItem);
            if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                return false;

            return super.isItemValid(slot, stack);
        }
    };

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.ALEMBIC_BE.get(), pos, Config.alembicEfficiency, state);
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private int progress = 0;

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.alembic");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AlembicMenu(id, inventory, this);
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

    public static int getScaledProgress(AlembicBlockEntity entity) {
        return PROGRESS_BAR_WIDTH * entity.progress / Config.alembicOperationTime;
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i< itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

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

    public static void tick(Level level, BlockPos pos, BlockState state, AlembicBlockEntity entity) {
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

        AlchemicalCompositionRecipe recipe = getRecipeInSlot(entity);
        if(processingItem != ItemStack.EMPTY && recipe != null) {
            if(entity.progress > Config.alembicOperationTime) {
                if(!level.isClientSide())
                    craftItem(entity, recipe);
                if(!entity.isStalled)
                    entity.resetProgress();
            }
            else
                entity.incrementProgress();
        }
        else if(processingItem == ItemStack.EMPTY)
            entity.resetProgress();
    }

    private static void craftItem(AlembicBlockEntity entity, AlchemicalCompositionRecipe recipe) {
        SimpleContainer outputSlots = new SimpleContainer(9);
        for(int i=0; i<SLOT_OUTPUT_COUNT; i++) {
            outputSlots.setItem(i, entity.itemHandler.getStackInSlot(SLOT_OUTPUT_START+i));
        }

        NonNullList<ItemStack> componentMateria = applyEfficiencyToCraftingResult(recipe.getComponentMateria(),
                baseEfficiency + entity.efficiencyMod, recipe.getOutputRate());

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
            if(processingSlotContents.getCount() == 0);
                entity.itemHandler.setStackInSlot(SLOT_PROCESSING, ItemStack.EMPTY);
        }
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
}
