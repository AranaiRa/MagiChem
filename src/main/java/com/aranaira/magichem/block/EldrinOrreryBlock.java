package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.EldrinOrreryBlockEntity;
import com.aranaira.magichem.foundation.saveddata.EldrinOrreryLimiterSD;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class EldrinOrreryBlock extends BaseEntityBlock {
    public EldrinOrreryBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EldrinOrreryBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof EldrinOrreryBlockEntity orrery) {
                NetworkHooks.openScreen((ServerPlayer)player, (EldrinOrreryBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("EldrinOrreryBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if(pContext.getPlayer() != null && !pContext.getLevel().isClientSide()) {
            final EldrinOrreryLimiterSD eldrinOrreryData = pContext.getLevel().getServer().overworld().getDataStorage().computeIfAbsent(EldrinOrreryLimiterSD::load, EldrinOrreryLimiterSD::create, "eldrinOrreryData");
            if(!eldrinOrreryData.playerHasOrrery(pContext.getPlayer())) {
                eldrinOrreryData.addOrrery(pContext.getPlayer());
                return super.getStateForPlacement(pContext);
            }
            else
                pContext.getPlayer().sendSystemMessage(Component.translatable("feedback.block.eldrin_orrery.over_limit"));
        }
        return null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if(level.getBlockEntity(pos) instanceof EldrinOrreryBlockEntity orrery) {
//                orrery.packInventoryToBlockItem();
                if(!level.isClientSide()) {
                    final EldrinOrreryLimiterSD eldrinOrreryData = level.getServer().overworld().getDataStorage().computeIfAbsent(EldrinOrreryLimiterSD::load, EldrinOrreryLimiterSD::create, "eldrinOrreryData");
                    eldrinOrreryData.removeOrrery(orrery.getPlacedBy().toString());
                }

            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);

        if(pLevel.getBlockEntity(pPos) instanceof EldrinOrreryBlockEntity orrery) {
            if(pPlacer instanceof Player p) {
                orrery.setPlacedBy(p);
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.ELDRIN_ORRERY_BE.get(),
                EldrinOrreryBlockEntity::tick);
    }
}
