package com.aranaira.magichem.events;

import com.aranaira.magichem.block.entity.ext.*;
import com.aranaira.magichem.capabilities.enhancement.EnhancementProvider;
import com.aranaira.magichem.capabilities.enhancement.IEnhancementCapability;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.*;
import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.routers.*;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.events.compat.FarmersDelightEventHelper;
import com.aranaira.magichem.events.compat.HexereiEventHelper;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.*;
import com.aranaira.magichem.interop.OccultismCompat;
import com.aranaira.magichem.item.ChaliceOfTearsItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.item.PhilosophersStoneItem;
import com.aranaira.magichem.networking.AdvancementQueryS2CPacket;
import com.aranaira.magichem.networking.ResetWisdomToggleS2CPacket;
import com.aranaira.magichem.networking.WisdomSyncC2SPacket;
import com.aranaira.magichem.networking.WisdomSyncS2CPacket;
import com.aranaira.magichem.registry.*;
import com.aranaira.magichem.registry.compat.OccultismItemRegistry;
import com.aranaira.magichem.util.InteropUtil;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mna.api.blocks.WizardLabBlock;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.events.SpellCastEvent;
import com.mna.api.events.SpellCooldownCalculatingEvent;
import com.mna.api.events.construct.ConstructSprayEffectEvent;
import com.mna.api.events.construct.ConstructSprayTargetingEvent;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.BookStandBlock;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.faction.Pixie;
import com.mna.entities.utility.WanderingWizard;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.ItemSpell;
import com.mna.tools.SummonUtils;
import com.mna.tools.TeleportHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.*;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;
import static com.aranaira.magichem.registry.MobEffectsRegistry.*;
import static com.mna.api.faction.FactionIDs.*;
import static com.mna.api.spells.collections.Components.*;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class CommonEventHandler {
    private static final TagKey<Item>
            TAG_MINECRAFT_AXES = ItemTags.create(new ResourceLocation("minecraft", "axes")),
            TAG_MAGICHEM_NODECAY = ItemTags.create(new ResourceLocation(MagiChemMod.MODID, "no_item_decay")),
            TAG_MAGICHEM_SENTINELS_PLACKART_VALID = ItemTags.create(new ResourceLocation(MagiChemMod.MODID, "sentinels_plackart_valid"));
    private static final Random r = new Random();
    private static final HashSet<UUID> playersGivenWarning = new HashSet<>();
    private static final HashMap<UUID, CachedArmorData> CACHED_ARMORS = new HashMap<>();

    public CommonEventHandler() {}

    @SubscribeEvent
    public void onConfigLoad(ModConfigEvent.Loading event) {
        ServerConfig.HAS_CONFIG_LOADED = true;
    }

    @SubscribeEvent
    public static void onItemDecay(ItemExpireEvent event) {
        if(event.getEntity().getItem().is(TAG_MAGICHEM_NODECAY)) {
            event.setExtraLife(Short.MAX_VALUE);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
        //Handle inserting or extracting from materia vessels
        BlockPos pos = event.getPos();
        ItemStack stack = event.getItemStack();
        BlockState targetState = event.getLevel().getBlockState(pos);
        BlockEntity target = event.getLevel().getBlockEntity(pos);

        //Conditional registration
        ModList modList = ModList.get();

        if(stack.getItem() instanceof IInteractsWithBlock iiwb) {
            final Pair<Boolean, InteractionResult> query = iiwb.shouldInterceptEvent(event);
            if(query.getFirst()) {
                if (iiwb.interceptEvent(event)) {
                    event.setCancellationResult(InteractionResult.CONSUME);
                    event.setCanceled(true);
                }
            } else {
                event.setCancellationResult(query.getSecond());
                event.setCanceled(true);
            }
        }
        if(target instanceof ICanHaveUnbottledMateriaInInputTray clogQuery && !stack.isEmpty() && stack.getItem() == Items.GLASS_BOTTLE && clogQuery.isClogged()) {
            if(!event.getLevel().isClientSide()) {
                ItemStack extracted = clogQuery.tryExtractUnbottled(stack);
                if (!extracted.isEmpty()) {
                    ItemEntity ie = new ItemEntity(event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), extracted);
                    event.getLevel().addFreshEntity(ie);

                    event.setCancellationResult(InteractionResult.CONSUME);
                    event.setCanceled(true);
                }
            }
        }
        else if(stack.getItem() == ItemRegistry.LABORATORY_CHARM.get() && !stack.isEmpty() && target instanceof ICanAcceptLaboratoryCharm icalc) {
            if(target.getBlockState().hasProperty(HAS_LABORATORY_UPGRADE) && !target.getBlockState().getValue(HAS_LABORATORY_UPGRADE)) {
                icalc.applyLaboratoryCharm();
                stack.shrink(1);
                event.setCancellationResult(InteractionResult.CONSUME);
                event.setCanceled(true);
            }
        }
        else if(target instanceof AbstractMateriaStorageSingleTypeBlockEntity amsbe) {
            if(stack.getItem() == Items.GLASS_BOTTLE) {
                if(amsbe.getMateriaType() != null) {
                    ItemStack extracted = amsbe.extractMateria(stack.getCount(), false);
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
        }
        else if(target instanceof ICanAcceptAnimusDust icaad) {
            if (stack.getItem() == ItemInit.ANIMUS_DUST.get()) {
                if (icaad.canAcceptDust(target.getBlockState())) {
                    event.getEntity().swing(event.getHand());
                    stack.shrink(1);
                    icaad.applyAnimusDust();
                }
            }
        }
        else if(target instanceof AbstractBlockEntityWithEfficiency bewe) {
            if(stack.getItem() == ItemRegistry.CLEANING_BRUSH.get()) {
                if (GrimeProvider.getCapability(bewe).getGrime() > 0) {
                    CommonEventHelper.generateWasteFromCleanedApparatus(event.getEntity(), event.getLevel(), bewe, stack);
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
        }
        else if(target instanceof ExperienceExchangerBlockEntity eebe) {
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
        else if(target instanceof MateriaManifestBlockEntity || target instanceof MateriaManifestRouterBlockEntity) {
            if(!event.getLevel().isClientSide() && stack.getItem() == ItemInit.RUNE_MARKING_PAIR.get()) {
                int exitCode = -1;
                if(target instanceof MateriaManifestBlockEntity mmbe) {
                    exitCode = mmbe.setMarkingPair(stack);
                } else {
                    exitCode = ((MateriaManifestRouterBlockEntity)target).getMaster().setMarkingPair(stack);
                }
                if(exitCode == 0) {
                    event.getEntity().sendSystemMessage(Component.translatable("feedback.block.materia_manifest.accepted"));
                } else if(exitCode == 1) {
                    event.getEntity().sendSystemMessage(Component.translatable("feedback.block.materia_manifest.too_far"));
                } else if(exitCode == 2) {
                    event.getEntity().sendSystemMessage(Component.empty()
                            .append(Component.translatable("feedback.block.materia_manifest.too_big.part1"))
                            .append("" + ServerConfig.materiaManifestSizeConstraint)
                            .append(Component.translatable("feedback.block.materia_manifest.too_big.part2"))
                    );
                }
                event.setCanceled(true);
            }
        }
        else if(target instanceof ICanAbsorbConstructs absorber) {
            if(!event.getLevel().isClientSide()) {
                if (stack.getItem() == ItemInit.BELL_OF_BIDDING.get()) {
                    if (stack.hasTag()) {
                        if (stack.getTag().getInt("index") == 2) {
                            boolean ding = false;
                            if(absorber.hasConstruct()) {
                                absorber.ejectConstruct();
                                ding = true;
                            } else {
                                ding = absorber.tryAbsorbConstruct(event.getEntity());
                            }

                            if(ding) {
                                event.getEntity().level().playSound((Player)null, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
        else if(stack.getItem() == ItemRegistry.CODEX_MATERIA.get()) {
            BlockState selectedState = null;
            BlockPos selectedPos = null;
            BlockState belowState = event.getLevel().getBlockState(event.getPos().below());
            if(targetState.getBlock() == BlockInit.BOOK_STAND.get()) {
                selectedState = targetState;
                selectedPos = event.getPos();
            } else if(targetState.getBlock() == BlockInit.EMPTY_FILLER_BLOCK.get() && belowState.getBlock() == BlockInit.BOOK_STAND.get()) {
                selectedState = belowState;
                selectedPos = event.getPos().below();
            }

            if(selectedState != null) {
                if(!selectedState.getValue(BookStandBlock.BOOK) && !event.getLevel().isClientSide()) {
                    boolean left = selectedState.getValue(WizardLabBlock.LEFT);
                    boolean right = selectedState.getValue(WizardLabBlock.RIGHT);
                    final Direction dir = selectedState.getValue(HorizontalDirectionalBlock.FACING);

                    BlockState newState = BlockRegistry.LECTERN_WITH_CODEX_MATERIA.get().defaultBlockState()
                            .setValue(WizardLabBlock.LEFT, left)
                            .setValue(WizardLabBlock.RIGHT, right)
                            .setValue(HorizontalDirectionalBlock.FACING, dir);
                    event.getLevel().setBlock(selectedPos, Blocks.AIR.defaultBlockState(), 4);
                    event.getLevel().setBlock(selectedPos, newState, 3);

                    event.getEntity().setItemInHand(event.getHand(), ItemStack.EMPTY);

                    event.setCancellationResult(InteractionResult.CONSUME);
                    event.setCanceled(true);
                }
            }
        }
        else if(stack.is(TAG_MINECRAFT_AXES) || stack.getItem() == ItemInit.BOUND_AXE.get()) {
            InteropUtil.tryGenerateVerdigris(event.getLevel(), event.getPos(), event.getHitVec());
        }
        else if(modList.isLoaded("farmersdelight")) {
            FarmersDelightEventHelper.onBlockActivated(event);
        }
        else if(modList.isLoaded("hexerei")) {
            HexereiEventHelper.onBlockActivated(event);
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        BlockEntity entity = event.getLevel().getBlockEntity(pos);

        if(entity instanceof IRequiresRouterCleanupOnDestruction irrcod) {
            irrcod.destroyRouters();
        } else if(entity instanceof IDestroysMasterOnDestruction idmod) {
            idmod.destroyMaster();
        }

        Block block = state.getBlock();
        block.getName();
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if(event.getEntity() instanceof LightningBolt bolt) {
            BlockPos onPos = bolt.getOnPos();

            int altarsTriggered = 0;
            for(int y=-2;y<=2;y++) {
                for(int x=-2;x<=2;x++) {
                    for(int z=-2;z<=2;z++) {
                        BlockEntity entityQuery = event.getLevel().getBlockEntity(onPos.offset(x, y, z));
                        if(entityQuery instanceof SkywrathAltarBlockEntity sabe) {
                            sabe.tryCraftItem();
                            altarsTriggered++;
                        }
                        if(altarsTriggered >= 4) break;
                    }
                    if(altarsTriggered >= 4) break;
                }
                if(altarsTriggered >= 4) break;
            }
        }
        else if(event.getEntity() instanceof ServerPlayer sp && !sp.level().isClientSide()) {
            if (!playersGivenWarning.contains(sp.getUUID())) {
                sp.sendSystemMessage(Component.translatable("feedback.warning.api_bug_tier_tooltips"));
                playersGivenWarning.add(sp.getUUID());
            }

            if(CACHED_ARMORS.containsKey(sp.getUUID())) {
                CACHED_ARMORS.get(sp.getUUID()).snapshot(sp);
            } else {
                CachedArmorData cad = new CachedArmorData();
                cad.snapshot(sp);
                CACHED_ARMORS.put(sp.getUUID(), cad);
            }

            final Optional<IWisdomCapability> wisdomCap = WisdomProvider.getCapability(sp);
            if(wisdomCap.isPresent()) {
                final Pair<Short, Short> cardinalIntercardinalPair = WisdomProvider.serializeShorts(wisdomCap.get());

                MagiChemMod.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> sp), new WisdomSyncS2CPacket(
                                cardinalIntercardinalPair.getFirst(),
                                cardinalIntercardinalPair.getSecond()
                        ));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if(event.getEntity() instanceof Player player) {
            final LazyOptional<IPlayerProgression> progressCapability = player.getCapability(PlayerProgressionProvider.PROGRESSION);
            progressCapability.ifPresent(pCap -> {
                final IFaction faction = pCap.getAlliedFaction();
                if(pCap.getTier() >= 5 && faction != null && faction.is(UNDEAD)) {
                    final LazyOptional<IEnhancementCapability> enhancementCapability = player.getCapability(EnhancementProvider.ENHANCEMENT);
                    enhancementCapability.ifPresent(eCap -> {
                        if(eCap.hasDeathRecoveryLocation()) {
                            final Pair<BlockPos, ResourceLocation> recoveryData = eCap.getDeathRecoveryLocation();
                            final ServerLevel sourceLevel = event.getEntity().level().getServer().getLevel(event.getEntity().level().dimension());
                            final ResourceKey<Level> destinationDimension = ResourceKey.create(Registries.DIMENSION, recoveryData.getSecond());
                            final ServerLevel destinationLevel = event.getEntity().level().getServer().getLevel(destinationDimension);

                            if(destinationLevel != null && destinationLevel.getGameTime() >= eCap.getBossTrophyUseTargetTime()){
                                eCap.setLastDeathTargetLocation(event.getEntity().blockPosition(), event.getEntity().level().dimension().location());
                                eCap.setBossTrophyUseTargetTime(destinationLevel.getGameTime() + 12000);

                                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 8, 4, false, false, false));
                                player.addEffect(new MobEffectInstance(RADIANT_RESOLVE.get(), 8, 4, false, false, false));
                                for (MobEffectInstance mei : player.getActiveEffects()) {
                                    if (mei.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                                        player.removeEffect(mei.getEffect());
                                    }
                                }

                                player.setHealth(10);

                                float wrappedYaw = Mth.wrapDegrees(player.getYRot());
                                if (event.getEntity().level() == destinationLevel) {
                                    event.getEntity().teleportTo(recoveryData.getFirst().getX(), recoveryData.getFirst().getY(), recoveryData.getFirst().getZ());
                                    event.getEntity().setYHeadRot(wrappedYaw);
                                } else {
                                    event.getEntity().unRide();
                                    Entity entity = event.getEntity().getType().create(sourceLevel);
                                    if (entity == null) {
                                        return;
                                    }

                                    entity.restoreFrom(event.getEntity());
                                    event.getEntity().teleportTo(recoveryData.getFirst().getX(), recoveryData.getFirst().getY(), recoveryData.getFirst().getZ());
                                    entity.setYHeadRot(wrappedYaw);
                                    sourceLevel.addDuringTeleport(entity);
                                    event.getEntity().remove(Entity.RemovalReason.CHANGED_DIMENSION);
                                }
                                event.setCanceled(true);

                                player.sendSystemMessage(Component.translatable("feedback.trophy.undead.death_interception"));
                            }
                        }
                    });
                }
            });
        }
    }

    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<?> event) {
        if(event.getObject() instanceof AbstractBlockEntityWithEfficiency) {
            event.addCapability(IGrimeCapability.GRIME, new GrimeProvider());
        }
        else if(event.getObject() instanceof Player) {
            event.addCapability(IWisdomCapability.WISDOM, new WisdomProvider());
            event.addCapability(IEnhancementCapability.ENHANCEMENT, new EnhancementProvider());
        }
    }

    @SubscribeEvent
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        if(event.getEntity() instanceof ItemEntity ie) {
            if(ie.getItem().getItem() == ItemRegistry.THUNDERSTONE.get()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        if(!event.getEntity().level().isClientSide()) {
            if(event.getEntity() instanceof Player player){
                Inventory inventory = player.getInventory();
                CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                    if(!CACHED_ARMORS.containsKey(player.getUUID())) {
                        CachedArmorData cad = new CachedArmorData();
                        cad.snapshot(player);
                        CACHED_ARMORS.put(player.getUUID(), cad);
                    }

                    MutableInt wisdom = new MutableInt(0);
                    curiosInventory.getStacksHandler("wisdom").ifPresent(slotsInventory -> {
                        for (int i = 0; i < slotsInventory.getStacks().getSlots(); i++) {
                            ItemStack stack = slotsInventory.getStacks().getStackInSlot(i);
                            if(stack.getItem() instanceof PhilosophersStoneItem stone) {
                                wisdom.setValue(stone.getWisdom());
                            }
                        }
                    });
                    curiosInventory.getStacksHandler("body").ifPresent(slotsInventory -> {
                        for (int i = 0; i < slotsInventory.getStacks().getSlots(); i++) {
                            ItemStack stack = slotsInventory.getStacks().getStackInSlot(i);
                            if(stack.getItem() == ItemRegistry.SENTINELS_PLACKART.get()) {
                                boolean doRefund = r.nextDouble() > (1d / (wisdom.intValue() + 2));
                                for(ItemStack armorStack : inventory.armor) {
                                    if(armorStack.getItem() instanceof ArmorItem armorItem && armorStack.is(TAG_MAGICHEM_SENTINELS_PLACKART_VALID)) {
                                        if (!armorStack.isEmpty()) {
                                            final CachedArmorData data = CACHED_ARMORS.get(player.getUUID());
                                            if (data.matchesPrevious(armorItem)) {
                                                if (doRefund) {
                                                    data.restore(armorStack);
                                                }
                                            }
                                            CACHED_ARMORS.get(player.getUUID()).setPrevious(armorItem, armorStack.getDamageValue());
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    });
                });

                //Chalice of Tears fill
                for (ItemStack item : inventory.items) {
                    if(!item.isEmpty() && item.getItem() == ItemRegistry.CHALICE_OF_TEARS.get() && !player.getCooldowns().isOnCooldown(item.getItem())) {
                        if(item.hasTag()) {
                            CompoundTag nbt = item.getTag();
                            if(nbt.contains("damageAccumulated")) {
                                int existingDamage = nbt.getInt("damageAccumulated");
                                int newDamage = Math.round(event.getAmount());
                                int limit = ChaliceOfTearsItem.getDamageAccumulationLimit();
                                nbt.putInt("damageAccumulated", Math.min(limit, existingDamage + newDamage));
                            }
                            else {
                                int newDamage = Math.round(event.getAmount());
                                nbt.putInt("damageAccumulated", newDamage);
                            }
                        } else {
                            CompoundTag nbt = item.getOrCreateTag();
                            int newDamage = Math.round(event.getAmount());
                            nbt.putInt("damageAccumulated", newDamage);
                        }
                    }
                }
            }

            //Golden Resurgence cancellation
            if (event.getEntity().hasEffect(MobEffectsRegistry.GOLDEN_RESURGENCE.get())) {
                event.setCanceled(true);
            }
            //Brutality damage reduction
            if (event.getEntity().hasEffect(MobEffectsRegistry.BRUTALITY.get())) {
                final MobEffectInstance effect = event.getEntity().getEffect(MobEffectsRegistry.BRUTALITY.get());
                event.setAmount(event.getAmount() * BRUTALITY_INCOMING_DAMAGE_REDUCTION[Math.min(effect.getAmplifier(), BRUTALITY_INCOMING_DAMAGE_REDUCTION.length)]);
            }
            //Evanescence evasion
            if (event.getEntity().hasEffect(MobEffectsRegistry.EVANESCENCE.get())) {
                final MobEffectInstance effect = event.getEntity().getEffect(EVANESCENCE.get());
                if (r.nextInt(100) < EVANESCENCE_EVASION_RATE[Math.min(effect.getAmplifier(), EVANESCENCE_EVASION_RATE.length)]) {
                    event.setCanceled(true);
                }
            }
            //Equanimity mana recovery
            if (event.getEntity().hasEffect(EQUANIMITY.get())) {
                final MobEffectInstance effect = event.getEntity().getEffect(EQUANIMITY.get());
                float perHeart = EQUANIMITY_MANA_PER_HEART[Math.min(effect.getAmplifier(), EQUANIMITY_MANA_PER_HEART.length)];
                float manaRecovery = event.getAmount() * perHeart;

                LazyOptional<IPlayerMagic> capLazy = event.getEntity().getCapability(PlayerMagicProvider.MAGIC);
                if(capLazy.isPresent()) {
                    Optional<IPlayerMagic> capQuery = capLazy.resolve();
                    capQuery.ifPresent(iPlayerMagic -> iPlayerMagic.getCastingResource().restore(manaRecovery));
                }
            }

            if (event.getSource().getEntity() instanceof LivingEntity living) {
                //Brutality damage boost
                if (living.hasEffect(MobEffectsRegistry.BRUTALITY.get()) && event.getSource().type().msgId().equals("player")) {
                    final MobEffectInstance effect = living.getEffect(MobEffectsRegistry.BRUTALITY.get());
                    float boost = BRUTALITY_BASE_DAMAGE_INCREASE[Math.min(effect.getAmplifier(), BRUTALITY_BASE_DAMAGE_INCREASE.length)];
                    float multiplier = BRUTALITY_DAMAGE_AMPLIFICATION[Math.min(effect.getAmplifier(), BRUTALITY_DAMAGE_AMPLIFICATION.length)];
                    event.setAmount((event.getAmount() + boost) * multiplier);
                }
                //Malice Wither discharge
                if (living.hasEffect(MobEffectsRegistry.MALICE.get()) && event.getSource().type().msgId().equals("player")) {
                    final MobEffectInstance effect = living.getEffect(MobEffectsRegistry.MALICE.get());
                    float damage = MALICE_DISCHARGE_DAMAGE[Math.min(effect.getAmplifier(), MALICE_DISCHARGE_DAMAGE.length)];

                    event.getEntity().removeEffect(MobEffects.WITHER);
                    event.getEntity().hurt(event.getEntity().damageSources().magic(), damage);
                }
            }
        }

        if(event.getEntity() instanceof Player player && event.getEntity().getHealth() - event.getAmount() <= 0) {
            if(!player.hasEffect(GOLDEN_RESURGENCE.get())) {
                CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                    curiosInventory.getStacksHandler("wisdom").ifPresent(slotsInventory -> {
                        for(int i=0; i<slotsInventory.getStacks().getSlots(); i++) {
                            final ItemStack query = slotsInventory.getStacks().getStackInSlot(i);
                            if(query.getItem() instanceof PhilosophersStoneItem wisdom && wisdom.getWisdom() >= 4) {
                                if (!player.getCooldowns().isOnCooldown(wisdom)) {
                                    player.getCooldowns().addCooldown(wisdom, wisdom.getWisdom() > 4 ? 12000 : 72000);
                                    player.addEffect(new MobEffectInstance(GOLDEN_RESURGENCE.get(), 120, 0, true, true));
                                    player.addEffect(new MobEffectInstance(RADIANT_RESOLVE.get(), 120, 0, true, true));
                                    player.heal(200f);
                                    player.displayClientMessage(Component.translatable("feedback.item.wisdom.death_protection"), true);
                                    event.setCanceled(true);
                                }
                            }
                        }
                    });
                });
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEntity() instanceof Player player && event.getEffectInstance() != null) {
            if (event.getEffectInstance().getEffect() == CHAINSPELL.get()) {
                player.sendSystemMessage(Component.translatable("feedback.trophy.council.expire"));
            } else if (event.getEffectInstance().getEffect() == REGAL_TWILIGHT.get()) {
                player.sendSystemMessage(Component.translatable("feedback.trophy.fey.expire"));
                for (Mob summon : SummonUtils.getSummons(player)) {
                    if (summon instanceof Pixie pixie) {
                        pixie.kill();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffectInstance() != null) {
            if (event.getEffectInstance().getEffect() == GOLDEN_RESURGENCE.get() ||
                event.getEffectInstance().getEffect() == EQUANIMITY.get() ||
                event.getEffectInstance().getEffect() == EVANESCENCE.get() ||
                event.getEffectInstance().getEffect() == BRUTALITY.get() ||
                event.getEffectInstance().getEffect() == MALICE.get()) {
                event.setCanceled(true);
            }
        }
    }

    private static final SpellEffect[] DAMAGE_COMPONENTS = new SpellEffect[]{
            FIRE_DAMAGE, BACKDRAFT, FROST_DAMAGE, SHATTER, LIGHTNING_DAMAGE, MAGIC_DAMAGE, IMPALE, WIND_SHEAR, PURGE
    };
    @SubscribeEvent
    public static void onCalculateSpellCooldown(SpellCooldownCalculatingEvent event) {
        final Player caster = event.getCaster();
        final ISpellDefinition spell = event.getSpell();

        int floor = 4;
        for (IModifiedSpellPart<SpellEffect> component : spell.getComponents()) {
            if(Arrays.asList(DAMAGE_COMPONENTS).contains(component.getPart())) {
                floor = 12;
                break;
            }
        }

        if(caster.hasEffect(CHAINSPELL.get())) {
            if (event.getCooldown() > floor) {
                int diff = event.getCooldown() - floor;
                int xpCost = Math.max(1, diff / 8);
                caster.giveExperiencePoints(-xpCost);
                event.setCooldown(floor);

                if (caster.totalExperience <= 0)
                    caster.removeEffect(CHAINSPELL.get());
            }
        }
    }

    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        final LivingEntity caster = event.getSource().getCaster();
        if(!caster.level().isClientSide()) {
            final ISpellDefinition spell = event.getSpell();

            if (caster.hasEffect(CHAINSPELL.get())) {
                spell.setManaCost(spell.getManaCost() * 0.667f);
            }
            if (caster.hasEffect(EQUANIMITY.get())) {
                final MobEffectInstance effect = caster.getEffect(EQUANIMITY.get());
                if(!spell.isChanneled()) {
                    float cost = spell.getManaCost();
                    float heal = Math.max(1.0f, cost * EQUANIMITY_HEAL_PER_MANA[Math.min(effect.getAmplifier(), EQUANIMITY_HEAL_PER_MANA.length)]);
                    caster.heal(heal);
                }
            }
            if (caster.hasEffect(MALICE.get())) {
                final MobEffectInstance effect = caster.getEffect(MALICE.get());

                float radius = MALICE_RADIUS[Math.min(effect.getAmplifier(), MALICE_RADIUS.length)];
                int amplifier = MALICE_WITHER_LEVEL[Math.min(effect.getAmplifier(), MALICE_WITHER_LEVEL.length)];

                AABB bounds = new AABB(caster.getX() - radius, caster.getY() - radius, caster.getZ() - radius, caster.getX() + radius, caster.getY() + radius, caster.getZ() + radius);
                for(Entity e : caster.level().getEntities(null, bounds)) {
                    if(e instanceof LivingEntity living && e != caster) {
                        boolean isMyConstruct = (e instanceof Construct c) && (c.getOwner() == caster);
                        boolean isMySummon = SummonUtils.isSummon(e) && SummonUtils.getSummoner(living) == caster;

                        if(!isMyConstruct && !isMySummon && !living.hasEffect(MobEffects.WITHER))
                            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, amplifier));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onUsePortal(PlayerEvent.PlayerChangedDimensionEvent event) {
        final ResourceKey<Level> to = event.getTo();

        if(to.location() != null) {
            if(to.location().equals(new ResourceLocation("minecraft:the_nether"))) {
                Level level = event.getEntity().level();
                BlockPos pos = event.getEntity().blockPosition();
                Block blockAtEntry = level.getBlockState(pos).getBlock();

                //If we entered through a portal, we want to shift the block by one so that the player doesn't warp back INTO the portal
                //We check two blocks ahead and two blocks back, if none of those are a solid spot to stand on we just put it in the portal anyway
                if(blockAtEntry == Blocks.NETHER_PORTAL) {
                    Vec3 fwdRaw = event.getEntity().getForward();
                    Vec3i fwd = new Vec3i((int)Math.round(fwdRaw.x), (int)Math.round(fwdRaw.y), (int)Math.round(fwdRaw.z));
                    BlockPos safePos = null;
                    BlockPos[] coordsToCheck = new BlockPos[] {
                            pos.offset(fwd.multiply(3)),
                            pos.offset(fwd.multiply(2)),
                            pos.offset(fwd),
                            pos.offset(fwd.multiply(-3)),
                            pos.offset(fwd.multiply(-2)),
                            pos.offset(fwd.multiply(-1))
                    };

                    for(BlockPos coordQuery : coordsToCheck) {
                        for(int i=-3; i<=3; i++){
                            if (!level.getBlockState(coordQuery.below(i)).isAir()) {
                                //make sure the player won't get suffocated or warp in partially inside a block
                                if(level.getBlockState(coordQuery.below(i - 1)).isAir() && level.getBlockState(coordQuery.below(i - 2)).isAir()) {
                                    safePos = coordQuery.below(i - 1);
                                    break;
                                }
                            }
                        }
                        if(safePos != null)
                            break;
                    }

                    if(safePos != null) {
                        event.getEntity().getPersistentData().putLong("lastNetherPortal", safePos.asLong());
                    }
                }
            }
        }
    }

    private static final HashMap<Player, HashSet<MobEffect>> pendingHeartEffects = new HashMap<>();
    @SubscribeEvent
    public static void checkCanApplyMobEffect(MobEffectEvent.Applicable event) {
        final LivingEntity entity = event.getEntity();
        if(entity != null && !entity.level().isClientSide()) {
            //Immortal Heart duration modification
            if(event.getEntity() instanceof Player player) {
                final LazyOptional<IEnhancementCapability> capLazy = player.getCapability(EnhancementProvider.ENHANCEMENT);
                if(capLazy.isPresent()) {
                    final Optional<IEnhancementCapability> capQuery = capLazy.resolve();
                    if(capQuery.isPresent()) {
                        final IEnhancementCapability cap = capQuery.get();
                        MobEffectInstance effect = event.getEffectInstance();

                        if(cap.getHeart() == IEnhancementCapability.EnhancedHeartType.IMMORTAL) {
                            boolean onBlacklist = false;
                            for(int i=0; i<IEnhancementCapability.BLACKLIST.length; i++) {
                                if(effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) continue;

                                String query = effect.getEffect().getDescriptionId();
                                if(query.equals(IEnhancementCapability.BLACKLIST[i])) {
                                    onBlacklist = true;
                                    break;
                                }
                            }
                            if(!onBlacklist){
                                if (isPendingEffect(player, effect.getEffect()) || effect.getDuration() == -1) {
                                    removeFromPendingEffects(player, effect.getEffect());
                                } else {
                                    if (effect.getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
                                        event.setResult(Event.Result.DENY);
                                        addToPendingEffects(player, effect.getEffect());
                                        player.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration() * 3, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
                                    } else if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                                        event.setResult(Event.Result.DENY);
                                        addToPendingEffects(player, effect.getEffect());
                                        player.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration() / 3, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Set<MobEffect> keys = entity.getActiveEffectsMap().keySet();
            if (keys.size() > 0) {
                MobEffect incomingEffect = event.getEffectInstance().getEffect();
                boolean entityHasRadiantResolve = keys.contains(MobEffectsRegistry.RADIANT_RESOLVE.get());
                boolean entityHasAcidWard = keys.contains(MobEffectsRegistry.ACID_WARD.get());
                boolean entityHasAbatedDissolution = keys.contains(MobEffectsRegistry.ABATED_DISSOLUTION.get());

                //Radiant Resolve cancelling negative status
                if (entityHasRadiantResolve) {
                    boolean incomingEffectNegative = event.getEffectInstance().getEffect().getCategory() == MobEffectCategory.HARMFUL;
                    boolean hasEffectAlready = keys.contains(event.getEffectInstance().getEffect());

                    if (incomingEffectNegative && !hasEffectAlready) {
                        event.setResult(Event.Result.DENY);
                    }
                }

                //Acid Ward reducing incoming Dissolution effects
                if (incomingEffect == MobEffectsRegistry.DISSOLUTION.get()) {
                    if (entityHasAbatedDissolution) {
                        event.setResult(Event.Result.DENY);
                    } else if (entityHasAcidWard) {
                        final MobEffectInstance acidWard = entity.getActiveEffectsMap().get(MobEffectsRegistry.ACID_WARD.get());
                        int wardPotency = acidWard.getAmplifier() + 1;
                        int acidPotency = event.getEffectInstance().getAmplifier();
                        int acidDuration = event.getEffectInstance().getDuration();
                        boolean acidIsAmbient = event.getEffectInstance().isAmbient();
                        boolean acidIsVisible = event.getEffectInstance().isVisible();
                        boolean acidShowIcon = event.getEffectInstance().showIcon();

                        if (acidPotency - wardPotency >= 0) {
                            MobEffectInstance mei = new MobEffectInstance(MobEffectsRegistry.ABATED_DISSOLUTION.get(), acidDuration, acidPotency - wardPotency, acidIsAmbient, acidIsVisible, acidShowIcon);
                            entity.addEffect(mei);
                        }

                        event.setResult(Event.Result.DENY);
                    }
                }
            }
        }
    }

    private static void addToPendingEffects(Player player, MobEffect effect) {
        if(pendingHeartEffects.containsKey(player)) {
            pendingHeartEffects.get(player).add(effect);
        } else {
            HashSet<MobEffect> effectsOnPlayer = new HashSet<>();
            effectsOnPlayer.add(effect);
            pendingHeartEffects.put(player, effectsOnPlayer);
        }
    }

    private static void removeFromPendingEffects(Player player, MobEffect effect) {
        if(pendingHeartEffects.containsKey(player)) {
            pendingHeartEffects.get(player).remove(effect);
        }
    }

    private static boolean isPendingEffect(Player player, MobEffect effect) {
        if(pendingHeartEffects.containsKey(player)) {
            final HashSet<MobEffect> effectsOnPlayer = pendingHeartEffects.get(player);
            return effectsOnPlayer.contains(effect);
        }
        return false;
    }

    @SubscribeEvent
    public static void onEntityActivatedWithItem(PlayerInteractEvent.EntityInteract event) {
        if(event.getTarget() instanceof WanderingWizard ww) {
            if(event.getEntity().getItemInHand(event.getHand()).getItem() == ItemInit.GUIDE_BOOK.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.CONSUME);
                if(!event.getLevel().isClientSide()) {
                    event.getEntity().setItemInHand(event.getHand(), ItemStack.EMPTY);
                    SimpleContainer codex = new SimpleContainer(1);
                    codex.setItem(0, new ItemStack(ItemRegistry.CODEX_MATERIA.get()));
                    Containers.dropContents(event.getLevel(), ww, codex);
                } else {
                    event.getEntity().sendSystemMessage(Component.translatable("chat.magichem.trade_for_codex_materia"));
                }
            }
            else if(event.getEntity().getItemInHand(event.getHand()).getItem() == ItemRegistry.CODEX_MATERIA.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.CONSUME);
                if(!event.getLevel().isClientSide()) {
                    event.getEntity().setItemInHand(event.getHand(), ItemStack.EMPTY);
                    SimpleContainer codex = new SimpleContainer(1);
                    codex.setItem(0, new ItemStack(ItemInit.GUIDE_BOOK.get()));
                    Containers.dropContents(event.getLevel(), ww, codex);
                } else {
                    event.getEntity().sendSystemMessage(Component.translatable("chat.magichem.trade_for_codex_arcana"));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderItemTooltip(ItemTooltipEvent event) {
        if(event.getItemStack().hasTag()) {
            final CompoundTag nbt = event.getItemStack().getTag();
            if(nbt != null && nbt.contains("magichemLumins")) {
                CompoundTag luminsTag = nbt.getCompound("magichemLumins");
                int type = luminsTag.getInt("type");
                int current = luminsTag.getInt("current") / 12;
                int needed = luminsTag.getInt("needed") / 12;

                event.getToolTip().add(1,
                        LuminType.luminComponentFromOrdinal(type).withStyle(LuminType.luminComponentFormattingFromOrdinal(type))
                                .append(" ["+current+"/"+needed+"]")
                );
            }
        }
    }

    @SubscribeEvent
    public static void onMobSpawnPositionCheck(MobSpawnEvent.PositionCheck event) {
        if(event.getSpawnType() == MobSpawnType.NATURAL) {
            for (Player player : event.getLevel().players()) {
                if (player.hasEffect(MobEffectsRegistry.SUNS_GRACE.get())) {
                    if(!(event.getEntity() instanceof AbstractSchoolingFish || event.getEntity() instanceof Squid)) {
                        if (event.getEntity().getPosition(0).distanceTo(player.getPosition(0)) <= 128)
                            event.setResult(Event.Result.DENY);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMobSpawnFinalize(MobSpawnEvent.FinalizeSpawn event) {
        if(event.getEntity().canAttackType(EntityType.PLAYER)) {
            for (Player player : event.getLevel().players()) {
                if (player.hasEffect(MobEffectsRegistry.SUNS_SCORN.get())) {
                    if(!(event.getEntity() instanceof AbstractSchoolingFish || event.getEntity() instanceof Squid)) {
                        for (int i = 0; i < r.nextInt(4) + 2; i++) {
                            event.getLevel().addFreshEntity(event.getEntity());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onConstructSprayTarget(ConstructSprayTargetingEvent event) {
        final Fluid fluid = event.getFluid();
        if(event.isTargetFriendly()) {
            if (fluid == FluidRegistry.SIMPLE_ACID.get() || fluid == FluidRegistry.AQUA_FORTIS.get() || fluid == FluidRegistry.AQUA_REGIA.get() || fluid == FluidRegistry.OIL_OF_VITRIOL.get() || fluid == FluidRegistry.AZOTH.get()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onConstructSpray(ConstructSprayEffectEvent event) {
        final Fluid fluid = event.getFluid();
        if(fluid == FluidRegistry.SIMPLE_ACID.get()) {
            event.getTarget().addEffect(new MobEffectInstance(MobEffectsRegistry.DISSOLUTION.get(), 200));
        } else if(fluid == FluidRegistry.AQUA_FORTIS.get()) {
            event.getTarget().addEffect(new MobEffectInstance(MobEffectsRegistry.DISSOLUTION.get(), 200, 1));
        } else if(fluid == FluidRegistry.AQUA_REGIA.get()) {
            event.getTarget().addEffect(new MobEffectInstance(MobEffectsRegistry.DISSOLUTION.get(), 200, 2));
        } else if(fluid == FluidRegistry.OIL_OF_VITRIOL.get()) {
            event.getTarget().addEffect(new MobEffectInstance(MobEffectsRegistry.DISSOLUTION.get(), 200, 3));
        } else if(fluid == FluidRegistry.AZOTH.get()) {
            event.getTarget().addEffect(new MobEffectInstance(MobEffectsRegistry.DISSOLUTION.get(), 200, 4));
        }
    }

    private static final UUID
        UUID_CHARM = UUID.fromString("78d0603e-1e08-42e9-80b2-84b7d9ca2c86"),
        UUID_BRACELET = UUID.fromString("78d0603e-1e08-42e9-80b2-84b7d9ca2c85"),
        UUID_NECKLACE = UUID.fromString("78d0603e-1e08-42e9-80b2-84b7d9ca2c84"),
        UUID_BELT = UUID.fromString("78d0603e-1e08-42e9-80b2-84b7d9ca2c83"),
        UUID_HEAD = UUID.fromString("78d0603e-1e08-42e9-80b2-84b7d9ca2c82"),
        UUID_RING = UUID.fromString("78d0603e-1e08-42e9-80b2-84b7d9ca2c81");
    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if(event.getIdentifier().equals("wisdom")) {
            if(event.getEntity() instanceof ServerPlayer p && WisdomProvider.getCapability(p).isPresent()) {
                WisdomProvider.getCapability(p).get().setIsDisabled(false);

                MagiChemMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new ResetWisdomToggleS2CPacket());
            }
        }
        else if(event.getTo().getItem() == ItemRegistry.CROWN_OF_GLORY.get()) {
            CuriosApi.getCuriosInventory(event.getEntity()).ifPresent(inventory -> {
                LinkedHashMultimap<@Nullable String, @Nullable AttributeModifier> map = LinkedHashMultimap.create();
                map.put("head", new AttributeModifier(UUID_HEAD, "crown_of_glory", 1, AttributeModifier.Operation.ADDITION));
                map.put("ring", new AttributeModifier(UUID_RING, "crown_of_glory", 2, AttributeModifier.Operation.ADDITION));
                map.put("bracelet", new AttributeModifier(UUID_BRACELET, "crown_of_glory", 1, AttributeModifier.Operation.ADDITION));
                map.put("necklace", new AttributeModifier(UUID_NECKLACE, "crown_of_glory", 1, AttributeModifier.Operation.ADDITION));
                map.put("belt", new AttributeModifier(UUID_BELT, "crown_of_glory", 1, AttributeModifier.Operation.ADDITION));
                map.put("charm", new AttributeModifier(UUID_CHARM, "crown_of_glory", 2, AttributeModifier.Operation.ADDITION));
                inventory.addTransientSlotModifiers(map);
            });
        }
        else if(event.getFrom().getItem() == ItemRegistry.CROWN_OF_GLORY.get()) {
            CuriosApi.getCuriosInventory(event.getEntity()).ifPresent(inventory -> {
                LinkedHashMultimap<@Nullable String, @Nullable AttributeModifier> map = LinkedHashMultimap.create();
                map.put("head", new AttributeModifier(UUID_HEAD, "crown_of_glory", 0, AttributeModifier.Operation.ADDITION));
                map.put("ring", new AttributeModifier(UUID_RING, "crown_of_glory", 0, AttributeModifier.Operation.ADDITION));
                map.put("bracelet", new AttributeModifier(UUID_BRACELET, "crown_of_glory", 0, AttributeModifier.Operation.ADDITION));
                map.put("necklace", new AttributeModifier(UUID_NECKLACE, "crown_of_glory", 0, AttributeModifier.Operation.ADDITION));
                map.put("belt", new AttributeModifier(UUID_BELT, "crown_of_glory", 0, AttributeModifier.Operation.ADDITION));
                map.put("charm", new AttributeModifier(UUID_CHARM, "crown_of_glory", 0, AttributeModifier.Operation.ADDITION));
                inventory.addTransientSlotModifiers(map);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        final Player original = event.getOriginal();
        final Player player = event.getEntity();

        original.reviveCaps();

        final Optional<IWisdomCapability> originalWisdom = WisdomProvider.getCapability(original);
        final Optional<IWisdomCapability> cloneWisdom = WisdomProvider.getCapability(player);
        final Optional<IEnhancementCapability> originalEnhancement = EnhancementProvider.getCapability(original);
        final Optional<IEnhancementCapability> cloneEnhancement = EnhancementProvider.getCapability(player);

        if(originalWisdom.isPresent() && cloneWisdom.isPresent()) {
            cloneWisdom.get().copyFrom(originalWisdom.get());
        }
        if(originalEnhancement.isPresent() && cloneEnhancement.isPresent()) {
            cloneEnhancement.get().copyFrom(originalEnhancement.get());
        }

        original.invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        //Immortal Heart absorption
        if(event.player.level().getGameTime() % 600 == 0) {
            final LazyOptional<IPlayerProgression> lazyProg = event.player.getCapability(PlayerProgressionProvider.PROGRESSION);
            if(lazyProg.isPresent()) {
                final Optional<IPlayerProgression> optProg = lazyProg.resolve();
                if (optProg.isPresent()) {
                    final IPlayerProgression prog = optProg.get();
                    final IFaction faction = prog.getAlliedFaction();

                    if (faction != null && (faction.is(DEMONS) || event.player.level().getGameTime() % 1200 == 0)) {
                        final LazyOptional<IEnhancementCapability> capLazy = event.player.getCapability(EnhancementProvider.ENHANCEMENT);
                        if (capLazy.isPresent()) {
                            final Optional<IEnhancementCapability> capQuery = capLazy.resolve();
                            if (capQuery.isPresent()) {
                                final IEnhancementCapability cap = capQuery.get();
                                if(cap.hasHeart() && cap.getHeart() == IEnhancementCapability.EnhancedHeartType.IMMORTAL) {
                                    if(faction.is(COUNCIL)) event.player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 2, false, false, false));
                                    if(faction.is(FEY)) event.player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 2, false, false, false));
                                    if(faction.is(DEMONS)) event.player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 4, false, false, false));
                                    if(faction.is(UNDEAD)) event.player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1, false, false, false));
                                }
                            }
                        }
                    }
                }
            }
        }
        if(event.player.level().getGameTime() % 10 == 0) {
            if (event.player.hasEffect(EQUANIMITY.get())) {
                final MobEffectInstance effect = event.player.getEffect(EQUANIMITY.get());
                if (event.player.hasEffect(EffectInit.MANA_STUNT.get())) {
                    ItemStack mainStack = event.player.getItemInHand(InteractionHand.MAIN_HAND);
                    ItemStack offStack = event.player.getItemInHand(InteractionHand.OFF_HAND);
                    ISpellDefinition spell = null;
                    if (mainStack.getItem() instanceof ItemSpell isp) {
                        spell = isp.getSpell(offStack, event.player);
                    } else if (offStack.getItem() instanceof ItemSpell isp) {
                        spell = isp.getSpell(offStack, event.player);
                    }
                    if (spell != null) {
                        float cost = spell.getManaCost() * 10;
                        float heal = Math.max(1.0f, cost * EQUANIMITY_HEAL_PER_MANA[Math.min(effect.getAmplifier(), EQUANIMITY_HEAL_PER_MANA.length)]);
                        event.player.heal(heal);
                    }
                }
            }
        }
    }
}
