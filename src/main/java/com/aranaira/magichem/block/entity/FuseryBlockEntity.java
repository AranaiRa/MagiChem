package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.gui.FuseryMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FuseryBlockEntity extends AbstractBlockEntityWithEfficiency implements MenuProvider {
    public static final int
        SLOT_COUNT = 21,
        SLOT_BOTTLES = 0, SLOT_BOTTLES_OUTPUT = 20,
        SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 10,
        SLOT_OUTPUT_START = 11, SLOT_OUTPUT_COUNT  = 9,
        PROGRESS_BAR_SIZE = 28;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_BOTTLES)
                return stack.getItem() == Items.GLASS_BOTTLE;
            if(slot == SLOT_BOTTLES_OUTPUT)
                return false;
            if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT)
                return stack.getItem() instanceof MateriaItem;
            if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                return false;

            return super.isItemValid(slot, stack);
        }
    };

    public FuseryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.FUSERY_BE.get(), pos, state);
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private FixationSeparationRecipe currentRecipe;
    private String currentRecipeID = "";

    private int craftingProgress = 0;

    public void setCurrentRecipeByOutput(Item item) {
        currentRecipe = null;
        currentRecipeID = "";
        level.getRecipeManager().getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE).stream().filter(
                fsr -> fsr.getResultAdmixture().getItem() == item).findFirst().ifPresent(filteredFSR -> {
            currentRecipe = filteredFSR;
            currentRecipeID = filteredFSR.getId().toString();
        });
    }

    private void setCurrentRecipeByRecipeID() {
        currentRecipe = (FixationSeparationRecipe) level.getRecipeManager().byKey(new ResourceLocation(currentRecipeID)).orElse(null);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
        super.onDataPacket(net, pkt);
    }

    @Nullable
    public FixationSeparationRecipe getCurrentRecipe() {
        if(currentRecipe == null) {
            setCurrentRecipeByRecipeID();
        }
        return currentRecipe;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.fusery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FuseryMenu(id, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
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
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.craftingProgress);

        if(currentRecipeID != null)
            nbt.putString("currentRecipe", this.currentRecipeID);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        craftingProgress = nbt.getInt("craftingProgress");
        currentRecipeID = nbt.getString("currentRecipe");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = new CompoundTag();
        data.putString("currentRecipe", currentRecipeID);
        data.putInt("craftingProgress", craftingProgress);
        return data;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        //When the game is already running, client side
        if(currentRecipeID != tag.getString("currentRecipe")) {
            currentRecipeID = tag.getString("currentRecipe");
            currentRecipe = null;
        }
        craftingProgress = tag.getInt("craftingProgress");
        super.handleUpdateTag(tag);
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public int getCraftingProgress(){
        return craftingProgress;
    }

    public static int getScaledProgress(FuseryBlockEntity entity) {
        return PROGRESS_BAR_SIZE * entity.craftingProgress / Config.fuseryOperationTime;
    }

    public void dropInventoryToWorld() {
        //Drop items in input slots, bottle slot, and processing slot as-is
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i < SLOT_INPUT_COUNT + 1; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);


        //Convert items in the output slots to alchemical waste
        SimpleContainer waste = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i < SLOT_OUTPUT_COUNT; i++) {
            ItemStack stack = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
            waste.setItem(i, new ItemStack(ItemRegistry.ALCHEMICAL_WASTE.get(), stack.getCount()));
        }

        Containers.dropContents(this.level, this.worldPosition, waste);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FuseryBlockEntity entity) {
        FixationSeparationRecipe recipe = entity.getCurrentRecipe();
        if(canCraftItem(entity, recipe)) {
            entity.incrementProgress();
        } else {
            entity.resetProgress();
        }

        if(entity.getCraftingProgress() > Config.fuseryOperationTime) {
            craftItem(entity, recipe);
            entity.resetProgress();
        }
    }

    private static boolean canCraftItem(FuseryBlockEntity entity, FixationSeparationRecipe recipe) {
        if(recipe == null)
            return false;

        SimpleContainer outputSlots = getOutputAsContainer(entity);

        //If the output slots can't absorb the output item, don't bother checking anything else
        if(!outputSlots.canAddItem(recipe.getResultAdmixture())) return false;

        //TODO: Turn this back on after the bottle slot gets expanded
        //If there's not enough space for the bottles to go, don't craft
        //if(entity.itemHandler.getStackInSlot(SLOT_BOTTLES).getCount() + bottlesToInsert > 64)
        //    return false;

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
        }

        return true;
    }

    private static void craftItem(FuseryBlockEntity entity, FixationSeparationRecipe recipe) {
        int bottlesToInsert = 0;
        for(ItemStack is : recipe.getComponentMateria()) {
            bottlesToInsert += is.getCount();
        }

        //TODO: Uncommment the original line once the bottle slot stack size has been expanded
        //Fill Bottle Slot
        //TODO: Remove this once the bottle slot stack size has been expanded
        ItemStack bottles = ItemStack.EMPTY;

        if(entity.itemHandler.getStackInSlot(SLOT_BOTTLES_OUTPUT) == ItemStack.EMPTY) {
            if(bottlesToInsert <= 64)
                entity.itemHandler.setStackInSlot(SLOT_BOTTLES_OUTPUT, new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert));
            else {
                entity.itemHandler.setStackInSlot(SLOT_BOTTLES_OUTPUT, new ItemStack(Items.GLASS_BOTTLE, 64));
                bottles = new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert - 64);
            }
        } else {
            int existingBottlesInSlot = entity.itemHandler.getStackInSlot(SLOT_BOTTLES_OUTPUT).getCount();
            if(bottlesToInsert + existingBottlesInSlot > 64) {
                int remainder = bottlesToInsert + existingBottlesInSlot - 64;
                entity.itemHandler.setStackInSlot(SLOT_BOTTLES_OUTPUT, new ItemStack(Items.GLASS_BOTTLE, 64));
                bottles = new ItemStack(Items.GLASS_BOTTLE, remainder);
            } else {
                entity.itemHandler.setStackInSlot(SLOT_BOTTLES_OUTPUT, new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert + existingBottlesInSlot));
            }
        }

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
        insert.addItem(recipe.getResultAdmixture());
        replaceOutputSlotsWithContainer(entity, insert);
        entity.syncAndSave();
    }

    @NotNull
    private static SimpleContainer getOutputAsContainer(FuseryBlockEntity entity) {
        SimpleContainer insert = new SimpleContainer(SLOT_OUTPUT_COUNT);
        int slotID = 0;
        //Add output item
        for (int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT; i++) {
            insert.setItem(slotID++, entity.itemHandler.getStackInSlot(i));
        }
        return insert;
    }

    private static void replaceOutputSlotsWithContainer(FuseryBlockEntity entity, SimpleContainer insert) {
        int slotID = 0;
        for(int i=SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT; i++) {
            entity.itemHandler.setStackInSlot(i, insert.getItem(slotID));
            slotID++;
        }
    }

    private void resetProgress() {
        craftingProgress = 0;
    }

    private void incrementProgress() {
        craftingProgress++;
    }

    @Override
    public int getGrimeFromData() {
        return 0;
    }

    @Override
    public int getMaximumGrime() {
        return 0;
    }

    @Override
    public int clean() {
        return 0;
    }
}
