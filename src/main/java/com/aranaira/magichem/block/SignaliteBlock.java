package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.SignaliteBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SignaliteBlock extends BaseEntityBlock {
    public static final VoxelShape
        VOXEL_SHAPE_CORE,
        VOXEL_SHAPE_NORTH, VOXEL_SHAPE_EAST, VOXEL_SHAPE_SOUTH, VOXEL_SHAPE_WEST, VOXEL_SHAPE_UP, VOXEL_SHAPE_DOWN,
        VOXEL_SHAPE_AGGREGATE;

    private final SignaliteBlockType type;

    public SignaliteBlock(Properties pProperties, SignaliteBlockType pType) {
        super(pProperties);
        type = pType;
    }

    public SignaliteBlockType getType(){
        return type;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SignaliteBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(pLevel.isClientSide() && pHand == InteractionHand.MAIN_HAND) {
            double rawX = Math.abs(pHit.getLocation().x % 1);
            double rawY = Math.abs(pHit.getLocation().y % 1);
            double rawZ = Math.abs(pHit.getLocation().z % 1);

            int x = rawX > 0.625 ? 1 : rawX < 0.375 ? -1 : 0;
            int y = rawY > 0.625 ? 1 : rawY < 0.375 ? -1 : 0;
            int z = rawZ > 0.625 ? 1 : rawZ < 0.375 ? -1 : 0;

            Direction dir =
                    z < 0 ? Direction.NORTH :
                    z > 0 ? Direction.SOUTH :
                    x < 0 ? Direction.EAST :
                    x > 0 ? Direction.WEST :
                    y > 0 ? Direction.DOWN :
                    y < 0 ? Direction.UP :
                    null;

            if(dir != null) {
                BlockEntity be = pLevel.getBlockEntity(pPos);
                if(be instanceof SignaliteBlockEntity sbe) {
                    sbe.toggle(dir);
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    static {
        VOXEL_SHAPE_CORE = Block.box(6, 6, 6, 10, 10, 10);

        VOXEL_SHAPE_NORTH = Block.box(7, 7, 0.5, 9, 9, 8);
        VOXEL_SHAPE_SOUTH = Block.box(7, 7, 8, 9, 9, 15.5);
        VOXEL_SHAPE_EAST  = Block.box(0.5, 7, 7, 8, 9, 9);
        VOXEL_SHAPE_WEST  = Block.box(8, 7, 7, 15.5, 9, 9);
        VOXEL_SHAPE_UP    = Block.box(7, 0.5, 7, 9, 8, 9);
        VOXEL_SHAPE_DOWN  = Block.box(7, 8, 7, 9, 15.5, 9);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_CORE,
                VOXEL_SHAPE_NORTH,
                VOXEL_SHAPE_EAST,
                VOXEL_SHAPE_SOUTH,
                VOXEL_SHAPE_WEST,
                VOXEL_SHAPE_UP,
                VOXEL_SHAPE_DOWN
        );
    }

    public enum SignaliteBlockType {
        STANDARD,
        CHAOTIC,
        DEVOURING,
        GATEKEEPING,
        NEGATING
    }
}
