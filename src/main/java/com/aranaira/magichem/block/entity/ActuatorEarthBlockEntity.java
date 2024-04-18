package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.IBlockWithPowerLevel;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.gui.ActuatorEarthMenu;
import com.aranaira.magichem.gui.ActuatorEarthScreen;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ActuatorEarthBlockEntity extends DirectionalPluginBlockEntity implements MenuProvider, IBlockWithPowerLevel, IPluginDevice, IEldrinConsumerTile {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 67, 82, 100},
            SAND_PER_OPERATION = {0, 140, 170, 200, 230, 260, 290, 320, 350, 380, 410, 440, 470, 500},
            GRIME_REDUCTION = {0, 34, 37, 40, 43, 46, 49, 52, 55, 58, 61, 64, 67, 70};
    public static final int
            SLOT_COUNT = 3, SLOT_SAND = 0, SLOT_WASTE = 1, SLOT_RAREFIED_WASTE = 2,
            DATA_COUNT = 6, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1, DATA_FLAGS = 2, DATA_SAND = 3, DATA_GRIME = 4, DATA_RAREFIED_GRIME = 5,
            FLAG_IS_SATISFIED = 1,
            STAMPER_ANIMATION_PERIOD = 10;
    private int
            powerLevel = 1,
            remainingEldrinTime = -1,
            remainingSand = 0,
            currentGrime = 0,
            currentRarefiedGrime = 0,
            flags;
    private float
            remainingEldrinForSatisfaction;
    public float
            stamperDepth = 0,
            stamperDepthNextTick = 0;
    protected ContainerData data;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_SAND)
                return stack.getItem().equals(Items.SAND);
            return false;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ActuatorEarthBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.flags = 0;
    }

    public ActuatorEarthBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_EARTH_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch(pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorEarthBlockEntity.this.remainingEldrinTime;
                    case DATA_POWER_LEVEL -> ActuatorEarthBlockEntity.this.powerLevel;
                    case DATA_FLAGS -> ActuatorEarthBlockEntity.this.flags;
                    case DATA_SAND -> ActuatorEarthBlockEntity.this.remainingSand;
                    case DATA_GRIME -> ActuatorEarthBlockEntity.this.currentGrime;
                    case DATA_RAREFIED_GRIME -> ActuatorEarthBlockEntity.this.currentRarefiedGrime;
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorEarthBlockEntity.this.remainingEldrinTime = pValue;
                    case DATA_POWER_LEVEL -> ActuatorEarthBlockEntity.this.powerLevel = pValue;
                    case DATA_FLAGS -> ActuatorEarthBlockEntity.this.flags = pValue;
                    case DATA_SAND -> ActuatorEarthBlockEntity.this.remainingSand = pValue;
                    case DATA_GRIME -> ActuatorEarthBlockEntity.this.currentGrime = pValue;
                    case DATA_RAREFIED_GRIME -> ActuatorEarthBlockEntity.this.currentRarefiedGrime = pValue;
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };

        this.flags = FLAG_IS_SATISFIED;
    }

    public int getEldrinPowerUsage() {
        return ELDRIN_POWER_USAGE[this.powerLevel];
    }

    public static int getEldrinPowerUsage(int pPowerLevel) {
        return ELDRIN_POWER_USAGE[pPowerLevel];
    }

    public int getSandPerOperation() {
        return SAND_PER_OPERATION[this.powerLevel];
    }

    public static int getSandPerOperation(int pPowerLevel) {
        return SAND_PER_OPERATION[pPowerLevel];
    }

    public int getGrimeReductionRate() {
        return GRIME_REDUCTION[this.powerLevel];
    }

    public static int getGrimeReductionRate(int pPowerLevel) {
        return GRIME_REDUCTION[pPowerLevel];
    }

    public int getPowerLevel() {
        return this.powerLevel;
    }

    public void increasePowerLevel() {
        this.powerLevel = Math.min(13, this.powerLevel + 1);
    }

    public void decreasePowerLevel() {
        this.powerLevel = Math.max(1, this.powerLevel - 1);
    }

    @Override
    public void setPowerLevel(int pPowerLevel) {
        this.powerLevel = pPowerLevel;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("remainingSand", remainingSand);
        nbt.putInt("currentGrime", currentGrime);
        nbt.putInt("currentRarefiedGrime", currentRarefiedGrime);
        nbt.putInt("flags", flags);
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.remainingEldrinTime = nbt.getInt("remainingEldrinTime");
        this.powerLevel = nbt.getInt("powerLevel");
        this.remainingSand = nbt.getInt("remainingSand");
        this.currentGrime = nbt.getInt("currentGrime");
        this.currentRarefiedGrime = nbt.getInt("currentRarefiedGrime");
        this.flags = nbt.getInt("flags");

        if(nbt.contains("owner"))
            ownerUUID = nbt.getUUID("owner");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("remainingSand", remainingSand);
        nbt.putInt("currentGrime", currentGrime);
        nbt.putInt("currentRarefiedGrime", currentRarefiedGrime);
        nbt.putInt("flags", flags);
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        return nbt;
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void processCompletedOperation() {
        syncAndSave();
    }

    public static boolean getIsSatisfied(ActuatorEarthBlockEntity entity) {
        return (entity.flags & FLAG_IS_SATISFIED) == FLAG_IS_SATISFIED;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof ActuatorEarthBlockEntity aebe) {
            if(level.isClientSide()) {
                aebe.handleAnimationDrivers();
            }

            //Fill the internal sand buffer
            if(Config.quakeRefinerySandCapacity - aebe.remainingSand >= 1000) {
                ItemStack sandStack = aebe.itemHandler.getStackInSlot(SLOT_SAND);
                if(!sandStack.isEmpty()) {
                    sandStack.shrink(1);
                    aebe.itemHandler.setStackInSlot(SLOT_SAND, sandStack);
                    aebe.remainingSand += 1000;
                    aebe.syncAndSave();
                }
            }

            //Dump grime to waste
            if(aebe.currentGrime > Config.grimePerWaste) {
                ItemStack wasteStack = aebe.itemHandler.getStackInSlot(SLOT_WASTE);
                int maxWasteToAdd = aebe.currentGrime / Config.grimePerWaste;
                int actualWasteToAdd;
                if(wasteStack == ItemStack.EMPTY) {
                    actualWasteToAdd = Math.min(maxWasteToAdd, 64);
                    wasteStack = new ItemStack(ItemRegistry.ALCHEMICAL_WASTE.get(), actualWasteToAdd);
                } else {
                    actualWasteToAdd = Math.min(64 - wasteStack.getCount(), maxWasteToAdd);
                    wasteStack = new ItemStack(ItemRegistry.ALCHEMICAL_WASTE.get(), wasteStack.getCount() + actualWasteToAdd);
                }
                aebe.itemHandler.setStackInSlot(SLOT_WASTE, wasteStack);
                aebe.currentGrime -= actualWasteToAdd * Config.grimePerWaste;
                aebe.syncAndSave();
            }

            //Dump rarefied
            if(aebe.currentRarefiedGrime > Config.grimePerWaste) {
                ItemStack wasteStack = aebe.itemHandler.getStackInSlot(SLOT_RAREFIED_WASTE);
                int maxWasteToAdd = aebe.currentRarefiedGrime / Config.grimePerWaste;
                int actualWasteToAdd;
                if(wasteStack == ItemStack.EMPTY) {
                    actualWasteToAdd = Math.min(maxWasteToAdd, 64);
                    wasteStack = new ItemStack(ItemRegistry.RAREFIED_WASTE.get(), actualWasteToAdd);
                } else {
                    actualWasteToAdd = Math.min(64 - wasteStack.getCount(), maxWasteToAdd);
                    wasteStack = new ItemStack(ItemRegistry.RAREFIED_WASTE.get(), wasteStack.getCount() + actualWasteToAdd);
                }
                aebe.itemHandler.setStackInSlot(SLOT_RAREFIED_WASTE, wasteStack);
                aebe.currentRarefiedGrime -= actualWasteToAdd * Config.grimePerWaste;
                aebe.syncAndSave();
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorEarthBlockEntity entity) {
        Player ownerCheck = entity.getOwner();
        int powerDraw = entity.getEldrinPowerUsage();

        if(ownerCheck != null) {
            float consumption = entity.consume(ownerCheck, pos, pos.getCenter(), Affinity.EARTH, Math.min(powerDraw, entity.remainingEldrinForSatisfaction), 1);
            entity.remainingEldrinForSatisfaction -= consumption;

            //Eldrin processing
            if(entity.remainingEldrinTime <= 0) {
                if(entity.remainingEldrinForSatisfaction <= 0) {
                    entity.remainingEldrinForSatisfaction = powerDraw;
                    entity.remainingEldrinTime = Config.quakeRefineryOperationTime;
                }

                if(!getIsSatisfied(entity)) {
                    entity.syncAndSave();
                }
            }
            entity.remainingEldrinTime = Math.max(-1, entity.remainingEldrinTime - 1);

            if(entity.remainingEldrinTime >= 0) entity.flags = entity.flags | ActuatorEarthBlockEntity.FLAG_IS_SATISFIED;
            else {
                if(getIsSatisfied(entity)) {
                    entity.flags = entity.flags & ~ActuatorEarthBlockEntity.FLAG_IS_SATISFIED;
                    entity.syncAndSave();
                } else
                    entity.flags = entity.flags & ~ActuatorEarthBlockEntity.FLAG_IS_SATISFIED;
            }
        }
    }

    public void handleAnimationDrivers() {
        boolean doDriverUpdate = true;

        if(((flags & FLAG_IS_SATISFIED) != FLAG_IS_SATISFIED) || (remainingSand < getSandPerOperation())) {
            if(stamperDepth == 0) {
                doDriverUpdate = false;
                stamperDepthNextTick = 0;
            }
        }

        if(doDriverUpdate) {
            //Stamper motion
            float loopingTime = (level.getGameTime() % STAMPER_ANIMATION_PERIOD) / (float) STAMPER_ANIMATION_PERIOD;
            stamperDepth = 1 - (float) Math.sin(loopingTime * Math.PI);

            loopingTime = ((level.getGameTime() + 1) % STAMPER_ANIMATION_PERIOD) / (float) STAMPER_ANIMATION_PERIOD;
            stamperDepthNextTick = 1 - (float) Math.sin(loopingTime * Math.PI);

            //Particles
            if(stamperDepth == 1) {
                spawnSandParticles();
            }
        }
    }

    public void spawnSandParticles() {
        Vector3f mid = new Vector3f(0.5f, 0.75f, 0.5f);

        int particleCount = 14;
        for(int i=0; i<particleCount; i++) {
            double theta = (i / (float)particleCount) * Math.PI * 2;
            float radius = 0.125f;
            double pLSpeed = 0.075f;
            Vector3f offset = new Vector3f((float)Math.cos(theta), 0.0f, (float)Math.sin(theta));

            Vector3 pos = new Vector3(getBlockPos().getX() + mid.x + offset.x * radius,
                                      getBlockPos().getY() + mid.y + offset.y * radius,
                                      getBlockPos().getZ() + mid.z + offset.z * radius);
            Vector3 speed = new Vector3(offset.x * pLSpeed,
                                        offset.y + 0.05,
                                        offset.z * pLSpeed);

            level.addParticle(new MAParticleType(ParticleInit.DUST.get())
                            .setPhysics(true).setScale(0.10f).setGravity(0.02f)
                            .setMover(new ParticleVelocityMover(speed.x, speed.y, speed.z, true))
                            .setColor(213, 196, 150, 48),
                    pos.x, pos.y, pos.z,
                    0, 0, 0);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyItemHandler.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ActuatorEarthMenu(i, inventory, this, this.data);
    }

    public int addGrimeToBuffer(int pGrimeToAdd) {
        int insertion = 0;
        int reduction;
        int rarefied = 0;
        int overflow = pGrimeToAdd;

        //Determine how much grime is being added
        if((flags & FLAG_IS_SATISFIED) == FLAG_IS_SATISFIED) {
            if (remainingSand >= getSandPerOperation()) {
                remainingSand -= getSandPerOperation();

                //Reduce insertion and generate rarefied grime
                reduction = Math.round((float) pGrimeToAdd * ((float) getGrimeReductionRate()) / 100.0f);
                insertion = pGrimeToAdd - reduction;
                rarefied = Math.round(((float) Config.quakeRefineryRarefiedRate / 100.0f) * (float) reduction);

                if (currentGrime + insertion > Config.quakeRefineryGrimeCapacity) {
                    overflow = insertion - (Config.quakeRefineryGrimeCapacity - currentGrime);
                    insertion = Config.quakeRefineryGrimeCapacity - currentGrime;
                } else {
                    overflow = 0;
                }
            }
        }

        //Final application
        currentGrime += insertion;
        currentRarefiedGrime += rarefied;
        syncAndSave();
        return overflow;
    }

    public static float getSandPercent(int pSandAmount) {
        return (float)pSandAmount * 100f / Config.quakeRefinerySandCapacity;
    }

    public float getSandPercent() {
        return (float)remainingSand * 100f / Config.quakeRefinerySandCapacity;
    }

    public static int getScaledSand(int pSandAmount) {
        return pSandAmount * ActuatorEarthScreen.FLUID_GAUGE_H / Config.quakeRefinerySandCapacity;
    }

    public static float getGrimePercent(int pGrimeAmount) {
        return (float)pGrimeAmount * 100f / Config.quakeRefineryGrimeCapacity;
    }

    public static int getScaledGrime(int pGrimeAmount) {
        return pGrimeAmount * ActuatorEarthScreen.FLUID_GAUGE_H / Config.quakeRefineryGrimeCapacity;
    }
}
