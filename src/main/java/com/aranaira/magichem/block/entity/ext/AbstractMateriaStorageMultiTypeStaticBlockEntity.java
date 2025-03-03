package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.item.MateriaItem;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Handles any block that acts as materia storage for a very specific number of types (like a Materia Jar Quad).
 * If you're instanceof-ing, use the superclass AbstractMateriaStorageMultiTypeBlockEntity instead of this or Dynamic.
 */
public abstract class AbstractMateriaStorageMultiTypeStaticBlockEntity extends AbstractMateriaStorageMultiTypeBlockEntity {
    protected Pair<MateriaItem, Integer>[] storedMateria;

    public AbstractMateriaStorageMultiTypeStaticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getCurrentStock(MateriaItem pMateriaType) {
        for(Pair<MateriaItem, Integer> pmi : storedMateria) {
            if(pmi != null && pmi.getFirst() == pMateriaType)
                return pmi.getSecond();
        }
        return 0;
    }

    @Override
    public float getCurrentStockPercent(MateriaItem pMateriaType) {
        for(Pair<MateriaItem, Integer> pmi : storedMateria) {
            if(pmi != null && pmi.getFirst() == pMateriaType)
                return (float)pmi.getSecond() / (float)getStorageLimit(pMateriaType);
        }
        return 0;
    }

    @Override
    public boolean containsMateriaType(MateriaItem pMateriaType) {
        for(Pair<MateriaItem, Integer> pmi : storedMateria) {
            if(pmi != null && pmi.getFirst() == pMateriaType)
                return true;
        }
        return false;
    }

    @Override
    public Collection<MateriaItem> getMateriaTypes() {
        Collection<MateriaItem> types = new ArrayList<>();
        for(Pair<MateriaItem, Integer> pmi : storedMateria) {
            if(pmi != null)
                types.add(pmi.getFirst());
        }
        return types;
    }

    @Override
    public void setContents(MateriaItem pMateriaType, int pCount) {
        for(int i=0; i<storedMateria.length; i++) {
            Pair<MateriaItem, Integer> pmi = storedMateria[i];
            if(pmi != null) {
                if(pmi.getFirst() == pMateriaType) {
                    storedMateria[i] = new Pair<>(pMateriaType, pCount);
                }
            } else {
                storedMateria[i] = new Pair<>(pMateriaType, pCount);
            }
        }
    }

    @Override
    public void setContents(int pSlot, MateriaItem pMateriaType, int pCount) {
        if(pSlot < storedMateria.length) {
            storedMateria[pSlot] = new Pair<>(pMateriaType, pCount);
        }
    }

    @Override
    public int fill(MateriaItem pMateriaType, int pAmount, boolean pVoidExcess) {
        int firstEmptyIndex = -1;
        for(int i=0; i<storedMateria.length; i++) {
            Pair<MateriaItem, Integer> pmi = storedMateria[i];
            if(pmi != null) {
                if(pmi.getFirst() == pMateriaType) {
                    int actual = pmi.getSecond() + pAmount;
                    int remainder = pVoidExcess ? 0 : Math.max(0, actual - getStorageLimit(pMateriaType));

                    storedMateria[i] = new Pair<>(pMateriaType, Math.min(getStorageLimit(pMateriaType), actual));
                    return remainder;
                }
            } else if(firstEmptyIndex == -1){
                firstEmptyIndex = i;
            }
        }

        if(firstEmptyIndex > -1) {
            int remainder = pVoidExcess ? 0 : Math.max(0, pAmount - getStorageLimit(pMateriaType));

            storedMateria[firstEmptyIndex] = new Pair<>(pMateriaType, Math.min(getStorageLimit(pMateriaType), pAmount));
            return remainder;
        }

        return 0;
    }

    @Override
    public int drain(MateriaItem pMateriaType, int pAmount, boolean pKeepOne) {
        for(int i=0; i<storedMateria.length; i++) {
            Pair<MateriaItem, Integer> pmi = storedMateria[i];
            if(pmi != null) {
                if(pmi.getFirst() == pMateriaType) {
                    int actual = Math.min(pKeepOne ? pmi.getSecond() - 1 : pmi.getSecond(), pAmount);

                    storedMateria[i] = new Pair<>(pMateriaType, pmi.getSecond() - actual);
                    return actual;
                }
            }
        }

        return 0;
    }

    @Override
    public abstract int getStorageLimit(MateriaItem pMateriaType);

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
