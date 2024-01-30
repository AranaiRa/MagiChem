package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.container.BottleStockSlot;
import com.aranaira.magichem.block.entity.container.OnlyMateriaInputSlot;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class CircleFabricationMenu extends AbstractContainerMenu {

    public final CircleFabricationBlockEntity blockEntity;
    private final Level level;
    public OnlyMateriaInputSlot[] inputSlots = new OnlyMateriaInputSlot[CircleFabricationBlockEntity.SLOT_INPUT_COUNT];
    private DataSlot dataHasSufficientPower;

    public CircleFabricationMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, ((CircleFabricationBlockEntity)inv.player.level.getBlockEntity(extraData.readBlockPos())).readFrom(extraData));
    }

    public CircleFabricationMenu(int id, Inventory inv, BlockEntity entity) {
        super(MenuRegistry.CIRCLE_FABRICATION_MENU.get(), id);
        checkContainerSize(inv, CircleFabricationBlockEntity.SLOT_COUNT);
        blockEntity = (CircleFabricationBlockEntity) entity;

        this.level = inv.player.level;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new BottleStockSlot(handler, CircleFabricationBlockEntity.SLOT_BOTTLES, 56, 67, true));

            for(int i=CircleFabricationBlockEntity.SLOT_INPUT_START;
            i < CircleFabricationBlockEntity.SLOT_INPUT_START + CircleFabricationBlockEntity.SLOT_INPUT_COUNT; i++) {
                int shiftedSlot = i - CircleFabricationBlockEntity.SLOT_INPUT_START;
                OnlyMateriaInputSlot slot = new OnlyMateriaInputSlot(handler, i, 8 + (18 * (shiftedSlot % 2)), -5 + (18 * (shiftedSlot / 2)));
                inputSlots[shiftedSlot] = slot;
                this.addSlot(slot);
            }

            for(int i=CircleFabricationBlockEntity.SLOT_OUTPUT_START;
                i < CircleFabricationBlockEntity.SLOT_OUTPUT_START + CircleFabricationBlockEntity.SLOT_OUTPUT_COUNT; i++) {
                int shiftedSlot = i - CircleFabricationBlockEntity.SLOT_OUTPUT_START;
                this.addSlot(new OnlyMateriaInputSlot(handler, i, 134 + (18 * (shiftedSlot % 2)), -5 + (18 * (shiftedSlot / 2))));
            }

            setInputSlotFilters(blockEntity.getCurrentRecipe());
        });

        this.dataHasSufficientPower = this.addDataSlot(DataSlot.forContainer(blockEntity, CircleFabricationBlockEntity.DATA_HAS_SUFFICIENT_POWER));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.CIRCLE_FABRICATION.get());
    }

    public void setInputSlotFilters(AlchemicalCompositionRecipe newRecipe) {
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
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 97 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 155)));
        }
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = CircleFabricationBlockEntity.SLOT_COUNT;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            //Check for bottles
            if (index >= TE_INVENTORY_FIRST_SLOT_INDEX + CircleFabricationBlockEntity.SLOT_OUTPUT_START &&
                    index < TE_INVENTORY_FIRST_SLOT_INDEX + CircleFabricationBlockEntity.SLOT_OUTPUT_START + CircleFabricationBlockEntity.SLOT_OUTPUT_COUNT) {
                int bottlesAvailable = slots.get(CircleFabricationBlockEntity.SLOT_BOTTLES).getItem().getCount();
            }

            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    // --------------------------------------
    // Data slot accessors
    // --------------------------------------

    public boolean getHasSufficientPower(){
        return this.dataHasSufficientPower.get() == 1;
    }
}
