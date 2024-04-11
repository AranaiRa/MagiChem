package com.aranaira.magichem.block;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.block.entity.routers.BaseActuatorRouterBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ActuatorFireBlock extends BaseEntityBlock {
    public ActuatorFireBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR                = Block.box(0,0,0,16,16,16),
            VOXEL_SHAPE_NORTH_WIDE           = Block.box(1,0,0,15,15,6),
            VOXEL_SHAPE_NORTH_LONG           = Block.box(4,0,0,12,15,12),
            VOXEL_SHAPE_NORTH_PIPELEFTOUTER  = Block.box(2,3,6,4,16,8),
            VOXEL_SHAPE_NORTH_PIPELEFTINNER  = Block.box(4,3,0,6,16,8),
            VOXEL_SHAPE_NORTH_PIPERIGHTOUTER = Block.box(12,3,6,14,16,8),
            VOXEL_SHAPE_NORTH_PIPERIGHTINNER = Block.box(10,3,0,12,16,8),
            VOXEL_SHAPE_NORTH_PIPECENTER     = Block.box(6,15,1,10,16,5),
            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if(pPlacer instanceof Player player) {
            ActuatorFireBlockEntity awbe = (ActuatorFireBlockEntity) pLevel.getBlockEntity(pPos);
            awbe.setOwner(player);
        }
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos().above();

        if(!pContext.getLevel().isEmptyBlock(pos)) {
            return null;
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
        BlockState state = BlockRegistry.ACTUATOR_FIRE_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        BlockPos targetPos = pPos.offset(0,1,0);
        pLevel.setBlock(targetPos, state, 3);
        ((BaseActuatorRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos, facing);

        ActuatorFireBlockEntity afbe = (ActuatorFireBlockEntity) pLevel.getBlockEntity(pPos);
        ICanTakePlugins ictp = afbe.getTargetMachine();
        if(ictp != null)
            ictp.linkPluginsDeferred();
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        pLevel.destroyBlock(pPos.offset(0, 1, 0), true);

        super.destroy(pLevel, pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            default -> VOXEL_SHAPE_ERROR;
            case NORTH -> VOXEL_SHAPE_AGGREGATE_NORTH;
            case SOUTH -> VOXEL_SHAPE_AGGREGATE_SOUTH;
            case WEST -> VOXEL_SHAPE_AGGREGATE_WEST;
            case EAST -> VOXEL_SHAPE_AGGREGATE_EAST;
        };
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return super.mirror(pState, pMirror);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return super.rotate(pState, pRotation);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        //super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    /* BLOCK ENTITY STUFF BELOW THIS POINT*/

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        ActuatorFireBlockEntity afbe = (ActuatorFireBlockEntity) level.getBlockEntity(pos);
        ICanTakePlugins ictp = afbe.getTargetMachine();
        if(ictp != null)
            ictp.removePlugin(afbe);

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            ItemStack heldItem = player.getItemInHand(hand);
            LazyOptional<IFluidHandlerItem> fluidCap = heldItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

            if (fluidCap.isPresent()) {
                BlockEntity be = level.getBlockEntity(pos);
                if(be instanceof ActuatorFireBlockEntity afbe) {
                    fluidCap.ifPresent(cap -> {
                        FluidStack fluid = cap.getFluidInTank(0);

                        //If container is empty or has steam
                        if(fluid.isEmpty() || fluid.getFluid() == FluidRegistry.SMOKE.get()) {
                            int capacity = cap.fill(new FluidStack(FluidRegistry.SMOKE.get(), Config.infernoEngineTankCapacity), IFluidHandler.FluidAction.SIMULATE);
                            FluidStack drainedFS = afbe.drain(new FluidStack(FluidRegistry.SMOKE.get(), Math.min(capacity, afbe.getFluidInTank(0).getAmount())), IFluidHandler.FluidAction.EXECUTE);
                            cap.fill(drainedFS, IFluidHandler.FluidAction.EXECUTE);
                        }
                    });
                }
            } else {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof ActuatorFireBlockEntity afbe) {
                    NetworkHooks.openScreen((ServerPlayer) player, (ActuatorFireBlockEntity) entity, pos);
                } else {
                    throw new IllegalStateException("ActuatorFireBlockEntity container provider is missing!");
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ActuatorFireBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide) {
            if(pBlockEntityType == BlockEntitiesRegistry.ACTUATOR_FIRE_BE.get()) {
                return ActuatorFireBlockEntity::tick;
            }
        }

        return null;
    }

    static {
        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_NORTH_WIDE, VOXEL_SHAPE_NORTH_LONG, VOXEL_SHAPE_NORTH_PIPELEFTINNER, VOXEL_SHAPE_NORTH_PIPELEFTOUTER,
                VOXEL_SHAPE_NORTH_PIPERIGHTINNER, VOXEL_SHAPE_NORTH_PIPERIGHTOUTER, VOXEL_SHAPE_NORTH_PIPECENTER);

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTINNER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTOUTER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTINNER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTOUTER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPECENTER, 1));

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTINNER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTOUTER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTINNER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTOUTER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPECENTER, 2));

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTINNER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTOUTER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTINNER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTOUTER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPECENTER, 3));
    }
}
