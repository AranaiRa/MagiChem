package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.CentrifugeBlock;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterAbstractBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.items.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
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
        } else if(target instanceof AbstractBlockEntityWithEfficiency bewe) {
            if(stack.getItem() == ItemInit.ANIMUS_DUST.get()) {
                if(bewe instanceof CentrifugeBlockEntity cbe) {
                    event.getEntity().swing(event.getHand());
                    stack.shrink(1);
                    cbe.dustCog();
                } else if(bewe instanceof CentrifugeRouterAbstractBlockEntity crbe) {
                    event.getEntity().swing(event.getHand());
                    stack.shrink(1);
                    crbe.getMaster().dustCog();
                }
            }
            else if(stack.getItem() == ItemRegistry.CLEANING_BRUSH.get()) {
                if(bewe.getGrimeFromData() > 0) {
                    CommonEventHelper.generateWasteFromCleanedApparatus(event.getLevel(), bewe, stack);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        BlockEntity entity = event.getLevel().getBlockEntity(pos);

        if(entity instanceof CentrifugeRouterAbstractBlockEntity crbe) {
            event.getLevel().destroyBlock(crbe.getMasterPos(), true);
            CentrifugeBlock.destroyRouters(event.getLevel(), crbe.getMasterPos(), crbe.getFacing());
        }

        Block block = state.getBlock();
        block.getName();
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

    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<?> event) {
        if(event.getObject() instanceof AbstractBlockEntityWithEfficiency) {
            event.addCapability(IGrimeCapability.GRIME, new GrimeProvider());
        }
    }
}
