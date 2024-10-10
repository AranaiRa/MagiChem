package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.GrandCircleFabricationBlockEntity;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
        registerDefaultState(this.defaultBlockState()
                .setValue(IS_EMITTING_LIGHT, false)
        );
    }

    public static VoxelShape
            VOXEL_SHAPE_OUTER_BASE_NORTH, VOXEL_SHAPE_INNER_BASE_NORTH, VOXEL_SHAPE_OUTER_BODY_NORTH, VOXEL_SHAPE_INNER_BODY_NORTH, VOXEL_SHAPE_PLATFORM_NORTH, VOXEL_SHAPE_MOUNT_NORTH,
            VOXEL_SHAPE_BASE_NORTHEAST, VOXEL_SHAPE_BODY_NORTHEAST,
            VOXEL_SHAPE_PLUG_NORTH,
            VOXEL_SHAPE_DAIS_SOUTH,

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
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(ROUTER_TYPE_GRAND_CIRCLE_FABRICATION) == 4 ? 15 : 0;
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
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        BlockState state = pLevel.getBlockState(pNeighborPos);

        if(state.hasProperty(BlockStateProperties.POWERED)) {
            boolean powered = state.getValue(BlockStateProperties.POWERED);
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if(be instanceof GrandCircleFabricationRouterBlockEntity gcfrbe) {
                if(gcfrbe.getMaster() != null)
                    gcfrbe.getMaster().setRedstonePaused(powered);
            }
        } else if(state.hasProperty(BlockStateProperties.POWER)) {
            int power = state.getValue(BlockStateProperties.POWER);
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if(be instanceof GrandCircleFabricationRouterBlockEntity gcfrbe) {
                if(gcfrbe.getMaster() != null)
                    gcfrbe.getMaster().setRedstonePaused(power > 0);
            }
        }

        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ROUTER_TYPE_GRAND_CIRCLE_FABRICATION);
        pBuilder.add(FACING);
        pBuilder.add(IS_EMITTING_LIGHT);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.GRAND_CIRCLE_FABRICATION.get());
    }

    static {
        VOXEL_SHAPE_OUTER_BASE_NORTH = Block.box(1, 0, 1, 15, 4, 16);
        VOXEL_SHAPE_INNER_BASE_NORTH = Block.box(0, 0, 8, 16, 4, 16);
        VOXEL_SHAPE_OUTER_BODY_NORTH = Block.box(2, 4, 2, 14, 9, 16);
        VOXEL_SHAPE_INNER_BODY_NORTH = Block.box(0, 4, 9, 16, 9, 16);
        VOXEL_SHAPE_PLATFORM_NORTH = Block.box(2, 9, 2, 14, 12, 10);

        VOXEL_SHAPE_AGGREGATE_0_NORTH = Shapes.or(
                VOXEL_SHAPE_OUTER_BASE_NORTH,
                VOXEL_SHAPE_INNER_BASE_NORTH,
                VOXEL_SHAPE_OUTER_BODY_NORTH,
                VOXEL_SHAPE_INNER_BODY_NORTH,
                VOXEL_SHAPE_PLATFORM_NORTH
        );
        VOXEL_SHAPE_AGGREGATE_0_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BASE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BASE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLATFORM_NORTH, 1)
        );
        VOXEL_SHAPE_AGGREGATE_0_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BASE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BASE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLATFORM_NORTH, 2)
        );
        VOXEL_SHAPE_AGGREGATE_0_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BASE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BASE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLATFORM_NORTH, 3)
        );

        VOXEL_SHAPE_BASE_NORTHEAST = Block.box(0, 0,  9, 7, 4, 16);
        VOXEL_SHAPE_BODY_NORTHEAST = Block.box(0, 4, 10, 6, 9, 16);
        VOXEL_SHAPE_AGGREGATE_1_NORTH = Shapes.or(
                VOXEL_SHAPE_BASE_NORTHEAST,
                VOXEL_SHAPE_BODY_NORTHEAST
        );
        VOXEL_SHAPE_AGGREGATE_1_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 1)
        );
        VOXEL_SHAPE_AGGREGATE_1_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 2)
        );
        VOXEL_SHAPE_AGGREGATE_1_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 3)
        );

        VOXEL_SHAPE_AGGREGATE_3_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 1)
        );
        VOXEL_SHAPE_AGGREGATE_3_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 2)
        );
        VOXEL_SHAPE_AGGREGATE_3_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 3)
        );
        VOXEL_SHAPE_AGGREGATE_3_WEST = Shapes.or(
                VOXEL_SHAPE_BASE_NORTHEAST,
                VOXEL_SHAPE_BODY_NORTHEAST
        );

        VOXEL_SHAPE_AGGREGATE_5_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 2)
        );
        VOXEL_SHAPE_AGGREGATE_5_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 3)
        );
        VOXEL_SHAPE_AGGREGATE_5_SOUTH = Shapes.or(
                VOXEL_SHAPE_BASE_NORTHEAST,
                VOXEL_SHAPE_BODY_NORTHEAST
        );
        VOXEL_SHAPE_AGGREGATE_5_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 1)
        );

        VOXEL_SHAPE_AGGREGATE_7_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 3)
        );
        VOXEL_SHAPE_AGGREGATE_7_EAST = Shapes.or(
                VOXEL_SHAPE_BASE_NORTHEAST,
                VOXEL_SHAPE_BODY_NORTHEAST
        );
        VOXEL_SHAPE_AGGREGATE_7_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 1)
        );
        VOXEL_SHAPE_AGGREGATE_7_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTHEAST, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTHEAST, 2)
        );

        VOXEL_SHAPE_PLUG_NORTH = Block.box(0, 0, 0, 16, 16, 4);

        VOXEL_SHAPE_AGGREGATE_2_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BASE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BASE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLATFORM_NORTH, 1)
        );
        VOXEL_SHAPE_AGGREGATE_2_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BASE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BASE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLATFORM_NORTH, 2)
        );
        VOXEL_SHAPE_AGGREGATE_2_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BASE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BASE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTER_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_INNER_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLATFORM_NORTH, 3)
        );
        VOXEL_SHAPE_AGGREGATE_2_WEST = Shapes.or(
                VOXEL_SHAPE_PLUG_NORTH,
                VOXEL_SHAPE_OUTER_BASE_NORTH,
                VOXEL_SHAPE_INNER_BASE_NORTH,
                VOXEL_SHAPE_OUTER_BODY_NORTH,
                VOXEL_SHAPE_INNER_BODY_NORTH,
                VOXEL_SHAPE_PLATFORM_NORTH
        );

        VOXEL_SHAPE_AGGREGATE_6_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_NORTH), 3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_OUTER_BASE_NORTH), 3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_INNER_BASE_NORTH), 3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_OUTER_BODY_NORTH), 3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_INNER_BODY_NORTH), 3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLATFORM_NORTH), 3)
        );
        VOXEL_SHAPE_AGGREGATE_6_EAST = Shapes.or(
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_NORTH),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_OUTER_BASE_NORTH),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_INNER_BASE_NORTH),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_OUTER_BODY_NORTH),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_INNER_BODY_NORTH),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLATFORM_NORTH)
        );
        VOXEL_SHAPE_AGGREGATE_6_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_NORTH), 1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_OUTER_BASE_NORTH), 1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_INNER_BASE_NORTH), 1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_OUTER_BODY_NORTH), 1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_INNER_BODY_NORTH), 1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLATFORM_NORTH), 1)
        );
        VOXEL_SHAPE_AGGREGATE_6_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_NORTH), 2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_OUTER_BASE_NORTH), 2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_INNER_BASE_NORTH), 2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_OUTER_BODY_NORTH), 2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_INNER_BODY_NORTH), 2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLATFORM_NORTH), 2)
        );

//        VOXEL_SHAPE_BASE_SOUTH = Block.box(1, 0, 0, 15, 15, 8);
//        VOXEL_SHAPE_DAIS_SOUTH = Block.box(-0.4496, 0, -0.4496, 16.4496, 16, 16.4496);
//        VOXEL_SHAPE_AGGREGATE_4_NORTH = Shapes.or(
//                VOXEL_SHAPE_BASE_SOUTH,
//                VOXEL_SHAPE_DAIS_SOUTH
//        );
//        VOXEL_SHAPE_AGGREGATE_4_EAST = Shapes.or(
//                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_SOUTH, 1),
//                MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_SOUTH, 1)
//        );
//        VOXEL_SHAPE_AGGREGATE_4_SOUTH = Shapes.or(
//                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_SOUTH, 2),
//                MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_SOUTH, 2)
//        );
//        VOXEL_SHAPE_AGGREGATE_4_WEST = Shapes.or(
//                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_SOUTH, 3),
//                MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_SOUTH, 3)
//        );

//        VOXEL_SHAPE_DAIS_SOUTH = Block.box(-0.4496, 0, -0.4496, 16.4496, 16, 16.4496);
        VOXEL_SHAPE_DAIS_SOUTH = Block.box(0, 0, 0, 16, 16, 16);
        VOXEL_SHAPE_AGGREGATE_4_NORTH = VOXEL_SHAPE_DAIS_SOUTH;
        VOXEL_SHAPE_AGGREGATE_4_EAST = VOXEL_SHAPE_DAIS_SOUTH;
        VOXEL_SHAPE_AGGREGATE_4_SOUTH = VOXEL_SHAPE_DAIS_SOUTH;
        VOXEL_SHAPE_AGGREGATE_4_WEST = VOXEL_SHAPE_DAIS_SOUTH;
    }
}