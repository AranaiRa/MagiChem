package com.aranaira.magichem.entities;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.IItemProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.enums.ShlorpParticleMode;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.MateriaItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

public class ItemShlorpEntity extends Entity implements IEntityAdditionalSpawnData {
    public ItemShlorpEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private static final Random r = new Random();
    public float
        speed = 1.0f, length = 1.0f, distanceBetweenClusters = 0.625f, currentPosOnTrack = 0.0f;
    Vector3
        startLocation, endLocation, startTangent, endTangent;
    NonNullList<ItemStack> stacksInTransit;
    BlockPos fallback = null;
    boolean doInstantPayload = false, processedFirstTick = false;

    public void configure(BlockPos pStartLocation, Vector3 pStartOrigin, Vector3 pStartTangent, BlockPos pEndLocation, Vector3 pEndOrigin, Vector3 pEndTangent, float pSpeed, NonNullList<ItemStack> pStacks) {
        Vector3 start = new Vector3(pStartLocation.getX(), pStartLocation.getY(), pStartLocation.getZ());
        Vector3 end = new Vector3(pEndLocation.getX(), pEndLocation.getY(), pEndLocation.getZ());

        configure(start, pStartOrigin, pStartTangent,
                end, pEndOrigin, pEndTangent,
                pSpeed, pStacks);
    }

    public void configure(Vector3 pStartLocation, Vector3 pStartOrigin, Vector3 pStartTangent, Vector3 pEndLocation, Vector3 pEndOrigin, Vector3 pEndTangent, float pSpeed, NonNullList<ItemStack> pStacks) {
        Vec3 entityPositionRaw = position();
        Vector3 entityPosition = new Vector3(entityPositionRaw.x, entityPositionRaw.y, entityPositionRaw.z);

        this.startLocation = pStartLocation.add(pStartOrigin).sub(entityPosition).add(new Vector3(0.5, 0, 0.5));
        this.endLocation = pEndLocation.add(pEndOrigin).sub(entityPosition).add(new Vector3(0.5, 0, 0.5));

        this.startTangent = this.startLocation.add(pStartTangent);
        this.endTangent = this.endLocation.add(pEndTangent);

        this.speed = pSpeed;
        this.length = (float)startLocation.distanceTo(endLocation);

        this.stacksInTransit = pStacks;
    }

    public void setInstantPayload() {
        doInstantPayload = true;
    }

    public void setFallback(BlockPos pBlockPos) {
        this.fallback = pBlockPos;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        //Itemstacks
        CompoundTag itemsTag = pCompound.getCompound("items");
        stacksInTransit = NonNullList.create();

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
        if(pCompound.contains("fallback")) {
            fallback = BlockPos.of(pCompound.getLong("fallback"));
        }

        //Misc configs
        speed = pCompound.getFloat("speed");
        distanceBetweenClusters = pCompound.getFloat("separation");
        currentPosOnTrack = pCompound.getFloat("currentPos");
        doInstantPayload = pCompound.getBoolean("doInstantPayload");
        processedFirstTick = pCompound.getBoolean("processedFirstTick");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        //Items
        ListTag stacksTag = new ListTag();
        for(int i=0;i<stacksInTransit.size(); i++) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putString("item", ForgeRegistries.ITEMS.getKey(stacksInTransit.get(i).getItem()).toString());
            itemTag.putInt("count", stacksInTransit.get(i).getCount());
            stacksTag.add(i, itemTag);
        }
        pCompound.put("stacksInTransit", stacksTag);

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

        //fallback
        if(fallback != null) {
            pCompound.putLong("fallback",fallback.asLong());
        }

        //Misc configs
        pCompound.putFloat("speed",speed);
        pCompound.putFloat("separation",distanceBetweenClusters);
        pCompound.putFloat("currentPos",currentPosOnTrack);
        pCompound.putBoolean("doInstantPayload",doInstantPayload);
        pCompound.putBoolean("processedFirstTick",processedFirstTick);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        //Items
        buffer.writeInt(stacksInTransit.size());
        for(int i=0; i<stacksInTransit.size(); i++) {
            buffer.writeItemStack(stacksInTransit.get(i), true);
        }

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
        buffer.writeBoolean(doInstantPayload);

        //fallback
        buffer.writeBoolean(fallback != null);
        if(fallback != null) buffer.writeLong(fallback.asLong());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        //Itemstack
        int itemTotal = additionalData.readInt();
        stacksInTransit = NonNullList.create();
        for(int i=0; i<itemTotal; i++) {
            stacksInTransit.add(i, additionalData.readItem());
        }

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
        doInstantPayload = additionalData.readBoolean();

        if(additionalData.readBoolean()) {
            fallback = BlockPos.of(additionalData.readLong());
        }
    }

    public Vector3 generatePointOnBezierCurve(float time, float duration) {
        return Vector3.bezier(this.startLocation, this.endLocation, this.startTangent, this.endTangent, time / duration);
    }

    public NonNullList<ItemStack> getStacksInTransit() {
        return stacksInTransit;
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if(!processedFirstTick) {
            processedFirstTick = true;

            if(doInstantPayload) {
                deliverPayload();
            }
        }

        float limit = distanceBetweenClusters * (stacksInTransit.size() + 1);

        if(currentPosOnTrack >= length + limit) {
            //deliver the payload
            if(!this.level().isClientSide()) {
                if(!doInstantPayload) {
                    deliverPayload();
                }
            }
            kill();
        } else {
            currentPosOnTrack += speed;
            if (currentPosOnTrack > length + limit)
                currentPosOnTrack = length + limit;
        }
        currentPosOnTrack += 0;
    }

    private void deliverPayload() {
        if(stacksInTransit.isEmpty())
            return;

        Vector3 actualTargetPos = endLocation.add(new Vector3(position().x, position().y, position().z));
        BlockPos targetBlockPos = new BlockPos((int) Math.floor(actualTargetPos.x - 0.5), (int) Math.floor(actualTargetPos.y), (int) Math.floor(actualTargetPos.z - 0.5));
        BlockEntity be = this.level().getBlockEntity(targetBlockPos);

        if (fallback != null) {
            be = this.level().getBlockEntity(fallback);

            if (be instanceof IItemProvisionRequester ipr) {
                ipr.provideItems(stacksInTransit);
                ipr.cancelItemProvisioningInProgress();
            }
        } else if (be instanceof IItemProvisionRequester ipr) {
            ipr.provideItems(stacksInTransit);
            ipr.cancelItemProvisioningInProgress();
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(getX()-1000, getY()-100, getZ()-1000, getX()+1000, getY()+100, getZ()+1000);
    }
}
