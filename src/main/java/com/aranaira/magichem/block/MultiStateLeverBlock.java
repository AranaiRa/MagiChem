package com.aranaira.magichem.block;

import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.LEVER_SIGNAL;

public class MultiStateLeverBlock extends FaceAttachedHorizontalDirectionalBlock {
    private static final VoxelShape
        VOXEL_SHAPE_2S_GROUND_NS, VOXEL_SHAPE_2S_GROUND_EW, VOXEL_SHAPE_2S_CEILING_NS, VOXEL_SHAPE_2S_CEILING_EW,
        VOXEL_SHAPE_2S_WALL_N, VOXEL_SHAPE_2S_WALL_E, VOXEL_SHAPE_2S_WALL_S, VOXEL_SHAPE_2S_WALL_W,

        VOXEL_SHAPE_4S_GROUND_N, VOXEL_SHAPE_4S_GROUND_E, VOXEL_SHAPE_4S_GROUND_S, VOXEL_SHAPE_4S_GROUND_W,
        VOXEL_SHAPE_4S_CEILING_N, VOXEL_SHAPE_4S_CEILING_E, VOXEL_SHAPE_4S_CEILING_S, VOXEL_SHAPE_4S_CEILING_W,
        VOXEL_SHAPE_4S_WALL_N, VOXEL_SHAPE_4S_WALL_E, VOXEL_SHAPE_4S_WALL_S, VOXEL_SHAPE_4S_WALL_W,

        VOXEL_SHAPE_5S_GROUND, VOXEL_SHAPE_5S_CEILING,
        VOXEL_SHAPE_5S_WALL_N, VOXEL_SHAPE_5S_WALL_E, VOXEL_SHAPE_5S_WALL_S, VOXEL_SHAPE_5S_WALL_W,

        VOXEL_SHAPE_6S_GROUND_N, VOXEL_SHAPE_6S_GROUND_E, VOXEL_SHAPE_6S_GROUND_S, VOXEL_SHAPE_6S_GROUND_W,
        VOXEL_SHAPE_6S_CEILING_N, VOXEL_SHAPE_6S_CEILING_E, VOXEL_SHAPE_6S_CEILING_S, VOXEL_SHAPE_6S_CEILING_W,
        VOXEL_SHAPE_6S_WALL_N, VOXEL_SHAPE_6S_WALL_E, VOXEL_SHAPE_6S_WALL_S, VOXEL_SHAPE_6S_WALL_W;
    private final int maxSignal;

    public MultiStateLeverBlock(int maxSignal, BlockBehaviour.Properties pProperties) {
        super(pProperties);
        this.maxSignal = maxSignal;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(BlockStateProperties.ATTACH_FACE);
        pBuilder.add(FACING);
        pBuilder.add(LEVER_SIGNAL);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        int oldSignal = pState.getValue(LEVER_SIGNAL);
        int value = pPlayer.isCrouching() ?
                oldSignal <= 0 ? maxSignal : oldSignal - 1 :
                oldSignal >= maxSignal ? 0 : oldSignal + 1;

        if(pLevel.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            float f = value * 0.1f + 0.5f;
            pLevel.playSound((Player)null, pPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
            BlockState newState = pState.setValue(LEVER_SIGNAL, value);
            pLevel.setBlock(pPos, newState, 3);
            pLevel.sendBlockUpdated(pPos, pState, newState, 3);
            pLevel.updateNeighborsAt(pPos, this);
            pLevel.updateNeighborsAt(pPos.relative(getConnectedDirection(pState).getOpposite()), this);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return pState.getValue(LEVER_SIGNAL);
    }

    @Override
    public int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return pState.getValue(LEVER_SIGNAL);
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction facing = pState.getValue(FACING);
        AttachFace vertical = pState.getValue(FACE);

        if(maxSignal == 1 || maxSignal == 2) {
            if(vertical == AttachFace.FLOOR) {
                return (facing == Direction.NORTH || facing == Direction.SOUTH) ? VOXEL_SHAPE_2S_GROUND_NS : VOXEL_SHAPE_2S_GROUND_EW;
            } else if(vertical == AttachFace.CEILING) {
                return (facing == Direction.NORTH || facing == Direction.SOUTH) ? VOXEL_SHAPE_2S_CEILING_NS : VOXEL_SHAPE_2S_CEILING_EW;
            } else if(vertical == AttachFace.WALL) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_2S_WALL_N;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_2S_WALL_E;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_2S_WALL_S;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_2S_WALL_W;
            }
        } else if(maxSignal == 3) {
            if(vertical == AttachFace.FLOOR) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_4S_GROUND_N;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_4S_GROUND_E;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_4S_GROUND_S;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_4S_GROUND_W;
            } else if(vertical == AttachFace.CEILING) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_4S_CEILING_N;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_4S_CEILING_E;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_4S_CEILING_S;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_4S_CEILING_W;
            } else if(vertical == AttachFace.WALL) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_4S_WALL_N;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_4S_WALL_E;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_4S_WALL_S;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_4S_WALL_W;
            }
        } else if(maxSignal == 4) {
            if(vertical == AttachFace.FLOOR) {
                return VOXEL_SHAPE_5S_GROUND;
            } else if(vertical == AttachFace.CEILING) {
                return VOXEL_SHAPE_5S_CEILING;
            } else if(vertical == AttachFace.WALL) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_5S_WALL_N;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_5S_WALL_E;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_5S_WALL_S;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_5S_WALL_W;
            }
        } else if(maxSignal == 5) {
            if(vertical == AttachFace.FLOOR) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_6S_GROUND_N;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_6S_GROUND_E;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_6S_GROUND_S;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_6S_GROUND_W;
            } else if(vertical == AttachFace.CEILING) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_6S_CEILING_N;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_6S_CEILING_E;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_6S_CEILING_S;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_6S_CEILING_W;
            } else if(vertical == AttachFace.WALL) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_6S_WALL_N;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_6S_WALL_E;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_6S_WALL_S;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_6S_WALL_W;
            }
        }

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    static {
        VOXEL_SHAPE_2S_GROUND_NS = Block.box(5.5, 0, 3.5, 10.5, 6.5, 12.5);
        VOXEL_SHAPE_2S_GROUND_EW = MathHelper.rotateVoxelShape(VOXEL_SHAPE_2S_GROUND_NS, 1);
        VOXEL_SHAPE_2S_CEILING_NS = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_2S_GROUND_NS);
        VOXEL_SHAPE_2S_CEILING_EW = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_2S_GROUND_EW);
        VOXEL_SHAPE_2S_WALL_S = Block.box(5.5, 3.5, 0, 10.5, 12.5, 6.5);
        VOXEL_SHAPE_2S_WALL_W = MathHelper.rotateVoxelShape(VOXEL_SHAPE_2S_WALL_S, 1);
        VOXEL_SHAPE_2S_WALL_N = MathHelper.rotateVoxelShape(VOXEL_SHAPE_2S_WALL_S, 2);
        VOXEL_SHAPE_2S_WALL_E = MathHelper.rotateVoxelShape(VOXEL_SHAPE_2S_WALL_S, 3);

        VOXEL_SHAPE_4S_GROUND_N = Block.box(4.028, 0, 5.418, 11.972, 6.5, 12.297);
        VOXEL_SHAPE_4S_GROUND_E = MathHelper.rotateVoxelShape(VOXEL_SHAPE_4S_GROUND_N, 1);
        VOXEL_SHAPE_4S_GROUND_S = MathHelper.rotateVoxelShape(VOXEL_SHAPE_4S_GROUND_N, 2);
        VOXEL_SHAPE_4S_GROUND_W = MathHelper.rotateVoxelShape(VOXEL_SHAPE_4S_GROUND_N, 3);
        VOXEL_SHAPE_4S_CEILING_N = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_4S_GROUND_N);
        VOXEL_SHAPE_4S_CEILING_E = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_4S_GROUND_E);
        VOXEL_SHAPE_4S_CEILING_S = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_4S_GROUND_S);
        VOXEL_SHAPE_4S_CEILING_W = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_4S_GROUND_W);
        VOXEL_SHAPE_4S_WALL_S = Block.box(4.0285, 3.704, 0, 11.972, 10.582, 6.5);
        VOXEL_SHAPE_4S_WALL_W = MathHelper.rotateVoxelShape(VOXEL_SHAPE_4S_WALL_S, 1);
        VOXEL_SHAPE_4S_WALL_N = MathHelper.rotateVoxelShape(VOXEL_SHAPE_4S_WALL_S, 2);
        VOXEL_SHAPE_4S_WALL_E = MathHelper.rotateVoxelShape(VOXEL_SHAPE_4S_WALL_S, 3);

        VOXEL_SHAPE_5S_GROUND = Block.box(3.492, 0, 3.492, 12.508, 6.5, 12.508);
        VOXEL_SHAPE_5S_CEILING = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_5S_GROUND);
        VOXEL_SHAPE_5S_WALL_S = Block.box(3.492, 3.492, 0, 12.508, 12.508, 6.5);
        VOXEL_SHAPE_5S_WALL_W = MathHelper.rotateVoxelShape(VOXEL_SHAPE_5S_WALL_S, 1);
        VOXEL_SHAPE_5S_WALL_N = MathHelper.rotateVoxelShape(VOXEL_SHAPE_5S_WALL_S, 2);
        VOXEL_SHAPE_5S_WALL_E = MathHelper.rotateVoxelShape(VOXEL_SHAPE_5S_WALL_S, 3);

        VOXEL_SHAPE_6S_GROUND_N = Block.box(3.379, 0, 3.907, 12.621, 6.5, 12.696);
        VOXEL_SHAPE_6S_GROUND_E = MathHelper.rotateVoxelShape(VOXEL_SHAPE_6S_GROUND_N, 1);
        VOXEL_SHAPE_6S_GROUND_S = MathHelper.rotateVoxelShape(VOXEL_SHAPE_6S_GROUND_N, 2);
        VOXEL_SHAPE_6S_GROUND_W = MathHelper.rotateVoxelShape(VOXEL_SHAPE_6S_GROUND_N, 3);
        VOXEL_SHAPE_6S_CEILING_N = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_6S_GROUND_N);
        VOXEL_SHAPE_6S_CEILING_E = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_6S_GROUND_E);
        VOXEL_SHAPE_6S_CEILING_S = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_6S_GROUND_S);
        VOXEL_SHAPE_6S_CEILING_W = MathHelper.flipVoxelShapeY(VOXEL_SHAPE_6S_GROUND_W);
        VOXEL_SHAPE_6S_WALL_S = Block.box(3.379, 3.304, 0, 12.621, 12.093, 6.5);
        VOXEL_SHAPE_6S_WALL_W = MathHelper.rotateVoxelShape(VOXEL_SHAPE_6S_WALL_S, 1);
        VOXEL_SHAPE_6S_WALL_N = MathHelper.rotateVoxelShape(VOXEL_SHAPE_6S_WALL_S, 2);
        VOXEL_SHAPE_6S_WALL_E = MathHelper.rotateVoxelShape(VOXEL_SHAPE_6S_WALL_S, 3);
    }
}
