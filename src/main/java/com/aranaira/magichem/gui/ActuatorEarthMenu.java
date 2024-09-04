package com.aranaira.magichem.gui;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.joml.Vector2i;

import static com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity.*;
import static com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity.*;

public class ActuatorEarthMenu extends AbstractContainerMenu {

    public final ActuatorEarthBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private static final int
            SLOT_SAND_X = 75, SLOT_SAND_Y = 13,
            SLOT_WASTE_X = 66, SLOT_WASTE_Y = 43,
            SLOT_RAREFIED_WASTE_X = 84, SLOT_RAREFIED_WASTE_Y = 43;

    public ActuatorEarthMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(DATA_COUNT));
    }

    public ActuatorEarthMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.ACTUATOR_EARTH_MENU.get(), id);
        blockEntity = (ActuatorEarthBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            //Sand slot
            this.addSlot(new SlotItemHandler(handler, SLOT_SAND, SLOT_SAND_X, SLOT_SAND_Y));
            this.addSlot(new SlotItemHandler(handler, SLOT_WASTE, SLOT_WASTE_X, SLOT_WASTE_Y));
            this.addSlot(new SlotItemHandler(handler, SLOT_RAREFIED_WASTE, SLOT_RAREFIED_WASTE_X, SLOT_RAREFIED_WASTE_Y));

            this.addSlot(new SlotItemHandler(handler, SLOT_ESSENTIA_INSERTION, 183, 15));

            this.addSlot(new SlotItemHandler(handler, SLOT_BOTTLES, 183, 41));
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.ACTUATOR_EARTH.get());
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
        int current = Math.min(getValue(IDs.MAX_POWER_LEVEL), blockEntity.getPowerLevel() + 1);
        if(previous != current) {
            PacketRegistry.sendToServer(new ActuatorSyncPowerLevelC2SPacket(
                    blockEntity.getBlockPos(), true, Affinity.EARTH
            ));
        }
    }

    public void decrementPowerLevel() {
        int previous = blockEntity.getPowerLevel();
        int current = Math.max(1, blockEntity.getPowerLevel() - 1);
        if(previous != current) {
            PacketRegistry.sendToServer(new ActuatorSyncPowerLevelC2SPacket(
                    blockEntity.getBlockPos(), false, Affinity.EARTH
            ));
        }
    }

    private static final int
            SLOT_INVENTORY_BEGIN = 0,
            SLOT_INVENTORY_COUNT = 36;
    Pair<Item, Integer>[] DIRSPEC = new Pair[]{
            new Pair<>(Items.SAND, SLOT_INVENTORY_COUNT + SLOT_SAND)
    };
    Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
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
