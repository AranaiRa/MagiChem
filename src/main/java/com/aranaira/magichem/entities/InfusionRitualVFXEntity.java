package com.aranaira.magichem.entities;

import com.aranaira.magichem.foundation.VesselData;
import com.aranaira.magichem.recipe.AlchemicalInfusionRitualRecipe;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.ritual.RitualEffectAlchemicalInfusion;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.faction.base.BaseFactionMob;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class InfusionRitualVFXEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final Random r = new Random();
    private BlockPos ritualCenter;
    private AlchemicalInfusionRitualRecipe recipe;
    Pair<VesselData, VesselData> vesselData;
    private int remainingTicks, state;
    private static final int
            STATE_ERROR = -1, STATE_WINDUP = 0, STATE_BUILDUP = 1, STATE_BOOM = 2, STATE_CLEANUP = 3,
            DURATION_ERROR = 200, DURATION_WINDUP = 10, DURATION_BUILDUP = RitualEffectAlchemicalInfusion.RITUAL_LIFESPAN - 4, DURATION_BOOM = 4;

    public InfusionRitualVFXEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void configure(BlockPos pRitualCenter, AlchemicalInfusionRitualRecipe pRecipe) {
        this.ritualCenter = pRitualCenter;
        this.recipe = pRecipe;

        this.remainingTicks = DURATION_WINDUP;
        this.state = STATE_WINDUP;

        doInitialStateCheck();
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        String key = ForgeRegistries.ITEMS.getKey(recipe.getAlchemyObject().getItem()).toString();
        nbt.putString("recipe", key);

        nbt.putLong("ritualCenter", ritualCenter.asLong());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        String key = nbt.getString("recipe");
        Item query = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
        this.recipe = AlchemicalInfusionRitualRecipe.getInfusionRitualRecipe(this.level(), new ItemStack(query));

        this.ritualCenter = BlockPos.of(nbt.getLong("ritualCenter"));

        doInitialStateCheck();
    }

    private void doInitialStateCheck() {
        this.vesselData = RitualEffectAlchemicalInfusion.getVesselPositions(this.level(), ritualCenter, recipe);
        VesselData lv = vesselData.getFirst();
        VesselData rv = vesselData.getSecond();

        if (lv.vesselBlockEntity == null || rv.vesselBlockEntity == null) {
            remainingTicks = DURATION_ERROR;
            state = STATE_ERROR;
        } else {
            boolean firstVesselSufficient = false;
            if (recipe.getComponentMateria().getFirst().getItem() == lv.vesselBlockEntity.getMateriaType())
                firstVesselSufficient = lv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getFirst().getCount();
            else if (recipe.getComponentMateria().getFirst().getItem() == rv.vesselBlockEntity.getMateriaType())
                firstVesselSufficient = rv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getFirst().getCount();

            boolean secondVesselSufficient = false;
            if (recipe.getComponentMateria().getSecond().getItem() == lv.vesselBlockEntity.getMateriaType())
                secondVesselSufficient = lv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getSecond().getCount();
            else if (recipe.getComponentMateria().getSecond().getItem() == rv.vesselBlockEntity.getMateriaType())
                secondVesselSufficient = rv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getSecond().getCount();

            if(!(firstVesselSufficient && secondVesselSufficient)) {
                state = STATE_CLEANUP;
            }
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeItemStack(recipe.getAlchemyObject(), true);

        buffer.writeLong(ritualCenter.asLong());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        ItemStack query = additionalData.readItem();
        this.recipe = AlchemicalInfusionRitualRecipe.getInfusionRitualRecipe(this.level(), query);

        this.ritualCenter = BlockPos.of(additionalData.readLong());

        doInitialStateCheck();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        if(ritualCenter != null && recipe != null){
            Vec3 center = new Vec3(ritualCenter.getX() + 0.5D, ritualCenter.getY() + RitualEffectAlchemicalInfusion.RITUAL_VFX_HEIGHT, ritualCenter.getZ() + 0.5D);

            //putter about for a bit
            if (state == STATE_ERROR) {
                VesselData lv = vesselData.getFirst();
                VesselData rv = vesselData.getSecond();

                //left side
                if (lv.vesselBlockEntity == null) {
                    this.level().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(128, 10, 10).setScale(1.5f),
                            lv.origin.x, lv.origin.y, lv.origin.z,
                            0, 0, 0);
                }
                //right side
                if (rv.vesselBlockEntity == null) {
                    this.level().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(128, 10, 10).setScale(1.5f),
                            rv.origin.x, rv.origin.y, rv.origin.z,
                            0, 0, 0);
                }

                remainingTicks--;
                if (remainingTicks <= 0) {
                    state = STATE_CLEANUP;
                }
            }
            //Wait until we're good to start animating
            else if (state == STATE_WINDUP) {

                remainingTicks--;
                if (remainingTicks <= 0) {
                    remainingTicks = DURATION_BUILDUP;
                    state = STATE_BUILDUP;
                }
            }
            //build up the visuals
            else if (state == STATE_BUILDUP) {
                //center orb
                float centerScale = (float) (RitualEffectAlchemicalInfusion.RITUAL_LIFESPAN - remainingTicks) / (float) RitualEffectAlchemicalInfusion.RITUAL_LIFESPAN;
                this.level().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setColor(22, 94, 69).setScale(0.2f + 2.2f * centerScale),
                        center.x, center.y, center.z,
                        0, 0, 0);

                //sparks
                if (remainingTicks % 10 == 0) {
                    for (int i = 0; i < r.nextInt(7); i++) {
                        Vec3 vector = new Vec3(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, r.nextFloat() - 0.5f).normalize();
                        final float speed = 0.08f;
                        this.level().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setColor(36, 151, 110).setScale(0.2f).setGravity(0.02f).setMaxAge(30).setPhysics(true),
                                center.x, center.y, center.z,
                                vector.x * speed, vector.y * speed + (r.nextFloat() * 0.125f), vector.z * speed);
                    }
                }

                remainingTicks--;
                if (remainingTicks <= 0) {
                    remainingTicks = DURATION_BOOM;
                    state = STATE_BOOM;
                }
            }
            //kaboom!
            else if (state == STATE_BOOM) {
                for (int i = 0; i < 40; i++) {
                    Vec3 vector = new Vec3(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, r.nextFloat() - 0.5f).normalize();
                    float speed = 0.09f;

                    //big seafoam
                    this.level().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(36, 151, 110).setScale(0.4f).setGravity(0.01f).setMaxAge(35).setPhysics(true),
                            center.x, center.y, center.z,
                            vector.x * speed, vector.y * speed + (r.nextFloat() * 0.125f), vector.z * speed);

                    //small white
                    speed = 0.12f;
                    this.level().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(255, 255, 255).setScale(0.15f).setGravity(0.005f).setMaxAge(35).setPhysics(true),
                            center.x, center.y, center.z,
                            vector.x * speed, vector.y * speed + (r.nextFloat() * 0.125f), vector.z * speed);
                }

                remainingTicks--;
                if (remainingTicks <= 0) {
                    state = STATE_CLEANUP;
                }
            } else if (state == STATE_CLEANUP) {
                this.kill();
            }
        }
    }
}
