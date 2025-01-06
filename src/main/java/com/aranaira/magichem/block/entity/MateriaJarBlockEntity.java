package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MateriaJarBlockEntity extends AbstractMateriaStorageBlockEntity  {

    public MateriaJarBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MATERIA_JAR_BE.get(), pos, state);
    }

    public int getStorageLimit() {
        if(currentMateriaType instanceof EssentiaItem) {
            return ServerConfig.materiaJarEssentiaCapacity;
        }
        return ServerConfig.materiaJarAdmixtureCapacity;
    }

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent() {
        Vector3 origin = new Vector3(0.5, 0.5, 0.5);

        Vector3 tangent = new Vector3(0, 1, 0);

        return new Pair<>(origin, tangent);
    }
}
