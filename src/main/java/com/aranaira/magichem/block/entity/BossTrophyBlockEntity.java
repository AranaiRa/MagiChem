package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.tools.render.ModelUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class BossTrophyBlockEntity extends BlockEntity {
    public static final Random r = new Random();
    private static final int[][] COUNCIL_PARTICLE_COLORS = {
            {72, 80, 219},
            {89, 72, 219},
            {72, 104, 219},
            {113, 72, 219}
    };
    private static final int[][] DEMON_PARTICLE_COLORS = {
            {53, 128, 57},
            {84, 152, 54},
            {130, 189, 56}
    };
    private static final int[][] FEY_DAY_PARTICLE_COLORS = {
            {47, 141, 44},
            {47, 141, 44},
            {18, 95, 15},
            {18, 95, 15},
            {235, 94, 167}
    };
    private static final int[] FEY_PARTICLE_DIVISOR = {3, 5, 8, 20, 8, 5, 3};

    public BossTrophyBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.BOSS_TROPHY_BE.get(), pPos, pBlockState);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof BossTrophyBlockEntity entity) {
            if(blockState.getBlock() == BlockRegistry.BOSS_TROPHY_COUNCIL.get()) {
                if(level.getGameTime() % 5 == 0) {
                    final Vec3 center = /*new Vec3(0.5f, 0.5f, 0.5f);*/entity.getBlockPos().getCenter();
                    int colorIndex = r.nextInt(4);
                    level.addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                                    .setPhysics(false).setScale(0.015625f).setMaxAge(60)
                                    .setColor(COUNCIL_PARTICLE_COLORS[colorIndex][0], COUNCIL_PARTICLE_COLORS[colorIndex][1], COUNCIL_PARTICLE_COLORS[colorIndex][2], 128),
                            center.x, center.y, center.z,
                            r.nextDouble(0.1) - 0.05, 0.0075, 0.0015);
                    level.addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                                    .setPhysics(false).setScale(0.015625f).setMaxAge(60)
                                    .setColor(COUNCIL_PARTICLE_COLORS[colorIndex][0], COUNCIL_PARTICLE_COLORS[colorIndex][1], COUNCIL_PARTICLE_COLORS[colorIndex][2], 128),
                            center.x, center.y, center.z,
                            r.nextDouble(0.1) - 0.05, -0.0075, 0.0015);
                }
            }
            else if(blockState.getBlock() == BlockRegistry.BOSS_TROPHY_DEMONS.get()) {
                if(level.getGameTime() % 2 == 0) {
                    double x = 0, y = 0.1705 + r.nextDouble(0.7578), z = 0;
                    final Direction dir = blockState.getValue(MagiChemBlockStateProperties.FACING);
                    if(dir == Direction.NORTH) {
                        x = 0.1875 + r.nextDouble(0.625);
                        z = 0.3724 + r.nextDouble(0.4375);
                    }
                    else if(dir == Direction.EAST) {
                        x = 0.3724 + r.nextDouble(0.4375);
                        z = 0.1875 + r.nextDouble(0.625);
                    }
                    else if(dir == Direction.SOUTH) {
                        x = 1 -(0.1875 + r.nextDouble(0.625));
                        z = 1 -(0.3724 + r.nextDouble(0.4375));
                    }
                    else if(dir == Direction.WEST) {
                        x = 1 - (0.3724 + r.nextDouble(0.4375));
                        z = 1 - (0.1875 + r.nextDouble(0.625));
                    }

                    final Vec3 center = entity.getBlockPos().getCenter().add(x-0.5, y-0.5, z-0.5);
                    int colorIndex = r.nextInt(3);
                    level.addParticle(new MAParticleType(ParticleInit.FLAME.get())
                                    .setPhysics(false).setGravity(0).setScale(0.0625f + r.nextFloat(0.0625f)).setMaxAge(60)
                                    .setColor(DEMON_PARTICLE_COLORS[colorIndex][0], DEMON_PARTICLE_COLORS[colorIndex][1], DEMON_PARTICLE_COLORS[colorIndex][2], 32),
                            center.x, center.y, center.z,
                            0, 0.005 + r.nextDouble(0.005), 0);
                }
            }
            else if(blockState.getBlock() == BlockRegistry.BOSS_TROPHY_FEY.get()) {
                int phase = 0;
                {
                    long dayTime = level.getDayTime() % 24000;
                    boolean isNight = dayTime >= 12800 && dayTime < 23200;

                    if ((dayTime >= 0 && dayTime < 800) || (dayTime >= 11200 && dayTime < 12000))
                        phase = 1;
                    else if ((dayTime >= 800 && dayTime < 1600) || (dayTime >= 10400 && dayTime < 11200))
                        phase = 2;
                    else if ((dayTime >= 1600 && dayTime < 10400))
                        phase = 3;
                    else if ((dayTime >= 13600 && dayTime < 14400) || (dayTime >= 21600 && dayTime < 22400))
                        phase = -1;
                    else if ((dayTime >= 14400 && dayTime < 15200) || (dayTime >= 20800 && dayTime < 21600))
                        phase = -2;
                    else if ((dayTime >= 15200 && dayTime < 20800))
                        phase = -3;
                }
                int divisor = FEY_PARTICLE_DIVISOR[phase + 3];

                if(phase != 0 && level.getGameTime() % divisor == 0) {
                    double theta = Math.PI * 2 * r.nextDouble();
                    double radius = 0.4375 + r.nextDouble(0.125);
                    final Vec3 center = entity.getBlockPos().getCenter().add(Math.cos(theta) * radius, -0.125, Math.sin(theta) * radius);
                    if(phase > 0) {
                        int colorIndex = r.nextInt(5);
                        level.addParticle(new MAParticleType(ParticleInit.EARTH.get())
                                        .setPhysics(false).setScale(0.015625f + r.nextFloat(0.0625f)).setMaxAge(60 + r.nextInt(60))
                                        .setColor(FEY_DAY_PARTICLE_COLORS[colorIndex][0], FEY_DAY_PARTICLE_COLORS[colorIndex][1], FEY_DAY_PARTICLE_COLORS[colorIndex][2], 128),
                                center.x, center.y, center.z,
                                r.nextDouble(0.005) - 0.0025, 0.0025 + r.nextDouble(0.005), r.nextDouble(0.005) - 0.0025);
                    } else {
                        level.addParticle(new MAParticleType(ParticleInit.DUST.get())
                                        .setPhysics(false).setGravity(0).setScale(0.0625f + r.nextFloat(0.0625f)).setMaxAge(60 + r.nextInt(60))
                                        .setColor(197 + r.nextInt(41), 239, 238, 64),
                                center.x, center.y, center.z,
                                r.nextDouble(0.005) - 0.0025, 0.0005 + r.nextDouble(0.001), r.nextDouble(0.005) - 0.0025);
                    }
                }
            }
            else if(blockState.getBlock() == BlockRegistry.BOSS_TROPHY_UNDEAD.get()) {
                if(level.getGameTime() % 6 == 0){
                    final Direction dir = blockState.getValue(MagiChemBlockStateProperties.FACING);
                    final Vec3 center = entity.getBlockPos().getCenter();
                    level.addParticle(new MAParticleType(ParticleInit.DUST.get())
                                    .setPhysics(false).setGravity(0).setColor(40, 25, 60, 128)
                                    .setScale(0.10f).setMaxAge(60),
                            center.x + (dir == Direction.WEST ? 0.0625 : (dir == Direction.EAST ? -0.0625 : 0)), center.y - 0.25, center.z + (dir == Direction.NORTH ? 0.0625 : (dir == Direction.SOUTH ? -0.0625 : 0)),
                            0, 0.02f, 0);
                }
            }
        }
    }
}
