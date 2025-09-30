package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.events.compat.OccultismEventHelper;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.item.*;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.KeybindRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
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
                        double adjusted = Math.PI - ((rotation - 90d) * 0.01745329238474369d - getAngle(pLevel, entity, pStack));

                        if(hasEntity) {
                            adjusted = wobble(pLevel, adjusted);
                        }

                        final float f = (float)(adjusted / (Math.PI * 2d));
                        return Mth.positiveModulo(f, 1.0f);
                    }
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
                        CompoundTag nbt = pStack.getOrCreateTag();
                        BlockPos pos;
                        if (nbt.contains("LodestonePos")) {
                            CompoundTag posTag = nbt.getCompound("LodestonePos");
                            pos = new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
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
