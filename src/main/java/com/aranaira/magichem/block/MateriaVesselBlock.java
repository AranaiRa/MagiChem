package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class MateriaVesselBlock extends BaseEntityBlock {
    public MateriaVesselBlock(Properties properties) {
        super(properties);
    }

    private static final VoxelShape SHAPE_JAR;
    private static final VoxelShape SHAPE_LID;
    private static final VoxelShape SHAPE_AGGREGATE;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MateriaVesselBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_AGGREGATE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    static {
        SHAPE_JAR = Block.box(3.0D, 0.0D,  3.0D, 13.0D, 14.0D, 13.0D);
        SHAPE_LID = Block.box(5.0D, 12.5D, 5.0D, 11.0D, 15.5D, 11.0D);
        SHAPE_AGGREGATE = Shapes.or(SHAPE_JAR, new VoxelShape[]{SHAPE_LID});
    }
}
