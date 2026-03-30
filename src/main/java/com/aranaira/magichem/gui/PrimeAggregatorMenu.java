package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.PrimeAggregatorBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.util.BypassedItemHandler;
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
import org.joml.Vector2i;

public class PrimeAggregatorMenu extends AbstractContainerMenu {

    public final PrimeAggregatorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public PrimeAggregatorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(0));
    }

    public PrimeAggregatorMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.PRIME_AGGREGATOR_MENU.get(), id);
        checkContainerSize(inv, PrimeAggregatorBlockEntity.SLOT_COUNT);
        blockEntity = (PrimeAggregatorBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            BypassedItemHandler bypassed = new BypassedItemHandler.Extract(handler, PrimeAggregatorBlockEntity.SLOT_PROGRESS_HOLDER);
            this.addSlot(new SlotItemHandler(handler, PrimeAggregatorBlockEntity.SLOT_ITEM_INPUT, -6, -28));
            this.addSlot(new SlotItemHandler(handler, PrimeAggregatorBlockEntity.SLOT_MATERIA_INPUT, 152, -28));
            this.addSlot(new SlotItemHandler(handler, PrimeAggregatorBlockEntity.SLOT_BOTTLES_OUTPUT, 184, -28));
            this.addSlot(new SlotItemHandler(bypassed, PrimeAggregatorBlockEntity.SLOT_PROGRESS_HOLDER, 80, -7));

            //Output item slots
            for(int i = PrimeAggregatorBlockEntity.SLOT_OUTPUT_START; i< PrimeAggregatorBlockEntity.SLOT_OUTPUT_START + PrimeAggregatorBlockEntity.SLOT_OUTPUT_COUNT; i++)
            {
                int y = i - PrimeAggregatorBlockEntity.SLOT_OUTPUT_START;

                this.addSlot(new SlotItemHandler(handler, i, 116, 10 + y * 18));
            }
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.PRIME_AGGREGATOR.get());
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

    Pair<Item, Integer>[] DIRSPEC = null;
    Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT,
                    SLOT_INVENTORY_COUNT + PrimeAggregatorBlockEntity.SLOT_INPUT_COUNT),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    Pair<Integer, Vector2i> SPEC_CONTAINER = new Pair<>(SLOT_INVENTORY_COUNT + PrimeAggregatorBlockEntity.SLOT_BOTTLES_OUTPUT, new Vector2i(
            SLOT_INVENTORY_COUNT + PrimeAggregatorBlockEntity.SLOT_OUTPUT_START,
            SLOT_INVENTORY_COUNT + PrimeAggregatorBlockEntity.SLOT_OUTPUT_START + PrimeAggregatorBlockEntity.SLOT_OUTPUT_COUNT
    ));

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY, SPEC_CONTAINER);

        slots.get(pIndex).set(result);

        return ItemStack.EMPTY;
    }
}
