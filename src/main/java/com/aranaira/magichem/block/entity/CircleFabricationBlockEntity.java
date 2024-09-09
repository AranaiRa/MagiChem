package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractFabricationBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.mna.tools.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CircleFabricationBlockEntity extends AbstractFabricationBlockEntity implements MenuProvider, Consumer<FriendlyByteBuf>, IShlorpReceiver, IMateriaProvisionRequester {
    public static final int
            SLOT_COUNT = 22,
            SLOT_BOTTLES = 0, SLOT_RECIPE = 21,
            SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 10,
            SLOT_OUTPUT_START = 11, SLOT_OUTPUT_COUNT = 10;

    private static final int[] POWER_DRAW = { //TODO: Convert this to config
            10, 30, 50, 80, 120, 170, 230, 300, 390, 500,
            640, 820, 1040, 1320, 1670, 2100, 2640, 3320, 4170, 5230,
            6550, 8200, 10270, 12850, 16080, 20120, 25170, 31480, 39370, 49230
    };

    private static final int[] OPERATION_TICKS = { //TODO: Convert this to config
            1735, 1388, 1110, 888, 710, 568, 454, 363, 290, 232,
            185, 148, 118, 94, 75, 60, 48, 38, 30, 24,
            19, 15, 12, 9, 7, 5, 4, 3, 2, 1
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    private int
            powerUsageSetting = 1;

    public CircleFabricationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_FABRICATION_BE.get(), pos, state);

        itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    if(recipe != null) {
                        if(((slot - SLOT_INPUT_START) / 2) >= recipe.getComponentMateria().size())
                            return false;
                        ItemStack component = recipe.getComponentMateria().get((slot - SLOT_INPUT_START) / 2);
                        return stack.getItem() == component.getItem();
                    } else {
                        return false;
                    }
                }

                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    ItemStack item = super.extractItem(slot, amount, simulate);
                    if (item.hasTag()) {
                        CompoundTag nbt = item.getTag();
                        if (nbt.contains("CustomModelData")) return ItemStack.EMPTY;
                    }
                    return item;
                } else if(slot == SLOT_RECIPE) {
                    return ItemStack.EMPTY;
                }

                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                DistillationFabricationRecipe pre = recipe;
                if(slot == SLOT_RECIPE) {
                    getCurrentRecipe();
                }
                if(recipe != pre)
                    syncAndSave();
            }
        };
    }

    @Nullable
    public DistillationFabricationRecipe getCurrentRecipe() {
        if(recipe == null) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(SLOT_RECIPE);
            if(!stackInSlot.isEmpty()) {
                recipe = DistillationFabricationRecipe.getFabricatingRecipe(getLevel(), itemHandler.getStackInSlot(SLOT_RECIPE));
            }
        } else if(recipe.getAlchemyObject() != itemHandler.getStackInSlot(SLOT_RECIPE)) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(SLOT_RECIPE);
            if(!stackInSlot.isEmpty()) {
                recipe = DistillationFabricationRecipe.getFabricatingRecipe(getLevel(), itemHandler.getStackInSlot(SLOT_RECIPE));
            }
        }
        return recipe;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.circle_fabrication");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CircleFabricationMenu(id, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
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
        progress = nbt.getInt("craftingProgress");
        powerUsageSetting = nbt.getInt("powerUsageSetting");
        ENERGY_STORAGE.setEnergy(nbt.getInt("storedPower"));

        getCurrentRecipe();
    }

    public static int getScaledProgress(CircleFabricationBlockEntity entity) {
        return entity.getCraftingProgress() * 28 / entity.getOperationTicks();
    }

    public int getCraftingProgress(){
        return progress;
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i< itemHandler.getSlots(); i++) {
            if(i == SLOT_RECIPE)
                continue;

            final ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            boolean dropItem = false;
            if(stackInSlot.getItem() instanceof MateriaItem) {
                if(stackInSlot.hasTag()) {
                    if(!stackInSlot.getTag().contains("CustomModelData")) {
                        dropItem = true;
                    }
                } else {
                    dropItem = true;
                }
            } else {
                dropItem = true;
            }

            if(dropItem) inventory.setItem(i, stackInSlot);
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CircleFabricationBlockEntity entity) {
        //Power check
        if(!level.isClientSide()) {
            entity.isFESatisfied = entity.ENERGY_STORAGE.getEnergyStored() >= entity.getPowerDraw();
        }

        entity.operationTicks = entity.getOperationTicks();

        boolean changed = AbstractFabricationBlockEntity.tick(level, pos, state, entity, CircleFabricationBlockEntity::getVar);

        if(changed)
            entity.syncAndSave();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", this.itemHandler.serializeNBT());
        nbt.putInt("progress", this.progress);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        return nbt;
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @NotNull
    private static SimpleContainer getOutputAsContainer(CircleFabricationBlockEntity entity) {
        SimpleContainer insert = new SimpleContainer(SLOT_OUTPUT_COUNT);
        int slotID = 0;
        //Add output item
        for (int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT; i++) {
            insert.setItem(slotID++, entity.itemHandler.getStackInSlot(i));
        }
        return insert;
    }

    public int getPowerUsageSetting() {
        return powerUsageSetting;
    }

    public int getPowerDraw() {
        return POWER_DRAW[MathUtils.clamp(powerUsageSetting, 1, 30)-1];
    }

    public int getOperationTicks() {
        return OPERATION_TICKS[MathUtils.clamp(powerUsageSetting, 1, 30)-1];
    }

    public int setPowerUsageSetting(int pPowerUsageSetting) {
        this.powerUsageSetting = pPowerUsageSetting;
        this.resetProgress();
        if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
            ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        return this.powerUsageSetting;
    }

    public int incrementPowerUsageSetting() {
        if(powerUsageSetting + 1 < 31) {
            this.powerUsageSetting++;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    public int decrementPowerUsageSetting() {
        if(powerUsageSetting - 1 > 0) {
            this.powerUsageSetting--;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    public void setCurrentRecipe(ItemStack pQuery) {
        itemHandler.setStackInSlot(SLOT_RECIPE, pQuery);

        syncAndSave();
    }

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {

            int powerToLimit = Math.max(0, (getPowerDraw() * Config.circlePowerBuffer) - getEnergyStored());
            int actualReceive = Math.min(maxReceive, powerToLimit);

            return super.receiveEnergy(actualReceive, simulate);
        }
    };

    // --------------------------------------
    // Consumer Interface Stuff
    // --------------------------------------

    @Override
    public void accept(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.getBlockPos());
        friendlyByteBuf.writeBoolean(this.isFESatisfied);
    }

    //client side, needs to be the same order as above
    public CircleFabricationBlockEntity readFrom(FriendlyByteBuf friendlyByteBuf){
        this.isFESatisfied = friendlyByteBuf.readBoolean();
        return this;
    }

    ////////////////////
    // FINAL RETRIEVAL
    ////////////////////

    public static int getVar(IDs pID) {
        return switch(pID) {
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case SLOT_INPUT_START -> SLOT_INPUT_START;
            case SLOT_INPUT_COUNT -> SLOT_INPUT_COUNT;
            case SLOT_OUTPUT_START -> SLOT_OUTPUT_START;
            case SLOT_OUTPUT_COUNT -> SLOT_OUTPUT_COUNT;
            case SLOT_RECIPE -> SLOT_RECIPE;

            default -> -1;
        };
    }

    ////////////////////
    // MATERIA PROVISION AND SHLORPS
    ////////////////////

    private final NonNullList<MateriaItem> activeProvisionRequests = NonNullList.create();

    @Override
    public boolean allowIncreasedDeliverySize() {
        return true;
    }

    @Override
    public boolean needsProvisioning() {
        if(recipe == null)
            return false;

        return getProvisioningNeeds().size() > 0;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        if(recipe != null) {
            for (ItemStack recipeMateria : recipe.getComponentMateria()) {
                if(activeProvisionRequests.contains((MateriaItem)recipeMateria.getItem()))
                    continue;

                int amountToAdd = recipeMateria.getCount();
                for(int i=SLOT_INPUT_START; i<SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    if(stackInSlot.getItem() == recipeMateria.getItem()) {
                        amountToAdd -= stackInSlot.getCount();

                        if(amountToAdd <= 0)
                            break;
                    }
                }

                if(amountToAdd > 0)
                    result.put((MateriaItem)recipeMateria.getItem(), amountToAdd);
            }
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        CompoundTag nbt = pStack.getOrCreateTag();
        nbt.putInt("CustomModelData", 1);
        pStack.setTag(nbt);
        if(!pStack.isEmpty()) {
            activeProvisionRequests.remove((MateriaItem) pStack.getItem());

            boolean changed = false;
            for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
                if (itemHandler.isItemValid(i, pStack)) {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    if (stackInSlot.isEmpty()) {
                        int slotLimit = itemHandler.getSlotLimit(i);
                        if (pStack.getCount() <= slotLimit) {
                            itemHandler.setStackInSlot(i, pStack.copy());
                            pStack.shrink(pStack.getCount());
                            changed = true;
                        } else {
                            ItemStack copy = pStack.copy();
                            copy.setCount(slotLimit);
                            pStack.shrink(slotLimit);
                            itemHandler.setStackInSlot(i, copy);
                            changed = true;
                        }

                        if (pStack.isEmpty())
                            break;
                    } else if (stackInSlot.hasTag()) {
                        CompoundTag nbtInSlot = stackInSlot.getTag();
                        if (nbtInSlot.contains("CustomModelData")) {
                            int capacity = (itemHandler.getSlotLimit(i) - stackInSlot.getCount());
                            int delta = Math.min(capacity, pStack.getCount());
                            stackInSlot.grow(delta);
                            pStack.shrink(delta);

                            changed = true;

                            if (pStack.isEmpty())
                                break;
                        }
                    }
                }
            }

            if (changed) {
                syncAndSave();
            }
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);

        return 0;
    }
}
