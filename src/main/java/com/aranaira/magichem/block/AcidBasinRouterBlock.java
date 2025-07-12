package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AcidBasinBlockEntity;
import com.aranaira.magichem.block.entity.routers.AcidBasinRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.AcidBasinRouterType;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.block.entity.AcidBasinBlockEntity.*;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_ACID_BASIN;

public class AcidBasinRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public static final int
            ROUTER_TYPE_MAIN_TANK = 1, ROUTER_TYPE_MAIN_TANK_ABOVE = 2, ROUTER_TYPE_OUTPUT_TANK = 3, ROUTER_TYPE_OUTPUT_TANK_ABOVE = 4;
    public static final VoxelShape
            VOXEL_SHAPE_MAIN_BASE_NORTH, VOXEL_SHAPE_MAIN_BODY_NORTH, VOXEL_SHAPE_MAIN_TANK_NORTH, VOXEL_SHAPE_MAIN_CONNECTOR_NORTH,
            VOXEL_SHAPE_OUTPUT_BASE_NORTH, VOXEL_SHAPE_OUTPUT_BODY_NORTH, VOXEL_SHAPE_OUTPUT_TANK_NORTH, VOXEL_SHAPE_OUTPUT_RIM_NORTH,
            VOXEL_SHAPE_MAIN_ABOVE_TANK, VOXEL_SHAPE_MAIN_ABOVE_RIM,
            VOXEL_SHAPE_OUTPUT_ABOVE,

            VOXEL_SHAPE_AGGREGATE_MAIN_NORTH, VOXEL_SHAPE_AGGREGATE_MAIN_EAST, VOXEL_SHAPE_AGGREGATE_MAIN_SOUTH, VOXEL_SHAPE_AGGREGATE_MAIN_WEST,
            VOXEL_SHAPE_AGGREGATE_OUTPUT_NORTH, VOXEL_SHAPE_AGGREGATE_OUTPUT_EAST, VOXEL_SHAPE_AGGREGATE_OUTPUT_SOUTH, VOXEL_SHAPE_AGGREGATE_OUTPUT_WEST,
            VOXEL_SHAPE_AGGREGATE_MAIN_ABOVE;

    public AcidBasinRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, ROUTER_TYPE_ACID_BASIN);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AcidBasinRouterBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(pLevel.isClientSide()) return InteractionResult.CONSUME;

        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof AcidBasinRouterBlockEntity router && pHand == InteractionHand.MAIN_HAND) {
            final AcidBasinRouterType type = router.getRouterType();
            ItemStack itemInHand = pPlayer.getItemInHand(pHand);
            final LazyOptional<IFluidHandler> capabilityQuery = router.getCapability(ForgeCapabilities.FLUID_HANDLER);
            final LazyOptional<IFluidHandlerItem> itemCapabilityQuery = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

            if(type == AcidBasinRouterType.MAIN_TANK || type == AcidBasinRouterType.MAIN_TANK_ABOVE) {
                //Try to extract an item
                if(itemInHand.isEmpty()) {
                    final LazyOptional<IItemHandler> itemHandlerQuery = router.getCapability(ForgeCapabilities.ITEM_HANDLER);
                    if(itemHandlerQuery.isPresent()) {
                        final IItemHandler itemHandler = itemHandlerQuery.resolve().get();
                        if(!itemHandler.getStackInSlot(SLOT_INPUT).isEmpty()) {
                            ItemStack extraction = itemHandler.extractItem(SLOT_INPUT, Integer.MAX_VALUE, false);
                            pPlayer.setItemInHand(InteractionHand.MAIN_HAND, extraction);
                        }
                    }
                }
                //Try to insert fluid into the main tank
                else if(itemCapabilityQuery.isPresent() && capabilityQuery.isPresent()) {
                    final IFluidHandler fluidHandler = capabilityQuery.resolve().get();
                    final IFluidHandlerItem iCap = itemCapabilityQuery.resolve().get();
                    final FluidStack fluidInItem = iCap.getFluidInTank(0);

                    if(itemInHand.getItem() == Items.BUCKET && fluidHandler instanceof AcidBasinBlockEntity basin) {
                        FluidStack extractionQuery = basin.drainFromTank(TANK_INPUT, 1000, IFluidHandler.FluidAction.SIMULATE);

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
                            basin.drainFromTank(TANK_INPUT, 1000, IFluidHandler.FluidAction.EXECUTE);
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
                        //Try to extract fluid from the main tank
                        else if (fluidHandler instanceof AcidBasinBlockEntity basin) {
                            FluidStack extractionQuery = basin.drainFromTank(TANK_INPUT, iCap.getTankCapacity(0), FluidAction.SIMULATE);
                            insertionQuery = iCap.fill(extractionQuery, FluidAction.SIMULATE);

                            if(insertionQuery > 0) {
                                basin.drainFromTank(TANK_INPUT, iCap.getTankCapacity(0), FluidAction.EXECUTE);
                                iCap.fill(extractionQuery, FluidAction.EXECUTE);
                            }
                        }
                    }
                }
                //Try to insert the item
                else {
                    final LazyOptional<IItemHandler> itemHandlerQuery = router.getCapability(ForgeCapabilities.ITEM_HANDLER);
                    if(itemHandlerQuery.isPresent()) {
                        final IItemHandler itemHandler = itemHandlerQuery.resolve().get();
                        ItemStack inputSlotItem = itemHandler.getStackInSlot(SLOT_INPUT);

                        if(inputSlotItem.isEmpty()) {
                            itemHandler.insertItem(SLOT_INPUT, itemInHand, false);
                            pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
                            pPlayer.swing(pHand);
                        } else if(inputSlotItem.getItem() == itemInHand.getItem()) {
                            int capacity = 64 - inputSlotItem.getCount();
                            int inserted = Math.min(capacity, itemInHand.getCount());
                            if(inserted > 0) {
                                itemHandler.insertItem(SLOT_INPUT, itemInHand.copy(), false);
                                itemInHand.shrink(inserted);
                                pPlayer.swing(pHand);
                            }
                        }

                        return InteractionResult.CONSUME;
                    }
                }
            }
            else if(type != AcidBasinRouterType.NONE) {
                if(itemCapabilityQuery.isPresent() && capabilityQuery.isPresent()) {
                    final IFluidHandler fluidHandler = capabilityQuery.resolve().get();
                    final IFluidHandlerItem iCap = itemCapabilityQuery.resolve().get();
                    if (itemInHand.getItem() == Items.BUCKET && fluidHandler instanceof AcidBasinBlockEntity basin) {
                        FluidStack extractionQuery = basin.drainFromTank(TANK_OUTPUT, 1000, IFluidHandler.FluidAction.SIMULATE);

                        if (extractionQuery.getAmount() == 1000) {
                            if (!pPlayer.isCreative()) {
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
                            basin.drainFromTank(TANK_OUTPUT, 1000, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                    else if(fluidHandler instanceof AcidBasinBlockEntity basin) {
                        FluidStack extractionQuery = basin.drainFromTank(TANK_OUTPUT, iCap.getTankCapacity(0), FluidAction.SIMULATE);

                        if(extractionQuery.getAmount() > 0) {
                            int actualFill = iCap.fill(extractionQuery, FluidAction.EXECUTE);
                            basin.drainFromTank(TANK_OUTPUT, actualFill, FluidAction.EXECUTE);
                        }

                        return InteractionResult.CONSUME;
                    }
                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.ACID_BASIN.get());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        final Direction dir = pState.getValue(FACING);
        int routerType = pState.getValue(ROUTER_TYPE_ACID_BASIN);

        if(routerType == ROUTER_TYPE_MAIN_TANK) {
            if(dir == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_MAIN_NORTH;
            if(dir == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_MAIN_EAST;
            if(dir == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_MAIN_SOUTH;
            if(dir == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_MAIN_WEST;
        }
        else if(routerType == ROUTER_TYPE_OUTPUT_TANK) {
            if(dir == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_OUTPUT_NORTH;
            if(dir == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_OUTPUT_EAST;
            if(dir == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_OUTPUT_SOUTH;
            if(dir == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_OUTPUT_WEST;
        }
        else if(routerType == ROUTER_TYPE_MAIN_TANK_ABOVE) {
            return VOXEL_SHAPE_AGGREGATE_MAIN_ABOVE;
        }
        else if(routerType == ROUTER_TYPE_OUTPUT_TANK_ABOVE) {
            return VOXEL_SHAPE_OUTPUT_ABOVE;
        }

        return Block.box(0,0,0,1,1,1);
    }

    static {
        //MAIN TANK
        {
            VOXEL_SHAPE_MAIN_BASE_NORTH = Block.box(0, 0, 0, 16, 3, 16);
            VOXEL_SHAPE_MAIN_BODY_NORTH = Block.box(1, 3, 1, 16, 8, 16);
            VOXEL_SHAPE_MAIN_TANK_NORTH = Block.box(2, 8, 2, 14, 16, 14);
            VOXEL_SHAPE_MAIN_CONNECTOR_NORTH = Block.box(2, 8, 3, 16, 14, 13);

            VOXEL_SHAPE_AGGREGATE_MAIN_NORTH = Shapes.or(
                    VOXEL_SHAPE_MAIN_BASE_NORTH,
                    VOXEL_SHAPE_MAIN_BODY_NORTH,
                    VOXEL_SHAPE_MAIN_TANK_NORTH,
                    VOXEL_SHAPE_MAIN_CONNECTOR_NORTH
            );

            VOXEL_SHAPE_AGGREGATE_MAIN_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_BASE_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_BODY_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_TANK_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_CONNECTOR_NORTH, 1)
            );

            VOXEL_SHAPE_AGGREGATE_MAIN_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_BASE_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_BODY_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_TANK_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_CONNECTOR_NORTH, 2)
            );

            VOXEL_SHAPE_AGGREGATE_MAIN_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_BASE_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_BODY_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_TANK_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_MAIN_CONNECTOR_NORTH, 3)
            );
        }

        //OUTPUT TANK
        {
            VOXEL_SHAPE_OUTPUT_BASE_NORTH = Block.box(0, 0, 0, 16, 3, 16);
            VOXEL_SHAPE_OUTPUT_BODY_NORTH = Block.box(2, 3, 0, 14, 8, 16);
            VOXEL_SHAPE_OUTPUT_TANK_NORTH = Block.box(3, 8, 3, 13, 14, 13);
            VOXEL_SHAPE_OUTPUT_RIM_NORTH = Block.box(4, 14, 4, 12, 16, 12);

            VOXEL_SHAPE_AGGREGATE_OUTPUT_NORTH = Shapes.or(
                    VOXEL_SHAPE_OUTPUT_BASE_NORTH,
                    VOXEL_SHAPE_OUTPUT_BODY_NORTH,
                    VOXEL_SHAPE_OUTPUT_TANK_NORTH,
                    VOXEL_SHAPE_OUTPUT_RIM_NORTH
            );

            VOXEL_SHAPE_AGGREGATE_OUTPUT_EAST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_BASE_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_BODY_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_TANK_NORTH, 1),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_RIM_NORTH, 1)
            );

            VOXEL_SHAPE_AGGREGATE_OUTPUT_SOUTH = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_BASE_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_BODY_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_TANK_NORTH, 2),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_RIM_NORTH, 2)
            );

            VOXEL_SHAPE_AGGREGATE_OUTPUT_WEST = Shapes.or(
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_BASE_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_BODY_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_TANK_NORTH, 3),
                    MathHelper.rotateVoxelShape(VOXEL_SHAPE_OUTPUT_RIM_NORTH, 3)
            );
        }

        //MAIN TANK ABOVE
        {
            VOXEL_SHAPE_MAIN_ABOVE_TANK = Block.box(2, 0, 2, 14, 2, 14);
            VOXEL_SHAPE_MAIN_ABOVE_RIM = Block.box(3, 2, 3, 13, 5, 13);

            VOXEL_SHAPE_AGGREGATE_MAIN_ABOVE = Shapes.or(
                    VOXEL_SHAPE_MAIN_ABOVE_TANK,
                    VOXEL_SHAPE_MAIN_ABOVE_RIM
            );
        }

        //MAIN TANK ABOVE
        {
            VOXEL_SHAPE_OUTPUT_ABOVE = Block.box(4, 0, 4, 12, 1, 12);
        }
    }
}
