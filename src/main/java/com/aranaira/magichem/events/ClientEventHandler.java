package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.events.compat.BloodMagicEventHelper;
import com.aranaira.magichem.events.compat.CreateEventHelper;
import com.aranaira.magichem.events.compat.OccultismEventHelper;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.item.*;
import com.aranaira.magichem.recipe.ConstructStudyMaterialRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.KeybindRegistry;
import com.aranaira.magichem.util.CompassTargetHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof EssentiaDropletsItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.ESSENTIA_DROPLETS_ENDER.get());
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof EssentiaDropletsItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.ESSENTIA_DROPLETS_EARTH.get());
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof EssentiaDropletsItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.ESSENTIA_DROPLETS_WATER.get());
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof EssentiaDropletsItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.ESSENTIA_DROPLETS_AIR.get());
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof EssentiaDropletsItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.ESSENTIA_DROPLETS_FIRE.get());
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof EssentiaDropletsItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.ESSENTIA_DROPLETS_ARCANE.get());

        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof MateriaItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.getEssentia().toArray(new EssentiaItem[0]));
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof MateriaItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.getAdmixtures().toArray(new AdmixtureItem[0]));

        event.register( (stack, layer) -> (layer == 1 && stack.getItem() instanceof BucketItem mItem) ? IClientFluidTypeExtensions.of(mItem.getFluid()).getTintColor() : -1, ItemRegistry.LIQUEFACTED_COPPER_BUCKET.get());
        event.register( (stack, layer) -> (layer == 1 && stack.getItem() instanceof BucketItem mItem) ? IClientFluidTypeExtensions.of(mItem.getFluid()).getTintColor() : -1, ItemRegistry.LIQUEFACTED_IRON_BUCKET.get());
        event.register( (stack, layer) -> (layer == 1 && stack.getItem() instanceof BucketItem mItem) ? IClientFluidTypeExtensions.of(mItem.getFluid()).getTintColor() : -1, ItemRegistry.LIQUEFACTED_GOLD_BUCKET.get());
        event.register( (stack, layer) -> (layer == 1 && stack.getItem() instanceof BucketItem mItem) ? IClientFluidTypeExtensions.of(mItem.getFluid()).getTintColor() : -1, ItemRegistry.LIQUEFACTED_DEBRIS_BUCKET.get());

        //Compat items below this point
        ModList modList = ModList.get();

        //Blood Magic
        if(modList.isLoaded("bloodmagic")) {
            BloodMagicEventHelper.registerItemLayers(event);
        }

        //Create
        if(modList.isLoaded("create")) {
            CreateEventHelper.registerItemLayers(event);
        }

        //Occultism
        if(modList.isLoaded("occultism")) {
            OccultismEventHelper.registerItemLayers(event);
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Code to make the compass point correctly largely copied from the implementation in Explorer's Compass.
        // https://github.com/MattCzyr/ExplorersCompass/blob/f2adedb71a8df2e3c4c56a7a2d56890a7932731e/src/main/java/com/chaosthedude/explorerscompass/client/ExplorersCompassClient.java#L90
        event.enqueueWork(() -> {
            ItemProperties.register(ItemRegistry.TRAVELLERS_COMPASS.get(), new ResourceLocation("angle"), new ClampedItemPropertyFunction() {
                @OnlyIn(Dist.CLIENT)
                private double rotation;
                @OnlyIn(Dist.CLIENT)
                private double rotAdjust;
                @OnlyIn(Dist.CLIENT)
                private long lastUpdateTick;

                @OnlyIn(Dist.CLIENT)
                @Override
                public float unclampedCall(ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
                    if(pEntity == null && !pStack.isFramed()) {
                        return 0;
                    } else {
                        final boolean hasEntity = pEntity != null;
                        final Entity entity = (Entity)(hasEntity ? pEntity : pStack.getFrame());
                        if(pLevel == null && pEntity.level() instanceof ClientLevel cl) {
                            pLevel = cl;
                        }

                        double rotation = hasEntity ? (double) pEntity.getYRot() : getFrameRotation((ItemFrame)entity);
                        rotation = rotation % 360d;
                        double facing = Math.PI - (rotation - 90d) * 0.01745329238474369d;

                        if(pEntity instanceof Player player && player.isLocalPlayer()) {
                            facing = wobble(pLevel, facing);
                        }

                        if(CompassTargetHelper.getPoint(pStack) != null && !CompassTargetHelper.inDimension(pStack, pLevel)) {
                            return spin(pLevel, pSeed);
                        }

                        double adjusted = facing + getAngle(pLevel, entity, pStack);

                        final float f = (float)(adjusted / (Math.PI * 2d));
                        return Mth.positiveModulo(f, 1.0f);
                    }
                }

                @OnlyIn(Dist.CLIENT)
                private double spinRotation;
                @OnlyIn(Dist.CLIENT)
                private double spinAdjust;
                @OnlyIn(Dist.CLIENT)
                private long spinLastTick;

                // Vanilla's compass in the wrong dimension (CompassItemPropertyFunction.getRandomlySpinningRotation):
                // each tick the needle drifts toward a new random direction; the seed shifts each slot's phase.
                @OnlyIn(Dist.CLIENT)
                private float spin(ClientLevel pLevel, int pSeed) {
                    if (pLevel.getGameTime() != spinLastTick) {
                        spinLastTick = pLevel.getGameTime();
                        double d0 = Math.random() - spinRotation;
                        d0 = Mth.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
                        spinAdjust += d0 * 0.1D;
                        spinAdjust *= 0.8D;
                        spinRotation = Mth.positiveModulo(spinRotation + spinAdjust, 1.0D);
                    }
                    double phase = (float) (pSeed * 1327217883) / (float) Integer.MAX_VALUE;
                    return Mth.positiveModulo((float) (spinRotation + phase), 1.0f);
                }

                @OnlyIn(Dist.CLIENT)
                private double getFrameRotation(ItemFrame pFrame) {
                    Direction direction = pFrame.getDirection();
                    int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
                    return Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + pFrame.getRotation() * 45 + i);
                }

                @OnlyIn(Dist.CLIENT)
                private double wobble(ClientLevel pLevel, double pAmount) {
                    if (pLevel.getGameTime() != lastUpdateTick) {
                        lastUpdateTick = pLevel.getGameTime();
                        double d0 = pAmount - rotation;
                        d0 = Mth.positiveModulo(d0 + Math.PI, Math.PI * 2D) - Math.PI;
                        d0 = Mth.clamp(d0, -1.0D, 1.0D);
                        rotAdjust += d0 * 0.1D;
                        rotAdjust *= 0.8D;
                        rotation += rotAdjust;
                    }

                    return rotation;
                }

                @OnlyIn(Dist.CLIENT)
                private double getAngle(ClientLevel pWorld, Entity pEntity, ItemStack pStack) {
                    if (pStack.getItem() == ItemRegistry.TRAVELLERS_COMPASS.get()) {
                        pStack.getOrCreateTag(); // kept: gives a tagless compass an empty tag; the HUD's hasTag() test relies on it
                        CompassTargetHelper.CompassPoint point = CompassTargetHelper.getPoint(pStack);
                        BlockPos pos;
                        if (point != null) {
                            pos = new BlockPos(point.x(), point.y(), point.z());
                        } else {
                            pos = pWorld.getSharedSpawnPos();
                        }
                        return Math.atan2((double) pos.getZ() - pEntity.position().z(), (double) pos.getX() - pEntity.position().x());
                    }
                    return 0.0D;
                }
            });
        });
    }
}
