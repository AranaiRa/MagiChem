package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.routers.*;
import com.aranaira.magichem.util.MathHelper;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BaseActuatorRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public BaseActuatorRouterBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(ELEMENT, 0).setValue(FACING, Direction.NORTH));
    }

    public static final IntegerProperty ELEMENT = IntegerProperty.create("element", 0, 6);
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final int
            ELEMENT_ERROR = 0,
            ELEMENT_ENDER = 1, ELEMENT_EARTH = 2, ELEMENT_WATER = 3,
            ELEMENT_AIR = 4, ELEMENT_FIRE = 5, ELEMENT_ARCANE = 6;
    public static VoxelShape
            VOXEL_SHAPE_WATER_PIPES_NORTH, VOXEL_SHAPE_WATER_TUBE_BODY_NORTH, VOXEL_SHAPE_WATER_TUBE_CAP_NORTH,
            VOXEL_SHAPE_FIRE_LEFT_NUB, VOXEL_SHAPE_FIRE_LEFT_HORIZONTAL, VOXEL_SHAPE_FIRE_LEFT_VERTICAL, VOXEL_SHAPE_FIRE_RIGHT_NUB, VOXEL_SHAPE_FIRE_RIGHT_HORIZONTAL, VOXEL_SHAPE_FIRE_RIGHT_VERTICAL, VOXEL_SHAPE_FIRE_CENTER,

            VOXEL_SHAPE_WATER_AGGREGATE_NORTH, VOXEL_SHAPE_WATER_AGGREGATE_EAST, VOXEL_SHAPE_WATER_AGGREGATE_SOUTH, VOXEL_SHAPE_WATER_AGGREGATE_WEST,
            VOXEL_SHAPE_FIRE_AGGREGATE_NORTH, VOXEL_SHAPE_FIRE_AGGREGATE_EAST, VOXEL_SHAPE_FIRE_AGGREGATE_SOUTH, VOXEL_SHAPE_FIRE_AGGREGATE_WEST,

            VOXEL_SHAPE_EARTH_CAP_NORTH, VOXEL_SHAPE_EARTH_POST_NW_NORTH, VOXEL_SHAPE_EARTH_POST_NE_NORTH,
            VOXEL_SHAPE_EARTH_POST_SW_NORTH, VOXEL_SHAPE_EARTH_POST_SE_NORTH, VOXEL_SHAPE_EARTH_HOUSING_NORTH,
            VOXEL_SHAPE_EARTH_AGGREGATE_NORTH, VOXEL_SHAPE_EARTH_AGGREGATE_EAST, VOXEL_SHAPE_EARTH_AGGREGATE_SOUTH, VOXEL_SHAPE_EARTH_AGGREGATE_WEST,

            VOXEL_SHAPE_AIR_SIPHON_BASE, VOXEL_SHAPE_AIR_SIPHON_CONNECTOR, VOXEL_SHAPE_AIR_SIPHON, VOXEL_SHAPE_AIR_TANK, VOXEL_SHAPE_AIR_TANK_MOUNT,
            VOXEL_SHAPE_AIR_AGGREGATE_NORTH, VOXEL_SHAPE_AIR_AGGREGATE_EAST, VOXEL_SHAPE_AIR_AGGREGATE_SOUTH, VOXEL_SHAPE_AIR_AGGREGATE_WEST,

            VOXEL_SHAPE_ARCANE;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BaseActuatorRouterBlockEntity(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ELEMENT);
        pBuilder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof BaseActuatorRouterBlockEntity barbe) {
            BlockEntity master = barbe.getMaster();
            pPlayer.swing(InteractionHand.MAIN_HAND);
            return master.getBlockState().getBlock().use(master.getBlockState(), pLevel, master.getBlockPos(), pPlayer, pHand, pHit);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int element = pState.getValue(ELEMENT);
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        if(element == ELEMENT_WATER) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_WATER_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_WATER_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_WATER_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_WATER_AGGREGATE_WEST;
        } else if(element == ELEMENT_FIRE) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_FIRE_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_FIRE_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_FIRE_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_FIRE_AGGREGATE_WEST;
        } else if(element == ELEMENT_EARTH) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_EARTH_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_EARTH_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_EARTH_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_EARTH_AGGREGATE_WEST;
        } else if(element == ELEMENT_AIR) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_AIR_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_AIR_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_AIR_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_AIR_AGGREGATE_WEST;
        } else if(element == ELEMENT_ARCANE) {
            return VOXEL_SHAPE_ARCANE;
        }

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos().above();

        if(!pContext.getLevel().isEmptyBlock(pos)) {
            return null;
        }

        return super.getStateForPlacement(pContext);
    }

    static {
        VOXEL_SHAPE_FIRE_LEFT_NUB = Block.box(2, 0, 6, 4, 1, 8);
        VOXEL_SHAPE_FIRE_LEFT_HORIZONTAL = Block.box(4, 0, 2, 6, 1, 8);
        VOXEL_SHAPE_FIRE_LEFT_VERTICAL = Block.box(4, 0, 0, 6, 15, 2);
        VOXEL_SHAPE_FIRE_RIGHT_NUB = Block.box(12, 0, 6, 14, 1, 8);
        VOXEL_SHAPE_FIRE_RIGHT_HORIZONTAL = Block.box(10, 0, 2, 12, 1, 8);
        VOXEL_SHAPE_FIRE_RIGHT_VERTICAL = Block.box(10, 0, 0, 12, 15, 2);
        VOXEL_SHAPE_FIRE_CENTER = Block.box(6, 0, 1, 10, 9, 5);

        VOXEL_SHAPE_FIRE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_FIRE_LEFT_NUB,
                VOXEL_SHAPE_FIRE_LEFT_HORIZONTAL,
                VOXEL_SHAPE_FIRE_LEFT_VERTICAL,
                VOXEL_SHAPE_FIRE_RIGHT_NUB,
                VOXEL_SHAPE_FIRE_RIGHT_HORIZONTAL,
                VOXEL_SHAPE_FIRE_RIGHT_VERTICAL,
                VOXEL_SHAPE_FIRE_CENTER);

        VOXEL_SHAPE_FIRE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_LEFT_NUB, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_LEFT_HORIZONTAL, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_LEFT_VERTICAL, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_RIGHT_NUB, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_RIGHT_HORIZONTAL, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_RIGHT_VERTICAL, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_CENTER, 1));

        VOXEL_SHAPE_FIRE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_LEFT_NUB, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_LEFT_HORIZONTAL, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_LEFT_VERTICAL, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_RIGHT_NUB, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_RIGHT_HORIZONTAL, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_RIGHT_VERTICAL, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_CENTER, 2));

        VOXEL_SHAPE_FIRE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_LEFT_NUB, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_LEFT_HORIZONTAL, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_LEFT_VERTICAL, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_RIGHT_NUB, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_RIGHT_HORIZONTAL, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_RIGHT_VERTICAL, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FIRE_CENTER, 3));

        VOXEL_SHAPE_WATER_TUBE_BODY_NORTH = Block.box(5, 0,  8, 11, 12, 14);
        VOXEL_SHAPE_WATER_TUBE_CAP_NORTH = Block.box(4, 12,  7, 12, 15, 15);
        VOXEL_SHAPE_WATER_PIPES_NORTH = Block.box(5.5, 0,  5, 10.5, 11, 8);

        VOXEL_SHAPE_WATER_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_WATER_TUBE_BODY_NORTH,
                VOXEL_SHAPE_WATER_TUBE_CAP_NORTH,
                VOXEL_SHAPE_WATER_PIPES_NORTH);

        VOXEL_SHAPE_WATER_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_WATER_TUBE_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_WATER_TUBE_CAP_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_WATER_PIPES_NORTH, 1));

        VOXEL_SHAPE_WATER_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_WATER_TUBE_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_WATER_TUBE_CAP_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_WATER_PIPES_NORTH, 2));

        VOXEL_SHAPE_WATER_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_WATER_TUBE_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_WATER_TUBE_CAP_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_WATER_PIPES_NORTH, 3));

        VOXEL_SHAPE_EARTH_CAP_NORTH = Block.box(1, 12, 1, 15, 15, 15);
        VOXEL_SHAPE_EARTH_POST_NW_NORTH = Block.box(2,0,2,4,12,4);
        VOXEL_SHAPE_EARTH_POST_NE_NORTH = Block.box(12,0,2,14,12,4);
        VOXEL_SHAPE_EARTH_POST_SW_NORTH = Block.box(2,0,12,4,12,14);
        VOXEL_SHAPE_EARTH_POST_SE_NORTH = Block.box(12,0,12,14,12,14);
        VOXEL_SHAPE_EARTH_HOUSING_NORTH = Block.box(3,5,3,13,12,13);

        VOXEL_SHAPE_EARTH_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_EARTH_CAP_NORTH,
                VOXEL_SHAPE_EARTH_POST_NW_NORTH,
                VOXEL_SHAPE_EARTH_POST_NE_NORTH,
                VOXEL_SHAPE_EARTH_POST_SW_NORTH,
                VOXEL_SHAPE_EARTH_POST_SE_NORTH,
                VOXEL_SHAPE_EARTH_HOUSING_NORTH);

        VOXEL_SHAPE_EARTH_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_CAP_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_NW_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_NE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_SW_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_SE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_HOUSING_NORTH, 1));

        VOXEL_SHAPE_EARTH_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_CAP_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_NW_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_NE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_SW_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_SE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_HOUSING_NORTH, 2));

        VOXEL_SHAPE_EARTH_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_CAP_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_NW_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_NE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_SW_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_POST_SE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EARTH_HOUSING_NORTH, 3));

        VOXEL_SHAPE_AIR_SIPHON_BASE      = Block.box(6, 0, 1, 10, 1, 5);
        VOXEL_SHAPE_AIR_SIPHON_CONNECTOR = Block.box(6,6,4.5,10,10,6.5);
        VOXEL_SHAPE_AIR_SIPHON           = Block.box(7,0,2,9,9,10);
        VOXEL_SHAPE_AIR_TANK             = Block.box(4,2,6.5,12,15,14.5);
        VOXEL_SHAPE_AIR_TANK_MOUNT       = Block.box(5.5,0,8,10.5,2,13);

        VOXEL_SHAPE_AIR_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_AIR_SIPHON_BASE,
                VOXEL_SHAPE_AIR_SIPHON_CONNECTOR,
                VOXEL_SHAPE_AIR_SIPHON,
                VOXEL_SHAPE_AIR_TANK,
                VOXEL_SHAPE_AIR_TANK_MOUNT);

        VOXEL_SHAPE_AIR_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_SIPHON_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_SIPHON_CONNECTOR, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_SIPHON, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_TANK, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_TANK_MOUNT, 1));

        VOXEL_SHAPE_AIR_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_SIPHON_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_SIPHON_CONNECTOR, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_SIPHON, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_TANK, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_TANK_MOUNT, 2));

        VOXEL_SHAPE_AIR_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_SIPHON_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_SIPHON_CONNECTOR, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_SIPHON, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_TANK, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_AIR_TANK_MOUNT, 3));

        VOXEL_SHAPE_ARCANE      = Block.box(5, 2, 5, 11, 10, 11);
    }
}