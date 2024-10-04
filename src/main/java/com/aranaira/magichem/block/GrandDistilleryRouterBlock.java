package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.GrandDistilleryBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandDistilleryRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.GrandDistilleryRouterType;
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
import net.minecraft.world.level.LevelReader;
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

public class GrandDistilleryRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public GrandDistilleryRouterBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
            this.stateDefinition.any()
                    .setValue(FACING, Direction.NORTH)
                    .setValue(HAS_LABORATORY_UPGRADE, false)
                    .setValue(IS_EMITTING_LIGHT, false)
        );
    }

    public static VoxelShape
        VOXEL_SHAPE_DAIS_BASE, VOXEL_SHAPE_DAIS_DAIS,
        VOXEL_SHAPE_DAIS_AGGREGATE_NORTH, VOXEL_SHAPE_DAIS_AGGREGATE_EAST, VOXEL_SHAPE_DAIS_AGGREGATE_SOUTH, VOXEL_SHAPE_DAIS_AGGREGATE_WEST,

        VOXEL_SHAPE_PLUG_FRONT_LEFT_BASE, VOXEL_SHAPE_PLUG_FRONT_LEFT_PLUG, VOXEL_SHAPE_PLUG_FRONT_LEFT_BRACE,
        VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_FRONT_RIGHT_BASE, VOXEL_SHAPE_PLUG_FRONT_RIGHT_PLUG, VOXEL_SHAPE_PLUG_FRONT_RIGHT_BRACE,
        VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_PLUG_MID_LEFT_BASE, VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, VOXEL_SHAPE_PLUG_MID_LEFT_BRACE, VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD,
        VOXEL_SHAPE_PLUG_MID_LEFT_TANK,
        VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST_UPGRADED, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST_UPGRADED,
        VOXEL_SHAPE_PLUG_MID_RIGHT_BASE, VOXEL_SHAPE_PLUG_MID_RIGHT_PLUG, VOXEL_SHAPE_PLUG_MID_RIGHT_BRACE, VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD,
        VOXEL_SHAPE_PLUG_MID_RIGHT_TANK,
        VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST_UPGRADED, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH_UPGRADED, VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST_UPGRADED,

        VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, VOXEL_SHAPE_PLUG_BACK_LEFT_BRACE, VOXEL_SHAPE_PLUG_BACK_LEFT_BACKBOARD,
        VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_PLUG_BACK_RIGHT_BASE, VOXEL_SHAPE_PLUG_BACK_RIGHT_PLUG, VOXEL_SHAPE_PLUG_BACK_RIGHT_BRACE, VOXEL_SHAPE_PLUG_BACK_RIGHT_BACKBOARD,
        VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_BACK_BASE, VOXEL_SHAPE_BACK_BACKBOARD, VOXEL_SHAPE_BACK_BACKBOARD_BULGE,
        VOXEL_SHAPE_BACK_AGGREGATE_NORTH, VOXEL_SHAPE_BACK_AGGREGATE_EAST, VOXEL_SHAPE_BACK_AGGREGATE_SOUTH, VOXEL_SHAPE_BACK_AGGREGATE_WEST,

        VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_MID, VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_LEFT, VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_RIGHT,
        VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_LOW, VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_HIGH,
        VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_EAST, VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_WEST,

        VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_OUTER, VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_MID, VOXEL_SHAPE_TANK_MID_FRONT_LEFT_BACKBOARD,
        VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_OUTER, VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_MID, VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_BACKBOARD,
        VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_WIDE, VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_LONG,
        VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_LEFT, VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_RIGHT,
        VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_EAST, VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_WEST,

        VOXEL_SHAPE_TANK_MID_REAR_LEFT_BACKBOARD, VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_OUTER, VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_INNER,
        VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_TANK_MID_REAR_RIGHT_BACKBOARD, VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_OUTER, VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_INNER,
        VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_WEST,

        VOXEL_SHAPE_TANK_TOP_FRONT_TANK_MID,
        VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_EAST, VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_WEST,

        VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_MID, VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_LEFT, VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_RIGHT,
        VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_EAST, VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_WEST,

        VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_OUTER, VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_INNER,
        VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_WEST,
        VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_OUTER, VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_INNER,
        VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_WEST;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GrandDistilleryRouterBlockEntity(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, ROUTER_TYPE_GRAND_DISTILLERY, HAS_LABORATORY_UPGRADE, IS_EMITTING_LIGHT);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockState state = pLevel.getBlockState(pPos);

        if(state.getBlock() == BlockRegistry.GRAND_DISTILLERY_ROUTER.get()) {
            GrandDistilleryRouterType routerType = unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_GRAND_DISTILLERY));
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            boolean hasLaboratoryUpgrade = state.getValue(HAS_LABORATORY_UPGRADE);

            //Again, switch statements always default here and I have no idea why
            if (routerType == GrandDistilleryRouterType.DAIS) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_DAIS_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_DAIS_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_DAIS_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_DAIS_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.BACK) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_BACK_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_BACK_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_BACK_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_BACK_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.PLUG_FRONT_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_FRONT_LEFT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.PLUG_FRONT_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_FRONT_RIGHT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.PLUG_MID_LEFT) {
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
            } else if (routerType == GrandDistilleryRouterType.PLUG_MID_RIGHT) {
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
            } else if (routerType == GrandDistilleryRouterType.PLUG_BACK_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.PLUG_BACK_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_MID_FRONT_CENTER) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_MID_FRONT_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_MID_FRONT_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_MID_REAR_CENTER) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_MID_REAR_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_MID_REAR_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_TOP_FRONT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_TOP_REAR_CENTER) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_TOP_REAR_LEFT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_WEST;
            } else if (routerType == GrandDistilleryRouterType.TANK_TOP_REAR_RIGHT) {
                if (facing == Direction.NORTH) return VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_NORTH;
                else if (facing == Direction.EAST) return VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_EAST;
                else if (facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_SOUTH;
                else if (facing == Direction.WEST) return VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_WEST;
            }
        }

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof GrandDistilleryRouterBlockEntity gdrbe) {
            GrandDistilleryBlockEntity master = gdrbe.getMaster();
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
            if(be instanceof GrandDistilleryRouterBlockEntity gdrbe) {
                if(gdrbe.getMaster() != null)
                    gdrbe.getMaster().setRedstonePaused(powered);
            }
        } else if(state.hasProperty(BlockStateProperties.POWER)) {
            int power = state.getValue(BlockStateProperties.POWER);
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if(be instanceof GrandDistilleryRouterBlockEntity gdrbe) {
                if(gdrbe.getMaster() != null)
                    gdrbe.getMaster().setRedstonePaused(power > 0);
            }
        }

        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        //can only emit light if this router is a dais
//        if(state.getValue(ROUTER_TYPE_GRAND_DISTILLERY) == 1 && state.getValue(IS_EMITTING_LIGHT)) {
//            return 15;
//        }

        return state.getValue(ROUTER_TYPE_GRAND_DISTILLERY) == 1 ? 15 : 0;
    }

    public static int mapRouterTypeToInt(GrandDistilleryRouterType pRouterType) {
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
            case TANK_MID_FRONT_CENTER -> 9;
            case TANK_MID_FRONT_LEFT -> 10;
            case TANK_MID_FRONT_RIGHT -> 11;
            case TANK_MID_REAR_CENTER -> 12;
            case TANK_MID_REAR_LEFT -> 13;
            case TANK_MID_REAR_RIGHT -> 14;
            case TANK_TOP_FRONT -> 15;
            case TANK_TOP_REAR_CENTER -> 16;
            case TANK_TOP_REAR_LEFT -> 17;
            case TANK_TOP_REAR_RIGHT -> 18;
            default -> 0;
        };
    }

    public static GrandDistilleryRouterType unmapRouterTypeFromInt(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> GrandDistilleryRouterType.DAIS;
            case 2 -> GrandDistilleryRouterType.BACK;
            case 3 -> GrandDistilleryRouterType.PLUG_FRONT_LEFT;
            case 4 -> GrandDistilleryRouterType.PLUG_FRONT_RIGHT;
            case 5 -> GrandDistilleryRouterType.PLUG_MID_LEFT;
            case 6 -> GrandDistilleryRouterType.PLUG_MID_RIGHT;
            case 7 -> GrandDistilleryRouterType.PLUG_BACK_LEFT;
            case 8 -> GrandDistilleryRouterType.PLUG_BACK_RIGHT;
            case 9 -> GrandDistilleryRouterType.TANK_MID_FRONT_CENTER;
            case 10 -> GrandDistilleryRouterType.TANK_MID_FRONT_LEFT;
            case 11 -> GrandDistilleryRouterType.TANK_MID_FRONT_RIGHT;
            case 12 -> GrandDistilleryRouterType.TANK_MID_REAR_CENTER;
            case 13 -> GrandDistilleryRouterType.TANK_MID_REAR_LEFT;
            case 14 -> GrandDistilleryRouterType.TANK_MID_REAR_RIGHT;
            case 15 -> GrandDistilleryRouterType.TANK_TOP_FRONT;
            case 16 -> GrandDistilleryRouterType.TANK_TOP_REAR_CENTER;
            case 17 -> GrandDistilleryRouterType.TANK_TOP_REAR_LEFT;
            case 18 -> GrandDistilleryRouterType.TANK_TOP_REAR_RIGHT;
            default -> GrandDistilleryRouterType.NONE;
        };
    }

    static {
        //DAIS
        {
            VOXEL_SHAPE_DAIS_BASE = Block.box(0, 0, 0, 16, 8, 14);
            VOXEL_SHAPE_DAIS_DAIS = Block.box(-0.4496, 0, -0.4496, 16.4496, 16, 16.4496);

            VOXEL_SHAPE_DAIS_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_DAIS_BASE,
                    VOXEL_SHAPE_DAIS_DAIS
            );

            VOXEL_SHAPE_DAIS_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_DAIS, 1)
            );

            VOXEL_SHAPE_DAIS_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_DAIS, 2)
            );

            VOXEL_SHAPE_DAIS_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_DAIS, 3)
            );
        }

        //BACK
        {
            VOXEL_SHAPE_BACK_BASE = Block.box(0, 0, 2, 16, 8, 16);
            VOXEL_SHAPE_BACK_BACKBOARD = Block.box(0, 8, 3, 16, 16, 16);
            VOXEL_SHAPE_BACK_BACKBOARD_BULGE = Block.box(5, 8, 2, 11, 16, 8);

            VOXEL_SHAPE_BACK_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_BACK_BASE,
                    VOXEL_SHAPE_BACK_BACKBOARD,
                    VOXEL_SHAPE_BACK_BACKBOARD_BULGE
            );

            VOXEL_SHAPE_BACK_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BACKBOARD, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BACKBOARD_BULGE, 1)
            );

            VOXEL_SHAPE_BACK_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BACKBOARD, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BACKBOARD_BULGE, 2)
            );

            VOXEL_SHAPE_BACK_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BACKBOARD, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_BACK_BACKBOARD_BULGE, 3)
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
            VOXEL_SHAPE_PLUG_MID_LEFT_BRACE = Block.box(4, 8, 4, 8, 12, 12);
            VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD = Block.box(6, 8, 0, 16, 16, 3);
            VOXEL_SHAPE_PLUG_MID_LEFT_TANK = Block.box(3.1715, 14.1005, 0, 12.8285, 16, 7.1005);

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_MID_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 1)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 2)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 3)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_NORTH_UPGRADED = Shapes.or(
                    VOXEL_SHAPE_PLUG_MID_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_MID_LEFT_PLUG,
                    VOXEL_SHAPE_PLUG_MID_LEFT_BRACE,
                    VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD,
                    VOXEL_SHAPE_PLUG_MID_LEFT_TANK
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_EAST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BRACE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 1)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_SOUTH_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BRACE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 2)
            );

            VOXEL_SHAPE_PLUG_MID_LEFT_AGGREGATE_WEST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BRACE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_BACKBOARD, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_LEFT_TANK, 3)
            );
        }

        //PLUG_MID_RIGHT
        {
            VOXEL_SHAPE_PLUG_MID_RIGHT_BASE = Block.box(0, 0, 0, 14, 8, 16);
            VOXEL_SHAPE_PLUG_MID_RIGHT_PLUG = Block.box(12, 0, 1, 16, 16, 15);
            VOXEL_SHAPE_PLUG_MID_RIGHT_BRACE = Block.box(8, 8, 4, 12, 12, 12);
            VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD = Block.box(0, 8, 0, 10, 16, 3);
            VOXEL_SHAPE_PLUG_MID_RIGHT_TANK = Block.box(3.1715, 14.1005, 0, 12.8285, 16, 7.1005);

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_MID_RIGHT_BASE,
                    VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD,
                    VOXEL_SHAPE_PLUG_MID_RIGHT_TANK
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_TANK, 1)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_TANK, 2)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_TANK, 3)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_NORTH_UPGRADED = Shapes.or(
                    VOXEL_SHAPE_PLUG_MID_RIGHT_BASE,
                    VOXEL_SHAPE_PLUG_MID_RIGHT_PLUG,
                    VOXEL_SHAPE_PLUG_MID_RIGHT_BRACE,
                    VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD,
                    VOXEL_SHAPE_PLUG_MID_RIGHT_TANK
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_EAST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BRACE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_TANK, 1)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_SOUTH_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BRACE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_TANK, 2)
            );

            VOXEL_SHAPE_PLUG_MID_RIGHT_AGGREGATE_WEST_UPGRADED = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BRACE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_BACKBOARD, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_MID_RIGHT_TANK, 3)
            );
        }

        //PLUG_BACK_LEFT
        {
            VOXEL_SHAPE_PLUG_BACK_LEFT_BASE = Block.box(2, 0, 2, 16, 8, 16);
            VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG = Block.box(0, 0, 0, 4, 16, 16);
            VOXEL_SHAPE_PLUG_BACK_LEFT_BRACE = Block.box(4, 8, 4, 8, 12, 12);
            VOXEL_SHAPE_PLUG_BACK_LEFT_BACKBOARD = Block.box(6, 8, 3, 16, 16, 16);

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_BACK_LEFT_BASE,
                    VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG,
                    VOXEL_SHAPE_PLUG_BACK_LEFT_BRACE,
                    VOXEL_SHAPE_PLUG_BACK_LEFT_BACKBOARD
            );

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BRACE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BACKBOARD, 1)
            );

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BRACE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BACKBOARD, 2)
            );

            VOXEL_SHAPE_PLUG_BACK_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BRACE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_LEFT_BACKBOARD, 3)
            );
        }

        //PLUG_BACK_RIGHT
        {
            VOXEL_SHAPE_PLUG_BACK_RIGHT_BASE = Block.box(0, 0, 2, 14, 8, 16);
            VOXEL_SHAPE_PLUG_BACK_RIGHT_PLUG = Block.box(12, 0, 0, 16, 16, 16);
            VOXEL_SHAPE_PLUG_BACK_RIGHT_BRACE = Block.box(8, 8, 4, 12, 12, 12);
            VOXEL_SHAPE_PLUG_BACK_RIGHT_BACKBOARD = Block.box(0, 8, 3, 10, 16, 16);

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_PLUG_BACK_RIGHT_BASE,
                    VOXEL_SHAPE_PLUG_BACK_RIGHT_PLUG,
                    VOXEL_SHAPE_PLUG_BACK_RIGHT_BRACE,
                    VOXEL_SHAPE_PLUG_BACK_RIGHT_BACKBOARD
            );

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_BASE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_PLUG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_BRACE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_BACKBOARD, 1)
            );

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_BASE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_PLUG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_BRACE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_BACKBOARD, 2)
            );

            VOXEL_SHAPE_PLUG_BACK_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_BASE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_PLUG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_BRACE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_BACK_RIGHT_BACKBOARD, 3)
            );
        }

        //TANK_MID_FRONT_CENTER
        {
            VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_MID = Block.box(3.1715, 8, 3.1715, 13.4144, 16, 13.4144);
            VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_LEFT = Block.box(0, 3.0496, 0, 4.8284, 16, 10.0502);
            VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_RIGHT = Block.box(11.1716, 3.0496, 0, 16, 16, 10.0502);
            VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_HIGH = Block.box(0, 0, 0, 16, 7, 3);
            VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_LOW = Block.box(0, 0, 0, 16, 2, 3);

            VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_MID,
                    VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_LEFT,
                    VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_RIGHT,
                    VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_HIGH,
                    VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_LOW
            );

            VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_MID, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_LEFT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_RIGHT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_HIGH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_LOW, 1)
            );

            VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_MID, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_LEFT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_RIGHT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_HIGH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_LOW, 2)
            );

            VOXEL_SHAPE_TANK_MID_FRONT_CENTER_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_MID, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_LEFT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_TANK_RIGHT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_HIGH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_CENTER_BACKBOARD_LOW, 3)
            );
        }

        //TANK_MID_FRONT_LEFT
        {
            VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_OUTER = Block.box(3.1715, 0, 0, 12.8284, 11.32224, 7.1005);
            VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_MID = Block.box(11.1715, 3.0496, 0, 16, 16, 10.0502);
            VOXEL_SHAPE_TANK_MID_FRONT_LEFT_BACKBOARD = Block.box(14, 0, 0, 16, 2, 3);

            VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_OUTER,
                    VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_MID,
                    VOXEL_SHAPE_TANK_MID_FRONT_LEFT_BACKBOARD
            );

            VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_OUTER, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_MID, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_LEFT_BACKBOARD, 1)
            );

            VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_OUTER, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_MID, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_LEFT_BACKBOARD, 2)
            );

            VOXEL_SHAPE_TANK_MID_FRONT_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_OUTER, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_LEFT_TANK_MID, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_LEFT_BACKBOARD, 3)
            );
        }

        //TANK_MID_FRONT_RIGHT
        {
            VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_OUTER = Block.box(3.1715, 0, 0, 12.8284, 11.32224, 7.1005);
            VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_MID = Block.box(0, 3.0496, 0, 4.8285, 16, 10.0502);
            VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_BACKBOARD = Block.box(0, 0, 0, 2, 2, 3);

            VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_OUTER,
                    VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_MID,
                    VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_BACKBOARD
            );

            VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_OUTER, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_MID, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_BACKBOARD, 1)
            );

            VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_OUTER, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_MID, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_BACKBOARD, 2)
            );

            VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_OUTER, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_TANK_MID, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_FRONT_RIGHT_BACKBOARD, 3)
            );
        }

        //TANK_MID_REAR_CENTER
        {
            VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_WIDE = Block.box(0, 0, 3, 16, 6, 15);
            VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_LONG = Block.box(5, 0, 2, 11, 7, 16);
            VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_LEFT = Block.box(0, 3.0502, 6.27216, 4.8285, 16, 16);
            VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_RIGHT = Block.box(11.1715, 3.0502, 6.27216, 16, 16, 16);

            VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_WIDE,
                    VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_LONG,
                    VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_LEFT,
                    VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_RIGHT
            );

            VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_WIDE, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_LONG, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_LEFT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_RIGHT, 1)
            );

            VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_WIDE, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_LONG, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_LEFT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_RIGHT, 2)
            );

            VOXEL_SHAPE_TANK_MID_REAR_CENTER_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_WIDE, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_BACKBOARD_LONG, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_LEFT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_CENTER_TANK_RIGHT, 3)
            );
        }

        //TANK_MID_REAR_LEFT
        {
            VOXEL_SHAPE_TANK_MID_REAR_LEFT_BACKBOARD = Block.box(14, 0, 3, 16, 6, 16);
            VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_OUTER = Block.box(3.1715, 3.0502, 0.715744, 12.8284, 16, 16);
            VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_INNER = Block.box(11.1715, 3.0502, 3.66544, 16, 16, 16);

            VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_MID_REAR_LEFT_BACKBOARD,
                    VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_OUTER,
                    VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_INNER
            );

            VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_LEFT_BACKBOARD, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_OUTER, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_INNER, 1)
            );

            VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_LEFT_BACKBOARD, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_OUTER, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_INNER, 2)
            );

            VOXEL_SHAPE_TANK_MID_REAR_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_LEFT_BACKBOARD, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_OUTER, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_LEFT_TANK_INNER, 3)
            );
        }

        //TANK_MID_REAR_RIGHT
        {
            VOXEL_SHAPE_TANK_MID_REAR_RIGHT_BACKBOARD = Block.box(0, 0, 3, 2, 6, 16);
            VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_OUTER = Block.box(3.1715, 3.0502, 0.715744, 12.8284, 16, 16);
            VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_INNER = Block.box(0, 3.0502, 3.66544, 4.8285, 16, 16);

            VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_MID_REAR_RIGHT_BACKBOARD,
                    VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_OUTER,
                    VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_INNER
            );

            VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_RIGHT_BACKBOARD, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_OUTER, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_INNER, 1)
            );

            VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_RIGHT_BACKBOARD, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_OUTER, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_INNER, 2)
            );

            VOXEL_SHAPE_TANK_MID_REAR_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_RIGHT_BACKBOARD, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_OUTER, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MID_REAR_RIGHT_TANK_INNER, 3)
            );
        }

        //TANK_TOP_FRONT
        {
            VOXEL_SHAPE_TANK_TOP_FRONT_TANK_MID = Block.box(3.1715, 0, 0, 13.4144, 11.82832, 11.82832);

            VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_TOP_FRONT_TANK_MID
            );

            VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_FRONT_TANK_MID, 1)
            );

            VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_FRONT_TANK_MID, 2)
            );

            VOXEL_SHAPE_TANK_TOP_FRONT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_FRONT_TANK_MID, 3)
            );
        }

        //TANK_TOP_REAR_CENTER
        {
            VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_MID = Block.box(3.1715, 0, 6.6152, 13.4144, 14.3848, 16);
            VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_LEFT = Block.box(0, 0, 3.66544, 4.8285, 9.43504, 16);
            VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_RIGHT = Block.box(11.1715, 0, 3.66544, 16, 9.43504, 16);

            VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_MID,
                    VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_LEFT,
                    VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_RIGHT
            );

            VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_MID, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_LEFT, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_RIGHT, 1)
            );

            VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_MID, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_LEFT, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_RIGHT, 2)
            );

            VOXEL_SHAPE_TANK_TOP_REAR_CENTER_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_MID, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_LEFT, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_CENTER_TANK_RIGHT, 3)
            );
        }

        //TANK_TOP_REAR_LEFT
        {
            VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_OUTER = Block.box(3.1715, 0, 0.7157, 12.8284, 4.4852, 12.0296);
            VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_INNER = Block.box(11.1715, 0, 3.6654, 16, 9.4350, 16);

            VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_OUTER,
                    VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_INNER
            );

            VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_OUTER, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_INNER, 1)
            );

            VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_OUTER, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_INNER, 2)
            );

            VOXEL_SHAPE_TANK_TOP_REAR_LEFT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_OUTER, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_LEFT_TANK_INNER, 3)
            );
        }

        //TANK_TOP_REAR_RIGHT
        {
            VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_OUTER = Block.box(3.1715, 0, 0.7157, 12.8284, 4.4852, 16);
            VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_INNER = Block.box(0, 0, 3.6654, 4.8285, 9.4350, 16);

            VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_NORTH = Shapes.or(
                    VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_OUTER,
                    VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_INNER
            );

            VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_OUTER, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_INNER, 1)
            );

            VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_OUTER, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_INNER, 2)
            );

            VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_AGGREGATE_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_OUTER, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_TOP_REAR_RIGHT_TANK_INNER, 3)
            );
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.GRAND_DISTILLERY.get());
    }
}
