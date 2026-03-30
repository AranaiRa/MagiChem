package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.PrimeAggregatorBlockEntity;
import com.aranaira.magichem.block.entity.routers.PrimeAggregatorRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.PrimeAggregatorRouterType;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_PRIME_AGGREGATOR;
import static com.aranaira.magichem.foundation.enums.PrimeAggregatorRouterType.*;

public class PrimeAggregatorRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public static final VoxelShape
            VOXEL_SHAPE_FRONT_BASE_NORTH, VOXEL_SHAPE_FRONT_MID_NORTH, VOXEL_SHAPE_FRONT_TOP_NORTH,
            VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH, VOXEL_SHAPE_FRONT_LEFT_MID_NORTH, VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH,
            VOXEL_SHAPE_PLUG_LEFT_MID_NORTH, VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH, VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH, VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH,

            VOXEL_SHAPE_AGGREGATE_FRONT_NORTH, VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_NORTH, VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_NORTH,
            VOXEL_SHAPE_AGGREGATE_FRONT_EAST,  VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_EAST,  VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_EAST,
            VOXEL_SHAPE_AGGREGATE_FRONT_SOUTH, VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_SOUTH, VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_SOUTH,
            VOXEL_SHAPE_AGGREGATE_FRONT_WEST,  VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_WEST,  VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_WEST,
            VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_NORTH, VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_NORTH,
            VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_EAST,  VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_EAST,
            VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_SOUTH, VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_SOUTH,
            VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_WEST,  VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_WEST,
            VOXEL_SHAPE_AGGREGATE_REAR_NORTH, VOXEL_SHAPE_AGGREGATE_REAR_LEFT_NORTH, VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_NORTH,
            VOXEL_SHAPE_AGGREGATE_REAR_EAST,  VOXEL_SHAPE_AGGREGATE_REAR_LEFT_EAST,  VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_EAST,
            VOXEL_SHAPE_AGGREGATE_REAR_SOUTH, VOXEL_SHAPE_AGGREGATE_REAR_LEFT_SOUTH, VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_SOUTH,
            VOXEL_SHAPE_AGGREGATE_REAR_WEST,  VOXEL_SHAPE_AGGREGATE_REAR_LEFT_WEST,  VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_WEST;

    public PrimeAggregatorRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ROUTER_TYPE_PRIME_AGGREGATOR, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.PRIME_AGGREGATOR.get());
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof PrimeAggregatorRouterBlockEntity router) {
            PrimeAggregatorBlockEntity master = router.getMaster();
            pPlayer.swing(InteractionHand.MAIN_HAND);
            return master.getBlockState().getBlock().use(master.getBlockState(), pLevel, master.getBlockPos(), pPlayer, pHand, pHit);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    public static int mapRouterTypeToInt(PrimeAggregatorRouterType type) {
        if(type == null) return 0;

        return switch(type) {
            case NONE -> 0;
            case FRONT -> 1;
            case FRONT_LEFT -> 2;
            case FRONT_RIGHT -> 3;
            case PLUG_LEFT -> 4;
            case PLUG_RIGHT -> 5;
            case REAR -> 6;
            case REAR_LEFT -> 7;
            case REAR_RIGHT -> 8;
        };
    }

    public static PrimeAggregatorRouterType unmapRouterTypeFromInt(int value) {
        return switch(value) {
            case 1 -> FRONT;
            case 2 -> PrimeAggregatorRouterType.FRONT_LEFT;
            case 3 -> PrimeAggregatorRouterType.FRONT_RIGHT;
            case 4 -> PrimeAggregatorRouterType.PLUG_LEFT;
            case 5 -> PrimeAggregatorRouterType.PLUG_RIGHT;
            case 6 -> PrimeAggregatorRouterType.REAR;
            case 7 -> PrimeAggregatorRouterType.REAR_LEFT;
            case 8 -> PrimeAggregatorRouterType.REAR_RIGHT;
            default -> null;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PrimeAggregatorRouterBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockState state = pLevel.getBlockState(pPos);

        if(state.getBlock() == BlockRegistry.PRIME_AGGREGATOR_ROUTER.get()) {
            PrimeAggregatorRouterType routerType = unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_PRIME_AGGREGATOR));
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            //Again, switch statements always default here and I have no idea why
            if (routerType == FRONT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_FRONT_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_FRONT_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_FRONT_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_FRONT_WEST;
            }
            else if (routerType == FRONT_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_WEST;
            }
            else if (routerType == FRONT_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_WEST;
            }
            else if (routerType == PLUG_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_WEST;
            }
            else if (routerType == PLUG_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_WEST;
            }
            else if (routerType == REAR) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_REAR_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_REAR_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_REAR_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_REAR_WEST;
            }
            else if (routerType == REAR_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_REAR_LEFT_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_REAR_LEFT_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_REAR_LEFT_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_REAR_LEFT_WEST;
            }
            else if (routerType == REAR_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_WEST;
            }
        }

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    static {
        VOXEL_SHAPE_FRONT_BASE_NORTH = Block.box(0, 0, 0, 16, 3, 15.0525);
        VOXEL_SHAPE_FRONT_MID_NORTH = Block.box(0, 3, 0, 16, 8, 14.1865);
        VOXEL_SHAPE_FRONT_TOP_NORTH = Block.box(0, 8, 0, 16, 16, 11.5885);

        VOXEL_SHAPE_AGGREGATE_FRONT_NORTH = Shapes.or(
                VOXEL_SHAPE_FRONT_BASE_NORTH,
                VOXEL_SHAPE_FRONT_MID_NORTH,
                VOXEL_SHAPE_FRONT_TOP_NORTH
        );
        VOXEL_SHAPE_AGGREGATE_FRONT_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_MID_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_TOP_NORTH,1)
        );
        VOXEL_SHAPE_AGGREGATE_FRONT_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_MID_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_TOP_NORTH,2)
        );
        VOXEL_SHAPE_AGGREGATE_FRONT_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_MID_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_TOP_NORTH,3)
        );

        VOXEL_SHAPE_AGGREGATE_REAR_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_MID_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_TOP_NORTH,2)
        );
        VOXEL_SHAPE_AGGREGATE_REAR_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_MID_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_TOP_NORTH,3)
        );
        VOXEL_SHAPE_AGGREGATE_REAR_SOUTH = Shapes.or(
                VOXEL_SHAPE_FRONT_BASE_NORTH,
                VOXEL_SHAPE_FRONT_MID_NORTH,
                VOXEL_SHAPE_FRONT_TOP_NORTH
        );
        VOXEL_SHAPE_AGGREGATE_REAR_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_BASE_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_MID_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_TOP_NORTH,1)
        );

        VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH = Block.box(2, 0, 0, 16, 3, 12.7845);
        VOXEL_SHAPE_FRONT_LEFT_MID_NORTH = Block.box(3, 3, 0, 16, 8, 11.6299);
        VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH = Block.box(6, 8, 0, 16, 16, 8.1658);

        VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_NORTH = Shapes.or(
                VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH,
                VOXEL_SHAPE_FRONT_LEFT_MID_NORTH,
                VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH
        );
        VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH,1)
        );
        VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH,2)
        );
        VOXEL_SHAPE_AGGREGATE_FRONT_LEFT_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH,3)
        );

        VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_NORTH = Shapes.or(
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH)
        );
        VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH),1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH),1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH),1)
        );
        VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH),2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH),2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH),2)
        );
        VOXEL_SHAPE_AGGREGATE_FRONT_RIGHT_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH),3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH),3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH),3)
        );

        VOXEL_SHAPE_AGGREGATE_REAR_LEFT_NORTH = Shapes.or(
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH),
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH),
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH)
        );
        VOXEL_SHAPE_AGGREGATE_REAR_LEFT_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH),1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH),1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH),1)
        );
        VOXEL_SHAPE_AGGREGATE_REAR_LEFT_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH),2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH),2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH),2)
        );
        VOXEL_SHAPE_AGGREGATE_REAR_LEFT_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH),3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH),3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH),3)
        );

        VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_NORTH = Shapes.or(
                MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH)),
                MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH)),
                MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH))
        );
        VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH)),1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH)),1),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH)),1)
        );
        VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH)),2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH)),2),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH)),2)
        );
        VOXEL_SHAPE_AGGREGATE_REAR_RIGHT_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_BASE_NORTH)),3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_MID_NORTH)),3),
                MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_FRONT_LEFT_TOP_NORTH)),3)
        );

        VOXEL_SHAPE_PLUG_LEFT_MID_NORTH = Block.box(4, 0, 0, 16, 8, 16);
        VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH = Block.box(6, 0, 0, 16, 16, 16);
        VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH = Block.box(0, 0, 0, 4, 16, 16);
        VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH = Block.box(0, 8, 3, 16, 12, 13);

        VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_NORTH = Shapes.or(
                VOXEL_SHAPE_PLUG_LEFT_MID_NORTH,
                VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH,
                VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH,
                VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH
        );
        VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_MID_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH,1)
        );
        VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_MID_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH,2)
        );
        VOXEL_SHAPE_AGGREGATE_PLUG_LEFT_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_MID_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH,3)
        );

        VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_NORTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_MID_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH,2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH,2)
        );
        VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_MID_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH,3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH,3)
        );
        VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_SOUTH = Shapes.or(
                VOXEL_SHAPE_PLUG_LEFT_MID_NORTH,
                VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH,
                VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH,
                VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH
        );
        VOXEL_SHAPE_AGGREGATE_PLUG_RIGHT_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_MID_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_TOP_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_PLUG_NORTH,1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_LEFT_CONNECTOR_NORTH,1)
        );
    }
}
