package com.aranaira.magichem.item;

import com.aranaira.magichem.block.CirclePowerBlock;
import com.aranaira.magichem.block.entity.PowerSpikeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PowerSpikeItem extends BlockItem {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        if(stack.hasTag()) {
            if (stack.getTag().contains("magichem.powerspike.targetpos")) {
                BlockPos pos = BlockPos.of(stack.getTag().getLong("magichem.powerspike.targetpos"));
                components.add(Component.literal("Drawing power from (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"));
            }
        }

        super.appendHoverText(stack, level, components, tooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(!context.getLevel().isClientSide) {
            if(context.getPlayer() != null && context.getPlayer().isCrouching()) {
                BlockPos pos = context.getClickedPos();
                if(context.getLevel().getBlockState(pos).getBlock() instanceof CirclePowerBlock) {
                    context.getPlayer().sendSystemMessage(Component.literal("Used on magic circle @ "+pos));
                    CompoundTag tag = new CompoundTag();
                    tag.putLong("magichem.powerspike.targetpos",pos.asLong());

                    context.getItemInHand().setTag(tag);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);

        if(!context.getLevel().isClientSide) {
            BlockPos clickedPos = context.getClickedPos();
            ItemStack item = context.getItemInHand();
            BlockEntity entity = context.getLevel().getBlockEntity(clickedPos);

            if (entity != null) {
                if (entity instanceof PowerSpikeBlockEntity && item.hasTag()) {
                    if (item.getTag().contains("magichem.powerspike.targetpos")) {
                        BlockPos drawPos = BlockPos.of(item.getTag().getLong("magichem.powerspike.targetpos"));
                        PowerSpikeBlockEntity typedEntity = (PowerSpikeBlockEntity) entity;

                        typedEntity.setPowerDrawPos(drawPos);
                    }
                }
            }
        }

        return result;
    }

    public PowerSpikeItem(Block block, Properties properties) {
        super(block, properties);
    }
}
