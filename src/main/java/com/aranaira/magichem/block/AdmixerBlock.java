package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AdmixerBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class AdmixerBlock extends BaseEntityBlock {
    public AdmixerBlock(Properties properties) {
        super(properties);
    }

    private static final VoxelShape SHAPE_BASE;
    private static final VoxelShape SHAPE_CENTER;
    private static final VoxelShape SHAPE_AGGREGATE;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE_AGGREGATE;
    }

    /* BLOCK ENTITY STUFF BELOW THIS POINT*/

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof AdmixerBlockEntity) {
                ((AdmixerBlockEntity) blockEntity).dropInventoryToWorld();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof AdmixerBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer)player, (AdmixerBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("AdmixerBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdmixerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.ADMIXER_BE.get(),
                AdmixerBlockEntity::tick);
    }

    static {
        SHAPE_BASE = Block.box(0.0D, 0.0D,  0.0D, 16.0D, 11.5D, 16.0D);
        SHAPE_CENTER = Block.box(5.5D, 0.0D, 5.5D, 10.5D, 15.0D, 10.5D);
        SHAPE_AGGREGATE = Shapes.or(SHAPE_BASE, new VoxelShape[]{SHAPE_CENTER});
    }
}
