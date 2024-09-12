package com.aranaira.magichem.block;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.block.entity.routers.BaseActuatorRouterBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
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

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ACTUATOR_ELEMENT;

public class ActuatorWaterBlock extends BaseEntityBlock {
    public ActuatorWaterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR           = Block.box(0,0,0,16,16,16),
            VOXEL_SHAPE_NORTH_WIDE      = Block.box(1,0,0,15,15,4),
            VOXEL_SHAPE_NORTH_LONG      = Block.box(4,0,4,12,12,8),
            VOXEL_SHAPE_NORTH_TUBEBASE  = Block.box(4,0,8,12,2,15),
            VOXEL_SHAPE_NORTH_TUBEBODY  = Block.box(5,0,8,11,16,14),
            VOXEL_SHAPE_NORTH_PIPES     = Block.box(5.5,9,5,10.5,16,8),
            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if(pPlacer instanceof Player player) {
            ActuatorWaterBlockEntity awbe = (ActuatorWaterBlockEntity) pLevel.getBlockEntity(pPos);
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
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be != null) {
            if(be instanceof ActuatorWaterBlockEntity awbe) {
                awbe.setPaused(pLevel.hasNeighborSignal(pPos));
            }
        }
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockState state = BlockRegistry.ACTUATOR_WATER_ROUTER.get().defaultBlockState();
        state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        state = state.setValue(ACTUATOR_ELEMENT, BaseActuatorRouterBlock.ELEMENT_WATER);

        BlockPos targetPos = pPos.offset(0,1,0);
        pLevel.setBlock(targetPos, state, 3);
        ((BaseActuatorRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos, facing);

        ActuatorWaterBlockEntity awbe = (ActuatorWaterBlockEntity) pLevel.getBlockEntity(pPos);
        ICanTakePlugins ictp = awbe.getTargetMachine();
        if(ictp != null)
            ictp.linkPluginsDeferred();
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        pLevel.destroyBlock(pPos.above(), true);

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
        ActuatorWaterBlockEntity awbe = (ActuatorWaterBlockEntity) level.getBlockEntity(pos);
        ICanTakePlugins ictp = awbe.getTargetMachine();
        if(ictp != null)
            ictp.removePlugin(awbe);

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            ItemStack heldItem = player.getItemInHand(hand);
            LazyOptional<IFluidHandlerItem> fluidCap = heldItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

            if (fluidCap.isPresent()) {
                BlockEntity be = level.getBlockEntity(pos);
                if(be instanceof ActuatorWaterBlockEntity awbe) {
                    fluidCap.ifPresent(cap -> {
                        FluidStack fluidInItem = cap.getFluidInTank(0);

                        //If container has water
                        if(fluidInItem.getFluid() == Fluids.WATER || heldItem.getItem() == Items.WATER_BUCKET) {
                            int capacity = awbe.fill(new FluidStack(Fluids.WATER, Config.delugePurifierTankCapacity), IFluidHandler.FluidAction.SIMULATE);
                            FluidStack drainedFS;
                            if(player.isCreative())
                                drainedFS = new FluidStack(Fluids.WATER, fluidInItem.getAmount());
                            else
                                drainedFS = cap.drain(new FluidStack(Fluids.WATER, capacity), IFluidHandler.FluidAction.EXECUTE);
                            awbe.fill(drainedFS, IFluidHandler.FluidAction.EXECUTE);

                            if(player.getItemInHand(hand).getItem() == Items.WATER_BUCKET)
                                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        }
                        //If container is empty or has steam
                        else if(fluidInItem.isEmpty() || fluidInItem.getFluid() == FluidRegistry.STEAM.get()) {
                            if(player.getItemInHand(hand).getItem() == Items.BUCKET) {
                                if(awbe.getFluidInTank(0).getAmount() >= 1000) {
                                    awbe.drain(new FluidStack(FluidRegistry.STEAM.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
                                    player.getItemInHand(hand).shrink(1);
                                    ItemEntity ie = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), new ItemStack(ItemRegistry.STEAM_BUCKET.get()));
                                    player.level().addFreshEntity(ie);
                                }
                            } else {
                                int capacity = cap.fill(new FluidStack(FluidRegistry.STEAM.get(), Config.delugePurifierTankCapacity), IFluidHandler.FluidAction.SIMULATE);
                                FluidStack drainedFS = awbe.drain(new FluidStack(FluidRegistry.STEAM.get(), Math.min(capacity, awbe.getFluidInTank(ActuatorWaterBlockEntity.TANK_ID_STEAM).getAmount())), IFluidHandler.FluidAction.EXECUTE);
                                cap.fill(drainedFS, IFluidHandler.FluidAction.EXECUTE);
                            }
                        }
                    });
                }
            } else {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof ActuatorWaterBlockEntity awbe) {
                    NetworkHooks.openScreen((ServerPlayer) player, (ActuatorWaterBlockEntity) entity, pos);
                } else {
                    throw new IllegalStateException("ActuatorWaterBlockEntity container provider is missing!");
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ActuatorWaterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pBlockEntityType == BlockEntitiesRegistry.ACTUATOR_WATER_BE.get()) {
            return ActuatorWaterBlockEntity::tick;
        }
        return null;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    static {
        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_NORTH_WIDE, VOXEL_SHAPE_NORTH_LONG,
                VOXEL_SHAPE_NORTH_TUBEBASE, VOXEL_SHAPE_NORTH_TUBEBODY,
                VOXEL_SHAPE_NORTH_PIPES);

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBODY, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPES, 1));

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBODY, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPES, 2));

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBODY, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPES, 3));
    }
}
