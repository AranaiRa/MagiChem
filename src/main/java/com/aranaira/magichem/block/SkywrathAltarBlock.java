package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.SkywrathAltarBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
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
        if(pPlayer.getItemInHand(InteractionHand.MAIN_HAND).getItem() != ItemRegistry.THUNDERSTONE.get()) {
            if (pLevel.getBlockEntity(pPos) instanceof SkywrathAltarBlockEntity sabe && pHand == InteractionHand.MAIN_HAND) {
                ItemStack stackQuery = pPlayer.getItemInHand(pHand);
                sabe.setHeldItem(pPlayer, stackQuery);
                pPlayer.setItemInHand(pHand, ItemStack.EMPTY);

                return InteractionResult.CONSUME;
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
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
