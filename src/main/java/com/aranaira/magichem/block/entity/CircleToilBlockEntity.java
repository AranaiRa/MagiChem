package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.entities.EntityInit;
import com.mna.entities.constructs.animated.Construct;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.items.ItemStackHandler;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.Nullable;

public class CircleToilBlockEntity extends BlockEntity {

    private CompoundTag storedConstruct = new CompoundTag();
    public static float
        MAXIMUM_THETA = 1f, THETA_ACCELERATION_RATE = 0.02f;
    public float
        theta;
    public boolean
        constructDataChanged = false;

    public CircleToilBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_TOIL_BE.get(), pos, state);
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
        syncAndSave();

        return true;
    }

    public boolean hasConstruct() {
        return !storedConstruct.isEmpty();
    }

    public void ejectConstruct() {
        if(storedConstruct != null) {
            Construct construct = new Construct(EntityInit.ANIMATED_CONSTRUCT.get(), getLevel());
            construct.deserializeNBT(storedConstruct);
            construct.setPos(getBlockPos().getX(), getBlockPos().getY() + 1, getBlockPos().getZ());
            getLevel().addFreshEntity(construct);
            storedConstruct = new CompoundTag();
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
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        CompoundTag pre = storedConstruct.copy();
        super.load(nbt);
        storedConstruct = nbt.getCompound("construct");
        if(!storedConstruct.equals(pre))
            constructDataChanged = true;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("construct", this.storedConstruct);
        return nbt;
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, CircleToilBlockEntity entity) {
        int a = 0;
    }
}
