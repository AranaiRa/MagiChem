package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.routers.MirrorLabyrinthRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.MirrorLabyrinthRouterType;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_MIRROR_LABYRINTH;

public class MirrorLabyrinthRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public MirrorLabyrinthRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, ROUTER_TYPE_MIRROR_LABYRINTH);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MirrorLabyrinthRouterBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    public static int mapRouterTypeToInt(MirrorLabyrinthRouterType pRouterType) {
        if(pRouterType == null)
            return 0;

        return switch(pRouterType) {
            case DAIS -> 1;
            case CENTER -> 2;
            case LEFT_FRONT -> 3;
            case LEFT -> 4;
            case LEFT_BACK -> 5;
            case CENTER_BACK -> 6;
            case RIGHT_BACK -> 7;
            case RIGHT -> 8;
            case RIGHT_FRONT -> 9;
            case CONSTRUCT_LOWER -> 10;
            case CONSTRUCT_UPPER -> 11;
            case MATRIX_LOWER -> 12;
            case MATRIX_UPPER -> 13;
            default -> 0;
        };
    }

    public static MirrorLabyrinthRouterType unmapRouterTypeFromInt(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> MirrorLabyrinthRouterType.DAIS;
            case 2 -> MirrorLabyrinthRouterType.CENTER;
            case 3 -> MirrorLabyrinthRouterType.LEFT_FRONT;
            case 4 -> MirrorLabyrinthRouterType.LEFT;
            case 5 -> MirrorLabyrinthRouterType.LEFT_BACK;
            case 6 -> MirrorLabyrinthRouterType.CENTER_BACK;
            case 7 -> MirrorLabyrinthRouterType.RIGHT_BACK;
            case 8 -> MirrorLabyrinthRouterType.RIGHT;
            case 9 -> MirrorLabyrinthRouterType.RIGHT_FRONT;
            case 10 -> MirrorLabyrinthRouterType.CONSTRUCT_LOWER;
            case 11 -> MirrorLabyrinthRouterType.CONSTRUCT_UPPER;
            case 12 -> MirrorLabyrinthRouterType.MATRIX_LOWER;
            case 13 -> MirrorLabyrinthRouterType.MATRIX_UPPER;
            default -> MirrorLabyrinthRouterType.NONE;
        };
    }
}
