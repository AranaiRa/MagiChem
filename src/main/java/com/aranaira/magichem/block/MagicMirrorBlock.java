package com.aranaira.magichem.block;

import com.aranaira.magichem.util.MathHelper;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.USER_TIER_TYPE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class MagicMirrorBlock extends Block {

    public static final VoxelShape
            VOXEL_SHAPE_N, VOXEL_SHAPE_E, VOXEL_SHAPE_S, VOXEL_SHAPE_W,
            VOXEL_SHAPE_C_NS, VOXEL_SHAPE_C_EW, VOXEL_SHAPE_F_NS, VOXEL_SHAPE_F_EW;

    public MagicMirrorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, ATTACH_FACE);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        for(Direction direction : pContext.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState().setValue(ATTACH_FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).setValue(FACING, pContext.getHorizontalDirection());
            } else {
                blockstate = this.defaultBlockState().setValue(ATTACH_FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
                return blockstate;
            }
        }

        return null;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        AttachFace attachFace = pState.getValue(ATTACH_FACE);
        Direction facing = pState.getValue(FACING);

        if(attachFace == AttachFace.WALL) {
            if(facing == Direction.NORTH) return VOXEL_SHAPE_N;
            else if(facing == Direction.EAST) return VOXEL_SHAPE_E;
            else if(facing == Direction.SOUTH) return VOXEL_SHAPE_S;
            else if(facing == Direction.WEST) return VOXEL_SHAPE_W;
        }
        else if(attachFace == AttachFace.CEILING) {
            if(facing == Direction.NORTH || facing == Direction.SOUTH) return VOXEL_SHAPE_C_NS;
            else if(facing == Direction.EAST || facing == Direction.WEST) return VOXEL_SHAPE_C_EW;
        }
        else if(attachFace == AttachFace.FLOOR) {
            if(facing == Direction.NORTH || facing == Direction.SOUTH) return VOXEL_SHAPE_F_NS;
            else if(facing == Direction.EAST || facing == Direction.WEST) return VOXEL_SHAPE_F_EW;
        }

        return Block.box(0, 0, 0, 1, 1, 1);
    }

    static {
        VOXEL_SHAPE_N = Block.box(2, 0, 14, 14, 16, 16);
        VOXEL_SHAPE_E = MathHelper.rotateVoxelShape(VOXEL_SHAPE_N, 1);
        VOXEL_SHAPE_S = MathHelper.rotateVoxelShape(VOXEL_SHAPE_N, 2);
        VOXEL_SHAPE_W = MathHelper.rotateVoxelShape(VOXEL_SHAPE_N, 3);

        VOXEL_SHAPE_C_NS = Block.box(2, 14, 0, 14, 16, 16);
        VOXEL_SHAPE_C_EW = MathHelper.rotateVoxelShape(VOXEL_SHAPE_C_NS, 1);

        VOXEL_SHAPE_F_NS = Block.box(2, 0, 0, 14, 2, 16);
        VOXEL_SHAPE_F_EW = MathHelper.rotateVoxelShape(VOXEL_SHAPE_F_NS, 1);
    }
}
