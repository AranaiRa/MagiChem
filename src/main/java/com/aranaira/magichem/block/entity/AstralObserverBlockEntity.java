package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.recipe.IlluminationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AstralObserverBlockEntity extends BlockEntity {
    private LuminType luminType = LuminType.NONE;
    private int
            currentLumins = 0, luminsNeeded = 0;
    private IlluminationRecipe recipe = null;

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(!simulate && level != null && !level.isClientSide()) {
                ItemStack stack = getStackInSlot(slot);
                CompoundTag nbt = new CompoundTag();
                if(stack.hasTag()) {
                    nbt = stack.getTag();
                }
                CompoundTag luminsTag = new CompoundTag();
                luminsTag.putInt("type", luminType.ordinal());
                luminsTag.putInt("current", currentLumins);
                luminsTag.putInt("needed", luminsNeeded);
                nbt.put("magichemLumins", luminsTag);
                stack.setTag(nbt);
            }

            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if(!simulate && level != null && !level.isClientSide() && stack.hasTag()) {
                final CompoundTag nbt = stack.getTag();
                if(nbt != null && nbt.contains("magichemLumins")) {
                    CompoundTag luminsTag = nbt.getCompound("magichemLumins");
                    luminType = LuminType.luminTypeFromOrdinal(luminsTag.getInt("type"));
                    currentLumins = luminsTag.getInt("current");
                    luminsNeeded = luminsTag.getInt("needed");
                }
            }

            return super.insertItem(slot, stack, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    public AstralObserverBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ASTRAL_OBSERVER_BE.get(), pPos, pBlockState);
    }

    public void dropInventory() {
        SimpleContainer inventory = new SimpleContainer(1);
        inventory.setItem(0, itemHandler.getStackInSlot(0));
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public ItemStack getItem() {
        return itemHandler.getStackInSlot(0);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        lazyItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("type", luminType.ordinal());
        nbt.putInt("current", currentLumins);
        nbt.putInt("needed", luminsNeeded);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        luminType = LuminType.luminTypeFromOrdinal(nbt.getInt("type"));
        currentLumins = nbt.getInt("current");
        luminsNeeded = nbt.getInt("needed");

        //If we have no item or if it changed, we need to reset the current recipe
        if(itemHandler.getStackInSlot(0).isEmpty() || recipe != null && recipe.getInputItem().getItem() != itemHandler.getStackInSlot(0).getItem()) {
            recipe = null;
            luminType = LuminType.NONE;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("type", luminType.ordinal());
        nbt.putInt("current", currentLumins);
        nbt.putInt("needed", luminsNeeded);
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

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof AstralObserverBlockEntity entity) {
            //we should have a recipe if there's an item present
            if((entity.luminType == null || entity.recipe == null) && !entity.itemHandler.getStackInSlot(0).isEmpty()) {
                entity.recipe = IlluminationRecipe.getIlluminationRecipe(level, entity.itemHandler.getStackInSlot(0).getItem(), entity.luminType);
            }
            entity.luminType = LuminType.SIDEREAL;
            entity.currentLumins = 374;
            entity.luminsNeeded = 500;
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3,3,3));
    }
}
