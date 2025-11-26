package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity;
import com.aranaira.magichem.block.entity.routers.MirrorLabyrinthRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.MirrorLabyrinthRouterType;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_MIRROR_LABYRINTH;
import static com.aranaira.magichem.foundation.enums.MirrorLabyrinthRouterType.*;

public class MirrorLabyrinthRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public static final VoxelShape
        VOXEL_SHAPE_DEFAULT, VOXEL_SHAPE_TOP_HALF, VOXEL_SHAPE_BOTTOM_HALF,

        VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH,
        VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_WEST,

        VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_WEST,
        VOXEL_SHAPE_BACK_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_BACK_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_BACK_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_BACK_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_LEFT_BASE_NORTH, VOXEL_SHAPE_LEFT_BODY_NORTH,
        VOXEL_SHAPE_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_LEFT_AGGREGATE_WEST,

        VOXEL_SHAPE_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_RIGHT_AGGREGATE_WEST,
        VOXEL_SHAPE_BACK_AGGREGATE_NORTH, VOXEL_SHAPE_BACK_AGGREGATE_EAST, VOXEL_SHAPE_BACK_AGGREGATE_SOUTH, VOXEL_SHAPE_BACK_AGGREGATE_WEST;


    public MirrorLabyrinthRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, ROUTER_TYPE_MIRROR_LABYRINTH);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MirrorLabyrinthRouterBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof MirrorLabyrinthRouterBlockEntity router) {
            MirrorLabyrinthBlockEntity master = router.getMaster();
            pPlayer.swing(InteractionHand.MAIN_HAND);
            if(master != null) return master.getBlockState().getBlock().use(master.getBlockState(), pLevel, master.getBlockPos(), pPlayer, pHand, pHit);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.MIRROR_LABYRINTH.get());
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be != null) {
            if(be instanceof MirrorLabyrinthRouterBlockEntity router && router.getMaster() != null) {
                router.getMaster().checkPaused();
            }
        }
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    public static int mapRouterTypeToInt(MirrorLabyrinthRouterType pRouterType) {
        if(pRouterType == null)
            return 0;

        return switch(pRouterType) {
            case DAIS -> 1;
            case CENTER -> 2;
            case LEFT_FRONT -> 3;
            case LEFT -> 4;
            case LEFT_BACK -> 5;
            case CENTER_BACK -> 6;
            case RIGHT_BACK -> 7;
            case RIGHT -> 8;
            case RIGHT_FRONT -> 9;
            case CONSTRUCT_LOWER -> 10;
            case CONSTRUCT_UPPER -> 11;
            case MATRIX_LOWER -> 12;
            case MATRIX_UPPER -> 13;
            default -> 0;
        };
    }

    public static MirrorLabyrinthRouterType unmapRouterTypeFromInt(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> DAIS;
            case 2 -> CENTER;
            case 3 -> LEFT_FRONT;
            case 4 -> LEFT;
            case 5 -> LEFT_BACK;
            case 6 -> CENTER_BACK;
            case 7 -> RIGHT_BACK;
            case 8 -> RIGHT;
            case 9 -> RIGHT_FRONT;
            case 10 -> CONSTRUCT_LOWER;
            case 11 -> CONSTRUCT_UPPER;
            case 12 -> MATRIX_LOWER;
            case 13 -> MATRIX_UPPER;
            default -> NONE;
        };
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockState state = pLevel.getBlockState(pPos);

        if (state.getBlock() == BlockRegistry.MIRROR_LABYRINTH_ROUTER.get()) {
            MirrorLabyrinthRouterType routerType = unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_MIRROR_LABYRINTH));
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            //Again, switch statements always default here and I have no idea why
            if (routerType == LEFT_FRONT) {
                if (facing == Direction.NORTH) return      VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return  VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return  VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_WEST;
            }
            else if (routerType == LEFT_BACK) {
                if (facing == Direction.NORTH) return      VOXEL_SHAPE_BACK_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return  VOXEL_SHAPE_BACK_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_BACK_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return  VOXEL_SHAPE_BACK_LEFT_AGGREGATE_WEST;
            }
            else if (routerType == RIGHT_FRONT) {
                if (facing == Direction.NORTH) return      VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return  VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return  VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_WEST;
            }
            else if (routerType == RIGHT_BACK) {
                if (facing == Direction.NORTH) return      VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return  VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return  VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_WEST;
            }
            else if (routerType == LEFT) {
                if (facing == Direction.NORTH) return      VOXEL_SHAPE_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return  VOXEL_SHAPE_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return  VOXEL_SHAPE_LEFT_AGGREGATE_WEST;
            }
            else if (routerType == RIGHT) {
                if (facing == Direction.NORTH) return      VOXEL_SHAPE_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return  VOXEL_SHAPE_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return  VOXEL_SHAPE_RIGHT_AGGREGATE_WEST;
            }
            else if (routerType == CENTER_BACK) {
                if (facing == Direction.NORTH) return      VOXEL_SHAPE_BACK_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return  VOXEL_SHAPE_BACK_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_BACK_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return  VOXEL_SHAPE_BACK_AGGREGATE_WEST;
            }
            else if (routerType == CONSTRUCT_LOWER || routerType == MATRIX_LOWER) {
                return VOXEL_SHAPE_TOP_HALF;
            }
            else if (routerType == CENTER) {
                return VOXEL_SHAPE_BOTTOM_HALF;
            }
        }

        return VOXEL_SHAPE_DEFAULT;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_MIRROR_LABYRINTH)) == DAIS ? 15 : 0;
    }

    static {
        VOXEL_SHAPE_DEFAULT = Block.box(0,0,0,16,16,16);
        VOXEL_SHAPE_TOP_HALF = Block.box(0,8,0,16,16,16);
        VOXEL_SHAPE_BOTTOM_HALF = Block.box(0,0,0,16,8,16);

        //FRONT LEFT
        {
            VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH = Block.box(0, 0, 0, 16, 7, 16);
            VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH = Block.box(8.1, 7, 0, 16, 16, 8.1);

            VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH,
                    VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH
            );

            VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 1)
            );

            VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 2)
            );

            VOXEL_SHAPE_FRONT_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 3)
            );
        }

        //FRONT RIGHT
        {
            VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 3)
            );

            VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_EAST = Shapes.or(
                    VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH,
                    VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH
            );

            VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 1)
            );

            VOXEL_SHAPE_FRONT_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 2)
            );
        }

        //BACK RIGHT
        {
            VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 2)
            );

            VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 3)
            );

            VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH,
                    VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH
            );

            VOXEL_SHAPE_BACK_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 1)
            );
        }

        //BACK LEFT
        {
            VOXEL_SHAPE_BACK_LEFT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 1)
            );

            VOXEL_SHAPE_BACK_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 2)
            );

            VOXEL_SHAPE_BACK_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH, 3)
            );

            VOXEL_SHAPE_BACK_LEFT_AGGREGATE_WEST = Shapes.or(
                    VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH,
                    VOXEL_SHAPE_FRONT_LEFT_BODY_NORTH
            );
        }

        //LEFT
        {
            VOXEL_SHAPE_LEFT_BASE_NORTH = Block.box(0, 0, 0, 16, 7, 16);
            VOXEL_SHAPE_LEFT_BODY_NORTH = Block.box(7.1, 7, 0, 16, 16, 16);

            VOXEL_SHAPE_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_LEFT_BASE_NORTH,
                    VOXEL_SHAPE_LEFT_BODY_NORTH
            );

            VOXEL_SHAPE_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 1)
            );

            VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 2)
            );

            VOXEL_SHAPE_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 3)
            );
        }

        //RIGHT
        {
            VOXEL_SHAPE_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 2)
            );

            VOXEL_SHAPE_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 3)
            );

            VOXEL_SHAPE_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    VOXEL_SHAPE_LEFT_BASE_NORTH,
                    VOXEL_SHAPE_LEFT_BODY_NORTH
            );

            VOXEL_SHAPE_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 1)
            );
        }

        //BACK
        {
            VOXEL_SHAPE_BACK_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 1)
            );

            VOXEL_SHAPE_BACK_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 2)
            );

            VOXEL_SHAPE_BACK_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 3)
            );

            VOXEL_SHAPE_BACK_AGGREGATE_WEST = Shapes.or(
                    VOXEL_SHAPE_LEFT_BASE_NORTH,
                    VOXEL_SHAPE_LEFT_BODY_NORTH
            );
        }
    }
}
