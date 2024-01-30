package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.mna.tools.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CircleFabricationBlockEntity extends BlockEntity implements MenuProvider, ContainerData, Consumer<FriendlyByteBuf> {
    public static final int
            SLOT_COUNT = 21,
            SLOT_BOTTLES = 0,
            SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 10,
            SLOT_OUTPUT_START = 11, SLOT_OUTPUT_COUNT = 10,
            PROGRESS_BAR_WIDTH = 66, PROGRESS_BAR_HEIGHT = 57;

    private static final int[] POWER_DRAW = { //TODO: Convert this to config
            1, 3, 5, 8, 12, 17, 23, 30, 39, 50,
            64, 82, 104, 132, 167, 210, 264, 332, 417, 523,
            655, 820, 1027, 1285, 1608, 2012, 2517, 3148, 3937, 4923
    };

    private static final int[] OPERATION_TICKS = { //TODO: Convert this to config
            1735, 1388, 1110, 888, 710, 568, 454, 363, 290, 232,
            185, 148, 118, 94, 75, 60, 48, 38, 30, 24,
            19, 15, 12, 9, 7, 5, 4, 3, 2, 1
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    private int
            craftingProgress = 0,
            powerUsageSetting = 1;
    private AlchemicalCompositionRecipe currentRecipe;
    private String currentRecipeID = "";
    private boolean hasSufficientPower = false;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public CircleFabricationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_FABRICATION_BE.get(), pos, state);
    }

    public void setCurrentRecipeByOutput(Item item) {
        currentRecipe = null;
        currentRecipeID = "";
        level.getRecipeManager().getAllRecipesFor(AlchemicalCompositionRecipe.Type.INSTANCE).stream().filter(
                acr -> acr.getAlchemyObject().getItem() == item).findFirst().ifPresent(filteredACR -> {
                    currentRecipe = filteredACR;
                    currentRecipeID = filteredACR.getId().toString();
        });
    }

    private void setCurrentRecipeByRecipeID() {
        currentRecipe = (AlchemicalCompositionRecipe) level.getRecipeManager().byKey(new ResourceLocation(currentRecipeID)).orElse(null);
    }

    @Nullable
    public AlchemicalCompositionRecipe getCurrentRecipe() {
        if(currentRecipe == null) {
            setCurrentRecipeByRecipeID();
        }
        return currentRecipe;
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
        nbt.putInt("craftingProgress", this.craftingProgress);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());

        if(currentRecipeID != null)
            nbt.putString("currentRecipe", this.currentRecipeID);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        craftingProgress = nbt.getInt("craftingProgress");
        powerUsageSetting = nbt.getInt("powerUsageSetting");
        ENERGY_STORAGE.setEnergy(nbt.getInt("storedPower"));
        currentRecipeID = nbt.getString("currentRecipe");
    }

    public static int getScaledProgress(CircleFabricationBlockEntity entity) {
        return entity.getCraftingProgress() * PROGRESS_BAR_WIDTH / entity.getOperationTicks();
    }

    public int getCraftingProgress(){
        return craftingProgress;
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i< itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CircleFabricationBlockEntity entity) {
        //Power check
        if(!level.isClientSide()) {
            //entity.set(DATA_HAS_SUFFICIENT_POWER, );
            entity.hasSufficientPower = entity.ENERGY_STORAGE.getEnergyStored() >= entity.getPowerDraw();
        }

        AlchemicalCompositionRecipe recipe = entity.getCurrentRecipe();
        if(entity.hasSufficientPower) {
            if(canCraftItem(entity, recipe)) {
                entity.ENERGY_STORAGE.extractEnergy(entity.getPowerDraw(), false);
                entity.incrementProgress();
            }
            else {
                entity.resetProgress();
            }

            if(entity.getCraftingProgress() > entity.getOperationTicks()) {
                craftItem(entity, recipe);
                entity.resetProgress();
            }
        } else {
            entity.resetProgress();
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        //When the game is already running, client side
        if(currentRecipeID != tag.getString("currentRecipe")) {
            currentRecipeID = tag.getString("currentRecipe");
            currentRecipe = null;
        }
        powerUsageSetting = tag.getInt("powerUsageSetting");
        craftingProgress = tag.getInt("craftingProgress");
        ENERGY_STORAGE.setEnergy(tag.getInt("storedPower"));
        super.handleUpdateTag(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
        super.onDataPacket(net, pkt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        //Create a compound tag and stuff all the info in it I need on the client side, server side
        CompoundTag data = new CompoundTag();
        data.putString("currentRecipe", currentRecipeID);
        data.putInt("powerUsageSetting", powerUsageSetting);
        data.putInt("craftingProgress", craftingProgress);
        data.putInt("storedPower", ENERGY_STORAGE.getEnergyStored());
        return data;
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private static boolean canCraftItem(CircleFabricationBlockEntity entity, AlchemicalCompositionRecipe recipe){
        if(recipe == null) {
            return false;
        }

        SimpleContainer outputSlots = getOutputAsContainer(entity);

        //If the output slots can't absorb the output item, don't bother checking anything else
        if(!outputSlots.canAddItem(recipe.getAlchemyObject())) return false;

        //TODO: Turn this back on after the bottle slot gets expanded
        //If there's not enough space for the bottles to go, don't craft
        //if(entity.itemHandler.getStackInSlot(SLOT_BOTTLES).getCount() + bottlesToInsert > 64)
        //    return false;

        List<Item> inputItemsAvailable = new ArrayList<>();
        for (ItemStack materia : recipe.getComponentMateria()) {
            int remaining = materia.getCount();

            for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
                if (entity.itemHandler.getStackInSlot(i).getItem() == materia.getItem()) {
                    remaining -= entity.itemHandler.getStackInSlot(i).getCount();

                    if (remaining <= 0) {
                        break;
                    }
                }
            }


            //If any of the ingredients are insufficient there's no reason to check more stuff
            if(remaining > 0) return false;

            inputItemsAvailable.add(materia.getItem());
        }

        //Nothing else hit a point of failure, so we must be good to craft
        return true;
    }

    private static void craftItem(CircleFabricationBlockEntity entity, AlchemicalCompositionRecipe recipe) {
        int bottlesToInsert = 0;
        for(ItemStack is : recipe.getComponentMateria()) {
            bottlesToInsert += is.getCount();
        }

        //TODO: Uncommment the original line once the bottle slot stack size has been expanded
        //Fill Bottle Slot
        //entity.itemHandler.insertItem(SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert), false);
        //TODO: Remove this once the bottle slot stack size has been expanded
        ItemStack bottles = entity.itemHandler.insertItem(SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert), false);
        SimpleContainer bottleSpill = new SimpleContainer(5);
        while(bottles.getCount() > 0) {
            int count = bottles.getCount();
            int thisPile;
            if(count > 64) {
                thisPile = 64;
                count -= 64;
            } else {
                thisPile = count;
                count = 0;
            }
            ItemStack bottlesToDrop = new ItemStack(Items.GLASS_BOTTLE, thisPile);
            bottleSpill.addItem(bottlesToDrop);
            bottles.setCount(count);
        }
        Containers.dropContents(entity.getLevel(), entity.getBlockPos(), bottleSpill);

        //Consume Materia
        for(ItemStack materia : recipe.getComponentMateria()) {
            int remaining = materia.getCount();
            for(int i=SLOT_INPUT_START + SLOT_INPUT_COUNT - 1; i >= SLOT_INPUT_START; i--) {
                if(entity.itemHandler.getStackInSlot(i).getItem() == materia.getItem()) {
                    remaining = remaining - entity.itemHandler.extractItem(i, remaining, false).getCount();
                    if (remaining == 0)
                        break;
                }
            }
        }

        SimpleContainer insert = getOutputAsContainer(entity);
        insert.addItem(recipe.getAlchemyObject());
        replaceOutputSlotsWithContainer(entity, insert);
        entity.syncAndSave();
    }

    private static void replaceOutputSlotsWithContainer(CircleFabricationBlockEntity entity, SimpleContainer insert) {
        int slotID = 0;
        for(int i=SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT; i++) {
            entity.itemHandler.setStackInSlot(i, insert.getItem(slotID));
            slotID++;
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

    private void resetProgress() {
        this.craftingProgress = 0;
    }

    private void incrementProgress() {
        this.craftingProgress++;
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

    public void setHasSufficientPower(boolean hasSufficientPower) {
        this.hasSufficientPower = hasSufficientPower;
    }

    public boolean getHasSufficientPower() {
        return this.hasSufficientPower;
    }

    // --------------------------------------
    // Data Slot Interface Stuff
    // --------------------------------------

    public static final int
            DATA_SLOT_COUNT = 1,
            DATA_HAS_SUFFICIENT_POWER = 0;

    @Override
    public int get(int pIndex) {
        switch(pIndex) {
            case DATA_HAS_SUFFICIENT_POWER: return hasSufficientPower ? 1 : 0;
        }

        return -1;
    }

    @Override
    public void set(int pIndex, int pValue) {
        switch(pIndex) {
            case DATA_HAS_SUFFICIENT_POWER:
                hasSufficientPower = pValue == 1;
                break;
        }
    }

    @Override
    public int getCount() {
        return DATA_SLOT_COUNT;
    }

    // --------------------------------------
    // Consumer Interface Stuff
    // --------------------------------------

    @Override
    public void accept(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.getBlockPos());
        friendlyByteBuf.writeBoolean(this.hasSufficientPower);
    }

    //client side, needs to be the same order as above
    public CircleFabricationBlockEntity readFrom(FriendlyByteBuf friendlyByteBuf){
        this.hasSufficientPower = friendlyByteBuf.readBoolean();
        return this;
    }
}
