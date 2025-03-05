package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.HashMap;

/**
 * Handles any block that acts as materia storage for a variable number of types (like the Mirror Labyrinth).
 * If you're instanceof-ing, use the superclass AbstractMateriaStorageMultiTypeBlockEntity instead of this or Dynamic.
 */
public abstract class AbstractMateriaStorageMultiTypeDynamicBlockEntity extends AbstractMateriaStorageMultiTypeBlockEntity {
    protected HashMap<MateriaItem, Integer> materiaStorage = new HashMap<>();

    public AbstractMateriaStorageMultiTypeDynamicBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getCurrentStock(MateriaItem pMateriaType) {
        return 0;
    }

    @Override
    public float getCurrentStockPercent(MateriaItem pMateriaType) {
        return 0;
    }

    @Override
    public boolean containsMateriaType(MateriaItem pMateriaType) {
        return false;
    }

    @Override
    public Collection<MateriaItem> getMateriaTypes() {
        return null;
    }

    @Override
    public void setContents(MateriaItem pMateriaType, int pCount) {

    }

    @Override
    public void setContents(int pSlot, MateriaItem pMateriaType, int pCount) {

    }

    @Override
    public int fill(MateriaItem pMateriaType, int pAmount, boolean pVoidExcess) {
        return 0;
    }

    @Override
    public int drain(MateriaItem pMateriaType, int pAmount, boolean pKeepOne) {
        return 0;
    }

    @Override
    public int getStorageLimit(MateriaItem pMateriaType) {
        return 0;
    }

    @Override
    public boolean isBelowTypeLimit() {
        return false;
    }

    @Override
    public abstract void load(CompoundTag nbt);

    @Override
    protected abstract void saveAdditional(CompoundTag nbt);

    @Override
    public abstract void handleUpdateTag(CompoundTag nbt);

    @Override
    public abstract CompoundTag getUpdateTag();

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return 0;
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return 0;
    }

    @Override
    public abstract Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType);
}
