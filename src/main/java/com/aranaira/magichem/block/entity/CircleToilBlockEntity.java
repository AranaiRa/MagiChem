package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.EntityInit;
import com.mna.entities.constructs.animated.Construct;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CircleToilBlockEntity extends BlockEntity {

    private CompoundTag storedConstruct = new CompoundTag();
    public static float
            MAXIMUM_ROTATION_SPEED = 0.9f,
            THETA_ACCELERATION_RATE_WICKERWOOD   = 0.00015f, //5 minutes
            THETA_ACCELERATION_RATE_WOODSTONE    = 0.00075f, //60 seconds
            THETA_ACCELERATION_RATE_IRONGOLDBONE = 0.015f; //3 seconds
    public float
        theta, rotSpeed, acceleration;
    public boolean
        constructDataChanged = false;
    private static final Random r = new Random();

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    public CircleToilBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_TOIL_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
    }

    private void calculateAcceleration() {
        if(storedConstruct.isEmpty()) {
            acceleration = -THETA_ACCELERATION_RATE_IRONGOLDBONE;
            return;
        }

        acceleration = 0;

        CompoundTag composition = storedConstruct.getCompound("animated_construct_composition");
        final String[] head = composition.getString("HEAD").split("_");
        acceleration += getSpeedFromMaterialName(head[head.length-1]);

        final String[] torso = composition.getString("TORSO").split("_");
        acceleration += getSpeedFromMaterialName(torso[torso.length-1]);

        final String[] armL = composition.getString("LEFT_ARM").split("_");
        acceleration += getSpeedFromMaterialName(armL[armL.length-1]);

        final String[] armR = composition.getString("RIGHT_ARM").split("_");
        acceleration += getSpeedFromMaterialName(armR[armR.length-1]);

        final String[] legs = composition.getString("LEGS").split("_");
        acceleration += getSpeedFromMaterialName(legs[legs.length-1]);

        acceleration *= 0.2;
    }

    private float getSpeedFromMaterialName(String pQuery) {
        if(pQuery.equals("wickerwood")) return THETA_ACCELERATION_RATE_WICKERWOOD;
        else if(pQuery.equals("wood")) return THETA_ACCELERATION_RATE_WOODSTONE;
        else if(pQuery.equals("stone")) return THETA_ACCELERATION_RATE_WOODSTONE;

        return THETA_ACCELERATION_RATE_IRONGOLDBONE;
    }

    public boolean tryAbsorbConstruct(Player pPlayer) {
        AABB zone = new AABB(getBlockPos().offset(-5, -5, -5), getBlockPos().offset(5, 5, 5));

        Construct targetConstruct = null;
        for (Construct constructInZone : pPlayer.level().getEntitiesOfClass(Construct.class, zone)) {
            if(constructInZone.isFollowing(pPlayer)) {
                //no ender leggy!
                if(!constructInZone.getConstructData().isCapabilityEnabled(ConstructCapability.TELEPORT)) {
                    targetConstruct = constructInZone;
                    break;
                }
            }
        }

        if(targetConstruct == null)
            return false;

        storedConstruct = targetConstruct.serializeNBT();
        targetConstruct.remove(Entity.RemovalReason.DISCARDED);
        calculateAcceleration();
        syncAndSave();

        return true;
    }

    public boolean hasConstruct() {
        return !storedConstruct.isEmpty();
    }

    public void ejectConstruct() {
        if(!storedConstruct.isEmpty()) {
            Construct construct = new Construct(EntityInit.ANIMATED_CONSTRUCT.get(), getLevel());
            construct.deserializeNBT(storedConstruct);
            construct.setPos(getBlockPos().getX(), getBlockPos().getY() + 1, getBlockPos().getZ());
            getLevel().addFreshEntity(construct);
            storedConstruct = new CompoundTag();
            calculateAcceleration();
            syncAndSave();
        }
    }

    public CompoundTag getStoredConstructComposition() {
        if(storedConstruct.contains("animated_construct_composition"))
            return storedConstruct.getCompound("animated_construct_composition");
        else
            return new CompoundTag();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("construct", storedConstruct);
        nbt.putFloat("rotSpeed", this.rotSpeed);
        nbt.putInt("storedEnergy", this.ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        CompoundTag pre = storedConstruct.copy();
        super.load(nbt);
        storedConstruct = nbt.getCompound("construct");
        if(!storedConstruct.equals(pre))
            constructDataChanged = true;
        ENERGY_STORAGE.setEnergy(nbt.getInt("storedEnergy"));
        this.rotSpeed = nbt.getFloat("rotSpeed");
        calculateAcceleration();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("construct", this.storedConstruct);
        nbt.putFloat("rotSpeed", this.rotSpeed);
        nbt.putInt("storedEnergy", this.ENERGY_STORAGE.getEnergyStored());
        return nbt;
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, CircleToilBlockEntity entity) {
        //Animation driver, but also used to determine if we're generating power and generating particles
        entity.theta = (entity.theta + entity.rotSpeed) % 360;
        entity.rotSpeed = Math.max(0,Math.min(MAXIMUM_ROTATION_SPEED, entity.rotSpeed + entity.acceleration));

        if(entity.hasConstruct())
            generatePower(entity);

        //do occasional particles if we're at max speed
        if(entity.rotSpeed >= MAXIMUM_ROTATION_SPEED) {
            if(level.getGameTime() % 36 == 0) {

                for(int i=0; i<3; i++) {
                    double theta = r.nextDouble() * Math.PI * 2;

                    double x = Math.cos(theta);
                    double z = Math.sin(theta);

                    Vector3 inner = new Vector3(0.5, 1.1875, 0.5);
                    Vector3 outer = new Vector3(0.5, 0.8125, 0.5).add(new Vector3(x, 0, z).scale(0.42188f));

                    level.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                    .setMaxAge(10 + r.nextInt(25))
                            /*.setColor(150 + r.nextInt(75), 150 + r.nextInt(75), 150 + r.nextInt(75))*/,
                            pos.getX() + inner.x, pos.getY() + inner.y, pos.getZ() + inner.z,
                            pos.getX() + outer.x, pos.getY() + outer.y, pos.getZ() + outer.z);
                }
            }
        }
    }

    /* FE STUFF */

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    private static void generatePower(CircleToilBlockEntity entity) {
        int cap;
        int currentEnergy = entity.ENERGY_STORAGE.getEnergyStored();

        int genRate = Math.round(Config.circleToilGen * (entity.rotSpeed / MAXIMUM_ROTATION_SPEED));

        cap = genRate * Config.circleToilBuffer;
        entity.ENERGY_STORAGE.receiveEnergy(genRate, false);
        if (currentEnergy + genRate > cap) entity.ENERGY_STORAGE.setEnergy(cap);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3,5,3));
    }
}
