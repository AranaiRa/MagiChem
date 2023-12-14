package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.BlockEntityWithEfficiency;
import com.aranaira.magichem.gui.AlembicMenu;
import com.aranaira.magichem.gui.CircleFabricationMenu;
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

public class CircleFabricationBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
            SLOT_BOTTLES = 0,
            SLOT_INPUT_1 = 1, SLOT_INPUT_2 = 2, SLOT_INPUT_3 = 3,
            SLOT_PROCESSING = 4,
            SLOT_OUTPUT_1 = 5, SLOT_OUTPUT_2  = 6, SLOT_OUTPUT_3  = 7,
            SLOT_OUTPUT_4 = 8, SLOT_OUTPUT_5  = 9, SLOT_OUTPUT_6  = 10,
            SLOT_OUTPUT_7 = 11, SLOT_OUTPUT_8 = 12, SLOT_OUTPUT_9 = 13,
            PROGRESS_BAR_WIDTH = 22;

    private final ItemStackHandler itemHandler = new ItemStackHandler(21) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public CircleFabricationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_FABRICATION_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> CircleFabricationBlockEntity.this.progress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CircleFabricationBlockEntity.this.progress = value;
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
    private int progress = 0;
    private boolean isStalled = false;

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.circle_fabrication");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CircleFabricationMenu(id, inventory, this);
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
        nbt.put("circle_fabrication.inventory", itemHandler.serializeNBT());
        nbt.putInt("circle_fabrication.progress", this.progress);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("circle_fabrication.inventory"));
        progress = nbt.getInt("circle_fabrication.progress");
    }

    public static int getScaledProgress(CircleFabricationBlockEntity entity) {
        return 0;
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i< itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    private static NonNullList<ItemStack> getRecipeComponents(CircleFabricationBlockEntity entity) {
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

    private static AlchemicalCompositionRecipe getRecipeInSlot(CircleFabricationBlockEntity entity) {
        Level level = entity.level;

        AlchemicalCompositionRecipe recipe = AlchemicalCompositionRecipe.getDistillingRecipe(level, entity.itemHandler.getStackInSlot(SLOT_PROCESSING));

        if(recipe != null) {
            return recipe;
        }

        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CircleFabricationBlockEntity entity) {
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

    private static void craftItem(CircleFabricationBlockEntity entity, AlchemicalCompositionRecipe recipe) {
    }

    private static int nextInputSlotWithItem(CircleFabricationBlockEntity entity) {
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
