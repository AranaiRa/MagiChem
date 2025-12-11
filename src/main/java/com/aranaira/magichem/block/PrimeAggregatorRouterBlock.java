package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.routers.PrimeAggregatorRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.PrimeAggregatorRouterType;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class PrimeAggregatorRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public PrimeAggregatorRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(MagiChemBlockStateProperties.ROUTER_TYPE_PRIME_AGGREGATOR, BlockStateProperties.HORIZONTAL_FACING);
    }

    public static int mapRouterTypeToInt(PrimeAggregatorRouterType type) {
        if(type == null) return 0;

        return switch(type) {
            case NONE -> 0;
            case FRONT -> 1;
            case FRONT_LEFT -> 2;
            case FRONT_RIGHT -> 3;
            case PLUG_LEFT -> 4;
            case PLUG_RIGHT -> 5;
            case REAR -> 6;
            case REAR_LEFT -> 7;
            case REAR_RIGHT -> 8;
        };
    }

    public static PrimeAggregatorRouterType unmapRouterTypeFromInt(int value) {
        return switch(value) {
            case 1 -> PrimeAggregatorRouterType.FRONT;
            case 2 -> PrimeAggregatorRouterType.FRONT_LEFT;
            case 3 -> PrimeAggregatorRouterType.FRONT_RIGHT;
            case 4 -> PrimeAggregatorRouterType.PLUG_LEFT;
            case 5 -> PrimeAggregatorRouterType.PLUG_RIGHT;
            case 6 -> PrimeAggregatorRouterType.REAR;
            case 7 -> PrimeAggregatorRouterType.REAR_LEFT;
            case 8 -> PrimeAggregatorRouterType.REAR_RIGHT;
            default -> null;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PrimeAggregatorRouterBlockEntity(pPos, pState);
    }
}
