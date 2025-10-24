package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.SkywrathAltarBlockEntity;
import com.aranaira.magichem.block.entity.SkywrathCondenserBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SkywrathAltarBlock extends BaseEntityBlock {
    public SkywrathAltarBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SkywrathAltarBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        final ItemStack stackInHand = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if(stackInHand.getItem() == BlockRegistry.SKYWRATH_CONDENSER.get().asItem()) {
            BlockPos posQuery = pPos.offset(0, 3, 0);
            if(pLevel.getBlockState(posQuery).isAir()) {
                pLevel.setBlock(posQuery, BlockRegistry.SKYWRATH_CONDENSER.get().defaultBlockState(), 3);
                if(stackInHand.hasTag() && pLevel.getBlockEntity(posQuery) instanceof SkywrathCondenserBlockEntity condenser) {
                    condenser.unpackDataFromNBT(stackInHand.getTag());
                }
                stackInHand.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        else if(stackInHand.getItem() != ItemRegistry.THUNDERSTONE.get()) {
            if (pLevel.getBlockEntity(pPos) instanceof SkywrathAltarBlockEntity sabe && pHand == InteractionHand.MAIN_HAND) {
                ItemStack stackQuery = pPlayer.getItemInHand(pHand);
                int idealCount = sabe.getIdealInsertingAmount(stackQuery, 0);
                sabe.setHeldItem(pPlayer, stackQuery.copyWithCount(idealCount));
                pPlayer.setItemInHand(pHand, stackQuery.copyWithCount(stackQuery.getCount() - idealCount));

                return InteractionResult.CONSUME;
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof SkywrathAltarBlockEntity sabe) {
                sabe.dropInventory();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pBlockEntityType == BlockEntitiesRegistry.SKYWRATH_ALTAR_BE.get()) {
            return SkywrathAltarBlockEntity::tick;
        }

        return null;
    }
}
