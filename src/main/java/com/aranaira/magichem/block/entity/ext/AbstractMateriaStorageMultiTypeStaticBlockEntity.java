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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public MateriaItem getMateriaTypeInSlot(int pSlot) {
        if(pSlot < storedMateria.length) {
            if(storedMateria[pSlot] != null)
                return storedMateria[pSlot].getFirst();
        }

        return null;
    }

    @Nullable
    public int getMateriaAmountInSlot(int pSlot) {
        if(pSlot < storedMateria.length) {
            if(storedMateria[pSlot] != null)
                return storedMateria[pSlot].getSecond();
        }

        return -1;
    }

    @Override
    public void setContents(MateriaItem pMateriaType, int pCount) {
        if (containsMateriaType(pMateriaType)) {
            for(int i=0; i<storedMateria.length; i++) {
                Pair<MateriaItem, Integer> pmi = storedMateria[i];
                if(pmi != null) {
                    if(pmi.getFirst() == pMateriaType) {
                        storedMateria[i] = new Pair<>(pMateriaType, pCount);
                        break;
                    }
                }
            }
        }
        else {
            for (int i = 0; i < storedMateria.length; i++) {
                Pair<MateriaItem, Integer> pmi = storedMateria[i];
                if (pmi == null) {
                    storedMateria[i] = new Pair<>(pMateriaType, pCount);
                    break;
                }
            }
        }
        syncAndSave();
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

                    syncAndSave();
                    return remainder;
                }
            } else if(firstEmptyIndex == -1){
                firstEmptyIndex = i;
            }
        }

        if(firstEmptyIndex > -1) {
            int remainder = pVoidExcess ? 0 : Math.max(0, pAmount - getStorageLimit(pMateriaType));

            storedMateria[firstEmptyIndex] = new Pair<>(pMateriaType, Math.min(getStorageLimit(pMateriaType), pAmount));

            syncAndSave();
            return remainder;
        }

        return 0;
    }

    public int fillSlot(int pSlot, MateriaItem pMateriaType, int pAmount, boolean pVoidExcess) {
        Pair<MateriaItem, Integer> pmi = storedMateria[pSlot];
        if(pmi == null) {
            int inserted = Math.min(pAmount, getStorageLimitIgnoreStoredTypes(pMateriaType));
            storedMateria[pSlot] = new Pair<>(pMateriaType, inserted);

            syncAndSave();
            return inserted;
        } else if(pmi.getFirst() == pMateriaType) {
            int inserted = Math.min(getStorageLimit(pMateriaType), pmi.getSecond() + pAmount);
            storedMateria[pSlot] = new Pair<>(pMateriaType, inserted);

            syncAndSave();
            return pVoidExcess ? pAmount : inserted;
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

                    if(pmi.getSecond() - actual <= 0) {
                        storedMateria[i] = null;
                    } else {
                        storedMateria[i] = new Pair<>(pMateriaType, pmi.getSecond() - actual);
                    }
                    syncAndSave();
                    return actual;
                }
            }
        }

        return 0;
    }

    @Override
    public abstract int getStorageLimit(MateriaItem pMateriaType);

    public abstract int getStorageLimitIgnoreStoredTypes(MateriaItem pMateriaType);

    public abstract int getTypeLimit();

    @Override
    public void load(CompoundTag nbt) {
        for(int i=0; i<storedMateria.length; i++) {
            CompoundTag entry = nbt.getCompound("materiaType"+i);
            String type = entry.getString("type");
            if(!type.equals("empty")) {
                MateriaItem mi = ItemRegistry.getMateriaMap(false, false).get(type);
                storedMateria[i] = new Pair<>(mi, entry.getInt("count"));
            }
        }
        super.load(nbt);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        for(int i=0; i<storedMateria.length; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putString("type", storedMateria[i] == null ? "empty" : storedMateria[i].getFirst().getMateriaName());
            entry.putInt("count", storedMateria[i] == null ? 0 : storedMateria[i].getSecond());

            nbt.put("materiaType"+i, entry);
        }
        super.saveAdditional(nbt);
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        super.handleUpdateTag(nbt);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        for(int i=0; i<4; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putString("type", storedMateria[i] == null ? "empty" : storedMateria[i].getFirst().getMateriaName());
            entry.putInt("count", storedMateria[i] == null ? 0 : storedMateria[i].getSecond());

            nbt.put("materiaType"+i, entry);
        }
        return nbt;
    }

    public abstract int getSlotFromWorldCoord(Vec3 pCoord);

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() instanceof MateriaItem mi) {
            for(int i=0; i<storedMateria.length; i++) {
                if(getMateriaTypeInSlot(i) == mi) {
                    int capacity = getStorageLimit(mi) - getMateriaAmountInSlot(i);
                    return Math.min(pStack.getCount(), capacity);
                }
            }
        }
        return 0;
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() instanceof MateriaItem mi) {
            for(int i=0; i<storedMateria.length; i++) {
                if(getMateriaTypeInSlot(i) == mi) {
                    int capacity = getStorageLimit(mi) - getMateriaAmountInSlot(i);
                    int actual = Math.min(pStack.getCount(), capacity);

                    storedMateria[i] = new Pair<>(mi, storedMateria[i].getSecond() + actual);
                    syncAndSave();

                    return actual;
                }
            }
            //No existing type found, fill a new slot if possible
            for(int i=0; i<storedMateria.length; i++) {
                if(getMateriaTypeInSlot(i) == null) {
                    int capacity = getStorageLimit(mi) - getMateriaAmountInSlot(i);
                    int actual = Math.min(pStack.getCount(), capacity);

                    storedMateria[i] = new Pair<>(mi, actual);
                    syncAndSave();

                    return actual;
                }
            }
        }
        return 0;
    }

    @Override
    public abstract Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType);
}
