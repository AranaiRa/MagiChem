package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class CommonEventHandler {
    public CommonEventHandler() {}

    @SubscribeEvent
    public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
        //Handle inserting or extracting from materia vessels
        ItemStack stack = event.getItemStack();
        BlockEntity target = event.getLevel().getBlockEntity(event.getPos());
        if(target instanceof MateriaVesselBlockEntity mvbe) {
            if(stack.getItem() == Items.GLASS_BOTTLE) {
                if(mvbe.getMateriaType() != null) {
                    ItemStack extracted = mvbe.extractMateria(stack.getCount());
                    stack.shrink(extracted.getCount());

                    ItemEntity ie = new ItemEntity(event.getLevel(),
                            event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                            extracted);
                    event.getLevel().addFreshEntity(ie);
                }
            } else if(stack.getItem() instanceof MateriaItem) {
                int inserted = mvbe.insertMateria(stack);
                stack.shrink(inserted);

                ItemEntity ie = new ItemEntity(event.getLevel(),
                        event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                        new ItemStack(Items.GLASS_BOTTLE, inserted));
                event.getLevel().addFreshEntity(ie);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onDrawScreenPost(RenderGuiOverlayEvent.Post event) {
        HitResult hitResult = Minecraft.getInstance().hitResult;

        if(hitResult == null) return;

        if(hitResult.getType() == HitResult.Type.BLOCK) {
            BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(new BlockPos(MathHelper.V3toV3i(hitResult.getLocation())));
            if(blockEntity == null) return;
            if(blockEntity instanceof MateriaVesselBlockEntity mvbe) {
                MateriaItem type = mvbe.getMateriaType();
                if(type != null) {
                    int x = event.getWindow().getGuiScaledWidth() / 2;
                    int y = event.getWindow().getGuiScaledHeight() / 2;

                    MutableComponent textRow1 = Component.translatable("item.magichem."+type.toString());
                    MutableComponent textRow2 = Component.literal("   " + mvbe.getCurrentStock() + " / " + mvbe.getStorageLimit());
                    MutableComponent textRow3 = Component.literal("   " + type.getDisplayFormula()).withStyle(ChatFormatting.GRAY);

                    Font font = Minecraft.getInstance().font;
                    event.getGuiGraphics().drawString(font, textRow1, x+4, y+4, 0xffffff, true);
                    event.getGuiGraphics().drawString(font, textRow2, x+4, y+14, 0xffffff, true);
                    event.getGuiGraphics().drawString(font, textRow3, x+4, y+24, 0xffffff, true);
                }
            }
        }
    }
}
