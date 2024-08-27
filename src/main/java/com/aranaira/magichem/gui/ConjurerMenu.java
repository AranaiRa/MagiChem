package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.ConjurerBlockEntity;
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
            this.addSlot(new SlotItemHandler(handler, SLOT_CATALYST, 44, 22));

            this.addSlot(new SlotItemHandler(handler, SLOT_OUTPUT, 116, 22));

            this.addSlot(new SlotItemHandler(handler, SLOT_MATERIA_INPUT, 152, 17));

            this.addSlot(new SlotItemHandler(handler, SLOT_BOTTLES, 152, 43));
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

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistry.CONJURER.get());
    }
}
