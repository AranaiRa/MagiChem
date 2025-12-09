package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.PrimeAggregatorBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class PrimeAggregatorBlock extends BaseEntityBlock {
    public PrimeAggregatorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
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
//                else if (be instanceof PrimeAggregatorRouterBlockEntity router)
//                    capabilityQuery = router.getCapability(ForgeCapabilities.FLUID_HANDLER);

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
