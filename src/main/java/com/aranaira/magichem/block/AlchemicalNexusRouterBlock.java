package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.routers.ActuatorWaterRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.AlchemicalNexusRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.util.MathHelper;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

public class AlchemicalNexusRouterBlock extends BaseEntityBlock implements INoCreativeTab {

    public AlchemicalNexusRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    public static final IntegerProperty ALCHEMICAL_NEXUS_ROUTER_TYPE = IntegerProperty.create("alchemical_nexus_router_type", 0, 9);
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape
            VOXEL_SHAPE_ERROR,

            VOXEL_SHAPE_FRONT_BASE, VOXEL_SHAPE_FRONT_CANDLE_LEFT, VOXEL_SHAPE_FRONT_CANDLE_RIGHT,
            VOXEL_SHAPE_FRONT_SHELF_1, VOXEL_SHAPE_FRONT_SHELF_2, VOXEL_SHAPE_FRONT_RAISED_CENTER,
            VOXEL_SHAPE_FRONT_AGGREGATE_NORTH, VOXEL_SHAPE_FRONT_AGGREGATE_EAST, VOXEL_SHAPE_FRONT_AGGREGATE_SOUTH, VOXEL_SHAPE_FRONT_AGGREGATE_WEST,

            VOXEL_SHAPE_FRONT_LEFT_BASE, VOXEL_SHAPE_FRONT_LEFT_SHELF_1, VOXEL_SHAPE_FRONT_LEFT_SHELF_2,
            VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_WEST,

            VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_WEST,

            VOXEL_SHAPE_BACK_TANK, VOXEL_SHAPE_BACK_SPOUT,
            VOXEL_SHAPE_BACK_AGGREGATE_NORTH, VOXEL_SHAPE_BACK_AGGREGATE_EAST, VOXEL_SHAPE_BACK_AGGREGATE_SOUTH, VOXEL_SHAPE_BACK_AGGREGATE_WEST,

            VOXEL_SHAPE_BACK_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_BACK_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_BACK_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_BACK_LEFT_AGGREGATE_WEST,

            VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_WEST,

            VOXEL_SHAPE_PLUG_LEFT,
            VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_WEST,

            VOXEL_SHAPE_PLUG_RIGHT,
            VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_WEST,

            VOXEL_SHAPE_TOP_DAIS, VOXEL_SHAPE_TOP_CRYSTAL,
            VOXEL_SHAPE_TOP_AGGREGATE;
    private static final int
        PLUG_LEFT = 1, PLUG_RIGHT = 2, FRONT = 3, FRONT_LEFT = 4, FRONT_RIGHT = 5, BACK_TANK = 6, BACK_LEFT = 7, BACK_RIGHT = 8, TOP_CRYSTAL = 9;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AlchemicalNexusRouterBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof ActuatorWaterRouterBlockEntity router) {
            BlockState masterState = router.getMaster().getBlockState();
            pLevel.levelEvent(pPlayer, 2001, pPos, getId(masterState));
        }
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ALCHEMICAL_NEXUS_ROUTER_TYPE);
        pBuilder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof AlchemicalNexusRouterBlockEntity anrbe) {
            AlchemicalNexusBlockEntity master = anrbe.getMaster();
            pPlayer.swing(InteractionHand.MAIN_HAND);
            return master.getBlockState().getBlock().use(master.getBlockState(), pLevel, master.getBlockPos(), pPlayer, pHand, pHit);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        Integer routerType = state.getValue(ALCHEMICAL_NEXUS_ROUTER_TYPE);
        return routerType == TOP_CRYSTAL ? 15 : 0;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int routerType = pState.getValue(ALCHEMICAL_NEXUS_ROUTER_TYPE);
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        if(routerType == PLUG_LEFT) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_WEST;
        } else if(routerType == PLUG_RIGHT) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_WEST;
        } else if(routerType == FRONT) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_FRONT_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_FRONT_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_FRONT_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_FRONT_AGGREGATE_WEST;
        } else if(routerType == FRONT_LEFT) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_WEST;
        } else if(routerType == FRONT_RIGHT) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_WEST;
        } else if(routerType == BACK_TANK) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_BACK_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_BACK_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_BACK_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_BACK_AGGREGATE_WEST;
        } else if(routerType == BACK_LEFT) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_BACK_LEFT_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_BACK_LEFT_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_BACK_LEFT_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_BACK_LEFT_AGGREGATE_WEST;
        } else if(routerType == BACK_RIGHT) {
            if (facing == Direction.NORTH) return VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_NORTH;
            if (facing == Direction.EAST) return VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_EAST;
            if (facing == Direction.SOUTH) return VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_SOUTH;
            if (facing == Direction.WEST) return VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_WEST;
        } else if(routerType == TOP_CRYSTAL) {
            return VOXEL_SHAPE_TOP_AGGREGATE;
        }

        return VOXEL_SHAPE_ERROR;
    }

    static {
        VOXEL_SHAPE_ERROR = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);

        VOXEL_SHAPE_FRONT_BASE          = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 12.0);
        VOXEL_SHAPE_FRONT_CANDLE_LEFT   = Block.box(-0.478, 4.0, 5.776, 3.442, 14.0, 9.695);
        VOXEL_SHAPE_FRONT_CANDLE_RIGHT  = Block.box(12.558, 4.0, 5.776, 16.478, 14.0, 9.695);
        VOXEL_SHAPE_FRONT_SHELF_1       = Block.box(0.0, 4.0, 0.0, 16.0, 7.0, 9.0);
        VOXEL_SHAPE_FRONT_SHELF_2       = Block.box(0.0, 7.0, 0.0, 16.0, 10.0, 8.0);
        VOXEL_SHAPE_FRONT_RAISED_CENTER = Block.box(0.0, 10.0, 0.0, 16.0, 14.0, 2.926);

        VOXEL_SHAPE_FRONT_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_FRONT_BASE,
                VOXEL_SHAPE_FRONT_CANDLE_LEFT,
                VOXEL_SHAPE_FRONT_CANDLE_RIGHT,
                VOXEL_SHAPE_FRONT_SHELF_1,
                VOXEL_SHAPE_FRONT_SHELF_2,
                VOXEL_SHAPE_FRONT_RAISED_CENTER
        );

        VOXEL_SHAPE_FRONT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 1)
        );

        VOXEL_SHAPE_FRONT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 2)
        );

        VOXEL_SHAPE_FRONT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 3)
        );

        VOXEL_SHAPE_FRONT_LEFT_BASE          = Block.box(4.0, 0.0, 0.0, 16.0, 4.0, 12.0);
        VOXEL_SHAPE_FRONT_LEFT_SHELF_1       = Block.box(7.0, 4.0, 0.0, 16.0, 7.0, 9.0);
        VOXEL_SHAPE_FRONT_LEFT_SHELF_2       = Block.box(8.0, 7.0, 0.0, 16.0, 10.0, 8.0);

        VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_FRONT_LEFT_BASE,
                VOXEL_SHAPE_FRONT_LEFT_SHELF_1,
                VOXEL_SHAPE_FRONT_LEFT_SHELF_2
        );

        VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 1)
        );

        VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 2)
        );

        VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 3)
        );

        VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 3)
        );

        VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_EAST = Shapes.or(
                VOXEL_SHAPE_FRONT_LEFT_BASE,
                VOXEL_SHAPE_FRONT_LEFT_SHELF_1,
                VOXEL_SHAPE_FRONT_LEFT_SHELF_2
        );

        VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 1)
        );

        VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 2)
        );

        VOXEL_SHAPE_BACK_TANK          = Block.box(4.0, 0.0, 1.0, 12.0, 12.0, 12.0);
        VOXEL_SHAPE_BACK_SPOUT         = Block.box(6.0, 12.0, 6.0, 10.0, 16.0, 10.0);

        VOXEL_SHAPE_BACK_AGGREGATE_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 2),
                VOXEL_SHAPE_BACK_TANK,
                VOXEL_SHAPE_BACK_SPOUT
                );

        VOXEL_SHAPE_BACK_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_TANK, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_SPOUT, 1)
        );

        VOXEL_SHAPE_BACK_AGGREGATE_SOUTH = Shapes.or(
                VOXEL_SHAPE_FRONT_BASE,
                VOXEL_SHAPE_FRONT_CANDLE_LEFT,
                VOXEL_SHAPE_FRONT_CANDLE_RIGHT,
                VOXEL_SHAPE_FRONT_SHELF_1,
                VOXEL_SHAPE_FRONT_SHELF_2,
                VOXEL_SHAPE_FRONT_RAISED_CENTER,
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_TANK, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_SPOUT, 2)
        );

        VOXEL_SHAPE_BACK_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_TANK, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_SPOUT, 3)
        );

        VOXEL_SHAPE_BACK_LEFT_AGGREGATE_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 1)
        );

        VOXEL_SHAPE_BACK_LEFT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 2)
        );

        VOXEL_SHAPE_BACK_LEFT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 3)
        );

        VOXEL_SHAPE_BACK_LEFT_AGGREGATE_WEST = Shapes.or(
                VOXEL_SHAPE_FRONT_LEFT_BASE,
                VOXEL_SHAPE_FRONT_LEFT_SHELF_1,
                VOXEL_SHAPE_FRONT_LEFT_SHELF_2
        );

        VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 2)
        );

        VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 3)
        );

        VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                VOXEL_SHAPE_FRONT_LEFT_BASE,
                VOXEL_SHAPE_FRONT_LEFT_SHELF_1,
                VOXEL_SHAPE_FRONT_LEFT_SHELF_2
        );

        VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_1, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_SHELF_2, 1)
        );

        VOXEL_SHAPE_PLUG_LEFT = Block.box(0.0, 0.0, 0.0, 4.0, 16.0, 16.0);

        VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 1),
                VOXEL_SHAPE_PLUG_LEFT
        );

        VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT, 1)
        );

        VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT, 2)
        );

        VOXEL_SHAPE_PLUG_LEFT_AGGREGATE_WEST = Shapes.or(
                VOXEL_SHAPE_FRONT_BASE,
                VOXEL_SHAPE_FRONT_CANDLE_LEFT,
                VOXEL_SHAPE_FRONT_CANDLE_RIGHT,
                VOXEL_SHAPE_FRONT_SHELF_1,
                VOXEL_SHAPE_FRONT_SHELF_2,
                VOXEL_SHAPE_FRONT_RAISED_CENTER,
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT, 3)
        );

        VOXEL_SHAPE_PLUG_RIGHT = Block.box(12.0, 0.0, 0.0, 16.0, 16.0, 16.0);

        VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 3),
                VOXEL_SHAPE_PLUG_RIGHT
        );

        VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_EAST = Shapes.or(
                VOXEL_SHAPE_FRONT_BASE,
                VOXEL_SHAPE_FRONT_CANDLE_LEFT,
                VOXEL_SHAPE_FRONT_CANDLE_RIGHT,
                VOXEL_SHAPE_FRONT_SHELF_1,
                VOXEL_SHAPE_FRONT_SHELF_2,
                VOXEL_SHAPE_FRONT_RAISED_CENTER,
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_RIGHT, 1)
        );

        VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_RIGHT, 2)
        );

        VOXEL_SHAPE_PLUG_RIGHT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_LEFT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_CANDLE_RIGHT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_1, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_SHELF_2, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_RAISED_CENTER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_RIGHT, 3)
        );

        VOXEL_SHAPE_TOP_DAIS    = Block.box(2.074, 0.0, 2.074, 13.956, 4.0, 13.926);
        VOXEL_SHAPE_TOP_CRYSTAL = Block.box(6.5, 6.0, 6.5, 9.5, 26.0, 9.5);

        VOXEL_SHAPE_TOP_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_TOP_DAIS,
                VOXEL_SHAPE_TOP_CRYSTAL
        );
    }
}