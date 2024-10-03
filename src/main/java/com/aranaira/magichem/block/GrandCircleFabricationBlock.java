package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.GrandCircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandCircleFabricationRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.gui.GrandCircleFabricationMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class GrandCircleFabricationBlock extends BaseEntityBlock {

    public GrandCircleFabricationBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    private static final VoxelShape
            VOXEL_SHAPE_BASE, VOXEL_SHAPE_HOLE, VOXEL_SHAPE_AGGREGATE;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
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
            if(blockEntity instanceof GrandCircleFabricationBlockEntity) {
                ((GrandCircleFabricationBlockEntity) blockEntity).dropInventoryToWorld();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof GrandCircleFabricationBlockEntity cfbe) {
                NetworkHooks.openScreen((ServerPlayer)player, new SimpleMenuProvider((id, playerInventory, user) -> {
                    return new GrandCircleFabricationMenu(id, playerInventory, cfbe);
                }, Component.empty()), cfbe);
            } else {
                throw new IllegalStateException("GrandCircleFabricationBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction facing) {
        for(Triplet<BlockPos, Integer, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for (Triplet<BlockPos, Integer, DevicePlugDirection> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.GRAND_CIRCLE_FABRICATION_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Triplet<BlockPos, Integer, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                int routerType = posAndType.getSecond();

                pLevel.setBlock(targetPos, state
                                .setValue(ROUTER_TYPE_GRAND_CIRCLE_FABRICATION, routerType)
                                .setValue(FACING, facing),
                        3);
                ((GrandCircleFabricationRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos, posAndType.getThird());
            }
        }

    }

    public static List<Triplet<BlockPos, Integer, DevicePlugDirection>> getRouterOffsets(Direction pFacing) {
        List<Triplet<BlockPos, Integer, DevicePlugDirection>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);

        if(pFacing == Direction.NORTH) {
            offsets.add(new Triplet<>(origin.north(), 0, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north().east(), 1, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east(), 2, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.south().east(), 3, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south(), 4, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south().west(), 5, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west(), 6, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.west().north(), 7, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Triplet<>(origin.east(), 0, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east().south(), 1, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south(), 2, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.south().west(), 3, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west(), 4, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west().north(), 5, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north(), 6, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.north().east(), 7, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Triplet<>(origin.south(), 0, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south().west(), 1, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west(), 2, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.west().north(), 3, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north(), 4, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north().east(), 5, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east(), 6, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.east().south(), 7, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Triplet<>(origin.west(), 0, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west().north(), 1, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north(), 2, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.north().east(), 3, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east(), 4, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east().south(), 5, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south(), 6, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.south().west(), 7, DevicePlugDirection.NONE));
        }

        return offsets;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrandCircleFabricationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.GRAND_CIRCLE_FABRICATION_BE.get(),
                GrandCircleFabricationBlockEntity::tick);
    }

    static {
        VOXEL_SHAPE_BASE = Block.box(0, 0, 0, 16, 15, 16);
        VOXEL_SHAPE_HOLE = Block.box(4, 15, 4, 12, 16, 12);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_BASE,
                VOXEL_SHAPE_HOLE
        );
    }
}
