package com.aranaira.magichem.entities;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.enums.ShlorpParticleMode;
import com.aranaira.magichem.item.MateriaItem;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class ShlorpEntity extends Entity implements IEntityAdditionalSpawnData {
    public ShlorpEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private static final ItemStack PARTICLE_STACK = new ItemStack(Items.SNOW_BLOCK);
    private static final Random r = new Random();
    public int vertClusterCount = 2;
    public int[] color = {255, 255, 255, 255};
    public float
        speed = 1.0f, length = 1.0f, distanceBetweenClusters = 0.25f, currentPosOnTrack = 0.0f;
    Vector3
        startLocation, endLocation, startTangent, endTangent;
    ItemStack stackInTransit;
    ShlorpParticleMode particleMode;

    public void configure(BlockPos pStartLocation, Vector3 pStartOrigin, Vector3 pStartTangent, BlockPos pEndLocation, Vector3 pEndOrigin, Vector3 pEndTangent, float pSpeed, float pDistanceBetweenClusters, int pClusterCount, MateriaItem pMateriaType, int pMateriaCount, ShlorpParticleMode pParticleMode) {
        Vector3 start = new Vector3(pStartLocation.getX(), pStartLocation.getY(), pStartLocation.getZ());
        Vector3 end = new Vector3(pEndLocation.getX(), pEndLocation.getY(), pEndLocation.getZ());

        configure(start, pStartOrigin, pStartTangent,
                end, pEndOrigin, pEndTangent,
                pSpeed, pDistanceBetweenClusters, pClusterCount,
                pMateriaType, pMateriaCount, pParticleMode);
    }

    public void configure(Vector3 pStartLocation, Vector3 pStartOrigin, Vector3 pStartTangent, Vector3 pEndLocation, Vector3 pEndOrigin, Vector3 pEndTangent, float pSpeed, float pDistanceBetweenClusters, int pClusterCount, MateriaItem pMateriaType, int pMateriaCount, ShlorpParticleMode pParticleMode) {
        Vec3 entityPositionRaw = position();
        Vector3 entityPosition = new Vector3(entityPositionRaw.x, entityPositionRaw.y, entityPositionRaw.z);

        this.startLocation = pStartLocation.add(pStartOrigin).sub(entityPosition).add(new Vector3(0.5, 0, 0.5));
        this.endLocation = pEndLocation.add(pEndOrigin).sub(entityPosition).add(new Vector3(0.5, 0, 0.5));

        this.startTangent = this.startLocation.add(pStartTangent);
        this.endTangent = this.endLocation.add(pEndTangent);

        this.speed = pSpeed;
        this.distanceBetweenClusters = pDistanceBetweenClusters;
        this.vertClusterCount = pClusterCount;
        this.length = (float)startLocation.distanceTo(endLocation);

        this.color[3] = 255;
        this.color[0] = pMateriaType.getMateriaColor() >> 16 & 255;
        this.color[1] = pMateriaType.getMateriaColor() >> 8 & 255;
        this.color[2] = pMateriaType.getMateriaColor() & 255;

        this.stackInTransit = new ItemStack(pMateriaType, pMateriaCount);

        this.particleMode = pParticleMode;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        //Itemstack
        String path = pCompound.getString("materiaType");
        Item materia = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MagiChemMod.MODID, path));
        if(materia != null) {
            int count = pCompound.getInt("materiaCount");
            stackInTransit = new ItemStack(materia, count);
        }

        //Vectors
        startLocation = new Vector3(
                pCompound.getDouble("startPosX"),
                pCompound.getDouble("startPosY"),
                pCompound.getDouble("startPosZ")
        );
        startTangent = new Vector3(
                pCompound.getDouble("startTanX"),
                pCompound.getDouble("startTanY"),
                pCompound.getDouble("startTanZ")
        );
        endLocation = new Vector3(
                pCompound.getDouble("endPosX"),
                pCompound.getDouble("endPosY"),
                pCompound.getDouble("endPosZ")
        );
        endTangent = new Vector3(
                pCompound.getDouble("endTanX"),
                pCompound.getDouble("endTanY"),
                pCompound.getDouble("endTanZ")
        );

        //Misc configs
        speed = pCompound.getFloat("speed");
        distanceBetweenClusters = pCompound.getFloat("separation");
        currentPosOnTrack = pCompound.getFloat("currentPos");
        vertClusterCount = pCompound.getInt("clusters");
        particleMode = ShlorpParticleMode.values()[pCompound.getInt("particleMode")];
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        //Itemstack
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(this.stackInTransit.getItem());
        pCompound.putString("materiaType",key.getPath());
        pCompound.putInt("materiaCount",stackInTransit.getCount());

        //Vectors
        pCompound.putDouble("startPosX",startLocation.x);
        pCompound.putDouble("startPosY",startLocation.y);
        pCompound.putDouble("startPosZ",startLocation.z);

        pCompound.putDouble("startTanX",startTangent.x);
        pCompound.putDouble("startTanY",startTangent.y);
        pCompound.putDouble("startTanZ",startTangent.z);

        pCompound.putDouble("endPosX",endLocation.x);
        pCompound.putDouble("endPosY",endLocation.y);
        pCompound.putDouble("endPosZ",endLocation.z);

        pCompound.putDouble("endTanX",endTangent.x);
        pCompound.putDouble("endTanY",endTangent.y);
        pCompound.putDouble("endTanZ",endTangent.z);

        //Misc configs
        pCompound.putFloat("speed",speed);
        pCompound.putFloat("separation",distanceBetweenClusters);
        pCompound.putFloat("currentPos",currentPosOnTrack);
        pCompound.putInt("clusters",vertClusterCount);
        pCompound.putInt("particleMode",particleMode.ordinal());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        //Itemstack
        buffer.writeItemStack(stackInTransit, true);

        //Vectors
        buffer.writeDouble(startLocation.x);
        buffer.writeDouble(startLocation.y);
        buffer.writeDouble(startLocation.z);

        buffer.writeDouble(startTangent.x);
        buffer.writeDouble(startTangent.y);
        buffer.writeDouble(startTangent.z);

        buffer.writeDouble(endLocation.x);
        buffer.writeDouble(endLocation.y);
        buffer.writeDouble(endLocation.z);

        buffer.writeDouble(endTangent.x);
        buffer.writeDouble(endTangent.y);
        buffer.writeDouble(endTangent.z);

        //Misc configs
        buffer.writeFloat(speed);
        buffer.writeFloat(distanceBetweenClusters);
        buffer.writeInt(vertClusterCount);
        buffer.writeInt(((MateriaItem)stackInTransit.getItem()).getMateriaColor());
        buffer.writeInt(particleMode.ordinal());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        //Itemstack
        stackInTransit = additionalData.readItem();

        //Vectors
        startLocation = new Vector3(
                additionalData.readDouble(),
                additionalData.readDouble(),
                additionalData.readDouble()
        );
        startTangent = new Vector3(
                additionalData.readDouble(),
                additionalData.readDouble(),
                additionalData.readDouble()
        );
        endLocation = new Vector3(
                additionalData.readDouble(),
                additionalData.readDouble(),
                additionalData.readDouble()
        );
        endTangent = new Vector3(
                additionalData.readDouble(),
                additionalData.readDouble(),
                additionalData.readDouble()
        );

        length = (float)startLocation.distanceTo(endLocation);

        //Misc configs
        speed = additionalData.readFloat();
        distanceBetweenClusters = additionalData.readFloat();
        vertClusterCount = additionalData.readInt();
        int packedColor = additionalData.readInt();
        color[3] = 255;
        color[0] = packedColor >> 16 & 255;
        color[1] = packedColor >> 8 & 255;
        color[2] = packedColor & 255;
        particleMode = ShlorpParticleMode.values()[additionalData.readInt()];
    }

    public Vector3 generatePointOnBezierCurve(float time, float duration) {
        return Vector3.bezier(this.startLocation, this.endLocation, this.startTangent, this.endTangent, time / duration);
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        float limit = distanceBetweenClusters * (vertClusterCount + 1);

        if(currentPosOnTrack >= length + limit) {
            //deliver the payload
            if(!this.level().isClientSide()) {
                Vector3 actualTargetPos = endLocation.add(new Vector3(position().x, position().y, position().z));
                BlockPos targetBlockPos = new BlockPos((int)Math.floor(actualTargetPos.x-0.5), (int)Math.floor(actualTargetPos.y), (int)Math.floor(actualTargetPos.z-0.5));
                BlockEntity be = this.level().getBlockEntity(targetBlockPos);

                if(be instanceof IShlorpReceiver isr) {
                    isr.insertStackFromShlorp(stackInTransit);
                }
            }
            kill();
        } else {
            currentPosOnTrack += speed;
            if (currentPosOnTrack > length + limit)
                currentPosOnTrack = length + limit;
        }
        currentPosOnTrack += 0;

        if(particleMode == ShlorpParticleMode.INVERSE_ENTRY_TANGENT && this.currentPosOnTrack > this.length) {
            Vec3 pos = getPosition(0).add(endLocation.x, endLocation.y, endLocation.z);

            Vector3 fwd = Vector3.bezier(this.startLocation, this.endLocation, this.startTangent, this.endTangent, 1)
                    .sub(Vector3.bezier(this.startLocation, this.endLocation, this.startTangent, this.endTangent, 0.5f)
                    ).normalize();
            Vector3 speed = fwd.scale(0.25f).add(
                    new Vector3((r.nextFloat() - 0.5f) * 1.2f + 0.4f, (r.nextFloat() - 0.5f) * 1.2f + 0.4f, (r.nextFloat() - 0.5f) * 1.2f + 0.4f)
                    .scale(0.1f)
                    );

            level().addParticle(new MAParticleType(ParticleInit.ITEM.get())
                            .setStack(PARTICLE_STACK)
                            .setScale(0.10f).setGravity(0.05f).setMaxAge(30)
                            .setColor(color[0], color[1], color[2], color[3])
                            .setMover(new ParticleVelocityMover(speed.x, speed.y, speed.z, true)),
                    pos.x - 0.5, pos.y, pos.z - 0.5,
                    0,0,0);
        } else if(particleMode == ShlorpParticleMode.DESTINATION_TANGENT && this.currentPosOnTrack > this.length) {
            Vec3 pos = getPosition(0).add(endLocation.x, endLocation.y, endLocation.z);

            Vector3 speed = endTangent.normalize().scale(0.25f).add(
                    new Vector3((r.nextFloat() - 0.5f) * 1.2f + 0.4f, (r.nextFloat() - 0.5f) * 1.2f + 0.4f, (r.nextFloat() - 0.5f) * 1.2f + 0.4f)
                            .scale(0.1f)
            );

            level().addParticle(new MAParticleType(ParticleInit.ITEM.get())
                            .setStack(PARTICLE_STACK)
                            .setScale(0.10f).setGravity(0.05f).setMaxAge(30)
                            .setColor(color[0], color[1], color[2], color[3])
                            .setMover(new ParticleVelocityMover(speed.x, speed.y, speed.z, true)),
                    pos.x - 0.5, pos.y, pos.z - 0.5,
                    0,0,0);
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(getX()-1000, getY()-100, getZ()-1000, getX()+1000, getY()+100, getZ()+1000);
    }
}
