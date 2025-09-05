package com.aranaira.magichem.item;

import com.aranaira.magichem.gui.WisdomMenu;
import com.aranaira.magichem.item.renderer.MasterItemRenderer;
import com.aranaira.magichem.item.renderer.mna.SublimationPrimerItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class PhilosophersStoneItem extends Item {
    int wisdom = 0;

    public PhilosophersStoneItem(Properties pProperties, int pWisdom) {
        super(pProperties);
        this.wisdom = pWisdom;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return MasterItemRenderer.getOrCreateMasterRenderer().get();
            }
        });
    }

    public int getWisdom() {
        return wisdom;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem."+this.toString()).withStyle(ChatFormatting.DARK_GRAY));

        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.wisdom_stone_recipes").withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, @Nonnull InteractionHand pHand) {
        if (!pLevel.isClientSide && pHand == InteractionHand.MAIN_HAND) {
            NetworkHooks.openScreen((ServerPlayer)pPlayer, new SimpleMenuProvider(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pInternalPlayer) {
                    ItemStack itemInHand = pInternalPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                    int slot = pPlayerInventory.findSlotMatchingItem(itemInHand);

                    ContainerData data = new SimpleContainerData(1);
                    if(itemInHand.getItem() instanceof PhilosophersStoneItem psi) {
                        data.set(0, psi.getWisdom());
                    }
                    return new WisdomMenu(pContainerId, pPlayerInventory, data);
                }
            }, Component.empty()));
        }

        return InteractionResultHolder.success(pPlayer.getItemInHand(pHand));
    }
}
