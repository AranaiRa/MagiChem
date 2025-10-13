package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.gui.ActuatorEarthMenu;
import com.aranaira.magichem.gui.ActuatorEarthScreen;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ActuatorNeutralBlockEntity extends AbstractDirectionalPluginBlockEntity implements IPluginDevice {

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    public ActuatorNeutralBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public ActuatorNeutralBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_NEUTRAL_BE.get(), pPos, pBlockState);
    }

    @Override
    public boolean getIsSatisfied() {
        return true;
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

    @Override
    public void onLoad() {
        super.onLoad();
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putBoolean("isPaused", isPaused);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.isPaused = nbt.getBoolean("isPaused");
        this.ENERGY_STORAGE.setEnergy(nbt.getInt("storedPower"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("isPaused", isPaused);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void processCompletedOperation(int pCyclesCompleted) {
        syncAndSave();
    }

    public static void setPaused(ActuatorNeutralBlockEntity entity, boolean pauseState) {
        entity.isPaused = pauseState;
        entity.syncAndSave();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        boolean changed = AbstractDirectionalPluginBlockEntity.tick(level, pos, blockState, t, ActuatorNeutralBlockEntity::getValue);

        if(t instanceof ActuatorNeutralBlockEntity entity) {
            if(changed && !level.isClientSide())
                entity.syncAndSave();

            if (!entity.getPaused()) {
                //do stuff
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorNeutralBlockEntity entity) {
        boolean changed = AbstractDirectionalPluginBlockEntity.delegatedTick(level, pos, state, entity,
                ActuatorNeutralBlockEntity::getValue,
                ActuatorNeutralBlockEntity::getAffinity,
                ActuatorNeutralBlockEntity::getPowerDraw,
                ActuatorNeutralBlockEntity::handleAuxiliaryRequirements);

        if(changed) entity.syncAndSave();
    }

    private static Affinity getAffinity(Void unused) {
        return Affinity.LIGHTNING;
    }

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(60, 60) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    ////////////////////
    // STATIC RETRIEVAL
    ////////////////////

    public static int getValue(IDs id) {
        return 0;
    }

    public static int getPowerDraw(AbstractDirectionalPluginBlockEntity entity) {
        return ServerConfig.protoActuatorFECost;
    }

    public static boolean handleAuxiliaryRequirements(AbstractDirectionalPluginBlockEntity entity) {
        entity.satisfyAuxiliaryRequirements();
        return true;
    }

    ////////////////////
    // PROVISIONING CODE REQUIRED BY SUPERCLASS
    ////////////////////

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        return false;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return new HashMap<>();
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void provide(ItemStack pStack) {

    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return 0;
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return 0;
    }
}
