package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.block.entity.routers.AlchemicalNexusRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.FuseryRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandFuseryRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.MirrorLabyrinthRouterBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.enums.ShlorpParticleMode;
import com.aranaira.magichem.gui.ActuatorArcaneMenu;
import com.aranaira.magichem.gui.ActuatorArcaneScreen;
import com.aranaira.magichem.gui.ActuatorEnderMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.items.ItemInit;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ActuatorEnderBlockEntity extends AbstractDirectionalPluginBlockEntity implements MenuProvider, IPluginDevice, IEldrinConsumerTile, IShlorpReceiver, IMateriaProvisionRequester {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 140, 500};
    public static final int
            MAX_POWER_LEVEL = 3,
            SLOT_COUNT = 3, SLOT_MARK = 0, SLOT_ESSENTIA_INSERTION = 1, SLOT_BOTTLES = 2;
    protected ContainerData data;
    public static final MateriaItem ESSENTIA_ENDER = ItemRegistry.getEssentiaMap(false, false).get("ender");
    private static final Random r = new Random();

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ActuatorEnderBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public ActuatorEnderBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_ENDER_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return 0;
            }

            @Override
            public void set(int pIndex, int pValue) {

            }

            @Override
            public int getCount() {
                return 0;
            }
        };

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_ESSENTIA_INSERTION) {
                    if(stack.getItem() == ItemRegistry.DEBUG_ORB.get())
                        return true;
                    else if(stack.getItem() instanceof MateriaItem mi) {
                        return mi == ESSENTIA_ENDER;
                    }
                } else if(slot == SLOT_MARK) {
                    return stack.getItem() == ItemInit.RUNE_MARKING.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if(slot == SLOT_ESSENTIA_INSERTION) {
                    if(InventoryHelper.isMateriaUnbottled(itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION)))
                        return ItemStack.EMPTY;
                }

                return super.extractItem(slot, amount, simulate);
            }
        };
    }

    public int getEldrinPowerUsage() {
        return ELDRIN_POWER_USAGE[this.powerLevel];
    }

    public static int getEldrinPowerUsage(int pPowerLevel) {
        return ELDRIN_POWER_USAGE[pPowerLevel];
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewEssentiaThisCycle", drewEssentiaThisCycle);
        nbt.putBoolean("isPaused", isPaused);
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
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
        this.remainingCycleTime = nbt.getInt("remainingCycleTime");
        this.powerLevel = nbt.getInt("powerLevel");
        this.storedMateria = nbt.getInt("storedMateria");
        this.drewEldrinThisCycle = nbt.getBoolean("drewEldrinThisCycle");
        this.drewEssentiaThisCycle = nbt.getBoolean("drewEssentiaThisCycle");
        this.isPaused = nbt.getBoolean("isPaused");

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
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewEssentiaThisCycle", drewEssentiaThisCycle);
        nbt.putBoolean("isPaused", isPaused);
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void processCompletedOperation(int pCyclesCompleted) {
        syncAndSave();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        boolean changed = AbstractDirectionalPluginBlockEntity.tick(level, pos, blockState, t, ActuatorEnderBlockEntity::getValue);

        if(t instanceof ActuatorEnderBlockEntity entity) {
            if(changed && !level.isClientSide())
                entity.syncAndSave();

            if (level.isClientSide()) {
                entity.handleAnimationDrivers();
            }

            if (!level.isClientSide()) {

            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorEnderBlockEntity entity) {
        boolean changed = AbstractDirectionalPluginBlockEntity.delegatedTick(level, pos, state, entity,
                ActuatorEnderBlockEntity::getValue,
                ActuatorEnderBlockEntity::getAffinity,
                ActuatorEnderBlockEntity::getPowerDraw,
                ActuatorEnderBlockEntity::handleAuxiliaryRequirements);

        if(changed) entity.syncAndSave();
    }

    public void handleAnimationDrivers() {

    }

    @Nullable
    public BlockEntity getMirrorTarget() {
        final ItemStack markStack = itemHandler.getStackInSlot(SLOT_MARK);
        if(markStack.hasTag() && level != null) {
            final CompoundTag markTag = markStack.getTag().getCompound("mark");

            BlockPos posQuery = new BlockPos(markTag.getInt("x"),markTag.getInt("y"),markTag.getInt("z"));
            BlockEntity entityQuery = level.getBlockEntity(posQuery);

            if(entityQuery instanceof MagicMirrorBlockEntity ||
               entityQuery instanceof MirrorLabyrinthBlockEntity ||
               entityQuery instanceof MirrorLabyrinthRouterBlockEntity) {
                return entityQuery;
            }
        }

        return null;
    }

    public void createShlorpToTarget(ItemStack pPayload, boolean isInstant) {
        BlockEntity beQuery = getMirrorTarget();
        if(beQuery instanceof IShlorpReceiver isr && level != null) {
            double theta = r.nextDouble() * Math.PI;

            Vector3 origin = Vector3.zero(), tangent = Vector3.zero();
            if(pPayload.getItem() instanceof MateriaItem mi) {
                if (beQuery instanceof MagicMirrorBlockEntity mmbe) {
                    origin = mmbe.getDefaultOriginAndTangent(mi).getFirst();
                    tangent = mmbe.getDefaultOriginAndTangent(mi).getSecond();
                } else if (beQuery instanceof MirrorLabyrinthBlockEntity mlbe) {
                    origin = mlbe.getDefaultOriginAndTangent(mi).getFirst();
                    tangent = mlbe.getDefaultOriginAndTangent(mi).getSecond();
                } else if (beQuery instanceof MirrorLabyrinthRouterBlockEntity mlrbe) {
                    origin = mlrbe.getDefaultOriginAndTangent(mi).getFirst();
                    tangent = mlrbe.getDefaultOriginAndTangent(mi).getSecond();
                }

                ShlorpEntity shlorp = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), level);
                shlorp.configure(
                        getBlockPos().above(), new Vector3(0.5f, 0.5f, 0.5f), new Vector3(Math.cos(theta), r.nextFloat() - 0.5f, Math.sin(theta)).scale(3f),
                        beQuery.getBlockPos(), origin, tangent,
                        isInstant ? (0.375f + r.nextFloat() * 0.125f) : (0.120f + r.nextFloat() * 0.06f), isInstant ? 0.2125f : 0.1875f, pPayload.getCount() * 2 + 2, mi, pPayload.getCount(),
                        ShlorpParticleMode.DESTINATION_TANGENT
                );

                if(isInstant)
                    shlorp.setInstantPayload();

                level.addFreshEntity(shlorp);
            }
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
        return new ActuatorEnderMenu(i, inventory, this, this.data);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,2,1));
    }

    public static Affinity getAffinity(Void v) {
        return Affinity.ENDER;
    }

    public void dropContents() {
        if(!itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION).isEmpty() && !InventoryHelper.isMateriaUnbottled(itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION)) && getLevel() != null) {
            ItemEntity ie = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION));
            getLevel().addFreshEntity(ie);
        }
        if(!itemHandler.getStackInSlot(SLOT_BOTTLES).isEmpty() && getLevel() != null) {
            ItemEntity ie = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), itemHandler.getStackInSlot(SLOT_BOTTLES));
            getLevel().addFreshEntity(ie);
        }
    }

    ////////////////////
    // PROVISIONING AND SHLORPS
    ////////////////////

    private final NonNullList<MateriaItem> activeProvisionRequests = NonNullList.create();

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        if(activeProvisionRequests.size() > 0)
            return false;
        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION);
        if(InventoryHelper.isMateriaUnbottled(insertionStack)) {
            return insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION) / 2;
        }
        return insertionStack.isEmpty();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION);

        if(insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION) / 2) {
            result.put(ESSENTIA_ENDER, itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION) - insertionStack.getCount());
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(pMateriaItem == ESSENTIA_ENDER)
            activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        if(pStack.getItem() == ESSENTIA_ENDER) {
            ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION);

            if(insertionStack.isEmpty()) {
                insertionStack = pStack.copy();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("CustomModelData", 1);
                insertionStack.setTag(nbt);
            } else {
                insertionStack.grow(pStack.getCount());
            }
            itemHandler.setStackInSlot(SLOT_ESSENTIA_INSERTION, insertionStack);

            syncAndSave();

            activeProvisionRequests.remove((MateriaItem)pStack.getItem());
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() == ESSENTIA_ENDER) {
            return 0;
        }
        return pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);
        return 0;
    }

    ////////////////////
    // STATIC RETRIEVAL
    ////////////////////

    public static int getValue(IDs id) {
        return switch (id) {
            case SLOT_COUNT -> SLOT_COUNT;
            case SLOT_ESSENTIA_INSERTION -> SLOT_ESSENTIA_INSERTION;
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case MAX_POWER_LEVEL -> MAX_POWER_LEVEL;
        };
    }

    public static int getPowerDraw(AbstractDirectionalPluginBlockEntity entity) {
        if(entity == null)
            return 1;
        return ELDRIN_POWER_USAGE[entity.getPowerLevel()];
    }

    public static boolean handleAuxiliaryRequirements(AbstractDirectionalPluginBlockEntity entity) {
        entity.satisfyAuxiliaryRequirements();
        return true;
    }
}
