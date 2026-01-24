package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.gui.StandingRetortMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.blocks.tileentities.wizard_lab.EldrinFumeTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StandingRetortBlockEntity extends BlockEntity implements MenuProvider, IShlorpReceiver, IMateriaProvisionRequester {
    private int element = -1;
    private boolean provisioningInProgress = false;
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
            return 1;
        }
    };

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    public static HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(false, false);

    public static final int
        SLOT_COUNT = 2, SLOT_ESSENTIA = 0, SLOT_BOTTLES = 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_ESSENTIA) {
                if (stack.getItem() instanceof MateriaItem mi) {
                    String matName = mi.getMateriaName();
                    return matName.equals(getMateriaType());
                }
            }

            return false;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(InventoryHelper.hasCustomModelData(getStackInSlot(slot)))
                return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }
    };

    public StandingRetortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.STANDING_RETORT_BE.get(), pPos, pBlockState);
    }

    public String getMateriaType() {
        if(element == 0) return "ender";
        else if(element == 1) return "earth";
        else if(element == 2) return "water";
        else if(element == 3) return "air";
        else if(element == 4) return "fire";
        else if(element == 5) return "arcane";

        return "";
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new StandingRetortMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("element", this.element);
        nbt.putBoolean("provisioningInProgress", this.provisioningInProgress);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        element = nbt.getInt("element");
        provisioningInProgress = nbt.getBoolean("provisioningInProgress");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("element", this.element);
        nbt.putBoolean("provisioningInProgress", this.provisioningInProgress);

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

    public int getElementID() {
        return element;
    }

    public void setElementID(int pElement) {
        element = pElement;
    }

    private static final Affinity[] AFFINITY_MAP = {
            Affinity.ENDER, Affinity.EARTH, Affinity.WATER, Affinity.WIND, Affinity.FIRE, Affinity.ARCANE
    };
    public Affinity getAffinityFromElement() {
        if(element > 5 || element < 0) {
            return Affinity.UNKNOWN;
        }
        else return AFFINITY_MAP[element];
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        boolean hasEnoughSpace = itemHandler.getStackInSlot(0).getCount() < 16;
        boolean isMateriaUnbottled = InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(0));
        boolean isElementSelected = element > -1;

        return hasEnoughSpace && (isMateriaUnbottled || itemHandler.getStackInSlot(0).isEmpty()) && isElementSelected;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        HashMap<MateriaItem, Integer> needs = new HashMap<>();

        int currentCount = itemHandler.getStackInSlot(0).getCount();
        if(currentCount < 16 && !provisioningInProgress) {
            if(itemHandler.getStackInSlot(0).isEmpty() || InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(0))) {
                needs.put(materiaMap.get(getMateriaType()), 64 - currentCount);
            }
        }

        return needs;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        provisioningInProgress = true;
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        provisioningInProgress = false;
    }

    @Override
    public void provide(ItemStack pStack) {
        if(pStack.getItem() == materiaMap.get(getMateriaType())) {
            provisioningInProgress = false;
            pStack.getOrCreateTag().putInt("CustomModelData", 1);
            itemHandler.insertItem(SLOT_ESSENTIA, pStack, false);
            syncAndSave();
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() == materiaMap.get(getMateriaType()))
            return Math.min(pStack.getCount(), 64 - itemHandler.getStackInSlot(0).getCount());

        return 0;
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);
        return 0;
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    public ItemStack getMateria() {
        return itemHandler.getStackInSlot(SLOT_ESSENTIA);
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, StandingRetortBlockEntity entity) {
        if(!level.isClientSide() && level.getGameTime() % 60 == 0) {
            ItemStack essentiaInSlot = entity.itemHandler.getStackInSlot(0);
            if(essentiaInSlot.isEmpty())
                return;

            BlockEntity be = level.getBlockEntity(pos.below().below());
            if(be instanceof EldrinFumeTile eft) {
                ItemStack fumeFilterQuery = eft.getItem(1);
                if(fumeFilterQuery.getCount() == 64)
                    return;

                boolean matchesMateriaType = false;
                if(fumeFilterQuery.getItem() instanceof MateriaItem ffmi && essentiaInSlot.getItem() instanceof MateriaItem esmi) {
                    matchesMateriaType = ffmi.getMateriaName().equals(esmi.getMateriaName());
                }

                ItemStack insertionQuery = convertEssentiaToDroplets(essentiaInSlot);
                insertionQuery.setCount(Math.min(4, essentiaInSlot.getCount()));

                boolean retortIsBottled = !InventoryHelper.hasCustomModelData(entity.itemHandler.getStackInSlot(SLOT_ESSENTIA));

                boolean transferIsValid = fumeFilterQuery.isEmpty() || matchesMateriaType;

                if(transferIsValid) {
                    if(retortIsBottled) {
                        int bottleCount = entity.itemHandler.getStackInSlot(SLOT_BOTTLES).getCount();
                        if(bottleCount == 0) {
                            entity.itemHandler.setStackInSlot(SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE, insertionQuery.getCount()));
                        }
                        else if(bottleCount <= 60) {
                            entity.itemHandler.getStackInSlot(SLOT_BOTTLES).grow(insertionQuery.getCount());
                        }
                        else {
                            int limit = 64 - bottleCount;
                            entity.itemHandler.getStackInSlot(SLOT_BOTTLES).setCount(64);
                            insertionQuery.setCount(Math.min(insertionQuery.getCount(), limit));
                        }
                    }

                    if(fumeFilterQuery.isEmpty()) {
                        essentiaInSlot.shrink(insertionQuery.getCount());
                        fumeFilterQuery = insertionQuery.copy();
                    }
                    else if(fumeFilterQuery.getCount() + insertionQuery.getCount() <= 64) {
                        essentiaInSlot.shrink(insertionQuery.getCount());
                        fumeFilterQuery.grow(insertionQuery.getCount());
                    }
                    else {
                        int transferLimit = 64 - (fumeFilterQuery.getCount() + insertionQuery.getCount());

                        essentiaInSlot.shrink(transferLimit);
                        fumeFilterQuery.grow(transferLimit);
                    }

                    entity.itemHandler.setStackInSlot(SLOT_ESSENTIA, essentiaInSlot);
                    eft.setItem(1, fumeFilterQuery);
                }
            }
        }
    }

    public static ItemStack convertEssentiaToDroplets(ItemStack pQuery) {
        Item dropletItem = Items.BEDROCK;
        MateriaItem materia = (MateriaItem) pQuery.getItem();
        if(materia.getMateriaName().equals("ender")) dropletItem = ItemRegistry.ESSENTIA_DROPLETS_ENDER.get();
        if(materia.getMateriaName().equals("earth")) dropletItem = ItemRegistry.ESSENTIA_DROPLETS_EARTH.get();
        if(materia.getMateriaName().equals("water")) dropletItem = ItemRegistry.ESSENTIA_DROPLETS_WATER.get();
        if(materia.getMateriaName().equals("air")) dropletItem = ItemRegistry.ESSENTIA_DROPLETS_AIR.get();
        if(materia.getMateriaName().equals("fire")) dropletItem = ItemRegistry.ESSENTIA_DROPLETS_FIRE.get();
        if(materia.getMateriaName().equals("arcane")) dropletItem = ItemRegistry.ESSENTIA_DROPLETS_ARCANE.get();

        return new ItemStack(dropletItem, pQuery.getCount());
    }

    public void dropContents() {
        if(!itemHandler.getStackInSlot(SLOT_ESSENTIA).isEmpty() && !InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(SLOT_ESSENTIA)) && getLevel() != null) {
            ItemEntity ie = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), itemHandler.getStackInSlot(SLOT_ESSENTIA));
            getLevel().addFreshEntity(ie);
        }
        if(!itemHandler.getStackInSlot(SLOT_BOTTLES).isEmpty() && getLevel() != null) {
            ItemEntity ie = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), itemHandler.getStackInSlot(SLOT_BOTTLES));
            getLevel().addFreshEntity(ie);
        }
    }
}
