package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.blocks.BlockInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class StandingRetortBlockItem extends BlockItem {

    public StandingRetortBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.standingretort")
                .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        ItemStack stack = pContext.getItemInHand();

        if(stack.hasTag()) {
        }
        return super.place(pContext);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        BlockState stateQuery = pContext.getLevel().getBlockState(pContext.getClickedPos());
        BlockPos posQuery = null;
        Direction dir = null;

        if(stateQuery.getBlock() == BlockInit.ELDRIN_FUME.get()) {
            posQuery = pContext.getClickedPos().offset(0,2,0);
            dir = stateQuery.getValue(HORIZONTAL_FACING);
        } else if(stateQuery.getBlock() == BlockInit.EMPTY_FILLER_BLOCK.get()) {
            posQuery = pContext.getClickedPos().offset(0,1,0);
            dir = pContext.getLevel().getBlockState(pContext.getClickedPos().below()).getValue(HORIZONTAL_FACING);
        }

        if(posQuery != null) {
            BlockState spaceQuery = pContext.getLevel().getBlockState(posQuery);
            if(spaceQuery.isAir()) {
                if (!pContext.getLevel().isClientSide()) {
                    pContext.getLevel().setBlockAndUpdate(posQuery, BlockRegistry.STANDING_RETORT.get().defaultBlockState().setValue(HORIZONTAL_FACING, dir));
                }

                if (!pContext.getPlayer().isCreative())
                    pContext.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.CONSUME;
    }
}
