package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ActuatorNeutralBlockEntity extends AbstractDirectionalPluginBlockEntity implements IPluginDevice {

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();
    private boolean isFESatisfied = false;
    private static final int FE_CONSUMPTION_RATE = 10;
    private static final Random r = new Random();

    public ActuatorNeutralBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public ActuatorNeutralBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_NEUTRAL_BE.get(), pPos, pBlockState);
    }

    @Override
    public boolean getIsSatisfied() {
        return isFESatisfied;
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
        nbt.putBoolean("isFESatisfied", isFESatisfied);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.isPaused = nbt.getBoolean("isPaused");
        this.isFESatisfied = nbt.getBoolean("isFESatisfied");
        this.ENERGY_STORAGE.setEnergy(nbt.getInt("storedPower"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("isPaused", isPaused);
        nbt.putBoolean("isFESatisfied", isFESatisfied);
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

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof ActuatorNeutralBlockEntity entity) {
            if (!entity.isPaused && entity.isFESatisfied && level.isClientSide()) {
                if (level.getGameTime() % 4 == 0) {
                    Vector3 start, end;
                    Direction dir = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

                    if(dir != Direction.NORTH) {
                        start = new Vector3(r.nextDouble(0.3125) + 0.375, 0.6875, 0.125);
                        end = new Vector3(r.nextDouble(0.3125) + 0.375, 0.25, 0.125);

                        level.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                        .setMaxAge(10),
                                pos.getX() + start.x, pos.getY() + start.y, pos.getZ() + start.z,
                                pos.getX() + end.x, pos.getY() + end.y, pos.getZ() + end.z);
                    }
                    if(dir != Direction.EAST) {
                        start = new Vector3(0.875, 0.6875, r.nextDouble(0.3125) + 0.375);
                        end = new Vector3(0.875, 0.25, r.nextDouble(0.3125) + 0.375);

                        level.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                        .setMaxAge(10),
                                pos.getX() + start.x, pos.getY() + start.y, pos.getZ() + start.z,
                                pos.getX() + end.x, pos.getY() + end.y, pos.getZ() + end.z);
                    }
                    if(dir != Direction.SOUTH) {
                        start = new Vector3(r.nextDouble(0.3125) + 0.375, 0.6875, 0.875);
                        end = new Vector3(r.nextDouble(0.3125) + 0.375, 0.25, 0.875);

                        level.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                        .setMaxAge(10),
                                pos.getX() + start.x, pos.getY() + start.y, pos.getZ() + start.z,
                                pos.getX() + end.x, pos.getY() + end.y, pos.getZ() + end.z);
                    }
                    if(dir != Direction.WEST) {
                        start = new Vector3(0.125, 0.6875, r.nextDouble(0.3125) + 0.375);
                        end = new Vector3(0.125, 0.25, r.nextDouble(0.3125) + 0.375);

                        level.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                        .setMaxAge(10),
                                pos.getX() + start.x, pos.getY() + start.y, pos.getZ() + start.z,
                                pos.getX() + end.x, pos.getY() + end.y, pos.getZ() + end.z);
                    }
                }
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorNeutralBlockEntity entity) {
        if(!entity.isPaused){
            boolean pre = entity.isFESatisfied;
            int extracted = entity.ENERGY_STORAGE.extractEnergy(FE_CONSUMPTION_RATE, false);
            entity.isFESatisfied = extracted == FE_CONSUMPTION_RATE;

            if (pre != entity.isFESatisfied) {
                entity.syncAndSave();
            }
        }
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
