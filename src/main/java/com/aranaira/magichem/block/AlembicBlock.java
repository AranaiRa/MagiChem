package com.aranaira.magichem.block;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.events.CommonEventHelper;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.HAS_PASSIVE_HEAT;

public class AlembicBlock extends BaseEntityBlock implements ISpellInteractibleBlock<AlembicBlock> {
    public AlembicBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_PASSIVE_HEAT, false)
        );
    }

    private static final VoxelShape VOXEL_SHAPE = Block.box(4,0,4,12,10,12);
    public static final TagKey<Block> PASSIVE_HEAT_TAG = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "alembic_passive_heat_source"));

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return VOXEL_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState state = this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
        BlockState below = pContext.getLevel().getBlockState(pContext.getClickedPos().below());

        if(below.getBlockHolder().is(PASSIVE_HEAT_TAG))
            state = state.setValue(HAS_PASSIVE_HEAT, true);

        return state;
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
        pBuilder.add(FACING, HAS_PASSIVE_HEAT);
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
            if(blockEntity instanceof AlembicBlockEntity) {
                ((AlembicBlockEntity) blockEntity).packDataToBlockItem();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void onNeighborChange(BlockState pState, LevelReader pLevelReader, BlockPos pPos, BlockPos pNeighborPos) {
        boolean hasPassiveHeat = false;

        Stream<TagKey<Block>> tags = pLevelReader.getBlockState(pPos.below()).getBlockHolder().tags();

        if (pLevelReader.getBlockState(pPos.below()).getBlockHolder().is(new ResourceLocation(MagiChemMod.MODID, "alembic_passive_heat_source"))) {
            hasPassiveHeat = true;
        }

        pState = pState.setValue(HAS_PASSIVE_HEAT, hasPassiveHeat);
        super.onNeighborChange(pState, pLevelReader, pPos, pNeighborPos);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
        boolean hasPassiveHeat = false;

        for(TagKey<Block> key : pLevel.getBlockState(pPos.below()).getBlockHolder().getTagKeys().collect(Collectors.toList())) {
            ResourceLocation location = key.location();

            int a = 0;
        }

        if (pLevel.getBlockState(pPos.below()).getBlockHolder().is(PASSIVE_HEAT_TAG)) {
            hasPassiveHeat = true;
        }

        pState = pState.setValue(HAS_PASSIVE_HEAT, hasPassiveHeat);
        pLevel.setBlock(pPos, pState, 3);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if(!pLevel.isClientSide()) {
            boolean holdingCleaningBrush = pPlayer.getInventory().getSelected().getItem() == ItemRegistry.CLEANING_BRUSH.get();

            BlockEntity be = pLevel.getBlockEntity(pPos);
            ItemStack itemInHand = pPlayer.getItemInHand(pHand);
            LazyOptional<IFluidHandlerItem> itemCapabilityQuery = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
            LazyOptional<IFluidHandler> capabilityQuery = null;

            if(be instanceof AlembicBlockEntity main)
                capabilityQuery = main.getCapability(ForgeCapabilities.FLUID_HANDLER);

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
                } else if(itemInHand.getItem() == Items.MILK_BUCKET) {
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
                if (entity instanceof AlembicBlockEntity) {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, (AlembicBlockEntity) entity, pPos);
                } else {
                    throw new IllegalStateException("AlembicBlockEntity container provider is missing!");
                }
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlembicBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.ALEMBIC_BE.get(),
                AlembicBlockEntity::tick);
    }

    @Override
    public boolean onHitBySpell(Level level, BlockPos blockPos, ISpellDefinition iSpellDefinition) {
        for(IModifiedSpellPart isp : iSpellDefinition.getComponents()){
            if(isp.getPart().equals(Components.FIRE_DAMAGE)) {
                float damage = isp.getValue(Attribute.DAMAGE);
                float duration = isp.getValue(Attribute.DURATION);
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof AlembicBlockEntity abe) {
                    abe.setHeat(Math.round(damage * duration * 20));
                    return true;
                }
            } else if(isp.getPart().equals(Components.SPLASH)) {
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof AlembicBlockEntity abe) {
                    CommonEventHelper.generateWasteFromCleanedApparatus(null, level, abe, null);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }
}
