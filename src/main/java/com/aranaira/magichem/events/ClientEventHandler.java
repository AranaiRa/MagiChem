package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.renderer.CentrifugeBlockEntityRenderer;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientEventHandler {
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof MateriaItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.getEssentia().toArray(new EssentiaItem[0]));
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof MateriaItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.getAdmixtures().toArray(new AdmixtureItem[0]));
    }

    //TODO: This makes Minecraft very upset, figure it out later
    /*@SubscribeEvent
    public static void renderTooltips(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getItemStack();

        if(stack.getItem() == ItemRegistry.STEAM_BUCKET.get()) {
            event.getComponents().add(new ClientTextTooltip(
                    Component.translatable("tooltip.magichem.gasbucket")
                            .withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText()
            ));
        }
        else if(stack.getItem() == ItemRegistry.SMOKE_BUCKET.get()) {
            event.getComponents().add(new ClientTextTooltip(
                    Component.translatable("tooltip.magichem.gasbucket")
                            .withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText()
            ));
        }
    }*/
}
