package com.aranaira.magichem.item.compat.occultism;

import com.aranaira.magichem.events.compat.OccultismEventHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OccultRitualTalismanItem extends Item {
    private static final ResourceLocation MINECRAFT_CANDLE_TAG = new ResourceLocation("minecraft", "candles");
    private static final ResourceLocation OCCULTISM_CANDLE_TAG = new ResourceLocation("occultism", "candles");

    public OccultRitualTalismanItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        OccultismEventHelper.handleRitualTalismanPlacement(pContext);

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.occultritualtalisman")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        CompoundTag nbt = pStack.getOrCreateTag();
        if(nbt.contains("pentacleID")) {
            pTooltipComponents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.occultritualtalisman.storedpentacle").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("multiblock.occultism."+nbt.getString("pentacleID")).withStyle(ChatFormatting.DARK_AQUA))
            );
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
