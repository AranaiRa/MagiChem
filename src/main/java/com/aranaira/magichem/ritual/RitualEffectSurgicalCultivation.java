package com.aranaira.magichem.ritual;

import com.aranaira.magichem.capabilities.enhancement.EnhancementProvider;
import com.aranaira.magichem.capabilities.enhancement.IEnhancementCapability;
import com.aranaira.magichem.data.MagiChemDamageTypes;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.blocks.BlockInit;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity.TRAIL_PARTICLE_COLORS;

public class RitualEffectSurgicalCultivation extends RitualEffect {

    public static final int RITUAL_LIFESPAN = 1;
    private static final Random r = new Random();
    private static final double GIBLET_SPREAD = 0.5f;
    private static final double GIBLET_BURST = 0.85f;

    public RitualEffectSurgicalCultivation(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        final Player player = context.getCaster();

        player.getCapability(EnhancementProvider.ENHANCEMENT).ifPresent(cap -> {
            player.hurt(MagiChemDamageTypes.source(context.getLevel().registryAccess(), MagiChemDamageTypes.SURGERY), 60);

            if(!player.isDeadOrDying()) {
                cap.setHeart(IEnhancementCapability.EnhancedHeartType.IMMORTAL);
                player.sendSystemMessage(Component.translatable("feedback.ritual.surgical_cultivation.success"));

                final Vec3 routeBase = player.getForward().scale(3);
                Vec3 route = routeBase.add(r.nextDouble(GIBLET_SPREAD)-(GIBLET_SPREAD*0.5), r.nextDouble(0.05), r.nextDouble(GIBLET_SPREAD)-(GIBLET_SPREAD*0.5)).scale(0.375);
                ItemStack is = new ItemStack(ItemRegistry.GIBLETS.get(), 6+r.nextInt(16));
                ItemEntity ie = new ItemEntity(context.getLevel(), player.getX(), player.getY()+1, player.getZ(),
                        is, route.x(), route.y(), route.z());
                ie.setPickUpDelay(60);
                context.getLevel().addFreshEntity(ie);
            } else {
                player.hurt(MagiChemDamageTypes.source(context.getLevel().registryAccess(), MagiChemDamageTypes.SURGERY), 30);
                final Vec3 routeBase = player.getForward().scale(3);
                int total = 14+r.nextInt(6);
                for(int i=0; i<total; i++) {
                    Vec3 route = new Vec3(Math.cos((Math.PI * 2) * ((double)i / (double)total)), r.nextDouble(0.05), Math.sin((Math.PI * 2) * ((double)i / (double)total))).scale(0.5);
                    ItemStack is = new ItemStack(ItemRegistry.GIBLETS.get(), 40+r.nextInt(24));
                    ItemEntity ie = new ItemEntity(context.getLevel(), player.getX() + routeBase.x(), player.getY()+1 + routeBase.y(), player.getZ() + routeBase.z(),
                            is, route.x(), route.y(), route.z());
                    context.getLevel().addFreshEntity(ie);
                }
                ItemStack is = new ItemStack(ItemRegistry.IMMORTAL_HEART.get());
                ItemEntity ie = new ItemEntity(context.getLevel(), player.getX() + routeBase.x(), player.getY()+1 + routeBase.y(), player.getZ() + routeBase.z(),
                        is, 0, 0, 0);
                context.getLevel().addFreshEntity(ie);
            }
        });

        return true;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        Direction dir = calculateDirection(context);

        Vec3 center = new Vec3((double)context.getCenter().getX() + 0.5D + (dir == Direction.EAST ? -2 : (dir == Direction.WEST ? 2 : 0)), (double)context.getCenter().getY() + 0.1D, (double)context.getCenter().getZ() + 0.5D + (dir == Direction.SOUTH ? -2 : (dir == Direction.NORTH ? 2 : 0)));
        double radius = (double)context.getRecipe().getLowerBound();

        for(float i = 0.0F; i < 360.0F; i += 15.0F) {
            double angleR = Math.toRadians((double)i);
            double offsetX = Math.cos(angleR) * radius;
            double offsetZ = Math.sin(angleR) * radius;
            Vec3 start = center.add(offsetX, 0.0D, offsetZ);
            context.getLevel().addParticle(new MAParticleType(
                    ParticleInit.ARCANE_LERP.get())
                            .setColor(46 + r.nextInt(40), 15, 11, 48).setScale(0.5f),
                    start.x, start.y, start.z,
                    center.x, center.y + 0.25, center.z);
        }

        if(context.getLevel().getGameTime() % 7 == 0){
            int hueShift = r.nextInt(128);
            context.getLevel().addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                            .setPhysics(false).setScale(0.125f).setMaxAge(40+r.nextInt(20))
                            .setColor(255, 127 + hueShift, 127 + (hueShift / 2)),
                    center.x, center.y - 0.75f, center.z,
                    r.nextDouble(0.125) + 0.025, 0.08, 0.375);
        }

        if(context.getLevel().getGameTime() % 8 == 0){
            double theta = r.nextDouble()*Math.PI*2;
            double dist = 1.25 + r.nextDouble(2.75);
            Vec3 boltStart = new Vec3(center.x, center.y, center.z).add(new Vec3(Math.cos(theta), 0, Math.sin(theta)).scale(dist));
            int hueShift = r.nextInt(64);
            context.getLevel().addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                            .setPhysics(false).setScale(0.125f).setMaxAge(40+r.nextInt(30))
                            .setColor(255, 64 + hueShift, 64 + (hueShift / 2), 32),
                    boltStart.x, boltStart.y - 0.75f, boltStart.z,
                    boltStart.x, boltStart.y + 5 + r.nextInt(5), boltStart.z);
        }

        {
            double theta = r.nextDouble()*Math.PI*2;
            double dist = 1.25 + r.nextDouble(2.75);
            Vec3 boltStart = new Vec3(center.x, center.y, center.z).add(new Vec3(Math.cos(theta), 0, Math.sin(theta)).scale(dist));
            int hueShift = r.nextInt(64);
            context.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                            .setPhysics(false).setScale(0.125f).setMaxAge(40+r.nextInt(70))
                            .setColor(255, 64 + hueShift, 64 + (hueShift / 2)),
                    boltStart.x, boltStart.y - 0.75f, boltStart.z,
                    boltStart.x, boltStart.y + 1 + r.nextDouble(), boltStart.z);
        }


        return true;
    }

    private Direction calculateDirection(IRitualContext context) {
        final Level level = context.getLevel();
        final BlockPos center = context.getCenter();

        //North
        BlockState queryL = level.getBlockState(center.offset(-3, 0, -1));
        BlockState queryR = level.getBlockState(center.offset(3, 0, -1));
        if(queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
            return Direction.NORTH;
        }

        //South
        queryL = level.getBlockState(center.offset(-3, 0, 1));
        queryR = level.getBlockState(center.offset(3, 0, 1));
        if(queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
            return Direction.SOUTH;
        }

        //East
        queryL = level.getBlockState(center.offset(1, 0, -3));
        queryR = level.getBlockState(center.offset(1, 0, 3));
        if(queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
            return Direction.EAST;
        }

        //East
        queryL = level.getBlockState(center.offset(-1, 0, -3));
        queryR = level.getBlockState(center.offset(-1, 0, 3));
        if(queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
            return Direction.WEST;
        }

        return null;
    }

    @Override
    protected int getApplicationTicks(IRitualContext iRitualContext) {
        return RITUAL_LIFESPAN;
    }
}
