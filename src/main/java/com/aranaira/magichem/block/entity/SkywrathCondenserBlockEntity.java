package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.gui.SkywrathCondenserMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkywrathCondenserBlockEntity extends BlockEntity implements MenuProvider, IMateriaProvisionRequester, IShlorpReceiver {
    public static final int
        SLOT_COUNT = 2,
        SLOT_MATERIA = 0, SLOT_BOTTLES = 1;
    private int droplets = 0;
    public static final MateriaItem ADMIXTURE_STORM = ItemRegistry.getMateriaMap(false, false).get("storm");

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_MATERIA) return stack.getItem() instanceof MateriaItem mi && mi.getMateriaName().equals("storm");

            return false;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(slot == SLOT_MATERIA && InventoryHelper.isMateriaUnbottled(getStackInSlot(SLOT_MATERIA))) return ItemStack.EMPTY;

            return super.extractItem(slot, amount, simulate);
        }
    };

    public SkywrathCondenserBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.SKYWRATH_CONDENSER_BE.get(), pPos, pBlockState);
    }

    public void tryTriggerCraft(boolean pLightningDamageTrigger) {
        final HashMap<Integer, SkywrathAltarBlockEntity> altars = getAltarsInOperatingArea();
        boolean hasAltars = !altars.isEmpty();
        boolean hasDroplets = droplets >= (pLightningDamageTrigger ? ServerConfig.skywrathCondenserLightningTriggerCost : ServerConfig.skywrathCondenserRedstoneTriggerCost);

        if(hasAltars && hasDroplets) {
            droplets -= (pLightningDamageTrigger ? ServerConfig.skywrathCondenserLightningTriggerCost : ServerConfig.skywrathCondenserRedstoneTriggerCost);
            syncAndSave();
            for(SkywrathAltarBlockEntity altar : altars.values()) {
                if(pLightningDamageTrigger) altar.tryCraftItemFast();
                else altar.tryCraftItem();
            }
        }
    }

    public HashMap<Integer, SkywrathAltarBlockEntity> getAltarsInOperatingArea() {
        HashMap<Integer, SkywrathAltarBlockEntity> out = new HashMap<>();

        int cX = getBlockPos().getX();
        int cY = getBlockPos().getY() - 3;
        int cZ = getBlockPos().getZ();

        int id = 0;
        for(int z = cZ-1; z <= cZ+1; z++) {
            for(int x = cX+1; x >= cX-1; x--) {
                BlockEntity be = getLevel().getBlockEntity(new BlockPos(x, cY, z));
                if(be instanceof SkywrathAltarBlockEntity altar) {
                    out.put(id, altar);
                }
                id++;
            }
        }

        return out;
    }

    public void packInventoryToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.SKYWRATH_CONDENSER.get());

        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("droplets", droplets);

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        int size = pInventoryTag.getCompound("inventory").getInt("Size");
        if(size == SLOT_COUNT) {
            itemHandler.deserializeNBT(pInventoryTag.getCompound("inventory"));
        } else if(getLevel() != null && getLevel().isClientSide()) {
            final LocalPlayer player = Minecraft.getInstance().player;
            if(player != null) {
                MutableComponent msg = Component.translatable("feedback.warning.inventory_size_mismatch.part1")
                        .append(Component.translatable("block.magichem.skywrath_condenser").withStyle(ChatFormatting.GOLD))
                        .append(Component.translatable("feedback.warning.inventory_size_mismatch.part2"));
                player.displayClientMessage(msg, false);
            }
        }
        droplets = pInventoryTag.getInt("droplets");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("droplets", droplets);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        if(nbt.getCompound("inventory").getInt("Size") != itemHandler.getSlots()) {
            ItemStackHandler temp = new ItemStackHandler(nbt.getCompound("inventory").size());
            temp.deserializeNBT(nbt.getCompound("inventory"));
            for(int i=0; i<temp.getSlots(); i++) {
                itemHandler.setStackInSlot(i, temp.getStackInSlot(i));
            }
        } else {
            this.itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        }
        this.droplets = nbt.getInt("droplets");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
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

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, SkywrathCondenserBlockEntity pEntity) {
        if(!pLevel.isClientSide()) {
            final ItemStack materiaSlot = pEntity.itemHandler.getStackInSlot(SLOT_MATERIA);
            final ItemStack bottleSlot = pEntity.itemHandler.getStackInSlot(SLOT_BOTTLES);
            int upd = ServerConfig.skywrathCondenserMateriaUnitsPerDram;
            int limit = upd * 5;
            if (pEntity.droplets <= limit - upd && !materiaSlot.isEmpty()) {
                int maxDeduction = (limit - pEntity.droplets) / upd;
                int actualDeduction = Math.min(maxDeduction, materiaSlot.getCount());

                if(bottleSlot.isEmpty() || bottleSlot.getCount() <= pEntity.itemHandler.getSlotLimit(SLOT_BOTTLES) - actualDeduction) {
                    materiaSlot.shrink(actualDeduction);
                    if(!InventoryHelper.isMateriaUnbottled(materiaSlot)){
                        if (bottleSlot.isEmpty()) {
                            pEntity.itemHandler.setStackInSlot(SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE, actualDeduction));
                        } else {
                            bottleSlot.grow(actualDeduction);
                        }
                    }
                    pEntity.droplets += actualDeduction * upd;
                    pEntity.syncAndSave();
                }
            }
        }
    }

    public int getDroplets() {
        return droplets;
    }

    ////////////////////
    // PROVISIONING AND SHLORPS
    ////////////////////

    private final NonNullList<MateriaItem> activeProvisionRequests = NonNullList.create();

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        if(activeProvisionRequests.size() > 0)
            return false;
        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_MATERIA);
        if(InventoryHelper.isMateriaUnbottled(insertionStack)) {
            return insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_MATERIA) / 2;
        }
        return insertionStack.isEmpty();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_MATERIA);

        if(insertionStack.isEmpty() || insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_MATERIA) / 2) {
            result.put(ADMIXTURE_STORM, itemHandler.getSlotLimit(SLOT_MATERIA) - insertionStack.getCount());
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(pMateriaItem == ADMIXTURE_STORM)
            activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        if(pStack.getItem() == ADMIXTURE_STORM) {
            ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_MATERIA);

            if(insertionStack.isEmpty()) {
                insertionStack = pStack.copy();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("CustomModelData", 1);
                insertionStack.setTag(nbt);
            } else {
                insertionStack.grow(pStack.getCount());
            }
            itemHandler.setStackInSlot(SLOT_MATERIA, insertionStack);

            syncAndSave();

            activeProvisionRequests.remove((MateriaItem)pStack.getItem());
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() == ADMIXTURE_STORM) {
            return 0;
        }
        return pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);
        return 0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.skywrath_condenser");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new SkywrathCondenserMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(0));
    }
}
