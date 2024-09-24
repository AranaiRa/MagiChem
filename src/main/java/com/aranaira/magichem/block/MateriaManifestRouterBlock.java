package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.MateriaManifestBlockEntity;
import com.aranaira.magichem.block.entity.routers.BaseActuatorRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.MateriaManifestRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class MateriaManifestRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public MateriaManifestRouterBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    public static final VoxelShape
        VOXEL_SHAPE_ERROR, VOXEL_SHAPE_LEGS, VOXEL_SHAPE_TOP_NORTH, VOXEL_SHAPE_BACKBOARD_NORTH,

        VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_WEST;

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction facing = pState.getValue(FACING);
        if(facing == Direction.NORTH) {
            return VOXEL_SHAPE_AGGREGATE_NORTH;
        } else if(facing == Direction.EAST) {
            return VOXEL_SHAPE_AGGREGATE_EAST;
        } else if(facing == Direction.SOUTH) {
            return VOXEL_SHAPE_AGGREGATE_SOUTH;
        } else if(facing == Direction.WEST) {
            return VOXEL_SHAPE_AGGREGATE_WEST;
        }
        return VOXEL_SHAPE_ERROR;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof MateriaManifestRouterBlockEntity mmrbe) {
            BlockEntity master = mmrbe.getMaster();
            pPlayer.swing(InteractionHand.MAIN_HAND);
            return master.getBlockState().getBlock().use(master.getBlockState(), pLevel, master.getBlockPos(), pPlayer, pHand, pHit);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MateriaManifestRouterBlockEntity(pPos, pState);
    }

    static {
        VOXEL_SHAPE_ERROR = Block.box(2, 2, 2, 14, 14, 14);

        VOXEL_SHAPE_LEGS = Block.box(4.5, 0, 4.5, 11.5, 2, 11.5);
        VOXEL_SHAPE_TOP_NORTH = Block.box(3, 1, 2, 13, 5, 14);
        VOXEL_SHAPE_BACKBOARD_NORTH = Block.box(2, 5, 2.5, 14, 13, 4.5);

        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_LEGS,
                VOXEL_SHAPE_TOP_NORTH,
                VOXEL_SHAPE_BACKBOARD_NORTH);

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                VOXEL_SHAPE_LEGS,
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TOP_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACKBOARD_NORTH, 1));

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                VOXEL_SHAPE_LEGS,
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TOP_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACKBOARD_NORTH, 2));

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                VOXEL_SHAPE_LEGS,
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TOP_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACKBOARD_NORTH, 3));

    }
}
