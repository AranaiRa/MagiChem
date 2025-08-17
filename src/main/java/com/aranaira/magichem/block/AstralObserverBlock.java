package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AstralObserverBlockEntity;
import com.aranaira.magichem.block.entity.SkywrathAltarBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class AstralObserverBlock extends BaseEntityBlock {
    public AstralObserverBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AstralObserverBlockEntity(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(MagiChemBlockStateProperties.NEEDS_HARD_UPDATE);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide() && pLevel.getBlockEntity(pPos) instanceof AstralObserverBlockEntity astral && pHand == InteractionHand.MAIN_HAND) {
            ItemStack stackQuery = pPlayer.getItemInHand(pHand);

            final LazyOptional<IItemHandler> capQuery = astral.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if(capQuery.isPresent()) {
                final IItemHandler cap = capQuery.resolve().get();

                if(stackQuery.isEmpty()) {
                    final ItemStack extractQuery = cap.extractItem(0, cap.getSlotLimit(0), false);
                    pPlayer.setItemInHand(pHand, extractQuery);
                } else if(stackQuery.getItem() == ItemRegistry.DEBUG_ORB.get()) {
                    astral.skipToFullCharge();
                } else {
                    if (!cap.getStackInSlot(0).isEmpty()) {
                        final ItemStack extractQuery = cap.extractItem(0, cap.getSlotLimit(0), false);
                        ItemEntity ie = new ItemEntity(pLevel, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), extractQuery);
                        pLevel.addFreshEntity(ie);
                    } else {
                        cap.insertItem(0, pPlayer.getItemInHand(pHand).copy(), false);
                        pPlayer.getItemInHand(pHand).shrink(1);
                    }
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        if(pLevel.getBlockEntity(pPos) instanceof AstralObserverBlockEntity astral) {
            return astral.getComparatorOutput();
        }
        return super.getAnalogOutputSignal(pState, pLevel, pPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof AstralObserverBlockEntity aobe) {
                aobe.dropInventory();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pBlockEntityType == BlockEntitiesRegistry.ASTRAL_OBSERVER_BE.get()) {
            return AstralObserverBlockEntity::tick;
        }

        return null;
    }
}
