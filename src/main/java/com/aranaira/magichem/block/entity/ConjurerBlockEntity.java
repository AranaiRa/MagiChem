package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.ConjurerMenu;
import com.aranaira.magichem.gui.GrandDistilleryMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.ConjurationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConjurerBlockEntity extends BlockEntity implements MenuProvider {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public static final int
        SLOT_COUNT = 4,
        SLOT_CATALYST = 0, SLOT_OUTPUT = 1, SLOT_MATERIA_INPUT = 2, SLOT_BOTTLES = 3;
    private ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_CATALYST) {
                return ConjurationRecipe.getConjurationRecipe(level, stack) != null;
            } else if(slot == SLOT_MATERIA_INPUT) {
                return stack.getItem() instanceof MateriaItem;
            } else if(slot == SLOT_OUTPUT || slot == SLOT_BOTTLES) {
                return false;
            }

            return super.isItemValid(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            if(slot == SLOT_CATALYST)
                return 1;

            return super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    public ConjurerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CONJURER_BE.get(), pos, state);
    }

    public ConjurerBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ConjurerMenu(pContainerId, pPlayerInventory, this, null);
    }
}
