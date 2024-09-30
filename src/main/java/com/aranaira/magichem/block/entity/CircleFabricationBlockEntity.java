package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractFabricationBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.aranaira.magichem.util.render.ColorUtils;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class CircleFabricationBlockEntity extends AbstractFabricationBlockEntity implements MenuProvider, Consumer<FriendlyByteBuf>, IShlorpReceiver, IMateriaProvisionRequester {
    public static final int
            SLOT_COUNT = 22,
            SLOT_BOTTLES = 0, SLOT_RECIPE = 21,
            SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 10,
            SLOT_OUTPUT_START = 11, SLOT_OUTPUT_COUNT = 10;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();
    private BlockPos linkedCircleToil = null;
    private static final Random r = new Random();

    public CircleFabricationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_FABRICATION_BE.get(), pos, state);

        itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    if(recipe == null) {
                        getCurrentRecipe();
                    }

                    if(recipe != null) {
                        if(((slot - SLOT_INPUT_START) / 2) >= recipe.getComponentMateria().size())
                            return false;
                        ItemStack component = recipe.getComponentMateria().get((slot - SLOT_INPUT_START) / 2);
                        return stack.getItem() == component.getItem();
                    } else {
                        return false;
                    }
                }

                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    ItemStack item = super.extractItem(slot, amount, simulate);
                    if (item.hasTag()) {
                        CompoundTag nbt = item.getTag();
                        if (nbt.contains("CustomModelData")) return ItemStack.EMPTY;
                    }
                    return item;
                } else if(slot == SLOT_RECIPE) {
                    return ItemStack.EMPTY;
                }

                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                DistillationFabricationRecipe pre = recipe;
                if(slot == SLOT_RECIPE) {
                    getCurrentRecipe();
                }
                if(recipe != pre)
                    syncAndSave();
            }
        };
    }

    @Nullable
    public DistillationFabricationRecipe getCurrentRecipe() {
        if(recipe == null) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(SLOT_RECIPE);
            if(!stackInSlot.isEmpty()) {
                recipe = DistillationFabricationRecipe.getFabricatingRecipe(getLevel(), itemHandler.getStackInSlot(SLOT_RECIPE));
            }
        } else if(recipe.getAlchemyObject() != itemHandler.getStackInSlot(SLOT_RECIPE)) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(SLOT_RECIPE);
            if(!stackInSlot.isEmpty()) {
                recipe = DistillationFabricationRecipe.getFabricatingRecipe(getLevel(), itemHandler.getStackInSlot(SLOT_RECIPE));
            }
        }
        return recipe;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.circle_fabrication");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CircleFabricationMenu(id, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
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
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
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
        progress = nbt.getInt("craftingProgress");
        ENERGY_STORAGE.setEnergy(nbt.getInt("storedPower"));

        if(getLevel() != null)
            getCurrentRecipe();
    }

    public static int getScaledProgress(CircleFabricationBlockEntity entity) {
        return entity.getCraftingProgress() * 28 / entity.getOperationTicks();
    }

    public int getCraftingProgress(){
        return progress;
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i< itemHandler.getSlots(); i++) {
            if(i == SLOT_RECIPE)
                continue;

            final ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            boolean dropItem = false;
            if(stackInSlot.getItem() instanceof MateriaItem) {
                if(stackInSlot.hasTag()) {
                    if(!stackInSlot.getTag().contains("CustomModelData")) {
                        dropItem = true;
                    }
                } else {
                    dropItem = true;
                }
            } else {
                dropItem = true;
            }

            if(dropItem) inventory.setItem(i, stackInSlot);
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CircleFabricationBlockEntity entity) {
        if(!level.isClientSide()) {
            //Power check
            if(entity.operationTicks > 0) {
                int cost = entity.getPowerDraw();
                if (entity.ENERGY_STORAGE.getEnergyStored() >= cost) {
                    entity.ENERGY_STORAGE.extractEnergy(cost, false);
                    entity.isFESatisfied = true;
                } else {
                    entity.isFESatisfied = false;
                }
            } else {
                entity.isFESatisfied = true;
            }

            //Try to find a circle of toil
            if(entity.linkedCircleToil == null) {
                //Only search once every 5 seconds to cut down on server load
                if(level.getGameTime() % 100 == 0) {
                    final Direction facing = state.getValue(FACING);
                    if(facing == Direction.NORTH) {
                        for(int z=-2; z>=-8; z--) {
                            BlockPos queryPos = pos.offset(0, 0, z);
                            if(level.getBlockState(queryPos).getBlock() == BlockRegistry.CIRCLE_TOIL.get()) {
                                entity.linkedCircleToil = queryPos;
                                break;
                            }
                        }
                    } else if(facing == Direction.EAST) {
                        for(int x=2; x<=8; x++) {
                            BlockPos queryPos = pos.offset(x, 0, 0);
                            if(level.getBlockState(queryPos).getBlock() == BlockRegistry.CIRCLE_TOIL.get()) {
                                entity.linkedCircleToil = queryPos;
                                break;
                            }
                        }
                    } else if(facing == Direction.SOUTH) {
                        for(int z=2; z<=8; z++) {
                            BlockPos queryPos = pos.offset(0, 0, z);
                            if(level.getBlockState(queryPos).getBlock() == BlockRegistry.CIRCLE_TOIL.get()) {
                                entity.linkedCircleToil = queryPos;
                                break;
                            }
                        }
                    } else if(facing == Direction.WEST) {
                        for(int x=-2; x>=-8; x--) {
                            BlockPos queryPos = pos.offset(x, 0, 0);
                            if(level.getBlockState(queryPos).getBlock() == BlockRegistry.CIRCLE_TOIL.get()) {
                                entity.linkedCircleToil = queryPos;
                                break;
                            }
                        }
                    }
                }
            } else {
                BlockEntity be = level.getBlockEntity(entity.linkedCircleToil);
                if(be instanceof CircleToilBlockEntity ctbe) {
                    LazyOptional<IEnergyStorage> query = ctbe.getCapability(ForgeCapabilities.ENERGY);
                    if(query.isPresent()) {
                        IEnergyStorage cap = query.resolve().get();
                        int tryExtract = cap.extractEnergy(entity.ENERGY_STORAGE.getMaxEnergyStored(), true);
                        int insert = entity.ENERGY_STORAGE.receiveEnergy(tryExtract, false);
                        cap.extractEnergy(insert, false);
                    }
                } else {
                    entity.linkedCircleToil = null;
                }
            }
        }
        //particle work
        else if(entity.progress > 0 && level.getGameTime() % 8 == 0) {
            Direction facing = state.getValue(FACING);

            Vector3[] bowlPositions = {};
            if(facing == Direction.NORTH) {
                bowlPositions = new Vector3[]{
                        new Vector3(0.5, 0.1875, -0.6875),
                        new Vector3(1.6294, 0.1875, 0.1875),
                        new Vector3(1.198, 0.1875, 1.4607),
                        new Vector3(-0.198, 0.1875, 1.4607),
                        new Vector3(-0.6294, 0.1875, 0.1875),
                };
            } else if(facing == Direction.EAST) {
                bowlPositions = new Vector3[]{
                        new Vector3(1.6875, 0.1875, 0.5),
                        new Vector3(0.867, 0.1875, 1.6294),
                        new Vector3(-0.461, 0.1875, 1.198),
                        new Vector3(-0.461, 0.1875, -0.198),
                        new Vector3(0.867, 0.1875, -0.6294),
                };
            } else if(facing == Direction.SOUTH) {
                bowlPositions = new Vector3[]{
                        new Vector3(0.5, 0.1875, 1.6875),
                        new Vector3(-0.6294, 0.1875, 0.867),
                        new Vector3(-0.198, 0.1875, -0.4607),
                        new Vector3(1.198, 0.1875, -0.4607),
                        new Vector3(1.6294, 0.1875, 0.867),
                };
            } else if(facing == Direction.WEST) {
                bowlPositions = new Vector3[]{
                        new Vector3(-0.6875, 0.1875, 0.5),
                        new Vector3(0.133, 0.1875, -0.6294),
                        new Vector3(1.461, 0.1875, -0.198),
                        new Vector3(1.461, 0.1875, 1.198),
                        new Vector3(0.133, 0.1875, 1.6294),
                };
            }

            if(bowlPositions.length > 0) {
                final ItemStack[] contentsOfInputSlots = entity.getContentsOfInputSlots();
                boolean has1 = !contentsOfInputSlots[0].isEmpty() || !contentsOfInputSlots[1].isEmpty();
                boolean has2 = !contentsOfInputSlots[2].isEmpty() || !contentsOfInputSlots[3].isEmpty();
                boolean has3 = !contentsOfInputSlots[4].isEmpty() || !contentsOfInputSlots[5].isEmpty();
                boolean has4 = !contentsOfInputSlots[6].isEmpty() || !contentsOfInputSlots[7].isEmpty();
                boolean has5 = !contentsOfInputSlots[8].isEmpty() || !contentsOfInputSlots[9].isEmpty();

                if (has1) {
                    int[] color = ColorUtils.getRGBAIntTintFromPackedInt((
                            contentsOfInputSlots[0].isEmpty() ?
                                    (MateriaItem) contentsOfInputSlots[1].getItem() :
                                    (MateriaItem) contentsOfInputSlots[0].getItem()
                    ).getMateriaColor());

                    entity.generateMateriaCloud(bowlPositions[0], color);
                }

                if (has2) {
                    int[] color = ColorUtils.getRGBAIntTintFromPackedInt((
                            contentsOfInputSlots[2].isEmpty() ?
                                    (MateriaItem) contentsOfInputSlots[3].getItem() :
                                    (MateriaItem) contentsOfInputSlots[2].getItem()
                    ).getMateriaColor());

                    entity.generateMateriaCloud(bowlPositions[1], color);
                }

                if (has3) {
                    int[] color = ColorUtils.getRGBAIntTintFromPackedInt((
                            contentsOfInputSlots[4].isEmpty() ?
                                    (MateriaItem) contentsOfInputSlots[5].getItem() :
                                    (MateriaItem) contentsOfInputSlots[4].getItem()
                    ).getMateriaColor());

                    entity.generateMateriaCloud(bowlPositions[2], color);
                }

                if (has4) {
                    int[] color = ColorUtils.getRGBAIntTintFromPackedInt((
                            contentsOfInputSlots[6].isEmpty() ?
                                    (MateriaItem) contentsOfInputSlots[7].getItem() :
                                    (MateriaItem) contentsOfInputSlots[6].getItem()
                    ).getMateriaColor());

                    entity.generateMateriaCloud(bowlPositions[3], color);
                }

                if (has5) {
                    int[] color = ColorUtils.getRGBAIntTintFromPackedInt((
                            contentsOfInputSlots[8].isEmpty() ?
                                    (MateriaItem) contentsOfInputSlots[9].getItem() :
                                    (MateriaItem) contentsOfInputSlots[8].getItem()
                    ).getMateriaColor());

                    entity.generateMateriaCloud(bowlPositions[4], color);
                }

                if(has1 || has2 || has3 || has4 || has5) {
                    int total = 10;
                    for(int i=0; i<total; i++) {
                        double radianWiggle = ((Math.PI * 2) / (double)total) * (r.nextDouble() - 0.5);

                        double x = Math.cos((Math.PI * 2) * i / (double)total + radianWiggle);
                        double z = Math.sin((Math.PI * 2) * i / (double)total + radianWiggle);

                        double rx = r.nextDouble() - 0.5;
                        double ry = r.nextDouble();
                        double rz = r.nextDouble() - 0.5;

                        Vector3 mid = new Vector3(0.5, 0.015625, 0.5).add(new Vector3(x, 0, z).scale(0.9f));

                        level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setMaxAge(20 + r.nextInt(30)).setScale(0.03f + r.nextFloat() * 0.03f)
                                        .setColor(150+r.nextInt(75), 150+r.nextInt(75), 150+r.nextInt(75))
                                        .setMover(new ParticleVelocityMover(rx * 0.03, 0.01 + ry * 0.02, rz * 0.03, true)),
                                pos.getX() + mid.x, pos.getY() + mid.y, pos.getZ() + mid.z,
                                0, 0, 0);
                    }
                }
            }
        }

        entity.operationTicks = entity.getOperationTicks();

        boolean changed = AbstractFabricationBlockEntity.tick(level, pos, state, entity, CircleFabricationBlockEntity::getVar);

        if(changed)
            entity.syncAndSave();
    }

    public void generateMateriaCloud(Vector3 pPosition, int[] pColor) {
        Vector3 origin = new Vector3(getBlockPos()).add(pPosition);

        double spreadRadius = 0.015625d;

        double x = r.nextDouble() * spreadRadius * 2 - spreadRadius;
        double z = r.nextDouble() * spreadRadius * 2 - spreadRadius;

        Vector3 pos = origin.add(new Vector3(x, 0, z));

        getLevel().addParticle(new MAParticleType(ParticleInit.DUST_LERP.get())
                        .setScale(0.05f).setMaxAge(64)
                        .setMover(new ParticleLerpMover(pos.x, pos.y, pos.z, pos.x, pos.y + 0.5, pos.z))
                        .setColor(pColor[0], pColor[1], pColor[2], 64),
                pos.x, pos.y, pos.z,
                0, 0, 0);
    }

    public ItemStack[] getContentsOfInputSlots() {
        ItemStack[] out = new ItemStack[10];
        for(int i=SLOT_INPUT_START; i<SLOT_INPUT_START+SLOT_INPUT_COUNT; i++) {
            out[i-SLOT_INPUT_START] = itemHandler.getStackInSlot(i);
        }
        return out;
    }

    public ItemStack getOutputInLastSlot() {
        ItemStack out = null;
        for(int i=SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT-1; i>=SLOT_OUTPUT_START; i--) {
            if(!itemHandler.getStackInSlot(i).isEmpty())
                out = itemHandler.getStackInSlot(i);
        }
        return out;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", this.itemHandler.serializeNBT());
        nbt.putInt("progress", this.progress);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        return nbt;
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @NotNull
    private static SimpleContainer getOutputAsContainer(CircleFabricationBlockEntity entity) {
        SimpleContainer insert = new SimpleContainer(SLOT_OUTPUT_COUNT);
        int slotID = 0;
        //Add output item
        for (int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT; i++) {
            insert.setItem(slotID++, entity.itemHandler.getStackInSlot(i));
        }
        return insert;
    }

    public int getPowerDraw() {
        return 10;
    }

    public int getOperationTicks() {
        return 1800;
    }

    public void setCurrentRecipe(ItemStack pQuery) {
        itemHandler.setStackInSlot(SLOT_RECIPE, pQuery);

        syncAndSave();
    }

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(60, 60) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {

            int powerToLimit = Math.max(0, (getPowerDraw() * Config.circlePowerBuffer) - getEnergyStored());
            int actualReceive = Math.min(maxReceive, powerToLimit);

            return super.receiveEnergy(actualReceive, simulate);
        }
    };

    // --------------------------------------
    // Consumer Interface Stuff
    // --------------------------------------

    @Override
    public void accept(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.getBlockPos());
        friendlyByteBuf.writeBoolean(this.isFESatisfied);
    }

    //client side, needs to be the same order as above
    public CircleFabricationBlockEntity readFrom(FriendlyByteBuf friendlyByteBuf){
        this.isFESatisfied = friendlyByteBuf.readBoolean();
        return this;
    }

    ////////////////////
    // FINAL RETRIEVAL
    ////////////////////

    public static int getVar(IDs pID) {
        return switch(pID) {
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case SLOT_INPUT_START -> SLOT_INPUT_START;
            case SLOT_INPUT_COUNT -> SLOT_INPUT_COUNT;
            case SLOT_OUTPUT_START -> SLOT_OUTPUT_START;
            case SLOT_OUTPUT_COUNT -> SLOT_OUTPUT_COUNT;
            case SLOT_RECIPE -> SLOT_RECIPE;

            default -> -1;
        };
    }

    ////////////////////
    // MATERIA PROVISION AND SHLORPS
    ////////////////////

    private final NonNullList<MateriaItem> activeProvisionRequests = NonNullList.create();

    @Override
    public boolean allowIncreasedDeliverySize() {
        return true;
    }

    @Override
    public boolean needsProvisioning() {
        if(recipe == null)
            return false;

        return getProvisioningNeeds().size() > 0;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        if(recipe != null) {
            for (ItemStack recipeMateria : recipe.getComponentMateria()) {
                if(activeProvisionRequests.contains((MateriaItem)recipeMateria.getItem()))
                    continue;

                int amountToAdd = recipeMateria.getCount();
                for(int i=SLOT_INPUT_START; i<SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    if(stackInSlot.getItem() == recipeMateria.getItem()) {
                        amountToAdd -= stackInSlot.getCount();

                        if(amountToAdd <= 0)
                            break;
                    }
                }

                if(amountToAdd > 0)
                    result.put((MateriaItem)recipeMateria.getItem(), amountToAdd);
            }
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        CompoundTag nbt = pStack.getOrCreateTag();
        nbt.putInt("CustomModelData", 1);
        pStack.setTag(nbt);
        if(!pStack.isEmpty()) {
            activeProvisionRequests.remove((MateriaItem) pStack.getItem());

            boolean changed = false;
            for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
                if (itemHandler.isItemValid(i, pStack)) {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    if (stackInSlot.isEmpty()) {
                        int slotLimit = itemHandler.getSlotLimit(i);
                        if (pStack.getCount() <= slotLimit) {
                            itemHandler.setStackInSlot(i, pStack.copy());
                            pStack.shrink(pStack.getCount());
                            changed = true;
                        } else {
                            ItemStack copy = pStack.copy();
                            copy.setCount(slotLimit);
                            pStack.shrink(slotLimit);
                            itemHandler.setStackInSlot(i, copy);
                            changed = true;
                        }

                        if (pStack.isEmpty())
                            break;
                    } else if (stackInSlot.hasTag()) {
                        CompoundTag nbtInSlot = stackInSlot.getTag();
                        if (nbtInSlot.contains("CustomModelData")) {
                            int capacity = (itemHandler.getSlotLimit(i) - stackInSlot.getCount());
                            int delta = Math.min(capacity, pStack.getCount());
                            stackInSlot.grow(delta);
                            pStack.shrink(delta);

                            changed = true;

                            if (pStack.isEmpty())
                                break;
                        }
                    }
                }
            }

            if (changed) {
                syncAndSave();
            }
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);

        return 0;
    }
}
