package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
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

public class CentrifugeRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public CentrifugeRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    public static VoxelShape
            VOXEL_SHAPE_LEFT_BASE_NORTH, VOXEL_SHAPE_LEFT_PLUG_NORTH,
            VOXEL_SHAPE_RIGHT_BASE_NORTH, VOXEL_SHAPE_RIGHT_PLUG_NORTH,
            VOXEL_SHAPE_COG_BASE_NORTH, VOXEL_SHAPE_COG_COG_NORTH,
            VOXEL_SHAPE_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_RIGHT_AGGREGATE_NORTH, VOXEL_SHAPE_COG_AGGREGATE_NORTH,
            VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_RIGHT_AGGREGATE_SOUTH, VOXEL_SHAPE_COG_AGGREGATE_SOUTH,
            VOXEL_SHAPE_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_RIGHT_AGGREGATE_EAST, VOXEL_SHAPE_COG_AGGREGATE_EAST,
            VOXEL_SHAPE_LEFT_AGGREGATE_WEST, VOXEL_SHAPE_RIGHT_AGGREGATE_WEST, VOXEL_SHAPE_COG_AGGREGATE_WEST;
    private static final float FUZZ_FACTOR = 0.000625f;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CentrifugeRouterBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        CentrifugeRouterBlockEntity crbe = (CentrifugeRouterBlockEntity) pLevel.getBlockEntity(pPos);
        if(crbe != null) {
            CentrifugeRouterType routerType = crbe.getRouterType();
            Direction facing = crbe.getFacing();

            //Again, switch statements always default here and I have no idea why
            if(routerType == CentrifugeRouterType.PLUG_LEFT) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_LEFT_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_LEFT_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_LEFT_AGGREGATE_WEST;
            } else if(crbe.getRouterType() == CentrifugeRouterType.PLUG_RIGHT) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_RIGHT_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_RIGHT_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_RIGHT_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_RIGHT_AGGREGATE_WEST;
            } else if(crbe.getRouterType() == CentrifugeRouterType.COG) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_COG_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_COG_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_COG_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_COG_AGGREGATE_WEST;
            }
        }
        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof CentrifugeRouterBlockEntity crbe) {
            if(crbe.getRouterType() == CentrifugeRouterType.COG) {
                crbe.getMaster().activateCog();
            } else {
                CentrifugeBlockEntity master = crbe.getMaster();
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
        return new ItemStack(BlockRegistry.CENTRIFUGE.get());
    }

    static {
        VOXEL_SHAPE_LEFT_BASE_NORTH = Block.box(2.0D, 0.0D,  0.0D + FUZZ_FACTOR, 16.0D - FUZZ_FACTOR, 8.0D, 16.0D - FUZZ_FACTOR);
        VOXEL_SHAPE_LEFT_PLUG_NORTH = Block.box(0.0D + FUZZ_FACTOR, 0.0D + FUZZ_FACTOR, 12.0D, 16.0D - FUZZ_FACTOR, 16.0D - FUZZ_FACTOR, 16.0D - FUZZ_FACTOR);
        VOXEL_SHAPE_LEFT_AGGREGATE_NORTH = Shapes.or(VOXEL_SHAPE_LEFT_BASE_NORTH, new VoxelShape[]{VOXEL_SHAPE_LEFT_PLUG_NORTH});
        VOXEL_SHAPE_LEFT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 1), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PLUG_NORTH, 1)});
        VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 2), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PLUG_NORTH, 2)});
        VOXEL_SHAPE_LEFT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BASE_NORTH, 3), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PLUG_NORTH, 3)});

        VOXEL_SHAPE_RIGHT_BASE_NORTH = Block.box(0.0D + FUZZ_FACTOR, 0.0D,  2.0D, 16.0D - FUZZ_FACTOR, 8.0D, 16.0D - FUZZ_FACTOR);
        VOXEL_SHAPE_RIGHT_PLUG_NORTH = Block.box(12.0D, 0.0D + FUZZ_FACTOR, 0.0D + FUZZ_FACTOR, 16.0D - FUZZ_FACTOR, 16.0D - FUZZ_FACTOR, 16.0D - FUZZ_FACTOR);
        VOXEL_SHAPE_RIGHT_AGGREGATE_NORTH = Shapes.or(VOXEL_SHAPE_RIGHT_BASE_NORTH, new VoxelShape[]{VOXEL_SHAPE_RIGHT_PLUG_NORTH});
        VOXEL_SHAPE_RIGHT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_BASE_NORTH, 1), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_PLUG_NORTH, 1)});
        VOXEL_SHAPE_RIGHT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_BASE_NORTH, 2), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_PLUG_NORTH, 2)});
        VOXEL_SHAPE_RIGHT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_BASE_NORTH, 3), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_RIGHT_PLUG_NORTH, 3)});

        VOXEL_SHAPE_COG_BASE_NORTH = Block.box(2.0D, 0.0D,  2.0D, 16.0D, 8.0D, 16.0D);
        VOXEL_SHAPE_COG_COG_NORTH = Block.box(4.0D, 8.0D, 4.0D, 19.0D, 15.0D, 5.0D);
        VOXEL_SHAPE_COG_AGGREGATE_NORTH = Shapes.or(VOXEL_SHAPE_COG_BASE_NORTH, new VoxelShape[]{VOXEL_SHAPE_COG_COG_NORTH});
        VOXEL_SHAPE_COG_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_BASE_NORTH, 1), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_COG_NORTH, 1)});
        VOXEL_SHAPE_COG_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_BASE_NORTH, 2), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_COG_NORTH, 2)});
        VOXEL_SHAPE_COG_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_BASE_NORTH, 3), new VoxelShape[]{
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_COG_COG_NORTH, 3)});
    }
}