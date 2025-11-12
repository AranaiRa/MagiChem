package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.gui.CovetousCofferMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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

public class CovetousCofferBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
        SLOT_COUNT = 5,
        SLOT_INPUT = 0, SLOT_OUTPUT_ITEM_1 = 1, SLOT_OUTPUT_ITEM_2 = 2, SLOT_OUTPUT_ITEM_3 = 3, SLOT_OUTPUT_ITEM_4 = 4;
    private Item
        itemType1 = null, itemType2 = null, itemType3 = null, itemType4 = null;
    private int
        itemCount1 = 0, itemCount2 = 0, itemCount3 = 0, itemCount4 = 0;

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
//                            if(!simulate) setCountFromSlotID(i, Math.min(ServerConfig.covetousCofferCapacity, getCountFromSlotID(i) + stack.getCount()));

                            foundType = true;
                            break;
                        }
                    }
                    if(!foundType) {
                        for(int i=1;i<=4;i++) {
                            if(getTypeFromSlotID(i) == null) {
//                                if(!simulate) {
//                                    setTypeFromSlotID(i, stack.getItem());
//                                    setCountFromSlotID(i, Math.min(ServerConfig.covetousCofferCapacity, stack.getCount()));
//                                }

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
                if(level != null && !level.isClientSide()){
                    if (!getStackInSlot(SLOT_INPUT).isEmpty()) {
                        ItemStack stack = getStackInSlot(SLOT_INPUT);
                        boolean foundType = false;
                        for (int i = 1; i <= 4; i++) {
                            if (getTypeFromSlotID(i) == stack.getItem()) {
                                setCountFromSlotID(i, Math.min(ServerConfig.covetousCofferCapacity, getCountFromSlotID(i) + stack.getCount()));

                                foundType = true;
                                break;
                            }
                        }
                        if (!foundType) {
                            for (int i = 1; i <= 4; i++) {
                                if (getTypeFromSlotID(i) == null) {
                                    setTypeFromSlotID(i, stack.getItem());
                                    setCountFromSlotID(i, Math.min(ServerConfig.covetousCofferCapacity, stack.getCount()));
                                    break;
                                }
                            }
                        }
                        tryUpdateOutputSlots();

                        setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
                    } else if (slot >= SLOT_OUTPUT_ITEM_1 && slot <= SLOT_OUTPUT_ITEM_4) {
                        tryUpdateOutputSlots();
                    }
                }
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_INPUT) {
                    boolean matches1 = getStackInSlot(SLOT_OUTPUT_ITEM_1).getItem() == stack.getItem() || getStackInSlot(SLOT_OUTPUT_ITEM_1).isEmpty();
                    boolean matches2 = getStackInSlot(SLOT_OUTPUT_ITEM_2).getItem() == stack.getItem() || getStackInSlot(SLOT_OUTPUT_ITEM_2).isEmpty();
                    boolean matches3 = getStackInSlot(SLOT_OUTPUT_ITEM_3).getItem() == stack.getItem() || getStackInSlot(SLOT_OUTPUT_ITEM_3).isEmpty();
                    boolean matches4 = getStackInSlot(SLOT_OUTPUT_ITEM_4).getItem() == stack.getItem() || getStackInSlot(SLOT_OUTPUT_ITEM_4).isEmpty();

                    return matches1 || matches2 || matches3 || matches4;
                }
                return false;
            }
        };
    }

    private void tryUpdateOutputSlots() {
        boolean changed = false;
        for(int i=1; i<=4; i++) {
            if (getTypeFromSlotID(i) != null) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if(stack.isEmpty()) {
                    int count = Math.min(getCountFromSlotID(i), new ItemStack(getTypeFromSlotID(i)).getMaxStackSize());
                    stack = new ItemStack(getTypeFromSlotID(i), count);
                    setCountFromSlotID(i, Math.max(0, getCountFromSlotID(i) - count));
                    itemHandler.setStackInSlot(i, stack);
                    changed = true;
                }
                else if(stack.getCount() < stack.getMaxStackSize()) {
                    int maxStackSize = new ItemStack(getTypeFromSlotID(i)).getMaxStackSize();
                    int count = Math.min(getCountFromSlotID(i), maxStackSize - stack.getCount());
                    if(count > 0) {
                        stack.grow(count);
                        changed = true;
                    }
                }
            }
        }

        if(changed) syncAndSave();
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
}
