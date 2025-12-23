package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.gui.PrimeAggregatorMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.ExaltationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
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
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            TO_MATERIA_DURATION = 40, TO_SLURRY_DURATION = 40, TO_ELDRIN_DURATION = 40, CRAFTING_DURATION = 60;
    public boolean clearRecipeAfterNextProcess = false;

    private Player owner;
    private UUID ownerUUID;
    private int animStage = ANIM_STAGE_IDLE, itemsDelivered = 0, materiaDelivered = 0, slurryDelivered = 0, progress = 0;
    private HashMap<Affinity, Integer> eldrinDelivered = new HashMap<>();
    private boolean doDeferredRecipeCheck = false;
    private ExaltationRecipe currentRecipe = null;
    private ResourceLocation deferredRecipeQuery = null;
    private FluidStack containedSlurry = FluidStack.EMPTY.copy();

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.of(() -> this);
    private final ItemStackHandler itemHandler;

    public PrimeAggregatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.PRIME_AGGREGATOR_BE.get(), pPos, pBlockState);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_PROGRESS_HOLDER) return false;
                if(currentRecipe != null) {
                    if(slot == SLOT_ITEM_INPUT) return stack.getItem() == currentRecipe.getItemType();
                    else if(slot == SLOT_MATERIA_INPUT) return stack.getItem() == currentRecipe.getMateriaType();
                }

                return false;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };

        clearDeliveries();
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
//        linkPlugins();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putBoolean("clearRecipeAfterNextProcess", this.clearRecipeAfterNextProcess);
        nbt.putInt("progress", progress);
        nbt.putInt("animStage", animStage);
        nbt.putInt("fluidContents", this.containedSlurry.getAmount());

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
        if(currentRecipe != null) return new Pair<>(slurryDelivered, currentRecipe.getSlurryRequired());
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
        return slurryDelivered * 46 / currentRecipe.getSlurryRequired();
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

                            if (pEntity.materiaDelivered >= pEntity.currentRecipe.getMateriaRequired()) {
                                pEntity.progress = 0;
                                pEntity.animStage = ANIM_STAGE_TO_ELDRIN;
                            }
                        }
                    }

                    if (changed) {
                        pEntity.syncAndSave();
                    }
                } else if (pEntity.animStage == ANIM_STAGE_GATHERING_ELDRIN) {
                    boolean changed = false;
                    boolean complete = true;

                    if (pEntity.getOwner() != null) {
                        for (Affinity affinity : pEntity.currentRecipe.getEldrinTypes()) {
                            float consumedRaw = pEntity.consume(pEntity.getOwner(), pEntity.getBlockPos(), pEntity.getBlockPos().getCenter(), affinity, pEntity.currentRecipe.getEldrinRequired() - pEntity.eldrinDelivered.get(affinity), 1);
                            if (consumedRaw > 0) {
                                int consumed = (int) Math.ceil(consumedRaw);
                                int updated = Math.min(pEntity.eldrinDelivered.get(affinity) + consumed, pEntity.currentRecipe.getEldrinRequired());
                                pEntity.eldrinDelivered.put(affinity, updated);

                                changed = true;
                            }
                            complete &= pEntity.eldrinDelivered.get(affinity) >= pEntity.currentRecipe.getEldrinRequired();
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

                    if (!pEntity.containedSlurry.isEmpty() && pEntity.containedSlurry.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get()) {
                        int remaining = pEntity.currentRecipe.getSlurryRequired() - pEntity.slurryDelivered;
                        int extraction = Math.min(pEntity.containedSlurry.getAmount(), remaining);

                        if (extraction > 0) {
                            pEntity.slurryDelivered += extraction;
                            pEntity.containedSlurry.shrink(extraction);
                            changed = true;

                            if (pEntity.slurryDelivered >= pEntity.currentRecipe.getSlurryRequired()) {
                                pEntity.progress = 0;
                                pEntity.animStage = ANIM_STAGE_CRAFTING;
                            }
                        }
                    }

                    if (changed) {
                        pEntity.syncAndSave();
                    }
                }
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
        return currentRecipe != null && animStage == ANIM_STAGE_GATHERING_MATERIA;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        HashMap<MateriaItem, Integer> needs = new HashMap<>();

        if(currentRecipe != null && animStage == ANIM_STAGE_GATHERING_MATERIA && !provisioningInProgress) {
            int materiaNeeded = currentRecipe.getMateriaRequired() - materiaDelivered;
            if(materiaNeeded > 0)
                needs.put(currentRecipe.getMateriaType(), materiaNeeded);
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
            ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_MATERIA_INPUT);

            if(insertionStack.isEmpty()) {
                insertionStack = pStack.copy();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("CustomModelData", 1);
                insertionStack.setTag(nbt);
            } else {
                insertionStack.grow(pStack.getCount());
            }
            itemHandler.setStackInSlot(SLOT_MATERIA_INPUT, insertionStack);

            syncAndSave();

            provisioningInProgress = false;
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

    }

    @Override
    public void linkPlugins() {

    }

    @Override
    public void removePlugin(AbstractDirectionalPluginBlockEntity pPlugin) {

    }

    @Override
    public void destroyRouters() {

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
