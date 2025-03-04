package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeStaticBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MateriaJarQuadBlockEntity extends AbstractMateriaStorageMultiTypeStaticBlockEntity {
    public MateriaJarQuadBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MATERIA_JAR_QUAD_BE.get(), pos, state);
        storedMateria = new Pair[4];
    }

    @Override
    public int getStorageLimit(MateriaItem pMateriaType) {
        if(containsMateriaType(pMateriaType))
            return (pMateriaType instanceof AdmixtureItem) ? ServerConfig.materiaJarAdmixtureCapacity : ServerConfig.materiaJarEssentiaCapacity;

        return 0;
    }

    @Override
    public int getStorageLimitIgnoreStoredTypes(MateriaItem pMateriaType) {
        return (pMateriaType instanceof AdmixtureItem) ? ServerConfig.materiaJarAdmixtureCapacity : ServerConfig.materiaJarEssentiaCapacity;
    }

    @Override
    public int getTypeLimit() {
        return 4;
    }

    @Override
    public int getSlotFromWorldCoord(Vec3 pCoord) {
        double x = ((pCoord.x % 1) + 4) % 1;
        double y = ((pCoord.y % 1) + 4) % 1;
        double z = ((pCoord.z % 1) + 4) % 1;

        int xCell = 0, zCell = 0;
        boolean yValid = false;

        if(x >= 0.0625 && x <= 0.4375) xCell = 1;
        else if(x >= 0.5625 && x <= 0.9375) xCell = 2;

        if(z >= 0.0625 && z <= 0.4375) zCell = 1;
        else if(z >= 0.5625 && z <= 0.9375) zCell = 2;

        if(y >= 0 && y <= 0.59375) yValid = true;

        if(xCell == 1 && zCell == 1 && yValid) return 0;
        else if(xCell == 2 && zCell == 1 && yValid) return 1;
        else if(xCell == 1 && zCell == 2 && yValid) return 2;
        else if(xCell == 2 && zCell == 2 && yValid) return 3;

        return -1;
    }

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType) {
        int index = -1;
        for(int i=0; i<4; i++) {
            if(storedMateria[i] != null && storedMateria[i].getFirst() == pMateriaType) {
                index = i;
                break;
            }
        }

        if(index == 0) return new Pair<>(new Vector3(0.25, 0.5, 0.25), Vector3.up());
        else if(index == 1) return new Pair<>(new Vector3(0.75, 0.5, 0.25), Vector3.up());
        else if(index == 2) return new Pair<>(new Vector3(0.25, 0.5, 0.75), Vector3.up());
        else if(index == 3) return new Pair<>(new Vector3(0.75, 0.5, 0.75), Vector3.up());

        return null;
    }
}
