package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.MateriaManifestBlockEntity;
import com.aranaira.magichem.block.entity.routers.MateriaManifestRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class MateriaManifestBlock extends BaseEntityBlock {

    public static final VoxelShape VOXEL_SHAPE = Block.box(4.5, 0, 4.5, 11.5, 16, 11.5);

    public MateriaManifestBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MateriaManifestBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState state = this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());

        if(!pContext.getLevel().isEmptyBlock(pContext.getClickedPos().above())) {
            return null;
        }

        return state;
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
        Direction facing = pNewState.getValue(FACING);
        BlockState state = BlockRegistry.MATERIA_MANIFEST_ROUTER.get().defaultBlockState();
        state = state.setValue(FACING, facing);

        BlockPos targetPos = pPos.offset(0,1,0);
        pLevel.setBlock(targetPos, state, 3);
        ((MateriaManifestRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            boolean holdingMarkPair = player.getInventory().getSelected().getItem() == ItemInit.RUNE_MARKING_PAIR.get();

            if(!holdingMarkPair) {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof MateriaManifestBlockEntity) {
                    NetworkHooks.openScreen((ServerPlayer) player, (MateriaManifestBlockEntity) entity, pos);
                } else {
                    throw new IllegalStateException("MateriaManifestBlockEntity container provider is missing!");
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
