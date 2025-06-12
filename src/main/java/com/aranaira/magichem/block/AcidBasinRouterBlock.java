package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AcidBasinBlockEntity;
import com.aranaira.magichem.block.entity.routers.AcidBasinRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.AcidBasinRouterType;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.block.entity.AcidBasinBlockEntity.SLOT_INPUT;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_ACID_BASIN;

public class AcidBasinRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public static final int
            ROUTER_TYPE_MAIN_TANK = 1, ROUTER_TYPE_MAIN_TANK_ABOVE = 2, ROUTER_TYPE_OUTPUT_TANK = 3, ROUTER_TYPE_OUTPUT_TANK_ABOVE = 4;

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
            if(type == AcidBasinRouterType.MAIN_TANK || type == AcidBasinRouterType.MAIN_TANK_ABOVE) {
                ItemStack itemInHand = pPlayer.getItemInHand(pHand);
                final LazyOptional<IFluidHandler> capabilityQuery = router.getCapability(ForgeCapabilities.FLUID_HANDLER);
                final LazyOptional<IFluidHandlerItem> itemCapabilityQuery = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

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

                    if(itemInHand.getItem() == Items.WATER_BUCKET || itemInHand.getItem() == Items.LAVA_BUCKET) {

                    } else {
                        int insertionQuery = fluidHandler.fill(fluidInItem, IFluidHandler.FluidAction.SIMULATE);

                        if (insertionQuery > 0) {
                            fluidHandler.fill(fluidInItem, IFluidHandler.FluidAction.EXECUTE);
                            iCap.drain(insertionQuery, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }

//                    if(player.getItemInHand(hand).getItem() == Items.WATER_BUCKET && !player.isCreative())
//                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                //Try to insert the item
                else {
                    final LazyOptional<IItemHandler> itemHandlerQuery = router.getCapability(ForgeCapabilities.ITEM_HANDLER);
                    if(itemHandlerQuery.isPresent()) {
                        final IItemHandler itemHandler = itemHandlerQuery.resolve().get();
                        ItemStack insertionQuery = itemHandler.insertItem(SLOT_INPUT, pPlayer.getItemInHand(pHand), true);

                        if(insertionQuery.isEmpty()) {
                            itemHandler.insertItem(SLOT_INPUT, pPlayer.getItemInHand(pHand), false);
                            pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
                        } else {
                            int a = 0;
                        }
                        return InteractionResult.CONSUME;
                    }
                }
            } else if(type != AcidBasinRouterType.NONE) {

            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
