package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.ConjurerBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.ConjurerRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class ConjurerRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public ConjurerRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    public static VoxelShape
            VOXEL_SHAPE_TOP_BASE, VOXEL_SHAPE_TOP_PILLAR_1, VOXEL_SHAPE_TOP_PILLAR_2, VOXEL_SHAPE_TOP_PILLAR_3, VOXEL_SHAPE_TOP_PILLAR_4,
            VOXEL_SHAPE_TOP_BODY, VOXEL_SHAPE_TOP_SETTING, VOXEL_SHAPE_TOP_GEM, VOXEL_SHAPE_TOP_AGGREGATE,

            VOXEL_SHAPE_MID_PILLAR_A1, VOXEL_SHAPE_MID_PILLAR_A2, VOXEL_SHAPE_MID_PILLAR_A3, VOXEL_SHAPE_MID_PILLAR_A4,
            VOXEL_SHAPE_MID_PILLAR_B1, VOXEL_SHAPE_MID_PILLAR_B2, VOXEL_SHAPE_MID_PILLAR_B3, VOXEL_SHAPE_MID_PILLAR_B4,
            VOXEL_SHAPE_MID_ITEM, VOXEL_SHAPE_MID_AGGREGATE;
    public static final int
            ROUTER_TYPE_NONE = 0, ROUTER_TYPE_MIDDLE = 1, ROUTER_TYPE_TOP = 2;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ConjurerRouterBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        int routerType = pState.getValue(ROUTER_TYPE_CONJURER);

        //Again, switch statements always default here and I have no idea why
        if(routerType == ROUTER_TYPE_MIDDLE) {
            return VOXEL_SHAPE_MID_AGGREGATE;
        } else if(routerType == ROUTER_TYPE_TOP) {
            return VOXEL_SHAPE_TOP_AGGREGATE;
        }

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof ConjurerRouterBlockEntity crbe) {
            ConjurerBlockEntity master = crbe.getMaster();
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ROUTER_TYPE_CONJURER);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.CONJURER.get());
    }

    static {
        VOXEL_SHAPE_TOP_BASE     = Block.box(1, 13, 1, 15, 15, 15);
        VOXEL_SHAPE_TOP_PILLAR_1 = Block.box(2, 0, 2, 4, 13, 4);
        VOXEL_SHAPE_TOP_PILLAR_2 = Block.box(12, 0, 2, 14, 13, 4);
        VOXEL_SHAPE_TOP_PILLAR_3 = Block.box(12, 0, 12, 14, 13, 14);
        VOXEL_SHAPE_TOP_PILLAR_4 = Block.box(2, 0, 12, 4, 13, 14);
        VOXEL_SHAPE_TOP_BODY     = Block.box(3, 4, 3, 13, 13, 13);
        VOXEL_SHAPE_TOP_SETTING  = Block.box(5, 3, 5, 11, 4, 11);
        VOXEL_SHAPE_TOP_GEM      = Block.box(6, 0, 6, 10, 3, 10);

        VOXEL_SHAPE_TOP_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_TOP_BASE,
                VOXEL_SHAPE_TOP_PILLAR_1,
                VOXEL_SHAPE_TOP_PILLAR_2,
                VOXEL_SHAPE_TOP_PILLAR_3,
                VOXEL_SHAPE_TOP_PILLAR_4,
                VOXEL_SHAPE_TOP_BODY,
                VOXEL_SHAPE_TOP_SETTING,
                VOXEL_SHAPE_TOP_GEM
        );

        VOXEL_SHAPE_MID_PILLAR_A1 = Block.box(2, 0, 2, 4, 6, 4);
        VOXEL_SHAPE_MID_PILLAR_A2 = Block.box(12, 0, 2, 14, 6, 4);
        VOXEL_SHAPE_MID_PILLAR_A3 = Block.box(12, 0, 12, 14, 6, 14);
        VOXEL_SHAPE_MID_PILLAR_A4 = Block.box(2, 0, 12, 4, 6, 14);
        VOXEL_SHAPE_MID_PILLAR_B1 = Block.box(2, 10, 2, 4, 16, 4);
        VOXEL_SHAPE_MID_PILLAR_B2 = Block.box(12, 10, 2, 14, 16, 4);
        VOXEL_SHAPE_MID_PILLAR_B3 = Block.box(12, 10, 12, 14, 16, 14);
        VOXEL_SHAPE_MID_PILLAR_B4 = Block.box(2, 10, 12, 4, 16, 14);
        VOXEL_SHAPE_MID_ITEM = Block.box(6.5, 6.5, 6.5, 9.5, 9.5, 9.5);

        VOXEL_SHAPE_MID_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_MID_PILLAR_A1,
                VOXEL_SHAPE_MID_PILLAR_A2,
                VOXEL_SHAPE_MID_PILLAR_A3,
                VOXEL_SHAPE_MID_PILLAR_A4,
                VOXEL_SHAPE_MID_PILLAR_B1,
                VOXEL_SHAPE_MID_PILLAR_B2,
                VOXEL_SHAPE_MID_PILLAR_B3,
                VOXEL_SHAPE_MID_PILLAR_B4,
                VOXEL_SHAPE_MID_ITEM
        );
    }
}