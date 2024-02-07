package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.AdmixerBlockEntity;
import com.aranaira.magichem.block.entity.container.BottleConsumingResultSlot;
import com.aranaira.magichem.block.entity.container.BottleStockSlot;
import com.aranaira.magichem.block.entity.container.OnlyAdmixtureInputSlot;
import com.aranaira.magichem.block.entity.container.OnlyMateriaInputSlot;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public class AdmixerMenu extends AbstractContainerMenu {

    public final AdmixerBlockEntity blockEntity;
    private final Level level;
    public OnlyMateriaInputSlot[] inputSlots = new OnlyMateriaInputSlot[AdmixerBlockEntity.SLOT_INPUT_COUNT];

    public AdmixerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, Minecraft.getInstance().level.getBlockEntity(extraData.readBlockPos()));
    }

    public AdmixerMenu(int id, Inventory inv, BlockEntity entity) {
        super(MenuRegistry.ADMIXER_MENU.get(), id);
        checkContainerSize(inv, AdmixerBlockEntity.SLOT_COUNT);
        blockEntity = (AdmixerBlockEntity) entity;
        this.level = Minecraft.getInstance().level;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

            //Bottle slots
            this.addSlot(new BottleStockSlot(handler, AdmixerBlockEntity.SLOT_BOTTLES, 134, -12, false));
            this.addSlot(new BottleStockSlot(handler, AdmixerBlockEntity.SLOT_BOTTLES_OUTPUT, 80, 3, true));

            //Input item slots
            for(int i=AdmixerBlockEntity.SLOT_INPUT_START; i<AdmixerBlockEntity.SLOT_INPUT_START + AdmixerBlockEntity.SLOT_INPUT_COUNT; i++)
            {
                int j = i - AdmixerBlockEntity.SLOT_INPUT_START;
                OnlyMateriaInputSlot slot = new OnlyMateriaInputSlot(handler, i, 26 + 18 * (j % 2), 3 + 18 * (j / 2));
                this.addSlot(slot);
                inputSlots[j] = slot;
            }

            //Output item slots
            for(int i=AdmixerBlockEntity.SLOT_OUTPUT_START; i<AdmixerBlockEntity.SLOT_OUTPUT_START + AdmixerBlockEntity.SLOT_OUTPUT_COUNT; i++)
            {
                int x = (i - AdmixerBlockEntity.SLOT_OUTPUT_START) % 3;
                int y = (i - AdmixerBlockEntity.SLOT_OUTPUT_START) / 3;

                this.addSlot(new BottleConsumingResultSlot(handler, i, 116 + (x) * 18, 21 + (y) * 18, AdmixerBlockEntity.SLOT_BOTTLES));
            }

            setInputSlotFilters(blockEntity.getCurrentRecipe());
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.ADMIXER.get());
    }

    public void setInputSlotFilters(FixationSeparationRecipe newRecipe) {
        if(newRecipe != null) {
            int slotSet = 0;
            for (ItemStack stack : newRecipe.getComponentMateria()) {
                inputSlots[(slotSet * 2)].setSlotFilter((MateriaItem) stack.getItem());
                inputSlots[(slotSet * 2) + 1].setSlotFilter((MateriaItem) stack.getItem());
                slotSet++;
            }
        }
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
            moveItemStackTo(targetStackCopy, SLOT_INPUT_BEGIN, SLOT_INPUT_BEGIN + AdmixerBlockEntity.SLOT_INPUT_COUNT, false);
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
        if(pIndex >= SLOT_INPUT_BEGIN && pIndex < SLOT_INPUT_BEGIN + AdmixerBlockEntity.SLOT_INPUT_COUNT) {
            moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If output slots
        if(pIndex >= SLOT_OUTPUT_BEGIN && pIndex < SLOT_OUTPUT_BEGIN + AdmixerBlockEntity.SLOT_OUTPUT_COUNT) {
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
