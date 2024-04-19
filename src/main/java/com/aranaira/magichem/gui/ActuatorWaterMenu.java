package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.networking.ActuatorSyncPowerLevelC2SPacket;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.registry.PacketRegistry;
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
import org.joml.Vector2i;

public class ActuatorWaterMenu extends AbstractContainerMenu {

    public final ActuatorWaterBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public ActuatorWaterMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(ActuatorWaterBlockEntity.DATA_COUNT));
    }

    public ActuatorWaterMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.ACTUATOR_WATER_MENU.get(), id);
        blockEntity = (ActuatorWaterBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.ACTUATOR_WATER.get());
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

    public int getPowerLevel() {
        return data.get(ActuatorWaterBlockEntity.DATA_POWER_LEVEL);
    }

    public int getFlags() {
        return data.get(ActuatorWaterBlockEntity.DATA_FLAGS);
    }

    public void incrementPowerLevel() {
        int previous = getPowerLevel();
        int current = Math.min(13, getPowerLevel() + 1);
        if(previous != current) {
            PacketRegistry.sendToServer(new ActuatorSyncPowerLevelC2SPacket(
                    blockEntity.getBlockPos(), true
            ));
        }
    }

    public void decrementPowerLevel() {
        int previous = getPowerLevel();
        int current = Math.max(1, getPowerLevel() - 1);
        if(previous != current) {
            PacketRegistry.sendToServer(new ActuatorSyncPowerLevelC2SPacket(
                    blockEntity.getBlockPos(), false
            ));
        }
    }

    public int getRemainingEldrinTime() {
        return data.get(ActuatorWaterBlockEntity.DATA_REMAINING_ELDRIN_TIME);
    }

    public int getWaterInTank() {
        return data.get(ActuatorWaterBlockEntity.DATA_WATER);
    }

    public int getSteamInTank() { return data.get(ActuatorWaterBlockEntity.DATA_STEAM); }

    private static final int
            SLOT_INVENTORY_BEGIN = 0,
            SLOT_INVENTORY_COUNT = 36;
    Pair<Item, Integer>[] DIRSPEC = new Pair[]{};
    Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackPriorityHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY);

        slots.get(pIndex).set(result);

        return getSlot(pIndex).getItem();
    }
}
