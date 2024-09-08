package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.container.BottleStockSlot;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.items.ItemInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.joml.Vector2i;

import static com.aranaira.magichem.block.entity.CircleFabricationBlockEntity.*;

public class CircleFabricationMenu extends AbstractContainerMenu {

    public final CircleFabricationBlockEntity blockEntity;
    private final Level level;
    public SlotItemHandler[] inputSlots = new SlotItemHandler[SLOT_INPUT_COUNT];

    public CircleFabricationMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, ((CircleFabricationBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos())).readFrom(extraData));
    }

    public CircleFabricationMenu(int id, Inventory inv, BlockEntity entity) {
        super(MenuRegistry.CIRCLE_FABRICATION_MENU.get(), id);
        checkContainerSize(inv, CircleFabricationBlockEntity.SLOT_COUNT);
        blockEntity = (CircleFabricationBlockEntity) entity;

        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new BottleStockSlot(handler, CircleFabricationBlockEntity.SLOT_BOTTLES, 82, -5, true));

            for(int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
                int shiftedSlot = i - SLOT_INPUT_START;
                SlotItemHandler slot = new SlotItemHandler(handler, i, 28 + (18 * (shiftedSlot % 2)), -5 + (18 * (shiftedSlot / 2)));
                inputSlots[shiftedSlot] = slot;
                this.addSlot(slot);
            }

            for(int i=CircleFabricationBlockEntity.SLOT_OUTPUT_START;
                i < CircleFabricationBlockEntity.SLOT_OUTPUT_START + CircleFabricationBlockEntity.SLOT_OUTPUT_COUNT; i++) {
                int shiftedSlot = i - CircleFabricationBlockEntity.SLOT_OUTPUT_START;
                this.addSlot(new SlotItemHandler(handler, i, 118 + (18 * (shiftedSlot % 2)), -5 + (18 * (shiftedSlot / 2))));
            }
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.CIRCLE_FABRICATION.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 10 + l*18, 95 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 10 + i*18, 153)));
        }
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;

    private static final Pair<Item, Integer>[] DIRSPEC = null;
    private static final Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT + SLOT_INPUT_START - 1,
                    SLOT_INVENTORY_COUNT + SLOT_INPUT_START + SLOT_INPUT_COUNT),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Pair<Integer, Vector2i> SPEC_CONTAINER = null;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY, SPEC_CONTAINER);

        slots.get(pIndex).set(result);

        return ItemStack.EMPTY;
    }
}
