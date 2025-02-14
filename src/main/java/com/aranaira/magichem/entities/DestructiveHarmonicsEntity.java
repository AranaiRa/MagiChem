package com.aranaira.magichem.entities;

import com.aranaira.magichem.MagiChemMod;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.util.*;

import static com.aranaira.magichem.util.render.ColorUtils.SIX_STEP_PARTICLE_COLORS;

public class DestructiveHarmonicsEntity extends Entity implements IEntityAdditionalSpawnData {
    public DestructiveHarmonicsEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private static final Random r = new Random();
    private BlockPos targetPos = null;
    private Phase phase = Phase.SETUP_AND_SCAN;
    private ArrayList<BlockPos> scannedHighPriorityBlocks = new ArrayList();
    private ArrayList<BlockPos> scannedLowPriorityBlocks = new ArrayList();
    private int phaseTimer = 0;

    public static final TagKey<Block> TAG_HIGH_PRIORITY = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "harmoniscope_high_priority"));
    public static final TagKey<Block> TAG_LOW_PRIORITY = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "harmoniscope_low_priority"));

    public void setTargetPos(BlockPos pTargetPos) {
        targetPos = pTargetPos;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if(pCompound.contains("targetPos"))
            targetPos = BlockPos.of(pCompound.getLong("targetPos"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        if(targetPos != null)
            pCompound.putLong("targetPos", targetPos.asLong());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeLong(targetPos != null ? targetPos.asLong() : 0);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        long pos = additionalData.readLong();
        targetPos = BlockPos.of(pos);
    }

    @Override
    public void tick() {
        super.tick();

        if(level().getGameTime() % 2 > 0) return;

        if(phase == Phase.SETUP_AND_SCAN) {
            if(!level().isClientSide())
                scanBlocksAroundTarget();
            phaseTimer = 22;
            phase = Phase.PARTICLES;
        }
        else if(phase == Phase.PARTICLES) {
            if(level().isClientSide())
            {
                Vector3 center = new Vector3(targetPos.getX()+0.5, targetPos.getY()+0.5, targetPos.getZ()+0.5);

                if(phaseTimer == 20) {
                    for (int i = 0; i < 70; i++) {
                        double x = (r.nextDouble() - 0.5) * 4;
                        double y = (r.nextDouble() - 0.5) * 4;
                        double z = (r.nextDouble() - 0.5) * 4;
                        Vector3 speed = new Vector3(x, y, z).normalize().scale(8.0f);

                        int c = r.nextInt(55);
                        level().addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(200+c, 200+c, 200+c, 255)
                                        .setScale(0.04f).setMaxAge(16),
                                center.x+speed.x, center.y+speed.y, center.z+speed.z,
                                center.x, center.y, center.z);
                    }
                }
                if(phaseTimer > 13) {
                    for (int i = 0; i < 10; i++) {
                        double x = (r.nextDouble() - 0.5) * 4;
                        double y = (r.nextDouble() - 0.5) * 4;
                        double z = (r.nextDouble() - 0.5) * 4;
                        Vector3 point = new Vector3(x, y, z).normalize().scale(2.0f).add(center);

                        int c = r.nextInt(155);
                        level().addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(100+c, 100+c*2, 255, 48)
                                        .setScale(1.6f).setMaxAge(132),
                                point.x, point.y, point.z,
                                point.x, point.y, point.z);
                    }
                }
            }

            phaseTimer--;
            if(phaseTimer <= 0)
                phase = Phase.SHATTER_HIGH_PRIORITY;
        }
        else if(phase == Phase.SHATTER_HIGH_PRIORITY) {
            if(scannedHighPriorityBlocks.size() > 0) {
                BlockPos posQuery = scannedHighPriorityBlocks.remove(r.nextInt(scannedHighPriorityBlocks.size()));
                BlockState stateQuery = level().getBlockState(posQuery);
                ItemEntity ie = new ItemEntity(level(), posQuery.getX(), posQuery.getY(), posQuery.getZ(), new ItemStack(stateQuery.getBlock().asItem()));
                level().destroyBlock(posQuery, false);
                level().addFreshEntity(ie);
            } else {
                phase = Phase.SHATTER_LOW_PRIORITY;
            }
        }
        else if(phase == Phase.SHATTER_LOW_PRIORITY) {
            if(scannedLowPriorityBlocks.size() > 0) {
                BlockPos posQuery = scannedLowPriorityBlocks.remove(r.nextInt(scannedLowPriorityBlocks.size()));
                BlockState stateQuery = level().getBlockState(posQuery);
                ItemEntity ie = new ItemEntity(level(), posQuery.getX(), posQuery.getY(), posQuery.getZ(), new ItemStack(stateQuery.getBlock().asItem()));
                level().destroyBlock(posQuery, false);
                level().addFreshEntity(ie);
            } else {
                phase = Phase.CLEANUP;
            }
        }
        else if(phase == Phase.CLEANUP) {
            kill();
        }
    }

    private void scanBlocksAroundTarget() {
        int tX = targetPos.getX();
        int tY = targetPos.getY();
        int tZ = targetPos.getZ();

        for(int y=tY-2;y<=tY+2;y++) {
            for(int z=tZ-2;z<=tZ+2;z++) {
                for (int x=tX-2;x<=tX+2;x++) {
                    boolean bT = y == tY + 2;
                    boolean bB = y == tY - 2;
                    boolean bL = z == tZ + 2;
                    boolean bR = z == tZ - 2;
                    boolean bF = x == tX + 2;
                    boolean bK = x == tX - 2;

                    //Skip the border BlockPos to make this look a bit more spherical
                    if((bT && bF) || (bT && bK) || (bT && bL) || (bT && bR) ||
                       (bB && bF) || (bB && bK) || (bB && bL) || (bB && bR) ||
                       (bF && bL) || (bF && bR) || (bK && bL) || (bK && bR))
                        continue;

                    BlockPos posQuery = new BlockPos(x,y,z);
                    BlockState stateQuery = level().getBlockState(posQuery);
                    if(stateQuery.is(TAG_HIGH_PRIORITY))
                        scannedHighPriorityBlocks.add(posQuery);
                    else if(stateQuery.is(TAG_LOW_PRIORITY))
                        scannedLowPriorityBlocks.add(posQuery);
                }
            }
        }
    }

    private enum Phase {
        SETUP_AND_SCAN,
        PARTICLES,
        SHATTER_HIGH_PRIORITY,
        SHATTER_LOW_PRIORITY,
        CLEANUP
    }
}
