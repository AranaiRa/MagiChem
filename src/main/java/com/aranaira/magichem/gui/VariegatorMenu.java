package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import com.aranaira.magichem.block.entity.container.BottleConsumingResultSlot;
import com.aranaira.magichem.block.entity.container.BottleStockSlot;
import com.aranaira.magichem.block.entity.container.NoMateriaInputSlot;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

import static com.aranaira.magichem.block.entity.VariegatorBlockEntity.*;

public class VariegatorMenu extends AbstractContainerMenu {

    public final VariegatorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public VariegatorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(VariegatorBlockEntity.DATA_COUNT));
    }

    public VariegatorMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.VARIEGATOR_MENU.get(), id);
        checkContainerSize(inv, VariegatorBlockEntity.SLOT_COUNT);
        blockEntity = (VariegatorBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

            //Dye Input slot
            this.addSlot(new SlotItemHandler(handler, SLOT_DYE_INPUT, 157, 15));

            //Bottle slot
            this.addSlot(new BottleStockSlot(handler, SLOT_DYE_BOTTLES, 157, 41, false));

            //Input item slots
            for(int i = SLOT_INPUT_START; i<SLOT_INPUT_START + SLOT_INPUT_COUNT; i++)
            {
                this.addSlot(new NoMateriaInputSlot(handler, i, 44, 11 + (i - SLOT_INPUT_START) * 18));
            }

            //Output item slots
            for(int i = SLOT_OUTPUT_START; i< SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT; i++)
            {
                int y = (i - SLOT_OUTPUT_START) % 3;

                this.addSlot(new SlotItemHandler(handler, i, 116, 11 + (y) * 18));
            }
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistry.VARIEGATOR.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 82 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 140)));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }
}
