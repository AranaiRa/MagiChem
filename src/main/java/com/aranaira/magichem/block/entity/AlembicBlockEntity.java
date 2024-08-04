package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.AlembicBlock;
import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.gui.AlembicMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.particles.types.movers.ParticleVelocityMover;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class AlembicBlockEntity extends AbstractDistillationBlockEntity implements MenuProvider {
    public static final int
        SLOT_COUNT = 13,
        SLOT_BOTTLES = 0,
        SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 3,
        SLOT_OUTPUT_START = 4, SLOT_OUTPUT_COUNT  = 8,
        GUI_PROGRESS_BAR_WIDTH = 24, GUI_GRIME_BAR_WIDTH = 50,
        DATA_COUNT = 3, DATA_PROGRESS = 0, DATA_GRIME = 1, DATA_REMAINING_HEAT = 2;
    private static final Random r = new Random();

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.ALEMBIC_BE.get(), pos, state);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if((slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)) {
                    isStalled = false;
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == SLOT_BOTTLES)
                    return stack.getItem() == Items.GLASS_BOTTLE;
                if (slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT)
                    return !(stack.getItem() instanceof MateriaItem);
                if (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                    return false;

                return super.isItemValid(slot, stack);
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        return AlembicBlockEntity.this.progress;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(AlembicBlockEntity.this);
                        return grime.getGrime();
                    }
                    case DATA_REMAINING_HEAT: {
                        return AlembicBlockEntity.this.remainingHeat;
                    }
                    default: return -1;
                }
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        AlembicBlockEntity.this.progress = pValue;
                        break;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(AlembicBlockEntity.this);
                        grime.setGrime(pValue);
                        break;
                    }
                    case DATA_REMAINING_HEAT: {
                        AlembicBlockEntity.this.remainingHeat = pValue;
                        break;
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    //////////
    // BOILERPLATE CODE
    //////////

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.alembic");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AlembicMenu(id, inventory, this, this.data);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        return nbt;
    }

    public void packInventoryToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.ALEMBIC.get());
        IGrimeCapability grimeCap = GrimeProvider.getCapability(AlembicBlockEntity.this);

        CompoundTag nbt = new CompoundTag();
        nbt.putInt("grime", grimeCap.getGrime());
        nbt.put("inventory", itemHandler.serializeNBT());

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        itemHandler.deserializeNBT(pInventoryTag);
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    @Override
    public int getGrimeFromData() {
        return data.get(DATA_GRIME);
    }

    @Override
    public int getMaximumGrime() {
        return Config.alembicMaximumGrime;
    }

    @Override
    public int clean() {
        int grimeDetected = GrimeProvider.getCapability(this).getGrime();
        IGrimeCapability grimeCapability = GrimeProvider.getCapability(this);
        grimeCapability.setGrime(0);
        data.set(DATA_GRIME, 0);
        return grimeDetected / Config.grimePerWaste;
    }

    public static int getScaledGrime(int grime) {
        return (GUI_GRIME_BAR_WIDTH * grime) / Config.alembicMaximumGrime;
    }

    @Override
    protected void pushData() {
        this.data.set(DATA_PROGRESS, progress);
        this.data.set(DATA_GRIME, GrimeProvider.getCapability(this).getGrime());
    }

    public int getRemainingHeat() {
        return this.remainingHeat;
    }

    ////////////////////
    // OVERRIDES
    ////////////////////

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(AlembicBlockEntity::getVar);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AlembicBlockEntity pEntity) {
        BlockState stateBelow = pLevel.getBlockState(pPos.below());
        if(stateBelow.getBlock() instanceof AbstractFurnaceBlock) {
            if(pLevel.getBlockState(pPos.below()).getValue(BlockStateProperties.LIT)) {
                pEntity.remainingHeat = Math.max(5, pEntity.remainingHeat);
            }
        }

        AbstractDistillationBlockEntity.tick(pLevel, pPos, pState, pEntity, AlembicBlockEntity::getVar, pEntity::getPoweredOperationTime);

        //Particles
        boolean hasPassiveHeat = pEntity.getBlockState().getValue(AlembicBlock.HAS_PASSIVE_HEAT);
        if(hasPassiveHeat || pEntity.remainingHeat > 0) {
            int particleCount = 0;
            int particleRate = 0;

            if(stateBelow.getBlock() == Blocks.BLAST_FURNACE || stateBelow.getBlock() == Blocks.SMOKER) {
                particleRate = 2;
                particleCount = 4;
            } else if(pEntity.remainingHeat > 0) {
                particleRate = 4;
                particleCount = 2;
            }

            if(particleRate > 0) {
                Vec3 blockPos = new Vec3(pEntity.getBlockPos().getX() + 0.25, pEntity.getBlockPos().getY() + 0.0, pEntity.getBlockPos().getZ() + 0.25);
                if(pLevel.getGameTime() % particleRate == 0) {
                    for (int i = 0; i < particleCount; i++) {
                        Vec3 particlePos = new Vec3(r.nextFloat(), r.nextFloat(), r.nextFloat()).normalize().scale(0.575f).add(blockPos);
                        Vec3 particleEnd = new Vec3(particlePos.x, particlePos.y + 1, particlePos.z);

                        pLevel.addParticle(new MAParticleType(ParticleInit.DUST_LERP.get())
                                        .setScale(0.05f).setMaxAge(80)
                                        .setMover(new ParticleLerpMover(particlePos.x, particlePos.y, particlePos.z, particleEnd.x, particleEnd.y, particleEnd.z))
                                        .setColor(128, 128, 128, 40),
                                particlePos.x, particlePos.y, particlePos.z,
                                0, 0, 0);
                    }
                }
            }
        }
    }

    public static int getVar(IDs pID) {
        return switch(pID) {
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case SLOT_INPUT_START -> SLOT_INPUT_START;
            case SLOT_INPUT_COUNT -> SLOT_INPUT_COUNT;
            case SLOT_OUTPUT_START -> SLOT_OUTPUT_START;
            case SLOT_OUTPUT_COUNT -> SLOT_OUTPUT_COUNT;

            case DATA_PROGRESS -> DATA_PROGRESS;
            case DATA_GRIME -> DATA_GRIME;
            case DATA_REMAINING_HEAT -> DATA_REMAINING_HEAT;

            case MODE_USES_RF -> 0;

            case GUI_PROGRESS_BAR_WIDTH -> GUI_PROGRESS_BAR_WIDTH;
            case GUI_GRIME_BAR_WIDTH -> GUI_GRIME_BAR_WIDTH;

            case CONFIG_BASE_EFFICIENCY -> Config.alembicEfficiency;
            case CONFIG_MAX_GRIME -> Config.alembicMaximumGrime;
            case CONFIG_OPERATION_TIME -> Config.alembicOperationTime;
            case CONFIG_GRIME_ON_SUCCESS -> Config.alembicGrimeOnSuccess;
            case CONFIG_GRIME_ON_FAILURE -> Config.alembicGrimeOnFailure;

            default -> -1;
        };
    }
}
