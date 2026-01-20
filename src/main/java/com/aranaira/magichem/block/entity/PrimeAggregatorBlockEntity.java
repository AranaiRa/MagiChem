package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.PrimeAggregatorBlock;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeDynamicBlockEntity;
import com.aranaira.magichem.block.entity.routers.PrimeAggregatorRouterBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.PrimeAggregatorRouterType;
import com.aranaira.magichem.gui.PrimeAggregatorMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.ExaltationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.aranaira.magichem.util.MathHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.items.ItemInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import static com.mna.api.affinity.Affinity.*;

public class PrimeAggregatorBlockEntity extends BlockEntity implements MenuProvider, ICanTakePlugins, IFluidHandler, IHasDeviceRecipeSlot, IEldrinConsumerTile, IShlorpReceiver, IMateriaProvisionRequester, IRequiresRouterCleanupOnDestruction, IKeepsInventoryOnBreak {
    public static final int
            SLOT_COUNT = 7, SLOT_INPUT_COUNT = 2,
            SLOT_ITEM_INPUT = 0, SLOT_MATERIA_INPUT = 1, SLOT_BOTTLES_OUTPUT = 2, SLOT_PROGRESS_HOLDER = 3,
            SLOT_OUTPUT_START = 4, SLOT_OUTPUT_COUNT  = 3,
            ANIM_STAGE_IDLE = 0,
            ANIM_STAGE_GATHERING_ITEMS = 1, ANIM_STAGE_TO_MATERIA = 2,
            ANIM_STAGE_GATHERING_MATERIA = 3, ANIM_STAGE_TO_ELDRIN = 4,
            ANIM_STAGE_GATHERING_ELDRIN = 5, ANIM_STAGE_TO_SLURRY = 6,
            ANIM_STAGE_GATHERING_SLURRY = 7, ANIM_STAGE_CRAFTING = 8,
            TO_MATERIA_DURATION = 100, TO_ELDRIN_DURATION = 100, TO_SLURRY_DURATION = 100, CRAFTING_DURATION = 100;
    public static final HashMap<Direction, Vector3[]> CRYSTAL_POSITIONS = new HashMap<>();
    private static final Random r = new Random();

    private Player owner;
    private UUID ownerUUID;
    private int animStage = ANIM_STAGE_IDLE, itemsDelivered = 0, materiaDelivered = 0, slurryDelivered = 0, progress = 0, pluginLinkageCountdown = 3;
    private final HashMap<Affinity, Integer> eldrinDelivered = new HashMap<>();
    private boolean doDeferredRecipeCheck = false;
    private ExaltationRecipe currentRecipe = null;
    private ResourceLocation deferredRecipeQuery = null;
    private FluidStack containedSlurry = FluidStack.EMPTY.copy();
    protected List<AbstractDirectionalPluginBlockEntity> pluginDevices = new ArrayList<>();

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.of(() -> this);
    private final ItemStackHandler itemHandler;

    public boolean clearRecipeAfterNextProcess = false;
    public float reductionRate = 0.0f;

    public PrimeAggregatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.PRIME_AGGREGATOR_BE.get(), pPos, pBlockState);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_PROGRESS_HOLDER) return stack.getItem() == ItemRegistry.EXALTATION_IN_PROGRESS.get();
                if(currentRecipe != null) {
                    if(slot == SLOT_ITEM_INPUT) return stack.getItem() == currentRecipe.getItemType();
                    else if(slot == SLOT_MATERIA_INPUT) return stack.getItem() == currentRecipe.getMateriaType();
                }

                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == SLOT_PROGRESS_HOLDER) {
                    final ItemStack stackInSlot = itemHandler.getStackInSlot(slot).copy();
                    if (!simulate) {
                        stackInSlot.setTag(packCraftDataToTag());
                        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
                    }
                    return stackInSlot;
                } /*else if(slot == SLOT_WISDOM) {
                    return ItemStack.EMPTY;
                }*/

                return super.extractItem(slot, amount, simulate);
            }

                @Override
            protected void onContentsChanged(int slot) {
                ItemStack stackInSlot = getStackInSlot(slot);
                if(slot == SLOT_PROGRESS_HOLDER && !getLevel().isClientSide()) {
                    if(stackInSlot.isEmpty()) {
                        animStage = ANIM_STAGE_IDLE;
                        clearDeliveries();
                        syncAndSave();
                    } else if(stackInSlot.hasTag()) {
                        unpackCraftDataFromTag(stackInSlot.getTag());
                    }
                }
                setChanged();
            }
        };

        clearDeliveries();

        if(CRYSTAL_POSITIONS.size() == 0) {
            CRYSTAL_POSITIONS.put(Direction.NORTH, new Vector3[]{
                    new Vector3( 0.500, 1.875, -0.506),
                    new Vector3( 1.371, 1.750, -0.003),
                    new Vector3( 1.371, 1.625,  1.003),
                    new Vector3( 0.500, 1.500,  1.506),
                    new Vector3(-0.371, 1.625,  1.003),
                    new Vector3(-0.371, 1.750, -0.003)
            });
            CRYSTAL_POSITIONS.put(Direction.EAST,  new Vector3[]{
                    new Vector3( 1.506, 1.875,  0.500),
                    new Vector3( 1.003, 1.750,  1.371),
                    new Vector3(-0.003, 1.625,  1.371),
                    new Vector3(-0.506, 1.500,  0.500),
                    new Vector3(-0.003, 1.625, -0.371),
                    new Vector3( 1.003, 1.750, -0.371)
            });
            CRYSTAL_POSITIONS.put(Direction.SOUTH, new Vector3[]{
                    new Vector3( 0.500, 1.875,  1.506),
                    new Vector3(-0.371, 1.750,  1.003),
                    new Vector3(-0.371, 1.625, -0.003),
                    new Vector3( 0.500, 1.500, -0.506),
                    new Vector3( 1.371, 1.625, -0.003),
                    new Vector3( 1.371, 1.750,  1.003)
            });
            CRYSTAL_POSITIONS.put(Direction.WEST,  new Vector3[]{
                    new Vector3(-0.506, 1.875,  0.500),
                    new Vector3(-0.003, 1.750, -0.371),
                    new Vector3( 1.003, 1.625, -0.371),
                    new Vector3( 1.506, 1.500,  0.500),
                    new Vector3( 1.003, 1.625,  1.371),
                    new Vector3(-0.003, 1.750,  1.371)
            });
        }
    }

    private CompoundTag packCraftDataToTag() {
        CompoundTag nbt = new CompoundTag();

        if(currentRecipe != null) {
            nbt.putString("result", ForgeRegistries.ITEMS.getKey(currentRecipe.getResultItem().getItem()).toString());
            nbt.putInt("animStage", animStage);
            nbt.putInt("itemsDelivered", itemsDelivered);
            nbt.putInt("materiaDelivered", materiaDelivered);
            nbt.putInt("slurryDelivered", slurryDelivered);
            CompoundTag eldrinDeliveryTag = new CompoundTag();
            for(Affinity aff : eldrinDelivered.keySet()) {
                eldrinDeliveryTag.putInt(aff.name(), eldrinDelivered.get(aff));
            }
            nbt.put("eldrinDelivered", eldrinDeliveryTag);
        }

        return nbt;
    }

    public void unpackCraftDataFromTag(CompoundTag nbt) {
        if(nbt.contains("result")) {
            Item itemQuery = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("result")));
            if(itemQuery != null && level != null) {
                ExaltationRecipe er = ExaltationRecipe.getExaltationRecipe(level, itemQuery);
                if(er != null) {
                    currentRecipe = er;
                    doDeferredRecipeCheck = false;
                    progress = 0;

                    animStage = nbt.getInt("animStage");
                    itemsDelivered = nbt.getInt("itemsDelivered");
                    materiaDelivered = nbt.getInt("materiaDelivered");
                    slurryDelivered = nbt.getInt("slurryDelivered");

                    CompoundTag eldrinDeliveryTag = nbt.getCompound("eldrinDelivered");
                    for(String key : eldrinDeliveryTag.getAllKeys()){
                        eldrinDelivered.put(Affinity.valueOf(key), eldrinDeliveryTag.getInt(key));
                    }

                    syncAndSave();
                }
            }
        }
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

    private void clearDeliveries() {
        itemsDelivered = 0;
        materiaDelivered = 0;
        slurryDelivered = 0;
        provisioningInProgress = false;

        eldrinDelivered.clear();
        eldrinDelivered.put(ENDER, 0);
        eldrinDelivered.put(EARTH, 0);
        eldrinDelivered.put(WATER, 0);
        eldrinDelivered.put(WIND, 0);
        eldrinDelivered.put(FIRE, 0);
        eldrinDelivered.put(ARCANE, 0);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PrimeAggregatorMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(0));
    }

    public ExaltationRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-2, 0, -2), getBlockPos().offset(2,4,2));
    }

    public void setRecipeByOutput(ItemStack pRecipeOutput) {
        if(currentRecipe != null && pRecipeOutput.getItem() == currentRecipe.getResultItem().getItem())
            return;

        ExaltationRecipe er = ExaltationRecipe.getExaltationRecipe(level, pRecipeOutput.getItem());

        if(er != null) {
            this.currentRecipe = er;
            this.clearDeliveries();
            this.animStage = ANIM_STAGE_IDLE;
            this.syncAndSave();
        }
    }

    public void clearRecipe() {
        this.currentRecipe = null;
        this.clearDeliveries();
        this.animStage = ANIM_STAGE_IDLE;
        this.syncAndSave();
    }

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
        linkPlugins();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putBoolean("clearRecipeAfterNextProcess", this.clearRecipeAfterNextProcess);
        nbt.putInt("progress", progress);
        nbt.putInt("animStage", animStage);
        nbt.putInt("fluidContents", this.containedSlurry.getAmount());
        nbt.putFloat("reductionRate", reductionRate);

        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);

        if(currentRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.ITEMS.getKey(currentRecipe.getResultItem().getItem());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
        }

        CompoundTag deliveryTag = new CompoundTag();
        deliveryTag.putInt("items", itemsDelivered);
        deliveryTag.putInt("materia", materiaDelivered);
        deliveryTag.putInt("slurry", slurryDelivered);
        deliveryTag.putInt("eldrinEnder", eldrinDelivered.get(ENDER));
        deliveryTag.putInt("eldrinEarth", eldrinDelivered.get(EARTH));
        deliveryTag.putInt("eldrinWater", eldrinDelivered.get(WATER));
        deliveryTag.putInt("eldrinAir", eldrinDelivered.get(WIND));
        deliveryTag.putInt("eldrinFire", eldrinDelivered.get(FIRE));
        deliveryTag.putInt("eldrinArcane", eldrinDelivered.get(ARCANE));
        nbt.put("deliveries", deliveryTag);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        clearRecipeAfterNextProcess = nbt.getBoolean("clearRecipeAfterNextProcess");
        progress = nbt.getInt("progress");
        animStage = nbt.getInt("animStage");
        reductionRate = nbt.getFloat("reductionRate");

        if(nbt.contains("owner"))
            ownerUUID = nbt.getUUID("owner");

        int fluidContents = nbt.getInt("fluidContents");
        if(fluidContents > 0)
            containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), fluidContents);
        else
            containedSlurry = FluidStack.EMPTY;

        if(nbt.contains("recipe"))
            deferredRecipeQuery = new ResourceLocation(nbt.getString("recipe"));
        else
            deferredRecipeQuery = null;
        doDeferredRecipeCheck = true;

        final CompoundTag deliveryTag = nbt.getCompound("deliveries");
        itemsDelivered = deliveryTag.getInt("items");
        materiaDelivered = deliveryTag.getInt("materia");
        slurryDelivered = deliveryTag.getInt("slurry");
        eldrinDelivered.put(ENDER, deliveryTag.getInt("eldrinEnder"));
        eldrinDelivered.put(EARTH, deliveryTag.getInt("eldrinEarth"));
        eldrinDelivered.put(WATER, deliveryTag.getInt("eldrinWater"));
        eldrinDelivered.put(WIND, deliveryTag.getInt("eldrinAir"));
        eldrinDelivered.put(FIRE, deliveryTag.getInt("eldrinFire"));
        eldrinDelivered.put(ARCANE, deliveryTag.getInt("eldrinArcane"));

//        updateActuatorValues(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putBoolean("clearRecipeAfterNextProcess", this.clearRecipeAfterNextProcess);
        nbt.putInt("progress", progress);
        nbt.putInt("animStage", animStage);
        nbt.putInt("fluidContents", this.containedSlurry.getAmount());
        nbt.putFloat("reductionRate", reductionRate);

        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);

        if(currentRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.ITEMS.getKey(currentRecipe.getResultItem().getItem());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
        }

        CompoundTag deliveryTag = new CompoundTag();
        deliveryTag.putInt("items", itemsDelivered);
        deliveryTag.putInt("materia", materiaDelivered);
        deliveryTag.putInt("slurry", slurryDelivered);
        deliveryTag.putInt("eldrinEnder", eldrinDelivered.get(ENDER));
        deliveryTag.putInt("eldrinEarth", eldrinDelivered.get(EARTH));
        deliveryTag.putInt("eldrinWater", eldrinDelivered.get(WATER));
        deliveryTag.putInt("eldrinAir", eldrinDelivered.get(WIND));
        deliveryTag.putInt("eldrinFire", eldrinDelivered.get(FIRE));
        deliveryTag.putInt("eldrinArcane", eldrinDelivered.get(ARCANE));
        nbt.put("deliveries", deliveryTag);

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

    public Pair<Integer, Integer> getItems() {
        if(currentRecipe != null) return new Pair<>(itemsDelivered, currentRecipe.getItemsRequired());
        return new Pair<>(0, -1);
    }

    public Pair<Integer, Integer> getMateria() {
        if(currentRecipe != null) return new Pair<>(materiaDelivered, currentRecipe.getMateriaRequired());
        return new Pair<>(0, -1);
    }

    public Pair<Integer, Integer> getSlurry() {
        if(currentRecipe != null) return new Pair<>(slurryDelivered, Math.round(currentRecipe.getSlurryRequired() * (1f - reductionRate)));
        return new Pair<>(0, -1);
    }

    public Pair<Integer, Integer> getEldrin() {
        if(currentRecipe != null) {
            int types = 0;
            int delivered = 0;
            if(currentRecipe.usesEldrinType(ENDER)) {
                types++;
                delivered += eldrinDelivered.get(ENDER);
            }
            if(currentRecipe.usesEldrinType(EARTH)) {
                types++;
                delivered += eldrinDelivered.get(EARTH);
            }
            if(currentRecipe.usesEldrinType(WATER)) {
                types++;
                delivered += eldrinDelivered.get(WATER);
            }
            if(currentRecipe.usesEldrinType(WIND)) {
                types++;
                delivered += eldrinDelivered.get(WIND);
            }
            if(currentRecipe.usesEldrinType(FIRE)) {
                types++;
                delivered += eldrinDelivered.get(FIRE);
            }
            if(currentRecipe.usesEldrinType(ARCANE)) {
                types++;
                delivered += eldrinDelivered.get(ARCANE);
            }
            return new Pair<>(delivered, currentRecipe.getEldrinRequired() * types);
        }
        return new Pair<>(0, -1);
    }

    public int getProgress() {
        return progress;
    }

    public int getScaledProgress() {
        if(currentRecipe == null || animStage != ANIM_STAGE_CRAFTING) return 0;
        return (progress * 28) / CRAFTING_DURATION;
    }

    public int getScaledItems() {
        if(currentRecipe == null) return 0;
        return itemsDelivered * 46 / currentRecipe.getItemsRequired();
    }

    public int getScaledMateria() {
        if(currentRecipe == null) return 0;
        return materiaDelivered * 46 / currentRecipe.getMateriaRequired();
    }

    public int getScaledSlurry() {
        if(currentRecipe == null) return 0;
        return slurryDelivered * 46 / Math.round(currentRecipe.getSlurryRequired() * (1f - reductionRate));
    }

    public int getScaledEldrin() {
        if(currentRecipe == null) return 0;

        int types = 0;
        int delivered = 0;
        if(currentRecipe.usesEldrinType(ENDER)) {
            types++;
            delivered += eldrinDelivered.get(ENDER);
        }
        if(currentRecipe.usesEldrinType(EARTH)) {
            types++;
            delivered += eldrinDelivered.get(EARTH);
        }
        if(currentRecipe.usesEldrinType(WATER)) {
            types++;
            delivered += eldrinDelivered.get(WATER);
        }
        if(currentRecipe.usesEldrinType(WIND)) {
            types++;
            delivered += eldrinDelivered.get(WIND);
        }
        if(currentRecipe.usesEldrinType(FIRE)) {
            types++;
            delivered += eldrinDelivered.get(FIRE);
        }
        if(currentRecipe.usesEldrinType(ARCANE)) {
            types++;
            delivered += eldrinDelivered.get(ARCANE);
        }

        return delivered * 46 / (currentRecipe.getEldrinRequired() * types);
    }

    public int getScaledEldrinSingle(Affinity pAffinity) {
        if(currentRecipe == null) return 0;
        return eldrinDelivered.get(pAffinity) * 28 / currentRecipe.getEldrinRequired();
    }

    public int getAnimStage() {
        return animStage;
    }

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, PrimeAggregatorBlockEntity pEntity) {
        if(!pLevel.isClientSide()) {
            for(AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
                if(dpbe instanceof ActuatorArcaneBlockEntity arcane) {
                    if(pEntity.animStage == ANIM_STAGE_GATHERING_SLURRY) ActuatorArcaneBlockEntity.delegatedTick(pLevel, pPos, pBlockState, arcane, true);
                    float newReductionRate = arcane.getSlurryReductionRate() / 100f;
                    if(newReductionRate == 0 && pEntity.reductionRate != 0) {
                        pEntity.reductionRate = 0;
                        pEntity.syncAndSave();
                    }
                    else if(newReductionRate != pEntity.reductionRate) {
                        pEntity.reductionRate = newReductionRate;
                        pEntity.syncAndSave();
                    }
                } else if(dpbe instanceof ActuatorEnderBlockEntity ender) {
                    if(pEntity.animStage == ANIM_STAGE_GATHERING_MATERIA) ActuatorEnderBlockEntity.delegatedTick(pLevel, pPos, pBlockState, ender);
                    if (ender.getIsSatisfied() && !ender.getPaused()) {
                            //importing
                            final Map<MateriaItem, Integer> provisioningNeeds = pEntity.getProvisioningNeeds();
                            if (provisioningNeeds != null && provisioningNeeds.size() > 0) {
                                if (ender.getMirrorTarget() instanceof AbstractMateriaStorageMultiTypeDynamicBlockEntity multi) {
                                    for (MateriaItem mi : provisioningNeeds.keySet()) {
                                        int requested = provisioningNeeds.get(mi);
                                        int inStorage = multi.getCurrentStock(mi);
                                        boolean instant = ender.getPowerLevel() == 2;

                                        int actualDrain = Math.min(requested, inStorage);
                                        if (actualDrain > 0) {
                                            multi.drain(mi, actualDrain, false);
                                            ender.createShlorpFromTarget(new ItemStack(mi, actualDrain), instant);

                                            pEntity.setProvisioningInProgress(mi);
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }

        if(pEntity.doDeferredRecipeCheck) {
            boolean changed;
            Item itemQuery = ForgeRegistries.ITEMS.getValue(pEntity.deferredRecipeQuery);
            ExaltationRecipe recipeQuery = ExaltationRecipe.getExaltationRecipe(pLevel, itemQuery);

            if(recipeQuery != null) {
                changed = pEntity.currentRecipe != recipeQuery;
                pEntity.currentRecipe = recipeQuery;
            } else {
                changed = pEntity.currentRecipe != null;
                pEntity.currentRecipe = null;
            }
            pEntity.doDeferredRecipeCheck = false;
            if(changed)
                pEntity.syncAndSave();
        }

        if(pEntity.getCurrentRecipe() != null) {
            //particle work
            if(pLevel.isClientSide()) {
                if(pEntity.animStage == ANIM_STAGE_GATHERING_ITEMS) {
                    float progThroughPhase = (float)pEntity.itemsDelivered / (float)pEntity.currentRecipe.getItemsRequired();
                    int particlesPerTick = progThroughPhase > 0.2 ? (int)Math.min(4,Math.max(1,Math.floor(progThroughPhase * 5) - 1)) : 1;
                    int modulus = progThroughPhase > 0.2 ? 1 : (int)Math.round((1 - (progThroughPhase / 0.2)) * 9) + 1;

                    if(pLevel.getGameTime() % modulus == 0) {
                        for (int i = 0; i < particlesPerTick; i++) {
                            Vector3 start = new Vector3(pEntity.getBlockPos().getX() + 0.5, pEntity.getBlockPos().getY() + 0.5, pEntity.getBlockPos().getZ() + 0.5);
                            Vector3 speed = new Vector3(r.nextDouble() - 0.5, 0, r.nextDouble() - 0.5).normalize().scale(0.16f + r.nextFloat(1.2f));

                            pLevel.addParticle(new MAParticleType(ParticleInit.ITEM.get())
                                            .setMover(new ParticleOrbitMover(new Vec3(start.x, start.y, start.z), -0.03, 0.015 + r.nextDouble(0.025), 0.02 + r.nextDouble(0.28)))
                                            .setScale(0.05f).setMaxAge(20 + r.nextInt(240))
                                            .setStack(pEntity.currentRecipe.getInputItemAsStack()).setPhysics(false),
                                    start.x, start.y, start.z,
                                    speed.x, speed.y, speed.z);
                        }
                    }
                } else if(pEntity.animStage == ANIM_STAGE_TO_MATERIA) {
                    Vector3 center = new Vector3(pEntity.getBlockPos().getX() + 0.5, pEntity.getBlockPos().getY() + 1.8125, pEntity.getBlockPos().getZ() + 0.5);
                    float progThroughPhase = (float)pEntity.progress / TO_MATERIA_DURATION;
                    int particlesPerTick = progThroughPhase > 0.333 ? (int)Math.min(4,Math.max(1,Math.floor(progThroughPhase * 10) - 1)) : 1;
                    int modulus = progThroughPhase > 0.333 ? 1 : (int)Math.round((1 - (progThroughPhase / 0.333)) * 9) + 1;

                    //ITEM CHUNKS
                    if(progThroughPhase <= 0.625f){
                        for (int i = 0; i < 4; i++) {
                            Vector3 start = new Vector3(pEntity.getBlockPos().getX() + 0.5, pEntity.getBlockPos().getY() + 0.5, pEntity.getBlockPos().getZ() + 0.5);
                            Vector3 speed = new Vector3(r.nextDouble() - 0.5, 0, r.nextDouble() - 0.5).normalize().scale(0.16f + r.nextFloat(1.2f));

                            pLevel.addParticle(new MAParticleType(ParticleInit.ITEM.get())
                                            .setMover(new ParticleOrbitMover(new Vec3(start.x, start.y, start.z), -0.03, 0.015 + r.nextDouble(0.025), 0.02 + r.nextDouble(0.28)))
                                            .setScale(0.05f).setMaxAge(20 + r.nextInt(240))
                                            .setStack(pEntity.currentRecipe.getInputItemAsStack()).setPhysics(false),
                                    start.x, start.y, start.z,
                                    speed.x, speed.y, speed.z);
                        }
                    }

                    if(pLevel.getGameTime() % modulus == 0) {
                        for (int i = 0; i < particlesPerTick; i++) {
                            Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.875f);
                            pLevel.addParticle(new MAParticleType(ParticleInit.ITEM.get())
                                            .setStack(pEntity.currentRecipe.getInputItemAsStack())
                                            .setScale(0.05f).setMaxAge(16)
                                            .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                    center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                    0, 0, 0);
                        }
                    }

                    //SPHERE
                    pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(255, 255, 255).setScale(0.2f * progThroughPhase),
                            center.x, center.y, center.z,
                            0, 0, 0);

                    for (int i = 0; i < 3; i++) {
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                        pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(30, 78, 121, Math.round(64 * progThroughPhase))
                                        .setScale(0.09f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }
                } else if(pEntity.animStage == ANIM_STAGE_GATHERING_MATERIA) {
                    float progThroughPhase = (float)pEntity.materiaDelivered / pEntity.getCurrentRecipe().getMateriaRequired();
                    int modulus = progThroughPhase > 0.666 ? 2 : (int)Math.round((1 - (progThroughPhase / 0.666)) * 9) + 2;
                    int segments = progThroughPhase > 0.666 ? 6 : (int)Math.round((progThroughPhase / 0.666) * 5) + 1;
                    Vector3 center = new Vector3(pEntity.getBlockPos().getX() + 0.5, pEntity.getBlockPos().getY() + 1.8125, pEntity.getBlockPos().getZ() + 0.5);

                    //SPHERE
                    pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(255, 255, 255).setScale(0.2f),
                            center.x, center.y, center.z,
                            0, 0, 0);

                    for (int i = 0; i < 3; i++) {
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                        pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(30, 78, 121, 64)
                                        .setScale(0.09f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }

                    //LIGHTNING
                    if(pLevel.getGameTime() % modulus == 0) {
                        Vector3[] offsets = CRYSTAL_POSITIONS.get(pBlockState.getValue(MagiChemBlockStateProperties.FACING));
                        BlockPos pos = pEntity.getBlockPos();
                        int firstSegment = r.nextInt(6);
                        for(int i=0;i<segments;i++) {
                            int start = (firstSegment+i) % 6;
                            int end = (firstSegment+i+1) % 6;

                            pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                            .setMaxAge(8 + r.nextInt(8))
                                            .setColor(43, 113, 175, 255),
                                    pos.getX() + offsets[start].x, pos.getY() + offsets[start].y, pos.getZ() + offsets[start].z,
                                    pos.getX() + offsets[end].x, pos.getY() + offsets[end].y, pos.getZ() + offsets[end].z);
                        }
                    }

                } else if(pEntity.animStage == ANIM_STAGE_TO_ELDRIN) {
                    float progThroughPhase = (float)pEntity.progress / TO_ELDRIN_DURATION;
                    Vector3 center = new Vector3(pEntity.getBlockPos().getX() + 0.5, pEntity.getBlockPos().getY() + 1.8125 + MathHelper.doubleExponentialSeat(progThroughPhase, 3) * 1.1875, pEntity.getBlockPos().getZ() + 0.5);

                    //SPHERE
                    pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(255, 255, 255).setScale(0.2f * progThroughPhase).setMaxAge(20),
                            center.x, center.y, center.z,
                            0, 0, 0);

                    for (int i = 0; i < 3; i++) {
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                        pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(30, 78, 121, 64)
                                        .setScale(0.09f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }

                    //LIGHTNING
                    if(pLevel.getGameTime() % 2 == 0){
                        Vector3[] offsets = CRYSTAL_POSITIONS.get(pBlockState.getValue(MagiChemBlockStateProperties.FACING));
                        BlockPos pos = pEntity.getBlockPos();

                        //TO ORB
                        for (int i = 0; i < 6; i += 2) {
                            pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                            .setMaxAge(8 + r.nextInt(8))
                                            .setColor(43, 113, 175, 255),
                                    pos.getX() + offsets[i].x, pos.getY() + offsets[i].y, pos.getZ() + offsets[i].z,
                                    center.x, center.y, center.z);
                        }
                        //TO VOID
                        for (int i = 1; i < 6; i += 2) {
                            pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                            .setMaxAge(8 + r.nextInt(8))
                                            .setColor(43, 113, 175, 255),
                                    pos.getX() + offsets[i].x, pos.getY() + offsets[i].y, pos.getZ() + offsets[i].z,
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        }
                        //VOID TO ORB
                        for(int i=0; i<3; i++) {
                            pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                            .setMaxAge(4)
                                            .setColor(43, 113, 175, 255),
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    center.x, center.y, center.z);
                        }
                    }
                } else if(pEntity.animStage == ANIM_STAGE_GATHERING_ELDRIN) {
                    Pair<Integer, Integer> eldrinData = pEntity.getEldrin();
                    float progThroughPhase = (float)eldrinData.getFirst() / (float)eldrinData.getSecond();
                    float period = 0.333f;
                    int particlesPerTick = progThroughPhase > period ? 1 : (int)Math.min(2,Math.max(0,Math.floor((1 - (progThroughPhase / period)) * 2) + 1));
                    int modulus = progThroughPhase > period ? Math.round(((progThroughPhase - period) / period) * 9) + 1 : 1;
                    BlockPos pos = pEntity.getBlockPos();
                    Vector3 center = new Vector3(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5);

                    //SPHERE
                    pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(255, 255, 255).setScale(0.2f + 0.4f * progThroughPhase),
                            center.x, center.y, center.z,
                            0, 0, 0);

                    for (int i = 0; i < 3; i++) {
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f + 0.1f * progThroughPhase);
                        pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(30, 78, 121, 64)
                                        .setScale(0.09f + 0.09f * progThroughPhase).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }

                    //VOID TO ORB
                    if(pLevel.getGameTime() % modulus == 0) {
                        for (int i = 0; i < particlesPerTick; i++) {
                            pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                            .setMaxAge(4 + Math.round(progThroughPhase * 4))
                                            .setColor(43, 113, 175, 255),
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    center.x, center.y, center.z);
                        }
                    }
                } else if(pEntity.animStage == ANIM_STAGE_TO_SLURRY) {
                    float progThroughPhase = (float)pEntity.progress / (float)TO_SLURRY_DURATION;
                    Vector3 center = new Vector3(pEntity.getBlockPos().getX() + 0.5, pEntity.getBlockPos().getY() + 1.8125 + MathHelper.doubleExponentialSeat(1 - progThroughPhase, 3) * 1.1875, pEntity.getBlockPos().getZ() + 0.5);

                    //SPHERE
                    pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(255, 255, 255).setScale(0.6f).setMaxAge(20),
                            center.x, center.y, center.z,
                            0, 0, 0);

                    for (int i = 0; i < 3; i++) {
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.4f);
                        pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(30, 78, 121, 64)
                                        .setScale(0.18f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }
                } else if(pEntity.animStage == ANIM_STAGE_GATHERING_SLURRY) {
                    float progThroughPhase = (float)pEntity.slurryDelivered / (float)Math.round(pEntity.currentRecipe.getSlurryRequired() * (1f - pEntity.reductionRate));
                    Vector3 center = new Vector3(pEntity.getBlockPos().getX() + 0.5, pEntity.getBlockPos().getY() + 1.8125, pEntity.getBlockPos().getZ() + 0.5);
                    int modulus = progThroughPhase > 0.666 ? 1 : (int)Math.round((1 - (progThroughPhase / 0.666)) * 14) + 1;

                    //SPHERE
                    pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(255, 255, 255).setScale(0.6f).setMaxAge(20),
                            center.x, center.y, center.z,
                            0, 0, 0);

                    for (int i = 0; i < 3; i++) {
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.4f);
                        pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(30, 78, 121, 64)
                                        .setScale(0.18f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }

                    if(pLevel.getGameTime() % modulus == 0) {
                        BlockPos pos = pEntity.getBlockPos();
                        int loopingTime = (int) (pLevel.getGameTime() % 12);
                        double theta = ((double) loopingTime / 6.0) * Math.PI + Math.PI / 4.0;
                        double scale = 0.2;

                        Vector3 shift = new Vector3(Math.cos(-theta) * scale, 0, Math.sin(-theta) * scale);

                        pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                        .setColor(188, 232, 95).setScale(0.2f).setMaxAge(15 + r.nextInt(15)),
                                pos.getX() + shift.x + 0.5, pos.getY() + 0.3125, pos.getZ() + shift.z + 0.5,
                                center.x, center.y, center.z);

                        scale = 0.4;
                        shift = new Vector3(Math.cos(theta) * scale, 0, Math.sin(theta) * scale);

                        pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                        .setColor(188, 232, 95).setScale(0.2f).setMaxAge(15 + r.nextInt(15)),
                                pos.getX() + shift.x + 0.5, pos.getY() + 0.3125, pos.getZ() + shift.z + 0.5,
                                center.x, center.y, center.z);
                    }
                } else if(pEntity.animStage == ANIM_STAGE_CRAFTING) {
                    float progThroughPhase = (float)pEntity.progress / (float)CRAFTING_DURATION;
                    Vector3 center = new Vector3(pEntity.getBlockPos().getX() + 0.5, pEntity.getBlockPos().getY() + 1.8125, pEntity.getBlockPos().getZ() + 0.5);

                    pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(255, 255, 255).setScale(0.6f).setMaxAge(20),
                            center.x, center.y, center.z,
                            0, 0, 0);

                    for (int i = 0; i < 3; i++) {
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.4f);
                        pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(30, 78, 121, 64)
                                        .setScale(0.18f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }

                    //ITEM CHUNKS
                    if(progThroughPhase < 0.875f){
                        for (int i = 0; i < 4; i++) {
                            Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.875f);
                            pLevel.addParticle(new MAParticleType(ParticleInit.ITEM.get())
                                            .setStack(pEntity.currentRecipe.getResultItem())
                                            .setScale(0.05f).setMaxAge(16)
                                            .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                    center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                    0, 0, 0);
                        }
                    }
                }
            }

            if (pEntity.animStage == ANIM_STAGE_TO_MATERIA) {
                pEntity.progress++;

                if (pEntity.progress >= TO_MATERIA_DURATION) {
                    pEntity.animStage = ANIM_STAGE_GATHERING_MATERIA;
                }
            }
            else if (pEntity.animStage == ANIM_STAGE_TO_SLURRY) {
                pEntity.progress++;

                if (pEntity.progress >= TO_SLURRY_DURATION) {
                    pEntity.animStage = ANIM_STAGE_GATHERING_SLURRY;
                }
            }
            else if (pEntity.animStage == ANIM_STAGE_TO_ELDRIN) {
                pEntity.progress++;

                if (pEntity.progress >= TO_ELDRIN_DURATION) {
                    pEntity.animStage = ANIM_STAGE_GATHERING_ELDRIN;
                }
            }
            else if (pEntity.animStage == ANIM_STAGE_CRAFTING) {
                if (pEntity.canCraftItem() && pEntity.progress < CRAFTING_DURATION) {
                    pEntity.progress++;

                    if (!pLevel.isClientSide() && pEntity.progress >= CRAFTING_DURATION) {
                        pEntity.craftItem();
                        pEntity.clearDeliveries();
                        pEntity.progress = 0;
                        pEntity.animStage = ANIM_STAGE_IDLE;
                        pEntity.syncAndSave();
                    }
                }
            }
            else if (!pLevel.isClientSide()) {
                if (pEntity.animStage == ANIM_STAGE_IDLE || pEntity.animStage == ANIM_STAGE_GATHERING_ITEMS) {
                    boolean changed = false;

                    ItemStack itemQuery = pEntity.itemHandler.getStackInSlot(SLOT_ITEM_INPUT);
                    if (!itemQuery.isEmpty() && itemQuery.getItem() == pEntity.currentRecipe.getItemType()) {
                        int remaining = pEntity.currentRecipe.getItemsRequired() - pEntity.itemsDelivered;
                        int extraction = Math.min(itemQuery.getCount(), remaining);

                        if (extraction > 0) {
                            pEntity.itemsDelivered += extraction;
                            itemQuery.shrink(extraction);
                            changed = true;

                            if(pEntity.itemHandler.getStackInSlot(SLOT_PROGRESS_HOLDER).isEmpty()) {
                                pEntity.itemHandler.setStackInSlot(SLOT_PROGRESS_HOLDER, new ItemStack(ItemRegistry.EXALTATION_IN_PROGRESS.get()));
                            }

                            if (pEntity.itemsDelivered >= pEntity.currentRecipe.getItemsRequired()) {
                                pEntity.progress = 0;
                                pEntity.animStage = ANIM_STAGE_TO_MATERIA;
                            } else if (pEntity.itemsDelivered > 0) {
                                pEntity.animStage = ANIM_STAGE_GATHERING_ITEMS;
                            }
                        }
                    }

                    if (changed) {
                        pEntity.syncAndSave();
                    }
                } else if (pEntity.animStage == ANIM_STAGE_GATHERING_MATERIA) {
                    boolean changed = false;

                    ItemStack materiaQuery = pEntity.itemHandler.getStackInSlot(SLOT_MATERIA_INPUT);
                    ItemStack bottleQuery = pEntity.itemHandler.getStackInSlot(SLOT_BOTTLES_OUTPUT);
                    if (!materiaQuery.isEmpty() && materiaQuery.getItem() == pEntity.currentRecipe.getMateriaType()) {
                        int bottleSpace = bottleQuery.isEmpty() ? 64 : bottleQuery.getMaxStackSize() - bottleQuery.getCount();
                        int remaining = pEntity.currentRecipe.getMateriaRequired() - pEntity.materiaDelivered;
                        int extraction = Math.min(Math.min(materiaQuery.getCount(), remaining), bottleSpace);

                        if (extraction > 0) {
                            pEntity.materiaDelivered += extraction;
                            if (!InventoryHelper.isMateriaUnbottled(materiaQuery)) {
                                if (bottleQuery.isEmpty()) {
                                    pEntity.itemHandler.setStackInSlot(SLOT_BOTTLES_OUTPUT, new ItemStack(Items.GLASS_BOTTLE, extraction));
                                } else {
                                    bottleQuery.grow(extraction);
                                }
                            }
                            materiaQuery.shrink(extraction);
                            changed = true;
                        }
                    }

                    if (pEntity.materiaDelivered >= pEntity.currentRecipe.getMateriaRequired()) {
                        pEntity.progress = 0;
                        pEntity.provisioningInProgress = false;
                        pEntity.animStage = ANIM_STAGE_TO_ELDRIN;
                        changed = true;
                    }

                    if (changed) {
                        pEntity.syncAndSave();
                    }
                } else if (pEntity.animStage == ANIM_STAGE_GATHERING_ELDRIN) {
                    boolean changed = false;
                    boolean complete = false;

                    if (pEntity.getOwner() != null) {
                        int maxDrainPerTick = Math.max(1, pEntity.currentRecipe.getEldrinRequired() / 4);

                        if(pLevel.getGameTime() % 20 == 0) {
                            complete = true;
                            for (Affinity affinity : pEntity.currentRecipe.getEldrinTypes()) {
                                float consumedRaw = pEntity.consume(pEntity.getOwner(), pEntity.getBlockPos(), pEntity.getBlockPos().getCenter(), affinity, Math.min(maxDrainPerTick, pEntity.currentRecipe.getEldrinRequired() - pEntity.eldrinDelivered.get(affinity)), 1);
                                if (consumedRaw > 0) {
                                    int consumed = (int) Math.ceil(consumedRaw);
                                    int updated = Math.min(pEntity.eldrinDelivered.get(affinity) + consumed, pEntity.currentRecipe.getEldrinRequired());
                                    pEntity.eldrinDelivered.put(affinity, updated);

                                    changed = true;
                                }
                                complete &= pEntity.eldrinDelivered.get(affinity) >= pEntity.currentRecipe.getEldrinRequired();
                            }
                        }
                    }

                    if (complete) {
                        pEntity.progress = 0;
                        pEntity.animStage = ANIM_STAGE_TO_SLURRY;
                    }

                    if (changed) {
                        pEntity.syncAndSave();
                    }
                } else if (pEntity.animStage == ANIM_STAGE_GATHERING_SLURRY) {
                    boolean changed = false;

                    if(pLevel.getGameTime() % 5 == 0){
                        if (!pEntity.containedSlurry.isEmpty() && pEntity.containedSlurry.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get()) {
                            int remaining = Math.round(pEntity.currentRecipe.getSlurryRequired() * (1f - pEntity.reductionRate)) - pEntity.slurryDelivered;
                            int extraction = Math.min(pEntity.containedSlurry.getAmount(), remaining);
                            int limit = Math.max(1, Math.round(pEntity.currentRecipe.getSlurryRequired() * (1f - pEntity.reductionRate)) / 16);
                            extraction = Math.min(extraction, limit);

                            if (extraction > 0) {
                                pEntity.slurryDelivered += extraction;
                                pEntity.containedSlurry.shrink(extraction);
                                changed = true;
                            }
                        }

                        if (pEntity.slurryDelivered >= Math.round(pEntity.currentRecipe.getSlurryRequired() * (1f - pEntity.reductionRate))) {
                            pEntity.progress = 0;
                            pEntity.animStage = ANIM_STAGE_CRAFTING;
                        }
                    }

                    if (changed) {
                        pEntity.syncAndSave();
                    }
                }
            }
        }

        //deferred plugin linkage
        if(!pLevel.isClientSide()) {
            if (pEntity.pluginLinkageCountdown == 0) {
                pEntity.pluginLinkageCountdown = -1;
                pEntity.linkPlugins();
            } else if (pEntity.pluginLinkageCountdown > 0) {
                pEntity.pluginLinkageCountdown--;
            }
        }
    }

    public SimpleContainer getContentsOfOutputSlots() {
        SimpleContainer output = new SimpleContainer(SLOT_OUTPUT_COUNT);

        for(int i = SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            output.setItem(i-SLOT_OUTPUT_START, itemHandler.getStackInSlot(i));
        }

        return output;
    }

    public ItemStack getFirstOutputItem() {
        for(int i=SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            if(!itemHandler.getStackInSlot(i).isEmpty()) return itemHandler.getStackInSlot(i);
        }

        return ItemStack.EMPTY;
    }

    private boolean canCraftItem() {
        SimpleContainer output = getContentsOfOutputSlots();
        boolean valid = false;

        for(int i=0; i<output.getContainerSize(); i++) {
            ItemStack query = output.getItem(i);
            if(query.isEmpty()) valid = true;
            else if(query.getItem() == currentRecipe.getResultItem().getItem()) {
                int space = currentRecipe.getResultItem().getMaxStackSize() - query.getCount();
                valid = space >= currentRecipe.getResultItem().getCount();
            }

            if(valid) break;
        }

        return valid;
    }

    private void craftItem() {
        SimpleContainer output = getContentsOfOutputSlots();
        output.addItem(currentRecipe.getResultItem().copy());

        for(int i=0; i<SLOT_OUTPUT_COUNT; i++) {
            itemHandler.setStackInSlot(SLOT_OUTPUT_START+i, output.getItem(i));
        }
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
        return ServerConfig.primeAggregatorTankCapacity;
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

    public static int getScaledTankSlurry(int pSlurry) {
        return (36 * pSlurry) / ServerConfig.primeAggregatorTankCapacity;
    }

    ////////////////////
    // SHLORP HANDLING
    ////////////////////

    private boolean provisioningInProgress = false;

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        return currentRecipe != null && animStage == ANIM_STAGE_GATHERING_MATERIA && materiaDelivered < currentRecipe.getMateriaRequired();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        HashMap<MateriaItem, Integer> needs = new HashMap<>();

        if(currentRecipe != null && animStage == ANIM_STAGE_GATHERING_MATERIA && !provisioningInProgress) {
            int materiaNeeded = currentRecipe.getMateriaRequired() - materiaDelivered;
            if(materiaNeeded > 0)
                needs.put(currentRecipe.getMateriaType(), Math.min(64,materiaNeeded));
        }

        return needs;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(currentRecipe != null && animStage == ANIM_STAGE_GATHERING_MATERIA && pMateriaItem == currentRecipe.getMateriaType())
            provisioningInProgress = true;
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        if(currentRecipe != null && currentRecipe.getMateriaType() == pMateriaItem)
            provisioningInProgress = false;
    }

    @Override
    public void provide(ItemStack pStack) {
        if(currentRecipe != null && pStack.getItem() == currentRecipe.getMateriaType()) {
            materiaDelivered += pStack.getCount();
            provisioningInProgress = false;
            syncAndSave();
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(currentRecipe != null && pStack.getItem() == currentRecipe.getMateriaType()) return 0;
        return pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(currentRecipe != null && pStack.getItem() == currentRecipe.getMateriaType()) {
            provide(pStack);
            return 0;
        }
        return pStack.getCount();
    }

    ////////////////////
    // ACTUATOR HANDLING
    ////////////////////

    @Override
    public void linkPluginsDeferred() {
        pluginLinkageCountdown = 3;
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();

        List<BlockEntity> query = new ArrayList<>();
        for(Triplet<BlockPos, PrimeAggregatorRouterType, DevicePlugDirection> posAndType : PrimeAggregatorBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof PrimeAggregatorRouterBlockEntity parbe) {
                BlockEntity pe = parbe.getPlugEntity();
                if(pe instanceof AbstractDirectionalPluginBlockEntity dpbe) {
                    final ICanTakePlugins targetMachine = dpbe.getTargetMachine();
                    if(targetMachine instanceof PrimeAggregatorRouterBlockEntity router) {
                        PrimeAggregatorBlockEntity master = router.getMaster();
                        if(master == this) pluginDevices.add(dpbe);
                    }
                }
            }
        }

        for (AbstractDirectionalPluginBlockEntity device : pluginDevices) {
            if(device instanceof ActuatorArcaneBlockEntity arcane) ActuatorArcaneBlockEntity.delegatedTick(level, getBlockPos(), getBlockState(), arcane, true);
            if(device instanceof ActuatorEnderBlockEntity ender) ActuatorEnderBlockEntity.delegatedTick(level, getBlockPos(), getBlockState(), ender);
        }

    }

    @Override
    public void removePlugin(AbstractDirectionalPluginBlockEntity pPlugin) {
        this.pluginDevices.remove(pPlugin);
        if(pPlugin instanceof ActuatorArcaneBlockEntity) {
            reductionRate = 0.0f;
        }
        syncAndSave();
    }

    @Override
    public void destroyRouters() {
        PrimeAggregatorBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    @Override
    public byte setRecipe(ItemStack pStack, Player player) {
        if(pStack.isEmpty()) clearRecipe();
        ExaltationRecipe recipePre = currentRecipe;
        ExaltationRecipe recipeQuery = ExaltationRecipe.getExaltationRecipe(level, pStack.getItem());

        if(recipeQuery == null) {
            return ERROR_CODE_NO_SUCH_RECIPE;
        } else if(recipePre == recipeQuery) {
            return ERROR_CODE_SUCCESS;
        } else {
            this.currentRecipe = recipeQuery;
            this.clearDeliveries();
            this.animStage = ANIM_STAGE_IDLE;
            this.syncAndSave();
            return ERROR_CODE_SUCCESS;
        }
    }

    @Override
    public ItemStack getRecipeItem() {
        return currentRecipe.getResultItem();
    }

    @Override
    public ItemStack getRecipeItem(boolean pMakeCopy) {
        return pMakeCopy ? currentRecipe.getResultItem().copy() : currentRecipe.getResultItem();
    }

    @Override
    public void packDataToBlockItem() {

    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {

    }
}
