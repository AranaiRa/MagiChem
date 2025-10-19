package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.SkywrathCondenserBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class SkywrathCondenserBlock extends BaseEntityBlock implements ISpellInteractibleBlock<SkywrathCondenserBlock> {
    public SkywrathCondenserBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SkywrathCondenserBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        if(pLevel.hasNeighborSignal(pPos)) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if(be instanceof SkywrathCondenserBlockEntity condenser) {
                condenser.tryTriggerCraft(false);
            }
        }

        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public boolean onHitBySpell(Level level, BlockPos blockPos, ISpellDefinition iSpellDefinition) {
        for(IModifiedSpellPart isp : iSpellDefinition.getComponents()){
            if(isp.getPart().equals(Components.LIGHTNING_DAMAGE)) {
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof SkywrathCondenserBlockEntity condenser) {
                    condenser.tryTriggerCraft(true);
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.SKYWRATH_CONDENSER_BE.get(),
                SkywrathCondenserBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof SkywrathCondenserBlockEntity condenser) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, condenser, pPos);
            } else {
                throw new IllegalStateException("SkywrathCondenserBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }
}
