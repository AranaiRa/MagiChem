package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.joml.Vector2i;

public class MirrorLabyrinthMenu extends AbstractContainerMenu {

    public final MirrorLabyrinthBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public MirrorLabyrinthMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(DistilleryBlockEntity.DATA_COUNT));
    }

    public MirrorLabyrinthMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.MIRROR_LABYRINTH_MENU.get(), id);
        checkContainerSize(inv, 4);
        blockEntity = (MirrorLabyrinthBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

            //Input slots
            this.addSlot(new SlotItemHandler(handler, MirrorLabyrinthBlockEntity.SLOT_INPUT, 157, 95));
            this.addSlot(new SlotItemHandler(handler, MirrorLabyrinthBlockEntity.SLOT_INPUT_RESULT, 182, 95));

            //Extraction slots
            this.addSlot(new SlotItemHandler(handler, MirrorLabyrinthBlockEntity.SLOT_EXTRACT, 182, 153));
            this.addSlot(new SlotItemHandler(handler, MirrorLabyrinthBlockEntity.SLOT_EXTRACT_RESULT, 157, 153));

        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.MIRROR_LABYRINTH.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, -22 + l*18, 95 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, -22 + i*18, 153)));
        }
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;

    Pair<Item, Integer>[] DIRSPEC = null;
    Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slot
                    SLOT_INVENTORY_COUNT + MirrorLabyrinthBlockEntity.SLOT_INPUT,
                    SLOT_INVENTORY_COUNT + MirrorLabyrinthBlockEntity.SLOT_INPUT + 1),
            new Vector2i( //Extraction slot
                    SLOT_INVENTORY_COUNT + MirrorLabyrinthBlockEntity.SLOT_EXTRACT,
                    SLOT_INVENTORY_COUNT + MirrorLabyrinthBlockEntity.SLOT_EXTRACT + 1),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slot
                    SLOT_INVENTORY_COUNT + MirrorLabyrinthBlockEntity.SLOT_INPUT_RESULT,
                    SLOT_INVENTORY_COUNT + MirrorLabyrinthBlockEntity.SLOT_INPUT_RESULT + 1),
            new Vector2i( //Extraction slot
                    SLOT_INVENTORY_COUNT + MirrorLabyrinthBlockEntity.SLOT_EXTRACT_RESULT,
                    SLOT_INVENTORY_COUNT + MirrorLabyrinthBlockEntity.SLOT_EXTRACT_RESULT + 1),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    Pair<Integer, Vector2i> SPEC_CONTAINER = null;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY, SPEC_CONTAINER);

        slots.get(pIndex).set(result);

        return ItemStack.EMPTY;
    }

    public int getProgress() {
        return data.get(DistilleryBlockEntity.DATA_PROGRESS);
    }

    public int getGrime() {
        return data.get(DistilleryBlockEntity.DATA_GRIME);
    }

    public int getHeat() {
        return data.get(DistilleryBlockEntity.DATA_REMAINING_HEAT);
    }

    public int getHeatDuration() {
        return data.get(DistilleryBlockEntity.DATA_HEAT_DURATION);
    }

    public int getEfficiencyMod() {
        return data.get(DistilleryBlockEntity.DATA_EFFICIENCY_MOD);
    }

    public int getOperationTimeMod() {
        return data.get(DistilleryBlockEntity.DATA_OPERATION_TIME_MOD);
    }

    public int getBatchSize() {
        return data.get(DistilleryBlockEntity.DATA_BATCH_SIZE);
    }
}
