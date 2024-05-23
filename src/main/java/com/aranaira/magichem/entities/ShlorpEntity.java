package com.aranaira.magichem.entities;

import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class ShlorpEntity extends Entity {
    public ShlorpEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        BlockPos bp = blockPosition();

        configure(bp.offset(-1,1,-1), bp.offset(1,1,1), 0.015625f, 0.25f, 6);
    }

    public int vertClusterCount = 2;
    public float
        speed = 1.0f, length = 1.0f, distanceBetweenClusters = 0.25f, currentPosOnTrack = 0.0f;
    Vector3
        startLocation, endLocation, control_a, control_b;

    public void configure(BlockPos pStartLocation, BlockPos pEndLocation, float pSpeed, float pDistanceBetweenClusters, int pClusterCount) {
        Vector3 start = new Vector3(pStartLocation.getX() + 0.5, pStartLocation.getY() + 0.5, pStartLocation.getZ() + 0.5);
        Vector3 end = new Vector3(pEndLocation.getX() + 0.5, pEndLocation.getY() + 0.5, pEndLocation.getZ() + 0.5);

        configure(start, end, pSpeed, pDistanceBetweenClusters, pClusterCount);
    }

    public void configure(Vector3 pStartLocation, Vector3 pEndLocation, float pSpeed, float pDistanceBetweenClusters, int pClusterCount) {
        this.startLocation = pStartLocation;
        this.endLocation = pEndLocation;
        this.speed = pSpeed;
        this.distanceBetweenClusters = pDistanceBetweenClusters;
        this.vertClusterCount = pClusterCount;

        this.length = (float)startLocation.distanceTo(endLocation);
        generateBezierControlPoints();
    }

    //This is straight up stolen from Mithion
    private void generateBezierControlPoints() {
        Vector3 o1 = new Vector3((double)this.startLocation.x, (double)this.startLocation.y, (double)this.startLocation.z);
        Vector3 midPoint = new Vector3((double)((this.startLocation.x + this.endLocation.x) / 2.0F), (double)((this.startLocation.y + this.endLocation.y) / 2.0F), (double)((this.startLocation.z + this.endLocation.z) / 2.0F));
        midPoint = midPoint.sub(o1);
        midPoint = midPoint.rotateYaw(1.5707964F);
        this.control_a = new Vector3((double)(this.startLocation.x + (this.endLocation.x - this.startLocation.x) / 3.0F), (double)(this.startLocation.y + (this.endLocation.y - this.startLocation.y) / 3.0F), (double)(this.startLocation.z + (this.endLocation.z - this.startLocation.z) / 3.0F));
        this.control_b = new Vector3((double)(this.startLocation.x + (this.endLocation.x - this.startLocation.x) / 3.0F * 2.0F), (double)(this.startLocation.y + (this.endLocation.y - this.startLocation.y) / 3.0F * 2.0F), (double)(this.startLocation.z + (this.endLocation.z - this.startLocation.z) / 3.0F * 2.0F));
        this.control_a = this.control_a.add(midPoint);
        this.control_b = this.control_b.add(midPoint);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public Vector3 generatePointOnBezierCurve(float time, float duration) {
        return Vector3.bezier(this.startLocation, this.endLocation, this.control_a, this.control_b, time / duration);
    }

    @Override
    public void tick() {
        super.tick();

        float limit = distanceBetweenClusters * (vertClusterCount + 1);

        if(currentPosOnTrack >= length + limit) {
            currentPosOnTrack = 0;
        } else {
            currentPosOnTrack += speed;
            if (currentPosOnTrack > length + limit)
                currentPosOnTrack = length + limit;
        }

        currentPosOnTrack += 0;

//        currentPosOnTrack = length-0.5f;
    }
}
