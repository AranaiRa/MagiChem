package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.PrimeAggregatorMenu;
import com.aranaira.magichem.recipe.ExaltationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class PrimeAggregatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
            SLOT_COUNT = 7, SLOT_INPUT_COUNT = 2,
            SLOT_ITEM_INPUT = 0, SLOT_MATERIA_INPUT = 1, SLOT_BOTTLES_OUTPUT = 2, SLOT_PROGRESS_HOLDER = 3,
            SLOT_OUTPUT_START = 4, SLOT_OUTPUT_COUNT  = 3;
    public boolean clearRecipeAfterNextProcess = false;
    private boolean doDeferredRecipeCheck = false;
    private ExaltationRecipe currentRecipe = null;
    private ResourceLocation deferredRecipeQuery = null;

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler;

    public PrimeAggregatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.PRIME_AGGREGATOR_BE.get(), pPos, pBlockState);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_PROGRESS_HOLDER) return false;
                if(currentRecipe == null) return false;

                return stack.getItem() == currentRecipe.getItemType();
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PrimeAggregatorMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(0));
    }

    public ExaltationRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-2, 0, -2), getBlockPos().offset(2,4,2));
    }

    public void setRecipeByOutput(ItemStack pRecipeOutput) {
        ExaltationRecipe er = ExaltationRecipe.getExaltationRecipe(level, pRecipeOutput.getItem());

        if(er != null) {
            this.currentRecipe = er;
            this.syncAndSave();
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
//        linkPlugins();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putBoolean("clearRecipeAfterNextProcess", this.clearRecipeAfterNextProcess);

        if(currentRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.ITEMS.getKey(currentRecipe.getResultItem().getItem());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
        }

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        clearRecipeAfterNextProcess = nbt.getBoolean("clearRecipeAfterNextProcess");

        if(nbt.contains("recipe"))
            deferredRecipeQuery = new ResourceLocation(nbt.getString("recipe"));
        else
            deferredRecipeQuery = null;
        doDeferredRecipeCheck = true;

//        updateActuatorValues(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putBoolean("clearRecipeAfterNextProcess", this.clearRecipeAfterNextProcess);

        if(currentRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.ITEMS.getKey(currentRecipe.getResultItem().getItem());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
        }

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

    public Pair<Integer, Integer> getItems() {
        if(currentRecipe != null) return new Pair<>(0, currentRecipe.getItemsRequired());
        return new Pair<>(0, -1);
    }

    public Pair<Integer, Integer> getMateria() {
        if(currentRecipe != null) return new Pair<>(0, currentRecipe.getMateriaRequired());
        return new Pair<>(0, -1);
    }

    public Pair<Integer, Integer> getSlurry() {
        if(currentRecipe != null) return new Pair<>(0, currentRecipe.getSlurryRequired());
        return new Pair<>(0, -1);
    }

    public Pair<Integer, Integer> getEldrin() {
        if(currentRecipe != null) {
            int types = currentRecipe.getEldrinTypeIndex() == 9 ? 6 : currentRecipe.getEldrinTypeIndex() >= 7 ? 3 : 1;
            return new Pair<>(0, currentRecipe.getEldrinRequired() * types);
        }
        return new Pair<>(0, -1);
    }

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, PrimeAggregatorBlockEntity pEntity) {
        if(pEntity.doDeferredRecipeCheck) {
            boolean changed = false;
            Item itemQuery = ForgeRegistries.ITEMS.getValue(pEntity.deferredRecipeQuery);
            ExaltationRecipe recipeQuery = ExaltationRecipe.getExaltationRecipe(pLevel, itemQuery);

            if(recipeQuery != null) {
                changed = pEntity.currentRecipe != recipeQuery;
                pEntity.currentRecipe = recipeQuery;
            } else {
                changed = pEntity.currentRecipe != null;
                pEntity.currentRecipe = null;
            }
            pEntity.doDeferredRecipeCheck = false;
            if(changed)
                pEntity.syncAndSave();
        }
    }
}
