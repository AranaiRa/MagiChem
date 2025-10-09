package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.MirrorLabyrinthBlock;
import com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeDynamicBlockEntity;
import com.aranaira.magichem.foundation.ICanAbsorbConstructs;
import com.aranaira.magichem.foundation.IDestroysMasterOnDestruction;
import com.aranaira.magichem.foundation.IShlorpReceiver;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class MirrorLabyrinthRouterBlockEntity extends AbstractMateriaStorageMultiTypeDynamicBlockEntity implements IShlorpReceiver, IDestroysMasterOnDestruction, ICanAbsorbConstructs {
    BlockPos masterPos = null;
    MirrorLabyrinthBlockEntity master = null;

    public MirrorLabyrinthRouterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MIRROR_LABYRINTH_ROUTER_BE.get(), pos, state);
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

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType) {
        return new Pair<>(new Vector3(0.5f, 0.5f, 0.5f), Vector3.up());
    }

    public void configure(BlockPos pPos) {
        masterPos = pPos;
        syncAndSave();
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public MirrorLabyrinthBlockEntity getMaster() {
        if(masterPos == null) return null;

        if(master == null) {
            BlockEntity be = getLevel().getBlockEntity(masterPos);
            if(be instanceof MirrorLabyrinthBlockEntity mmbe) {
                master = mmbe;
                return mmbe;
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
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return 0;

        return master.getCurrentStock(pMateriaType);
    }

    @Override
    public float getCurrentStockPercent(MateriaItem pMateriaType) {
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return 0;

        return master.getCurrentStockPercent(pMateriaType);
    }

    @Override
    public boolean containsMateriaType(MateriaItem pMateriaType) {
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return false;

        return master.containsMateriaType(pMateriaType);
    }

    @Override
    public Collection<MateriaItem> getMateriaTypes() {
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return new ArrayList<>();

        return master.getMateriaTypes();
    }

    @Override
    public void setContents(MateriaItem pMateriaType, int pCount) {
        if(masterPos != null || master != null)
            getMaster();
        if(master != null) {
            master.setContents(pMateriaType, pCount);
        }
    }

    @Override
    public void setContents(int pSlot, MateriaItem pMateriaType, int pCount) {
        if(masterPos != null || master != null)
            getMaster();
        if(master != null) {
            master.setContents(pSlot, pMateriaType, pCount);
        }
    }

    @Override
    public int fill(MateriaItem pMateriaType, int pAmount, boolean pVoidExcess) {
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return 0;

        return master.fill(pMateriaType, pAmount, pVoidExcess);
    }

    @Override
    public int drain(MateriaItem pMateriaType, int pAmount, boolean pKeepOne) {
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return 0;

        return master.drain(pMateriaType, pAmount, pKeepOne);
    }

    @Override
    public int getStorageLimit(MateriaItem pMateriaType) {
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return 0;

        return master.getStorageLimit(pMateriaType);
    }

    @Override
    public boolean isBelowTypeLimit() {
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return false;

        return master.isBelowTypeLimit();
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return 0;

        return master.canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(masterPos != null || master != null)
            getMaster();
        if(master == null) return pStack.getCount();

        return master.insertStackFromShlorp(pStack);
    }
}
