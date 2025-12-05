package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.PrimeAggregatorMenu;
import com.aranaira.magichem.recipe.ExaltationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrimeAggregatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
            SLOT_COUNT = 21, SLOT_INPUT_COUNT = 2,
            SLOT_ITEM_INPUT = 0, SLOT_MATERIA_INPUT = 1, SLOT_BOTTLES_OUTPUT = 2,
            SLOT_OUTPUT_START = 3, SLOT_OUTPUT_COUNT  = 4;
    public boolean clearRecipeAfterNextProcess = false;
    private ExaltationRecipe currentRecipe = null;

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler;

    public PrimeAggregatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.PRIME_AGGREGATOR_BE.get(), pPos, pBlockState);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(currentRecipe == null) return false;

                return stack.getItem() == currentRecipe.getItemType();
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PrimeAggregatorMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(0));
    }

    public ExaltationRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-2, 0, -2), getBlockPos().offset(2,4,2));
    }

    public void setRecipeByOutput(ItemStack pRecipeOutput) {
        ExaltationRecipe er = ExaltationRecipe.getExaltationRecipe(level, pRecipeOutput.getItem());

        if(er != null) {
            this.currentRecipe = er;
            this.syncAndSave();
        }
    }

    ////////////////////
    // BOILERPLATE CODE
    ////////////////////

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, PrimeAggregatorBlockEntity entity) {

    }
}
