package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AcidBasinBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

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
//        BlockPos pos = pContext.getClickedPos();
//
//        for(Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
//            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
//                return null;
//            }
//        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
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
}
