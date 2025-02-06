package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.GrandFuseryBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandFuseryRouterBlockEntity;
import com.aranaira.magichem.events.CommonEventHelper;
import com.aranaira.magichem.foundation.enums.GrandFuseryRouterType;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
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
import static com.aranaira.magichem.foundation.enums.GrandFuseryRouterType.*;

public class GrandFuseryRouterBlock extends BaseEntityBlock implements INoCreativeTab, ISpellInteractibleBlock<GrandFuseryRouterBlock> {
    public GrandFuseryRouterBlock(Properties pProperties) {
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

        VOXEL_SHAPE_PLUG_MID_LEFT_BASE, VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, VOXEL_SHAPE_PLUG_MID_LEFT_BODY, VOXEL_SHAPE_PLUG_MID_LEFT_TANK,
        VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST_UPGRADED, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST_UPGRADED,
        VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST_UPGRADED, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST_UPGRADED,

        VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, VOXEL_SHAPE_PLUG_BACK_LEFT_BODY, VOXEL_SHAPE_PLUG_BACK_LEFT_TANK,
        VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_BACK_BASE, VOXEL_SHAPE_BACK_STAND, VOXEL_SHAPE_BACK_TANK,
        VOXEL_SHAPE_BACK_AGGREGATE_NORTH, VOXEL_SHAPE_BACK_AGGREGATE_EAST, VOXEL_SHAPE_BACK_AGGREGATE_SOUTH, VOXEL_SHAPE_BACK_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_MID_MID_TANK_LOW, VOXEL_SHAPE_ASSEMBLY_MID_MID_TANK_MID, VOXEL_SHAPE_ASSEMBLY_MID_MID_TANK_HIGH, VOXEL_SHAPE_ASSEMBLY_MID_MID_NUB,
        VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE,

        VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK,
        VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_STAND, VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_TANK,
        VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK,
        VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK, VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM, VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP,
        VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_TANK, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_BRIM, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_CAP,
        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_WEST,

        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP,
        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_WEST;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GrandFuseryRouterBlockEntity(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, ROUTER_TYPE_GRAND_FUSERY, HAS_LABORATORY_UPGRADE, IS_EMITTING_LIGHT);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockState state = pLevel.getBlockState(pPos);

        if(state.getBlock() == BlockRegistry.GRAND_FUSERY_ROUTER.get()) {
            GrandFuseryRouterType routerType = unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_GRAND_FUSERY));
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
                return VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE;
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
            else if (routerType == ASSEMBLY_UPPER_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_WEST;
            }
            else if (routerType == ASSEMBLY_UPPER_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_WEST;
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
        if(be instanceof GrandFuseryRouterBlockEntity gdrbe) {
            GrandFuseryBlockEntity master = gdrbe.getMaster();
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
            if(be instanceof GrandFuseryRouterBlockEntity gfrbe) {
                if(gfrbe.getMaster() != null)
                    gfrbe.getMaster().setRedstonePaused(powered);
            }
        } else if(state.hasProperty(BlockStateProperties.POWER)) {
            int power = state.getValue(BlockStateProperties.POWER);
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if(be instanceof GrandFuseryRouterBlockEntity gfrbe) {
                if(gfrbe.getMaster() != null)
                    gfrbe.getMaster().setRedstonePaused(power > 0);
            }
        }

        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(ROUTER_TYPE_GRAND_FUSERY) == 1 ? 15 : 0;
    }

    public static int mapRouterTypeToInt(GrandFuseryRouterType pRouterType) {
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
            case ASSEMBLY_UPPER_LEFT -> 15;
            case ASSEMBLY_UPPER_RIGHT -> 16;
            case ASSEMBLY_UPPER_BACK_MID -> 17;
            case ASSEMBLY_UPPER_BACK_LEFT -> 18;
            case ASSEMBLY_UPPER_BACK_RIGHT -> 19;
            default -> 0;
        };
    }

    public static GrandFuseryRouterType unmapRouterTypeFromInt(int pBitpack) {
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
            case 15 -> ASSEMBLY_UPPER_LEFT;
            case 16 -> ASSEMBLY_UPPER_RIGHT;
            case 17 -> ASSEMBLY_UPPER_BACK_MID;
            case 18 -> ASSEMBLY_UPPER_BACK_LEFT;
            case 19 -> ASSEMBLY_UPPER_BACK_RIGHT;
            default -> NONE;
        };
    }

    @Override
    public boolean onHitBySpell(Level level, BlockPos blockPos, ISpellDefinition iSpellDefinition) {
        for(IModifiedSpellPart isp : iSpellDefinition.getComponents()){
            if(isp.getPart().equals(Components.SPLASH)) {
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof GrandFuseryRouterBlockEntity gcrbe) {
                    CommonEventHelper.generateWasteFromCleanedApparatus(null, level, gcrbe.getMaster(), null);
                    return true;
                }
            }
        }
        return false;
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
            VOXEL_SHAPE_BACK_STAND = Block.box(0, 8, 4, 16, 14, 16);
            VOXEL_SHAPE_BACK_TANK = Block.box(4, 14, 6, 12, 16, 14);

            VOXEL_SHAPE_BACK_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_BACK_BASE,
                    VOXEL_SHAPE_BACK_STAND,
                    VOXEL_SHAPE_BACK_TANK
            );

            VOXEL_SHAPE_BACK_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_STAND, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_TANK, 1)
            );

            VOXEL_SHAPE_BACK_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_STAND, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_TANK, 2)
            );

            VOXEL_SHAPE_BACK_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_STAND, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_TANK, 3)
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
            VOXEL_SHAPE_PLUG_MID_LEFT_BODY = Block.box(4, 8, 0, 16, 14, 16);
            VOXEL_SHAPE_PLUG_MID_LEFT_TANK = Block.box(6, 14, 4, 14, 16, 12);

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_MID_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_MID_LEFT_BODY,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 1)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 2)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 3)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH_UPGRADED = Shapes.or(
                    VOXEL_SHAPE_PLUG_MID_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_MID_LEFT_PLUG,
                    VOXEL_SHAPE_PLUG_MID_LEFT_BODY,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 1)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 2)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BODY, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 3)
            );
        }

        //PLUG_MID_RIGHT
        {
            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK), 1)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK), 2)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK), 3)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH_UPGRADED = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK), 1)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK), 2)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BASE), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_BODY), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_MID_LEFT_TANK), 3)
            );
        }

        //PLUG_BACK_LEFT
        {
            VOXEL_SHAPE_PLUG_BACK_LEFT_BASE = Block.box(2, 0, 2, 16, 8, 16);
            VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG = Block.box(0, 0, 0, 4, 16, 16);
            VOXEL_SHAPE_PLUG_BACK_LEFT_BODY = Block.box(4, 8, 4, 16, 14, 16);
            VOXEL_SHAPE_PLUG_BACK_LEFT_TANK = Block.box(10, 14, 10, 18, 16, 18);

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_BACK_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG,
                    VOXEL_SHAPE_PLUG_BACK_LEFT_BODY,
                    VOXEL_SHAPE_PLUG_BACK_LEFT_TANK
            );

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_TANK, 1)
            );

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_TANK, 2)
            );

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_TANK, 3)
            );
        }

        //PLUG_BACK_RIGHT
        {
            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_TANK)
            );

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_TANK), 1)
            );

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_TANK), 2)
            );

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_BODY), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_PLUG_BACK_LEFT_TANK), 3)
            );
        }

        //ASSEMBLY_MID_LEFT
        {
            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND = Block.box(6, 0, 4, 14, 5, 12);
            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK = Block.box(8, 5, 6, 12, 16, 10);

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND,
                    VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK
            );

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK, 3)
            );
        }

        //ASSEMBLY_MID_RIGHT
        {
            VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK), 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK), 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_STAND), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_LEFT_TANK), 3)
            );
        }

        //ASSEMBLY_MID_MID
        {
            VOXEL_SHAPE_ASSEMBLY_MID_MID_TANK_LOW = Block.box(2, 0, 2, 14, 3, 14);
            VOXEL_SHAPE_ASSEMBLY_MID_MID_TANK_MID = Block.box(4, 3, 4, 12, 9, 12);
            VOXEL_SHAPE_ASSEMBLY_MID_MID_TANK_HIGH = Block.box(2, 9, 2, 14, 13, 14);
            VOXEL_SHAPE_ASSEMBLY_MID_MID_NUB = Block.box(6, 13, 6, 10, 14, 10);

            VOXEL_SHAPE_ASSEMBLY_MID_MID_AGGREGATE = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_MID_MID_TANK_LOW,
                    VOXEL_SHAPE_ASSEMBLY_MID_MID_TANK_MID,
                    VOXEL_SHAPE_ASSEMBLY_MID_MID_TANK_HIGH,
                    VOXEL_SHAPE_ASSEMBLY_MID_MID_NUB
            );
        }

        //ASSEMBLY_MID_BACK_LEFT
        {
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY = Block.box(10, 0, 10, 18, 7, 18);
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK = Block.box(12, 7, 12, 16, 16, 16);

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY,
                    VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK, 3)
            );
        }

        //ASSEMBLY_MID_BACK_RIGHT
        {
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK), 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK), 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_BODY), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_LEFT_TANK), 3)
            );
        }

        //ASSEMBLY_MID_BACK_MID
        {
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_STAND = Block.box(4, 0, 6, 12, 9, 14);
            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_TANK = Block.box(6, 9, 8, 10, 16, 12);

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_STAND),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_TANK)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_STAND), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_TANK), 1)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_STAND), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_TANK), 2)
            );

            VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_STAND), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_MID_BACK_MID_TANK), 3)
            );
        }

        //ASSEMBLY_UPPER_LEFT
        {
            VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK = Block.box(8, 0, 6, 12, 1, 10);
            VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM = Block.box(7, 1, 5, 13, 2, 11);
            VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP = Block.box(8, 2, 6, 12, 4, 10);

            VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK,
                    VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM,
                    VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP, 3)
            );
        }

        //ASSEMBLY_UPPER_RIGHT
        {
            VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP), 1)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP), 2)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_TANK), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_BRIM), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_LEFT_CAP), 3)
            );
        }

        //ASSEMBLY_UPPER_BACK_MID
        {
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_TANK = Block.box(6, 0, 8, 10, 5, 12);
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_BRIM = Block.box(5, 5, 7, 11, 6, 13);
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_CAP = Block.box(6, 6, 8, 10, 8, 12);

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_TANK,
                    VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_BRIM,
                    VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_CAP
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_TANK, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_BRIM, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_CAP, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_TANK, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_BRIM, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_CAP, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_TANK, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_BRIM, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_MID_CAP, 3)
            );
        }

        //ASSEMBLY_UPPER_BACK_LEFT
        {
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK = Block.box(12, 0, 12, 16, 3, 16);
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM = Block.box(11, 3, 11, 17, 4, 17);
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP = Block.box(12, 4, 12, 16, 6, 16);

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK,
                    VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM,
                    VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP, 1)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP, 2)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP, 3)
            );
        }

        //ASSEMBLY_MID_BACK_RIGHT
        {
            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM),
                    MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM), 1),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP), 1)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM), 2),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP), 2)
            );

            VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_TANK), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_BRIM), 3),
                    MathHelper.rotateVoxelShape(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_ASSEMBLY_UPPER_BACK_LEFT_CAP), 3)
            );
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.GRAND_FUSERY.get());
    }
}
