package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.ActuatorArcaneBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.networking.ActuatorSyncPowerLevelC2SPacket;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.registry.PacketRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
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

import static com.aranaira.magichem.block.entity.ActuatorAirBlockEntity.ESSENTIA_AIR;
import static com.aranaira.magichem.block.entity.ActuatorAirBlockEntity.SLOT_ESSENTIA_INSERTION;
import static com.aranaira.magichem.block.entity.ActuatorArcaneBlockEntity.*;
import static com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity.IDs.MAX_POWER_LEVEL;

public class ActuatorArcaneMenu extends AbstractContainerMenu {

    public final ActuatorArcaneBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private static final int
            SLOT_INPUT_X = 84, SLOT_INPUT_Y = 16,
            SLOT_OUTPUT_X = 84, SLOT_OUTPUT_Y = 39;

    public ActuatorArcaneMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(0));
    }

    public ActuatorArcaneMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.ACTUATOR_ARCANE_MENU.get(), id);
        blockEntity = (ActuatorArcaneBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, ActuatorArcaneBlockEntity.SLOT_INPUT, SLOT_INPUT_X, SLOT_INPUT_Y));
            this.addSlot(new SlotItemHandler(handler, ActuatorArcaneBlockEntity.SLOT_OUTPUT, SLOT_OUTPUT_X, SLOT_OUTPUT_Y));

            this.addSlot(new SlotItemHandler(handler, SLOT_ESSENTIA_INSERTION, 174, 15));

            this.addSlot(new SlotItemHandler(handler, SLOT_BOTTLES, 174, 41));
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.ACTUATOR_ARCANE.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 81 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 139 )));
        }
    }

    public void incrementPowerLevel() {
        int previous = blockEntity.getPowerLevel();
        int current = Math.min(getValue(MAX_POWER_LEVEL), blockEntity.getPowerLevel() + 1);
        if(previous != current) {
            PacketRegistry.sendToServer(new ActuatorSyncPowerLevelC2SPacket(
                    blockEntity.getBlockPos(), true, Affinity.ARCANE
            ));
        }
    }

    public void decrementPowerLevel() {
        int previous = blockEntity.getPowerLevel();
        int current = Math.max(1, blockEntity.getPowerLevel() - 1);
        if(previous != current) {
            PacketRegistry.sendToServer(new ActuatorSyncPowerLevelC2SPacket(
                    blockEntity.getBlockPos(), false, Affinity.ARCANE
            ));
        }
    }

    private static final int
            SLOT_INVENTORY_BEGIN = 0,
            SLOT_INVENTORY_COUNT = 36;
    Pair<Item, Integer>[] DIRSPEC = new Pair[]{
            new Pair<>(ESSENTIA_ARCANE, SLOT_INVENTORY_COUNT + SLOT_ESSENTIA_INSERTION)
    };
    Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_COUNT + ActuatorArcaneBlockEntity.SLOT_INPUT,
                         SLOT_INVENTORY_COUNT + ActuatorArcaneBlockEntity.SLOT_INPUT + ActuatorArcaneBlockEntity.SLOT_COUNT),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    Pair<Integer, Vector2i> SPEC_CONTAINER = null;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY, SPEC_CONTAINER);

        slots.get(pIndex).set(result);

        return getSlot(pIndex).getItem();
    }
}
