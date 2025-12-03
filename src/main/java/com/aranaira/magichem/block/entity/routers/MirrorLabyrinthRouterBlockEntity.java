package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.MirrorLabyrinthBlock;
import com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeDynamicBlockEntity;
import com.aranaira.magichem.foundation.ICanAbsorbConstructs;
import com.aranaira.magichem.foundation.IDestroysMasterOnDestruction;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.enums.MirrorLabyrinthRouterType;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_MIRROR_LABYRINTH;

public class MirrorLabyrinthRouterBlockEntity extends AbstractMateriaStorageMultiTypeDynamicBlockEntity implements IShlorpReceiver, IDestroysMasterOnDestruction, ICanAbsorbConstructs {
    BlockPos masterPos = null;
    MirrorLabyrinthBlockEntity master = null;

    public MirrorLabyrinthRouterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MIRROR_LABYRINTH_ROUTER_BE.get(), pos, state);

        final MirrorLabyrinthRouterType routerType = MirrorLabyrinthRouterBlockEntity.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_MIRROR_LABYRINTH));
        for (Pair<BlockPos, MirrorLabyrinthRouterType> data : MirrorLabyrinthBlock.getRouterOffsets(state.getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            if(data.getSecond() == routerType) {
                this.masterPos = pos.offset(data.getFirst().multiply(-1));
            }
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        if(nbt.contains("masterPos"))
            masterPos = BlockPos.of(nbt.getLong("masterPos"));
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putLong("masterPos", masterPos.asLong());
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        load(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        if(masterPos != null)
            nbt.putLong("masterPos", masterPos.asLong());
        return nbt;
    }

    public static int mapRouterTypeToInt(MirrorLabyrinthRouterType pRouterType) {
        if(pRouterType == null)
            return 0;

        return switch(pRouterType) {
            case DAIS -> 1;
            case CENTER -> 2;
            case LEFT_FRONT -> 3;
            case LEFT -> 4;
            case LEFT_BACK -> 5;
            case CENTER_BACK -> 6;
            case RIGHT_BACK -> 7;
            case RIGHT -> 8;
            case RIGHT_FRONT -> 9;
            case CONSTRUCT_LOWER -> 10;
            case CONSTRUCT_UPPER -> 11;
            case MATRIX_LOWER -> 12;
            case MATRIX_UPPER -> 13;
            default -> 0;
        };
    }

    public static MirrorLabyrinthRouterType unmapRouterTypeFromInt(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> MirrorLabyrinthRouterType.DAIS;
            case 2 -> MirrorLabyrinthRouterType.CENTER;
            case 3 -> MirrorLabyrinthRouterType.LEFT_FRONT;
            case 4 -> MirrorLabyrinthRouterType.LEFT;
            case 5 -> MirrorLabyrinthRouterType.LEFT_BACK;
            case 6 -> MirrorLabyrinthRouterType.CENTER_BACK;
            case 7 -> MirrorLabyrinthRouterType.RIGHT_BACK;
            case 8 -> MirrorLabyrinthRouterType.RIGHT;
            case 9 -> MirrorLabyrinthRouterType.RIGHT_FRONT;
            case 10 -> MirrorLabyrinthRouterType.CONSTRUCT_LOWER;
            case 11 -> MirrorLabyrinthRouterType.CONSTRUCT_UPPER;
            case 12 -> MirrorLabyrinthRouterType.MATRIX_LOWER;
            case 13 -> MirrorLabyrinthRouterType.MATRIX_UPPER;
            default -> MirrorLabyrinthRouterType.NONE;
        };
    }

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType) {
        return new Pair<>(new Vector3(0.5f, 0.5f, 0.5f), Vector3.up());
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public MirrorLabyrinthBlockEntity getMaster() {
        if(master == null || masterPos == null) {
            final MirrorLabyrinthRouterType routerType = MirrorLabyrinthRouterBlockEntity.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_MIRROR_LABYRINTH));
            for (Pair<BlockPos, MirrorLabyrinthRouterType> query : MirrorLabyrinthBlock.getRouterOffsets(getBlockState().getValue(FACING))) {
                if(routerType == query.getSecond()) {
                    BlockPos offset = query.getFirst().multiply(-1);
                    BlockPos target = getBlockPos().offset(offset);
                    BlockEntity be = level.getBlockEntity(target);
                    if(be instanceof MirrorLabyrinthBlockEntity labyrinth) {
                        masterPos = labyrinth.getBlockPos();
                        master = labyrinth;
                        return master;
                    }
                }
            }
        }

        return master;
    }

    @Override
    public void destroyMaster() {
        getLevel().destroyBlock(getMasterPos(), true);
        MirrorLabyrinthBlock.destroyRouters(getLevel(), getMasterPos(), getBlockState().getValue(FACING));
    }

    @Override
    public boolean tryAbsorbConstruct(Player pPlayer) {
        return getMaster().tryAbsorbConstruct(pPlayer);
    }

    @Override
    public void ejectConstruct() {
        getMaster().ejectConstruct();
    }

    @Override
    public boolean hasConstruct() {
        return getMaster().hasConstruct();
    }

    @Override
    public CompoundTag getStoredConstructComposition() {
        return getMaster().getStoredConstructComposition();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getMaster() == null ? LazyOptional.empty() : getMaster().getCapability(cap, side);
    }

    @Override
    public int getCurrentStock(MateriaItem pMateriaType) {
        if(getMaster() == null) return 0;

        return master.getCurrentStock(pMateriaType);
    }

    @Override
    public float getCurrentStockPercent(MateriaItem pMateriaType) {
        if(getMaster() == null) return 0;

        return master.getCurrentStockPercent(pMateriaType);
    }

    @Override
    public boolean containsMateriaType(MateriaItem pMateriaType) {
        if(getMaster() == null) return false;

        return master.containsMateriaType(pMateriaType);
    }

    @Override
    public Collection<MateriaItem> getMateriaTypes() {
        if(getMaster() == null) return new ArrayList<>();

        return master.getMateriaTypes();
    }

    @Override
    public void setContents(MateriaItem pMateriaType, int pCount) {
        if(getMaster() != null) master.setContents(pMateriaType, pCount);
    }

    @Override
    public void setContents(int pSlot, MateriaItem pMateriaType, int pCount) {
        if(getMaster() != null) master.setContents(pSlot, pMateriaType, pCount);
    }

    @Override
    public int fill(MateriaItem pMateriaType, int pAmount, boolean pVoidExcess) {
        if(getMaster() == null) return 0;

        return master.fill(pMateriaType, pAmount, pVoidExcess);
    }

    @Override
    public int drain(MateriaItem pMateriaType, int pAmount, boolean pKeepOne) {
        if(getMaster() == null) return 0;

        return master.drain(pMateriaType, pAmount, pKeepOne);
    }

    @Override
    public int getStorageLimit(MateriaItem pMateriaType) {
        if(getMaster() == null) return 0;

        return master.getStorageLimit(pMateriaType);
    }

    @Override
    public boolean isBelowTypeLimit() {
        if(getMaster() == null) return false;

        return master.isBelowTypeLimit();
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(getMaster() == null) return 0;

        return master.canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(getMaster() == null) return 0;

        return master.insertStackFromShlorp(pStack);
    }
}
