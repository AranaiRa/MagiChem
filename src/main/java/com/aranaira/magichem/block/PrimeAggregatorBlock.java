package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.PrimeAggregatorBlockEntity;
import com.aranaira.magichem.block.entity.routers.PrimeAggregatorRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.PrimeAggregatorRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_PRIME_AGGREGATOR;
import static com.aranaira.magichem.foundation.enums.PrimeAggregatorRouterType.*;

public class PrimeAggregatorBlock extends BaseEntityBlock {
    public PrimeAggregatorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for(Triplet<BlockPos, PrimeAggregatorRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for(Triplet<BlockPos, PrimeAggregatorRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.PRIME_AGGREGATOR_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Triplet<BlockPos, PrimeAggregatorRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                int routerType = PrimeAggregatorRouterBlock.mapRouterTypeToInt(posAndType.getSecond());

                pLevel.setBlock(
                        targetPos,
                        state
                                .setValue(FACING, facing)
                                .setValue(ROUTER_TYPE_PRIME_AGGREGATOR, routerType),
                        3);
            }
        }
    }

    public static List<Triplet<BlockPos, PrimeAggregatorRouterType, DevicePlugDirection>> getRouterOffsets(Direction pFacing) {
        List<Triplet<BlockPos, PrimeAggregatorRouterType, DevicePlugDirection>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);
        if(pFacing == Direction.NORTH) {
            offsets.add(new Triplet<>(origin.south(), FRONT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north(), REAR, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south().west(), FRONT_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.south().east(), FRONT_RIGHT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.west(), PLUG_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.east(), PLUG_RIGHT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.north().west(), REAR_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.north().east(), REAR_RIGHT, DevicePlugDirection.EAST));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Triplet<>(origin.north(), FRONT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south(), REAR, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north().east(), PLUG_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.north().west(), PLUG_RIGHT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.east(), PLUG_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.west(), PLUG_RIGHT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.south().east(), REAR_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.south().west(), REAR_RIGHT, DevicePlugDirection.WEST));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Triplet<>(origin.west(), FRONT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east(), REAR, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west().north(), FRONT_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.west().south(), FRONT_RIGHT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.north(), PLUG_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.south(), PLUG_RIGHT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.east().north(), REAR_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.east().south(), REAR_RIGHT, DevicePlugDirection.SOUTH));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Triplet<>(origin.east(), FRONT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west(), REAR, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east().south(), FRONT_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.east().north(), FRONT_RIGHT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.south(), PLUG_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.north(), PLUG_RIGHT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.west().south(), REAR_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.west().north(), REAR_RIGHT, DevicePlugDirection.NORTH));
        }
        return offsets;
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pMasterPos, Direction pFacing) {
        for(Triplet<BlockPos, PrimeAggregatorRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pFacing)) {
            pLevel.destroyBlock(pMasterPos.offset(posAndType.getFirst()), true);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof PrimeAggregatorBlockEntity aggregator) {
                aggregator.packDataToBlockItem();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            boolean holdingExperienceExchanger = player.getInventory().getSelected().getItem() == BlockRegistry.EXPERIENCE_EXCHANGER.get().asItem();

            if (holdingExperienceExchanger) {
                return InteractionResult.PASS;
            } else {
                BlockEntity be = level.getBlockEntity(pos);
                ItemStack itemInHand = player.getItemInHand(hand);
                LazyOptional<IFluidHandlerItem> itemCapabilityQuery = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
                LazyOptional<IFluidHandler> capabilityQuery = null;

                if (be instanceof PrimeAggregatorBlockEntity main)
                    capabilityQuery = main.getCapability(ForgeCapabilities.FLUID_HANDLER);
                else if (be instanceof PrimeAggregatorRouterBlockEntity router)
                    capabilityQuery = router.getCapability(ForgeCapabilities.FLUID_HANDLER);

                if (!itemInHand.isEmpty() && itemCapabilityQuery.isPresent() && capabilityQuery != null) {
                    final IFluidHandler fluidHandler = capabilityQuery.resolve().get();
                    final IFluidHandlerItem iCap = itemCapabilityQuery.resolve().get();
                    final FluidStack fluidInItem = iCap.getFluidInTank(0);

                    if (itemInHand.getItem() == ItemRegistry.ACADEMIC_SLURRY_BUCKET.get()) {
                        int insertionQuery = fluidHandler.fill(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), 1000), IFluidHandler.FluidAction.SIMULATE);

                        if (insertionQuery == 1000) {
                            fluidHandler.fill(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
                            if (!player.isCreative()) {
                                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                            }
                        }
                    } else {
                        int insertionQuery = fluidHandler.fill(fluidInItem, IFluidHandler.FluidAction.SIMULATE);

                        if (insertionQuery > 0) {
                            fluidHandler.fill(fluidInItem, IFluidHandler.FluidAction.EXECUTE);
                            if (!player.isCreative()) {
                                iCap.drain(insertionQuery, IFluidHandler.FluidAction.EXECUTE);
                                if (iCap.getFluidInTank(0).isEmpty() && itemInHand.getItem() instanceof BucketItem bi) {
                                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                                }
                            }
                        }
                    }

                    return InteractionResult.CONSUME;
                } else {
                    BlockEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof PrimeAggregatorBlockEntity aggregator) {
                        int spaceInTank = aggregator.getTankCapacity(0) - aggregator.getFluidInTank(0).getAmount();
                        if (!itemInHand.isEmpty() && itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
                            if (spaceInTank >= 1000 && itemInHand.getItem() == ItemRegistry.ACADEMIC_SLURRY_BUCKET.get() && !player.isCreative()) {
                                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                            }
                            return InteractionResult.CONSUME;
                        } else {
                            NetworkHooks.openScreen((ServerPlayer) player, aggregator, pos);
                        }
                    } else {
                        throw new IllegalStateException("AlchemicalNexusBlockEntity container provider is missing!");
                    }
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PrimeAggregatorBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.PRIME_AGGREGATOR_BE.get(),
                PrimeAggregatorBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if(pPlacer instanceof Player player) {
            PrimeAggregatorBlockEntity aggregator = (PrimeAggregatorBlockEntity) pLevel.getBlockEntity(pPos);
            aggregator.setOwner(player);
        }
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }
}
