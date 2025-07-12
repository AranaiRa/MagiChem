package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AcidBasinBlockEntity;
import com.aranaira.magichem.block.entity.routers.AcidBasinRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.AcidBasinRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.block.entity.AcidBasinBlockEntity.SLOT_OUTPUT;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_ACID_BASIN;

public class AcidBasinBlock extends BaseEntityBlock {
    public static final VoxelShape
        VOXEL_SHAPE_BASE_NORTH, VOXEL_SHAPE_BODY_NORTH, VOXEL_SHAPE_TRAY_NORTH,
        VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_WEST;

    public AcidBasinBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for(Pair<BlockPos, AcidBasinRouterType> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.ACID_BASIN_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Pair<BlockPos, AcidBasinRouterType> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                pLevel.setBlock(targetPos, state
                                .setValue(ROUTER_TYPE_ACID_BASIN, posAndType.getSecond().ordinal())
                                .setValue(FACING, facing)
                        , 3);
                ((AcidBasinRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos);
            }
        }
    }

    public static List<Pair<BlockPos, AcidBasinRouterType>> getRouterOffsets(Direction pFacing) {
        List<Pair<BlockPos, AcidBasinRouterType>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);

        if(pFacing == Direction.NORTH) {
            offsets.add(new Pair<>(origin.west(), AcidBasinRouterType.MAIN_TANK));
            offsets.add(new Pair<>(origin.west().above(), AcidBasinRouterType.MAIN_TANK_ABOVE));
            offsets.add(new Pair<>(origin.west().south(), AcidBasinRouterType.OUTPUT_TANK));
            offsets.add(new Pair<>(origin.west().south().above(), AcidBasinRouterType.OUTPUT_TANK_ABOVE));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Pair<>(origin.north(), AcidBasinRouterType.MAIN_TANK));
            offsets.add(new Pair<>(origin.north().above(), AcidBasinRouterType.MAIN_TANK_ABOVE));
            offsets.add(new Pair<>(origin.north().west(), AcidBasinRouterType.OUTPUT_TANK));
            offsets.add(new Pair<>(origin.north().west().above(), AcidBasinRouterType.OUTPUT_TANK_ABOVE));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Pair<>(origin.east(), AcidBasinRouterType.MAIN_TANK));
            offsets.add(new Pair<>(origin.east().above(), AcidBasinRouterType.MAIN_TANK_ABOVE));
            offsets.add(new Pair<>(origin.east().north(), AcidBasinRouterType.OUTPUT_TANK));
            offsets.add(new Pair<>(origin.east().north().above(), AcidBasinRouterType.OUTPUT_TANK_ABOVE));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Pair<>(origin.south(), AcidBasinRouterType.MAIN_TANK));
            offsets.add(new Pair<>(origin.south().above(), AcidBasinRouterType.MAIN_TANK_ABOVE));
            offsets.add(new Pair<>(origin.south().east(), AcidBasinRouterType.OUTPUT_TANK));
            offsets.add(new Pair<>(origin.south().east().above(), AcidBasinRouterType.OUTPUT_TANK_ABOVE));
        }

        return offsets;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AcidBasinBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        if(pLevel.getBlockEntity(pPos) instanceof AcidBasinBlockEntity basin) {

            //Try to extract an item
            if (itemInHand.isEmpty()) {
                final LazyOptional<IItemHandler> itemHandlerQuery = basin.getCapability(ForgeCapabilities.ITEM_HANDLER);
                if (itemHandlerQuery.isPresent()) {
                    final IItemHandler itemHandler = itemHandlerQuery.resolve().get();
                    if (!itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
                        ItemStack extraction = itemHandler.extractItem(SLOT_OUTPUT, Integer.MAX_VALUE, false);
                        pPlayer.setItemInHand(InteractionHand.MAIN_HAND, extraction);
                    }
                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction pFacing) {
        for(Pair<BlockPos, AcidBasinRouterType> posAndType : getRouterOffsets(pFacing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlockEntitiesRegistry.ACID_BASIN_BE.get(),
                AcidBasinBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof AcidBasinBlockEntity abbe) {
                abbe.packInventoryToBlockItem();
                destroyRouters(level, pos, state.getValue(FACING));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        final Direction dir = pState.getValue(FACING);

        if(dir == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_NORTH;
        else if(dir == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_EAST;
        else if(dir == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_SOUTH;
        else if(dir == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_WEST;

        return Block.box(0,0,0,1,1,1);
    }

    static {
        VOXEL_SHAPE_BASE_NORTH = Block.box(0, 0, 0, 16, 3, 16);
        VOXEL_SHAPE_BODY_NORTH = Block.box(0, 3, 1, 15, 8, 15);
        VOXEL_SHAPE_TRAY_NORTH = Block.box(0, 8, 3, 13, 14, 13);

        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_BASE_NORTH,
                VOXEL_SHAPE_BODY_NORTH,
                VOXEL_SHAPE_TRAY_NORTH
        );

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TRAY_NORTH, 1)
        );

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TRAY_NORTH, 2)
        );

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TRAY_NORTH, 3)
        );
    }
}
