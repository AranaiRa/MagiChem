package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.MirrorLabyrinthBlock;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeDynamicBlockEntity;
import com.aranaira.magichem.foundation.IRequiresRouterCleanupOnDestruction;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MirrorLabyrinthBlockEntity extends AbstractMateriaStorageMultiTypeDynamicBlockEntity implements IShlorpReceiver, IRequiresRouterCleanupOnDestruction {
    public MirrorLabyrinthBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MIRROR_LABYRINTH_BE.get(), pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {

    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {

    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {

    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        return nbt;
    }

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType) {
        return null;
    }

    @Override
    public void destroyRouters() {
        MirrorLabyrinthBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(MagiChemBlockStateProperties.FACING));
    }
}
