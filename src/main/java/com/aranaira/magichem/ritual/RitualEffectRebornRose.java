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
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
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
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity.TRAIL_PARTICLE_COLORS;

public class RitualEffectRebornRose extends RitualEffect {

    public static final int RITUAL_LIFESPAN = 20;
    private static final Random r = new Random();

    public RitualEffectRebornRose(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Nullable
    @Override
    public Component canRitualStart(IRitualContext context) {
        if(context.getRecipe().getResultItem().getItem() == ItemInit.RUNE_PATTERN_RITUAL_METAL.get())
            return null;

        return null;
    }

    @Override
    protected boolean matchReagents(IRitualContext context) {
        final List<ItemStack> collectedReagents = context.getCollectedReagents();

        boolean hasAbjuration = false, hasLiquidLight = false, hasMilk = false, hasRose = false;

        for(ItemStack is : collectedReagents) {
            if(is.getItem() == ItemRegistry.DENDRITIC_ABJURATION.get()) hasAbjuration = true;
            else if(is.getItem() == ItemRegistry.LIQUID_LIGHT_BUCKET.get()) hasLiquidLight = true;
            else if(is.getItem() == Items.MILK_BUCKET) hasMilk = true;
            else if(is.getItem() == Blocks.WITHER_ROSE.asItem()) hasRose = true;
        }

        if(hasAbjuration && hasLiquidLight && hasMilk && hasRose) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        for(ItemStack is : context.getCollectedReagents()) {
            if(is.getItem() == ItemRegistry.DENDRITIC_ABJURATION.get()) {
                int uses = 1;
                boolean doAbjurationDrop = true;
                CompoundTag nbt = null;

                if(is.hasTag()) {
                    nbt = is.getTag();
                    if(is.getTag().contains("uses")) {
                        uses = nbt.getInt("uses") + 1;
                        if(uses >= 3) {
                            doAbjurationDrop = false;
                            context.getCaster().sendSystemMessage(Component.translatable("feedback.ritual.reborn_rose.abjuration"));
                        }
                    }
                }

                if(doAbjurationDrop) {
                    if(nbt == null) {
                        nbt = new CompoundTag();
                    }
                    nbt.putInt("uses",uses);

                    ItemStack abjuration = new ItemStack(ItemRegistry.DENDRITIC_ABJURATION.get());
                    abjuration.setTag(nbt);
                    ItemEntity ie = new ItemEntity(context.getLevel(), context.getCenter().getX(), context.getCenter().getY() + 0.5, context.getCenter().getZ(), abjuration);
                    context.getLevel().addFreshEntity(ie);
                }

                break;
            }
        }

        ItemStack rose = new ItemStack(BlockRegistry.RADIANT_ROSE.get());
        ItemEntity ie = new ItemEntity(context.getLevel(), context.getCenter().getX(), context.getCenter().getY() + 0.5, context.getCenter().getZ(), rose);
        context.getLevel().addFreshEntity(ie);

        return true;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        Vec3 center = new Vec3((double)context.getCenter().getX() + 0.5D, (double)context.getCenter().getY() + 0.1D, (double)context.getCenter().getZ() + 0.5D);
        double radius = (double)context.getRecipe().getLowerBound();

        for(float i = 0.0F; i < 360.0F; i += 15.0F) {
            double angleR = Math.toRadians((double)i);
            double offsetX = Math.cos(angleR) * radius;
            double offsetZ = Math.sin(angleR) * radius;
            Vec3 start = center.add(offsetX, 0.0D, offsetZ);
            context.getLevel().addParticle(new MAParticleType(
                    ParticleInit.ARCANE_LERP.get())
                            .setColor(34, 113, 24, 32).setScale(0.5f),
                    start.x, start.y, start.z,
                    center.x, center.y + 0.25, center.z);
        }

        int colorIndex = (int)(context.getLevel().getGameTime() % 7);
        context.getLevel().addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                        .setPhysics(false).setScale(0.125f).setMaxAge(40)
                        .setColor(TRAIL_PARTICLE_COLORS[colorIndex][0], TRAIL_PARTICLE_COLORS[colorIndex][1], TRAIL_PARTICLE_COLORS[colorIndex][2]),
                center.x, center.y, center.z,
                r.nextDouble(0.125) + 0.025, 0.08, 0.5625);

        if(context.getLevel().getGameTime() % 3 == 0) {
            context.getLevel().addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                            .setPhysics(false).setScale(0.0625f).setMaxAge(60)
                            .setColor(TRAIL_PARTICLE_COLORS[colorIndex][0], TRAIL_PARTICLE_COLORS[colorIndex][1], TRAIL_PARTICLE_COLORS[colorIndex][2]),
                    center.x, center.y, center.z,
                    -r.nextDouble(0.175) + 0.05, 0.04, 3.25);
        }

        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext iRitualContext) {
        return RITUAL_LIFESPAN;
    }
}
