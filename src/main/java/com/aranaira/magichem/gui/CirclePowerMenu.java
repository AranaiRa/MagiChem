package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.CirclePowerBlock;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import static com.aranaira.magichem.block.entity.CirclePowerBlockEntity.*;

public class CirclePowerMenu extends AbstractContainerMenu {

    public final CirclePowerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public CirclePowerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public CirclePowerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.CIRCLE_POWER_MENU.get(), id);
        checkContainerSize(inv, 4);
        blockEntity = (CirclePowerBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, SLOT_REAGENT_1, 26, 18) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    if(stack.getItem() == ItemRegistry.SILVER_DUST.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get())
                        return true;
                    else
                        return false;
                }
            });
            this.addSlot(new SlotItemHandler(handler, SLOT_REAGENT_2, 62, 18) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    if(stack.getItem() == ItemRegistry.FOCUSING_CATALYST.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get())
                        return true;
                    else
                        return false;
                }
            });
            this.addSlot(new SlotItemHandler(handler, SLOT_REAGENT_3, 98, 18) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    if(stack.getItem() == ItemRegistry.AMPLIFYING_PRISM.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get())
                        return true;
                    else
                        return false;
                }
            });
            this.addSlot(new SlotItemHandler(handler, SLOT_REAGENT_4, 134, 18) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    if(stack.getItem() == ItemRegistry.AUXILIARY_CIRCLE_ARRAY.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get())
                        return true;
                    else
                        return false;
                }
            });
            this.addSlot(new SlotItemHandler(handler, SLOT_RECHARGE, 188, 18));
            this.addSlot(new SlotItemHandler(handler, WASTE_REAGENT_1, 26, 57) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
            this.addSlot(new SlotItemHandler(handler, WASTE_REAGENT_2, 62, 57) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
            this.addSlot(new SlotItemHandler(handler, WASTE_REAGENT_3, 98, 57) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
            this.addSlot(new SlotItemHandler(handler, WASTE_REAGENT_4, 134, 57) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.CIRCLE_POWER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 85 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 143)));
        }
    }

    public int getScaledProgress(int tier) {
        int BAR_WIDTH = 22;
        int progress = data.get(tier-1);
        int max = getMaxProgressByTier(tier);

        if(max == 0 || max == -1)
            return -1;
        else
            return (max - progress + 1) * BAR_WIDTH / max;
    }

    public boolean isCrafting(int tier) {
        return data.get(tier-1) > 0;
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
    private static final int TE_INVENTORY_SLOT_COUNT = 4;  // must be the number of slots you have!

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
}
