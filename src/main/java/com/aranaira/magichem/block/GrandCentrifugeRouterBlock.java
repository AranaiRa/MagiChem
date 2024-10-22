package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.GrandCentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandCentrifugeRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.GrandCentrifugeRouterType;
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
import static com.aranaira.magichem.foundation.enums.GrandCentrifugeRouterType.*;

public class GrandCentrifugeRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public GrandCentrifugeRouterBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
            this.stateDefinition.any()
                    .setValue(FACING, Direction.NORTH)
                    .setValue(HAS_LABORATORY_UPGRADE, false)
                    .setValue(IS_EMITTING_LIGHT, false)
        );
    }

    public static VoxelShape
        VOXEL_SHAPE_DAIS_DAIS,
        VOXEL_SHAPE_DAIS_AGGREGATE_NORTH, VOXEL_SHAPE_DAIS_AGGREGATE_EAST, VOXEL_SHAPE_DAIS_AGGREGATE_SOUTH, VOXEL_SHAPE_DAIS_AGGREGATE_WEST,

        VOXEL_SHAPE_PLUG_FRONT_LEFT_BASE, VOXEL_SHAPE_PLUG_FRONT_LEFT_PLUG, VOXEL_SHAPE_PLUG_FRONT_LEFT_BRACE,
        VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_FRONT_RIGHT_BASE, VOXEL_SHAPE_PLUG_FRONT_RIGHT_PLUG, VOXEL_SHAPE_PLUG_FRONT_RIGHT_BRACE,
        VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_PLUG_MID_LEFT_BASE, VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT, VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID, VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT, VOXEL_SHAPE_PLUG_MID_LEFT_BODY,
        VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST_UPGRADED, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST_UPGRADED,
        VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST_UPGRADED, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST_UPGRADED,

        VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, VOXEL_SHAPE_PLUG_BACK_LEFT_BODY,
        VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_BACK_BASE, VOXEL_SHAPE_BACK_BODY,
        VOXEL_SHAPE_BACK_AGGREGATE_NORTH, VOXEL_SHAPE_BACK_AGGREGATE_EAST, VOXEL_SHAPE_BACK_AGGREGATE_SOUTH, VOXEL_SHAPE_BACK_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH, VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW,
        VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT,
        VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_HIGH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_LOW,
        VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT,
        VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_SLICE,
        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT,
        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_WEST;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GrandCentrifugeRouterBlockEntity(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, ROUTER_TYPE_GRAND_CENTRIFUGE, HAS_LABORATORY_UPGRADE, IS_EMITTING_LIGHT);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockState state = pLevel.getBlockState(pPos);

        if(state.getBlock() == BlockRegistry.GRAND_CENTRIFUGE_ROUTER.get()) {
            GrandCentrifugeRouterType routerType = unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_GRAND_CENTRIFUGE));
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            boolean hasLaboratoryUpgrade = state.getValue(HAS_LABORATORY_UPGRADE);

            //Again, switch statements always default here and I have no idea why
            if (routerType == DAIS) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_DAIS_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_DAIS_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_DAIS_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_DAIS_AGGREGATE_WEST;
            }
            else if (routerType == BACK) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_BACK_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_BACK_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_BACK_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_BACK_AGGREGATE_WEST;
            }
            else if (routerType == PLUG_FRONT_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_WEST;
            }
            else if (routerType == PLUG_FRONT_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_WEST;
            }
            else if (routerType == PLUG_MID_LEFT) {
                if (hasLaboratoryUpgrade) {
                    if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH_UPGRADED;
                    else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST_UPGRADED;
                    else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH_UPGRADED;
                    else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST_UPGRADED;
                } else {
                    if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH;
                    else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST;
                    else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH;
                    else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST;
                }
            }
            else if (routerType == PLUG_MID_RIGHT) {
                if (hasLaboratoryUpgrade) {
                    if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH_UPGRADED;
                    else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST_UPGRADED;
                    else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH_UPGRADED;
                    else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST_UPGRADED;
                } else {
                    if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH;
                    else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST;
                    else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH;
                    else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST;
                }
            }
            else if (routerType == PLUG_BACK_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_WEST;
            }
            else if (routerType == PLUG_BACK_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_MID_MID) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_MID_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_MID_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_MID_BACK_MID) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_MID_BACK_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_MID_BACK_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_UPPER_BACK_MID) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_UPPER_BACK_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_UPPER_BACK_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_WEST;
            }
        }

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof GrandCentrifugeRouterBlockEntity gdrbe) {
            GrandCentrifugeBlockEntity master = gdrbe.getMaster();
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
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        BlockState state = pLevel.getBlockState(pNeighborPos);

        if(state.hasProperty(BlockStateProperties.POWERED)) {
            boolean powered = state.getValue(BlockStateProperties.POWERED);
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if(be instanceof GrandCentrifugeRouterBlockEntity gdrbe) {
                if(gdrbe.getMaster() != null)
                    gdrbe.getMaster().setRedstonePaused(powered);
            }
        } else if(state.hasProperty(BlockStateProperties.POWER)) {
            int power = state.getValue(BlockStateProperties.POWER);
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if(be instanceof GrandCentrifugeRouterBlockEntity gdrbe) {
                if(gdrbe.getMaster() != null)
                    gdrbe.getMaster().setRedstonePaused(power > 0);
            }
        }

        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(ROUTER_TYPE_GRAND_CENTRIFUGE) == 1 ? 15 : 0;
    }

    public static int mapRouterTypeToInt(GrandCentrifugeRouterType pRouterType) {
        if(pRouterType == null)
            return 0;

        return switch(pRouterType) {
            case DAIS -> 1;
            case BACK -> 2;
            case PLUG_FRONT_LEFT -> 3;
            case PLUG_FRONT_RIGHT -> 4;
            case PLUG_MID_LEFT -> 5;
            case PLUG_MID_RIGHT -> 6;
            case PLUG_BACK_LEFT -> 7;
            case PLUG_BACK_RIGHT -> 8;
            case ASSEMBLY_MID_MID -> 9;
            case ASSEMBLY_MID_LEFT -> 10;
            case ASSEMBLY_MID_RIGHT -> 11;
            case ASSEMBLY_MID_BACK_MID -> 12;
            case ASSEMBLY_MID_BACK_LEFT -> 13;
            case ASSEMBLY_MID_BACK_RIGHT -> 14;
            case ASSEMBLY_UPPER_BACK_MID -> 15;
            case ASSEMBLY_UPPER_BACK_LEFT -> 16;
            case ASSEMBLY_UPPER_BACK_RIGHT -> 17;
            default -> 0;
        };
    }

    public static GrandCentrifugeRouterType unmapRouterTypeFromInt(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> DAIS;
            case 2 -> BACK;
            case 3 -> PLUG_FRONT_LEFT;
            case 4 -> PLUG_FRONT_RIGHT;
            case 5 -> PLUG_MID_LEFT;
            case 6 -> PLUG_MID_RIGHT;
            case 7 -> PLUG_BACK_LEFT;
            case 8 -> PLUG_BACK_RIGHT;
            case 9 -> ASSEMBLY_MID_MID;
            case 10 -> ASSEMBLY_MID_LEFT;
            case 11 -> ASSEMBLY_MID_RIGHT;
            case 12 -> ASSEMBLY_MID_BACK_MID;
            case 13 -> ASSEMBLY_MID_BACK_LEFT;
            case 14 -> ASSEMBLY_MID_BACK_RIGHT;
            case 15 -> ASSEMBLY_UPPER_BACK_MID;
            case 16 -> ASSEMBLY_UPPER_BACK_LEFT;
            case 17 -> ASSEMBLY_UPPER_BACK_RIGHT;
            default -> NONE;
        };
    }

    static {
        //DAIS
        {
            VOXEL_SHAPE_DAIS_DAIS = Block.box(0, 0, 0, 16, 16, 16);

            VOXEL_SHAPE_DAIS_AGGREGATE_NORTH = VOXEL_SHAPE_DAIS_DAIS;
            VOXEL_SHAPE_DAIS_AGGREGATE_EAST = VOXEL_SHAPE_DAIS_DAIS;
            VOXEL_SHAPE_DAIS_AGGREGATE_SOUTH = VOXEL_SHAPE_DAIS_DAIS;
            VOXEL_SHAPE_DAIS_AGGREGATE_WEST = VOXEL_SHAPE_DAIS_DAIS;
        }

        //BACK
        {
            VOXEL_SHAPE_BACK_BASE = Block.box(0, 0, 2, 16, 8, 16);
            VOXEL_SHAPE_BACK_BODY = Block.box(0, 8, 3, 16, 16, 16);

            VOXEL_SHAPE_BACK_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_BACK_BASE,
                    VOXEL_SHAPE_BACK_BODY
            );

            VOXEL_SHAPE_BACK_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BODY, 1)
            );

            VOXEL_SHAPE_BACK_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BODY, 2)
            );

            VOXEL_SHAPE_BACK_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BODY, 3)
            );
        }

        //PLUG_FRONT_LEFT
        {
            VOXEL_SHAPE_PLUG_FRONT_LEFT_BASE = Block.box(2, 0, 0, 16, 8, 14);
            VOXEL_SHAPE_PLUG_FRONT_LEFT_PLUG = Block.box(0, 0, 0, 4, 16, 16);
            VOXEL_SHAPE_PLUG_FRONT_LEFT_BRACE = Block.box(4, 8, 4, 8, 12, 12);

            VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_FRONT_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_FRONT_LEFT_PLUG,
                    VOXEL_SHAPE_PLUG_FRONT_LEFT_BRACE
            );

            VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_LEFT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_LEFT_BRACE, 1)
            );

            VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_LEFT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_LEFT_BRACE, 2)
            );

            VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_LEFT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_LEFT_BRACE, 3)
            );
        }

        //PLUG_FRONT_RIGHT
        {
            VOXEL_SHAPE_PLUG_FRONT_RIGHT_BASE = Block.box(0, 0, 0, 14, 8, 14);
            VOXEL_SHAPE_PLUG_FRONT_RIGHT_PLUG = Block.box(12, 0, 0, 16, 16, 16);
            VOXEL_SHAPE_PLUG_FRONT_RIGHT_BRACE = Block.box(8, 8, 4, 12, 12, 12);

            VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_FRONT_RIGHT_BASE,
                    VOXEL_SHAPE_PLUG_FRONT_RIGHT_PLUG,
                    VOXEL_SHAPE_PLUG_FRONT_RIGHT_BRACE
            );

            VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_RIGHT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_RIGHT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_RIGHT_BRACE, 1)
            );

            VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_RIGHT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_RIGHT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_RIGHT_BRACE, 2)
            );

            VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_RIGHT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_RIGHT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_FRONT_RIGHT_BRACE, 3)
            );
        }

        //PLUG_MID_LEFT
        {
            VOXEL_SHAPE_PLUG_MID_LEFT_BASE = Block.box(2, 0, 0, 16, 8, 16);
            VOXEL_SHAPE_PLUG_MID_LEFT_PLUG = Block.box(0, 0, 1, 4, 16, 15);
            VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT = Block.box(6.138, 0, 7.764, 9.563, 16, 12.379);
            VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID = Block.box(9.147, 0, 12.170, 11.962, 16, 14.985);
            VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT = Block.box(12.157, 0, 14.777, 14.972, 16, 17.592);
            VOXEL_SHAPE_PLUG_MID_LEFT_BODY = Block.box(7.301, 8, 0, 16, 16, 16);

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_MID_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT,
                    VOXEL_SHAPE_PLUG_MID_LEFT_BODY
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 1)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 2)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 3)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH_UPGRADED = Shapes.or(
                    VOXEL_SHAPE_PLUG_MID_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_MID_LEFT_PLUG,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT,
                    VOXEL_SHAPE_PLUG_MID_LEFT_BODY
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 1)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 2)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 3)
            );
        }

        //PLUG_MID_RIGHT
        {
            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 1)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 2)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 3)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH_UPGRADED = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 1)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 2)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_LEFT), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_MID), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK_RIGHT), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 3)
            );
        }

        //PLUG_BACK_LEFT
        {
            VOXEL_SHAPE_PLUG_BACK_LEFT_BASE = Block.box(2, 0, 2, 16, 8, 16);
            VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG = Block.box(0, 0, 0, 4, 16, 16);
            VOXEL_SHAPE_PLUG_BACK_LEFT_BODY = Block.box(7.300, 8, 3, 16, 16, 16);

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_BACK_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG,
                    VOXEL_SHAPE_PLUG_BACK_LEFT_BODY
            );

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY, 1)
            );

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY, 2)
            );

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY, 3)
            );
        }

        //PLUG_BACK_RIGHT
        {
            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY)
            );

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY), 1)
            );

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY), 2)
            );

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY), 3)
            );
        }

        //ASSEMBLY_MID_MID
        {
            VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH = Block.box(0, 0, 0, 16, 10, 7.621);
            VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW = Block.box(0, 0, 7.621, 16, 5.598, 16);

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH,
                    VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW
            );

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW, 3)
            );
        }

        //ASSEMBLY_MID_LEFT
        {
            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH = Block.box(7.300, 0, 0, 16, 10, 7.621);
            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW = Block.box(7.300, 0, 7.621, 16, 5.598, 16);
            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT = Block.box(6.138, 0, 7.764, 9.563, 8.026, 12.379);
            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID = Block.box(9.147, 0, 12.170, 11.962, 7.026, 14.985);
            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT = Block.box(12.157, 0, 14.777, 14.972, 6.026, 17.592);

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH,
                    VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW,
                    VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT,
                    VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID,
                    VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT
            );

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT, 3)
            );
        }

        //ASSEMBLY_MID_RIGHT
        {
            VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT), 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT), 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_HIGH), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_BODY_LOW), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_LEFT), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_MID), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK_RIGHT), 3)
            );
        }

        //ASSEMBLY_MID_MID
        {
            VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH = Block.box(0, 0, 0, 16, 10, 7.621);
            VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW = Block.box(0, 0, 7.621, 16, 5.598, 16);

            VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH,
                    VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW
            );

            VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_HIGH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_MID_BODY_LOW, 3)
            );
        }

        //ASSEMBLY_MID_BACK_LEFT
        {
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH = Block.box(7.300, 0, 3, 16, 16, 12.303);
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW = Block.box(7.300, 0, 12.303, 16, 12.133, 16);
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT = Block.box(5.434, 0, 5.189, 11.063, 16, 10.820);
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT = Block.box(10.701, 4, 0.627, 16.330, 16, 6.258);

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH,
                    VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW,
                    VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT,
                    VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT, 3)
            );
        }

        //ASSEMBLY_MID_BACK_RIGHT
        {
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT), 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT), 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_HIGH), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY_LOW), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_LEFT), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_PIPE_RIGHT), 3)
            );
        }

        //ASSEMBLY_MID_BACK_MID
        {
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_HIGH = Block.box(0, 0, 3, 16, 16, 12.303);
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_LOW = Block.box(0, 0, 12.303, 16, 12.133, 16);

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_HIGH),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_LOW)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_HIGH), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_LOW), 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_HIGH), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_LOW), 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_HIGH), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_BODY_LOW), 3)
            );
        }

        //ASSEMBLY_UPPER_BACK_MID
        {
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_SLICE = Block.box(0, 0, 3, 16, 0.75376, 6.701424);

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_SLICE
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_SLICE, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_SLICE, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_SLICE, 3)
            );
        }

        //ASSEMBLY_UPPER_BACK_LEFT
        {
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT = Block.box(5.434, 0, 5.189, 11.063, 8, 10.820);
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT = Block.box(10.701, 0, 0.627, 16.330, 12, 6.258);

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT,
                    VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT, 3)
            );
        }

        //ASSEMBLY_MID_BACK_RIGHT
        {
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT), 1)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT), 2)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_LEFT), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_PIPE_RIGHT), 3)
            );
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.GRAND_CENTRIFUGE.get());
    }
}
