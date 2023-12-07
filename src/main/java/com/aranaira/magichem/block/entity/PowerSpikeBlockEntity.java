package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.MagicCircleMenu;
import com.aranaira.magichem.item.ModItems;
import com.aranaira.magichem.util.ModEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PowerSpikeBlockEntity extends BlockEntity {
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public PowerSpikeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POWER_SPIKE.get(), pos, state);
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private BlockPos powerDrawPos;

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
        nbt.putLong("magichem.powerspike.powerDrawPos", this.powerDrawPos.asLong());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        powerDrawPos = BlockPos.of(nbt.getLong("magichem.powerspike.powerDrawPos"));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PowerSpikeBlockEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        //System.out.println("hasReagent1="+hasReagent(1, entity)+";   prog="+entity.progressReagentTier1);
    }

    public void setPowerDrawTarget(BlockPos pos) {
        this.powerDrawPos = pos;
        System.out.println("Power draw target @ "+pos);
    }

    /* FE STUFF */
    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };
}
