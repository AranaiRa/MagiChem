package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.EldrinOrreryBlockEntity;
import com.aranaira.magichem.block.entity.routers.EldrinOrreryRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.EldrinOrreryRouterType;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
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

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_ELDRIN_ORRERY;
import static com.aranaira.magichem.foundation.enums.EldrinOrreryRouterType.*;

public class EldrinOrreryRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public EldrinOrreryRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    public static VoxelShape
            VOXEL_SHAPE_ABOVE, VOXEL_SHAPE_DOUBLE_ABOVE,

            VOXEL_SHAPE_EAST_BASE, VOXEL_SHAPE_EAST_PROTRUSION, VOXEL_SHAPE_EAST_PROTRUSION_EDGE, VOXEL_SHAPE_EAST_DAIS,
            VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_WEST,

            VOXEL_SHAPE_CORNER_BASE, VOXEL_SHAPE_CORNER_PROTRUSION, VOXEL_SHAPE_CORNER_PROTRUSION_EDGE, VOXEL_SHAPE_DAIS,
            VOXEL_SHAPE_AGGREGATE_SOUTH_EAST, VOXEL_SHAPE_AGGREGATE_SOUTH_WEST, VOXEL_SHAPE_AGGREGATE_NORTH_WEST, VOXEL_SHAPE_AGGREGATE_NORTH_EAST,

            VOXEL_SHAPE_SOUTH_BASE, VOXEL_SHAPE_SOUTH_BASE_EDGE, VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT, VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT_EDGE, VOXEL_SHAPE_SOUTH_DAIS,
            VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_NORTH;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EldrinOrreryRouterBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        final EldrinOrreryRouterType type = EldrinOrreryBlock.unmapRouterTypeFromInt(pState.getValue(ROUTER_TYPE_ELDRIN_ORRERY));

        if(type == ABOVE) return VOXEL_SHAPE_ABOVE;
        else if(type == DOUBLE_ABOVE) return VOXEL_SHAPE_DOUBLE_ABOVE;
        else if(type == EAST) return VOXEL_SHAPE_AGGREGATE_EAST;
        else if(type == WEST) return VOXEL_SHAPE_AGGREGATE_WEST;
        else if(type == NORTH_EAST) return VOXEL_SHAPE_AGGREGATE_NORTH_EAST;
        else if(type == NORTH_WEST) return VOXEL_SHAPE_AGGREGATE_NORTH_WEST;
        else if(type == SOUTH_EAST) return VOXEL_SHAPE_AGGREGATE_SOUTH_EAST;
        else if(type == SOUTH_WEST) return VOXEL_SHAPE_AGGREGATE_SOUTH_WEST;
        else if(type == SOUTH) return VOXEL_SHAPE_AGGREGATE_SOUTH;
        else if(type == NORTH) return VOXEL_SHAPE_AGGREGATE_NORTH;

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof EldrinOrreryRouterBlockEntity orrery) {
            EldrinOrreryBlockEntity master = orrery.getMaster();
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
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        EldrinOrreryRouterType type = EldrinOrreryBlock.unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_ELDRIN_ORRERY));
        return (type == ABOVE || type == DOUBLE_ABOVE) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ROUTER_TYPE_ELDRIN_ORRERY);
        pBuilder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    static {
        VOXEL_SHAPE_ABOVE = Block.box(4, 11, 4, 12, 16, 12);
        VOXEL_SHAPE_DOUBLE_ABOVE = Block.box(4, 0, 4, 12, 5, 12);

        VOXEL_SHAPE_EAST_BASE = Block.box(0, 0, 0, 9.729, 8, 16);
        VOXEL_SHAPE_EAST_PROTRUSION = Block.box(0, 0, 4, 14, 12, 12);
        VOXEL_SHAPE_EAST_PROTRUSION_EDGE = Block.box(0, 0, 3, 15, 3, 13);
        VOXEL_SHAPE_EAST_DAIS = Block.box(0, 0, 0, 6.943, 16, 16);

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                VOXEL_SHAPE_EAST_BASE,
                VOXEL_SHAPE_EAST_PROTRUSION,
                VOXEL_SHAPE_EAST_PROTRUSION_EDGE,
                VOXEL_SHAPE_EAST_DAIS
        );

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EAST_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EAST_PROTRUSION, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EAST_PROTRUSION_EDGE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_EAST_DAIS, 2)
        );

        VOXEL_SHAPE_CORNER_BASE = Block.box(0, 0, 0, 8, 8, 4);
        VOXEL_SHAPE_CORNER_PROTRUSION = Block.box(0, 0, 3, 4.964, 12, 11.552);
        VOXEL_SHAPE_CORNER_PROTRUSION_EDGE = Block.box(0, 0, 0, 5.830, 3, 12.419);
        VOXEL_SHAPE_DAIS = Block.box(0, 0, 0, 4.636, 16, 4.636);

        VOXEL_SHAPE_AGGREGATE_SOUTH_EAST = Shapes.or(
                VOXEL_SHAPE_CORNER_BASE,
                VOXEL_SHAPE_CORNER_PROTRUSION,
                VOXEL_SHAPE_CORNER_PROTRUSION_EDGE,
                VOXEL_SHAPE_DAIS
        );

        VOXEL_SHAPE_AGGREGATE_NORTH_EAST = Shapes.or(
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_CORNER_BASE),
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_CORNER_PROTRUSION),
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_CORNER_PROTRUSION_EDGE),
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_DAIS)
        );

        VOXEL_SHAPE_AGGREGATE_NORTH_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORNER_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORNER_PROTRUSION, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORNER_PROTRUSION_EDGE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS, 2)
        );

        VOXEL_SHAPE_AGGREGATE_SOUTH_WEST = Shapes.or(
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CORNER_BASE),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CORNER_PROTRUSION),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_CORNER_PROTRUSION_EDGE),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_DAIS)
        );

        VOXEL_SHAPE_SOUTH_BASE = Block.box(0,0,0,16,8,8.856);
        VOXEL_SHAPE_SOUTH_BASE_EDGE = Block.box(0,0,0,16,3,9.856);
        VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT = Block.box(0,0,0,3.992,12,10.981);
        VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT_EDGE = Block.box(0,0,0,3.992,3,11.7128);
        VOXEL_SHAPE_SOUTH_DAIS = Block.box(0,0,0,16,16,6.943);

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                VOXEL_SHAPE_SOUTH_BASE,
                VOXEL_SHAPE_SOUTH_BASE_EDGE,
                VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT,
                VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT_EDGE,
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT),
                MathHelper.flipVoxelShapeX(VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT_EDGE),
                VOXEL_SHAPE_SOUTH_DAIS
        );

        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_SOUTH_BASE),
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_SOUTH_BASE_EDGE),
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT),
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT_EDGE),
                MathHelper.flipVoxelShapeZ(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT)),
                MathHelper.flipVoxelShapeZ(MathHelper.flipVoxelShapeX(VOXEL_SHAPE_SOUTH_PROTRUSION_LEFT_EDGE)),
                MathHelper.flipVoxelShapeZ(VOXEL_SHAPE_SOUTH_DAIS)
        );
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.ELDRIN_ORRERY.get());
    }
}