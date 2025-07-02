package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.EldrinOrreryBlockEntity;
import com.aranaira.magichem.block.entity.container.BottleConsumingResultSlot;
import com.aranaira.magichem.block.entity.container.BottleStockSlot;
import com.aranaira.magichem.block.entity.container.NoMateriaInputSlot;
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
import org.joml.Vector2i;

public class EldrinOrreryMenu extends AbstractContainerMenu {

    public final EldrinOrreryBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public EldrinOrreryMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(EldrinOrreryBlockEntity.DATA_COUNT));
    }

    public EldrinOrreryMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.ELDRIN_ORRERY_MENU.get(), id);
        checkContainerSize(inv, EldrinOrreryBlockEntity.SLOT_COUNT);
        blockEntity = (EldrinOrreryBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_SOLAR_INPUT, -10, 43));
            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_LUNAR_INPUT, 8, 43));
            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_SIDEREAL_INPUT, 26, 43));

            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_SOLAR_OUTPUT, -10, 69));
            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_LUNAR_OUTPUT, 8, 69));
            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_SIDEREAL_OUTPUT, 26, 69));

            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_FIRMAMENT_INPUT, 152, 43));
            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_REALM_INPUT, 170, 43));

            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_FIRMAMENT_OUTPUT, 152, 69));
            this.addSlot(new SlotItemHandler(handler, EldrinOrreryBlockEntity.SLOT_REALM_OUTPUT, 170, 69));

        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.ELDRIN_ORRERY.get());
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
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 163)));
        }
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;

    Pair<Item, Integer>[] DIRSPEC = null;
    Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT + EldrinOrreryBlockEntity.SLOT_INPUT_START,
                    SLOT_INVENTORY_COUNT + EldrinOrreryBlockEntity.SLOT_INPUT_START + EldrinOrreryBlockEntity.SLOT_INPUT_COUNT),
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

        return ItemStack.EMPTY;
    }
}
