package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AcidBasinBlockEntity;
import com.aranaira.magichem.block.entity.routers.AcidBasinRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.AcidBasinRouterType;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_ACID_BASIN;

public class AcidBasinBlock extends BaseEntityBlock {
    public AcidBasinBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for(Pair<BlockPos, AcidBasinRouterType> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.ACID_BASIN_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Pair<BlockPos, AcidBasinRouterType> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                pLevel.setBlock(targetPos, state
                                .setValue(ROUTER_TYPE_ACID_BASIN, posAndType.getSecond().ordinal())
                                .setValue(FACING, facing)
                        , 3);
                ((AcidBasinRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos);
            }
        }
    }

    public static List<Pair<BlockPos, AcidBasinRouterType>> getRouterOffsets(Direction pFacing) {
        List<Pair<BlockPos, AcidBasinRouterType>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);

        if(pFacing == Direction.NORTH) {
            offsets.add(new Pair<>(origin.west(), AcidBasinRouterType.MAIN_TANK));
            offsets.add(new Pair<>(origin.west().above(), AcidBasinRouterType.MAIN_TANK_ABOVE));
            offsets.add(new Pair<>(origin.west().south(), AcidBasinRouterType.OUTPUT_TANK));
            offsets.add(new Pair<>(origin.west().south().above(), AcidBasinRouterType.OUTPUT_TANK_ABOVE));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Pair<>(origin.north(), AcidBasinRouterType.MAIN_TANK));
            offsets.add(new Pair<>(origin.north().above(), AcidBasinRouterType.MAIN_TANK_ABOVE));
            offsets.add(new Pair<>(origin.north().west(), AcidBasinRouterType.OUTPUT_TANK));
            offsets.add(new Pair<>(origin.north().west().above(), AcidBasinRouterType.OUTPUT_TANK_ABOVE));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Pair<>(origin.east(), AcidBasinRouterType.MAIN_TANK));
            offsets.add(new Pair<>(origin.east().above(), AcidBasinRouterType.MAIN_TANK_ABOVE));
            offsets.add(new Pair<>(origin.east().north(), AcidBasinRouterType.OUTPUT_TANK));
            offsets.add(new Pair<>(origin.east().north().above(), AcidBasinRouterType.OUTPUT_TANK_ABOVE));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Pair<>(origin.south(), AcidBasinRouterType.MAIN_TANK));
            offsets.add(new Pair<>(origin.south().above(), AcidBasinRouterType.MAIN_TANK_ABOVE));
            offsets.add(new Pair<>(origin.south().east(), AcidBasinRouterType.OUTPUT_TANK));
            offsets.add(new Pair<>(origin.south().east().above(), AcidBasinRouterType.OUTPUT_TANK_ABOVE));
        }

        return offsets;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AcidBasinBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
