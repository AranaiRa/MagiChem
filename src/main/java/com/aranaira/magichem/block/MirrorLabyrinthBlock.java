package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity;
import com.aranaira.magichem.block.entity.routers.MirrorLabyrinthRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.MirrorLabyrinthRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_MIRROR_LABYRINTH;
import static com.aranaira.magichem.foundation.enums.MirrorLabyrinthRouterType.*;

public class MirrorLabyrinthBlock extends BaseEntityBlock {
    public MirrorLabyrinthBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MirrorLabyrinthBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for(Pair<BlockPos, MirrorLabyrinthRouterType> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.MIRROR_LABYRINTH_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Pair<BlockPos, MirrorLabyrinthRouterType> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                int routerType = MirrorLabyrinthRouterBlock.mapRouterTypeToInt(posAndType.getSecond());

                pLevel.setBlock(
                        targetPos,
                        state
                                .setValue(FACING, facing)
                                .setValue(ROUTER_TYPE_MIRROR_LABYRINTH, routerType),
                        3);
                ((MirrorLabyrinthRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos);
            }
        }
    }

    public static List<Pair<BlockPos, MirrorLabyrinthRouterType>> getRouterOffsets(Direction pFacing) {
        List<Pair<BlockPos, MirrorLabyrinthRouterType>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);
        if(pFacing == Direction.NORTH) {
            offsets.add(new Pair<>(origin.south(), DAIS));
            offsets.add(new Pair<>(origin.west(), LEFT_FRONT));
            offsets.add(new Pair<>(origin.east(), RIGHT_FRONT));
            offsets.add(new Pair<>(origin.north().west(), LEFT));
            offsets.add(new Pair<>(origin.north().east(), RIGHT));
            offsets.add(new Pair<>(origin.north().north(), CENTER_BACK));
            offsets.add(new Pair<>(origin.north().north().west(), LEFT_BACK));
            offsets.add(new Pair<>(origin.north().north().east(), RIGHT_BACK));
            offsets.add(new Pair<>(origin.north().above(), CONSTRUCT_LOWER));
            offsets.add(new Pair<>(origin.north().above().above(), CONSTRUCT_UPPER));
            offsets.add(new Pair<>(origin.north().offset(0,3,0), MATRIX_UPPER));
            offsets.add(new Pair<>(origin.north().offset(0,4,0), MATRIX_UPPER));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Pair<>(origin.north(), DAIS));
            offsets.add(new Pair<>(origin.east(), LEFT_FRONT));
            offsets.add(new Pair<>(origin.west(), RIGHT_FRONT));
            offsets.add(new Pair<>(origin.south().east(), LEFT));
            offsets.add(new Pair<>(origin.south().west(), RIGHT));
            offsets.add(new Pair<>(origin.south().south(), CENTER_BACK));
            offsets.add(new Pair<>(origin.south().south().east(), LEFT_BACK));
            offsets.add(new Pair<>(origin.south().south().west(), RIGHT_BACK));
            offsets.add(new Pair<>(origin.south().above(), CONSTRUCT_LOWER));
            offsets.add(new Pair<>(origin.south().above().above(), CONSTRUCT_UPPER));
            offsets.add(new Pair<>(origin.south().offset(0,3,0), MATRIX_UPPER));
            offsets.add(new Pair<>(origin.south().offset(0,4,0), MATRIX_UPPER));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Pair<>(origin.west(), DAIS));
            offsets.add(new Pair<>(origin.north(), LEFT_FRONT));
            offsets.add(new Pair<>(origin.south(), RIGHT_FRONT));
            offsets.add(new Pair<>(origin.east().north(), LEFT));
            offsets.add(new Pair<>(origin.east().south(), RIGHT));
            offsets.add(new Pair<>(origin.east().east(), CENTER_BACK));
            offsets.add(new Pair<>(origin.east().east().north(), LEFT_BACK));
            offsets.add(new Pair<>(origin.east().east().south(), RIGHT_BACK));
            offsets.add(new Pair<>(origin.east().above(), CONSTRUCT_LOWER));
            offsets.add(new Pair<>(origin.east().above().above(), CONSTRUCT_UPPER));
            offsets.add(new Pair<>(origin.east().offset(0,3,0), MATRIX_UPPER));
            offsets.add(new Pair<>(origin.east().offset(0,4,0), MATRIX_UPPER));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Pair<>(origin.east(), DAIS));
            offsets.add(new Pair<>(origin.south(), LEFT_FRONT));
            offsets.add(new Pair<>(origin.north(), RIGHT_FRONT));
            offsets.add(new Pair<>(origin.west().south(), LEFT));
            offsets.add(new Pair<>(origin.west().north(), RIGHT));
            offsets.add(new Pair<>(origin.west().west(), CENTER_BACK));
            offsets.add(new Pair<>(origin.west().west().south(), LEFT_BACK));
            offsets.add(new Pair<>(origin.west().west().north(), RIGHT_BACK));
            offsets.add(new Pair<>(origin.west().above(), CONSTRUCT_LOWER));
            offsets.add(new Pair<>(origin.west().above().above(), CONSTRUCT_UPPER));
            offsets.add(new Pair<>(origin.west().offset(0,3,0), MATRIX_UPPER));
            offsets.add(new Pair<>(origin.west().offset(0,4,0), MATRIX_UPPER));
        }
        return offsets;
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction pFacing) {
        for(Pair<BlockPos, MirrorLabyrinthRouterType> posAndType : getRouterOffsets(pFacing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlockEntitiesRegistry.MIRROR_LABYRINTH_BE.get(),
                MirrorLabyrinthBlockEntity::tick);
    }
}
