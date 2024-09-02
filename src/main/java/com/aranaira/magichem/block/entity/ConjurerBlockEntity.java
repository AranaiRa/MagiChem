package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IRequiresRouterCleanupOnDestruction;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.gui.ConjurerMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.ConjurationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ConjurerBlockEntity extends BlockEntity implements MenuProvider, IRequiresRouterCleanupOnDestruction, IShlorpReceiver, IMateriaProvisionRequester {

    protected LazyOptional<IItemHandler>
            lazyInsertionItemHandler = LazyOptional.empty(),
            lazyExtractionItemHandler = LazyOptional.empty(),
            lazyCombinedItemHandler = LazyOptional.empty();
    protected ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return 0;
        }

        @Override
        public void set(int pIndex, int pValue) {

        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };
    private static final Random r = new Random();

    public static final int
        SLOT_INSERTION_COUNT = 2, SLOT_INSERTION_CATALYST = 0, SLOT_INSERTION_MATERIA = 1,
        SLOT_EXTRACTION_COUNT = 2, SLOT_EXTRACTION_OUTPUT = 0, SLOT_EXTRACTION_BOTTLES = 1,
        DATA_COUNT = 0;
    private int
        progress, materiaAmount;
    private MateriaItem
        materiaType;
    private ConjurationRecipe recipe;
    public static HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(false, false);

    private final ItemStackHandler itemInsertionHandler = new ItemStackHandler(SLOT_INSERTION_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_INSERTION_CATALYST) {
                ConjurationRecipe recipeQuery = ConjurationRecipe.getConjurationRecipe(level, stack);
                return recipeQuery != null;
            } else if(slot == SLOT_INSERTION_MATERIA) {
                if(recipe == null)
                    return false;

                if(stack.getItem() instanceof MateriaItem mi) {
                    return recipe.getMateria() == mi;
                }

                return false;
            }

            return super.isItemValid(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            if(slot == SLOT_INSERTION_CATALYST)
                return 1;

            return super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    private final ItemStackHandler itemExtractionHandler = new ItemStackHandler(SLOT_EXTRACTION_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    public ConjurerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CONJURER_BE.get(), pos, state);
    }

    public ConjurerBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyInsertionItemHandler = LazyOptional.of(() -> itemInsertionHandler);
        lazyExtractionItemHandler = LazyOptional.of(() -> itemExtractionHandler);
        lazyCombinedItemHandler = LazyOptional.of(() -> new CombinedInvWrapper(itemInsertionHandler, itemExtractionHandler));
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            if(side == null)
                return lazyCombinedItemHandler.cast();
            else if(side == Direction.UP)
                return lazyInsertionItemHandler.cast();
            else
                return lazyExtractionItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyInsertionItemHandler.invalidate();
        lazyExtractionItemHandler.invalidate();
        lazyCombinedItemHandler.invalidate();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ConjurerMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("insertionInventory", itemInsertionHandler.serializeNBT());
        nbt.put("extractionInventory", itemExtractionHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        if(this.materiaType == null)
            nbt.putString("materiaType", "null");
        else
            nbt.putString("materiaType", this.materiaType.getMateriaName());
        nbt.putInt("materiaAmount", this.materiaAmount);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemInsertionHandler.deserializeNBT(nbt.getCompound("insertionInventory"));
        itemExtractionHandler.deserializeNBT(nbt.getCompound("extractionInventory"));
        progress = nbt.getInt("craftingProgress");
        String materiaQuery = nbt.getString("materiaType");
        materiaType = materiaMap.get(materiaQuery);
        materiaAmount = nbt.getInt("materiaAmount");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("insertionInventory", itemInsertionHandler.serializeNBT());
        nbt.put("extractionInventory", itemExtractionHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        if(this.materiaType == null)
            nbt.putString("materiaType", "null");
        else
            nbt.putString("materiaType", this.materiaType.getMateriaName());
        nbt.putInt("materiaAmount", this.materiaAmount);

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

    public int getMateriaAmount() {
        return materiaAmount;
    }

    public MateriaItem getMateriaType() {
        return materiaType;
    }

    public ConjurationRecipe getRecipe() {
        return recipe;
    }

    public int getScaledProgress() {
        if(recipe == null)
            return 0;

        boolean isPassiveMode = true;
        if(materiaAmount > 0 && materiaType == recipe.getMateria())
            isPassiveMode = false;

        if(isPassiveMode)
            return 28 * progress / recipe.getPassiveData(false).getSecond();
        else
            return 28 * progress / recipe.getSuppliedData(false).getSecond();
    }

    public int getScaledMateria() {
        return (16 * Math.min(Config.conjurerMateriaCapacity, materiaAmount)) / Config.conjurerMateriaCapacity;
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, ConjurerBlockEntity entity) {
        ItemStack catalystStack = entity.itemInsertionHandler.getStackInSlot(SLOT_INSERTION_CATALYST);
        boolean syncThisTick = false;
        if(entity.recipe == null) {
            if(!catalystStack.isEmpty()) {
                ConjurationRecipe newRecipe = ConjurationRecipe.getConjurationRecipe(level, catalystStack);

                if (newRecipe != null) {
                    if (entity.materiaType != newRecipe.getMateria()) {
                        entity.materiaType = null;
                        entity.materiaAmount = 0;
                    }

                    entity.recipe = newRecipe;
                    syncThisTick = true;
                }
            }
        } else if(catalystStack.getItem() != entity.recipe.getCatalyst()) {
            entity.recipe = null;
            syncThisTick = true;
        } else {
            //Materia insertion
            if(!level.isClientSide()){
                ItemStack insert = entity.itemInsertionHandler.getStackInSlot(SLOT_INSERTION_MATERIA);
                ItemStack bottles = entity.itemExtractionHandler.getStackInSlot(SLOT_EXTRACTION_BOTTLES);

                if(insert.getItem() == entity.recipe.getMateria()) {
                    boolean allowInsertion = false;
                    if(entity.materiaType == null) allowInsertion = true;
                    else if(entity.materiaType == insert.getItem()) allowInsertion = true;

                    if(allowInsertion) {
                        if(bottles.getCount() < entity.itemExtractionHandler.getSlotLimit(SLOT_EXTRACTION_BOTTLES)) {
                            //Allow overfilling the gauge by one item for GUI aesthetic reasons
                            if(entity.materiaAmount < Config.conjurerMateriaCapacity) {
                                entity.materiaAmount += Config.conjurerPointsPerDram;
                                entity.materiaType = (MateriaItem)insert.getItem();

                                insert.shrink(1);
                                if(bottles.isEmpty()) {
                                    bottles = new ItemStack(Items.GLASS_BOTTLE, 1);
                                } else {
                                    bottles.grow(1);
                                }
                                entity.itemExtractionHandler.setStackInSlot(SLOT_EXTRACTION_BOTTLES, bottles);
                                syncThisTick = true;
                            }
                        }
                    }
                }
            }
            //Particles
            else {
                entity.generateParticles();
            }

            //Crafting logic
            {
                boolean isPassiveMode = true;
                if (entity.materiaAmount > 0 && entity.materiaType == entity.recipe.getMateria())
                    isPassiveMode = false;

                if (canCraftItem(entity, isPassiveMode)) {
                    entity.progress++;

                    if (isPassiveMode) {
                        final Pair<ItemStack, Integer> data = entity.recipe.getPassiveData(false);
                        if (entity.progress >= data.getSecond()) {
                            ItemStack outputStack = entity.itemExtractionHandler.getStackInSlot(SLOT_EXTRACTION_OUTPUT);
                            if (outputStack.isEmpty()) {
                                outputStack = data.getFirst().copy();
                            } else {
                                outputStack.grow(data.getFirst().getCount());
                            }
                            entity.itemExtractionHandler.setStackInSlot(SLOT_EXTRACTION_OUTPUT, outputStack);
                            entity.progress = 0;
                            entity.syncAndSave();
                        }
                    } else {
                        final Triplet<ItemStack, Integer, Integer> data = entity.recipe.getSuppliedData(false);
                        if (entity.progress >= data.getSecond()) {
                            ItemStack outputStack = entity.itemExtractionHandler.getStackInSlot(SLOT_EXTRACTION_OUTPUT);
                            if (outputStack.isEmpty()) {
                                outputStack = data.getFirst().copy();
                            } else {
                                outputStack.grow(data.getFirst().getCount());
                            }
                            entity.itemExtractionHandler.setStackInSlot(SLOT_EXTRACTION_OUTPUT, outputStack);
                            entity.progress = 0;
                            entity.materiaAmount -= data.getThird();
                            if (entity.materiaAmount <= 0)
                                entity.materiaType = null;
                            entity.syncAndSave();
                        }
                    }
                }
            }
        }

        if(syncThisTick) entity.syncAndSave();
    }

    private static boolean canCraftItem(ConjurerBlockEntity entity, boolean isPassiveMode) {
        if(entity.itemInsertionHandler.getStackInSlot(SLOT_INSERTION_CATALYST).isEmpty())
            return false;

        ItemStack outputStack = entity.itemExtractionHandler.getStackInSlot(SLOT_EXTRACTION_OUTPUT);
        int slotLimit = entity.itemExtractionHandler.getSlotLimit(SLOT_EXTRACTION_OUTPUT);

        if(outputStack.isEmpty())
            return true;
        else if(isPassiveMode) {
            final Pair<ItemStack, Integer> data = entity.recipe.getPassiveData(false);
            if(outputStack.getItem() != data.getFirst().getItem())
                return false;
            return slotLimit >= outputStack.getCount() + data.getFirst().getCount();
        }
        else {
            final Triplet<ItemStack, Integer, Integer> data = entity.recipe.getSuppliedData(false);
            if(outputStack.getItem() != data.getFirst().getItem())
                return false;
            return slotLimit >= outputStack.getCount() + data.getFirst().getCount();
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,4,1));
    }

    @Override
    public void destroyRouters() {
        getLevel().destroyBlock(getBlockPos().above(), true);
        getLevel().destroyBlock(getBlockPos().above().above(), true);
    }

    public void generateParticles() {
        Vector3 center = new Vector3(getBlockPos().getX() + 0.5, getBlockPos().getY() + 1.5, getBlockPos().getZ() + 0.5);

        //Spike lightning
        if (getLevel().getGameTime() % 16 == 0) {

            Vector3[] offsets = new Vector3[]{
                    new Vector3(0.25, 0.125, 0.25),
                    new Vector3(0.25, 0.125, -0.25),
                    new Vector3(-0.25, 0.125, -0.25),
                    new Vector3(-0.25, 0.125, 0.25),
                    new Vector3(0.25, -0.125, 0.25),
                    new Vector3(0.25, -0.125, -0.25),
                    new Vector3(-0.25, -0.125, -0.25),
                    new Vector3(-0.25, -0.125, 0.25)
            };

            for(int i=0; i<8; i++) {
                if(r.nextFloat() < 0.375f) {
                    getLevel().addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                    .setScale(2.5f).setMaxAge(32),
                            center.x + offsets[i].x, center.y + offsets[i].y, center.z + offsets[i].z,
                            center.x, center.y, center.z);
                }
            }
        }
        //Vertical lightning
        if (getLevel().getGameTime() % 4 == 0) {

            Vector3[] offsets = new Vector3[]{
                    new Vector3(0, 0.5625, 0),
                    new Vector3(0, -0.5625, 0),
            };

            for(int i=0; i<2; i++) {
                getLevel().addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                .setMaxAge(20),
                        center.x + offsets[i].x, center.y + offsets[i].y, center.z + offsets[i].z,
                        center.x, center.y, center.z);
            }
        }
        //Sparks
        {
            for (int i = 0; i < 2; i++) {
                getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setGravity(r.nextFloat() * 0.005f).setMaxAge(30).setScale(0.025f)
                                .setColor(96, 96, r.nextInt(128) + 127),
                        center.x, center.y, center.z,
                        r.nextDouble() * 0.05 - 0.025, r.nextDouble() * 0.035, r.nextDouble() * 0.05 - 0.025);
            }
        }
    }

    ////////////////////
    // MATERIA PROVISION AND SHLORPS
    ////////////////////

    private final NonNullList<MateriaItem> activeProvisionRequests = NonNullList.create();

    @Override
    public boolean needsProvisioning() {
        //We don't need provisioning if something is en route
        if(activeProvisionRequests.contains(recipe.getMateria()))
            return false;

        return materiaAmount < Config.conjurerMateriaCapacity / 2;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        //We obviously don't need materia provided if we don't have a recipe
        if(recipe != null) {
            //Don't report that we have a materia need if there's already a pile incoming
            if (!activeProvisionRequests.contains(recipe.getMateria())) {
                //Otherwise, only report that Admixture of Color is necessary if we're below half capacity
                if (materiaAmount < Config.variegatorMaxAdmixture) {
                    int needed = Math.max(0, Config.conjurerMateriaCapacity - materiaAmount);
                    if (needed > 0)
                        result.put(recipe.getMateria(), (int) Math.ceil((float) needed / Config.conjurerPointsPerDram));
                }
            }
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(!activeProvisionRequests.contains(pMateriaItem))
            activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        if(pStack.getItem() == recipe.getMateria()) {
            activeProvisionRequests.remove(recipe.getMateria());
            materiaAmount += Config.conjurerPointsPerDram * pStack.getCount();
            materiaType = recipe.getMateria();
            syncAndSave();
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        int max = (int)Math.ceil((float)Config.conjurerMateriaCapacity / (float)Config.conjurerPointsPerDram);
        int capacity = max - pStack.getCount();

        return Math.max(0, capacity);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() == recipe.getMateria()) {
            provide(pStack);
        }

        return 0;
    }
}
