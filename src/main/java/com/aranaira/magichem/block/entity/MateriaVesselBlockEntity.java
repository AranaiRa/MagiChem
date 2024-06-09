package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.MateriaVesselBlock;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class MateriaVesselBlockEntity extends AbstractMateriaStorageBlockEntity  {

    public MateriaVesselBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MATERIA_VESSEL_BE.get(), pos, state);
    }

    public int getStorageLimit() {
        if(currentMateriaType instanceof EssentiaItem) {
            return Config.materiaVesselEssentiaCapacity;
        }
        return Config.materiaVesselAdmixtureCapacity;
    }

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent() {
        BlockState state = getBlockState();
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean stacked = state.getValue(MateriaVesselBlock.STACKED);

        Vector3 origin = Vector3.zero();
        if(stacked) {
            if(facing == Direction.NORTH)
                origin = new Vector3(0.5, 0.1875, 0.25);
            else if(facing == Direction.SOUTH)
                origin = new Vector3(0.5, 0.1875, 0.75);
            else if(facing == Direction.EAST)
                origin = new Vector3(0.25, 0.1875, 0.5);
            else if(facing == Direction.WEST)
                origin = new Vector3(0.75, 0.1875, 0.5);
        } else {
            origin = new Vector3(0.5, 0.5, 0.5);
        }

        Vector3 tangent = Vector3.zero();
        if(stacked) {
            if(facing == Direction.NORTH)
                tangent = new Vector3(0, 0, -1);
            else if(facing == Direction.SOUTH)
                tangent = new Vector3(0, 0, 1);
            else if(facing == Direction.EAST)
                tangent = new Vector3(1, 0, 0);
            else if(facing == Direction.WEST)
                tangent = new Vector3(-1, 0, 0);
        } else {
            tangent = new Vector3(0, 1, 0);
        }

        return new Pair<>(origin, tangent);
    }
}
