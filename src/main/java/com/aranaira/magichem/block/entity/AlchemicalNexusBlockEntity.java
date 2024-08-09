package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.AlchemicalNexusBlock;
import com.aranaira.magichem.block.DistilleryBlock;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaProcessorBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.block.entity.renderer.AlchemicalNexusBlockEntityRenderer;
import com.aranaira.magichem.block.entity.routers.AlchemicalNexusRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.AlchemicalNexusRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.DistilleryRouterType;
import com.aranaira.magichem.foundation.enums.ShlorpParticleMode;
import com.aranaira.magichem.gui.AlchemicalNexusMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.AlchemicalInfusionRecipe;
import com.aranaira.magichem.registry.*;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.items.ItemInit;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AlchemicalNexusBlockEntity extends AbstractMateriaProcessorBlockEntity implements MenuProvider, ICanTakePlugins, IFluidHandler {

    protected ItemStackHandler itemHandler;
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.of(() -> this);

    protected FluidStack containedSlurry = FluidStack.EMPTY;
    protected AlchemicalInfusionRecipe currentRecipe;
    protected ContainerData data;
    protected AlchemicalNexusAnimSpec cachedSpec;
    protected int
        progress = 0, pluginLinkageCountdown = 3, animStage = 0, craftingStage = 0, powerLevel = 1, shlorpIndex = 0, remainingFluidForSatisfaction = 0;
    protected boolean
        isStalled = false, doDeferredRecipeLinkages = false;
    protected Random r = new Random();
    protected List<DirectionalPluginBlockEntity> pluginDevices = new ArrayList<>();

    public static final int
            FLUID_BAR_HEIGHT = 88,
            SLOT_COUNT = 17,
            SLOT_MARKS = 0, SLOT_PROGRESS_HOLDER = 1, SLOT_RECIPE = 2,
            SLOT_INPUT_START = 3, SLOT_INPUT_COUNT = 5, SLOT_OUTPUT_START = 8, SLOT_OUTPUT_COUNT = 9,
            DATA_COUNT = 5,
            DATA_PROGRESS = 0, DATA_ANIM_STAGE = 1, DATA_CRAFTING_STAGE = 2, DATA_POWER_LEVEL = 3, DATA_FLUID_NEEDED = 4,
            ANIM_STAGE_IDLE = 0,
            ANIM_STAGE_SHLORPS = 1, ANIM_STAGE_CRAFTING = 2, ANIM_STAGE_CRAFTING_IDLE = 3,
            ANIM_STAGE_RAMP_SPEEDUP = 11, ANIM_STAGE_RAMP_CIRCLE = 12, ANIM_STAGE_RAMP_CRAFTING = 13, ANIM_STAGE_RAMP_CRAFTING_SPEEDUP = 14, ANIM_STAGE_RAMP_CRAFTING_CIRCLE = 15,
            ANIM_STAGE_CANCEL_SPEEDUP = 21, ANIM_STAGE_CANCEL_CIRCLE = 22, ANIM_STAGE_CANCEL_CRAFTING = 23, ANIM_STAGE_CANCEL_CRAFTING_ADVANCED = 24, ANIM_STAGE_CANCEL_CRAFTING_SPEEDUP = 25, ANIM_STAGE_CANCEL_CRAFTING_CIRCLE = 26;

    public static final float
            CRYSTAL_SPEED_MIN = 0.75f, CRYSTAL_SPEED_MAX = 20.0f,
            CRYSTAL_BOB_HEIGHT_MAX = 0.125f, CRYSTAL_BOB_PERIOD = 90f,
            ITEM_SPEED_MIN = 0.0375f, ITEM_SPEED_MAX = 1.5f, ITEM_SCALE_START = 6f, ITEM_SCALE_END = 0f;
    public float
            crystalAngle = 0f, crystalRotSpeed = CRYSTAL_SPEED_MIN,
            itemAngle = 0f, itemRotSpeed = ITEM_SPEED_MIN, itemScale = 7f,
            reductionRate = 0.0f;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public AlchemicalNexusBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ALCHEMICAL_NEXUS_BE.get(), pPos, pBlockState);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                if(slot == SLOT_RECIPE) {
                    currentRecipe = AlchemicalInfusionRecipe.getInfusionRecipe(level, getStackInSlot(SLOT_RECIPE));
                    setChanged();
                }
                if((slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)) {
                    isStalled = false;
                }
                if(slot == SLOT_PROGRESS_HOLDER && !getLevel().isClientSide()) {
                    ItemStack stackInSlot = getStackInSlot(slot);
                    if(stackInSlot.isEmpty()) {
                        //Switch isn't working here for some reason, onto the ugly if chain!
                        if(animStage == ANIM_STAGE_RAMP_SPEEDUP)
                            animStage = ANIM_STAGE_CANCEL_SPEEDUP;
                        else if(animStage == ANIM_STAGE_RAMP_CIRCLE || animStage == ANIM_STAGE_SHLORPS)
                            animStage = ANIM_STAGE_CANCEL_CIRCLE;
                        else if(animStage == ANIM_STAGE_RAMP_CRAFTING || animStage == ANIM_STAGE_CRAFTING)
                            animStage = ANIM_STAGE_CANCEL_CRAFTING;
                        else if(animStage == ANIM_STAGE_CRAFTING_IDLE) {
                            craftingStage--;
                            animStage = ANIM_STAGE_CANCEL_CRAFTING_CIRCLE;
                        }
                        else if(animStage == ANIM_STAGE_RAMP_CRAFTING_CIRCLE)
                            animStage = ANIM_STAGE_CANCEL_CRAFTING_CIRCLE;
                        else if(animStage == ANIM_STAGE_RAMP_CRAFTING_SPEEDUP)
                            animStage = ANIM_STAGE_CANCEL_CRAFTING_SPEEDUP;

                        syncAndSave();
                    }
                }
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_MARKS)
                    return stack.getItem() == ItemInit.RUNE_MARKING.get() || stack.getItem() == ItemInit.BOOK_MARKS.get();
                else if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    if(currentRecipe == null)
                        return true;
                    InfusionStage stage = currentRecipe.getStages(false).get(craftingStage);

                    if((slot - SLOT_INPUT_START) >= stage.componentItems.size())
                        return false;
                    ItemStack component = stage.componentItems.get(slot - SLOT_INPUT_START);
                    return stack.getItem().equals(component.getItem());
                }
                else if(slot == SLOT_RECIPE || slot == SLOT_PROGRESS_HOLDER || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT))
                    return false;

                return super.isItemValid(slot, stack);
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case DATA_PROGRESS -> progress;
                    case DATA_ANIM_STAGE -> animStage;
                    case DATA_CRAFTING_STAGE -> craftingStage;
                    case DATA_POWER_LEVEL -> powerLevel;
                    case DATA_FLUID_NEEDED -> remainingFluidForSatisfaction;
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_PROGRESS -> AlchemicalNexusBlockEntity.this.progress = pValue;
                    case DATA_ANIM_STAGE -> AlchemicalNexusBlockEntity.this.animStage = pValue;
                    case DATA_CRAFTING_STAGE -> AlchemicalNexusBlockEntity.this.craftingStage = pValue;
                    case DATA_POWER_LEVEL -> AlchemicalNexusBlockEntity.this.powerLevel = pValue;
                    case DATA_FLUID_NEEDED -> AlchemicalNexusBlockEntity.this.remainingFluidForSatisfaction = pValue;
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    ////////////////////
    // BOILERPLATE CODE
    ////////////////////

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        } else if(cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> this);
        currentRecipe = AlchemicalInfusionRecipe.getInfusionRecipe(level, itemHandler.getStackInSlot(SLOT_RECIPE));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("animationStage", this.animStage);
        nbt.putInt("craftingStage", this.craftingStage);
        nbt.putInt("remainingFluidForSatisfaction", this.remainingFluidForSatisfaction);
        nbt.putInt("powerLevel", this.powerLevel);
        nbt.putInt("fluidContents", this.containedSlurry.getAmount());
        nbt.putFloat("reductionRate", this.reductionRate);

        nbt.putInt("numberOfDemands", satisfactionDemands.size());
        for(int i=0; i<satisfactionDemands.size(); i++) {
            nbt.putString("demandType"+i, ForgeRegistries.ITEMS.getKey(satisfactionDemands.get(i).getFirst()).toString());
            nbt.putInt("demandCount"+i, satisfactionDemands.get(i).getSecond());
            nbt.putBoolean("demandTransit"+i, satisfactionDemands.get(i).getThird());
        }

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        animStage = nbt.getInt("animationStage");
        craftingStage = nbt.getInt("craftingStage");
        remainingFluidForSatisfaction = nbt.getInt("remainingFluidForSatisfaction");
        powerLevel = nbt.getInt("powerLevel");
        reductionRate = nbt.getFloat("reductionRate");
        int fluidContents = nbt.getInt("fluidContents");
        if(fluidContents > 0)
            containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), fluidContents);
        else
            containedSlurry = FluidStack.EMPTY;

        satisfactionDemands.clear();
        for(int i=0; i<nbt.getInt("numberOfDemands"); i++) {
            Item query = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("demandType"+i)));
            if(query instanceof MateriaItem mi)
                satisfactionDemands.add(new Triplet<>(
                        mi,
                        nbt.getInt("demandCount"+i),
                        nbt.getBoolean("demandTransit"+i)
                ));
        }

        doDeferredRecipeLinkages = true;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("animationStage", this.animStage);
        nbt.putInt("craftingStage", this.craftingStage);
        nbt.putInt("remainingFluidForSatisfaction", this.remainingFluidForSatisfaction);
        nbt.putInt("powerLevel", this.powerLevel);
        nbt.putFloat("reductionRate", this.reductionRate);
        if(containedSlurry.isEmpty())
            nbt.putInt("fluidContents", 0);
        else
            nbt.putInt("fluidContents", containedSlurry.getAmount());

        nbt.putInt("numberOfDemands", satisfactionDemands.size());
        for(int i=0; i<satisfactionDemands.size(); i++) {
            nbt.putString("demandType"+i, ForgeRegistries.ITEMS.getKey(satisfactionDemands.get(i).getFirst()).toString());
            nbt.putInt("demandCount"+i, satisfactionDemands.get(i).getSecond());
            nbt.putBoolean("demandTransit"+i, satisfactionDemands.get(i).getThird());
        }

        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.alchemical_nexus");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AlchemicalNexusMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    public void packInventoryToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.ALCHEMICAL_NEXUS.get());

        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        itemHandler.deserializeNBT(pInventoryTag);
    }

    ////////////////////
    // CRAFTING HANDLERS
    ////////////////////

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, E e) {
        if(e instanceof AlchemicalNexusBlockEntity anbe) {

            if(pLevel.isClientSide()) {
                anbe.handleAnimationDrivers();
                anbe.spawnParticles();
            } else {
                anbe.reductionRate = 0;
                for (DirectionalPluginBlockEntity dpbe : anbe.pluginDevices) {
                    if (dpbe instanceof ActuatorArcaneBlockEntity arcane) {
                        ActuatorArcaneBlockEntity.delegatedTick(pLevel, pPos, pBlockState, arcane, true);
                        float newReductionRate = arcane.getSlurryReductionRate() / 100f;
                        if(newReductionRate != anbe.reductionRate) {
                            anbe.reductionRate = newReductionRate;
                            anbe.syncAndSave();
                        }
                    }
                }
            }

            if(anbe.doDeferredRecipeLinkages) {
                if (!anbe.itemHandler.getStackInSlot(SLOT_RECIPE).isEmpty() && anbe.currentRecipe == null) {
                    anbe.currentRecipe = AlchemicalInfusionRecipe.getInfusionRecipe(pLevel, anbe.itemHandler.getStackInSlot(SLOT_RECIPE));
                }
                if(anbe.animStage != ANIM_STAGE_IDLE) {
                    anbe.cacheAnimSpec(!anbe.getLevel().isClientSide());
                }
                anbe.doDeferredRecipeLinkages = false;
                anbe.syncAndSave();
            }

            if(anbe.animStage == ANIM_STAGE_IDLE) {
                //Just sit here and do nothing if we have no recipe
                if(anbe.currentRecipe != null)
                {
                    //Check if all the items are present
                    if (anbe.hasAllRecipeItemsForCurrentStage()) {
                        if (anbe.getContentsOfOutputSlots().canAddItem(anbe.currentRecipe.getAlchemyObject())) {
                            int experienceCost = anbe.currentRecipe.getStages(false).get(anbe.craftingStage).experience * Config.fluidPerXPPoint;
                            int fluidCost = experienceCost + getBaseExperienceCostPerStage(anbe.getPowerLevel());

                            anbe.animStage = ANIM_STAGE_RAMP_SPEEDUP;
                            anbe.cacheAnimSpec(true);
                            anbe.remainingFluidForSatisfaction = Math.round((float)fluidCost * (1f - anbe.reductionRate));
                            anbe.syncAndSave();
                        }
                    }
                }
            }
            else if(anbe.animStage == ANIM_STAGE_RAMP_SPEEDUP) {
                anbe.incrementProgress();

                if(!anbe.hasAllRecipeItemsForCurrentStage()) {
                    anbe.animStage = ANIM_STAGE_CANCEL_SPEEDUP;
                }

                if(anbe.progress > anbe.cachedSpec.ticksInRampSpeedup) {
                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_RAMP_CIRCLE;
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_CANCEL_SPEEDUP) {
                anbe.decrementProgress();

                if(anbe.progress <= 0) {
                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_IDLE;
                    anbe.craftingStage = 0;
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_RAMP_CIRCLE) {
                anbe.incrementProgress();

                if(!anbe.hasAllRecipeItemsForCurrentStage()) {
                    anbe.animStage = ANIM_STAGE_CANCEL_CIRCLE;
                }

                if(anbe.progress > anbe.cachedSpec.ticksInRampCircle) {
                    //check to see if we have enough experience
                    anbe.progress = anbe.cachedSpec.ticksInRampCircle;
                    int amountInTank = anbe.getFluidInTank(0).getAmount();

                    if(!pLevel.isClientSide()) {
                        if (anbe.remainingFluidForSatisfaction <= 0) {
                            anbe.resetProgress();
                            for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++)
                                anbe.itemHandler.getStackInSlot(i).shrink(1);
                            anbe.animStage = ANIM_STAGE_SHLORPS;
                            anbe.syncAndSave();
                        } else if (amountInTank > 0) {
                            anbe.remainingFluidForSatisfaction -= anbe.drain(Math.min(amountInTank, anbe.remainingFluidForSatisfaction), FluidAction.EXECUTE).getAmount();
                            anbe.syncAndSave();
                        }
                    }
                }
            }
            else if(anbe.animStage == ANIM_STAGE_CANCEL_CIRCLE) {
                anbe.incrementProgress();

                if(anbe.progress > anbe.cachedSpec.ticksInRampCircle) {
                    anbe.resetProgress();
                    anbe.progress = anbe.cachedSpec.ticksInRampCancel;
                    anbe.animStage = ANIM_STAGE_CANCEL_SPEEDUP;
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_SHLORPS && !pLevel.isClientSide()) {
                if(anbe.isFullySatisfied()) {
                    anbe.animStage = ANIM_STAGE_RAMP_CRAFTING;
                    anbe.syncAndSave();
                }
                else if(pLevel.getGameTime() % anbe.cachedSpec.ticksBetweenShlorpPulls == 0) {
                    NonNullList<Pair<AbstractMateriaStorageBlockEntity, BlockPos>> marks = anbe.getMarkedEntitiesAndLocations();
                    NonNullList<MateriaItem> outstanding = anbe.getDemandedMateriaNotInTransit();

                    if (marks.size() >= 1) {
                        Pair<AbstractMateriaStorageBlockEntity, BlockPos> pair;
                        if (marks.size() == 1) pair = marks.get(0);
                        else {
                            if(anbe.shlorpIndex >= marks.size())
                                anbe.shlorpIndex = 0;
                            pair = marks.get(anbe.shlorpIndex);
                        }

                        if(pair.getFirst() != null) {
                            MateriaItem type = pair.getFirst().getMateriaType();

                            if (type != null) {
                                for (MateriaItem mi : outstanding) {
                                    if (type == mi) {
                                        Pair<Vector3, Vector3> ot = pair.getFirst().getDefaultOriginAndTangent();
                                        Vector3 spawnPos = new Vector3(pair.getSecond());
                                        Vector3 origin = ot.getFirst();
                                        Vector3 tangent = ot.getSecond().scale(4.0f);

                                        int amount = 0;
                                        for (Triplet<MateriaItem, Integer, Boolean> demand : anbe.satisfactionDemands) {
                                            if (demand.getFirst() == mi) {
                                                amount = Math.min(pair.getFirst().getCurrentStock(), demand.getSecond());
                                                break;
                                            }
                                        }

                                        if (amount == 0) break;

                                        //create shlorp
                                        ShlorpEntity se = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), pLevel);
                                        se.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

                                        pair.getFirst().drain(amount);
                                        se.configure(
                                                spawnPos,
                                                origin, tangent,
                                                new Vector3(anbe.getBlockPos()),
                                                new Vector3(0.5, 1.9375f, 0.5), new Vector3(0, 0.5, 0),
                                                anbe.cachedSpec.shlorpSpeed, 0.125f, amount * 2,
                                                mi, amount, ShlorpParticleMode.INVERSE_ENTRY_TANGENT
                                        );

                                        pLevel.addFreshEntity(se);
                                        se.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                                        anbe.markInTransit(mi);

                                        //don't need to keep iterating at this point
                                        break;
                                    }
                                }
                            }
                        }

                        anbe.shlorpIndex = anbe.shlorpIndex >= marks.size() - 1 ? 0 : anbe.shlorpIndex + 1;
                    }
                }
            }
            else if(anbe.animStage == ANIM_STAGE_RAMP_CRAFTING) {
                anbe.incrementProgress();

                if(anbe.progress > anbe.cachedSpec.ticksInRampBeam) {
                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_CRAFTING;
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_CRAFTING) {
                anbe.incrementProgress();

                if(anbe.progress > anbe.cachedSpec.ticksToCraft) {
                    //If there's another stage, move to that
                    if(anbe.currentRecipe.getStages(false).size() > (anbe.craftingStage + 1)) {
                        anbe.createOrUpdateInProgressHolder(anbe.getCraftingStage());
                        anbe.craftingStage++;
                        int experienceCost = getBaseExperienceCostPerStage(anbe.getPowerLevel()) + anbe.currentRecipe.getStages(false).get(anbe.craftingStage).experience;
                        int fluidCost = experienceCost * Config.fluidPerXPPoint;

                        anbe.progress = anbe.cachedSpec.ticksInRampCancel;
                        anbe.remainingFluidForSatisfaction = Math.round((float)fluidCost * (1f - anbe.reductionRate));
                        anbe.animStage = ANIM_STAGE_CRAFTING_IDLE;
                    }
                    //Otherwise, we're done
                    else{
                        anbe.resetProgress();
                        anbe.craftItem();
                        if(anbe.craftingStage > 0)
                            anbe.animStage = ANIM_STAGE_CANCEL_CRAFTING_ADVANCED;
                        else
                            anbe.animStage = ANIM_STAGE_CANCEL_CRAFTING;
                    }
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_CANCEL_CRAFTING) {
                anbe.incrementProgress();

                if(anbe.progress > anbe.cachedSpec.ticksInRampBeam) {
                    anbe.resetProgress();
                    anbe.craftingStage = 0;
                    anbe.animStage = ANIM_STAGE_CANCEL_CIRCLE;
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_CANCEL_CRAFTING_ADVANCED) {
                anbe.incrementProgress();

                if(anbe.progress > anbe.cachedSpec.ticksInRampBeam) {
                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_CANCEL_CRAFTING_CIRCLE;
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_CRAFTING_IDLE) {
                if(anbe.getProgress() > 0)
                    anbe.decrementProgress();

                //Check if all the items are present
                if(anbe.hasAllRecipeItemsForCurrentStage()) {
                    anbe.animStage = ANIM_STAGE_RAMP_CRAFTING_SPEEDUP;
                    anbe.cacheAnimSpec(false);
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_RAMP_CRAFTING_SPEEDUP) {
                anbe.incrementProgress();

                if(!anbe.hasAllRecipeItemsForCurrentStage()) {
                    anbe.animStage = ANIM_STAGE_CANCEL_CRAFTING_SPEEDUP;
                }

                if(anbe.progress > anbe.cachedSpec.ticksInRampSpeedup) {
                    anbe.resetProgress();
                    anbe.animStage = ANIM_STAGE_RAMP_CRAFTING_CIRCLE;
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_CANCEL_CRAFTING_SPEEDUP) {
                anbe.decrementProgress();

                if(anbe.progress <= 0) {
                    anbe.resetProgress();
                    if(anbe.craftingStage > 0)
                        anbe.animStage = ANIM_STAGE_CRAFTING_IDLE;
                    else
                        anbe.animStage = ANIM_STAGE_IDLE;
                    anbe.syncAndSave();
                }
            }
            else if(anbe.animStage == ANIM_STAGE_RAMP_CRAFTING_CIRCLE) {
                anbe.incrementProgress();

                if(!anbe.hasAllRecipeItemsForCurrentStage()) {
                    anbe.animStage = ANIM_STAGE_CANCEL_CRAFTING_CIRCLE;
                }

                if(anbe.progress > anbe.cachedSpec.ticksInRampCircle) {
                    int amountInTank = anbe.getFluidInTank(0).getAmount();

                    if(anbe.remainingFluidForSatisfaction <= 0) {
                        anbe.remainingFluidForSatisfaction = 0;
                        anbe.resetProgress();
                        for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++)
                            anbe.itemHandler.getStackInSlot(i).shrink(1);
                        anbe.setSatisfactionDemands(anbe.currentRecipe.getStages(true).get(anbe.craftingStage).componentMateria);
                        anbe.animStage = ANIM_STAGE_SHLORPS;
                        anbe.syncAndSave();
                    } else if(amountInTank > 0) {
                        anbe.remainingFluidForSatisfaction -= anbe.drain(Math.min(amountInTank, anbe.remainingFluidForSatisfaction), FluidAction.EXECUTE).getAmount();
                        anbe.syncAndSave();
                    }
                }
            }
            else if(anbe.animStage == ANIM_STAGE_CANCEL_CRAFTING_CIRCLE) {
                anbe.incrementProgress();

                if(anbe.progress > anbe.cachedSpec.ticksInRampCircle) {
                    if(anbe.itemHandler.getStackInSlot(SLOT_PROGRESS_HOLDER).isEmpty()) {
                        anbe.craftingStage = 0;
                        anbe.animStage = ANIM_STAGE_CANCEL_SPEEDUP;
                    }
                    else {
                        anbe.progress = anbe.cachedSpec.ticksInRampSpeedup;
                        anbe.animStage = ANIM_STAGE_CANCEL_CRAFTING_SPEEDUP;
                    }
                    anbe.syncAndSave();
                }
            }

            //deferred plugin linkage
            if(!pLevel.isClientSide()) {
                if (anbe.pluginLinkageCountdown == 0) {
                    anbe.pluginLinkageCountdown = -1;
                    anbe.linkPlugins();
                } else if (anbe.pluginLinkageCountdown > 0) {
                    anbe.pluginLinkageCountdown--;
                }
            }
        }
    }

    private void cacheAnimSpec(boolean doSatisfactionDemandsUpdate) {
        this.cachedSpec = getAnimSpec(this.powerLevel);

        if(doSatisfactionDemandsUpdate) {
            InfusionStage currentStage = this.currentRecipe.getStages(false).get(this.craftingStage);
            setSatisfactionDemands(currentStage.componentMateria);
        }
    }

    public NonNullList<Pair<AbstractMateriaStorageBlockEntity, BlockPos>> getMarkedEntitiesAndLocations() {
        NonNullList<Pair<AbstractMateriaStorageBlockEntity, BlockPos>> markedPairs = NonNullList.create();
        ItemStack stackInSlot = itemHandler.getStackInSlot(SLOT_MARKS);

        if(stackInSlot.getItem() == ItemInit.RUNE_MARKING.get()) {
            if(stackInSlot.hasTag()) {
                CompoundTag nbt = stackInSlot.getTag();

                if(nbt.contains("mark")) {
                    CompoundTag nbtMark = nbt.getCompound("mark");
                    BlockPos markedPos = new BlockPos(nbtMark.getInt("x"), nbtMark.getInt("y"), nbtMark.getInt("z"));

                    BlockEntity be = level.getBlockEntity(markedPos);
                    if(be != null) {
                        if(be instanceof AbstractMateriaStorageBlockEntity amsbe) {
                            markedPairs.add(new Pair<>(amsbe, markedPos));
                        }
                    }
                }
            }
        }
        else if(stackInSlot.getItem() == ItemInit.BOOK_MARKS.get()) {
            if(stackInSlot.hasTag()) {
                CompoundTag nbt = stackInSlot.getTag();

                if(nbt.contains("Items")) {
                    CompoundTag nbtItems = nbt.getCompound("Items");

                    for(int i=0; i<16; i++) {
                        if(nbtItems.contains(""+i)) {
                            CompoundTag nbtItem = nbtItems.getCompound(""+i).getCompound("item");
                            if(nbtItem.contains("tag")) {
                                CompoundTag nbtMark = nbtItem.getCompound("tag").getCompound("mark");

                                BlockPos markedPos = new BlockPos(nbtMark.getInt("x"), nbtMark.getInt("y"), nbtMark.getInt("z"));

                                BlockEntity be = level.getBlockEntity(markedPos);
                                if(be != null) {
                                    if(be instanceof AbstractMateriaStorageBlockEntity amsbe) {
                                        markedPairs.add(new Pair<>(amsbe, markedPos));
                                    }
                                }
                            }
                        }
                    }

                    BlockPos markedPos = new BlockPos(nbtItems.getInt("x"), nbtItems.getInt("y"), nbtItems.getInt("z"));

                    BlockEntity be = level.getBlockEntity(markedPos);
                    if(be != null) {
                        if(be instanceof AbstractMateriaStorageBlockEntity amsbe) {
                            markedPairs.add(new Pair<>(amsbe, markedPos));
                        }
                    }
                }
            }
        }

        return markedPairs;
    }

    private static final int[] SPEC_PARAM_RAMP_SPEEDUP = {-1, 120, 90, 67, 50, 37};
    private static final int[] SPEC_PARAM_RAMP_CANCEL  = {-1, 90, 67, 50, 37, 28};
    private static final int[] SPEC_PARAM_RAMP_BEAM    = {-1, 32, 28, 24, 20, 16};
    private static final int[] SPEC_PARAM_RAMP_CIRCLE  = {-1, 80, 60, 45, 32, 24};
    private static final int[] SPEC_PARAM_SHLORP_PULL  = {-1, 25, 18, 12, 7, 3};
    private static final int[] SPEC_PARAM_CRAFT        = {-1, 420, 315, 235, 175, 130};
    private static final float[] SPEC_PARAM_SHLORP_SPEED = {-1, 0.040f, 0.050f, 0.063f, 0.078f, 0.100f};
    public static AlchemicalNexusAnimSpec getAnimSpec(int pPowerLevel) {
        return new AlchemicalNexusAnimSpec(
                SPEC_PARAM_RAMP_SPEEDUP[pPowerLevel],
                SPEC_PARAM_RAMP_CANCEL[pPowerLevel],
                SPEC_PARAM_RAMP_BEAM[pPowerLevel],
                SPEC_PARAM_RAMP_CIRCLE[pPowerLevel],
                SPEC_PARAM_SHLORP_PULL[pPowerLevel],
                SPEC_PARAM_SHLORP_SPEED[pPowerLevel],
                SPEC_PARAM_CRAFT[pPowerLevel]);
    }

    public boolean hasAllRecipeItemsForCurrentStage() {
        int i = 0;
        for(ItemStack is : currentRecipe.getStages(false).get(craftingStage).componentItems) {
            if(itemHandler.getStackInSlot(SLOT_INPUT_START + i).getItem() != is.getItem())
                return false;
            i++;
        }
        return true;
    }

    public SimpleContainer getContentsOfOutputSlots() {
        SimpleContainer output = new SimpleContainer(SLOT_OUTPUT_COUNT);

        for(int i = SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            output.setItem(i-SLOT_OUTPUT_START, itemHandler.getStackInSlot(i).copy());
        }

        return output;
    }

    public SimpleContainer getContentsOfInputSlots() {
        SimpleContainer input = new SimpleContainer(SLOT_INPUT_COUNT);

        for(int i = SLOT_INPUT_START; i<SLOT_INPUT_START+SLOT_INPUT_COUNT; i++) {
            input.setItem(i-SLOT_INPUT_START, itemHandler.getStackInSlot(i).copy());
        }

        return input;
    }

    private static final int[] SPEC_EXPERIENCE_COST = {-1, 360, 540, 810, 1215, 1825};
    public static int getBaseExperienceCostPerStage(int pPowerLevel) {
        return SPEC_EXPERIENCE_COST[pPowerLevel];
    }

    protected void craftItem() {
        SimpleContainer contentsOfOutputSlots = getContentsOfOutputSlots();
        ItemStack alchemyObject = currentRecipe.getAlchemyObject();

        contentsOfOutputSlots.addItem(alchemyObject.copy());

        for(int i=SLOT_OUTPUT_START;i<SLOT_INPUT_START+SLOT_OUTPUT_COUNT;i++) {
            itemHandler.setStackInSlot(i, contentsOfOutputSlots.getItem(i-SLOT_OUTPUT_START));
        }

        itemHandler.setStackInSlot(SLOT_PROGRESS_HOLDER, ItemStack.EMPTY);
    }

    protected void createOrUpdateInProgressHolder(int pRecipeStage) {
        ItemStack holder = itemHandler.getStackInSlot(SLOT_PROGRESS_HOLDER);

        if(holder.isEmpty()) {
            holder = new ItemStack(ItemRegistry.SUBLIMATION_IN_PROGRESS.get());
        }

        CompoundTag holderNBT = holder.getOrCreateTag();

        //Create a data structure that contains all of the bits and bobs we need
        Map<Item, Integer> itemMap = new HashMap<>();
        Map<Item, Integer> materiaMap = new HashMap<>();

        for(int i=0; i<=pRecipeStage; i++) {
            InfusionStage stage = currentRecipe.getStages(false).get(i);

            for(ItemStack is : stage.componentItems) {
                if(itemMap.containsKey(is.getItem())) {
                    itemMap.put(is.getItem(), itemMap.get(is.getItem()) + is.getCount());
                } else {
                    itemMap.put(is.getItem(), is.getCount());
                }
            }
            for(ItemStack is : stage.componentMateria) {
                if(materiaMap.containsKey(is.getItem())) {
                    materiaMap.put(is.getItem(), materiaMap.get(is.getItem()) + is.getCount());
                } else {
                    materiaMap.put(is.getItem(), is.getCount());
                }
            }
        }

        ListTag itemList = new ListTag();
        for (Item i : itemMap.keySet())
        {
            CompoundTag itemTag = new CompoundTag();
            ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(i);
            itemTag.putString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
            int amount = itemMap.get(i);
            itemTag.putShort("Count", (short)amount);
            itemList.add(itemTag);
        }
        ListTag materiaList = new ListTag();
        for (Item i : materiaMap.keySet())
        {
            CompoundTag materiaTag = new CompoundTag();
            ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(i);
            materiaTag.putString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
            int amount = materiaMap.get(i);
            materiaTag.putShort("Count", (short)amount);
            materiaList.add(materiaTag);
        }

        holderNBT.put("savedItems", itemList);
        holderNBT.put("savedMateria", materiaList);

        holder.setTag(holderNBT);
        itemHandler.setStackInSlot(SLOT_PROGRESS_HOLDER, holder);
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected void incrementProgress() {
        progress++;
    }

    protected void decrementProgress() {
        progress--;
    }

    ////////////////////
    // RECIPE HANDLING
    ////////////////////

    public List<ItemStack> getInputItems() {
        List<ItemStack> items = new ArrayList<>();

        for(int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++){
            if(!itemHandler.getStackInSlot(i).isEmpty())
                items.add(itemHandler.getStackInSlot(i));
        }

        return items;
    }

    public void setRecipeFromOutput(Level pLevel, ItemStack pQuery) {
        AlchemicalInfusionRecipe air = AlchemicalInfusionRecipe.getInfusionRecipe(pLevel, pQuery);
        if(air != null) {
            this.currentRecipe = air;
            this.itemHandler.setStackInSlot(SLOT_RECIPE, air.getAlchemyObject());
            this.syncAndSave();
        }
    }

    public NonNullList<Triplet<MateriaItem, Integer, Boolean>> getAllMateriaDemands() {
        return this.satisfactionDemands;
    }

    ////////////////////
    // FLUID HANDLING
    ////////////////////

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return containedSlurry;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Config.alchemicalNexusTankCapacity;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack fluidAction) {
        return fluidAction.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get();
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction action) {
        if(action.execute()) {
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();

            //Hit capacity
            if(incomingAmount + extantAmount > getTankCapacity(0)) {
                int actualTransfer = getTankCapacity(0) - extantAmount;
                if(action == FluidAction.EXECUTE)
                    this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), getTankCapacity(0));
                return actualTransfer;
            } else {
                if(action == FluidAction.EXECUTE)
                    this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), extantAmount + incomingAmount);
                return incomingAmount;
            }
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        if(fluidAction.execute()) {
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();
            if(extantAmount >= incomingAmount) {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry.shrink(incomingAmount);
                return new FluidStack(fluid, incomingAmount);
            } else {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry = FluidStack.EMPTY;
                return new FluidStack(fluid, extantAmount);
            }
        }
        return fluidStack;
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        return drain(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), i), fluidAction);
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    public AlchemicalInfusionRecipe getCurrentRecipe() {
        return this.currentRecipe;
    }

    public ItemStack getMarkItem() {
        return itemHandler.getStackInSlot(SLOT_MARKS);
    }

    public ItemStack getProgressHolderItem() {
        return itemHandler.getStackInSlot(SLOT_PROGRESS_HOLDER);
    }

    public static int getScaledSlurry(int pSlurry) {
        return (FLUID_BAR_HEIGHT * pSlurry) / Config.alchemicalNexusTankCapacity;
    }

    public int getPowerLevel() {
        return this.powerLevel;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getAnimStage() {
        return this.animStage;
    }

    public int getCraftingStage() {
        return this.craftingStage;
    }

    public void incrementPowerUsageSetting() {
        this.powerLevel = Math.min(this.powerLevel + 1, 5);
        this.setChanged();
    }

    public void decrementPowerUsageSetting() {
        this.powerLevel = Math.max(this.powerLevel - 1, 1);
        this.setChanged();
    }

    public void setPowerUsageSetting(int pNewSetting) {
        this.powerLevel = pNewSetting;
        this.setChanged();
    }

    public int getScaledProgress(int pWidth) {
        return progress * pWidth / cachedSpec.ticksToCraft;
    }

    ////////////////////
    // VFX HANDLING
    ////////////////////

    public void handleAnimationDrivers() {
        crystalAngle = (crystalAngle + crystalRotSpeed) % 360f;
        itemAngle = (itemAngle + itemRotSpeed) % (float)(Math.PI * 8);

        if(cachedSpec != null) {
            if(animStage == ANIM_STAGE_IDLE) {
                crystalRotSpeed = CRYSTAL_SPEED_MIN;
                itemRotSpeed = ITEM_SPEED_MIN;
                itemScale = ITEM_SCALE_START;
            }
            else if(animStage == ANIM_STAGE_RAMP_SPEEDUP ||
                    animStage == ANIM_STAGE_RAMP_CRAFTING_SPEEDUP ||
                    animStage == ANIM_STAGE_CANCEL_SPEEDUP ||
                    animStage == ANIM_STAGE_CANCEL_CRAFTING_SPEEDUP ||
                    animStage == ANIM_STAGE_CRAFTING_IDLE) {
                float timeInStage = ((float)progress / (float)cachedSpec.ticksInRampSpeedup);

                crystalRotSpeed = MathUtils.lerpf(CRYSTAL_SPEED_MIN, CRYSTAL_SPEED_MAX, timeInStage);
                itemRotSpeed = MathUtils.lerpf(ITEM_SPEED_MIN, ITEM_SPEED_MAX, timeInStage);
                itemScale = MathUtils.lerpf(ITEM_SCALE_START, ITEM_SCALE_END, timeInStage);
            }
        }
    }

    public static final Vector3[] CANDLE_PARTICLE_ORIGINS = {
            new Vector3( 0.092,0.975,-1.483), new Vector3( 0.907,0.975,-1.483),
            new Vector3( 1.483,0.975,-0.907), new Vector3( 1.483,0.975,-0.092),
            new Vector3( 0.907,0.975, 0.483), new Vector3( 0.092,0.975, 0.483),
            new Vector3(-0.483,0.975,-0.092), new Vector3(-0.483,0.975,-0.907)
    };
    public void spawnParticles() {
        BlockPos bp = getBlockPos();
        Vector3 origin = new Vector3(bp.getX(), bp.getY(), bp.getZ()).add(new Vector3(0,0,1));

        if(cachedSpec != null) {
            //Candles
            if (animStage == ANIM_STAGE_IDLE) {
                for (int i = 0; i < CANDLE_PARTICLE_ORIGINS.length; i++) {
                    if ((level.getGameTime() % 6 == 0 && i % 2 == 0) || (level.getGameTime() % 6 == 3 && i % 2 == 1)) {
                        Vector3 pos = origin.add(CANDLE_PARTICLE_ORIGINS[i]);

                        level.addParticle(new MAParticleType(ParticleInit.BLUE_FLAME.get())
                                        .setScale(0.05f).setMaxAge(30),
                                pos.x, pos.y, pos.z,
                                0, 0.04f, 0);
                    }
                }
            } else {
                for (int i = 0; i < CANDLE_PARTICLE_ORIGINS.length; i++) {
                    Vector3 pos = origin.add(CANDLE_PARTICLE_ORIGINS[i]);

                    level.addParticle(new MAParticleType(ParticleInit.GLOW.get())
                                    .setScale(0.06f).setMaxAge(10).setColor(135, 202, 195, 128),
                            pos.x, pos.y, pos.z,
                            0, 0.05f, 0);

                    if (level.getGameTime() % 2 == 0) {
                        level.addParticle(new MAParticleType(ParticleInit.GLOW.get())
                                        .setScale(0.2f).setMaxAge(5).setColor(15, 150, 135, 255),
                                pos.x, pos.y, pos.z,
                                0, 0.001f, 0);
                    }
                }
            }

            //Glowing ring that the items turn into
            float ringPercent = 0f;
            if (animStage == ANIM_STAGE_RAMP_SPEEDUP || animStage == ANIM_STAGE_RAMP_CRAFTING_SPEEDUP) {
                ringPercent = Math.min(1, Math.max(0, ((float) progress / (float) cachedSpec.ticksInRampSpeedup) * 6 - (ITEM_SCALE_START - 2)));


            } else if (animStage == ANIM_STAGE_RAMP_CIRCLE ||
                    animStage == ANIM_STAGE_RAMP_CRAFTING_CIRCLE ||
                    animStage == ANIM_STAGE_SHLORPS ||
                    animStage == ANIM_STAGE_RAMP_CRAFTING ||
                    animStage == ANIM_STAGE_CRAFTING) {
                ringPercent = 1f;
            }
            if (ringPercent > 0) {
                Vector3 pos = origin.add(new Vector3(0.5, 1.9375f, -0.5));

                for (int i = 0; i < 3; i++) {
                    level.addParticle(new MAParticleType(ParticleInit.FLAME.get())
                                    .setMover(new ParticleOrbitMover(pos.x, pos.y, pos.z, 0.3 + 0.1 * i, 0, AlchemicalNexusBlockEntityRenderer.ITEM_HOVER_RADIUS))
                                    .setScale(0.06f * ringPercent).setColor(36, 151, 110),
                            pos.x, pos.y, pos.z,
                            0, 0, 0);
                }
            }

            //Sparky orb above the device
            float orbPercent = 0f;
            if (animStage == ANIM_STAGE_RAMP_SPEEDUP ||
                    animStage == ANIM_STAGE_RAMP_CRAFTING_SPEEDUP ||
                    animStage == ANIM_STAGE_CANCEL_CRAFTING_SPEEDUP) {
                orbPercent = (float) progress / (float) cachedSpec.ticksInRampSpeedup;
            } else if (animStage == ANIM_STAGE_CRAFTING_IDLE) {
                orbPercent = (float) progress / (float) cachedSpec.ticksInRampCancel;
            } else if (animStage == ANIM_STAGE_RAMP_CRAFTING ||
                    animStage == ANIM_STAGE_RAMP_CIRCLE ||
                    animStage == ANIM_STAGE_RAMP_CRAFTING_CIRCLE ||
                    animStage == ANIM_STAGE_SHLORPS ||
                    animStage == ANIM_STAGE_CRAFTING) {
                orbPercent = 1f;
            }

            if (orbPercent > 0 || animStage == ANIM_STAGE_CRAFTING_IDLE) {
                if ((level.getGameTime() + 1) % 2 == 0) {
                    Vector3 pos = origin.add(new Vector3(0.5, 3.5, -0.5));

                    //center orb
                    level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(22, 94, 69).setScale(0.2f + 2.2f * orbPercent).setMaxAge(30),
                            pos.x, pos.y, pos.z,
                            0, 0, 0);
                    level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(128, 128, 128).setScale(0.1f + 0.3f * orbPercent).setMaxAge(20),
                            pos.x, pos.y, pos.z,
                            0, 0, 0);

                    //sparks
                    if ((level.getGameTime() + 1) % 2 == 0 && animStage != ANIM_STAGE_CRAFTING_IDLE) {
                        for (int i = 0; i < r.nextInt(7); i++) {
                            Vec3 vector = new Vec3(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, r.nextFloat() - 0.5f).normalize();
                            final float speed = 0.08f;
                            level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                            .setColor(36, 151, 110).setScale(0.2f).setGravity(0.02f).setMaxAge(30).setPhysics(true),
                                    pos.x, pos.y, pos.z,
                                    vector.x * speed, vector.y * speed + (r.nextFloat() * 0.125f), vector.z * speed);
                        }
                    }
                }
            }

            //Sparky blast during crafting
            if (animStage == ANIM_STAGE_CRAFTING) {
                Vector3 pos = new Vector3(getBlockPos()).add(new Vector3(0.5, 1.26, 0.5));
                for (int i = 0; i < 20; ++i) {
                    double sx = (-0.5D + Math.random()) * 0.15D;
                    double sy = 0.03D + ((double) i / 20.0D) * 0.04D;
                    double sz = (-0.5D + Math.random()) * 0.15D;
                    this.getLevel().addParticle(new MAParticleType((ParticleType) ParticleInit.BLUE_SPARKLE_GRAVITY.get()), pos.x, pos.y, pos.z, sx, sy, sz);
                }
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3, 4, 3));
    }

    ////////////////////
    // ACTUATOR HANDLING
    ////////////////////

    @Override
    public void linkPlugins() {
        pluginDevices.clear();

        List<BlockEntity> query = new ArrayList<>();
        for(Triplet<BlockPos, AlchemicalNexusRouterType, DevicePlugDirection> posAndType : AlchemicalNexusBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof AlchemicalNexusRouterBlockEntity anrbe) {
                BlockEntity pe = anrbe.getPlugEntity();
                if(pe instanceof DirectionalPluginBlockEntity dpbe) pluginDevices.add(dpbe);
            }
        }
    }

    @Override
    public void removePlugin(DirectionalPluginBlockEntity pPlugin) {
        this.pluginDevices.remove(pPlugin);
        if(pPlugin instanceof ActuatorArcaneBlockEntity) {
            reductionRate = 0.0f;
        }
        syncAndSave();
    }

    @Override
    public void linkPluginsDeferred() {
        pluginLinkageCountdown = 3;
    }

    ////////////////////
    // SHLORP HANDLING
    ////////////////////

    @Override
    public int satisfy(ItemStack pQuery) {
        int id = super.satisfy(pQuery);
        syncAndSave();
        return id;
    }
}
