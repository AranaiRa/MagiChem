package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.*;
import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractFixationBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.block.entity.routers.*;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.events.compat.OccultismEventHelper;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.enums.AlchemicalNexusRouterType;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DistilleryRouterType;
import com.aranaira.magichem.foundation.enums.FuseryRouterType;
import com.aranaira.magichem.interop.mna.MnAPlugin;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.item.compat.occultism.OccultRitualTalismanItem;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.items.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.antlr.v4.misc.MutableInt;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.block.GrandDistilleryBlock.HAS_LABORATORY_UPGRADE;

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
        BlockState targetState = event.getLevel().getBlockState(event.getPos());
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
                if (bewe.getGrimeFromData() > 0) {
                    CommonEventHelper.generateWasteFromCleanedApparatus(event.getLevel(), bewe, stack);
                }
            } else if(stack.getItem() == ItemRegistry.LABORATORY_CHARM.get()) {
                if(target instanceof GrandDistilleryBlockEntity gdbe) {
                    if(!gdbe.getBlockState().getValue(HAS_LABORATORY_UPGRADE)) {
                        gdbe.applyLaboratoryCharm();
                        stack.shrink(1);
                        event.setCanceled(true);
                    }
                } else if(target instanceof GrandDistilleryRouterBlockEntity gdrbe) {
                    if(!gdrbe.getBlockState().getValue(HAS_LABORATORY_UPGRADE)) {
                        gdrbe.getMaster().applyLaboratoryCharm();
                        stack.shrink(1);
                        event.setCanceled(true);
                    }
                }
            }
            else if(stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
                IFluidHandler targetEntity = null;
                if(target instanceof AbstractFixationBlockEntity afbe)
                    targetEntity = afbe;
                else if(target instanceof FuseryRouterBlockEntity frbe)
                    targetEntity = frbe.getMaster();

                if(targetEntity != null) {
                    LazyOptional<IFluidHandlerItem> itemCap = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
                    if(itemCap.isPresent()) {
                        if(itemCap.resolve().isPresent()) {
                            IFluidHandlerItem itemCapResolved = itemCap.resolve().get();

                            if(itemCapResolved.getFluidInTank(0).getFluid() == FluidRegistry.ACADEMIC_SLURRY.get()) {
                                FluidStack maxTransfer = itemCapResolved.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                                int actualTransfer = targetEntity.fill(maxTransfer, IFluidHandler.FluidAction.EXECUTE);
                                itemCapResolved.drain(actualTransfer, IFluidHandler.FluidAction.EXECUTE);
                            }
                        }
                    }
                }
            }
        } else if(target instanceof AlchemicalNexusBlockEntity || target instanceof AlchemicalNexusRouterBlockEntity) {
            IFluidHandler targetEntity = null;
            if(target instanceof AlchemicalNexusBlockEntity anbe)
                targetEntity = anbe;
            else if(target instanceof AlchemicalNexusRouterBlockEntity anrbe)
                targetEntity = anrbe.getMaster();

            if(targetEntity != null) {
                LazyOptional<IFluidHandlerItem> itemCap = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
                if(itemCap.isPresent()) {
                    if(itemCap.resolve().isPresent()) {
                        IFluidHandlerItem itemCapResolved = itemCap.resolve().get();

                        if(itemCapResolved.getFluidInTank(0).getFluid() == FluidRegistry.ACADEMIC_SLURRY.get()) {
                            FluidStack maxTransfer = itemCapResolved.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                            int actualTransfer = targetEntity.fill(maxTransfer, IFluidHandler.FluidAction.EXECUTE);
                            itemCapResolved.drain(actualTransfer, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }
        } else if(target instanceof ExperienceExchangerBlockEntity eebe) {
            if(!event.getLevel().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
                if (stack.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get()) {
                    eebe.ejectStack(event.getEntity().getOnPos().above());
                    stack = eebe.setContainedStack(stack);
                    event.getEntity().setItemSlot(EquipmentSlot.MAINHAND, stack);
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
        else if(entity instanceof AlchemicalNexusBlockEntity anbe) {
            AlchemicalNexusBlock.destroyRouters(event.getLevel(), anbe.getBlockPos(), anbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        else if(entity instanceof AlchemicalNexusRouterBlockEntity anrbe) {
            event.getLevel().destroyBlock(anrbe.getMasterPos(), true);
            AlchemicalNexusBlock.destroyRouters(event.getLevel(), anrbe.getMasterPos(), anrbe.getFacing());
        }
        else if(entity instanceof GrandDistilleryBlockEntity gdbe) {
            if(state.getValue(HAS_LABORATORY_UPGRADE)) {
                ItemStack charmStack = new ItemStack(ItemRegistry.LABORATORY_CHARM.get());
                ItemEntity ie = new ItemEntity((Level)event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), charmStack);
                event.getLevel().addFreshEntity(ie);
            }

            GrandDistilleryBlock.destroyRouters(event.getLevel(), gdbe.getBlockPos(), gdbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        else if(entity instanceof GrandDistilleryRouterBlockEntity gdrbe) {
            if(state.getValue(HAS_LABORATORY_UPGRADE)) {
                ItemStack charmStack = new ItemStack(ItemRegistry.LABORATORY_CHARM.get());
                ItemEntity ie = new ItemEntity((Level)event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), charmStack);
                event.getLevel().addFreshEntity(ie);
            }

            event.getLevel().destroyBlock(gdrbe.getMasterPos(), true);
            GrandDistilleryBlock.destroyRouters(event.getLevel(), gdrbe.getMasterPos(), gdrbe.getFacing());
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
        Font font = Minecraft.getInstance().font;

        if(hitResult == null) return;
        else if(hitResult instanceof BlockHitResult bhr) {

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                int x = event.getWindow().getGuiScaledWidth() / 2;
                int y = event.getWindow().getGuiScaledHeight() / 2;

                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(bhr.getBlockPos());
                if (blockEntity == null) return;
                if (blockEntity instanceof AbstractMateriaStorageBlockEntity amsbe) {
                    MateriaItem type = amsbe.getMateriaType();
                    if (type != null && amsbe.getCurrentStock() > 0) {

                        MutableComponent textRow1 = Component.translatable("item.magichem." + type.toString());
                        MutableComponent textRow2 = Component.literal("   " + amsbe.getCurrentStock() + " / " + amsbe.getStorageLimit());
                        MutableComponent textRow3 = Component.literal("   " + type.getDisplayFormula()).withStyle(ChatFormatting.GRAY);

                        event.getGuiGraphics().drawString(font, textRow1, x + 4, y + 4, 0xffffff, true);
                        event.getGuiGraphics().drawString(font, textRow2, x + 4, y + 14, 0xffffff, true);
                        event.getGuiGraphics().drawString(font, textRow3, x + 4, y + 24, 0xffffff, true);
                    }
                } else if (Minecraft.getInstance().player.isCrouching()) {
                    List<MutableComponent> components = new ArrayList<>();
                    BlockState state = blockEntity.getBlockState();
                    int mode = 0;
                    //mode 1: all six types
                    //mode 2: just arcane and ender

                    if (blockEntity instanceof DistilleryBlockEntity dbe) {
                        mode = 1;
                    } else if (blockEntity instanceof DistilleryRouterBlockEntity drbe) {
                        if (drbe.getRouterType() == DistilleryRouterType.PLUG_LEFT) {
                            mode = 1;
                        }
                    } else if (blockEntity instanceof CentrifugeRouterBlockEntity crbe) {
                        if (crbe.getRouterType() == CentrifugeRouterType.PLUG_LEFT || crbe.getRouterType() == CentrifugeRouterType.PLUG_RIGHT) {
                            mode = 1;
                        }
                    } else if (blockEntity instanceof FuseryRouterBlockEntity frbe) {
                        if (frbe.getRouterType() == FuseryRouterType.PLUG_LEFT || frbe.getRouterType() == FuseryRouterType.PLUG_RIGHT) {
                            mode = 1;
                        }
                    } else if (blockEntity instanceof AlchemicalNexusRouterBlockEntity anrbe) {
                        if (anrbe.getRouterType() == AlchemicalNexusRouterType.PLUG_LEFT || anrbe.getRouterType() == AlchemicalNexusRouterType.PLUG_RIGHT) {
                            mode = 2;
                        }
                    }

                    if (mode == 1) {
                        components.add(Component.translatable("overlay.magichem.actuator.port"));
                        components.add(Component.literal("• ").append(Component.translatable("block.magichem.actuator_fire")));
                        components.add(Component.literal("• ").append(Component.translatable("block.magichem.actuator_water")));
                        components.add(Component.literal("• ").append(Component.translatable("block.magichem.actuator_earth")));
                        components.add(Component.literal("• ").append(Component.translatable("block.magichem.actuator_air")));
                        components.add(Component.literal("• ").append(Component.translatable("block.magichem.actuator_arcane")));
                        components.add(Component.literal("• ").append(Component.translatable("block.magichem.actuator_ender")));
                    } else if (mode == 2) {
                        components.add(Component.translatable("overlay.magichem.actuator.port"));
                        components.add(Component.literal("• ").append(Component.translatable("block.magichem.actuator_arcane")));
                        components.add(Component.literal("• ").append(Component.translatable("block.magichem.actuator_ender")));
                    }

                    for (int i = 0; i < components.size(); i++) {
                        MutableComponent c = components.get(i);

                        event.getGuiGraphics().drawString(font, c, x + 4, y + 4 + i * 10, 0xffffff, true);

                    }
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
