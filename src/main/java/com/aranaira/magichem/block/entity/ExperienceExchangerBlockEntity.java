package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.CentrifugeMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExperienceExchangerBlockEntity extends BlockEntity {
    private boolean isPushMode = false;
    public float ringRotation, ringRotationNextTick, crystalRotation, crystalRotationNextTick, crystalBob, crystalBobNextTick;

    private static final int
        RING_ROTATION_PERIOD = 160, CRYSTAL_ROTATION_PERIOD = 300, CRYSTAL_BOB_PERIOD = 200;
    private static final float
        CRYSTAL_BOB_INTENSITY = 0.1f;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get();
        }
    };

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public ExperienceExchangerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.EXPERIENCE_EXCHANGER_BE.get(), pPos, pBlockState);
    }

    public ExperienceExchangerBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    ////////////////////
    // BOILERPLATE CODE
    ////////////////////

    public static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, T t) {
        if(t instanceof ExperienceExchangerBlockEntity eebe) {
            if(pLevel.isClientSide()) {
                handleAnimationDrivers(eebe);
            }

            if(eebe.itemHandler.getStackInSlot(0) == ItemStack.EMPTY) return;

            //make a connection with the block below
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return lazyItemHandler.cast();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));

        CompoundTag crystalNBT = itemHandler.getStackInSlot(0).getOrCreateTag();
        if(crystalNBT.contains("memory_crystal_fragment_mode")) {
            isPushMode = crystalNBT.getInt("memory_crystal_fragment_mode") == 1;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());

        CompoundTag crystalNBT = itemHandler.getStackInSlot(0).getOrCreateTag();
        if(crystalNBT.contains("memory_crystal_fragment_mode")) {
            isPushMode = crystalNBT.getInt("memory_crystal_fragment_mode") == 1;
        }

        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    ////////////////////
    // DATA INTERACTION
    ////////////////////

    public ItemStack setContainedStack(ItemStack pNewStack) {
        if(itemHandler.getStackInSlot(0) == ItemStack.EMPTY) {
            if (pNewStack.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get()) {
                itemHandler.setStackInSlot(0, pNewStack);
                syncAndSave();
                return ItemStack.EMPTY;
            }
        }
        return pNewStack;
    }

    public void ejectStack() {
        if(itemHandler.getStackInSlot(0) != ItemStack.EMPTY) {
            level.addFreshEntity(new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), itemHandler.getStackInSlot(0)));
            itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            syncAndSave();
        }
    }

    ////////////////////
    // VFX
    ////////////////////

    public ItemStack getItem() {
        return itemHandler.getStackInSlot(0);
    }

    public boolean getIsPushMode() {
        return isPushMode;
    }

    private static void handleAnimationDrivers(ExperienceExchangerBlockEntity pBlockEntity) {
        long gameTime = pBlockEntity.level.getGameTime();

        pBlockEntity.ringRotation = ((float)(gameTime % RING_ROTATION_PERIOD) / RING_ROTATION_PERIOD) * (float)Math.PI * 2;
        pBlockEntity.ringRotationNextTick = ((float)((gameTime + 1) % RING_ROTATION_PERIOD) / RING_ROTATION_PERIOD) * (float)Math.PI * 2;
        if((gameTime + 1) % RING_ROTATION_PERIOD == 0)
            pBlockEntity.ringRotationNextTick += Math.PI * 2;

        pBlockEntity.crystalRotation = ((float)(gameTime % CRYSTAL_ROTATION_PERIOD) / CRYSTAL_ROTATION_PERIOD) * (float)Math.PI * 2;
        pBlockEntity.crystalRotationNextTick = ((float)((gameTime + 1) % CRYSTAL_ROTATION_PERIOD) / CRYSTAL_ROTATION_PERIOD) * (float)Math.PI * 2;
        if((gameTime + 1) % CRYSTAL_ROTATION_PERIOD == 0)
            pBlockEntity.crystalRotationNextTick += Math.PI * 2;

        pBlockEntity.crystalBob = (float)Math.sin(((float)(gameTime % CRYSTAL_BOB_PERIOD) / CRYSTAL_BOB_PERIOD) * (float)Math.PI * 2) * CRYSTAL_BOB_INTENSITY;
        pBlockEntity.crystalBobNextTick = (float)Math.sin(((float)((gameTime + 1) % CRYSTAL_BOB_PERIOD) / CRYSTAL_BOB_PERIOD) * (float)Math.PI * 2) * CRYSTAL_BOB_INTENSITY;
    }
}
