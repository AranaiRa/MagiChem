package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.CovetousCofferBlockEntity;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.item.ItemEntity;
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

import static com.aranaira.magichem.block.entity.CovetousCofferBlockEntity.*;

public class CovetousCofferMenu extends AbstractContainerMenu {
    public final CovetousCofferBlockEntity blockEntity;
    private final Level level;

    public CovetousCofferMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
    }

    public CovetousCofferMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.COVETOUS_COFFER_MENU.get(), id);
        blockEntity = (CovetousCofferBlockEntity) entity;
        level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, SLOT_INPUT, 80, -2));

            this.addSlot(new SlotItemHandler(handler, SLOT_OUTPUT_ITEM_1, 26, 74));
            this.addSlot(new SlotItemHandler(handler, SLOT_OUTPUT_ITEM_2, 62, 74));
            this.addSlot(new SlotItemHandler(handler, SLOT_OUTPUT_ITEM_3, 98, 74));
            this.addSlot(new SlotItemHandler(handler, SLOT_OUTPUT_ITEM_4, 134, 74));
        });
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 99 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 157)));
        }
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;

    private static final Pair<Item, Integer>[] DIRSPEC = null;
    private static final Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT,
                    SLOT_INVENTORY_COUNT + 1),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Pair<Integer, Vector2i> SPEC_CONTAINER = null;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY, SPEC_CONTAINER, true);

        if(!result.isEmpty()) {
            ItemEntity overflow = new ItemEntity(pPlayer.level(), pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), result);
            pPlayer.level().addFreshEntity(overflow);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistry.COVETOUS_COFFER.get());
    }
}
