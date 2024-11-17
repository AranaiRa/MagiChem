package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.SignaliteBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
        if(pPlayer.getItemInHand(pHand).getItem() instanceof MateriaItem)
            return InteractionResult.PASS;

        if(!pLevel.isClientSide() && pHand == InteractionHand.MAIN_HAND) {
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
                    updateSignalStrength(pLevel, pPos);
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return super.isSignalSource(pState);
    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        //Reminder, the direction in redstone signal requests are backwards
        if(be instanceof SignaliteBlockEntity sbe) {
            int signalStrength = pState.getValue(BlockStateProperties.POWER);

            if(pDirection == Direction.NORTH && sbe.connectedSouth)
                return signalStrength;
            if(pDirection == Direction.SOUTH && sbe.connectedNorth)
                return signalStrength;
            if(pDirection == Direction.EAST && sbe.connectedWest)
                return signalStrength;
            if(pDirection == Direction.WEST && sbe.connectedEast)
                return signalStrength;
            if(pDirection == Direction.UP && sbe.connectedDown)
                return signalStrength;
            if(pDirection == Direction.DOWN && sbe.connectedUp)
                return signalStrength;
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return super.getDirectSignal(pState, pLevel, pPos, pDirection);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);

        updateSignalStrength(pLevel, pPos);
    }

    private void updateSignalStrength(Level pLevel, BlockPos pPos) {
        if(pLevel.getBlockEntity(pPos) instanceof SignaliteBlockEntity sbe) {
            int signalStrength = 0;
            BlockState myState = pLevel.getBlockState(pPos);
            int oldSignalStrength = myState.getValue(BlockStateProperties.POWER);

            for (Direction dir : sbe.getTransmittingDirections()) {
                BlockPos posQuery = pPos.offset(dir.getNormal());
                BlockState stateToCheck = pLevel.getBlockState(posQuery);

                int signalQuery = 0;
                if (stateToCheck.hasProperty(BlockStateProperties.POWER))
                    signalQuery = Math.max(0, stateToCheck.getSignal(pLevel, posQuery, dir) - 1);

                if (stateToCheck.getBlock() == Blocks.REDSTONE_BLOCK || stateToCheck.getBlock() == BlockRegistry.SIGNALITE_BLOCK.get() || stateToCheck.hasProperty(BlockStateProperties.POWERED) && stateToCheck.getValue(BlockStateProperties.POWERED)) {
                    signalStrength = 15;
                    break;
                } else if (signalQuery > signalStrength) {
                    signalStrength = signalQuery;
                }
            }

            if(signalStrength != oldSignalStrength) {
                pLevel.setBlock(pPos, myState.setValue(BlockStateProperties.POWER, signalStrength), 3);
                pLevel.sendBlockUpdated(pPos, myState, myState.setValue(BlockStateProperties.POWER, signalStrength), 2);
            }

            sbe.syncAndSave();
        }
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);

        updateSignalStrength(pLevel, pPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(BlockStateProperties.POWER);
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
