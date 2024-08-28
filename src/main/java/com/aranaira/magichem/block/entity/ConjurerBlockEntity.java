package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.gui.ConjurerMenu;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.ConjurationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ConjurerBlockEntity extends BlockEntity implements MenuProvider {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public static final int
        SLOT_COUNT = 4,
        SLOT_CATALYST = 0, SLOT_OUTPUT = 1, SLOT_MATERIA_INPUT = 2, SLOT_BOTTLES = 3;
    private int
        progress, materiaAmount;
    private MateriaItem
        materiaType;
    private ConjurationRecipe recipe;
    public static HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(false, false);

    private ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_CATALYST) {
                ConjurationRecipe recipeQuery = ConjurationRecipe.getConjurationRecipe(level, stack);
                return recipeQuery != null;
            } else if(slot == SLOT_MATERIA_INPUT) {
                if(recipe == null)
                    return false;

                if(stack.getItem() instanceof MateriaItem mi) {
                    return recipe.getMateria() == mi;
                }

                return false;
            } else if(slot == SLOT_OUTPUT || slot == SLOT_BOTTLES) {
                return false;
            }

            return super.isItemValid(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            if(slot == SLOT_CATALYST)
                return 1;

            return super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    public ConjurerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CONJURER_BE.get(), pos, state);
    }

    public ConjurerBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ConjurerMenu(pContainerId, pPlayerInventory, this, null);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        if(this.materiaType == null)
            nbt.putString("materiaType", "null");
        else
            nbt.putString("materiaType", this.materiaType.getMateriaName());
        nbt.putInt("materiaAmount", this.materiaAmount);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        String materiaQuery = nbt.getString("materiaType");
        materiaType = materiaMap.get(materiaQuery);
        materiaAmount = nbt.getInt("materiaAmount");

        recipe = ConjurationRecipe.getConjurationRecipe(level, itemHandler.getStackInSlot(SLOT_CATALYST));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        if(this.materiaType == null)
            nbt.putString("materiaType", "null");
        else
            nbt.putString("materiaType", this.materiaType.getMateriaName());
        nbt.putInt("materiaAmount", this.materiaAmount);

        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public int getMateriaAmount() {
        return materiaAmount;
    }

    public MateriaItem getMateriaType() {
        return materiaType;
    }

    public ConjurationRecipe getRecipe() {
        return recipe;
    }

    public int getScaledProgress() {
        if(recipe == null)
            return 0;

        boolean isPassiveMode = true;
        if(materiaAmount > 0 && materiaType == recipe.getMateria())
            isPassiveMode = false;

        if(isPassiveMode)
            return 28 * progress / recipe.getPassiveData(false).getSecond();
        else
            return 28 * progress / recipe.getSuppliedData(false).getSecond();
    }

    public int getScaledMateria() {
        return (16 * Math.min(Config.conjurerMateriaCapacity, materiaAmount)) / Config.conjurerMateriaCapacity;
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, ConjurerBlockEntity entity) {
        ItemStack catalystStack = entity.itemHandler.getStackInSlot(SLOT_CATALYST);
        boolean syncThisTick = false;
        if(entity.recipe == null) {
            if(!catalystStack.isEmpty()) {
                ConjurationRecipe newRecipe = ConjurationRecipe.getConjurationRecipe(level, catalystStack);

                if (newRecipe != null) {
                    if (entity.materiaType != newRecipe.getMateria()) {
                        entity.materiaType = null;
                        entity.materiaAmount = 0;
                    }

                    entity.recipe = newRecipe;
                    syncThisTick = true;
                }
            }
        } else if(catalystStack.getItem() != entity.recipe.getCatalyst()) {
            entity.recipe = null;
            syncThisTick = true;
        } else {
            //Materia insertion
            if(!level.isClientSide()){
                ItemStack insert = entity.itemHandler.getStackInSlot(SLOT_MATERIA_INPUT);
                ItemStack bottles = entity.itemHandler.getStackInSlot(SLOT_BOTTLES);

                if(insert.getItem() == entity.recipe.getMateria()) {
                    boolean allowInsertion = false;
                    if(entity.materiaType == null) allowInsertion = true;
                    else if(entity.materiaType == insert.getItem()) allowInsertion = true;

                    if(allowInsertion) {
                        if(bottles.getCount() < entity.itemHandler.getSlotLimit(SLOT_BOTTLES)) {
                            //Allow overfilling the gauge by one item for GUI aesthetic reasons
                            if(entity.materiaAmount < Config.conjurerMateriaCapacity) {
                                entity.materiaAmount += Config.conjurerPointsPerDram;
                                entity.materiaType = (MateriaItem)insert.getItem();

                                insert.shrink(1);
                                if(bottles.isEmpty()) {
                                    bottles = new ItemStack(Items.GLASS_BOTTLE, 1);
                                } else {
                                    bottles.grow(1);
                                }
                                entity.itemHandler.setStackInSlot(SLOT_BOTTLES, bottles);
                                syncThisTick = true;
                            }
                        }
                    }
                }
            }

            //Crafting logic
            {
                boolean isPassiveMode = true;
                if (entity.materiaAmount > 0 && entity.materiaType == entity.recipe.getMateria())
                    isPassiveMode = false;

                if (canCraftItem(entity, isPassiveMode)) {
                    entity.progress++;

                    if (isPassiveMode) {
                        final Pair<ItemStack, Integer> data = entity.recipe.getPassiveData(false);
                        if (entity.progress >= data.getSecond()) {
                            ItemStack outputStack = entity.itemHandler.getStackInSlot(SLOT_OUTPUT);
                            if (outputStack.isEmpty()) {
                                outputStack = data.getFirst().copy();
                            } else {
                                outputStack.grow(data.getFirst().getCount());
                            }
                            entity.itemHandler.setStackInSlot(SLOT_OUTPUT, outputStack);
                            entity.progress = 0;
                            entity.syncAndSave();
                        }
                    } else {
                        final Triplet<ItemStack, Integer, Integer> data = entity.recipe.getSuppliedData(false);
                        if (entity.progress >= data.getSecond()) {
                            ItemStack outputStack = entity.itemHandler.getStackInSlot(SLOT_OUTPUT);
                            if (outputStack.isEmpty()) {
                                outputStack = data.getFirst().copy();
                            } else {
                                outputStack.grow(data.getFirst().getCount());
                            }
                            entity.itemHandler.setStackInSlot(SLOT_OUTPUT, outputStack);
                            entity.progress = 0;
                            entity.materiaAmount -= data.getThird();
                            if (entity.materiaAmount <= 0)
                                entity.materiaType = null;
                            entity.syncAndSave();
                        }
                    }
                }
            }
        }

        if(syncThisTick) entity.syncAndSave();
    }

    private static boolean canCraftItem(ConjurerBlockEntity entity, boolean isPassiveMode) {
        if(entity.itemHandler.getStackInSlot(SLOT_CATALYST).isEmpty())
            return false;

        ItemStack outputStack = entity.itemHandler.getStackInSlot(SLOT_OUTPUT);
        int slotLimit = entity.itemHandler.getSlotLimit(SLOT_OUTPUT);

        if(outputStack.isEmpty())
            return true;
        else if(isPassiveMode) {
            final Pair<ItemStack, Integer> data = entity.recipe.getPassiveData(false);
            if(outputStack.getItem() != data.getFirst().getItem())
                return false;
            return slotLimit >= outputStack.getCount() + data.getFirst().getCount();
        }
        else {
            final Triplet<ItemStack, Integer, Integer> data = entity.recipe.getSuppliedData(false);
            if(outputStack.getItem() != data.getFirst().getItem())
                return false;
            return slotLimit >= outputStack.getCount() + data.getFirst().getCount();
        }
    }
}
