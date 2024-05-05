package com.aranaira.magichem.block;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.block.entity.routers.BaseActuatorRouterBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
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

public class ActuatorAirBlock extends BaseEntityBlock {
    public ActuatorAirBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR      = Block.box(0,0,0,16,16,16),
            VOXEL_SHAPE_PLUG       = Block.box(1,0,0,15,15,6),
            VOXEL_SHAPE_BASE_WIDE  = Block.box(2.5,0,6,13.5,6,14),
            VOXEL_SHAPE_BASE_LONG  = Block.box(5.5,0,6,10.5,14,15),
            VOXEL_SHAPE_FANS       = Block.box(1.5,6,7,14.5,13,14),
            VOXEL_SHAPE_SIPHON     = Block.box(6,15,1,10,16,5),
            VOXEL_SHAPE_TANK_MOUNT = Block.box(6,14,8.5,10,16,12.5),
            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if(pPlacer instanceof Player player) {
            ActuatorAirBlockEntity aabe = (ActuatorAirBlockEntity) pLevel.getBlockEntity(pPos);
            aabe.setOwner(player);
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
            if(be instanceof ActuatorAirBlockEntity aabe) {
                ActuatorAirBlockEntity.setPaused(aabe, pLevel.hasNeighborSignal(pPos));
            }
        }
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
        BlockState state = BlockRegistry.ACTUATOR_AIR_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        BlockPos targetPos = pPos.offset(0,1,0);
        pLevel.setBlock(targetPos, state, 3);
        ((BaseActuatorRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos, facing);

        ActuatorAirBlockEntity aibe = (ActuatorAirBlockEntity) pLevel.getBlockEntity(pPos);
        ICanTakePlugins ictp = aibe.getTargetMachine();
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
        ActuatorAirBlockEntity aabe = (ActuatorAirBlockEntity) level.getBlockEntity(pos);
        ICanTakePlugins ictp = aabe.getTargetMachine();
        if(ictp != null)
            ictp.removePlugin(aabe);

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            ItemStack heldItem = player.getItemInHand(hand);
            LazyOptional<IFluidHandlerItem> fluidCap = heldItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

            if (fluidCap.isPresent()) {
                BlockEntity be = level.getBlockEntity(pos);
                if(be instanceof ActuatorAirBlockEntity aabe) {
                    fluidCap.ifPresent(cap -> {
                        FluidStack fluid = cap.getFluidInTank(0);

                        if(fluid.getFluid() == FluidRegistry.SMOKE.get()) {
                            int capacity = aabe.fill(new FluidStack(FluidRegistry.SMOKE.get(), Config.galePressurizerTankCapacity), IFluidHandler.FluidAction.SIMULATE);
                            FluidStack drainedFS;
                            if(player.isCreative())
                                drainedFS = new FluidStack(FluidRegistry.SMOKE.get(), fluid.getAmount());
                            else
                                drainedFS = cap.drain(new FluidStack(FluidRegistry.SMOKE.get(), capacity), IFluidHandler.FluidAction.EXECUTE);
                            aabe.fill(drainedFS, IFluidHandler.FluidAction.EXECUTE);

                            if(player.getItemInHand(hand).getItem() == ItemRegistry.SMOKE_BUCKET.get())
                                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        }
                        else if(fluid.getFluid() == FluidRegistry.STEAM.get()) {
                            int capacity = aabe.fill(new FluidStack(FluidRegistry.STEAM.get(), Config.galePressurizerTankCapacity), IFluidHandler.FluidAction.SIMULATE);
                            FluidStack drainedFS;
                            if(player.isCreative())
                                drainedFS = new FluidStack(FluidRegistry.STEAM.get(), fluid.getAmount());
                            else
                                drainedFS = cap.drain(new FluidStack(FluidRegistry.STEAM.get(), capacity), IFluidHandler.FluidAction.EXECUTE);
                            aabe.fill(drainedFS, IFluidHandler.FluidAction.EXECUTE);

                            if(player.getItemInHand(hand).getItem() == ItemRegistry.STEAM_BUCKET.get())
                                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        }
                    });
                }
            } else {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof ActuatorAirBlockEntity aabe) {
                    NetworkHooks.openScreen((ServerPlayer) player, (ActuatorAirBlockEntity) entity, pos);
                } else {
                    throw new IllegalStateException("ActuatorAirBlockEntity container provider is missing!");
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ActuatorAirBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pBlockEntityType == BlockEntitiesRegistry.ACTUATOR_AIR_BE.get()) {
            return ActuatorAirBlockEntity::tick;
        }

        return null;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    static {
        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_PLUG,
                VOXEL_SHAPE_BASE_WIDE,
                VOXEL_SHAPE_BASE_LONG,
                VOXEL_SHAPE_FANS,
                VOXEL_SHAPE_SIPHON,
                VOXEL_SHAPE_TANK_MOUNT);

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_WIDE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_LONG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FANS, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_SIPHON, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MOUNT, 1));

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_WIDE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_LONG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FANS, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_SIPHON, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MOUNT, 2));

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_WIDE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_LONG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FANS, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_SIPHON, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_MOUNT, 3));
    }
}
