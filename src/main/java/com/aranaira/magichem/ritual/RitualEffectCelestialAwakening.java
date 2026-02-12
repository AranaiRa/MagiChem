package com.aranaira.magichem.ritual;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageSingleTypeBlockEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.entities.SublimationRitualVFXEntity;
import com.aranaira.magichem.foundation.VesselData;
import com.aranaira.magichem.foundation.enums.ShlorpParticleMode;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.item.MateriaJarItem;
import com.aranaira.magichem.item.MateriaVesselItem;
import com.aranaira.magichem.recipe.SublimationRitualRecipe;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.items.ItemInit;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RitualEffectCelestialAwakening extends RitualEffect {

    private boolean isDaylightMode = true;

    public static final int RITUAL_LIFESPAN = 160;
    private static final Random r = new Random();

    public RitualEffectCelestialAwakening(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        if(isDaylightMode) {
            ItemEntity ie = new ItemEntity(
                    context.getLevel(), context.getCenter().getX(), context.getCenter().getY()+1, context.getCenter().getZ(),
                    new ItemStack(ItemRegistry.ORICHALKOS.get()),
                    0.0, 0.125, 0.0
                    );
            context.getLevel().addFreshEntity(ie);
        } else {
            ItemEntity ie = new ItemEntity(
                    context.getLevel(), context.getCenter().getX(), context.getCenter().getY()+1, context.getCenter().getZ(),
                    new ItemStack(ItemRegistry.SELARGYROS.get()),
                    0.0, 0.125, 0.0
            );
            context.getLevel().addFreshEntity(ie);
        }

        return true;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext iRitualContext) {
        return RITUAL_LIFESPAN;
    }

    @Override
    protected boolean modifyRitualReagentsAndPatterns(ItemStack dataStack, IRitualContext context) {
        isDaylightMode = context.getLevel().isDay();

        NonNullList<ResourceLocation> locations = NonNullList.withSize(30, new ResourceLocation("minecraft:dirt"));
        if(isDaylightMode) {
            locations.set(0, new ResourceLocation("magichem:sunburn"));
            locations.set(1, new ResourceLocation("magichem:sunburn"));
            locations.set(2, new ResourceLocation("magichem:salt_of_bone"));
            locations.set(3, new ResourceLocation("magichem:salt_of_bone"));
            locations.set(4, new ResourceLocation("magichem:salt_of_bone"));
            locations.set(5, new ResourceLocation("magichem:salt_of_bone"));
            locations.set(6, new ResourceLocation("minecraft:glowstone_dust"));
            locations.set(7, new ResourceLocation("minecraft:glowstone_dust"));
            locations.set(8, new ResourceLocation("minecraft:glowstone_dust"));
            locations.set(9, new ResourceLocation("minecraft:glowstone_dust"));
            locations.set(10, new ResourceLocation("magichem:stardust"));
            locations.set(11, new ResourceLocation("magichem:stardust"));
            locations.set(12, new ResourceLocation("magichem:dormant_orichalkos"));
            locations.set(13, new ResourceLocation("minecraft:yellow_candle"));
            locations.set(14, new ResourceLocation("minecraft:yellow_candle"));
            locations.set(15, new ResourceLocation("minecraft:yellow_candle"));
            locations.set(16, new ResourceLocation("minecraft:yellow_candle"));
            locations.set(17, new ResourceLocation("minecraft:yellow_candle"));
            locations.set(18, new ResourceLocation("minecraft:yellow_candle"));
            locations.set(19, new ResourceLocation("minecraft:yellow_candle"));
            locations.set(20, new ResourceLocation("minecraft:yellow_candle"));
            locations.set(21, new ResourceLocation("magichem:black_diamond"));
            locations.set(22, new ResourceLocation("minecraft:black_candle"));
            locations.set(23, new ResourceLocation("minecraft:black_candle"));
            locations.set(24, new ResourceLocation("minecraft:black_candle"));
            locations.set(25, new ResourceLocation("minecraft:black_candle"));
            locations.set(26, new ResourceLocation("minecraft:black_candle"));
            locations.set(27, new ResourceLocation("minecraft:black_candle"));
            locations.set(28, new ResourceLocation("minecraft:black_candle"));
            locations.set(29, new ResourceLocation("minecraft:black_candle"));
        } else {
            locations.set(2, new ResourceLocation("minecraft:glowstone_dust"));
            locations.set(3, new ResourceLocation("minecraft:glowstone_dust"));
            locations.set(4, new ResourceLocation("magichem:salt_of_soot"));
            locations.set(5, new ResourceLocation("magichem:salt_of_soot"));
            locations.set(6, new ResourceLocation("magichem:salt_of_soot"));
            locations.set(7, new ResourceLocation("magichem:salt_of_soot"));
            locations.set(8, new ResourceLocation("magichem:moonshine"));
            locations.set(9, new ResourceLocation("magichem:moonshine"));
            locations.set(10, new ResourceLocation("minecraft:glowstone_dust"));
            locations.set(11, new ResourceLocation("minecraft:glowstone_dust"));
            locations.set(12, new ResourceLocation("magichem:stardust"));
            locations.set(13, new ResourceLocation("magichem:stardust"));
            locations.set(23, new ResourceLocation("magichem:dormant_selargyros"));
            locations.set(24, new ResourceLocation("minecraft:light_gray_candle"));
            locations.set(25, new ResourceLocation("minecraft:light_gray_candle"));
            locations.set(26, new ResourceLocation("minecraft:light_gray_candle"));
            locations.set(27, new ResourceLocation("minecraft:light_gray_candle"));
            locations.set(28, new ResourceLocation("minecraft:light_gray_candle"));
            locations.set(29, new ResourceLocation("minecraft:light_gray_candle"));
            locations.set(30, new ResourceLocation("minecraft:light_gray_candle"));
            locations.set(31, new ResourceLocation("minecraft:light_gray_candle"));
        }

        context.replaceReagents(new ResourceLocation(MagiChemMod.MODID, "dynamic_infusion"), locations);

        return true;
    }
}
