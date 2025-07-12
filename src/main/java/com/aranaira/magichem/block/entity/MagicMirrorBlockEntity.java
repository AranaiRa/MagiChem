package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeDynamicBlockEntity;
import com.aranaira.magichem.block.entity.routers.MirrorLabyrinthRouterBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class MagicMirrorBlockEntity extends AbstractMateriaStorageMultiTypeDynamicBlockEntity {
    private BlockPos masterPos;
    private String masterDimID;
    private Level masterDim;
    private MirrorLabyrinthBlockEntity master;

    public MagicMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MAGIC_MIRROR_BE.get(), pos, state);
    }

    public MirrorLabyrinthBlockEntity getMaster() {
        if(master != null) {
            return master;
        }

        if(masterPos != null && level != null) {
            if(masterDimID != null) {
                if(masterDim == null && level instanceof ServerLevel sl) {
                    final ResourceKey<Level> worldTarget = getWorldTarget(masterDimID);
                    if(worldTarget == null) return null;
                    masterDim = sl.getServer().getLevel(worldTarget);
                } else {
                    return null;
                }

                if(masterDim.isLoaded(masterPos)) {
                    BlockEntity be = masterDim.getBlockEntity(masterPos);
                    if(be instanceof MirrorLabyrinthBlockEntity mlbe) {
                        master = mlbe;
                        return master;
                    } else if(be instanceof MirrorLabyrinthRouterBlockEntity mlrbe) {
                        master = mlrbe.getMaster();
                        return master;
                    }
                }
            } else if(level.isLoaded(masterPos)) {
                BlockEntity be = level.getBlockEntity(masterPos);
                if(be instanceof MirrorLabyrinthBlockEntity mlbe) {
                    master = mlbe;
                    return master;
                } else if(be instanceof MirrorLabyrinthRouterBlockEntity mlrbe) {
                    master = mlrbe.getMaster();
                    return master;
                }
            }
        }

        return null;
    }

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType) {
        final Direction facing = getBlockState().getValue(FACING);
        final AttachFace attachFace = getBlockState().getValue(ATTACH_FACE);

        if(attachFace == AttachFace.CEILING) {
            return new Pair<>(new Vector3(0.5, 0.9375, 0.5), new Vector3(0, -1, 0));
        } else if(attachFace == AttachFace.FLOOR) {
            return new Pair<>(new Vector3(0.5, 0.0625, 0.5), new Vector3(0, 1, 0));
        } else {
            if(facing == Direction.NORTH) return new Pair<>(new Vector3(0.5, 0.5, 0.9375), new Vector3(0, 0, -1));
            if(facing == Direction.EAST) return new Pair<>(new Vector3(0.0625, 0.5, 0.5), new Vector3(1, 0, 0));
            if(facing == Direction.SOUTH) return new Pair<>(new Vector3(0.5, 0.5, 0.0625), new Vector3(0, 0, 1));
            if(facing == Direction.WEST) return new Pair<>(new Vector3(0.9375, 0.5, 0.5), new Vector3(-1, 0, 0));
        }

        return new Pair<>(new Vector3(0.5, 0.5, 0.5), new Vector3(0, 1, 0));
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(master == null) getMaster();
        if(master == null) return 0;

        return master.canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(master == null) getMaster();
        if(master == null) return 0;

        return master.insertStackFromShlorp(pStack);
    }

    @Override
    public int getCurrentStock(MateriaItem pMateriaType) {
        if(master == null) getMaster();
        if(master == null) return 0;

        return master.getCurrentStock(pMateriaType);
    }

    @Override
    public float getCurrentStockPercent(MateriaItem pMateriaType) {
        if(master == null) getMaster();
        if(master == null) return 0;

        return master.getCurrentStockPercent(pMateriaType);
    }

    @Override
    public boolean containsMateriaType(MateriaItem pMateriaType) {
        if(master == null) getMaster();
        if(master == null) return false;

        return master.containsMateriaType(pMateriaType);
    }

    @Override
    public Collection<MateriaItem> getMateriaTypes() {
        if(master == null) getMaster();
        if(master == null) return new ArrayList<>();

        return master.getMateriaTypes();
    }

    @Override
    public void setContents(MateriaItem pMateriaType, int pCount) {
        if(master == null) getMaster();
        if(master != null) master.setContents(pMateriaType, pCount);
    }

    @Override
    public void setContents(int pSlot, MateriaItem pMateriaType, int pCount) {
        if(master == null) getMaster();
        if(master != null) master.setContents(pSlot, pMateriaType, pCount);
    }

    @Override
    public int fill(MateriaItem pMateriaType, int pAmount, boolean pVoidExcess) {
        if(master == null) getMaster();
        if(master == null) return 0;

        return master.fill(pMateriaType, pAmount, pVoidExcess);
    }

    @Override
    public int drain(MateriaItem pMateriaType, int pAmount, boolean pKeepOne) {
        if(master == null) getMaster();
        if(master == null) return 0;

        return master.drain(pMateriaType, pAmount, pKeepOne);
    }

    @Override
    public int getStorageLimit(MateriaItem pMateriaType) {
        if(master == null) getMaster();
        if(master == null) return 0;

        return master.getStorageLimit(pMateriaType);
    }

    @Override
    public boolean isBelowTypeLimit() {
        if(master == null) getMaster();
        if(master == null) return false;

        return master.isBelowTypeLimit();
    }

    @Override
    public void load(CompoundTag nbt) {
        if(nbt.contains("masterPos"))
            masterPos = BlockPos.of(nbt.getLong("masterPos"));
        if(nbt.contains("masterDimID"))
            masterDimID = nbt.getString("masterDimID");
        super.load(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        if(masterPos != null)
            nbt.putLong("masterPos",masterPos.asLong());
        if(masterDimID != null)
            nbt.putString("masterDimID", masterDimID);
        super.saveAdditional(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        if(masterPos != null)
            nbt.putLong("masterPos",masterPos.asLong());
        if(masterDimID != null)
            nbt.putString("masterDimID", masterDimID);
        return nbt;
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        if(nbt.contains("masterPos"))
            masterPos = BlockPos.of(nbt.getLong("masterPos"));
        if(nbt.contains("masterDimID"))
            masterDimID = nbt.getString("masterDimID");
    }

    public void setMasterPos(BlockPos pPos) {
        masterPos = pPos;
        syncAndSave();
    }

    public void setMasterDim(String pDimID) {
        masterDimID = pDimID;
        syncAndSave();
    }

    //Shamelessly stolen and modified from MnA's WorldCharm class
    @Nullable
    private ResourceKey<Level> getWorldTarget(String pDimID) {
        if (pDimID != null) {
            ResourceLocation location = new ResourceLocation(pDimID);
            return ResourceKey.create(Registries.DIMENSION, location);
        } else {
            return null;
        }
    }
}
