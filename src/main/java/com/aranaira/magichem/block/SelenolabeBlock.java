package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.SelenolabeBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SelenolabeBlock extends BaseEntityBlock {
    public static final VoxelShape
        VOXEL_SHAPE_BASE, VOXEL_SHAPE_MOON, VOXEL_SHAPE_AGGREGATE;

    public SelenolabeBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        if(pLevel.getBlockEntity(pPos) instanceof SelenolabeBlockEntity sel && sel.getLevel() != null) {
            return getSignalFromPhase(getMoonPhaseFromWorldTime(sel.getLevel()));
        }
        return super.getSignal(pState, pLevel, pPos, pDirection);
    }

    public static MoonPhase getMoonPhaseFromWorldTime(Level pLevel) {
        int day = (int)(pLevel.getDayTime() / 24000) % 8;

        return switch (day) {
            case 4 -> MoonPhase.NEW_MOON;
            case 3 -> MoonPhase.WANING_CRESCENT;
            case 5 -> MoonPhase.WAXING_CRESCENT;
            case 2 -> MoonPhase.WANING_HALF;
            case 6 -> MoonPhase.WAXING_HALF;
            case 1 -> MoonPhase.WANING_GIBBOUS;
            case 7 -> MoonPhase.WAXING_GIBBOUS;
            case 0 -> MoonPhase.FULL_MOON;
            default -> MoonPhase.NONE;
        };
    }

    private int getSignalFromPhase(MoonPhase pPhase) {
        return switch (pPhase) {
            case NEW_MOON -> 1;
            case WANING_CRESCENT -> 2;
            case WAXING_CRESCENT -> 3;
            case WANING_HALF -> 4;
            case WAXING_HALF -> 5;
            case WANING_GIBBOUS -> 6;
            case WAXING_GIBBOUS -> 7;
            case FULL_MOON -> 8;
            default -> 0;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SelenolabeBlockEntity(pPos, pState);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.SELENOLABE_BE.get(),
                SelenolabeBlockEntity::tick);
    }

    static {
        VOXEL_SHAPE_BASE = Block.box(0.757, 0, 0.757, 15.242, 5, 15.242);
        VOXEL_SHAPE_MOON = Block.box(4, 6, 4, 12, 14, 12);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(VOXEL_SHAPE_BASE, VOXEL_SHAPE_MOON);
    }

    public enum MoonPhase {
        NONE,
        NEW_MOON,
        WANING_CRESCENT,
        WAXING_CRESCENT,
        WANING_HALF,
        WAXING_HALF,
        WANING_GIBBOUS,
        WAXING_GIBBOUS,
        FULL_MOON
    }
}
