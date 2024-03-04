package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.routers.ActuatorFireRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.ActuatorWaterRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.BaseActuatorRouterBlockEntity;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BaseActuatorRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public BaseActuatorRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    public static VoxelShape
            VOXEL_SHAPE_WATER_PIPES_NORTH, VOXEL_SHAPE_WATER_TUBE_BODY_NORTH, VOXEL_SHAPE_WATER_TUBE_CAP_NORTH,
            VOXEL_SHAPE_FIRE_LEFT_NUB, VOXEL_SHAPE_FIRE_LEFT_HORIZONTAL, VOXEL_SHAPE_FIRE_LEFT_VERTICAL, VOXEL_SHAPE_FIRE_RIGHT_NUB, VOXEL_SHAPE_FIRE_RIGHT_HORIZONTAL, VOXEL_SHAPE_FIRE_RIGHT_VERTICAL, VOXEL_SHAPE_FIRE_CENTER,

            VOXEL_SHAPE_WATER_AGGREGATE_NORTH, VOXEL_SHAPE_WATER_AGGREGATE_EAST, VOXEL_SHAPE_WATER_AGGREGATE_SOUTH, VOXEL_SHAPE_WATER_AGGREGATE_WEST,
            VOXEL_SHAPE_FIRE_AGGREGATE_NORTH, VOXEL_SHAPE_FIRE_AGGREGATE_EAST, VOXEL_SHAPE_FIRE_AGGREGATE_SOUTH, VOXEL_SHAPE_FIRE_AGGREGATE_WEST;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BaseActuatorRouterBlockEntity(pPos, pState);
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
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be != null) {
            Direction facing = ((BaseActuatorRouterBlockEntity)be).getFacing();

            if(be instanceof ActuatorWaterRouterBlockEntity) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_WATER_AGGREGATE_NORTH;
                if (facing == Direction.EAST) return VOXEL_SHAPE_WATER_AGGREGATE_EAST;
                if (facing == Direction.SOUTH) return VOXEL_SHAPE_WATER_AGGREGATE_SOUTH;
                if (facing == Direction.WEST) return VOXEL_SHAPE_WATER_AGGREGATE_WEST;
            } else if(be instanceof ActuatorFireRouterBlockEntity) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_FIRE_AGGREGATE_NORTH;
                if (facing == Direction.EAST) return VOXEL_SHAPE_FIRE_AGGREGATE_EAST;
                if (facing == Direction.SOUTH) return VOXEL_SHAPE_FIRE_AGGREGATE_SOUTH;
                if (facing == Direction.WEST) return VOXEL_SHAPE_FIRE_AGGREGATE_WEST;
            }
        }
        return super.getShape(pState, pLevel, pPos, pContext);
    }

    static {
        VOXEL_SHAPE_FIRE_LEFT_NUB = Block.box(2, 0, 6, 4, 1, 8);
        VOXEL_SHAPE_FIRE_LEFT_HORIZONTAL = Block.box(4, 0, 2, 6, 1, 8);
        VOXEL_SHAPE_FIRE_LEFT_VERTICAL = Block.box(4, 0, 0, 6, 16, 2);
        VOXEL_SHAPE_FIRE_RIGHT_NUB = Block.box(12, 0, 6, 14, 1, 8);
        VOXEL_SHAPE_FIRE_RIGHT_HORIZONTAL = Block.box(10, 0, 2, 12, 1, 8);
        VOXEL_SHAPE_FIRE_RIGHT_VERTICAL = Block.box(10, 0, 0, 12, 16, 2);
        VOXEL_SHAPE_FIRE_CENTER = Block.box(6, 0, 1, 10, 14, 5);

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
    }
}