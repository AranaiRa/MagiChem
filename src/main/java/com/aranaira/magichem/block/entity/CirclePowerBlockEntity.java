package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.CirclePowerBlock;
import com.aranaira.magichem.block.entity.renderer.CirclePowerBlockEntityRenderer;
import com.aranaira.magichem.foundation.IRequiresRouterCleanupOnDestruction;
import com.aranaira.magichem.gui.CirclePowerMenu;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.mna.api.capabilities.WellspringNode;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
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
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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

import java.util.Random;

import static com.aranaira.magichem.util.render.ColorUtils.SIX_STEP_PARTICLE_COLORS;

public class CirclePowerBlockEntity extends BlockEntity implements MenuProvider, IRequiresRouterCleanupOnDestruction {
    public static final int
        SLOT_COUNT = 9,
        SLOT_REAGENT_1 = 0, SLOT_REAGENT_2 = 1, SLOT_REAGENT_3 = 2, SLOT_REAGENT_4 = 3, SLOT_RECHARGE = 4,
        WASTE_REAGENT_1 = 5, WASTE_REAGENT_2 = 6, WASTE_REAGENT_3 = 7, WASTE_REAGENT_4 = 8;
    public float
        circleFillPercent, auxiliaryInnerCircleFillPercent, auxiliaryOuterCircleFillPercent;
    public static final float
        CIRCLE_FILL_RATE = 0.015f;
    private static final Random r = new Random();

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch(slot) {
                case SLOT_REAGENT_1 ->
                        stack.getItem() == ItemRegistry.SILVER_DUST.get() ||
                        stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                case SLOT_REAGENT_2 ->
                        stack.getItem() == ItemRegistry.FOCUSING_CATALYST.get() ||
                        stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                case SLOT_REAGENT_3 ->
                        stack.getItem() == ItemRegistry.AMPLIFYING_PRISM.get() ||
                        stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                case SLOT_REAGENT_4 ->
                        stack.getItem() == ItemRegistry.AUXILIARY_CIRCLE_ARRAY.get() ||
                        stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                case SLOT_RECHARGE ->
                        stack.getCapability(ForgeCapabilities.ENERGY).isPresent();
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public CirclePowerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_POWER_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> CirclePowerBlockEntity.this.progressReagentTier1;
                    case 1 -> CirclePowerBlockEntity.this.progressReagentTier2;
                    case 2 -> CirclePowerBlockEntity.this.progressReagentTier3;
                    case 3 -> CirclePowerBlockEntity.this.progressReagentTier4;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CirclePowerBlockEntity.this.progressReagentTier1 = value;
                    case 1 -> CirclePowerBlockEntity.this.progressReagentTier2 = value;
                    case 2 -> CirclePowerBlockEntity.this.progressReagentTier3 = value;
                    case 3 -> CirclePowerBlockEntity.this.progressReagentTier4 = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int
            progressReagentTier1 = 0,
            progressReagentTier2 = 0,
            progressReagentTier3 = 0,
            progressReagentTier4 = 0;
    private static final int
            maxProgressReagentTier1 = 640,
            maxProgressReagentTier2 = 2160,
            maxProgressReagentTier3 = 3680,
            maxProgressReagentTier4 = 5200;

    public static final Item
            REAGENT_TIER1 =  ItemRegistry.SILVER_DUST.get(),
            REAGENT_TIER2 =  ItemRegistry.FOCUSING_CATALYST.get(),
            REAGENT_TIER3 =  ItemRegistry.AMPLIFYING_PRISM.get(),
            REAGENT_TIER4 =  ItemRegistry.AUXILIARY_CIRCLE_ARRAY.get(),
            WASTE_TIER1 =  ItemRegistry.TARNISHED_SILVER_LUMP.get(),
            WASTE_TIER2 =  ItemRegistry.WARPED_FOCUSING_CATALYST.get(),
            WASTE_TIER3 =  ItemRegistry.MALFORMED_BRINDLE_GLASS.get(),
            WASTE_TIER4 =  ItemRegistry.RUINED_PROJECTION_APPARATUS.get();
    private NonNullList<ItemStack> itemsForRemoteCharging = NonNullList.create();

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.magichem.circle_power");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new CirclePowerMenu(id, inventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("progressReagentTier1", this.progressReagentTier1);
        nbt.putInt("progressReagentTier2", this.progressReagentTier2);
        nbt.putInt("progressReagentTier3", this.progressReagentTier3);
        nbt.putInt("progressReagentTier4", this.progressReagentTier4);
        nbt.putInt("storedEnergy", this.ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        if(nbt.getCompound("inventory").getInt("Size") != itemHandler.getSlots()) {
            ItemStackHandler temp = new ItemStackHandler(nbt.getCompound("inventory").size());
            temp.deserializeNBT(nbt.getCompound("inventory"));
            for(int i=0; i<temp.getSlots(); i++) {
                itemHandler.setStackInSlot(i, temp.getStackInSlot(i));
            }
        } else {
            this.itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        }
        progressReagentTier1 = nbt.getInt("progressReagentTier1");
        progressReagentTier2 = nbt.getInt("progressReagentTier2");
        progressReagentTier3 = nbt.getInt("progressReagentTier3");
        progressReagentTier4 = nbt.getInt("progressReagentTier4");
        ENERGY_STORAGE.setEnergy(nbt.getInt("storedEnergy"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("progressReagentTier1", this.progressReagentTier1);
        nbt.putInt("progressReagentTier2", this.progressReagentTier2);
        nbt.putInt("progressReagentTier3", this.progressReagentTier3);
        nbt.putInt("progressReagentTier4", this.progressReagentTier4);
        nbt.putInt("storedEnergy", this.ENERGY_STORAGE.getEnergyStored());
        return nbt;
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i=0; i<itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        //Make sure we don't void reagents entirely if the block is broken; always drop waste of a currently "burning" reagent
        if(progressReagentTier1 > 0) {
            if(itemHandler.getStackInSlot(SLOT_REAGENT_1).getItem() == ItemRegistry.DEBUG_ORB.get())
                inventory.addItem(new ItemStack(ItemRegistry.DEBUG_ORB.get(), 1));
            else
                inventory.addItem(new ItemStack(WASTE_TIER1, 1));
        }
        if(progressReagentTier2 > 0) {
            if (itemHandler.getStackInSlot(SLOT_REAGENT_2).getItem() == ItemRegistry.DEBUG_ORB.get())
                inventory.addItem(new ItemStack(ItemRegistry.DEBUG_ORB.get(), 1));
            else
                inventory.addItem(new ItemStack(WASTE_TIER2, 1));
        }
        if(progressReagentTier3 > 0) {
            if (itemHandler.getStackInSlot(SLOT_REAGENT_3).getItem() == ItemRegistry.DEBUG_ORB.get())
                inventory.addItem(new ItemStack(ItemRegistry.DEBUG_ORB.get(), 1));
            else
                inventory.addItem(new ItemStack(WASTE_TIER3, 1));
        }
        if(progressReagentTier4 > 0) {
            if (itemHandler.getStackInSlot(SLOT_REAGENT_4).getItem() == ItemRegistry.DEBUG_ORB.get())
                inventory.addItem(new ItemStack(ItemRegistry.DEBUG_ORB.get(), 1));
            else
                inventory.addItem(new ItemStack(WASTE_TIER4, 6));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void addRemoteChargeItem(ItemStack pStack) {
        itemsForRemoteCharging.add(pStack);
    }

    public boolean removeRemoteChargeItem(ItemStack pStack) {
        if (itemsForRemoteCharging.contains(pStack)) {
            itemsForRemoteCharging.remove(pStack);
            return true;
        }

        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CirclePowerBlockEntity entity) {
        if(level.isClientSide()) {
            entity.handleAnimationDrivers();

            //Particle Work
            {
                boolean has1 = entity.hasReagent(1);
                boolean has2 = entity.hasReagent(2);
                boolean has3 = entity.hasReagent(3);
                boolean has4 = entity.hasReagent(4);

                if(has1) {
                    float rot1 = CirclePowerBlockEntityRenderer.getReagent1Rotation(level, 0.5f);
                    int total = 12;
                    for(int i=0; i<total; i++) {
                        double radianWiggle = ((Math.PI * 2) / (double)total) * (r.nextDouble() - 0.5);

                        double x = Math.cos((Math.PI * 2) * i / (double)total + rot1 + radianWiggle);
                        double z = Math.sin((Math.PI * 2) * i / (double)total + rot1 + radianWiggle);

                        double rx = r.nextDouble() - 0.5;
                        double ry = r.nextDouble();
                        double rz = r.nextDouble() - 0.5;

                        Vector3 mid = new Vector3(0.5, 0.75, 0.5).add(new Vector3(x, 0, z).scale(1.9f));

                        level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setMaxAge(20 + r.nextInt(30)).setScale(0.03f + r.nextFloat() * 0.03f)
                                        .setColor(150+r.nextInt(75), 150+r.nextInt(75), 150+r.nextInt(75))
                                        .setMover(new ParticleVelocityMover(rx * 0.03, 0.01 + ry * 0.02, rz * 0.03, true)),
                                pos.getX() + mid.x, pos.getY() + mid.y, pos.getZ() + mid.z,
                                0, 0, 0);
                    }
                }

                if(has2 && has1) {
                    double catalystMidpoint = 1.75 + CirclePowerBlockEntityRenderer.getReagent2BobHeight(level, 0.5f);

                    //upper lightning
                    if(level.getGameTime() % 20 == 0) {
                        double top = catalystMidpoint + 0.546875;

                        int total = 3;
                        for(int i=0; i<total; i++) {
                            double theta = (Math.PI * 2) * i / (double) total;

                            double radianWiggle = ((Math.PI * 2d / (double)total) * (r.nextDouble() - 0.5));

                            double x = Math.cos(theta + radianWiggle);
                            double z = Math.sin(theta + radianWiggle);

                            Vector3 inner = new Vector3(0.5, top, 0.5);
                            Vector3 outer = new Vector3(0.5, 0.75, 0.5).add(new Vector3(x, 0, z).scale(1.9f));

                            for(int j=0; j<2; j++) {
                                level.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                                .setMaxAge(10 + r.nextInt(15))
                                        /*.setColor(150 + r.nextInt(75), 150 + r.nextInt(75), 150 + r.nextInt(75))*/,
                                        pos.getX() + inner.x, pos.getY() + inner.y, pos.getZ() + inner.z,
                                        pos.getX() + outer.x, pos.getY() + outer.y, pos.getZ() + outer.z);
                            }

                            //Impact cloud
                            level.addParticle(new MAParticleType(ParticleInit.FROST.get()).setAgePadding(5)
                                            .setMaxAge(40 + r.nextInt(30)).setScale(0.15f + r.nextFloat() * 0.15f)
                                            .setColor(10, 50, 120, 48),
                                    pos.getX() + outer.x, pos.getY() + outer.y, pos.getZ() + outer.z,
                                    0, 0, 0);
                        }
                    }

                    //lower lightning
                    if(level.getGameTime() % 14 == 7) {
                        double bottom = catalystMidpoint - 0.171875;

                        int total = 2;
                        for(int i=0; i<total; i++) {
                            double radianWiggle = ((Math.PI * 2) / (double) total) * (r.nextDouble() - 0.5);

                            double x = Math.cos((Math.PI * 2) * i / (double) total + radianWiggle);
                            double z = Math.sin((Math.PI * 2) * i / (double) total + radianWiggle);

                            Vector3 inner = new Vector3(0.5, bottom, 0.5);
                            Vector3 outer = new Vector3(0.5, 0.75, 0.5).add(new Vector3(x, 0, z).scale(1.9f));

                            for(int j=0; j<2; j++) {
                                level.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                                .setMaxAge(15 + r.nextInt(15)),
                                        pos.getX() + inner.x, pos.getY() + inner.y, pos.getZ() + inner.z,
                                        pos.getX() + outer.x, pos.getY() + outer.y, pos.getZ() + outer.z);
                            }

                            //Impact cloud
                            level.addParticle(new MAParticleType(ParticleInit.FROST.get()).setAgePadding(5)
                                            .setMaxAge(40 + r.nextInt(30)).setScale(0.15f + r.nextFloat() * 0.15f)
                                            .setColor(10, 50, 120, 48),
                                    pos.getX() + outer.x, pos.getY() + outer.y, pos.getZ() + outer.z,
                                    0, 0, 0);
                        }
                    }
                }

                if(has3) {
                    Vector3 center = new Vector3(
                            entity.getBlockPos().getX() + 0.5,
                            entity.getBlockPos().getY() + 3.25 + CirclePowerBlockEntityRenderer.getReagent3BobHeight(level, 0.5f),
                            entity.getBlockPos().getZ() + 0.5);

                    int colorIndex = r.nextInt(6);

                    for (int i = 0; i < 2; i++) {
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.6f);
                        level.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(SIX_STEP_PARTICLE_COLORS[colorIndex][0], SIX_STEP_PARTICLE_COLORS[colorIndex][1], SIX_STEP_PARTICLE_COLORS[colorIndex][2], 255)
                                        .setScale(0.18f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);

                        level.addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                        .setScale(0.02f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }
                }
            }

            return;
        }

        kickstart(entity);

        if(entity.ENERGY_STORAGE.getEnergyStored() < getEnergyLimit(entity)) {
            processReagent(level, pos, state, entity, 1);
            processReagent(level, pos, state, entity, 2);
            processReagent(level, pos, state, entity, 3);
            processReagent(level, pos, state, entity, 4);

            generatePower(entity);
        }

        //Charge anything in the inline charging slot
        {
            ItemStack toCharge = entity.itemHandler.getStackInSlot(SLOT_RECHARGE);
            if(!toCharge.isEmpty()) {
                LazyOptional<IEnergyStorage> energyCapability = toCharge.getCapability(ForgeCapabilities.ENERGY);

                //COMMENT THIS OUT DURING PARTICLE WORK
                energyCapability.ifPresent(itemCap -> {
                    int energyNeeded = itemCap.getMaxEnergyStored() - itemCap.getEnergyStored();
                    int energyExtracted = entity.ENERGY_STORAGE.extractEnergy(energyNeeded, false);
                    itemCap.receiveEnergy(energyExtracted, false);
                });
            }
        }

        //Charge anything routed to this Circle of Power from a Charging Talisman
        for(int i=entity.itemsForRemoteCharging.size()-1; i>=0; i--) {
            ItemStack toCharge = entity.itemsForRemoteCharging.get(i);
            LazyOptional<IEnergyStorage> energyCapability = toCharge.getCapability(ForgeCapabilities.ENERGY);

            energyCapability.ifPresent(itemCap -> {
                int energyNeeded = itemCap.getMaxEnergyStored() - itemCap.getEnergyStored();
                int energyExtracted = entity.ENERGY_STORAGE.extractEnergy(energyNeeded, false);
                itemCap.receiveEnergy(energyExtracted, false);
            });

            if(toCharge.isEmpty()) {
                entity.itemsForRemoteCharging.remove(i);
            }
        }
    }

    private void handleAnimationDrivers() {
        boolean
                has1 = hasReagent(1),
                has4 = hasReagent(4);

        if(has1) {
            circleFillPercent = Math.min(1, circleFillPercent + CIRCLE_FILL_RATE);
        } else {
            circleFillPercent = Math.max(0, circleFillPercent - CIRCLE_FILL_RATE);
        }

        if(has4) {
            auxiliaryInnerCircleFillPercent = Math.min(1, auxiliaryInnerCircleFillPercent + CIRCLE_FILL_RATE);
        } else {
            auxiliaryInnerCircleFillPercent = Math.max(0, auxiliaryInnerCircleFillPercent - CIRCLE_FILL_RATE);
        }

        if(has1 && auxiliaryInnerCircleFillPercent >= 1) {
            auxiliaryOuterCircleFillPercent = Math.min(1, auxiliaryOuterCircleFillPercent + CIRCLE_FILL_RATE);
        } else {
            auxiliaryOuterCircleFillPercent = Math.max(0, auxiliaryOuterCircleFillPercent - CIRCLE_FILL_RATE);
        }
    }

    public boolean hasReagent(int pReagentID) {
        if(pReagentID == 1) {
            if(!itemHandler.getStackInSlot(SLOT_REAGENT_1).isEmpty()) {
                return true;
            } else
                return progressReagentTier1 > 0;
        }
        else if(pReagentID == 2) {
            if(!itemHandler.getStackInSlot(SLOT_REAGENT_2).isEmpty()) {
                return true;
            } else
                return progressReagentTier2 > 0;
        }
        else if(pReagentID == 3) {
            if(!itemHandler.getStackInSlot(SLOT_REAGENT_3).isEmpty()) {
                return true;
            } else
                return progressReagentTier3 > 0;
        }
        else if(pReagentID == 4) {
            if(!itemHandler.getStackInSlot(SLOT_REAGENT_4).isEmpty()) {
                return true;
            } else
                return progressReagentTier4 > 0;
        }
        return false;
    }

    private static void kickstart(CirclePowerBlockEntity entity) {
        if(getProgressByTier(entity, 1) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_1).getItem() == REAGENT_TIER1) {
            entity.itemHandler.setStackInSlot(SLOT_REAGENT_1, ItemStack.EMPTY);
            entity.incrementProgress(1);
            entity.syncAndSave();
        } else if(getProgressByTier(entity, 1) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_1).getItem() == ItemRegistry.DEBUG_ORB.get()) {
            entity.incrementProgress(1);
        }

        if(getProgressByTier(entity, 2) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_2).getItem() == REAGENT_TIER2) {
            entity.itemHandler.setStackInSlot(SLOT_REAGENT_2, ItemStack.EMPTY);
            entity.incrementProgress(2);
            entity.syncAndSave();
        } else if(getProgressByTier(entity, 2) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_2).getItem() == ItemRegistry.DEBUG_ORB.get()) {
            entity.incrementProgress(2);
        }

        if(getProgressByTier(entity, 3) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_3).getItem() == REAGENT_TIER3) {
            entity.itemHandler.setStackInSlot(SLOT_REAGENT_3, ItemStack.EMPTY);
            entity.incrementProgress(3);
            entity.syncAndSave();
        } else if(getProgressByTier(entity, 3) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_3).getItem() == ItemRegistry.DEBUG_ORB.get()) {
            entity.incrementProgress(3);
        }

        if(getProgressByTier(entity, 4) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_4).getItem() == REAGENT_TIER4) {
            entity.itemHandler.setStackInSlot(SLOT_REAGENT_4, ItemStack.EMPTY);
            entity.incrementProgress(4);
            entity.syncAndSave();
        } else if(getProgressByTier(entity, 4) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_4).getItem() == ItemRegistry.DEBUG_ORB.get()) {
            entity.incrementProgress(4);
        }
    }

    private static void processReagent(Level level, BlockPos pos, BlockState state, CirclePowerBlockEntity entity, int tier) {
        if(entity.hasReagent(tier) || getProgressByTier(entity, tier) > 0) {
            entity.incrementProgress(tier);

            if(getProgressByTier(entity, tier) >= getMaxProgressByTier(tier)) {
                ejectWaste(tier, level, entity);
                entity.resetProgress(tier);
            }
            setChanged(level, pos, state);
        }
    }

    /* GENERATOR REAGENT USE LOGIC */

    private static void ejectWaste(int tier, Level level, CirclePowerBlockEntity entity) {
        if(entity.itemHandler.getStackInSlot(tier - 1).getItem() == ItemRegistry.DEBUG_ORB.get())
            return;

        ItemStack wasteInSlot = null;
        ItemStack wasteProduct = null;
        Item wasteItem = null;
        boolean eject = false;
        int slot = -1;
        int slotLimit = -1;

        if(tier == 1) {
            slot = WASTE_REAGENT_1;
            wasteItem = WASTE_TIER1;
        } else if(tier == 2) {
            slot = WASTE_REAGENT_2;
            wasteItem = WASTE_TIER2;
        } else if(tier == 3) {
            slot = WASTE_REAGENT_3;
            wasteItem = WASTE_TIER3;
        } else if(tier == 4) {
            slot = WASTE_REAGENT_4;
            wasteItem = WASTE_TIER4;
        } else {
            return;
        }
        wasteProduct = new ItemStack(wasteItem, tier == 4 ? 6 : 1);
        wasteInSlot = entity.itemHandler.getStackInSlot(slot);
        slotLimit = wasteProduct.getItem().getMaxStackSize(wasteProduct);

        if (wasteInSlot.isEmpty()) {
            entity.itemHandler.setStackInSlot(slot, wasteProduct);
            entity.syncAndSave();
        } else if (wasteInSlot.getCount() < slotLimit) {
            wasteInSlot.grow(tier == 4 ? 6 : 1);
            entity.itemHandler.setStackInSlot(slot, wasteInSlot);
            entity.syncAndSave();
        } else {
            eject = true;
        }

        if(eject)
            Containers.dropItemStack(level, entity.worldPosition.getX(), entity.worldPosition.getY()+0.125, entity.worldPosition.getZ(), wasteProduct);
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

    private void resetProgress(int tier) {
        if(tier == 1) progressReagentTier1 =0;
        else if(tier == 2) progressReagentTier2 = 0;
        else if(tier == 3) progressReagentTier3 = 0;
        else if(tier == 4) progressReagentTier4 = 0;
    }

    private void incrementProgress(int tier) {
        if(tier == 1) progressReagentTier1++;
        else if(tier == 2) progressReagentTier2++;
        else if(tier == 3) progressReagentTier3++;
        else if(tier == 4) progressReagentTier4++;
    }

    public static int getMaxProgressByTier(int tier) {
        if(tier == 1) return  maxProgressReagentTier1;
        else if(tier == 2) return  maxProgressReagentTier2;
        else if(tier == 3) return  maxProgressReagentTier3;
        else if(tier == 4) return  maxProgressReagentTier4;
        return -1;
    }

    public static int getProgressByTier(CirclePowerBlockEntity entity, int tier) {
        if(tier == 1) return  entity.progressReagentTier1;
        else if(tier == 2) return  entity.progressReagentTier2;
        else if(tier == 3) return  entity.progressReagentTier3;
        else if(tier == 4) return  entity.progressReagentTier4;
        return -1;
    }

    public static int getEnergyLimit(CirclePowerBlockEntity entity) {
        int currentEnergy = entity.ENERGY_STORAGE.getEnergyStored();

        int reagentCount = 0;
        if(entity.progressReagentTier1 > 0) reagentCount++;
        if(entity.progressReagentTier2 > 0) reagentCount++;
        if(entity.progressReagentTier3 > 0) reagentCount++;
        if(entity.progressReagentTier4 > 0) reagentCount++;

        return getGenRate(reagentCount) * Config.circlePowerBuffer;
    }

    /* FE STUFF */

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    private static void generatePower(CirclePowerBlockEntity entity) {
        int reagentCount = 0;
        int cap;
        int currentEnergy = entity.ENERGY_STORAGE.getEnergyStored();
        if(entity.progressReagentTier1 > 0) reagentCount++;
        if(entity.progressReagentTier2 > 0) reagentCount++;
        if(entity.progressReagentTier3 > 0) reagentCount++;
        if(entity.progressReagentTier4 > 0) reagentCount++;

        int genRate = getGenRate(reagentCount);

        cap = genRate * Config.circlePowerBuffer;
        entity.ENERGY_STORAGE.receiveEnergy(genRate, false);
        if (currentEnergy + genRate > cap) entity.ENERGY_STORAGE.setEnergy(cap);
    }

    public static int getGenRate(int reagentCount) {
        int genRate = 0;
        if(reagentCount == 1) genRate = Config.circlePowerGen1Reagent;
        else if(reagentCount == 2) genRate = Config.circlePowerGen2Reagent;
        else if(reagentCount == 3) genRate = Config.circlePowerGen3Reagent;
        else if(reagentCount == 4) genRate = Config.circlePowerGen4Reagent;
        return genRate;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3,5,3));
    }

    @Override
    public void destroyRouters() {
        CirclePowerBlock.destroyRouters(getLevel(), getBlockPos());
    }
}
