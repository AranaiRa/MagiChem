package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.ConjurerBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.ConjurationRecipe;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import static com.aranaira.magichem.block.entity.ConjurerBlockEntity.*;

public class ConjurerMenu extends AbstractContainerMenu {
    public final ConjurerBlockEntity blockEntity;
    private final Level level;

    public ConjurerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(0));
    }

    public ConjurerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.CONJURER_MENU.get(), id);
        blockEntity = (ConjurerBlockEntity) entity;
        level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if(handler instanceof CombinedInvWrapper ciw) {
                this.addSlot(new SlotItemHandler(handler, SLOT_INSERTION_CATALYST, 44, 22));
                this.addSlot(new SlotItemHandler(handler, SLOT_INSERTION_MATERIA, 152, 17));
                this.addSlot(new SlotItemHandler(handler, SLOT_INSERTION_COUNT + SLOT_EXTRACTION_OUTPUT, 116, 22));
                this.addSlot(new SlotItemHandler(handler, SLOT_INSERTION_COUNT + SLOT_EXTRACTION_BOTTLES, 152, 43));
            }
        });
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 79 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 137)));
        }
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;

    private static final Pair<Item, Integer>[] DIRSPEC = null;
    private static final Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT + SLOT_INSERTION_CATALYST,
                    SLOT_INVENTORY_COUNT + SLOT_INSERTION_CATALYST + 1),
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT + SLOT_INSERTION_MATERIA,
                    SLOT_INVENTORY_COUNT + SLOT_INSERTION_MATERIA + 1),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT + SLOT_EXTRACTION_BOTTLES,
                    SLOT_INVENTORY_COUNT + SLOT_EXTRACTION_BOTTLES),
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT + SLOT_EXTRACTION_OUTPUT,
                    SLOT_INVENTORY_COUNT + SLOT_EXTRACTION_OUTPUT),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Pair<Integer, Vector2i> SPEC_CONTAINER = null;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY, SPEC_CONTAINER);

        slots.get(pIndex).set(result);

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistry.CONJURER.get());
    }
}
