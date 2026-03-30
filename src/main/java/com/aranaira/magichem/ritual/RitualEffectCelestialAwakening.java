package com.aranaira.magichem.ritual;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.blocks.BlockInit;
import com.mna.entities.utility.PresentItem;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.rituals.effects.RitualEffectCreateEssence;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.WorldRenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class RitualEffectCelestialAwakening extends RitualEffect {

    public static final int RITUAL_LIFESPAN = 40;
    private static final Random r = new Random();
    private final LuminType phase;

    public RitualEffectCelestialAwakening(ResourceLocation pRitualName, LuminType pPhase) {
        super(pRitualName);
        phase = pPhase;
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        Direction dir = calculateDirection(context);
        BlockPos center = context.getCenter();
        BlockPos beamCenter = center;
        if (dir == Direction.NORTH)
            beamCenter = phase == LuminType.SOLAR ? center.offset(2, 0, -2) : center.offset(-1, 0, 1);
        else if (dir == Direction.EAST)
            beamCenter = phase == LuminType.SOLAR ? center.offset(2, 0, 2) : center.offset(-1, 0, -1);
        else if (dir == Direction.SOUTH)
            beamCenter = phase == LuminType.SOLAR ? center.offset(-2, 0, 2) : center.offset(1, 0, -1);
        else if (dir == Direction.WEST)
            beamCenter = phase == LuminType.SOLAR ? center.offset(-2, 0, -2) : center.offset(1, 0, 1);

        //Queue up the item craft
        Vec3 itemPos = new Vec3(beamCenter.getX()+0.5, beamCenter.getY()+1, beamCenter.getZ()+0.5);
        DelayedEventQueue.pushEvent(context.getLevel(),
                new TimedDelayedEvent<DelayDataOutputItem>("sublimation_ritual_output", RITUAL_LIFESPAN + 80,
                        new DelayDataOutputItem(context.getLevel(), itemPos, phase == LuminType.SOLAR ? new ItemStack(ItemRegistry.ORICHALKOS.get()) : new ItemStack(ItemRegistry.SELARGYROS.get())),
                        this::createOutputItem
                )
        );

        return false;
    }

    private void createOutputItem(String s, DelayDataOutputItem data) {
        ItemEntity ie = new ItemEntity(EntityType.ITEM, data.level);
        ie.setItem(data.itemToCreate);
        ie.setPos(data.creationPos);
        ie.setDeltaMovement(0, 0.375, 0);
        data.level.addFreshEntity(ie);

        int[] color = phase == LuminType.SOLAR ? new int[]{255, 196, 128} : new int[]{128, 128, 255};
        for(int i=0;i<40;i++) {

            if(data.level.getGameTime() % 2 == 0){
                data.level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setPhysics(true).setScale(0.125f).setMaxAge(20+r.nextInt(60))
                                .setColor(color[0], color[1], color[2],255),
                        data.creationPos.x, data.creationPos.y, data.creationPos.z,
                        r.nextDouble()-0.5,r.nextDouble()-0.5,r.nextDouble()-0.5);
            }
        }
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        Direction dir = calculateDirection(context);
        BlockPos center = context.getCenter();
        BlockPos beamCenter = center;
        if (dir == Direction.NORTH)
            beamCenter = phase == LuminType.SOLAR ? center.offset(2, 0, -2) : center.offset(-1, 0, 1);
        else if (dir == Direction.EAST)
            beamCenter = phase == LuminType.SOLAR ? center.offset(2, 0, 2) : center.offset(-1, 0, -1);
        else if (dir == Direction.SOUTH)
            beamCenter = phase == LuminType.SOLAR ? center.offset(-2, 0, 2) : center.offset(1, 0, -1);
        else if (dir == Direction.WEST)
            beamCenter = phase == LuminType.SOLAR ? center.offset(-2, 0, -2) : center.offset(1, 0, 1);

        if(context.getLevel().getGameTime() % 4 == 0){
            int dirID = (int)(context.getLevel().getGameTime() % 16) / 4;
            int[] color = phase == LuminType.SOLAR ? new int[]{255, 196, 128} : new int[]{128, 128, 255};
            context.getLevel().addParticle(new MAParticleType(ParticleInit.TRAIL_VELOCITY.get())
                            .setPhysics(false).setScale(0.5f).setMaxAge(40)
                            .setColor(color[0], color[1], color[2],128),
                    beamCenter.getX(), beamCenter.getY() + 20f + r.nextDouble(), beamCenter.getZ() + 0.5,
                    dirID == 0 ? 0.01 : (dirID == 1 ? -0.01 : 0), -0.75f, dirID == 2 ? 0.01 : (dirID == 3 ? -0.01 : 0));

            context.getLevel().addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                            .setPhysics(false).setScale(0.0625f).setMaxAge(20+r.nextInt(20))
                            .setColor(color[0], color[1], color[2],128),
                    beamCenter.getX() + 0.5, beamCenter.getY() + 0.5f, beamCenter.getZ() + 0.5,
                    0.125 + r.nextDouble(0.125), 0.03125 + r.nextDouble(0.03125), 2);
        }
        if(context.getLevel().getGameTime() % 2 == 0){
            context.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                            .setPhysics(false).setScale(0.5f).setMaxAge(40+r.nextInt(120))
                            .setColor(255, 255, 255,255)
                            .setMover(new ParticleOrbitMover(beamCenter.getX() + 0.5, beamCenter.getY() - 0.75f, beamCenter.getZ() + 0.5, -r.nextDouble(0.5), 0.125 + r.nextDouble(0.0625), .5)),
                    beamCenter.getX() + 0.5, beamCenter.getY() - 0.25f, beamCenter.getZ() + 0.5,
                    0,0,0);
        }

        //stars
        {
            double theta = r.nextDouble()*Math.PI*2;
            double dist = 1.25 + r.nextDouble(6.75);
            Vec3 sparkleStart = new Vec3(center.getX() + 0.5, center.getY() + 0.5 + r.nextDouble(5), center.getZ() + 0.5).add(new Vec3(Math.cos(theta), 0, Math.sin(theta)).scale(dist));
            context.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                            .setPhysics(false).setScale(0.125f).setMaxAge(20+r.nextInt(40))
                            .setColor(255, 255, 255),
                    sparkleStart.x, sparkleStart.y, sparkleStart.z,
                    sparkleStart.x, sparkleStart.y + (r.nextDouble(0.5) - 0.25), sparkleStart.z);
        }

        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext iRitualContext) {
        return RITUAL_LIFESPAN;
    }

    @Nullable
    @Override
    public Component canRitualStart(IRitualContext context) {
        return phase == LuminType.SOLAR ?
                (!context.getLevel().isDay() ? Component.translatable("feedback.ritual.celestial_awakening.solar") : null) :
                (context.getLevel().isDay() ? Component.translatable("feedback.ritual.celestial_awakening.lunar") : null);
    }

    private Direction calculateDirection(IRitualContext context) {
        final Level level = context.getLevel();
        final BlockPos center = context.getCenter();

        if(phase == LuminType.SOLAR){
            //North
            BlockState queryL = level.getBlockState(center.offset(0, 0, -1));
            BlockState queryR = level.getBlockState(center.offset(1, 0, 0));
            if (queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
                return Direction.NORTH;
            }

            //South
            queryL = level.getBlockState(center.offset(-1, 0, 0));
            queryR = level.getBlockState(center.offset(0, 0, 1));
            if (queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
                return Direction.SOUTH;
            }

            //East
            queryL = level.getBlockState(center.offset(1, 0, 0));
            queryR = level.getBlockState(center.offset(0, 0, 1));
            if (queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
                return Direction.EAST;
            }

            //West
            queryL = level.getBlockState(center.offset(-1, 0, 0));
            queryR = level.getBlockState(center.offset(0, 0, -1));
            if (queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
                return Direction.WEST;
            }
        }
        else {
            //North
            BlockState queryL = level.getBlockState(center.offset(0, 0, 1));
            BlockState queryR = level.getBlockState(center.offset(-1, 0, 0));
            if (queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
                return Direction.NORTH;
            }

            //South
            queryL = level.getBlockState(center.offset(1, 0, 0));
            queryR = level.getBlockState(center.offset(0, 0, -1));
            if (queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
                return Direction.SOUTH;
            }

            //East
            queryL = level.getBlockState(center.offset(-1, 0, 0));
            queryR = level.getBlockState(center.offset(0, 0, -1));
            if (queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
                return Direction.EAST;
            }

            //West
            queryL = level.getBlockState(center.offset(1, 0, 0));
            queryR = level.getBlockState(center.offset(0, 0, 1));
            if (queryL.getBlock() == BlockInit.CHALK_RUNE.get() && queryR.getBlock() == BlockInit.CHALK_RUNE.get()) {
                return Direction.WEST;
            }
        }

        return null;
    }

    private class DelayDataOutputItem {
        Level level;
        Vec3 creationPos;
        ItemStack itemToCreate;

        public DelayDataOutputItem(Level pLevel, Vec3 pCreationPos, ItemStack pItemToCreate) {
            this.level = pLevel;
            this.creationPos = pCreationPos;
            this.itemToCreate = pItemToCreate;
        }
    }
}
