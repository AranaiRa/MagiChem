package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.GrandCentrifugeRouterBlock;
import com.aranaira.magichem.block.GrandDistilleryRouterBlock;
import com.aranaira.magichem.block.GrandFuseryRouterBlock;
import com.aranaira.magichem.block.entity.ColoringCauldronBlockEntity;
import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.block.entity.MagicMirrorBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeStaticBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageSingleTypeBlockEntity;
import com.aranaira.magichem.block.entity.routers.*;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import com.aranaira.magichem.foundation.enums.*;
import com.aranaira.magichem.gui.radial.*;
import com.aranaira.magichem.item.*;
import com.aranaira.magichem.networking.OpenWisdomWheelC2SPacket;
import com.aranaira.magichem.networking.ToggleWisdomC2SPacket;
import com.aranaira.magichem.recipe.ConstructStudyMaterialRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.KeybindRegistry;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mna.KeybindInit;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.config.ClientConfigValues;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ClientEventForgeBusHandler {
    private static final TagKey<Item>
            TAG_MAGICHEM_WISDOM_STONES = ItemTags.create(new ResourceLocation(MagiChemMod.MODID, "wisdom_stones"));
    private static final ResourceLocation TEXTURE_WISDOM = new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_wisdom_active.png");
    private static final HashMap<Item, ConstructStudyMaterialRecipe> studyRecipes = new HashMap<>();
    public static final ResourceLocation RENDERER_CURIO_MODEL_CROWN_OF_GLORY = new ResourceLocation(MagiChemMod.MODID, "obj/special/crown_of_glory");

    @SubscribeEvent
    public static void onRenderEntity(RenderLivingEvent.Post<Player, PlayerModel<Player>> event) {
//        if(event.getEntity() instanceof Player player) {
//            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
//                curiosInventory.getStacksHandler("head").ifPresent(slotsInventory -> {
//                    for (int i = 0; i < slotsInventory.getStacks().getSlots(); i++) {
//                        ItemStack stack = slotsInventory.getStacks().getStackInSlot(i);
//                        if(stack.getItem() == ItemRegistry.CROWN_OF_GLORY.get() && slotsInventory.isVisible()) {
//                            final PoseStack pose = event.getPoseStack();
//                            final MultiBufferSource buffer = event.getMultiBufferSource();
//                            final Vec3 pos = player.position();
//                            final PlayerModel<Player> model = event.getRenderer().getModel();
//                            final ModelPart body = model.body;
//                            final ModelPart head = model.head;
//
//                            final PartPose storedHeadPose = head.storePose();
//
//                            pose.pushPose();
//
//                            pose.translate(0, 1.375f, 0);
//                            pose.mulPose(Axis.YN.rotationDegrees(player.getYRot()));
//                            pose.mulPose(Axis.YP.rotation(head.yRot));
//                            pose.mulPose(Axis.XP.rotation(head.xRot));
//                            pose.mulPose(Axis.ZP.rotation(head.zRot));
//                            ModelUtils.renderEntityModel(buffer.getBuffer(RenderType.cutout()), player.level(), RENDERER_CURIO_MODEL_CROWN_OF_GLORY, pose, event.getPackedLight(), event.getPackedLight());
//                            pose.popPose();
//                        }
//                    }
//                });
//            });
//        }
    }

    @SubscribeEvent
    public static void renderItemTooltips(ItemTooltipEvent event) {
        if(event.getEntity() != null && event.getEntity().level() != null){
            if (studyRecipes.size() == 0) {
                for (ConstructStudyMaterialRecipe recipe : ConstructStudyMaterialRecipe.getAllConstructStudyMaterialRecipes(event.getEntity().level())) {
                    studyRecipes.put(recipe.getItem(), recipe);
                }
            }

            if (event.getItemStack().hasTag() && event.getItemStack().getTag().contains("alreadyStudied")) {
                event.getToolTip().add(1,
                        Component.empty().withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable("tooltip.magichem.event.study.part1"))
                                .append(Component.translatable("tooltip.magichem.event.study.part2.complete"))
                );
            } else if (studyRecipes.containsKey(event.getItemStack().getItem())) {
                event.getToolTip().add(1,
                        Component.empty().withStyle(ChatFormatting.GREEN)
                                .append(Component.translatable("tooltip.magichem.event.study.part1"))
                                .append(Component.literal("" + studyRecipes.get(event.getItemStack().getItem()).getExperience()))
                                .append(Component.translatable("tooltip.magichem.event.study.part2.xp"))
                                .append(Component.translatable(studyRecipes.get(event.getItemStack().getItem()).isConsumed() ? "tooltip.magichem.event.study.part3.destroys" : "tooltip.magichem.event.study.part3.once"))
                );
            }
        }
    }

    @SubscribeEvent
    public static void onDrawScreenPost(RenderGuiOverlayEvent.Post event) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        Font font = Minecraft.getInstance().font;
        Player player = Minecraft.getInstance().player;
        final GuiGraphics gui = event.getGuiGraphics();

        IPlayerMagic playerMagicCap = null;
        IWisdomCapability playerWisdomCap = null;
        ICuriosItemHandler playerCuriosCap = null;

        if(player != null) {
            final LazyOptional<IPlayerMagic> query = player.getCapability(PlayerMagicProvider.MAGIC);
            if (query.isPresent() && query.resolve().isPresent())
                playerMagicCap = query.resolve().get();
            
            if(WisdomProvider.getCapability(player).isPresent())
                playerWisdomCap = WisdomProvider.getCapability(player).get();
            
            if(CuriosApi.getCuriosInventory(player).isPresent() && CuriosApi.getCuriosInventory(player).resolve().isPresent())
                playerCuriosCap = CuriosApi.getCuriosInventory(player).resolve().get();
        }

        //handle GUI element that displays whether Wisdom is active
        if(playerMagicCap != null && playerWisdomCap != null){
            if(!playerWisdomCap.getIsDisabled() && playerMagicCap.isMagicUnlocked()) {
                Pair<Integer, Integer> coords = getHudCoordinates(event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
                int x = coords.getFirst();
                int y = coords.getSecond();

                if(playerCuriosCap.getStacksHandler("wisdom").isPresent()) {
                    if(!playerCuriosCap.getStacksHandler("wisdom").get().getStacks().getStackInSlot(0).isEmpty()) {
                        if (ClientConfigValues.HudPosition == ClientConfigValues.HudPos.BottomLeft ||
                                ClientConfigValues.HudPosition == ClientConfigValues.HudPos.BottomCenter ||
                                ClientConfigValues.HudPosition == ClientConfigValues.HudPos.BottomRight) {
                            event.getGuiGraphics().blit(TEXTURE_WISDOM, x + 14, y - 22, 0, 0, 16, 16, 16, 16);
                        } else {
                            event.getGuiGraphics().blit(TEXTURE_WISDOM, x + 14, y + 38, 0, 0, 16, 16, 16, 16);
                        }
                    }
                }
            }
        }

        if(hitResult instanceof BlockHitResult bhr) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                int x = event.getWindow().getGuiScaledWidth() / 2;
                int y = event.getWindow().getGuiScaledHeight() / 2;

                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(bhr.getBlockPos());
                if (blockEntity instanceof AbstractMateriaStorageSingleTypeBlockEntity amsbe) {
                    MateriaItem type = amsbe.getMateriaType();
                    if (type != null && amsbe.getCurrentStock() > 0) {

                        MutableComponent textRow1 = Component.translatable("item.magichem." + type.toString());
                        MutableComponent textRow2 = Component.literal("   " + amsbe.getCurrentStock() + " / " + amsbe.getStorageLimit());
                        MutableComponent textRow3 = Component.literal("   " + type.getDisplayFormula()).withStyle(ChatFormatting.GRAY);

                        gui.drawString(font, textRow1, x + 4, y + 4, 0xffffff, true);
                        gui.drawString(font, textRow2, x + 4, y + 14, 0xffffff, true);
                        gui.drawString(font, textRow3, x + 4, y + 24, 0xffffff, true);
                        return;
                    }
                }
                else if (blockEntity instanceof AbstractMateriaStorageMultiTypeStaticBlockEntity amsbe) {
                    int slot = amsbe.getSlotFromWorldCoord(hitResult.getLocation());
                    if(slot == -1) return;

                    MateriaItem type = amsbe.getMateriaTypeInSlot(slot);
                    if (type != null && amsbe.getCurrentStock(type) > 0) {
                        MutableComponent textRow1 = Component.translatable("item.magichem." + type.toString());
                        MutableComponent textRow2 = Component.literal("   " + amsbe.getCurrentStock(type) + " / " + amsbe.getStorageLimit(type));
                        MutableComponent textRow3 = Component.literal("   " + type.getDisplayFormula()).withStyle(ChatFormatting.GRAY);

                        gui.drawString(font, textRow1, x + 4, y + 4, 0xffffff, true);
                        gui.drawString(font, textRow2, x + 4, y + 14, 0xffffff, true);
                        gui.drawString(font, textRow3, x + 4, y + 24, 0xffffff, true);
                        return;
                    }
                }
                else if (blockEntity instanceof ColoringCauldronBlockEntity ccbe) {
                    final List<String> infoReadout = ccbe.getInfoReadout();

                    boolean lit = ccbe.getBlockState().getValue(LIT);
                    MutableComponent indicator = Component.literal(" [")
                            .append((lit ? Component.translatable("hud.magichem.coloring_cauldron.dye_list.subtractive") : Component.translatable("hud.magichem.coloring_cauldron.dye_list.additive")).withStyle(lit ? ChatFormatting.RED : ChatFormatting.GREEN)
                                    .append(Component.literal("]").withStyle(ChatFormatting.WHITE)));

                    gui.drawString(font, infoReadout.get(0), x + 4, y + 4, 0xffffff, true);

                    if(!ccbe.isReadyToCollect()) {
                        if (ccbe.hasColors()) {
                            gui.drawString(font, Component.translatable("hud.magichem.coloring_cauldron.remaining.part1")
                                            .append(Component.literal(""+ccbe.getOperationsRemaining())
                                                    .append(Component.translatable("hud.magichem.coloring_cauldron.remaining.part2"))),
                                    x + 4, y + 20, 0xffffff, true);

                            gui.drawString(font, Component.translatable("hud.magichem.coloring_cauldron.dye_list")
                                            .append(indicator).append(":"),
                                    x + 4, y + 36, 0xffffff, true);

                            for(int i=1; i<infoReadout.size(); i++) {
                                gui.drawString(font, infoReadout.get(i), x + 10, y + 36 + (i * 12), 0xffffff, true);
                            }
                            return;
                        } else {
                            gui.drawString(font, Component.translatable("hud.magichem.coloring_cauldron.dye_list.waiting"), x + 4, y + 20, 0xffffff, true);
                            return;
                        }
                    }
                }
                else if (blockEntity instanceof MagicMirrorBlockEntity mirror) {
                    if(mirror.getMasterDim() != null) {
                        gui.drawString(font, Component.translatable("overlay.magichem.magic_mirror.another_dimension"), x + 4, y + 4, 0xffffff, true);
                    } else if(mirror.getMaster() == null) {
                        gui.drawString(font, Component.translatable("overlay.magichem.magic_mirror.not_linked").withStyle(ChatFormatting.RED), x + 4, y + 4, 0xffffff, true);
                    } else {
                        final int dist = mirror.getMasterPos().distManhattan(mirror.getBlockPos());
                        MutableComponent mc = Component.empty()
                                .append(Component.translatable("overlay.magichem.magic_mirror.linked.part1"))
                                .append(Component.literal(dist+"m").withStyle(ChatFormatting.GOLD))
                                .append(Component.translatable("overlay.magichem.magic_mirror.linked.part2"));
                        gui.drawString(font, mc, x + 4, y + 4, 0xffffff, true);
                    }
                }
                else if (Minecraft.getInstance().player.isCrouching() && blockEntity != null) {
                    List<MutableComponent> components = new ArrayList<>();
                    BlockState state = blockEntity.getBlockState();
                    int mode = 0;
                    boolean includeProto = false;
                    //mode 1: all six types
                    //mode 2: just arcane and ender

                    if (blockEntity instanceof DistilleryBlockEntity dbe) {
                        if(CommonEventHelper.checkDirectionAndPos(dbe.getPlugDirection(), bhr)) {
                            mode = 1;
                            includeProto = true;
                        }
                    } else if (blockEntity instanceof DistilleryRouterBlockEntity drbe) {
                        if (drbe.getRouterType() == DistilleryRouterType.PLUG_LEFT) {
                            if(CommonEventHelper.checkDirectionAndPos(drbe.getPlugDirection(), bhr)) {
                                mode = 1;
                                includeProto = true;
                            }
                        }
                    } else if (blockEntity instanceof CentrifugeRouterBlockEntity crbe) {
                        if (crbe.getRouterType() == CentrifugeRouterType.PLUG_LEFT || crbe.getRouterType() == CentrifugeRouterType.PLUG_RIGHT) {
                            if(CommonEventHelper.checkDirectionAndPos(crbe.getPlugDirection(), bhr)) {
                                mode = 1;
                                includeProto = true;
                            }
                        }
                    } else if (blockEntity instanceof FuseryRouterBlockEntity frbe) {
                        if (frbe.getRouterType() == FuseryRouterType.PLUG_LEFT || frbe.getRouterType() == FuseryRouterType.PLUG_RIGHT) {
                            if(CommonEventHelper.checkDirectionAndPos(frbe.getPlugDirection(), bhr)) {
                                mode = 1;
                                includeProto = true;
                            }
                        }
                    } else if (blockEntity instanceof AlchemicalNexusRouterBlockEntity anrbe) {
                        if (anrbe.getRouterType() == AlchemicalNexusRouterType.PLUG_LEFT || anrbe.getRouterType() == AlchemicalNexusRouterType.PLUG_RIGHT) {
                            if(CommonEventHelper.checkDirectionAndPos(anrbe.getPlugDirection(), bhr)) {
                                mode = 2;
                            }
                        }
                    } else if (blockEntity instanceof PrimeAggregatorRouterBlockEntity parbe) {
                        if (parbe.getRouterType() == PrimeAggregatorRouterType.PLUG_LEFT || parbe.getRouterType() == PrimeAggregatorRouterType.PLUG_RIGHT) {
                            if(CommonEventHelper.checkDirectionAndPos(parbe.getPlugDirection(), bhr)) {
                                mode = 2;
                            }
                        }
                    } else if (blockEntity instanceof GrandCircleFabricationRouterBlockEntity gcfrbe) {
                        int routerType = gcfrbe.getBlockState().getValue(ROUTER_TYPE_GRAND_CIRCLE_FABRICATION);
                        if ((routerType == 2 || routerType == 6) && CommonEventHelper.checkDirectionAndPos(gcfrbe.getPlugDirection(), bhr)) {
                            mode = 2;
                        }
                    } else if (blockEntity instanceof GrandDistilleryRouterBlockEntity gdrbe) {
                        boolean hasLaboratoryUpgrade = state.getValue(HAS_LABORATORY_UPGRADE);
                        GrandDistilleryRouterType routerType = GrandDistilleryRouterBlock.unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_GRAND_DISTILLERY));

                        if(routerType == GrandDistilleryRouterType.PLUG_BACK_LEFT ||
                                routerType == GrandDistilleryRouterType.PLUG_BACK_RIGHT ||
                                routerType == GrandDistilleryRouterType.PLUG_FRONT_LEFT ||
                                routerType == GrandDistilleryRouterType.PLUG_FRONT_RIGHT) {

                            if(CommonEventHelper.checkDirectionAndPos(gdrbe.getPlugDirection(), bhr)) {
                                mode = 1;
                            }
                        } else if(hasLaboratoryUpgrade && (routerType == GrandDistilleryRouterType.PLUG_MID_LEFT || routerType == GrandDistilleryRouterType.PLUG_MID_RIGHT)) {

                            if(CommonEventHelper.checkDirectionAndPos(gdrbe.getPlugDirection(), bhr)) {
                                mode = 1;
                            }
                        }
                    } else if (blockEntity instanceof GrandCentrifugeRouterBlockEntity gcrbe) {
                        boolean hasLaboratoryUpgrade = state.getValue(HAS_LABORATORY_UPGRADE);
                        GrandCentrifugeRouterType routerType = GrandCentrifugeRouterBlock.unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_GRAND_CENTRIFUGE));

                        if(routerType == GrandCentrifugeRouterType.PLUG_BACK_LEFT ||
                                routerType == GrandCentrifugeRouterType.PLUG_BACK_RIGHT ||
                                routerType == GrandCentrifugeRouterType.PLUG_FRONT_LEFT ||
                                routerType == GrandCentrifugeRouterType.PLUG_FRONT_RIGHT) {

                            if(CommonEventHelper.checkDirectionAndPos(gcrbe.getPlugDirection(), bhr)) {
                                mode = 1;
                            }
                        } else if(hasLaboratoryUpgrade && (routerType == GrandCentrifugeRouterType.PLUG_MID_LEFT || routerType == GrandCentrifugeRouterType.PLUG_MID_RIGHT)) {

                            if(CommonEventHelper.checkDirectionAndPos(gcrbe.getPlugDirection(), bhr)) {
                                mode = 1;
                            }
                        }
                    } else if (blockEntity instanceof GrandFuseryRouterBlockEntity gfrbe) {
                        boolean hasLaboratoryUpgrade = state.getValue(HAS_LABORATORY_UPGRADE);
                        GrandFuseryRouterType routerType = GrandFuseryRouterBlock.unmapRouterTypeFromInt(state.getValue(ROUTER_TYPE_GRAND_FUSERY));

                        if(routerType == GrandFuseryRouterType.PLUG_BACK_LEFT ||
                                routerType == GrandFuseryRouterType.PLUG_BACK_RIGHT ||
                                routerType == GrandFuseryRouterType.PLUG_FRONT_LEFT ||
                                routerType == GrandFuseryRouterType.PLUG_FRONT_RIGHT) {

                            if(CommonEventHelper.checkDirectionAndPos(gfrbe.getPlugDirection(), bhr)) {
                                mode = 1;
                            }
                        } else if(hasLaboratoryUpgrade && (routerType == GrandFuseryRouterType.PLUG_MID_LEFT || routerType == GrandFuseryRouterType.PLUG_MID_RIGHT)) {

                            if(CommonEventHelper.checkDirectionAndPos(gfrbe.getPlugDirection(), bhr)) {
                                mode = 1;
                            }
                        }
                    }

                    if (mode == 1) {
                        components.add(Component.translatable("overlay.magichem.actuator.port"));
                        if(includeProto)
                            components.add(Component.literal("• ").append(Component.translatable("block.magichem.actuator_neutral")));
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

                        gui.drawString(font, c, x + 4, y + 4 + i * 10, 0xffffff, true);
                    }

                    if(mode != 0) return;
                }
            }
        }
        if(player != null) {
            int x = event.getWindow().getGuiScaledWidth() / 2;
            int y = event.getWindow().getGuiScaledHeight() / 2;

            ItemStack
                    mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND),
                    offHandItem = player.getItemInHand(InteractionHand.OFF_HAND),
                    targetStack = null;

            if (mainHandItem.getItem() == ItemRegistry.TRAVELLERS_COMPASS.get()) targetStack = mainHandItem;
            else if(offHandItem.getItem() == ItemRegistry.TRAVELLERS_COMPASS.get()) targetStack = offHandItem;

            if(targetStack != null) {
                if (targetStack.hasTag()) {
                    if(targetStack.getTag().contains("LodestonePos")) {
                        CompoundTag posTag = targetStack.getTag().getCompound("LodestonePos");
                        BlockPos target = new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));

                        int distance = (int) Math.round(Math.sqrt(player.getOnPos().distSqr(target)));

                        MutableComponent dist = Component.literal(distance + "m");
                        gui.drawString(font, dist, x + 4, y + 4, 0xffffff, true);

                        float time = player.level().getTimeOfDay(0);
                        gui.drawString(font, CommonEventHelper.getTimeOfDayComponent(time), x + 4, y + 14, 0x888888, true);

                        float rot = (360 + (player.getYRot() % 360)) % 360;
                        gui.drawString(font, CommonEventHelper.getFacingComponent(rot), x + 4, y + 24, 0x888888, true);
                    } else {
                        if(targetStack.getTag().contains("respawnDimension")) {
                            if(targetStack.getTag().getString("respawnDimension").equals(player.level().dimension().location().toString())) {
                                int spawnBedDist = (int) Math.round(Math.sqrt(player.getOnPos().distSqr(BlockPos.of(targetStack.getTag().getLong("respawnPosition")))));
                                gui.drawString(font, spawnBedDist + "m", x + 4, y + 4, 0xffffff, true);
                                gui.drawString(font, Component.translatable("gui.magichem.distance.bedspawn"), x + 4, y + 14, 0x888888, true);
                            } else {
                                gui.drawString(font, "?m", x + 4, y + 4, 0xffffff, true);
                                gui.drawString(font, Component.translatable("gui.magichem.distance.otherdimbed"), x + 4, y + 14, 0x888888, true);
                            }
                        } else {
                            gui.drawString(font, "?m", x + 4, y + 4, 0xffffff, true);
                            gui.drawString(font, Component.translatable("gui.magichem.distance.nobed"), x + 4, y + 14, 0x888888, true);
                        }

                        int spawnWorldDist = (int) Math.round(Math.sqrt(player.getOnPos().distSqr(player.level().getSharedSpawnPos())));
                        gui.drawString(font, spawnWorldDist+"m", x + 4, y + 24, 0xffffff, true);
                        gui.drawString(font, Component.translatable("gui.magichem.distance.worldspawn"), x + 4, y + 34, 0x888888, true);

                        float time = player.level().getTimeOfDay(0);
                        gui.drawString(font, CommonEventHelper.getTimeOfDayComponent(time), x + 4, y + 44, 0x888888, true);

                        float rot = (360 + (player.getYRot() % 360)) % 360;
                        gui.drawString(font, CommonEventHelper.getFacingComponent(rot), x + 4, y + 54, 0x888888, true);
                    }
                } else {
                    float time = player.level().getTimeOfDay(0);
                    gui.drawString(font, CommonEventHelper.getTimeOfDayComponent(time), x + 4, y + 4, 0x888888, true);
                }
            }
        }
    }

    //copied from MnA so that I can add my element to the magic HUD
    private static Pair<Integer, Integer> getHudCoordinates(int screenWidth, int screenHeight) {
        int UIWidth = 170;
        int UIHeight = 35;
        switch(ClientConfigValues.HudPosition) {
            case BottomCenter:
                return new Pair(screenWidth / 2 - UIWidth / 2, screenHeight - UIHeight - 45);
            case BottomLeft:
                return new Pair(10, screenHeight - UIHeight);
            case BottomRight:
                return new Pair(screenWidth - UIWidth, screenHeight - UIHeight);
            case MiddleLeft:
                return new Pair(-10, screenHeight / 2 - UIHeight / 2);
            case MiddleRight:
                return new Pair(screenWidth - UIWidth, screenHeight / 2 - UIHeight / 2);
            case TopCenter:
                return new Pair(screenWidth / 2 - UIWidth / 2, 1);
            case TopRight:
                return new Pair(screenWidth - UIWidth, 1);
            case TopLeft:
            default:
                return new Pair(-10, 1);
        }
    }

    static boolean wisdomToggleWasDown = false;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (!mc.isPaused() && mc.screen == null) {
            handleRadialKeyDown();

            if (event.phase == TickEvent.Phase.END) {
                if (KeybindRegistry.OpenWisdomWheel.get().isDown()) {
                    CuriosApi.getCuriosInventory(mc.player).ifPresent(curiosInventory -> {
                        curiosInventory.getStacksHandler("wisdom").ifPresent(slotsInventory -> {
                            for (int i = 0; i < slotsInventory.getStacks().getSlots(); i++) {
                                ItemStack stack = slotsInventory.getStacks().getStackInSlot(i);
                                if (stack.is(TAG_MAGICHEM_WISDOM_STONES) && stack.getItem() instanceof PhilosophersStoneItem psi) {
                                    PacketRegistry.sendToServer(new OpenWisdomWheelC2SPacket(
                                            (short)psi.getWisdom()
                                    ));
                                }
                            }
                        });
                    });
                }
                if (KeybindRegistry.ToggleWisdomEffects.get().isDown() && !wisdomToggleWasDown) {

                    if(player != null && WisdomProvider.getCapability(player).isPresent()) {
                        IWisdomCapability playerWisdomCap = WisdomProvider.getCapability(player).get();
                        playerWisdomCap.setIsDisabled(!playerWisdomCap.getIsDisabled());
                        PacketRegistry.sendToServer(new ToggleWisdomC2SPacket());
                    }
                }
                wisdomToggleWasDown = KeybindRegistry.ToggleWisdomEffects.get().isDown();
            }
        }
    }

    //Shamelessly stolen from MnA
    private static boolean toolMenuKeyWasDown;
    private static void handleRadialKeyDown() {
        Minecraft mc = Minecraft.getInstance();
        boolean toolMenuKeyIsDown = ((KeyMapping) KeybindInit.RadialMenuOpen.get()).isDown();
        if (toolMenuKeyIsDown && !toolMenuKeyWasDown) {
            if (mc.screen == null) {
                ItemStack inHand = mc.player.getMainHandItem();
                boolean checkOffhand = true;
                if (inHand.getItem() instanceof SublimationPrimerItem) {
                    mc.setScreen(new SublimationPrimerRadialSelect(false));
                    checkOffhand = false;
                } else if (inHand.getItem() instanceof TravellersCompassItem) {
                    mc.setScreen(new TravellersCompassRadialSelect(false));
                    checkOffhand = false;
                }

                if (checkOffhand) {
                    inHand = mc.player.getOffhandItem();
                    if (inHand.getItem() instanceof SublimationPrimerItem) {
                        mc.setScreen(new SublimationPrimerRadialSelect(true));
                    } else if (inHand.getItem() instanceof TravellersCompassItem) {
                        mc.setScreen(new TravellersCompassRadialSelect(true));
                    }
                }
            }
        }

        toolMenuKeyWasDown = toolMenuKeyIsDown;
    }
}
