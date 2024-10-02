package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.GrandCircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.routers.CircleFabricationRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandCircleFabricationRouterBlockEntity;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class GrandCircleFabricationRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public GrandCircleFabricationRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    public static VoxelShape
            VOXEL_SHAPE_CIRCLE_NORTH, VOXEL_SHAPE_STONE_NORTH,
            VOXEL_SHAPE_CIRCLE_NORTHEAST, VOXEL_SHAPE_STONE_NORTHEAST,
            VOXEL_SHAPE_CIRCLE_EAST, VOXEL_SHAPE_STONE_EAST,
            VOXEL_SHAPE_CIRCLE_SOUTHEAST, VOXEL_SHAPE_STONE_SOUTHEAST,
            VOXEL_SHAPE_CIRCLE_SOUTH,

            VOXEL_SHAPE_AGGREGATE_0_NORTH, VOXEL_SHAPE_AGGREGATE_0_EAST, VOXEL_SHAPE_AGGREGATE_0_SOUTH, VOXEL_SHAPE_AGGREGATE_0_WEST,
            VOXEL_SHAPE_AGGREGATE_1_NORTH, VOXEL_SHAPE_AGGREGATE_1_EAST, VOXEL_SHAPE_AGGREGATE_1_SOUTH, VOXEL_SHAPE_AGGREGATE_1_WEST,
            VOXEL_SHAPE_AGGREGATE_2_NORTH, VOXEL_SHAPE_AGGREGATE_2_EAST, VOXEL_SHAPE_AGGREGATE_2_SOUTH, VOXEL_SHAPE_AGGREGATE_2_WEST,
            VOXEL_SHAPE_AGGREGATE_3_NORTH, VOXEL_SHAPE_AGGREGATE_3_EAST, VOXEL_SHAPE_AGGREGATE_3_SOUTH, VOXEL_SHAPE_AGGREGATE_3_WEST,
            VOXEL_SHAPE_AGGREGATE_4_NORTH, VOXEL_SHAPE_AGGREGATE_4_EAST, VOXEL_SHAPE_AGGREGATE_4_SOUTH, VOXEL_SHAPE_AGGREGATE_4_WEST,
            VOXEL_SHAPE_AGGREGATE_5_NORTH, VOXEL_SHAPE_AGGREGATE_5_EAST, VOXEL_SHAPE_AGGREGATE_5_SOUTH, VOXEL_SHAPE_AGGREGATE_5_WEST,
            VOXEL_SHAPE_AGGREGATE_6_NORTH, VOXEL_SHAPE_AGGREGATE_6_EAST, VOXEL_SHAPE_AGGREGATE_6_SOUTH, VOXEL_SHAPE_AGGREGATE_6_WEST,
            VOXEL_SHAPE_AGGREGATE_7_NORTH, VOXEL_SHAPE_AGGREGATE_7_EAST, VOXEL_SHAPE_AGGREGATE_7_SOUTH, VOXEL_SHAPE_AGGREGATE_7_WEST;
    public static final int
            ROUTER_TYPE_NORTH = 0, ROUTER_TYPE_NORTHEAST = 1, ROUTER_TYPE_EAST = 2, ROUTER_TYPE_SOUTHEAST = 3,
            ROUTER_TYPE_SOUTH = 4, ROUTER_TYPE_SOUTHWEST = 5, ROUTER_TYPE_WEST = 6, ROUTER_TYPE_NORTHWEST = 7;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GrandCircleFabricationRouterBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int routerType = pState.getValue(ROUTER_TYPE_GRAND_CIRCLE_FABRICATION);
        Direction facing = pState.getValue(FACING);

        //Again, switch statements always default here and I have no idea why
        if(facing == Direction.NORTH) {
            if (routerType == ROUTER_TYPE_NORTH) {
                return VOXEL_SHAPE_AGGREGATE_0_NORTH;
            } else if (routerType == ROUTER_TYPE_NORTHEAST) {
                return VOXEL_SHAPE_AGGREGATE_1_NORTH;
            } else if (routerType == ROUTER_TYPE_EAST) {
                return VOXEL_SHAPE_AGGREGATE_2_NORTH;
            } else if (routerType == ROUTER_TYPE_SOUTHEAST) {
                return VOXEL_SHAPE_AGGREGATE_3_NORTH;
            } else if (routerType == ROUTER_TYPE_SOUTH) {
                return VOXEL_SHAPE_AGGREGATE_4_NORTH;
            } else if (routerType == ROUTER_TYPE_SOUTHWEST) {
                return VOXEL_SHAPE_AGGREGATE_5_NORTH;
            } else if (routerType == ROUTER_TYPE_WEST) {
                return VOXEL_SHAPE_AGGREGATE_6_NORTH;
            } else if (routerType == ROUTER_TYPE_NORTHWEST) {
                return VOXEL_SHAPE_AGGREGATE_7_NORTH;
            }
        } else if(facing == Direction.EAST) {
            if (routerType == ROUTER_TYPE_NORTH) {
                return VOXEL_SHAPE_AGGREGATE_0_EAST;
            } else if (routerType == ROUTER_TYPE_NORTHEAST) {
                return VOXEL_SHAPE_AGGREGATE_1_EAST;
            } else if (routerType == ROUTER_TYPE_EAST) {
                return VOXEL_SHAPE_AGGREGATE_2_EAST;
            } else if (routerType == ROUTER_TYPE_SOUTHEAST) {
                return VOXEL_SHAPE_AGGREGATE_3_EAST;
            } else if (routerType == ROUTER_TYPE_SOUTH) {
                return VOXEL_SHAPE_AGGREGATE_4_EAST;
            } else if (routerType == ROUTER_TYPE_SOUTHWEST) {
                return VOXEL_SHAPE_AGGREGATE_5_EAST;
            } else if (routerType == ROUTER_TYPE_WEST) {
                return VOXEL_SHAPE_AGGREGATE_6_EAST;
            } else if (routerType == ROUTER_TYPE_NORTHWEST) {
                return VOXEL_SHAPE_AGGREGATE_7_EAST;
            }
        } else if(facing == Direction.SOUTH) {
            if (routerType == ROUTER_TYPE_NORTH) {
                return VOXEL_SHAPE_AGGREGATE_0_SOUTH;
            } else if (routerType == ROUTER_TYPE_NORTHEAST) {
                return VOXEL_SHAPE_AGGREGATE_1_SOUTH;
            } else if (routerType == ROUTER_TYPE_EAST) {
                return VOXEL_SHAPE_AGGREGATE_2_SOUTH;
            } else if (routerType == ROUTER_TYPE_SOUTHEAST) {
                return VOXEL_SHAPE_AGGREGATE_3_SOUTH;
            } else if (routerType == ROUTER_TYPE_SOUTH) {
                return VOXEL_SHAPE_AGGREGATE_4_SOUTH;
            } else if (routerType == ROUTER_TYPE_SOUTHWEST) {
                return VOXEL_SHAPE_AGGREGATE_5_SOUTH;
            } else if (routerType == ROUTER_TYPE_WEST) {
                return VOXEL_SHAPE_AGGREGATE_6_SOUTH;
            } else if (routerType == ROUTER_TYPE_NORTHWEST) {
                return VOXEL_SHAPE_AGGREGATE_7_SOUTH;
            }
        } else if(facing == Direction.WEST) {
            if (routerType == ROUTER_TYPE_NORTH) {
                return VOXEL_SHAPE_AGGREGATE_0_WEST;
            } else if (routerType == ROUTER_TYPE_NORTHEAST) {
                return VOXEL_SHAPE_AGGREGATE_1_WEST;
            } else if (routerType == ROUTER_TYPE_EAST) {
                return VOXEL_SHAPE_AGGREGATE_2_WEST;
            } else if (routerType == ROUTER_TYPE_SOUTHEAST) {
                return VOXEL_SHAPE_AGGREGATE_3_WEST;
            } else if (routerType == ROUTER_TYPE_SOUTH) {
                return VOXEL_SHAPE_AGGREGATE_4_WEST;
            } else if (routerType == ROUTER_TYPE_SOUTHWEST) {
                return VOXEL_SHAPE_AGGREGATE_5_WEST;
            } else if (routerType == ROUTER_TYPE_WEST) {
                return VOXEL_SHAPE_AGGREGATE_6_WEST;
            } else if (routerType == ROUTER_TYPE_NORTHWEST) {
                return VOXEL_SHAPE_AGGREGATE_7_WEST;
            }
        }

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof GrandCircleFabricationRouterBlockEntity gcfrbe) {
            GrandCircleFabricationBlockEntity master = gcfrbe.getMaster();
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ROUTER_TYPE_GRAND_CIRCLE_FABRICATION);
        pBuilder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.CIRCLE_FABRICATION.get());
    }

    static {
        VOXEL_SHAPE_CIRCLE_NORTH = Block.box(0, 0,  8, 16, 2, 16);
        VOXEL_SHAPE_STONE_NORTH = Block.box(5, 0, 0, 11, 13, 7);
        VOXEL_SHAPE_AGGREGATE_0_NORTH = Shapes.or(
                VOXEL_SHAPE_CIRCLE_NORTH,
                VOXEL_SHAPE_STONE_NORTH);
        VOXEL_SHAPE_AGGREGATE_0_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_NORTH, 1), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_NORTH, 1)});
        VOXEL_SHAPE_AGGREGATE_0_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_NORTH, 2), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_NORTH, 2)});
        VOXEL_SHAPE_AGGREGATE_0_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_NORTH, 3), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_NORTH, 3)});

        VOXEL_SHAPE_CIRCLE_NORTHEAST = Block.box(0, 0,  8, 8, 2, 16);
        VOXEL_SHAPE_STONE_NORTHEAST = Block.box(7.550, 0, 14.039, 15.443, 13, 16);
        VOXEL_SHAPE_AGGREGATE_1_NORTH = Shapes.or(
                VOXEL_SHAPE_CIRCLE_NORTHEAST,
                VOXEL_SHAPE_STONE_NORTHEAST);
        VOXEL_SHAPE_AGGREGATE_1_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_NORTHEAST, 1), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_NORTHEAST, 1)});
        VOXEL_SHAPE_AGGREGATE_1_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_NORTHEAST, 2), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_NORTHEAST, 2)});
        VOXEL_SHAPE_AGGREGATE_1_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_NORTHEAST, 3), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_NORTHEAST, 3)});

        VOXEL_SHAPE_AGGREGATE_7_NORTH = Shapes.or(
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_NORTHEAST),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_NORTHEAST));
        VOXEL_SHAPE_AGGREGATE_7_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_NORTHEAST), 1), new VoxelShape[]{
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_NORTHEAST), 1)});
        VOXEL_SHAPE_AGGREGATE_7_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_NORTHEAST), 2), new VoxelShape[]{
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_NORTHEAST), 2)});
        VOXEL_SHAPE_AGGREGATE_7_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_NORTHEAST), 3), new VoxelShape[]{
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_NORTHEAST), 3)});

        VOXEL_SHAPE_CIRCLE_EAST = Block.box(0, 0,  0, 8, 2, 16);
        VOXEL_SHAPE_STONE_EAST = Block.box(7.550, 0, 0, 15.443, 13, 5.291);
        VOXEL_SHAPE_AGGREGATE_2_NORTH = Shapes.or(
                VOXEL_SHAPE_CIRCLE_EAST,
                VOXEL_SHAPE_STONE_EAST);
        VOXEL_SHAPE_AGGREGATE_2_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_EAST, 1), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_EAST, 1)});
        VOXEL_SHAPE_AGGREGATE_2_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_EAST, 2), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_EAST, 2)});
        VOXEL_SHAPE_AGGREGATE_2_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_EAST, 3), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_EAST, 3)});

        VOXEL_SHAPE_AGGREGATE_6_NORTH = Shapes.or(
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_EAST),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_EAST));
        VOXEL_SHAPE_AGGREGATE_6_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_EAST), 1), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_EAST), 1)});
        VOXEL_SHAPE_AGGREGATE_6_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_EAST), 2), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_EAST), 2)});
        VOXEL_SHAPE_AGGREGATE_6_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_EAST), 3), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_EAST), 3)});

        VOXEL_SHAPE_CIRCLE_SOUTHEAST = Block.box(0, 0,  0, 8, 2, 8);
        VOXEL_SHAPE_STONE_SOUTHEAST = Block.box(0.153, 0, 4.578, 7.946, 13, 12.592);
        VOXEL_SHAPE_AGGREGATE_3_NORTH = Shapes.or(
                VOXEL_SHAPE_CIRCLE_SOUTHEAST,
                VOXEL_SHAPE_STONE_SOUTHEAST);
        VOXEL_SHAPE_AGGREGATE_3_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_SOUTHEAST, 1), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_SOUTHEAST, 1)});
        VOXEL_SHAPE_AGGREGATE_3_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_SOUTHEAST, 2), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_SOUTHEAST, 2)});
        VOXEL_SHAPE_AGGREGATE_3_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_SOUTHEAST, 3), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(VOXEL_SHAPE_STONE_SOUTHEAST, 3)});

        VOXEL_SHAPE_AGGREGATE_5_NORTH = Shapes.or(
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_SOUTHEAST),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_SOUTHEAST));
        VOXEL_SHAPE_AGGREGATE_5_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_SOUTHEAST), 1), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_SOUTHEAST), 1)});
        VOXEL_SHAPE_AGGREGATE_5_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_SOUTHEAST), 2), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_SOUTHEAST), 2)});
        VOXEL_SHAPE_AGGREGATE_5_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CIRCLE_SOUTHEAST), 3), new VoxelShape[]{
                        MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_STONE_SOUTHEAST), 3)});

        VOXEL_SHAPE_CIRCLE_SOUTH = Block.box(0, 0, 0, 16, 2, 8);
        VOXEL_SHAPE_AGGREGATE_4_NORTH = VOXEL_SHAPE_CIRCLE_SOUTH;
        VOXEL_SHAPE_AGGREGATE_4_EAST =
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_SOUTH, 1);
        VOXEL_SHAPE_AGGREGATE_4_SOUTH =
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_SOUTH, 2);
        VOXEL_SHAPE_AGGREGATE_4_WEST =
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CIRCLE_SOUTH, 3);
    }
}