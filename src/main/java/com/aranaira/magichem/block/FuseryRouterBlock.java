package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.block.entity.routers.FuseryRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.FuseryRouterType;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FuseryRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public FuseryRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    public static VoxelShape
            VOXEL_SHAPE_LEFT_BASE, VOXEL_SHAPE_LEFT_PLUG, VOXEL_SHAPE_LEFT_URN_FOOT, VOXEL_SHAPE_LEFT_URN_LOW, VOXEL_SHAPE_LEFT_URN_HIGH,
            VOXEL_SHAPE_RIGHT_BASE, VOXEL_SHAPE_RIGHT_PLUG, VOXEL_SHAPE_RIGHT_FOOT_BACK, VOXEL_SHAPE_RIGHT_FOOT_FORE, VOXEL_SHAPE_RIGHT_TANKS_BACK, VOXEL_SHAPE_RIGHT_TANKS_FORE,
            VOXEL_SHAPE_COG_BASE, VOXEL_SHAPE_COG_COG, VOXEL_SHAPE_COG_FOOT_BACK, VOXEL_SHAPE_COG_FOOT_FORE, VOXEL_SHAPE_COG_TANKS_BACK, VOXEL_SHAPE_COG_TANKS_FORE,
            VOXEL_SHAPE_TANK_RIGHT_BACK, VOXEL_SHAPE_TANK_RIGHT_FORE, VOXEL_SHAPE_TANK_RIGHT_TOP,
            VOXEL_SHAPE_TANK_ACROSS_BACK, VOXEL_SHAPE_TANK_ACROSS_FORE,
            VOXEL_SHAPE_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_COG_AGGREGATE_NORTH,
            VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_COG_AGGREGATE_SOUTH,
            VOXEL_SHAPE_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_COG_AGGREGATE_EAST,
            VOXEL_SHAPE_LEFT_AGGREGATE_WEST, VOXEL_SHAPE_RIGHT_AGGREGATE_WEST, VOXEL_SHAPE_COG_AGGREGATE_WEST,
            VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_WEST,
            VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_NORTH, VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_EAST, VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_SOUTH, VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_WEST;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FuseryRouterBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        FuseryRouterBlockEntity frbe = (FuseryRouterBlockEntity) pLevel.getBlockEntity(pPos);
        if(frbe != null) {
            FuseryRouterType routerType = frbe.getRouterType();
            Direction facing = frbe.getFacing();

            //Again, switch statements always default here and I have no idea why
            if(routerType == FuseryRouterType.PLUG_LEFT) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_LEFT_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_LEFT_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_LEFT_AGGREGATE_WEST;
            } else if(frbe.getRouterType() == FuseryRouterType.PLUG_RIGHT) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_RIGHT_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_RIGHT_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_RIGHT_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_RIGHT_AGGREGATE_WEST;
            } else if(frbe.getRouterType() == FuseryRouterType.COG) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_COG_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_COG_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_COG_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_COG_AGGREGATE_WEST;
            } else if(frbe.getRouterType() == FuseryRouterType.TANK_RIGHT) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_WEST;
            } else if(frbe.getRouterType() == FuseryRouterType.TANK_ACROSS) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_WEST;
            }
        }
        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof FuseryRouterBlockEntity frbe) {
            if(frbe.getRouterType() == FuseryRouterType.COG) {
                frbe.getMaster().activateCog();
            } else {
                FuseryBlockEntity master = frbe.getMaster();
                return master.getBlockState().getBlock().use(master.getBlockState(), pLevel, master.getBlockPos(), pPlayer, pHand, pHit);
            }
            pPlayer.swing(InteractionHand.MAIN_HAND);
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
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.FUSERY.get());
    }

    static {
        VOXEL_SHAPE_LEFT_BASE = Block.box(2.0D, 0.0D,  0.0D, 16.0D, 8.0D, 16.0D);
        VOXEL_SHAPE_LEFT_PLUG = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
        VOXEL_SHAPE_LEFT_URN_FOOT = Block.box(4.0D, 8.0D, 4.0D, 12.0D, 10.0D, 12.0D);
        VOXEL_SHAPE_LEFT_URN_LOW = Block.box(5.0D, 0.5D, 5.0D, 11.0D, 14.0D, 12.0D);
        VOXEL_SHAPE_LEFT_URN_HIGH = Block.box(6.0D, 14.0D, 6.0D, 10.0D, 16.0D, 10.0D);
        VOXEL_SHAPE_LEFT_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_LEFT_BASE,
                VOXEL_SHAPE_LEFT_PLUG,
                VOXEL_SHAPE_LEFT_URN_FOOT,
                VOXEL_SHAPE_LEFT_URN_LOW,
                VOXEL_SHAPE_LEFT_URN_HIGH);
        VOXEL_SHAPE_LEFT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PLUG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_URN_FOOT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_URN_LOW, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_URN_HIGH, 1));
        VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PLUG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_URN_FOOT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_URN_LOW, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_URN_HIGH, 2));
        VOXEL_SHAPE_LEFT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PLUG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_URN_FOOT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_URN_LOW, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_URN_HIGH, 3));

        VOXEL_SHAPE_RIGHT_BASE = Block.box(0.0D, 0.0D,  2.0D, 16.0D, 8.0D, 16.0D);
        VOXEL_SHAPE_RIGHT_PLUG = Block.box(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        VOXEL_SHAPE_RIGHT_FOOT_BACK = Block.box(0.0D, 8.0D, 3.0D, 12.0D, 10.0D, 9.0D);
        VOXEL_SHAPE_RIGHT_FOOT_FORE = Block.box(0.0D, 8.0D, 9.0D, 7.0D, 10.0D, 14.0D);
        VOXEL_SHAPE_RIGHT_TANKS_BACK = Block.box(0.0D, 10.0D, 4.0D, 9.0D, 16.0D, 8.0D);
        VOXEL_SHAPE_RIGHT_TANKS_FORE = Block.box(0.0D, 10.0D, 8.0D, 6.5D, 16.0D, 13.0D);
        VOXEL_SHAPE_RIGHT_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_RIGHT_BASE,
                VOXEL_SHAPE_RIGHT_PLUG,
                VOXEL_SHAPE_RIGHT_FOOT_BACK,
                VOXEL_SHAPE_RIGHT_FOOT_FORE,
                VOXEL_SHAPE_RIGHT_TANKS_BACK,
                VOXEL_SHAPE_RIGHT_TANKS_FORE);
        VOXEL_SHAPE_RIGHT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_PLUG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_FOOT_BACK, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_FOOT_FORE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_TANKS_BACK, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_TANKS_FORE, 1));
        VOXEL_SHAPE_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_PLUG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_FOOT_BACK, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_FOOT_FORE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_TANKS_BACK, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_TANKS_FORE, 2));
        VOXEL_SHAPE_RIGHT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_PLUG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_FOOT_BACK, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_FOOT_FORE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_TANKS_BACK, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_TANKS_FORE, 3));

        VOXEL_SHAPE_COG_BASE = Block.box(2.0D, 0.0D,  2.0D, 16.0D, 8.0D, 16.0D);
        VOXEL_SHAPE_COG_COG = Block.box(4.0D, 8.0D, 4.0D, 5.0D, 15.0D, 19.0D);
        VOXEL_SHAPE_COG_FOOT_BACK = Block.box(10.0D, 8.0D, 3.0D, 16.0D, 10.0D, 9.0D);
        VOXEL_SHAPE_COG_FOOT_FORE = Block.box(13.0D, 8.0D, 9.0D, 16.0D, 10.0D, 14.0D);
        VOXEL_SHAPE_COG_TANKS_BACK = Block.box(11.0D, 10.0D, 4.0D, 15.0D, 16.0D, 8.0D);
        VOXEL_SHAPE_COG_TANKS_FORE = Block.box(13.5D, 10.0D, 8.0D, 16.0D, 16.0D, 13.0D);
        VOXEL_SHAPE_COG_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_COG_BASE,
                VOXEL_SHAPE_COG_COG,
                VOXEL_SHAPE_COG_FOOT_BACK,
                VOXEL_SHAPE_COG_FOOT_FORE,
                VOXEL_SHAPE_COG_TANKS_BACK,
                VOXEL_SHAPE_COG_TANKS_FORE);
        VOXEL_SHAPE_COG_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_COG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_FOOT_BACK, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_FOOT_FORE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_TANKS_BACK, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_TANKS_FORE, 1));
        VOXEL_SHAPE_COG_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_COG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_FOOT_BACK, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_FOOT_FORE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_TANKS_BACK, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_TANKS_FORE, 2));
        VOXEL_SHAPE_COG_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_COG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_FOOT_BACK, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_FOOT_FORE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_TANKS_BACK, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_TANKS_FORE, 3));

        VOXEL_SHAPE_TANK_RIGHT_BACK = Block.box(0.0D, 0.0D, 4.0D, 9.0D, 4.0D, 8.0D);
        VOXEL_SHAPE_TANK_RIGHT_FORE = Block.box(0.0D, 0.0D, 8.0D, 6.5D, 2.0D, 13.0D);
        VOXEL_SHAPE_TANK_RIGHT_TOP = Block.box(0.0D, 4.0D, 4.0D, 4.0D, 6.0D, 8.0D);
        VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_TANK_RIGHT_BACK,
                VOXEL_SHAPE_TANK_RIGHT_FORE,
                VOXEL_SHAPE_TANK_RIGHT_TOP);
        VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_RIGHT_BACK, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_RIGHT_FORE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_RIGHT_TOP, 1));
        VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_RIGHT_BACK, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_RIGHT_FORE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_RIGHT_TOP, 2));
        VOXEL_SHAPE_TANK_RIGHT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_RIGHT_BACK, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_RIGHT_FORE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_RIGHT_TOP, 3));

        VOXEL_SHAPE_TANK_ACROSS_BACK = Block.box(11.0D, 0.0D, 4.0D, 16.0D, 4.0D, 8.0D);
        VOXEL_SHAPE_TANK_ACROSS_FORE = Block.box(13.5D, 0.0D, 8.0D, 16.0D, 2.0D, 13.0D);
        VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_TANK_ACROSS_BACK,
                VOXEL_SHAPE_TANK_ACROSS_FORE);
        VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_ACROSS_BACK, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_ACROSS_FORE, 1));
        VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_ACROSS_BACK, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_ACROSS_FORE, 2));
        VOXEL_SHAPE_TANK_ACROSS_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_ACROSS_BACK, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_ACROSS_FORE, 3));
    }
}