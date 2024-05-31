package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.BaseActuatorRouterBlock;
import com.aranaira.magichem.block.CentrifugeBlock;
import com.aranaira.magichem.block.DistilleryBlock;
import com.aranaira.magichem.block.FuseryBlock;
import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.block.entity.routers.BaseActuatorRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.FuseryRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
        if(target instanceof AbstractMateriaStorageBlockEntity amsbe) {
            if(stack.getItem() == Items.GLASS_BOTTLE) {
                if(amsbe.getMateriaType() != null) {
                    ItemStack extracted = amsbe.extractMateria(stack.getCount());
                    stack.shrink(extracted.getCount());

                    ItemEntity ie = new ItemEntity(event.getLevel(),
                            event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                            extracted);
                    event.getLevel().addFreshEntity(ie);
                }
            } else if(stack.getItem() instanceof MateriaItem) {
                int inserted = amsbe.insertMateria(stack);
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
                } else if(bewe instanceof CentrifugeRouterBlockEntity crbe) {
                    event.getEntity().swing(event.getHand());
                    stack.shrink(1);
                    crbe.getMaster().dustCog();
                } else if(bewe instanceof FuseryBlockEntity fbe) {
                    event.getEntity().swing(event.getHand());
                    stack.shrink(1);
                    fbe.dustCog();
                } else if(bewe instanceof FuseryRouterBlockEntity frbe) {
                    event.getEntity().swing(event.getHand());
                    stack.shrink(1);
                    frbe.getMaster().dustCog();
                }
            }
            else if(stack.getItem() == ItemRegistry.CLEANING_BRUSH.get()) {
                if(event.getEntity().isCrouching()) {
                    if (bewe.getGrimeFromData() > 0) {
                        CommonEventHelper.generateWasteFromCleanedApparatus(event.getLevel(), bewe, stack);
                    }
                }
            }
        } else if(target instanceof ExperienceExchangerBlockEntity eebe) {
            if(!event.getLevel().isClientSide()) {
                if (stack.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get()) {
                    eebe.ejectStack(event.getEntity().getOnPos().above());
                    stack = eebe.setContainedStack(stack);
                    EquipmentSlot hand = event.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    event.getEntity().setItemSlot(hand, stack);
                } else {
                    eebe.ejectStack(event.getEntity().getOnPos().above());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        BlockEntity entity = event.getLevel().getBlockEntity(pos);

        if(entity instanceof CentrifugeBlockEntity cbe) {
            CentrifugeBlock.destroyRouters(event.getLevel(), cbe.getBlockPos(), cbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        else if(entity instanceof CentrifugeRouterBlockEntity crbe) {
            event.getLevel().destroyBlock(crbe.getMasterPos(), true);
            CentrifugeBlock.destroyRouters(event.getLevel(), crbe.getMasterPos(), crbe.getFacing());
        }
        else if(entity instanceof DistilleryBlockEntity dbe) {
            DistilleryBlock.destroyRouters(event.getLevel(), dbe.getBlockPos(), dbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        else if(entity instanceof DistilleryRouterBlockEntity drbe) {
            event.getLevel().destroyBlock(drbe.getMasterPos(), true);
            DistilleryBlock.destroyRouters(event.getLevel(), drbe.getMasterPos(), drbe.getFacing());
        }
        else if(entity instanceof FuseryBlockEntity dbe) {
            FuseryBlock.destroyRouters(event.getLevel(), dbe.getBlockPos(), dbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        else if(entity instanceof FuseryRouterBlockEntity frbe) {
            event.getLevel().destroyBlock(frbe.getMasterPos(), true);
            FuseryBlock.destroyRouters(event.getLevel(), frbe.getMasterPos(), frbe.getFacing());
        }
        else if(entity instanceof DirectionalPluginBlockEntity dpbe) {
            event.getLevel().destroyBlock(dpbe.getBlockPos().above(), true);
        }
        else if(entity instanceof BaseActuatorRouterBlockEntity barbe) {
            event.getLevel().destroyBlock(barbe.getMasterPos(), true);
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
            if(blockEntity instanceof AbstractMateriaStorageBlockEntity amsbe) {
                MateriaItem type = amsbe.getMateriaType();
                if(type != null) {
                    int x = event.getWindow().getGuiScaledWidth() / 2;
                    int y = event.getWindow().getGuiScaledHeight() / 2;

                    MutableComponent textRow1 = Component.translatable("item.magichem."+type.toString());
                    MutableComponent textRow2 = Component.literal("   " + amsbe.getCurrentStock() + " / " + amsbe.getStorageLimit());
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
