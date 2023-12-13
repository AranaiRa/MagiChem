package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.BlockEntityWithEfficiency;
import com.aranaira.magichem.gui.AlembicMenu;
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
        SLOT_BOTTLES = 0,
        SLOT_INPUT_1 = 1, SLOT_INPUT_2 = 2, SLOT_INPUT_3 = 3,
        SLOT_PROCESSING = 4,
        SLOT_OUTPUT_1 = 5, SLOT_OUTPUT_2  = 6, SLOT_OUTPUT_3  = 7,
        SLOT_OUTPUT_4 = 8, SLOT_OUTPUT_5  = 9, SLOT_OUTPUT_6  = 10,
        SLOT_OUTPUT_7 = 11, SLOT_OUTPUT_8 = 12, SLOT_OUTPUT_9 = 13;

    private final ItemStackHandler itemHandler = new ItemStackHandler(21) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.ALEMBIC_BE.get(), pos, Config.alembicEfficiency, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> AlembicBlockEntity.this.progress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> AlembicBlockEntity.this.progress = value;
                }
            }

            @Override
            public int getCount() {
                return 14;
            }
        };
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int
            progress = 0;

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.distillery");
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
        nbt.put("alembic.inventory", itemHandler.serializeNBT());
        nbt.putInt("alembic.progress", this.progress);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("alembic.progress");
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

    private static SimpleContainer tryInsertIngredientsIntoOutputSlot(SimpleContainer inventory, NonNullList<Ingredient> ingredients) {
        SimpleContainer outputSlots = new SimpleContainer(9);
        for(int i=SLOT_OUTPUT_1; i<=SLOT_OUTPUT_9; i++) {
            outputSlots.setItem(i, inventory.getItem(i));
        }

        ingredients.forEach(ing -> {
            MagiChemMod.LOGGER.debug(ing.toString());
        });

        return outputSlots;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AlembicBlockEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        if(entity.itemHandler.getStackInSlot(SLOT_PROCESSING) == ItemStack.EMPTY) {
            //Move an item into the processing slot
            int targetSlot = nextInputSlotWithItem(entity);
            if (targetSlot != -1) {
                ItemStack stack = entity.itemHandler.extractItem(targetSlot, 1, false);
                entity.itemHandler.insertItem(SLOT_PROCESSING, stack, false);
            }
        }

        AlchemicalCompositionRecipe recipe = getRecipeInSlot(entity);
        if(entity.itemHandler.getStackInSlot(SLOT_PROCESSING) != ItemStack.EMPTY && recipe != null) {
            entity.incrementProgress();
            if(entity.progress > /*Config.alembicOperationTime*/60) {
                craftItem(entity, recipe);
                if(!entity.isStalled)
                    entity.resetProgress();
            }
            MagiChemMod.LOGGER.debug("Ticking, "+entity.progress+" of "+Config.alembicOperationTime);
        }
    }

    private static void craftItem(AlembicBlockEntity entity, AlchemicalCompositionRecipe recipe) {
        SimpleContainer outputSlots = new SimpleContainer(9);
        for(int i=0; i<9; i++) {
            outputSlots.setItem(i, entity.itemHandler.getStackInSlot(SLOT_OUTPUT_1+i));
        }

        NonNullList<ItemStack> componentMateria = applyEfficiencyToCraftingResult(recipe.getComponentMateria(),
                baseEfficiency + entity.efficiencyMod);

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
                entity.itemHandler.setStackInSlot(SLOT_OUTPUT_1+i, outputSlots.getItem(i));
            }
            ItemStack processingSlotContents = entity.itemHandler.getStackInSlot(SLOT_PROCESSING);
            processingSlotContents.shrink(1);
            if(processingSlotContents.getCount() == 0);
                entity.itemHandler.setStackInSlot(SLOT_PROCESSING, ItemStack.EMPTY);
        }
    }

    private static int nextInputSlotWithItem(AlembicBlockEntity entity) {
        //Select items bottom-first
        if(!entity.itemHandler.getStackInSlot(SLOT_INPUT_3).isEmpty())
            return SLOT_INPUT_3;
        else if(!entity.itemHandler.getStackInSlot(SLOT_INPUT_2).isEmpty())
            return SLOT_INPUT_2;
        else if(!entity.itemHandler.getStackInSlot(SLOT_INPUT_1).isEmpty())
            return SLOT_INPUT_1;
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
