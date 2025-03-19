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
        if(materiaStorage.containsKey(pMateriaType)) {
            return materiaStorage.get(pMateriaType);
        }

        return 0;
    }

    @Override
    public float getCurrentStockPercent(MateriaItem pMateriaType) {
        if(materiaStorage.containsKey(pMateriaType)) {
            return (float)materiaStorage.get(pMateriaType) / (float)getStorageLimit(pMateriaType);
        }

        return 0;
    }

    @Override
    public boolean containsMateriaType(MateriaItem pMateriaType) {
        return materiaStorage.containsKey(pMateriaType);
    }

    @Override
    public Collection<MateriaItem> getMateriaTypes() {
        return materiaStorage.keySet();
    }

    @Override
    public void setContents(MateriaItem pMateriaType, int pCount) {
        materiaStorage.put(pMateriaType, pCount);
    }

    @Override
    public void setContents(int pSlot, MateriaItem pMateriaType, int pCount) {
        setContents(pMateriaType, pCount);
    }

    @Override
    public int fill(MateriaItem pMateriaType, int pAmount, boolean pVoidExcess) {
        if(materiaStorage.containsKey(pMateriaType)) {
            int existing = materiaStorage.get(pMateriaType);
            int limitedInsertion = Math.min(pAmount, getStorageLimit(pMateriaType) - existing);
            int overflow = pVoidExcess ? 0 : Math.max(0, getStorageLimit(pMateriaType) - existing - limitedInsertion);

            materiaStorage.put(pMateriaType, existing + limitedInsertion);
            syncAndSave();
            return overflow;
        }
        else {
            int limitedInsertion = Math.min(pAmount, getStorageLimit(pMateriaType));
            int overflow = pVoidExcess ? 0 : Math.max(0, getStorageLimit(pMateriaType) - limitedInsertion);

            materiaStorage.put(pMateriaType, limitedInsertion);
            syncAndSave();
            return overflow;
        }
    }

    @Override
    public int drain(MateriaItem pMateriaType, int pAmount, boolean pKeepOne) {
        if(materiaStorage.containsKey(pMateriaType)) {
            int existing = materiaStorage.get(pMateriaType);
            if(pKeepOne && existing == 1) return 0;

            int drained = Math.min((pKeepOne ? 1 : 0), existing - Math.min(existing, pAmount));

            materiaStorage.put(pMateriaType, existing - drained);
            return drained;
        }
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
    public void load(CompoundTag nbt) {
        if(nbt.contains("materiaStorage")) {
            materiaStorage.clear();
            CompoundTag materiaStorageTag = nbt.getCompound("materiaStorage");
            for(String query : materiaStorageTag.getAllKeys()) {
                materiaStorage.put(materiaMap.get(query), materiaStorageTag.getInt(query));
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        CompoundTag materiaStorageTag = new CompoundTag();
        for(MateriaItem materiaQuery : materiaStorage.keySet()) {
            materiaStorageTag.putInt(materiaQuery.getMateriaName(), materiaStorage.get(materiaQuery));
        }
        nbt.put("materiaStorage", materiaStorageTag);
        super.saveAdditional(nbt);
    }

    @Override
    public abstract void handleUpdateTag(CompoundTag nbt);

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        CompoundTag materiaStorageTag = new CompoundTag();
        for(MateriaItem materiaQuery : materiaStorage.keySet()) {
            materiaStorageTag.putInt(materiaQuery.getMateriaName(), materiaStorage.get(materiaQuery));
        }
        nbt.put("materiaStorage", materiaStorageTag);
        return nbt;
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() instanceof MateriaItem mi) {
            return Math.max(0, getStorageLimit(mi) - materiaStorage.get(mi));
        }

        return 0;
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() instanceof MateriaItem mi) {
            return fill(mi, pStack.getCount(), true);
        }

        return 0;
    }

    @Override
    public abstract Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType);
}
