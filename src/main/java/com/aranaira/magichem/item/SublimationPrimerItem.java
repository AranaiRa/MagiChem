package com.aranaira.magichem.item;

import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.item.renderer.MateriaVesselItemRenderer;
import com.aranaira.magichem.item.renderer.SublimationPrimerItemRenderer;
import com.aranaira.magichem.recipe.AlchemicalInfusionRitualRecipe;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.KeybindInit;
import com.mna.items.base.IRadialInventorySelect;
import com.mna.items.base.IRadialMenuItem;
import com.mna.items.renderers.books.AlterationBookRenderer;
import com.mna.tools.math.Vector3;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class SublimationPrimerItem extends Item implements IRadialInventorySelect {
    public SublimationPrimerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(() -> new SublimationPrimerItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()));

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer.get();
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        //Had to copy this in from the interface because I'm adding more text myself
        String txt = I18n.get(((KeyMapping) KeybindInit.RadialMenuOpen.get()).getKey().getDisplayName().getString(), new Object[0]);
        pTooltipComponents.add(Component.translatable("item.mna.item-with-gui.radial-open", new Object[]{txt}).withStyle(ChatFormatting.AQUA));

        //My tooltip
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.sublimationprimer")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public int capacity() {
        return AlchemicalInfusionRitualRecipe.getAllOutputs().size();
    }

    @Override
    public void setIndex(ItemStack itemStack, int i) {
        itemStack.getOrCreateTag().putInt("recipe", i);
    }

    @Override
    public int getIndex(ItemStack itemStack) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        if(nbt.contains("recipe"))
            return nbt.getInt("recipe");
        return -1;
    }

    @Override
    public IItemHandlerModifiable getInventory(ItemStack stackEquipped) {
        return IRadialInventorySelect.super.getInventory(stackEquipped);
    }

    @Override
    public IItemHandlerModifiable getInventory(ItemStack stackEquipped, @Nullable Player player) {
        return IRadialInventorySelect.super.getInventory(stackEquipped, player);
    }
}
