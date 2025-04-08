package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

import java.util.HashMap;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class MateriaReflectorBlockEntity extends BlockEntity {
    public static final int
            SLOT_COUNT = 7, SLOT_MARK = 0,
            SLOT_UP = 1, SLOT_NORTH = 2, SLOT_EAST = 3, SLOT_SOUTH = 4, SLOT_WEST = 5, SLOT_DOWN = 6;
    private BlockPos targetPos;
    private MirrorLabyrinthBlockEntity targetEntity;

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_MARK) return stack.getItem() == ItemInit.RUNE_MARKING.get();
            else return stack.getItem() instanceof MateriaItem;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            setChanged();
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            setChanged();
            return super.extractItem(slot, amount, simulate);
        }
    };

    public MateriaReflectorBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public MateriaReflectorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.MATERIA_REFLECTOR_BE.get(), pPos, pBlockState);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        unpackInventoryFromNBT(nbt.getCompound("inventory"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        return nbt;
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

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        int size = pInventoryTag.getInt("Size");
        if(size == SLOT_COUNT) {
            itemHandler.deserializeNBT(pInventoryTag);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    public int getSignalStrengthForDirection(Direction dir) {
        if(hasEntity()) {
            MateriaItem mi = (MateriaItem)(getStackForDirection(dir).getItem());
            float storageLimit = targetEntity.getStorageLimit(mi);
            float currentStock = targetEntity.getCurrentStock(mi);
            float percentFull = currentStock / storageLimit;

            return currentStock == 0 ? 0 : (int)Math.ceil(percentFull * 15f);
        }
        return 0;
    }

    private ItemStack getStackForDirection(Direction dir) {
        if(dir == Direction.UP) return itemHandler.getStackInSlot(SLOT_UP);
        else if(dir == Direction.DOWN) return itemHandler.getStackInSlot(SLOT_DOWN);
        else if(dir == Direction.NORTH) return itemHandler.getStackInSlot(SLOT_NORTH);
        else if(dir == Direction.SOUTH) return itemHandler.getStackInSlot(SLOT_SOUTH);
        else if(dir == Direction.EAST) return itemHandler.getStackInSlot(SLOT_EAST);
        else if(dir == Direction.WEST) return itemHandler.getStackInSlot(SLOT_WEST);
        return ItemStack.EMPTY;
    }

    private boolean hasEntity() {
        if(targetEntity != null) return true;
        if(targetPos != null) {
            BlockEntity be = getLevel().getBlockEntity(targetPos);
            if(be instanceof MagicMirrorBlockEntity mmbe) {
                targetEntity = mmbe.getMaster();
                return targetEntity != null;
            }
            else if(be instanceof MirrorLabyrinthBlockEntity mlbe) {
                targetEntity = mlbe;
                return true;
            }
        }
        return false;
    }


    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pState, MateriaReflectorBlockEntity pEntity) {
        if(pLevel.getGameTime() % 5 == 0) {
            boolean changed = false;

            int existingSignalU = pState.getValue(REFLECTOR_U);
            int newSignalU = pEntity.getSignalStrengthForDirection(Direction.UP);
            if(existingSignalU != newSignalU) changed = true;

            int existingSignalD = pState.getValue(REFLECTOR_D);
            int newSignalD = pEntity.getSignalStrengthForDirection(Direction.DOWN);
            if(existingSignalD != newSignalD) changed = true;

            int existingSignalN = pState.getValue(REFLECTOR_N);
            int newSignalN = pEntity.getSignalStrengthForDirection(Direction.NORTH);
            if(existingSignalN != newSignalN) changed = true;

            int existingSignalS = pState.getValue(REFLECTOR_S);
            int newSignalS = pEntity.getSignalStrengthForDirection(Direction.SOUTH);
            if(existingSignalS != newSignalS) changed = true;

            int existingSignalE = pState.getValue(REFLECTOR_E);
            int newSignalE = pEntity.getSignalStrengthForDirection(Direction.EAST);
            if(existingSignalE != newSignalE) changed = true;

            int existingSignalW = pState.getValue(REFLECTOR_W);
            int newSignalW = pEntity.getSignalStrengthForDirection(Direction.WEST);
            if(existingSignalW != newSignalW) changed = true;

            //Check for a change
            if(changed) {
                pLevel.sendBlockUpdated(pPos, pState, pState
                        .setValue(REFLECTOR_U, newSignalU)
                        .setValue(REFLECTOR_D, newSignalD)
                        .setValue(REFLECTOR_N, newSignalN)
                        .setValue(REFLECTOR_S, newSignalS)
                        .setValue(REFLECTOR_E, newSignalE)
                        .setValue(REFLECTOR_W, newSignalW),
                        3);
            }
        }
    }
}
