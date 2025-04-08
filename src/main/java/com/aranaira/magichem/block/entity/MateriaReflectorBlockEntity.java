package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.entity.routers.MirrorLabyrinthRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.gui.MateriaReflectorMenu;
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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

public class MateriaReflectorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
            SLOT_COUNT = 7, SLOT_MARK = 0,
            SLOT_UP = 1, SLOT_NORTH = 2, SLOT_EAST = 3, SLOT_SOUTH = 4, SLOT_WEST = 5, SLOT_DOWN = 6;
    private BlockPos targetPos;
    private MirrorLabyrinthBlockEntity targetEntity;
    private final HashMap<Direction, Integer> signals;
    private ContainerData data = new SimpleContainerData(0);

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
            if(slot == SLOT_MARK) {
                targetPos = null;
                targetEntity = null;
            }
            return super.extractItem(slot, amount, simulate);
        }
    };

    public MateriaReflectorBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);

        signals = new HashMap<>();
        signals.put(Direction.UP, 0);
        signals.put(Direction.DOWN, 0);
        signals.put(Direction.NORTH, 0);
        signals.put(Direction.SOUTH, 0);
        signals.put(Direction.EAST, 0);
        signals.put(Direction.WEST, 0);
    }

    public MateriaReflectorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.MATERIA_REFLECTOR_BE.get(), pPos, pBlockState);

        signals = new HashMap<>();
        signals.put(Direction.UP, 0);
        signals.put(Direction.DOWN, 0);
        signals.put(Direction.NORTH, 0);
        signals.put(Direction.SOUTH, 0);
        signals.put(Direction.EAST, 0);
        signals.put(Direction.WEST, 0);
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
        nbt.put("signals", packSignalsToTag());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        unpackInventoryFromNBT(nbt.getCompound("inventory"));
        unpackSignalsFromTag(nbt.getCompound("signals"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.put("signals", packSignalsToTag());
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState().setValue(NEEDS_HARD_UPDATE, true), 3);
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
    }

    public void clearHardUpdateState() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState().setValue(NEEDS_HARD_UPDATE, false), 3);
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

    private int calculateSignalStrengthForDirection(Direction dir) {
        if(hasEntity()) {
            ItemStack stackQuery = getStackForDirection(dir.getOpposite());
            if(stackQuery.getItem() instanceof MateriaItem mi) {
                float storageLimit = targetEntity.getStorageLimit(mi);
                float currentStock = targetEntity.getCurrentStock(mi);
                float percentFull = currentStock / storageLimit;

                return currentStock == 0 ? 0 : (int) Math.min(15, Math.ceil(percentFull * 15f));
            }
        }
        return 0;
    }

    public int getSignalStrengthForDirection(Direction dir) {
        return signals.get(dir);
    }

    private void unpackSignalsFromTag(CompoundTag nbt) {
        signals.clear();

        signals.put(Direction.UP, nbt.getInt("u"));
        signals.put(Direction.DOWN, nbt.getInt("d"));
        signals.put(Direction.NORTH, nbt.getInt("n"));
        signals.put(Direction.SOUTH, nbt.getInt("s"));
        signals.put(Direction.EAST, nbt.getInt("e"));
        signals.put(Direction.WEST, nbt.getInt("w"));
    }

    private CompoundTag packSignalsToTag() {
        CompoundTag nbt = new CompoundTag();

        nbt.putInt("u", signals.getOrDefault(Direction.UP, 0));
        nbt.putInt("d", signals.getOrDefault(Direction.DOWN, 0));
        nbt.putInt("n", signals.getOrDefault(Direction.NORTH, 0));
        nbt.putInt("s", signals.getOrDefault(Direction.SOUTH, 0));
        nbt.putInt("e", signals.getOrDefault(Direction.EAST, 0));
        nbt.putInt("w", signals.getOrDefault(Direction.WEST, 0));

        return nbt;
    }

    public ItemStack getStackForDirection(Direction dir) {
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
        if(!pLevel.isClientSide() && pLevel.getGameTime() % 5 == 0) {
            if(pState.getValue(NEEDS_HARD_UPDATE)) {
                pEntity.clearHardUpdateState();
            }

            if(!pEntity.itemHandler.getStackInSlot(SLOT_MARK).isEmpty()) {
                if (pEntity.targetEntity == null) {
                    ItemStack markStack = pEntity.itemHandler.getStackInSlot(SLOT_MARK);
                    if (pEntity.targetPos == null && markStack.hasTag()) {
                        CompoundTag mark = markStack.getTag().getCompound("mark");
                        int x = mark.getInt("x");
                        int y = mark.getInt("y");
                        int z = mark.getInt("z");
                        pEntity.targetPos = new BlockPos(x, y, z);
                    }
                    if (pEntity.targetPos != null) {
                        BlockEntity be = pLevel.getBlockEntity(pEntity.targetPos);
                        if (be instanceof MirrorLabyrinthBlockEntity mlbe) {
                            pEntity.targetEntity = mlbe;
                        } else if (be instanceof MirrorLabyrinthRouterBlockEntity router) {
                            pEntity.targetEntity = router.getMaster();
                        } else if (be instanceof MagicMirrorBlockEntity mirror) {
                            pEntity.targetEntity = mirror.getMaster();
                        }
                    }
                }

                if (pEntity.targetEntity != null) {

                    boolean changed = false;

                    for (Direction dir : Direction.values()) {
                        int existingSignal = pEntity.signals.getOrDefault(dir, 0);
                        int newSignal = pEntity.calculateSignalStrengthForDirection(dir);

                        if (existingSignal != newSignal) {
                            changed = true;
                            pEntity.signals.put(dir, newSignal);
                        }
                    }

                    //Check for a change
                    if (changed && !pLevel.isClientSide()) {
                        pEntity.syncAndSave();
                    }
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new MateriaReflectorMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap);
    }
}
