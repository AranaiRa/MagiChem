package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.IKeepsInventoryOnBreak;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.gui.DisintegrationPyreMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.IlluminationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import java.util.Map;

public class DisintegrationPyreBlockEntity extends BlockEntity implements MenuProvider, IMateriaProvisionRequester, IShlorpReceiver, IKeepsInventoryOnBreak {
    public static final int
        SLOT_COUNT = 3,
        SLOT_ITEM = 0, SLOT_MATERIA = 1, SLOT_BOTTLES = 2;

    private int
            percent = 50, droplets = 0;

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_ITEM) return stack.getItem().isDamageable(stack);
            if(slot == SLOT_MATERIA) return stack.getItem() instanceof MateriaItem mi && mi.getMateriaName().equals("destruction");

            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    public DisintegrationPyreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.DISINTEGRATION_PYRE_BE.get(), pPos, pBlockState);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        lazyItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
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
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("percent", percent);
        nbt.putInt("droplets", droplets);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        percent = nbt.getInt("percent");
        droplets = nbt.getInt("droplets");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("percent", percent);
        nbt.putInt("droplets", droplets);
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

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new DisintegrationPyreMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(0));
    }

    public void setPercent(int pNewPercent) {
        percent = pNewPercent;
    }

    public int getPercent() {
        return percent;
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        return false;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return null;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void provide(ItemStack pStack) {

    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return 0;
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return 0;
    }

    @Override
    public void packDataToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.DISINTEGRATION_PYRE.get());

        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("percent", percent);
        nbt.putInt("droplets", droplets);

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {
        if(pNBT.contains("inventory")) {
            itemHandler.deserializeNBT(pNBT.getCompound("inventory"));
            percent = pNBT.getInt("percent");
            droplets = pNBT.getInt("droplets");
        }
    }

    public int getDroplets() {
        return droplets;
    }

    public float getDropletsPercent() {
        return (float)droplets / (float)(ServerConfig.disintegrationPyreMateriaUnitsPerDram * 3);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof DisintegrationPyreBlockEntity entity) {
            if(!level.isClientSide()) {
                boolean changed = false;

                if(!entity.itemHandler.getStackInSlot(SLOT_MATERIA).isEmpty()) {
                    int fill = ServerConfig.disintegrationPyreMateriaUnitsPerDram;
                    int limit = fill * 3;

                    //Fill gauge with droplets
                    int capacity = limit - entity.droplets;
                    if (capacity >= fill) {
                        ItemStack materiaStack = entity.itemHandler.getStackInSlot(SLOT_MATERIA);
                        ItemStack bottleStack = entity.itemHandler.getStackInSlot(SLOT_BOTTLES);

                        if(InventoryHelper.isMateriaUnbottled(materiaStack)) {
                            materiaStack.shrink(1);
                            entity.droplets = Math.min(limit, entity.droplets + fill);
                            changed = true;
                        }
                        else if(bottleStack.isEmpty() || bottleStack.getCount() < bottleStack.getMaxStackSize()) {
                            materiaStack.shrink(1);
                            if(bottleStack.isEmpty()) {
                                entity.itemHandler.setStackInSlot(SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE));
                            } else {
                                bottleStack.grow(1);
                            }
                            entity.droplets = Math.min(limit, entity.droplets + fill);
                            changed = true;
                        }
                    }
                }

                //Damage item in slot
                ItemStack stack = entity.itemHandler.getStackInSlot(SLOT_ITEM);
                if(!stack.isEmpty() && level.getGameTime() % 6 == 0) {
                    int maxDurability = stack.getMaxDamage();
                    int targetDurability = Math.min(maxDurability-1, Math.round((float)maxDurability * (1 - ((float)entity.percent / 100f))));
                    int remainingDurability = targetDurability - stack.getDamageValue();

                    if(remainingDurability > 0) {
                        int drain = Math.min(ServerConfig.disintegrationPyreMateriaUnitsPerDram, remainingDurability);
                        drain = Math.min(entity.droplets, drain);

                        entity.droplets = Math.max(0, entity.droplets - drain);
                        stack.setDamageValue(stack.getDamageValue() + drain);
                        changed = true;
                    }
                }

                if(changed) {
                    entity.syncAndSave();
                }
            }
        }
    }
}
