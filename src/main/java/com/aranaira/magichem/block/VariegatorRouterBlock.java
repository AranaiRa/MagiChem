package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import com.aranaira.magichem.block.entity.routers.ActuatorWaterRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.VariegatorRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.GrandDistilleryRouterType;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.GROUNDED;

public class VariegatorRouterBlock extends BaseEntityBlock implements INoCreativeTab {

    public VariegatorRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    public static final VoxelShape
            VOXEL_SHAPE_LAYER_1_UPRIGHT, VOXEL_SHAPE_LAYER_2_UPRIGHT, VOXEL_SHAPE_AGGREGATE_UPRIGHT,
            VOXEL_SHAPE_LAYER_1_REVERSED, VOXEL_SHAPE_LAYER_2_REVERSED, VOXEL_SHAPE_AGGREGATE_REVERSED;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new VariegatorRouterBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(GROUNDED);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof VariegatorRouterBlockEntity vrbe) {
            VariegatorBlockEntity master = vrbe.getMaster();
            pPlayer.swing(InteractionHand.MAIN_HAND);
            return master.getBlockState().getBlock().use(master.getBlockState(), pLevel, master.getBlockPos(), pPlayer, pHand, pHit);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.VARIEGATOR.get());
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if(pState.getValue(MagiChemBlockStateProperties.GROUNDED)) {
            return VOXEL_SHAPE_AGGREGATE_UPRIGHT;
        } else {
            return VOXEL_SHAPE_AGGREGATE_REVERSED;
        }
    }

    static {
        VOXEL_SHAPE_LAYER_1_UPRIGHT = Block.box(5, 0, 5, 11, 4, 11);
        VOXEL_SHAPE_LAYER_2_UPRIGHT = Block.box(6, 4, 6, 10, 9, 10);

        VOXEL_SHAPE_AGGREGATE_UPRIGHT = Shapes.or(VOXEL_SHAPE_LAYER_1_UPRIGHT, VOXEL_SHAPE_LAYER_2_UPRIGHT);

        VOXEL_SHAPE_LAYER_1_REVERSED = Block.box(5, 12, 5, 11, 16, 11);
        VOXEL_SHAPE_LAYER_2_REVERSED = Block.box(6, 7, 6, 10, 12, 10);

        VOXEL_SHAPE_AGGREGATE_REVERSED = Shapes.or(VOXEL_SHAPE_LAYER_1_REVERSED, VOXEL_SHAPE_LAYER_2_REVERSED);
    }
}