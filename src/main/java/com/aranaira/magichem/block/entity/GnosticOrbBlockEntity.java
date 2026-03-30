package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.GnosticOrbBlock;
import com.aranaira.magichem.entities.GnosticOrbExecutorEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.util.render.ColorUtils;
import com.machinezoo.noexception.throwing.ThrowingConsumer;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity.TRAIL_PARTICLE_COLORS;
import static com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeBlockEntity.materiaMap;
import static com.aranaira.magichem.entities.GnosticOrbExecutorEntity.TAG_BOOKSHELVES;

public class GnosticOrbBlockEntity extends BlockEntity {
    //Integer (first) is the number of minutes for a full charge
    //First ThrowingRunnable is a boolean-returning precondition to allow the prophecy to discharge. If this is false, the Orb won't discharge and will send a system message to the player.
    //Second ThrowingRunnable is the actual effect caused by the orb.
    private static final HashMap<String, Pair<Integer, Consumer<GnosticOrbBlockEntity>>> PROPHECY_DATA = new HashMap<>();
    private static final Random r = new Random();
    private static final ItemStack PARTICLE_STACK = new ItemStack(Blocks.OXIDIZED_COPPER.asItem());

    private int progress = 0, progressTarget = 0, materiaColor = 0;
    private String materiaType = "";
    private boolean preconditionValidated = false;

    public GnosticOrbBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.GNOSTIC_ORB_BE.get(), pPos, pBlockState);

        if(PROPHECY_DATA.size() == 0) {
            PROPHECY_DATA.put("construct", new Pair<>(12, GnosticOrbBlockEntity::prophecyConditionConstruct));
            PROPHECY_DATA.put("creature", new Pair<>(10, null));
            PROPHECY_DATA.put("delight", new Pair<>(15, null));
            PROPHECY_DATA.put("disaster", new Pair<>(3, null));
            PROPHECY_DATA.put("erosion", new Pair<>(3, null));
            PROPHECY_DATA.put("exanimate", new Pair<>(15, GnosticOrbBlockEntity::prophecyConditionExanimate));
            PROPHECY_DATA.put("luck", new Pair<>(20, null));
            PROPHECY_DATA.put("odors", new Pair<>(3, GnosticOrbBlockEntity::prophecyConditionOdors));
            PROPHECY_DATA.put("sleep", new Pair<>(10, null));
            PROPHECY_DATA.put("thought", new Pair<>(60, GnosticOrbBlockEntity::prophecyConditionThought));
        }
    }

    public boolean tryStart(ItemStack pStack) {
        if(pStack.getItem() instanceof MateriaItem mi && pStack.getCount() >= (mi.getMateriaName().equals("luck") ? 20 : 50)) {

            if (!materiaType.equals("")) return false;

            if (PROPHECY_DATA.keySet().contains(mi.getMateriaName())) {
                materiaType = mi.getMateriaName();
                progressTarget = PROPHECY_DATA.get(materiaType).getFirst() * 20 * 60;
                materiaColor = mi.getMateriaColor();
                syncAndSave();

                return true;
            }
        }
        return false;
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, E e) {
        if(e instanceof GnosticOrbBlockEntity entity) {
            if(entity.hasProphecyCooking()) {
                int pre = entity.progress;
                entity.progress = Math.min(entity.progressTarget, entity.progress + 1);
                if(!level.isClientSide()) entity.setChanged();
                if((pre != entity.progress) && (entity.progress >= entity.progressTarget))
                    level.setBlock(pos, blockState.setValue(MagiChemBlockStateProperties.READY_FOR_COLLECTION, true), 3);

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
        super.saveAdditional(nbt);
        nbt.putInt("progress", progress);
        nbt.putInt("progressTarget", progressTarget);
        nbt.putInt("materiaColor", materiaColor);
        if(!materiaType.equals(""))
            nbt.putString("materiaType", materiaType);
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

    public void skipToFullCharge() {
        this.progress = progressTarget - 5;
    }

    public void finalizeProphecy(Player pActivatingPlayer) {
        Consumer<GnosticOrbBlockEntity> condition = PROPHECY_DATA.get(materiaType).getSecond();

        if(condition != null) {
            try {
                condition.accept(this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else
            preconditionValidated = true;

        if(preconditionValidated) {
            //Spawn and configure an executor
            GnosticOrbExecutorEntity executor = new GnosticOrbExecutorEntity(EntitiesRegistry.GNOSTIC_ORB_EXECUTOR_ENTITY.get(), level);
            executor.configure(materiaType, materiaColor, pActivatingPlayer);
            executor.setPos(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
            level.addFreshEntity(executor);
            executor.setPos(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());

            //Destroy the orb
            level.destroyBlock(getBlockPos(), false);
            for(int i=0; i<10; i++) {
                Vector3 start = new Vector3(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
                Vector3 speed = new Vector3(r.nextDouble()-0.5, 0, r.nextDouble()-0.5).normalize().scale(0.16f + r.nextFloat(1.2f));

                level.addParticle(new MAParticleType(ParticleInit.ITEM.get())
                                .setScale(0.05f).setMaxAge(10 + r.nextInt(10))
                                .setStack(PARTICLE_STACK).setGravity(-0.05f).setPhysics(false),
                        start.x, start.y, start.z,
                        speed.x, speed.y + 1.5, speed.z);
            }
            int[] color = ColorUtils.getRGBAIntTintFromPackedInt(materiaColor);
            for(int i=0; i<36; i++) {
                Vector3 start = new Vector3(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
                Vector3 speed = new Vector3(r.nextDouble()-0.5, r.nextDouble()-0.5, r.nextDouble()-0.5).normalize().scale(0.66f + r.nextFloat(1.2f));

                level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setMaxAge(20 + r.nextInt(20))
                                .setColor(color[0], color[1], color[2]).setGravity(0).setPhysics(false),
                        start.x, start.y, start.z,
                        speed.x, speed.y, speed.z);
            }
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

    private static void prophecyConditionConstruct(GnosticOrbBlockEntity pEntity) {
        boolean foundIronBlock = false;

        int range = 8;
        for(int y = pEntity.getBlockPos().getY()-(range/2); y<=pEntity.getBlockPos().getY()+(range/2); y++) {
            for (int x = pEntity.getBlockPos().getX()-range; x<=pEntity.getBlockPos().getX()+range; x++) {
                for (int z = pEntity.getBlockPos().getZ()-range; z<=pEntity.getBlockPos().getZ()+range; z++) {
                    BlockPos posQuery = new BlockPos(x, y, z);
                    BlockState stateQuery = pEntity.level.getBlockState(posQuery);

                    boolean isIronBlock = stateQuery.getBlock() == Blocks.IRON_BLOCK;

                    if(isIronBlock) {
                        foundIronBlock = true;
                        break;
                    }
                }
            }
        }

        if(foundIronBlock)
            pEntity.preconditionValidated = true;
    }

    private static void prophecyConditionExanimate(GnosticOrbBlockEntity pEntity) {
        if(pEntity.getLevel() == null) return;

        boolean foundVillager = false;
        int range = 30;
        AABB bounds = new AABB(
                pEntity.getBlockPos().getX() - range, pEntity.getBlockPos().getY() - (range / 3f), pEntity.getBlockPos().getZ() - range,
                pEntity.getBlockPos().getX() + range, pEntity.getBlockPos().getY() + (range / 3f), pEntity.getBlockPos().getZ() + range
        );
        for(Entity e : pEntity.getLevel().getEntities(null, bounds)) {
            if(e instanceof Villager v) {
                foundVillager = true;
                break;
            }
        }

        pEntity.preconditionValidated = foundVillager;
    }

    private static void prophecyConditionOdors(GnosticOrbBlockEntity pEntity) {
        pEntity.preconditionValidated = true;
    }

    private static void prophecyConditionThought(GnosticOrbBlockEntity pEntity) {
        boolean foundBookshelf = false;

        int range = 6;
        for(int y = pEntity.getBlockPos().getY()-(range/2); y<=pEntity.getBlockPos().getY()+(range/2); y++) {
            for (int x = pEntity.getBlockPos().getX()-range; x<=pEntity.getBlockPos().getX()+range; x++) {
                for (int z = pEntity.getBlockPos().getZ()-range; z<=pEntity.getBlockPos().getZ()+range; z++) {
                    BlockPos posQuery = new BlockPos(x, y, z);
                    BlockState stateQuery = pEntity.level.getBlockState(posQuery);

                    boolean isBookshelf = stateQuery.is(TAG_BOOKSHELVES);

                    if(isBookshelf) {
                        foundBookshelf = true;
                        break;
                    }
                }
            }
        }

        if(foundBookshelf)
            pEntity.preconditionValidated = true;
    }
}
