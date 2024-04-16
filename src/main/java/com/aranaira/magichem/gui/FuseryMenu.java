package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.block.entity.container.BottleConsumingResultSlot;
import com.aranaira.magichem.block.entity.container.BottleStockSlot;
import com.aranaira.magichem.block.entity.container.OnlyMateriaInputSlot;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class FuseryMenu extends AbstractContainerMenu {

    public final FuseryBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    public OnlyMateriaInputSlot[] inputSlots = new OnlyMateriaInputSlot[FuseryBlockEntity.SLOT_INPUT_COUNT];

    public FuseryMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(FuseryBlockEntity.DATA_COUNT));
    }

    public FuseryMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.FUSERY_MENU.get(), id);
        checkContainerSize(inv, FuseryBlockEntity.SLOT_COUNT);
        blockEntity = (FuseryBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

            //Bottle slots
            this.addSlot(new BottleStockSlot(handler, FuseryBlockEntity.SLOT_BOTTLES, 134, -12, false));
            this.addSlot(new BottleStockSlot(handler, FuseryBlockEntity.SLOT_BOTTLES_OUTPUT, 80, 3, true));

            //Recipe slot
            //this.addSlot(new SlotItemHandler(handler, FuseryBlockEntity.SLOT_RECIPE, 80, 75));

            //Input item slots
            for(int i = FuseryBlockEntity.SLOT_INPUT_START; i< FuseryBlockEntity.SLOT_INPUT_START + FuseryBlockEntity.SLOT_INPUT_COUNT; i++)
            {
                int j = i - FuseryBlockEntity.SLOT_INPUT_START;
                OnlyMateriaInputSlot slot = new OnlyMateriaInputSlot(handler, i, 26 + 18 * (j % 2), 3 + 18 * (j / 2));
                this.addSlot(slot);
                inputSlots[j] = slot;
            }

            //Output item slots
            for(int i = FuseryBlockEntity.SLOT_OUTPUT_START; i< FuseryBlockEntity.SLOT_OUTPUT_START + FuseryBlockEntity.SLOT_OUTPUT_COUNT; i++)
            {
                int x = (i - FuseryBlockEntity.SLOT_OUTPUT_START) % 3;
                int y = (i - FuseryBlockEntity.SLOT_OUTPUT_START) / 3;

                this.addSlot(new BottleConsumingResultSlot(handler, i, 116 + (x) * 18, 21 + (y) * 18, FuseryBlockEntity.SLOT_BOTTLES));
            }

            setInputSlotFilters(blockEntity.getRecipeItem(FuseryBlockEntity::getVar));
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.FUSERY.get());
    }

    public void setInputSlotFilters(ItemStack pQueryStack) {
        FixationSeparationRecipe newRecipe = FixationSeparationRecipe.getSeparatingRecipe(level, pQueryStack);
        if(newRecipe != null) {
            int slotSet = 0;
            for (ItemStack stack : newRecipe.getComponentMateria()) {
                inputSlots[(slotSet * 2)].setSlotFilter((MateriaItem) stack.getItem());
                inputSlots[(slotSet * 2) + 1].setSlotFilter((MateriaItem) stack.getItem());
                slotSet++;
            }
        }
    }

    public ItemStack getRecipeItem() {
        return blockEntity.getRecipeItem(FuseryBlockEntity::getVar);
    }

    public FixationSeparationRecipe getCurrentRecipe() {
        return FixationSeparationRecipe.getSeparatingRecipe(level, getRecipeItem());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 105 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 163 )));
        }
    }

    public int getProgress() {
        return data.get(CentrifugeBlockEntity.DATA_PROGRESS);
    }

    public int getGrime() {
        return data.get(CentrifugeBlockEntity.DATA_GRIME);
    }

    public int getEfficiencyMod() {
        return data.get(CentrifugeBlockEntity.DATA_EFFICIENCY_MOD);
    }

    public int getOperationTimeMod() {
        return data.get(CentrifugeBlockEntity.DATA_OPERATION_TIME_MOD);
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;
    private static final int SLOT_BOTTLES = 36;
    private static final int SLOT_INPUT_BEGIN = 37;
    private static final int SLOT_OUTPUT_BEGIN = 41;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack targetStack = slots.get(pIndex).getItem();
        ItemStack targetStackCopy = new ItemStack(slots.get(pIndex).getItem().getItem(), slots.get(pIndex).getItem().getCount());

        //If player inventory
        if(pIndex >= SLOT_INVENTORY_BEGIN && pIndex < SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT) {
            //try to move to bottle slot first
            if(targetStack.getItem() == Items.GLASS_BOTTLE) {
                ItemStack bottleStack = slots.get(SLOT_BOTTLES).getItem();
                if(bottleStack.getCount() + targetStack.getCount() > 64) {
                    int amount = 64 - bottleStack.getCount();
                    bottleStack.grow(amount);
                    targetStack.shrink(amount);
                    slots.get(pIndex).set(targetStack);
                    return ItemStack.EMPTY;
                }
                else if(bottleStack.getCount() == 0) {
                    slots.get(SLOT_BOTTLES).set(targetStackCopy);
                    slots.get(pIndex).set(ItemStack.EMPTY);
                    return ItemStack.EMPTY;
                }
                else {
                    bottleStack.grow(targetStack.getCount());
                    targetStack = ItemStack.EMPTY;
                    slots.get(pIndex).set(targetStack);
                    return targetStack;
                }
            }
            //try to move to input slots
            moveItemStackTo(targetStackCopy, SLOT_INPUT_BEGIN, SLOT_INPUT_BEGIN + FuseryBlockEntity.SLOT_INPUT_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If bottle slot
        if(pIndex == SLOT_BOTTLES) {
            //try to move to player inventory
            moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If input slots
        if(pIndex >= SLOT_INPUT_BEGIN && pIndex < SLOT_INPUT_BEGIN + FuseryBlockEntity.SLOT_INPUT_COUNT) {
            moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If output slots
        if(pIndex >= SLOT_OUTPUT_BEGIN && pIndex < SLOT_OUTPUT_BEGIN + FuseryBlockEntity.SLOT_OUTPUT_COUNT) {
            //make sure there's enough bottles first, then try to move to player inventory
            int itemsToRemove = targetStackCopy.getCount();
            int bottles = slots.get(SLOT_BOTTLES).getItem().getCount();
            if(itemsToRemove >= bottles) {
                targetStack.setCount(bottles);
                targetStackCopy.shrink(bottles);
                if(moveItemStackTo(targetStack, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false))
                    slots.get(SLOT_BOTTLES).set(ItemStack.EMPTY);
            } else {
                if(moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false))
                    slots.get(SLOT_BOTTLES).getItem().shrink(itemsToRemove);
            }
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }

        return getSlot(pIndex).getItem();
    }
}
