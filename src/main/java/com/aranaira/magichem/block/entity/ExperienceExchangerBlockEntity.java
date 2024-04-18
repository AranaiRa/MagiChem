package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.gui.CentrifugeMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.items.ItemInit;
import com.mna.tools.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ExperienceExchangerBlockEntity extends BlockEntity {
    private boolean isPushMode = false;
    private int storedXP;
    public float ringRotation, ringRotationNextTick, crystalRotation, crystalRotationNextTick, crystalBob, crystalBobNextTick;

    private static final int
        RING_ROTATION_PERIOD = 160, CRYSTAL_ROTATION_PERIOD = 300, CRYSTAL_BOB_PERIOD = 200;
    private static final float
        CRYSTAL_BOB_INTENSITY = 0.1f;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get();
        }
    };

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public ExperienceExchangerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.EXPERIENCE_EXCHANGER_BE.get(), pPos, pBlockState);
    }

    public ExperienceExchangerBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    ////////////////////
    // BOILERPLATE CODE
    ////////////////////

    public static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, T t) {
        if(t instanceof ExperienceExchangerBlockEntity eebe) {
            if(pLevel.isClientSide()) {
                handleAnimationDrivers(eebe);
            }

            if(eebe.itemHandler.getStackInSlot(0) == ItemStack.EMPTY) return;

            //particles
            BlockPos pos = eebe.getBlockPos();
            int loopingTime = (int)(pLevel.getGameTime() % 12);
            double theta = ((double) loopingTime / 6.0) * Math.PI + Math.PI / 4.0;
            double scale = 0.2;

            if(eebe.getIsPushMode()) {
                Vector3 shift = new Vector3(Math.cos(theta) * scale, 0, Math.sin(theta) * scale);

                eebe.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                        .setColor(188, 232, 95).setScale(0.2f),
                        pos.getX() + shift.x + 0.5, pos.getY() + 0.8125, pos.getZ() + shift.z + 0.5,
                        0, -0.02f, 0);
            } else {
                Vector3 shift = new Vector3(Math.cos(-theta) * scale, 0, Math.sin(-theta) * scale);

                eebe.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setColor(188, 232, 95).setScale(0.2f),
                        pos.getX() + shift.x + 0.5, pos.getY() + 0.3125, pos.getZ() + shift.z + 0.5,
                        0, 0.02f, 0);
            }

            BlockEntity be = pLevel.getBlockEntity(pPos.below());
            if(be != null) {
                LazyOptional<IFluidHandler> fluidCap = be.getCapability(ForgeCapabilities.FLUID_HANDLER);

                if (fluidCap.isPresent()) {
                    if (eebe.isPushMode) {
                        fluidCap.ifPresent(cap -> {
                            if (eebe.getStoredXP() > 0) {
                                FluidStack attempt = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), cap.getTankCapacity(0));
                                int capacity = cap.fill(attempt, IFluidHandler.FluidAction.SIMULATE);
                                int maxPoints = capacity / Config.fluidPerXPPoint;
                                if (maxPoints > 0) {
                                    int consumedPoints = Math.min(eebe.getStoredXP(), Math.min(maxPoints, 10));
                                    attempt.setAmount(consumedPoints * Config.fluidPerXPPoint);
                                    cap.fill(attempt, IFluidHandler.FluidAction.EXECUTE);
                                    eebe.updateStoredXP(eebe.getStoredXP() - consumedPoints);
                                }
                            }
                        });
                    } else {
                        fluidCap.ifPresent(cap -> {
                            //Magic number, I don't know if there's a way to configure this
                            int comCapacity = 20000;
                            if (eebe.getStoredXP() < comCapacity) {
                                FluidStack attempt = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), cap.getTankCapacity(0));
                                int contents = cap.drain(attempt, IFluidHandler.FluidAction.SIMULATE).getAmount();
                                int maxInsert = comCapacity - eebe.getStoredXP();
                                if (maxInsert > 0) {
                                    int actualInsert = Math.min(maxInsert, Math.min(contents / Config.fluidPerXPPoint, 10));
                                    attempt.setAmount(actualInsert * Config.fluidPerXPPoint);
                                    cap.drain(attempt, IFluidHandler.FluidAction.EXECUTE);
                                    eebe.updateStoredXP(eebe.getStoredXP() + actualInsert);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return lazyItemHandler.cast();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("storedXP", storedXP);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));

        CompoundTag crystalNBT = itemHandler.getStackInSlot(0).getOrCreateTag();
        if(crystalNBT.contains("memory_crystal_fragment_mode")) {
            isPushMode = crystalNBT.getInt("memory_crystal_fragment_mode") == 1;
        }

        storedXP = nbt.getInt("storedXP");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());

        CompoundTag crystalNBT = itemHandler.getStackInSlot(0).getOrCreateTag();
        if(crystalNBT.contains("memory_crystal_fragment_mode")) {
            isPushMode = crystalNBT.getInt("memory_crystal_fragment_mode") == 1;
        }

        nbt.putInt("storedXP", storedXP);

        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    ////////////////////
    // DATA INTERACTION
    ////////////////////

    public ItemStack setContainedStack(ItemStack pNewStack) {
        if(itemHandler.getStackInSlot(0) == ItemStack.EMPTY) {
            if (pNewStack.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get()) {
                itemHandler.setStackInSlot(0, pNewStack);

                CompoundTag crystalNBT = pNewStack.getOrCreateTag();
                if(crystalNBT.contains("stored_xp"))
                    storedXP = crystalNBT.getInt("stored_xp");
                else
                    storedXP = 0;

                syncAndSave();
                return ItemStack.EMPTY;
            }
        }
        return pNewStack;
    }

    public void ejectStack() {
        ItemStack storedCrystal = itemHandler.getStackInSlot(0).copy();
        if(storedCrystal != ItemStack.EMPTY) {
            CompoundTag crystalNBT = storedCrystal.getOrCreateTag();
            crystalNBT.putInt("stored_xp", storedXP);
            storedCrystal.setTag(crystalNBT);
            storedXP = 0;

            level.addFreshEntity(new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), storedCrystal));
            itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            syncAndSave();
        }
    }

    ////////////////////
    // VFX
    ////////////////////

    public ItemStack getItem() {
        return itemHandler.getStackInSlot(0);
    }

    public int getStoredXP() {
        return storedXP;
    }

    public void updateStoredXP(int pNewValue) {
        storedXP = pNewValue;
    }

    public boolean getIsPushMode() {
        return isPushMode;
    }

    private static void handleAnimationDrivers(ExperienceExchangerBlockEntity pBlockEntity) {
        long gameTime = pBlockEntity.level.getGameTime();

        pBlockEntity.ringRotation = ((float)(gameTime % RING_ROTATION_PERIOD) / RING_ROTATION_PERIOD) * (float)Math.PI * 2;
        pBlockEntity.ringRotationNextTick = ((float)((gameTime + 1) % RING_ROTATION_PERIOD) / RING_ROTATION_PERIOD) * (float)Math.PI * 2;
        if((gameTime + 1) % RING_ROTATION_PERIOD == 0)
            pBlockEntity.ringRotationNextTick += Math.PI * 2;

        pBlockEntity.crystalRotation = ((float)(gameTime % CRYSTAL_ROTATION_PERIOD) / CRYSTAL_ROTATION_PERIOD) * (float)Math.PI * 2;
        pBlockEntity.crystalRotationNextTick = ((float)((gameTime + 1) % CRYSTAL_ROTATION_PERIOD) / CRYSTAL_ROTATION_PERIOD) * (float)Math.PI * 2;
        if((gameTime + 1) % CRYSTAL_ROTATION_PERIOD == 0)
            pBlockEntity.crystalRotationNextTick += Math.PI * 2;

        pBlockEntity.crystalBob = (float)Math.sin(((float)(gameTime % CRYSTAL_BOB_PERIOD) / CRYSTAL_BOB_PERIOD) * (float)Math.PI * 2) * CRYSTAL_BOB_INTENSITY;
        pBlockEntity.crystalBobNextTick = (float)Math.sin(((float)((gameTime + 1) % CRYSTAL_BOB_PERIOD) / CRYSTAL_BOB_PERIOD) * (float)Math.PI * 2) * CRYSTAL_BOB_INTENSITY;
    }
}
