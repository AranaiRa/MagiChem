package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.container.BottleConsumingResultSlot;
import com.aranaira.magichem.block.entity.container.NoMateriaInputSlot;
import com.aranaira.magichem.block.entity.container.OnlyAdmixtureInputSlot;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.recipe.AlchemicalInfusionRecipe;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class AlchemicalNexusMenu extends AbstractContainerMenu {
    public final AlchemicalNexusBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public AlchemicalNexusMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(AlchemicalNexusBlockEntity.DATA_COUNT));
    }

    public AlchemicalNexusMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.ALCHEMICAL_NEXUS_MENU.get(), id);
        checkContainerSize(inv, 14);
        blockEntity = (AlchemicalNexusBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            //Mark slot
            this.addSlot(new SlotItemHandler(handler, AlchemicalNexusBlockEntity.SLOT_MARKS, 134, -6));

            //Processing slot
            this.addSlot(new SlotItemHandler(handler, AlchemicalNexusBlockEntity.SLOT_PROCESSING, 80, -5));

            //Input item slots
            for(int i = AlchemicalNexusBlockEntity.SLOT_INPUT_START; i< AlchemicalNexusBlockEntity.SLOT_INPUT_START + AlchemicalNexusBlockEntity.SLOT_INPUT_COUNT; i++)
            {
                this.addSlot(new SlotItemHandler(handler, i, 22, -5 + (i - AlchemicalNexusBlockEntity.SLOT_INPUT_START) * 18));
            }

            //Output item slots
            for(int i = AlchemicalNexusBlockEntity.SLOT_OUTPUT_START; i< AlchemicalNexusBlockEntity.SLOT_OUTPUT_START + CentrifugeBlockEntity.SLOT_OUTPUT_COUNT; i++)
            {
                int x = (i - AlchemicalNexusBlockEntity.SLOT_OUTPUT_START) % 3;
                int y = (i - AlchemicalNexusBlockEntity.SLOT_OUTPUT_START) / 3;

                this.addSlot(new SlotItemHandler(handler, i, 116 + (x) * 18, 31 + (y) * 18));
            }
        });
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistry.ALCHEMICAL_NEXUS.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 97 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 155 )));
        }
    }

    public int getSlurryInTank() {
        return blockEntity.getFluidInTank(0).getAmount();
    }

    public AlchemicalInfusionRecipe getCurrentRecipe() {
        return blockEntity.getCurrentRecipe();
    }

    public InfusionStage getStage(int id) {
        return blockEntity.getCurrentRecipe().getStages(false).get(id);
    }

    public int getCurrentStageID() {
        return data.get(AlchemicalNexusBlockEntity.DATA_CRAFTING_STAGE);
    }

    public NonNullList<InfusionStage> getAllStages() {
        return blockEntity.getCurrentRecipe().getStages(false);
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;

    private static final Pair<Item, Integer>[] DIRSPEC = new Pair[]{};
    private static final Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT + AlchemicalNexusBlockEntity.SLOT_INPUT_START,
                    SLOT_INVENTORY_COUNT + AlchemicalNexusBlockEntity.SLOT_INPUT_START + AlchemicalNexusBlockEntity.SLOT_INPUT_COUNT),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Pair<Integer, Vector2i> SPEC_CONTAINER = null;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY, SPEC_CONTAINER);

        slots.get(pIndex).set(result);

        return ItemStack.EMPTY;
    }
}
