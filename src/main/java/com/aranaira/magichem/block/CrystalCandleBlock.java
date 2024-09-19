package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ConjurerBlockEntity;
import com.aranaira.magichem.block.entity.CrystalCandleBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.CANDLE_COUNT;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class CrystalCandleBlock extends BaseEntityBlock {
    public CrystalCandleBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(CANDLE_COUNT, 1)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CANDLE_COUNT);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(pPlayer.getItemInHand(pHand).getItem() == BlockRegistry.CRYSTAL_CANDLE.get().asItem()) {
            int count = pState.getValue(CANDLE_COUNT);
            if (count < 6) {
                if(!pPlayer.isCreative())
                    pPlayer.getItemInHand(pHand).shrink(1);

                BlockState newState = pState.setValue(CANDLE_COUNT, count + 1);
                pLevel.setBlock(pPos, newState, 3);
                pLevel.sendBlockUpdated(pPos, pState, newState, 3);
                return InteractionResult.CONSUME;
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(newState.isAir()) {
            ItemStack stack = new ItemStack(BlockRegistry.CRYSTAL_CANDLE.get(), state.getValue(CANDLE_COUNT));
            ItemEntity ie = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            level.addFreshEntity(ie);
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CrystalCandleBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int count = pState.getValue(CANDLE_COUNT);
        if(count == 1)
            return Block.box(7, 3, 7, 9, 10, 9);
        else
            return Block.box(3, 3, 3, 13, 10, 13);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }
}
