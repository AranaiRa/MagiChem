package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.events.CommonEventHelper;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.DistilleryRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import com.mna.spells.SpellsInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
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
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class DistilleryBlock extends BaseEntityBlock implements ISpellInteractibleBlock<DistilleryBlock> {

    public DistilleryBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR,

            VOXEL_SHAPE_PLUG_NORTH, VOXEL_SHAPE_BODY_NORTH, VOXEL_SHAPE_MOUNT_NORTH, VOXEL_SHAPE_FURNACE_NORTH,
            VOXEL_SHAPE_TANK_NORTH, VOXEL_SHAPE_PIPE_LEFT_NORTH, VOXEL_SHAPE_PIPE_RIGHT_NORTH,

            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_WEST;

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for(Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.DISTILLERY_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                pLevel.setBlock(targetPos, state
                                .setValue(ROUTER_TYPE_DISTILLERY, posAndType.getSecond().ordinal())
                                .setValue(FACING, facing)
                        , 3);
                ((DistilleryRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos, posAndType.getThird());
            }
        }
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for(Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        destroyRouters(pLevel, pPos, facing);

        super.destroy(pLevel, pPos, pState);
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction pFacing) {
        for(Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pFacing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
    }

    public static List<Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection>> getRouterOffsets(Direction pFacing) {
        List<Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);
        if(pFacing == Direction.NORTH) {
            offsets.add(new Triplet<>(origin.west(), DistilleryRouterType.PLUG_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.above(), DistilleryRouterType.ABOVE, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west().above(), DistilleryRouterType.ABOVE_LEFT, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Triplet<>(origin.east(), DistilleryRouterType.PLUG_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.above(), DistilleryRouterType.ABOVE, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east().above(), DistilleryRouterType.ABOVE_LEFT, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Triplet<>(origin.north(), DistilleryRouterType.PLUG_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.above(), DistilleryRouterType.ABOVE, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north().above(), DistilleryRouterType.ABOVE_LEFT, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Triplet<>(origin.south(), DistilleryRouterType.PLUG_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.above(), DistilleryRouterType.ABOVE, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south().above(), DistilleryRouterType.ABOVE_LEFT, DevicePlugDirection.NONE));
        }
        return offsets;
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
        pBuilder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof DistilleryBlockEntity) {
                ((DistilleryBlockEntity) blockEntity).packInventoryToBlockItem();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            default -> VOXEL_SHAPE_ERROR;
            case NORTH -> VOXEL_SHAPE_AGGREGATE_NORTH;
            case EAST -> VOXEL_SHAPE_AGGREGATE_EAST;
            case SOUTH -> VOXEL_SHAPE_AGGREGATE_SOUTH;
            case WEST -> VOXEL_SHAPE_AGGREGATE_WEST;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if(!pLevel.isClientSide()) {
            boolean holdingCleaningBrush = pPlayer.getInventory().getSelected().getItem() == ItemRegistry.CLEANING_BRUSH.get();

            BlockEntity be = pLevel.getBlockEntity(pPos);
            ItemStack itemInHand = pPlayer.getItemInHand(pHand);
            LazyOptional<IFluidHandlerItem> itemCapabilityQuery = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
            LazyOptional<IFluidHandler> capabilityQuery = null;

            if(be instanceof DistilleryBlockEntity main)
                capabilityQuery = main.getCapability(ForgeCapabilities.FLUID_HANDLER);
            else if(be instanceof DistilleryRouterBlockEntity router)
                capabilityQuery = router.getCapability(ForgeCapabilities.FLUID_HANDLER);

            if(!itemInHand.isEmpty() && itemCapabilityQuery.isPresent() && capabilityQuery != null) {
                final IFluidHandler fluidHandler = capabilityQuery.resolve().get();
                final IFluidHandlerItem iCap = itemCapabilityQuery.resolve().get();
                final FluidStack fluidInItem = iCap.getFluidInTank(0);

                if(itemInHand.getItem() == Items.BUCKET) {
                    FluidStack extractionQuery = fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);

                    if(extractionQuery.getAmount() == 1000) {
                        if(!pPlayer.isCreative()) {
                            ItemStack bucket = FluidUtil.getFilledBucket(extractionQuery);
                            if(itemInHand.getCount() == 1) {
                                pPlayer.setItemInHand(pHand, bucket);
                            }
                            else {
                                pPlayer.getItemInHand(pHand).shrink(1);
                                ItemEntity ie = new ItemEntity(pLevel, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), bucket);
                                pLevel.addFreshEntity(ie);
                            }
                        }
                        fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    }
                } else if(itemInHand.getItem() == Items.WATER_BUCKET) {
                    int insertionQuery = fluidHandler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.SIMULATE);

                    if(insertionQuery > 0) {
                        fluidHandler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                        if(!pPlayer.isCreative()) {
                            pPlayer.setItemInHand(pHand, new ItemStack(Items.BUCKET));
                        }
                    }
                } else if(itemInHand.getItem() == Items.LAVA_BUCKET) {
                    int insertionQuery = fluidHandler.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.SIMULATE);

                    if(insertionQuery > 0) {
                        fluidHandler.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
                        if(!pPlayer.isCreative()) {
                            pPlayer.setItemInHand(pHand, new ItemStack(Items.BUCKET));
                        }
                    }
                }  else if(itemInHand.getItem() == Items.MILK_BUCKET) {
                    int insertionQuery = fluidHandler.fill(new FluidStack(ForgeMod.MILK.get(), 1000), IFluidHandler.FluidAction.SIMULATE);

                    if(insertionQuery > 0) {
                        fluidHandler.fill(new FluidStack(ForgeMod.MILK.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
                        if(!pPlayer.isCreative()) {
                            pPlayer.setItemInHand(pHand, new ItemStack(Items.BUCKET));
                        }
                    }
                } else {
                    int insertionQuery = fluidHandler.fill(fluidInItem, IFluidHandler.FluidAction.SIMULATE);

                    if (insertionQuery > 0) {
                        fluidHandler.fill(fluidInItem, IFluidHandler.FluidAction.EXECUTE);
                        if(!pPlayer.isCreative()) {
                            iCap.drain(insertionQuery, IFluidHandler.FluidAction.EXECUTE);
                            if (iCap.getFluidInTank(0).isEmpty() && itemInHand.getItem() instanceof BucketItem bi) {
                                pPlayer.setItemInHand(pHand, new ItemStack(Items.BUCKET));
                            }
                        }
                    }
                }

                return InteractionResult.CONSUME;
            } else if(!holdingCleaningBrush) {
                BlockEntity entity = pLevel.getBlockEntity(pPos);
                if (entity instanceof DistilleryBlockEntity) {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, (DistilleryBlockEntity) entity, pPos);
                } else {
                    throw new IllegalStateException("DistilleryBlockEntity container provider is missing!");
                }
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DistilleryBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.DISTILLERY_BE.get(),
                DistilleryBlockEntity::tick);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    static {
        VOXEL_SHAPE_ERROR = Block.box(4, 4, 4, 12, 12, 12);

        VOXEL_SHAPE_BODY_NORTH = Block.box(0, 0, 2, 16, 8, 14);
        VOXEL_SHAPE_PLUG_NORTH = Block.box(12, 0, 0, 16, 16, 16);
        VOXEL_SHAPE_TANK_NORTH = Block.box(3, 13, 6, 7, 16, 10);
        VOXEL_SHAPE_MOUNT_NORTH = Block.box(2, 8, 5, 12, 12, 11);
        VOXEL_SHAPE_FURNACE_NORTH = Block.box(0, 0, 1, 6, 10, 15);
        VOXEL_SHAPE_PIPE_LEFT_NORTH = Block.box(0, 10, 5, 2, 16, 7);
        VOXEL_SHAPE_PIPE_RIGHT_NORTH = Block.box(7, 12, 7, 10, 16, 9);

        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_BODY_NORTH,
                VOXEL_SHAPE_PLUG_NORTH,
                VOXEL_SHAPE_TANK_NORTH,
                VOXEL_SHAPE_MOUNT_NORTH,
                VOXEL_SHAPE_FURNACE_NORTH,
                VOXEL_SHAPE_PIPE_LEFT_NORTH,
                VOXEL_SHAPE_PIPE_RIGHT_NORTH
                );

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_MOUNT_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FURNACE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_LEFT_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_RIGHT_NORTH, 1)
                );

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_MOUNT_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FURNACE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_LEFT_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_RIGHT_NORTH, 2)
                );

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_MOUNT_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FURNACE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_LEFT_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_RIGHT_NORTH, 3)
                );
    }

    @Override
    public boolean onHitBySpell(Level level, BlockPos blockPos, ISpellDefinition iSpellDefinition) {
        for(IModifiedSpellPart isp : iSpellDefinition.getComponents()){
            if(isp.getPart().equals(Components.FIRE_DAMAGE)) {
                float damage = isp.getValue(Attribute.DAMAGE);
                float duration = isp.getValue(Attribute.DURATION);
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof DistilleryBlockEntity dbe) {
                    dbe.setHeat(Math.round(damage * duration * 20));
                    return true;
                }
            } else if(isp.getPart().equals(Components.SPLASH)) {
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof DistilleryBlockEntity dbe) {
                    CommonEventHelper.generateWasteFromCleanedApparatus(null, level, dbe, null);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        if(pLevel.getBlockEntity(pPos) instanceof DistilleryBlockEntity dbe) {
            boolean hasInputItems = !dbe.getContentsOfInputSlots(DistilleryBlockEntity::getVar).isEmpty();
            boolean hasOutputItems = !dbe.getContentsOfOutputSlots(DistilleryBlockEntity::getVar).isEmpty();
            boolean hasFuel = dbe.hasFuelInSlot();
            boolean hasFluid = !dbe.getFluidInTank(0).isEmpty();

            int signal = 0;
            signal = signal | (hasFuel ? 1 : 0);
            signal = signal | (hasInputItems ? 1 << 1 : 0);
            signal = signal | (hasOutputItems ? 1 << 2 : 0);
            signal = signal | (hasFluid ? 1 << 3 : 0);

            return signal;
        }

        return 0;
    }
}
