package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.GnosticOrbBlock;
import com.aranaira.magichem.entities.GnosticOrbExecutorEntity;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.util.render.ColorUtils;
import com.machinezoo.noexception.throwing.ThrowingRunnable;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

import static com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity.TRAIL_PARTICLE_COLORS;
import static com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeBlockEntity.materiaMap;

public class GnosticOrbBlockEntity extends BlockEntity {
    //Integer (first) is the number of minutes for a full charge
    //First ThrowingRunnable is a boolean-returning precondition to allow the prophecy to discharge. If this is false, the Orb won't discharge and will send a system message to the player.
    //Second ThrowingRunnable is the actual effect caused by the orb.
    private static final HashMap<String, Pair<Integer, ThrowingRunnable>> PROPHECY_DATA = new HashMap<>();
    private static final Random r = new Random();
    private static final ItemStack PARTICLE_STACK = new ItemStack(Blocks.OXIDIZED_COPPER.asItem());

    private int progress = 0, progressTarget = 0, materiaColor = 0;
    private String materiaType = "";
    private boolean preconditionValidated = false;

    public GnosticOrbBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.GNOSTIC_ORB_BE.get(), pPos, pBlockState);

        if(PROPHECY_DATA.size() == 0) {
            PROPHECY_DATA.put("creature", new Pair<>(10, null));
            PROPHECY_DATA.put("delight", new Pair<>(15, null));
            PROPHECY_DATA.put("disaster", new Pair<>(3, null));
            PROPHECY_DATA.put("exanimate", new Pair<>(15, this::prophecyConditionExanimate));
            PROPHECY_DATA.put("metal", new Pair<>(30, null));
            PROPHECY_DATA.put("thought", new Pair<>(60, this::prophecyConditionThought));
        }
    }

    public boolean tryStart(ItemStack pStack) {
        if(pStack.getItem() instanceof MateriaItem mi) {

            if (!materiaType.equals("")) return false;

            if (PROPHECY_DATA.keySet().contains(mi.getMateriaName())) {
                materiaType = mi.getMateriaName();
                progressTarget = PROPHECY_DATA.get(materiaType).getFirst() * 20;
                materiaColor = mi.getMateriaColor();
                syncAndSave();

                pStack.shrink(1);

                return true;
            }
        }
        return false;
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, E e) {
        if(e instanceof GnosticOrbBlockEntity entity) {
            if(entity.hasProphecyCooking()) {
                entity.progress = Math.min(entity.progressTarget, entity.progress + 1);

                //Particle work
                if(level.isClientSide()) {
                    final int[] color = ColorUtils.getRGBAIntTintFromPackedInt(entity.materiaColor);
                    Vec3 center = new Vec3(entity.getBlockPos().getX()+0.5, entity.getBlockPos().getY()+0.5, entity.getBlockPos().getZ()+0.5);

                    if(!entity.isProphecyReady()) {
                        //Charging sparks
                        for(int i=0; i<3; i++) {
                            Vec3 offset = new Vec3(r.nextDouble() - 0.5, r.nextDouble() - 0.5, r.nextDouble() - 0.5).normalize().scale(1.75);

                            level.addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                            .setPhysics(false).setScale(0.125f).setMaxAge(6 + r.nextInt(8))
                                            .setColor(color[0], color[1], color[2]),
                                    center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z(),
                                    center.x(), center.y(), center.z());
                        }

                        //Charging smoke
                        if(level.getGameTime() % 2 == 0) {
                            level.addParticle(new MAParticleType(ParticleInit.DUST.get())
                                            .setPhysics(false).setScale(0.125f).setMaxAge(32 + r.nextInt(32)).setGravity(0)
                                            .setColor(color[0], color[1], color[2], 30),
                                    center.x(), center.y() - 0.5, center.z(),
                                    (r.nextDouble() - 0.5) * 0.04, r.nextDouble() * 0.02 + 0.02, (r.nextDouble() - 0.5) * 0.04);
                        }
                    }

                    //Completion spirals
                    if (entity.isProphecyReady() && level.getGameTime() % 31 == 0) {
                        level.addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                                        .setPhysics(false).setScale(0.0625f).setMaxAge(80)
                                        .setColor(color[0], color[1], color[2], 128),
                                center.x(), center.y() - 0.03125, center.z(),
                                r.nextDouble() * 0.125 + 0.0625, 0.01 + r.nextDouble() * 0.01, 0.1);

                        level.addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                                        .setPhysics(false).setScale(0.0625f).setMaxAge(80)
                                        .setColor(color[0], color[1], color[2]),
                                center.x(), center.y() - 0.004, center.z(),
                                r.nextDouble() * 0.125 + 0.0625, -(0.005 + r.nextDouble() * 0.005), 0.1);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putInt("progress", progress);
        nbt.putInt("progressTarget", progressTarget);
        nbt.putInt("materiaColor", materiaColor);
        if(!materiaType.equals(""))
            nbt.putString("materiaType", materiaType);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        progress = nbt.getInt("progress");
        progressTarget = nbt.getInt("progressTarget");
        materiaColor = nbt.getInt("materiaColor");
        if(nbt.contains("materiaType"))
            materiaType = nbt.getString("materiaType");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("progress", progress);
        nbt.putInt("progressTarget", progressTarget);
        nbt.putInt("materiaColor", materiaColor);
        if(!materiaType.equals(""))
            nbt.putString("materiaType", materiaType);

        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void finalizeProphecy() {
        final ThrowingRunnable condition = PROPHECY_DATA.get(materiaType).getSecond();

        if(condition != null) {
            try {
                condition.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else
            preconditionValidated = true;

        if(preconditionValidated) {
            //Spawn and configure an executor
            GnosticOrbExecutorEntity executor = new GnosticOrbExecutorEntity(EntitiesRegistry.GNOSTIC_ORB_EXECUTOR_ENTITY.get(), level);
            executor.configure(materiaType, materiaColor);
            executor.setPos(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
            level.addFreshEntity(executor);
            executor.setPos(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());

            //Destroy the orb
            for(int i=0; i<40; i++) {
                Vector3 start = new Vector3(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
                Vector3 speed = new Vector3(r.nextDouble()-0.5, r.nextDouble(), r.nextDouble()-0.5).normalize().scale(0.08f + r.nextFloat(0.2f));

                level.addParticle(new MAParticleType(ParticleInit.ITEM.get())
                                .setScale(0.05f).setMaxAge(10 + r.nextInt(10))
                                .setStack(PARTICLE_STACK).setGravity(-0.05f),
                        start.x, start.y, start.z,
                        speed.x, speed.y + 0.5, speed.z);
            }
            level.destroyBlock(getBlockPos(), false);
        }
    }

    public boolean hasProphecyCooking() {
        return !materiaType.equals("");
    }

    public boolean isProphecyReady() {
        return hasProphecyCooking() && progress >= progressTarget;
    }

    public int getMateriaColor() {
        return materiaColor;
    }

    public float getProgressPercent() {
        if(progressTarget == 0) return 0;
        return Math.min(1,Math.max(0, (float)progress / (float)progressTarget));
    }

    /////////////////////
    // PROPHECY CONDITIONS
    /////////////////////

    private void prophecyConditionExanimate() {
        preconditionValidated = true;
    }

    private void prophecyConditionThought() {
        preconditionValidated = true;
    }
}
