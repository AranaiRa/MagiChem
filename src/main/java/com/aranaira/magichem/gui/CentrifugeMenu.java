package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.block.entity.container.*;
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
import org.joml.Vector2i;

public class CentrifugeMenu extends AbstractContainerMenu {

    public final CentrifugeBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public CentrifugeMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(CentrifugeBlockEntity.DATA_COUNT));
    }

    public CentrifugeMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.CENTRIFUGE_MENU.get(), id);
        checkContainerSize(inv, CentrifugeBlockEntity.SLOT_COUNT);
        blockEntity = (CentrifugeBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

            //Bottle slots
            this.addSlot(new BottleStockSlot(handler, CentrifugeBlockEntity.SLOT_BOTTLES, 116, -3, false));
            this.addSlot(new BottleStockSlot(handler, CentrifugeBlockEntity.SLOT_BOTTLES_OUTPUT, 62, 11, true));

            //Input item slots
            for(int i = CentrifugeBlockEntity.SLOT_INPUT_START; i< CentrifugeBlockEntity.SLOT_INPUT_START + CentrifugeBlockEntity.SLOT_INPUT_COUNT; i++)
            {
                this.addSlot(new OnlyAdmixtureInputSlot(handler, i, 26, 30 + (i - CentrifugeBlockEntity.SLOT_INPUT_START) * 18));
            }

            //Output item slots
            for(int i = CentrifugeBlockEntity.SLOT_OUTPUT_START; i< CentrifugeBlockEntity.SLOT_OUTPUT_START + CentrifugeBlockEntity.SLOT_OUTPUT_COUNT; i++)
            {
                int x = (i - CentrifugeBlockEntity.SLOT_OUTPUT_START) % 3;
                int y = (i - CentrifugeBlockEntity.SLOT_OUTPUT_START) / 3;

                this.addSlot(new BottleConsumingResultSlot(handler, i, 98 + (x) * 18, 30 + (y) * 18, CentrifugeBlockEntity.SLOT_BOTTLES));
            }
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.CENTRIFUGE.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 96 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 154 )));
        }
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;

    private static final Pair<Item, Integer>[] DIRSPEC = new Pair[]{
            new Pair(Items.GLASS_BOTTLE, SLOT_INVENTORY_COUNT + CentrifugeBlockEntity.SLOT_BOTTLES)
    };
    private static final Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT + CentrifugeBlockEntity.SLOT_INPUT_START,
                    SLOT_INVENTORY_COUNT + CentrifugeBlockEntity.SLOT_INPUT_START + CentrifugeBlockEntity.SLOT_INPUT_COUNT),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Pair<Integer, Vector2i> SPEC_CONTAINER = new Pair<>(SLOT_INVENTORY_COUNT + CentrifugeBlockEntity.SLOT_BOTTLES_OUTPUT, new Vector2i(
            SLOT_INVENTORY_COUNT + CentrifugeBlockEntity.SLOT_OUTPUT_START,
            SLOT_INVENTORY_COUNT + CentrifugeBlockEntity.SLOT_OUTPUT_START + CentrifugeBlockEntity.SLOT_OUTPUT_COUNT
    ));

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY, SPEC_CONTAINER);

        slots.get(pIndex).set(result);

        return ItemStack.EMPTY;
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

    public int getBatchSize() {
        return data.get(CentrifugeBlockEntity.DATA_BATCH_SIZE);
    }
}
