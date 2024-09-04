package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IRequiresRouterCleanupOnDestruction;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.items.ItemStackHandler;

import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractDirectionalPluginBlockEntity extends BlockEntity implements IRequiresRouterCleanupOnDestruction, IEldrinConsumerTile, IShlorpReceiver, IMateriaProvisionRequester {
    protected Player owner;
    protected UUID ownerUUID;
    protected boolean
        drewEldrinThisCycle = false, drewEssentiaThisCycle = false, metAuxiliaryRequirementsThisCycle = false, isPaused = false;
    protected int
        powerLevel = 1, remainingCycleTime = 3, storedMateria = 0, remainingEssentiaForSatisfaction = 1;
    protected float
        remainingEldrinForSatisfaction = 1;
    protected ItemStackHandler itemHandler;

    public AbstractDirectionalPluginBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
        this.saveAdditional(this.getUpdateTag());
    }

    public Player getOwner() {
        if(owner != null) return owner;
        else if(ownerUUID != null && getLevel() != null) {
            return getLevel().getPlayerByUUID(ownerUUID);
        }
        return null;
    }

    public ICanTakePlugins getTargetMachine() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(facing));
        if(be != null) {
            if (be instanceof ICanTakePlugins ictp) return ictp;
        }
        return null;
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public abstract void processCompletedOperation(int pCyclesCompleted);

    @Override
    public void destroyRouters() {
        getLevel().destroyBlock(getBlockPos().above(), true);
    }

    public int getStoredMateria() {
        return storedMateria;
    }

    public boolean getIsSatisfied() {
        return drewEldrinThisCycle || drewEssentiaThisCycle;
    }

    public boolean getIsDoubleSatisfied() {
        return drewEldrinThisCycle && drewEssentiaThisCycle;
    }

    public boolean getPaused() {
        return isPaused;
    }

    public boolean isAuxiliaryRequirementSatisfied() {
        return metAuxiliaryRequirementsThisCycle;
    }

    public void satisfyAuxiliaryRequirements() {
        metAuxiliaryRequirementsThisCycle = true;
    }

    public void setPaused(boolean pNewPauseState) {
        isPaused = pNewPauseState;
        syncAndSave();
    }

    public int getPowerLevel() {
        return this.powerLevel;
    }

    public void increasePowerLevel(Function<IDs, Integer> pVarFunc) {
        this.powerLevel = Math.min(pVarFunc.apply(IDs.MAX_POWER_LEVEL), this.powerLevel + 1);
    }

    public void decreasePowerLevel() {
        this.powerLevel = Math.max(1, this.powerLevel - 1);
    }

    public void setPowerLevel(int pPowerLevel) {
        this.powerLevel = pPowerLevel;
    }

    public int getScaledCycleTime() {
        return remainingCycleTime * 21 / ((drewEssentiaThisCycle && drewEldrinThisCycle) ? Config.actuatorDoubleSuppliedPeriod : Config.actuatorSingleSuppliedPeriod);
    }

    public static <T extends BlockEntity> boolean tick(Level level, BlockPos pos, BlockState blockState, T t, Function<IDs, Integer> pVarFunc) {
        boolean changed = false;

        if(t instanceof AbstractDirectionalPluginBlockEntity entity) {
            //Try inserting materia
            if(!level.isClientSide()) {
                if (entity.storedMateria < Config.actuatorMateriaBufferMaximum) {
                    ItemStack insertionStack = entity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_ESSENTIA_INSERTION));
                    ItemStack bottleStack = entity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_BOTTLES));

                    if (!insertionStack.isEmpty()) {
                        if (bottleStack.getCount() < entity.itemHandler.getSlotLimit(pVarFunc.apply(IDs.SLOT_BOTTLES))) {
                            if(!InventoryHelper.isMateriaUnbottled(insertionStack)){
                                if (bottleStack.isEmpty())
                                    bottleStack = new ItemStack(Items.GLASS_BOTTLE);
                                else
                                    bottleStack.grow(1);
                            }

                            insertionStack.shrink(1);
                            entity.storedMateria += Config.actuatorMateriaUnitsPerDram;

                            entity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_ESSENTIA_INSERTION), insertionStack);
                            entity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_BOTTLES), bottleStack);
                            changed = true;
                        }
                    }
                }
            } else {
                if(!entity.isPaused) {
                    //Tick down the cycle time for GUI reasons
                    entity.remainingCycleTime = Math.max(-1, entity.remainingCycleTime - 1);
                }
            }
        }

        return changed;
    }

    public static boolean delegatedTick(Level level, BlockPos pos, BlockState state, AbstractDirectionalPluginBlockEntity entity, Function<IDs, Integer> pVarFunc, Function<Void, Affinity> pGetAffinity, Function<AbstractDirectionalPluginBlockEntity, Integer> pGetPowerDraw, Function<AbstractDirectionalPluginBlockEntity, Boolean> pAuxiliaryRequirementHandler) {
        Player ownerCheck = entity.getOwner();
        int powerDraw = pGetPowerDraw.apply(entity);
        boolean changed = false;

        if (ownerCheck != null) {
            //Consume Eldrin for this cycle if there's any outstanding
            float eldrinConsumption = entity.consume(ownerCheck, pos, pos.getCenter(), pGetAffinity.apply(null), Math.min(powerDraw, entity.remainingEldrinForSatisfaction));
            if(eldrinConsumption > 0)
                entity.remainingEldrinForSatisfaction -= eldrinConsumption;

            //Consume Essentia for this cycle if there's any outstanding
            if (entity.remainingEssentiaForSatisfaction > 0) {
                int materiaConsumption = Math.min(entity.remainingEssentiaForSatisfaction, entity.storedMateria);
                if(materiaConsumption > 0) {
                    entity.storedMateria -= materiaConsumption;
                    entity.remainingEssentiaForSatisfaction -= materiaConsumption;
                    changed = true;
                }
            }

            //Handle auxiliary requirements for this cycle
            changed = changed & pAuxiliaryRequirementHandler.apply(entity);
        }

        if(!entity.isPaused) {
            //Cycle processing
            if(entity.remainingCycleTime <= 0) {
                if (entity.remainingEldrinForSatisfaction <= 0) {
                    entity.remainingEldrinForSatisfaction = powerDraw;
                    entity.drewEldrinThisCycle = true;
                } else {
                    entity.drewEldrinThisCycle = false;
                }

                if (entity.remainingEssentiaForSatisfaction <= 0) {
                    entity.remainingEssentiaForSatisfaction = powerDraw;
                    entity.drewEssentiaThisCycle = true;
                } else {
                    entity.drewEssentiaThisCycle = false;
                }

                if (entity.drewEldrinThisCycle && entity.drewEssentiaThisCycle && entity.metAuxiliaryRequirementsThisCycle) {
                    entity.remainingCycleTime = Config.actuatorDoubleSuppliedPeriod;
                } else if ((entity.drewEldrinThisCycle || entity.drewEssentiaThisCycle) && entity.metAuxiliaryRequirementsThisCycle)
                    entity.remainingCycleTime = Config.actuatorSingleSuppliedPeriod;

                if((entity.drewEldrinThisCycle || entity.drewEssentiaThisCycle) && entity.metAuxiliaryRequirementsThisCycle)
                    changed = true;

                //Aux requirements are set by subclasses mid-cycle
                entity.metAuxiliaryRequirementsThisCycle = false;
            }

            entity.remainingCycleTime = Math.max(-1, entity.remainingCycleTime - 1);
        }

        return changed;
    }

    public enum IDs {
        SLOT_COUNT,
        SLOT_ESSENTIA_INSERTION,
        SLOT_BOTTLES,
        MAX_POWER_LEVEL
    }
}
