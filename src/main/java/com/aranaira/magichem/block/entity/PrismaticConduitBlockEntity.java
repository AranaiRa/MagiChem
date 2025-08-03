package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.PrismaticConduitBlock;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.EldrinCapacitorTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.tileentities.EldrinConduitTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.aranaira.magichem.util.render.ColorUtils.SIX_STEP_PARTICLE_COLORS;

public class PrismaticConduitBlockEntity extends EldrinCapacitorTile {
    public static final List<Affinity> AFFINITY_LIST = new ArrayList<>();
    public static final float CRYSTAL_OFFSET_LESSER = 1.5F;
    public static final float CRYSTAL_OFFSET = 0.75F;
    private boolean isLesser;
    private static final Random r = new Random();

    public PrismaticConduitBlockEntity(BlockEntityType<?> type, float capacity, boolean isLesser, BlockPos pos, BlockState state) {
        super(type, pos, state, capacity, Affinity.ENDER, Affinity.EARTH, Affinity.WATER, Affinity.WIND, Affinity.FIRE, Affinity.ARCANE);
        this.isLesser = isLesser;

        if(AFFINITY_LIST.size() == 0) {
            AFFINITY_LIST.add(Affinity.ENDER);
            AFFINITY_LIST.add(Affinity.EARTH);
            AFFINITY_LIST.add(Affinity.WATER);
            AFFINITY_LIST.add(Affinity.WIND);
            AFFINITY_LIST.add(Affinity.FIRE);
            AFFINITY_LIST.add(Affinity.ARCANE);
        }
    }

    public PrismaticConduitBlockEntity(float capacity, boolean isLesser, BlockPos pos, BlockState state) {
        this((BlockEntityType) BlockEntitiesRegistry.PRISMATIC_CONDUIT_BE.get(), capacity, isLesser, pos, state);
    }

    public PrismaticConduitBlockEntity(BlockPos pos, BlockState blockState) {
        this(blockState.getBlock() != BlockRegistry.PRISMATIC_CONDUIT.get() ? 5.0F : 250.0F,
                blockState.getBlock() != BlockRegistry.PRISMATIC_CONDUIT.get(),
                pos,
                blockState);
    }

    @Override
    public float getRateLimit() {
        return this.isLesser ? 0.1F : 5.0F;
    }

    public float getChargeRate() {
        return this.isLesser ? 1.0F : 10.0F;
    }

    public float getDispersalRadius() {
        return this.isLesser ? 8.0F : 16.0F;
    }

    public boolean isLesser() {
        return this.isLesser;
    }

    private void spawnParticles() {
        final Level level = this.getLevel();
        if(level == null) return;

        Vector3 center = new Vector3(getBlockPos().getX() + 0.5, getBlockPos().getY() + getCrystalOffset(), getBlockPos().getZ() + 0.5);

        int colorIndex = r.nextInt(6);
        if (level.getGameTime() % 8 == 0) {
            level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                            .setColor(SIX_STEP_PARTICLE_COLORS[colorIndex][0], SIX_STEP_PARTICLE_COLORS[colorIndex][1], SIX_STEP_PARTICLE_COLORS[colorIndex][2])
                            .setScale(0.4f).setMaxAge(80),
                    center.x, center.y, center.z,
                    0, 0, 0);
        }
        level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                        .setColor(255, 255, 255).setScale(0.2f),
                center.x, center.y, center.z,
                0, 0, 0);

        for (int i = 0; i < 3; i++) {
            Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
            level.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                            .setColor(SIX_STEP_PARTICLE_COLORS[colorIndex][0], SIX_STEP_PARTICLE_COLORS[colorIndex][1], SIX_STEP_PARTICLE_COLORS[colorIndex][2], 255)
                            .setScale(0.09f).setMaxAge(16)
                            .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                    center.x + offset.x, center.y + offset.y, center.z + offset.z,
                    0, 0, 0);

            level.addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                            .setScale(0.015f).setMaxAge(16)
                            .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                    center.x + offset.x, center.y + offset.y, center.z + offset.z,
                    0, 0, 0);
        }
    }

    private float getCrystalOffset() {
        return this.isLesser() ? 1.5F : 0.75F;
    }

    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putBoolean("lesser", this.isLesser);
    }

    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("lesser")) {
            this.isLesser = pTag.getBoolean("lesser");
        }

    }

    @Override
    public List<Affinity> getAffinities() {
        return AFFINITY_LIST;
    }

    public static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pState, T t) {
        if(t instanceof PrismaticConduitBlockEntity pEntity) {
            if (pLevel.isClientSide()) {
                pEntity.spawnParticles();
                Level level = pEntity.getLevel();
                int index = ((int)(level.getGameTime() % 12) % 6);

                for(int i=0; i<2; i++) {
                    level.addParticle((new MAParticleType((ParticleType) ParticleInit.LIGHT_VELOCITY.get())).setColor(SIX_STEP_PARTICLE_COLORS[index][0], SIX_STEP_PARTICLE_COLORS[index][1], SIX_STEP_PARTICLE_COLORS[index][2], 384), (double) ((float) pEntity.getBlockPos().getX() + 0.4F) + Math.random() * 0.20000000298023224D, (double) ((float) pEntity.getBlockPos().getY() + 1.4F), (double) ((float) pEntity.getBlockPos().getZ() + 0.4F) + Math.random() * 0.20000000298023224D, 0.0D, 0.01D, 0.0D);
                }
            } else {
                EldrinCapacitorTile.Tick(pLevel, pPos, pState, pEntity);
            }
        }
    }
}
