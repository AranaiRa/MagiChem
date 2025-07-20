package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.GnosticOrbBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.READY_FOR_COLLECTION;

public class GnosticOrbBlock extends BaseEntityBlock {
    public static final VoxelShape VOXEL_SHAPE = Block.box(4,4,4,12,12,12);

    public GnosticOrbBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(this.defaultBlockState()
                .setValue(READY_FOR_COLLECTION, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GnosticOrbBlockEntity(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(READY_FOR_COLLECTION);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        int range = 12;
        final BlockPos pos = pContext.getClickedPos();
        for(int y=pos.getY()-range;y<=pos.getY()+range;y++) {
            for(int x=pos.getX()-range;x<=pos.getX()+range;x++) {
                for(int z=pos.getZ()-range;z<=pos.getZ()+range;z++) {
                    if(pContext.getLevel().getBlockState(new BlockPos(x,y,z)).getBlock() == BlockRegistry.GNOSTIC_ORB.get()) {
                        if(!pContext.getLevel().isClientSide())
                            pContext.getPlayer().sendSystemMessage(Component.translatable("feedback.block.gnostic_orb.too_close_to_orb"));

                        return null;
                    }
                }
            }
        }

        return super.getStateForPlacement(pContext);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.GNOSTIC_ORB_BE.get(),
                GnosticOrbBlockEntity::tick);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        final ItemStack stack = pPlayer.getItemInHand(pHand);
        if(pLevel.getBlockEntity(pPos) instanceof GnosticOrbBlockEntity orb) {
            if(stack.getItem() == ItemRegistry.DEBUG_ORB.get()) {
                if(orb.hasProphecyCooking()) orb.skipToFullCharge();
            }
            else if (!pLevel.isClientSide()) {
                if (!orb.hasProphecyCooking()) {

                    if (stack.getItem() instanceof MateriaItem mi) {
                        boolean isLuck = mi.getMateriaName().equals("luck");

                        if (orb.tryStart(stack) && !pPlayer.isCreative()) {
                            stack.shrink(isLuck ? 20 : 50);
                            ItemEntity ie = new ItemEntity(pLevel, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), new ItemStack(Items.GLASS_BOTTLE, isLuck ? 20 : 50));
                            pLevel.addFreshEntity(ie);
                        }

                        return InteractionResult.CONSUME;
                    }
                } else if (orb.isProphecyReady()) {
                    orb.finalizeProphecy(pPlayer);
                    return InteractionResult.CONSUME;
                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return pState.getValue(READY_FOR_COLLECTION) ? 15 : 0;
    }
}
