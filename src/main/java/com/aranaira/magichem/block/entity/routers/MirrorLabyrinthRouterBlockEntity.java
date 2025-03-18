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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
        nbt.putLong("masterPos", masterPos.asLong());
        return nbt;
    }

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType) {
        return null;
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
}
