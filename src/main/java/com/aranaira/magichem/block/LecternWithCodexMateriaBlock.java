package com.aranaira.magichem.block;

import com.mna.api.blocks.WizardLabBlock;
import com.mna.api.tools.BlockUtilities;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LecternWithCodexMateriaBlock extends Block implements INoCreativeTab {
    private static final VoxelShape OFFSET_SHAPE_NORTH = Shapes.or(Block.box(3.0D, 0.0D, 3.0D, 13.0D, 3.0D, 13.0D), new VoxelShape[]{Block.box(2.0D, 0.0D, 13.0D, 14.0D, 3.0D, 15.0D), Block.box(3.5D, 3.0D, 13.5D, 4.5D, 6.0D, 14.5D), Block.box(11.5D, 3.0D, 13.5D, 12.5D, 6.0D, 14.5D), Block.box(2.5D, 4.0D, 13.0D, 13.5D, 5.0D, 14.0D)});
    private static final VoxelShape OFFSET_SHAPE_EAST;
    private static final VoxelShape OFFSET_SHAPE_SOUTH;
    private static final VoxelShape OFFSET_SHAPE_WEST;
    private static final VoxelShape OFFSET_WITH_BOOK_SHAPE_NORTH;
    private static final VoxelShape OFFSET_WITH_BOOK_SHAPE_EAST;
    private static final VoxelShape OFFSET_WITH_BOOK_SHAPE_SOUTH;
    private static final VoxelShape OFFSET_WITH_BOOK_SHAPE_WEST;

    public LecternWithCodexMateriaBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WizardLabBlock.LEFT, WizardLabBlock.RIGHT, HorizontalDirectionalBlock.FACING);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch((Direction)pState.getValue(HorizontalDirectionalBlock.FACING)) {
            case EAST:
                return OFFSET_SHAPE_EAST;
            case NORTH:
                return OFFSET_SHAPE_NORTH;
            case SOUTH:
                return OFFSET_SHAPE_SOUTH;
            case WEST:
                return OFFSET_SHAPE_WEST;
            default:
                return Shapes.block();
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    static {
        OFFSET_SHAPE_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH);
        OFFSET_SHAPE_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH);
        OFFSET_SHAPE_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH);
        OFFSET_WITH_BOOK_SHAPE_NORTH = Shapes.or(Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 5.0D), new VoxelShape[]{Block.box(3.0D, 0.0D, 5.0D, 13.0D, 3.0D, 13.0D), Block.box(2.0D, 0.0D, 13.0D, 14.0D, 3.0D, 15.0D), Block.box(3.5D, 3.0D, 13.5D, 4.5D, 6.0D, 14.5D), Block.box(11.5D, 3.0D, 13.5D, 12.5D, 6.0D, 14.5D), Block.box(2.5D, 4.0D, 13.0D, 13.5D, 5.0D, 14.0D), Block.box(0.0D, 1.0D, 4.5D, 16.0D, 4.5D, 11.5D)});
        OFFSET_WITH_BOOK_SHAPE_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_WITH_BOOK_SHAPE_NORTH);
        OFFSET_WITH_BOOK_SHAPE_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_WITH_BOOK_SHAPE_NORTH);
        OFFSET_WITH_BOOK_SHAPE_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_WITH_BOOK_SHAPE_NORTH);
    }
}
