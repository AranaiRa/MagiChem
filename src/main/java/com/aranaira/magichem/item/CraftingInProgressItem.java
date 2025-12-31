package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CraftingInProgressItem extends Item {
    public CraftingInProgressItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        CompoundTag nbt = pStack.getOrCreateTag();

        if(pStack.getItem() == ItemRegistry.SUBLIMATION_IN_PROGRESS.get()){
            pTooltipComponents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.sublimation_in_progress.line1").withStyle(ChatFormatting.DARK_GRAY))
            );
            if (nbt.contains("alchemyObject")) {
                ResourceLocation rl = new ResourceLocation(nbt.getString("alchemyObject"));
                Item itemQuery = ForgeRegistries.ITEMS.getValue(rl);
                String key = (itemQuery instanceof BlockItem ? "block." : "item.") + rl.getNamespace() + "." + rl.getPath();
                pTooltipComponents.add(Component.empty());
                pTooltipComponents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.sublimation_in_progress.line2").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.translatable(key).withStyle(ChatFormatting.DARK_AQUA))
                );
            }
        }
        else if(pStack.getItem() == ItemRegistry.EXALTATION_IN_PROGRESS.get()){
            pTooltipComponents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.exaltation_in_progress.line1").withStyle(ChatFormatting.DARK_GRAY))
            );
            if (nbt.contains("result")) {
                ResourceLocation rl = new ResourceLocation(nbt.getString("result"));
                Item itemQuery = ForgeRegistries.ITEMS.getValue(rl);
                String key = (itemQuery instanceof BlockItem ? "block." : "item.") + rl.getNamespace() + "." + rl.getPath();
                pTooltipComponents.add(Component.empty());
                pTooltipComponents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.exaltation_in_progress.line2").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.translatable(key).withStyle(ChatFormatting.DARK_AQUA))
                );
            }
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
