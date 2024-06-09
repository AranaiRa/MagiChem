package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.MateriaVesselBlock;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MateriaJarBlockEntity extends AbstractMateriaStorageBlockEntity  {

    public MateriaJarBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MATERIA_JAR_BE.get(), pos, state);
    }

    public int getStorageLimit() {
        if(currentMateriaType instanceof EssentiaItem) {
            return Config.materiaJarEssentiaCapacity;
        }
        return Config.materiaJarAdmixtureCapacity;
    }

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent() {
        Vector3 origin = new Vector3(0.5, 25, 0.5);

        Vector3 tangent = new Vector3(0, 1, 0);

        return new Pair<>(origin, tangent);
    }
}
