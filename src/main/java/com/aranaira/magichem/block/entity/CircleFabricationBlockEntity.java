package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.BlockEntityWithEfficiency;
import com.aranaira.magichem.gui.AlembicMenu;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.item.Item;
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
            SLOT_INPUT_1 = 1, SLOT_INPUT_2 = 2, SLOT_INPUT_3 = 3, SLOT_INPUT_4 = 4, SLOT_INPUT_5 = 5,
            SLOT_INPUT_6 = 6, SLOT_INPUT_7 = 7, SLOT_INPUT_8 = 8, SLOT_INPUT_9 = 9, SLOT_INPUT_10 = 10,
            SLOT_OUTPUT_1 = 11, SLOT_OUTPUT_2 = 12, SLOT_OUTPUT_3 = 13, SLOT_OUTPUT_4 = 14, SLOT_OUTPUT_5 = 15,
            SLOT_OUTPUT_6 = 16, SLOT_OUTPUT_7 = 17, SLOT_OUTPUT_8 = 18, SLOT_OUTPUT_9 = 19, SLOT_OUTPUT_10 = 20,
            PROGRESS_BAR_WIDTH = 66, PROGRESS_BAR_HEIGHT = 34;

    private static final int[] powerDraw = { //TODO: Convert this to config
            1, 3, 5, 8, 12, 17, 23, 30, 39, 50,
            64, 82, 104, 132, 167, 210, 264, 332, 417, 523,
            655, 820, 1027, 1285, 1608, 2012, 2517, 3148, 3937, 4923
    };

    private static final int[] operationTicks = { //TODO: Convert this to config
            1735, 1388, 1110, 888, 710, 568, 454, 363, 290, 232,
            185, 148, 118, 94, 75, 60, 48, 38, 30, 24,
            19, 15, 12, 9, 7, 5, 4, 3, 2, 1
    };

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
                return 21;
            }
        };
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int
            progress = 0,
            powerLevel = 1;
    private boolean isStalled = false;
    private Item currentRecipe;

    public void setCurrentRecipeTarget(Item currentRecipe) {
        this.currentRecipe = currentRecipe;
    }

    public Item getCurrentRecipeTarget() {
        return currentRecipe;
    }

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

    public static void tick(Level level, BlockPos pos, BlockState state, CircleFabricationBlockEntity entity) {
        if(!level.isClientSide()) {
            return;
        }
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

    public int getPowerLevel() {
        return powerLevel;
    }

    public int getPowerDraw() {
        return powerDraw[powerLevel-1];
    }

    public int getOperationTicks() {
        return operationTicks[powerLevel-1];
    }

    public int setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
        return this.powerLevel;
    }

    public int incrementPowerLevel() {
        if(powerLevel + 1 < 31)
            this.powerLevel++;
        return this.powerLevel;
    }

    public int decrementPowerLevel() {
        if(powerLevel - 1 > 0)
        this.powerLevel--;
        return this.powerLevel;
    }
}
