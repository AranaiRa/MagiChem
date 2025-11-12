package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.IKeepsInventoryOnBreak;
import com.aranaira.magichem.gui.CovetousCofferMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CovetousCofferBlockEntity extends BlockEntity implements MenuProvider, IKeepsInventoryOnBreak {
    public static final int
        SLOT_COUNT = 5,
        SLOT_INPUT = 0, SLOT_OUTPUT_ITEM_1 = 1, SLOT_OUTPUT_ITEM_2 = 2, SLOT_OUTPUT_ITEM_3 = 3, SLOT_OUTPUT_ITEM_4 = 4;
    private Item
        itemType1 = null, itemType2 = null, itemType3 = null, itemType4 = null;
    private int
        itemCount1 = 0, itemCount2 = 0, itemCount3 = 0, itemCount4 = 0;
    private ItemStack
        displayStack1 = null, displayStack2 = null, displayStack3 = null, displayStack4 = null;

    public boolean isLidOpening = false;
    public float lidAngle = 0f;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public CovetousCofferBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.COVETOUS_COFFER_BE.get(), pPos, pBlockState);

        itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                ItemStack result = stack;

                if(slot == SLOT_INPUT) {
                    boolean foundType = false;
                    for(int i=1;i<=4;i++) {
                        if(getTypeFromSlotID(i) == stack.getItem()) {
                            foundType = true;
                            break;
                        }
                    }
                    if(!foundType) {
                        for(int i=1;i<=4;i++) {
                            if(getTypeFromSlotID(i) == null) {
                                foundType = true;
                                break;
                            }
                        }
                    }
                    if(foundType) {
                        result = super.insertItem(slot, stack, simulate);
                    }
                }

                return result;
            }

            @Override
            protected void onContentsChanged(int slot) {
                boolean changed = false;
                if(level != null && !level.isClientSide()){
                    if (slot == SLOT_INPUT && !getStackInSlot(SLOT_INPUT).isEmpty()) {
                        ItemStack inputStack = getStackInSlot(SLOT_INPUT);
                        int targetSlot = 0;
                        if(!inputStack.isEmpty()){
                            boolean foundType = false;
                            for (int i = 1; i <= 4; i++) {
                                if (getTypeFromSlotID(i) == inputStack.getItem()) {
                                    setCountFromSlotID(i, Math.min(ServerConfig.covetousCofferCapacity, getCountFromSlotID(i) + inputStack.getCount()));

                                    targetSlot = i;
                                    foundType = true;
                                    changed = true;
                                    break;
                                }
                            }
                            if (!foundType) {
                                for (int i = 1; i <= 4; i++) {
                                    if (getTypeFromSlotID(i) == null) {
                                        setTypeFromSlotID(i, inputStack.getItem());
                                        setCountFromSlotID(i, Math.min(ServerConfig.covetousCofferCapacity, inputStack.getCount()));

                                        targetSlot = i;
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if(targetSlot > 0) {
                            updateOutputSlot(targetSlot);
                        }
                        setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
                    }
                    else if (slot >= SLOT_OUTPUT_ITEM_1 && slot <= SLOT_OUTPUT_ITEM_4) {
                        ItemStack outputSlot = getStackInSlot(slot);
                        if(getCountFromSlotID(slot) > 0 && (outputSlot.isEmpty() || outputSlot.getCount() < outputSlot.getMaxStackSize())) {
                            int pre = getCountFromSlotID(slot);
                            updateOutputSlot(slot);
                            changed = pre != getCountFromSlotID(slot);
                        }

                        if(getCountFromSlotID(slot) <= 0 && outputSlot.isEmpty()) {
                            setTypeFromSlotID(slot, null);
                            setCountFromSlotID(slot, 0);
                            changed = true;
                        }
                    }
                }
                if(changed) {
                    syncAndSave();
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_INPUT) {
                    boolean valid1 = getStackInSlot(SLOT_OUTPUT_ITEM_1).getItem() == stack.getItem() || getStackInSlot(SLOT_OUTPUT_ITEM_1).isEmpty() || getTypeFromSlotID(1) == null;
                    boolean valid2 = getStackInSlot(SLOT_OUTPUT_ITEM_2).getItem() == stack.getItem() || getStackInSlot(SLOT_OUTPUT_ITEM_2).isEmpty() || getTypeFromSlotID(2) == null;
                    boolean valid3 = getStackInSlot(SLOT_OUTPUT_ITEM_3).getItem() == stack.getItem() || getStackInSlot(SLOT_OUTPUT_ITEM_3).isEmpty() || getTypeFromSlotID(3) == null;
                    boolean valid4 = getStackInSlot(SLOT_OUTPUT_ITEM_4).getItem() == stack.getItem() || getStackInSlot(SLOT_OUTPUT_ITEM_4).isEmpty() || getTypeFromSlotID(4) == null;
                    boolean fullyRepaired = stack.getDamageValue() == 0;

                    return fullyRepaired && (valid1 || valid2 || valid3 || valid4);
                }
                return false;
            }
        };
    }

    private void updateOutputSlot(int pID) {
        if (getTypeFromSlotID(pID) != null) {
            ItemStack stack = itemHandler.getStackInSlot(pID);
            if (stack.isEmpty()) {
                int count = Math.min(getCountFromSlotID(pID), new ItemStack(getTypeFromSlotID(pID)).getMaxStackSize());
                stack = new ItemStack(getTypeFromSlotID(pID), count);
                setCountFromSlotID(pID, Math.max(0, getCountFromSlotID(pID) - count));
                itemHandler.setStackInSlot(pID, stack);
            } else if (stack.getCount() < stack.getMaxStackSize()) {
                int maxStackSize = new ItemStack(getTypeFromSlotID(pID)).getMaxStackSize();
                int count = Math.min(getCountFromSlotID(pID), maxStackSize - stack.getCount());
                if (count > 0) {
                    stack.grow(count);
                    setCountFromSlotID(pID, Math.max(0, getCountFromSlotID(pID) - count));
                }
            }
        }
    }

    public Item getTypeFromSlotID(int pID) {
        if(pID == 1) return itemType1;
        else if(pID == 2) return itemType2;
        else if(pID == 3) return itemType3;
        else if(pID == 4) return itemType4;
        return null;
    }

    public void setTypeFromSlotID(int pID, Item pNewType) {
        if(pID == 1) itemType1 = pNewType;
        else if(pID == 2) itemType2 = pNewType;
        else if(pID == 3) itemType3 = pNewType;
        else if(pID == 4) itemType4 = pNewType;
    }

    public int getCountFromSlotID(int pID) {
        if(pID == 1) return itemCount1;
        else if(pID == 2) return itemCount2;
        else if(pID == 3) return itemCount3;
        else if(pID == 4) return itemCount4;
        return 0;
    }

    public void setCountFromSlotID(int pID, int pNewCount) {
        if(pID == 1) itemCount1 = pNewCount;
        else if(pID == 2) itemCount2 = pNewCount;
        else if(pID == 3) itemCount3 = pNewCount;
        else if(pID == 4) itemCount4 = pNewCount;
    }

    public ItemStack getOutputStackFromSlotID(int pID) {
        if(pID == 1) return itemHandler.getStackInSlot(SLOT_OUTPUT_ITEM_1);
        else if(pID == 2) return itemHandler.getStackInSlot(SLOT_OUTPUT_ITEM_2);
        else if(pID == 3) return itemHandler.getStackInSlot(SLOT_OUTPUT_ITEM_3);
        else if(pID == 4) return itemHandler.getStackInSlot(SLOT_OUTPUT_ITEM_4);

        return ItemStack.EMPTY;
    }

    public void setDisplayStackFromSlotID(int pID, Item pType) {
        if(pID == 1) displayStack1 = pType == null ? null : new ItemStack(pType);
        else if(pID == 2) displayStack2 = pType == null ? null : new ItemStack(pType);
        else if(pID == 3) displayStack3 = pType == null ? null : new ItemStack(pType);
        else if(pID == 4) displayStack4 = pType == null ? null : new ItemStack(pType);
    }

    public ItemStack getDisplayStackFromSlotID(int pID) {
        if(pID == 1) return displayStack1;
        else if(pID == 2) return displayStack2;
        else if(pID == 3) return displayStack3;
        else if(pID == 4) return displayStack4;

        return ItemStack.EMPTY;
    }

    public void updateDisplayStacks() {
        for(int i=1; i<=4; i++) {
            if(getTypeFromSlotID(i) == null) {
                setDisplayStackFromSlotID(i, null);
            }
            else if(getDisplayStackFromSlotID(i) == null && getTypeFromSlotID(i) != null) {
                setDisplayStackFromSlotID(i, getTypeFromSlotID(i));
            }
            else if(getDisplayStackFromSlotID(i).getItem() != getTypeFromSlotID(i)) {
                setDisplayStackFromSlotID(i, getTypeFromSlotID(i));
            }
        }
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
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        for(int i=1;i<=4;i++) {
            CompoundTag bufferTag = new CompoundTag();
            bufferTag.putString("item",ForgeRegistries.ITEMS.getKey(getTypeFromSlotID(i)).toString());
            bufferTag.putInt("count",getCountFromSlotID(i));
            nbt.put("itemBuffer"+i, bufferTag);
        }
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
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

        for(int i=1; i<=4; i++) {
            CompoundTag bufferTag = nbt.getCompound("itemBuffer"+i);
            ResourceLocation key = new ResourceLocation(bufferTag.getString("item"));
            setTypeFromSlotID(i, ForgeRegistries.ITEMS.getValue(key));
            setCountFromSlotID(i, bufferTag.getInt("count"));
        }

        updateDisplayStacks();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        for(int i=1;i<=4;i++) {
            CompoundTag bufferTag = new CompoundTag();
            bufferTag.putString("item",ForgeRegistries.ITEMS.getKey(getTypeFromSlotID(i)).toString());
            bufferTag.putInt("count",getCountFromSlotID(i));
            nbt.put("itemBuffer"+i, bufferTag);
        }
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
        return new CovetousCofferMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(0));
    }

    @Override
    public void packDataToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.COVETOUS_COFFER.get());

        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        for (int i = 1; i <= 4; i++) {
            CompoundTag bufferTag = new CompoundTag();
            bufferTag.putString("item", ForgeRegistries.ITEMS.getKey(getTypeFromSlotID(i)).toString());
            bufferTag.putInt("count", getCountFromSlotID(i));
            nbt.put("itemBuffer" + i, bufferTag);
        }

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {
        load(pNBT);
    }

    public boolean containsItem(ItemStack item) {
        return itemType1 == item.getItem() || itemType2 == item.getItem() || itemType3 == item.getItem() || itemType4 == item.getItem();
    }
}
