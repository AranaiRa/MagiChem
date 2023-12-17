package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.networking.FabricationSyncDataS2CPacket;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.PacketRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.mna.tools.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CircleFabricationBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
            SLOT_COUNT = 21,
            SLOT_BOTTLES = 0,
            SLOT_INPUT_1 = 1, SLOT_INPUT_2 = 2, SLOT_INPUT_3 = 3, SLOT_INPUT_4 = 4, SLOT_INPUT_5 = 5,
            SLOT_INPUT_6 = 6, SLOT_INPUT_7 = 7, SLOT_INPUT_8 = 8, SLOT_INPUT_9 = 9, SLOT_INPUT_10 = 10,
            SLOT_OUTPUT_1 = 11, SLOT_OUTPUT_2 = 12, SLOT_OUTPUT_3 = 13, SLOT_OUTPUT_4 = 14, SLOT_OUTPUT_5 = 15,
            SLOT_OUTPUT_6 = 16, SLOT_OUTPUT_7 = 17, SLOT_OUTPUT_8 = 18, SLOT_OUTPUT_9 = 19, SLOT_OUTPUT_10 = 20,
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

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public CircleFabricationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_FABRICATION_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> CircleFabricationBlockEntity.this.craftingProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CircleFabricationBlockEntity.this.craftingProgress = value;
                }
            }

            @Override
            public int getCount() {
                return SLOT_COUNT;
            }
        };
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int
            craftingProgress = 0,
            powerLevel = 1;
    private boolean isStalled = false;
    private Item currentRecipe;
    private AlchemicalCompositionRecipe currentRecipeData;

    public void setCurrentRecipeTarget(Item item) {
        List<AlchemicalCompositionRecipe> allACRs = level.getRecipeManager().getAllRecipesFor(AlchemicalCompositionRecipe.Type.INSTANCE);
        AlchemicalCompositionRecipe targetRecipe = null;
        for(AlchemicalCompositionRecipe acr : allACRs) {
            if(acr.getAlchemyObject().getItem() == item) {
                targetRecipe = acr;
                break;
            }
        }

        if(targetRecipe != null) {
            setCurrentRecipeTarget(targetRecipe);
        } else {
            MagiChemMod.LOGGER.warn("Alchemical Composition recipe for ["+item+"] couldn't be found.");
        }
    }

    public void setCurrentRecipeTarget(AlchemicalCompositionRecipe acr) {
        this.currentRecipe = acr.getAlchemyObject().getItem();
        this.currentRecipeData = acr;
        //MagiChemMod.LOGGER.debug("current recipe is now "+currentRecipe);
        this.saveAdditional(this.getUpdateTag());
    }

    public Item getCurrentRecipeTarget() {
        return currentRecipe;
    }

    public AlchemicalCompositionRecipe getCurrentRecipeData() {
        if(currentRecipeData != null && currentRecipeData.getAlchemyObject().getItem() == currentRecipe){
            return currentRecipeData;
        }
        else {
            List<AlchemicalCompositionRecipe> allACRs = getAllRecipesSorted();
            for(AlchemicalCompositionRecipe acr : allACRs) {
                if(acr.getAlchemyObject().getItem() == currentRecipe)
                    currentRecipeData = acr;
                    return acr;
            }
        }
        return null;
    }

    private static List<AlchemicalCompositionRecipe> allRecipesSorted = new ArrayList<>();

    @NotNull
    private List<AlchemicalCompositionRecipe> getAllRecipesSorted() {
        if(allRecipesSorted.size() > 0) {
            return allRecipesSorted;
        } else {
            List<AlchemicalCompositionRecipe> allACRs = level.getRecipeManager().getAllRecipesFor(AlchemicalCompositionRecipe.Type.INSTANCE);
            HashMap<String, AlchemicalCompositionRecipe> sorter = new HashMap<>();

            for (AlchemicalCompositionRecipe acr : allACRs) {
                sorter.put(acr.getId().getNamespace() + ":" + acr.getAlchemyObject().getItem(), acr);
            }

            for(String key : sorter.keySet()) {

            }

            return allRecipesSorted;
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.circle_fabrication");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        if(!player.level.isClientSide()) {
            PacketRegistry.sendToPlayer(new FabricationSyncDataS2CPacket(
                    getBlockPos(),
                    getCurrentRecipeTarget(),
                    getPowerLevel()
            ), (ServerPlayer) player);
        }
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
        nbt.put("circle_fabrication.inventory", itemHandler.serializeNBT());
        nbt.putInt("circle_fabrication.progress", this.craftingProgress);
        nbt.putInt("circle_fabrication.powerLevel", this.powerLevel);
        nbt.putInt("circle_fabrication.energy", this.ENERGY_STORAGE.getEnergyStored());

        //MagiChemMod.LOGGER.debug("&&&& "+currentRecipe+" is trying to be ["+ForgeRegistries.ITEMS.getKey(currentRecipe).getNamespace()+":"+currentRecipe+"]");

        nbt.putString("circle_fabrication.recipe", ForgeRegistries.ITEMS.getKey(currentRecipe).getNamespace()+":"+currentRecipe);

        //MagiChemMod.LOGGER.debug("&&&& "+(level.isClientSide() ? "CLIENT" : "SERVER")+" NBT save-state...... Recipe=["+nbt.getString("circle_fabrication.recipe")+"], pl=["+nbt.getInt("circle_fabrication.powerLevel")+"]");
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        if(!level.isClientSide()) {
            //MagiChemMod.LOGGER.debug("&&&& " + (level.isClientSide() ? "CLIENT" : "SERVER") + " NBT pre-state...... Recipe=[" + currentRecipe + "], pl=[" + powerLevel + "]");

            itemHandler.deserializeNBT(nbt.getCompound("circle_fabrication.inventory"));
            craftingProgress = nbt.getInt("circle_fabrication.progress");
            powerLevel = nbt.getInt("circle_fabrication.powerLevel");
            ENERGY_STORAGE.setEnergy(nbt.getInt("circle_fabrication.energy"));

            //MagiChemMod.LOGGER.debug("&&&& " + (level.isClientSide() ? "CLIENT" : "SERVER") + " trying to load [" + nbt.getString("circle_fabrication.recipe") + "], pl @ [" + nbt.getInt("circle_fabrication.powerLevel") + "]");

            currentRecipe = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("circle_fabrication.recipe")));

            //MagiChemMod.LOGGER.debug("&&&& " + (level.isClientSide() ? "CLIENT" : "SERVER") + " NBT post-state...... Recipe=[" + currentRecipe + "], pl=[" + powerLevel + "]");

            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
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
        boolean isClientSide = level.isClientSide();
        AlchemicalCompositionRecipe recipe = entity.getCurrentRecipeData();
        if(entity.ENERGY_STORAGE.getEnergyStored() >= entity.getPowerDraw() || isClientSide) {
            //if(!isClientSide && canCraftItem(entity, recipe)) {
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
        currentRecipe = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("currentRecipe")));
        powerLevel = tag.getInt("powerLevel");
        craftingProgress = tag.getInt("craftingProgress");
        ENERGY_STORAGE.setEnergy(tag.getInt("storedPower"));
        super.handleUpdateTag(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        //Create a compound tag and stuff all the info in it I need on the client side, server side
        CompoundTag data = new CompoundTag();
        data.putString("currentRecipe", ForgeRegistries.ITEMS.getKey(currentRecipe).toString());
        data.putInt("powerLevel", powerLevel);
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
        if(recipe != null) {
            int bottlesToInsert = 0;
            for (ItemStack is : recipe.getComponentMateria()) {
                bottlesToInsert += is.getCount();
            }

            HashMap<Item, Boolean> inputItemsAvailable = new HashMap<>();
            for (ItemStack materia : recipe.getComponentMateria()) {
                int remaining = materia.getCount();
                boolean result = false;

                for (int i = SLOT_INPUT_1; i <= SLOT_INPUT_10; i++) {
                    if (entity.itemHandler.getStackInSlot(i).getItem() == materia.getItem()) {
                        remaining -= entity.itemHandler.getStackInSlot(i).getCount();

                        if (remaining <= 0) {
                            result = true;
                            break;
                        }
                    }
                }

                inputItemsAvailable.put(materia.getItem(), result);
            }

            //Comparisons
            boolean inventoryCheck = true;
            for (Item key : inputItemsAvailable.keySet())
                inventoryCheck &= inputItemsAvailable.get(key);

            //MagiChemMod.LOGGER.debug(
            //        "DATALOG\n...bottlesToInsert=" + bottlesToInsert +
            //                "\n...inventoryCheck=" + inventoryCheck +
            //                "\n...recipe=" + recipe.getAlchemyObject() +
            //                "\n...components=" + recipe.getComponentMateria());

            if (inventoryCheck) {
                if (entity.itemHandler.getStackInSlot(SLOT_BOTTLES).getCount() + bottlesToInsert <= 64) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void craftItem(CircleFabricationBlockEntity entity, AlchemicalCompositionRecipe recipe) {
        int bottlesToInsert = 0;
        for(ItemStack is : recipe.getComponentMateria()) {
            bottlesToInsert += is.getCount();
        }

        //Fill bottle slot
        Item bottle = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:glass_bottle")).asItem();
        entity.itemHandler.insertItem(SLOT_BOTTLES, new ItemStack(bottle, bottlesToInsert), false);

        //Consume materia
        for(ItemStack materia : recipe.getComponentMateria()) {
            int remaining = materia.getCount();
            for(int i=SLOT_INPUT_10; i>=SLOT_INPUT_1; i--) {
                if(entity.itemHandler.getStackInSlot(i).getItem() == materia.getItem()) {
                    remaining = remaining - entity.itemHandler.extractItem(i, remaining, false).getCount();
                    if (remaining == 0)
                        break;
                }
            }
        }

        SimpleContainer insert = new SimpleContainer(10);
        int j = 0;
        //Add output item
        for(int i=SLOT_OUTPUT_1; i<=SLOT_OUTPUT_10; i++) {
            insert.setItem(j, entity.itemHandler.getStackInSlot(i));
            j++;
        }

        insert.addItem(recipe.getAlchemyObject());
        j = 0;
        for(int i=SLOT_OUTPUT_1; i<=SLOT_OUTPUT_10; i++) {
            entity.itemHandler.setStackInSlot(i, insert.getItem(j));
            j++;
        }
    }

    private static int nextInputSlotWithItem(CircleFabricationBlockEntity entity) {
        //Select items bottom-first
        if(!entity.itemHandler.getStackInSlot(SLOT_INPUT_3).isEmpty())
            return SLOT_INPUT_3;
        else if(!entity.itemHandler.getStackInSlot(SLOT_INPUT_2).isEmpty())
            return SLOT_INPUT_2;
        else if(!entity.itemHandler.getStackInSlot(SLOT_INPUT_1).isEmpty())
            return SLOT_INPUT_1;
        else
            return -1;
    }

    private void resetProgress() {
        this.craftingProgress = 0;
    }

    private void incrementProgress() {
        this.craftingProgress++;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public int getPowerDraw() {
        return POWER_DRAW[MathUtils.clamp(powerLevel, 1, 30)-1];
    }

    public int getOperationTicks() {
        return OPERATION_TICKS[MathUtils.clamp(powerLevel, 1, 30)-1];
    }

    public int setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
        this.resetProgress();
        this.saveAdditional(this.getUpdateTag());
        if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
            ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        return this.powerLevel;
    }

    public int incrementPowerLevel() {
        if(powerLevel + 1 < 31) {
            this.powerLevel++;
            this.resetProgress();
            this.saveAdditional(this.getUpdateTag());
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        }
        return this.powerLevel;
    }

    public int decrementPowerLevel() {
        if(powerLevel - 1 > 0) {
            this.powerLevel--;
            this.resetProgress();
            this.saveAdditional(this.getUpdateTag());
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        }
        return this.powerLevel;
    }

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int actualReceive = maxReceive;

            if (maxReceive > getPowerDraw() * Config.circlePowerBuffer)
                actualReceive = getPowerDraw() * Config.circlePowerBuffer;

            return super.receiveEnergy(actualReceive, simulate);
        }
    };
}
