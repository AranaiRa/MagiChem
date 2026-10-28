package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.IKeepsInventoryOnBreak;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.saveddata.CenserWorkingAreasSD;
import com.aranaira.magichem.gui.NourishingCenserMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.networking.ParticleSpawnAnointingS2CPacket;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.items.ItemInit;
import com.mna.items.runes.RuneMarkingPair;
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
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class NourishingCenserBlockEntity extends BlockEntity implements MenuProvider, IKeepsInventoryOnBreak, IMateriaProvisionRequester {
    private int droplets, index;
    private BlockPos cornerOne, cornerTwo;
    private AABB operatingArea;
    private final NonNullList<BlockPos> remainingTargets = NonNullList.create();
    public static final int
        MINIMUM_OPERATING_AREA = 9, MAXIMUM_OPERATING_AREA = 900, LIST_ITERATION_LIMIT = 20,
        SLOT_COUNT = 3,
        SLOT_MARK_PAIR = 0, SLOT_MATERIA_INPUT = 1, SLOT_MATERIA_OUTPUT = 2;
    private boolean isOverlapping = false;
    public static final MateriaItem ESSENTIA_NOURISHING = ItemRegistry.getEssentiaMap(false, false).get("nourishing");

    protected ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            if(slot == SLOT_MARK_PAIR) {
                final ItemStack markStack = getStackInSlot(SLOT_MARK_PAIR);
                if(markStack.isEmpty() || !markStack.hasTag()) {
                    operatingArea = new AABB(getBlockPos().offset(-8, -1, -8), getBlockPos().offset(8, 1, 8));
                } else {
                    CompoundTag nbt = markStack.getTag();
                    if(nbt.contains("mark")) {
                        readAABB(nbt.getCompound("mark"));
                    } else {
                        operatingArea = null;
                    }
                    index = -1;
                }
            }
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_MATERIA_INPUT) {
                if(stack.getItem() == ItemRegistry.DEBUG_ORB.get())
                    return true;
                else if(stack.getItem() instanceof MateriaItem mi) {
                    return mi == ESSENTIA_NOURISHING;
                }
            }
            if(slot == SLOT_MARK_PAIR) {
                return stack.getItem() == ItemInit.RUNE_MARKING_PAIR.get();
            }
            return false;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(slot == SLOT_MATERIA_INPUT) {
                if(InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(SLOT_MATERIA_INPUT)))
                    return ItemStack.EMPTY;
            }

            return super.extractItem(slot, amount, simulate);
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public NourishingCenserBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.NOURISHING_CENSER_BE.get(), pPos, pBlockState);
    }

    private boolean readAABB(CompoundTag tag) {
        if(tag.contains("minx") && tag.contains("miny") && tag.contains("minz") && tag.contains("maxx") && tag.contains("maxy") && tag.contains("maxz")) {
            double minX = tag.getDouble("minx");
            double minY = tag.getDouble("miny");
            double minZ = tag.getDouble("minz");
            double maxX = tag.getDouble("maxx");
            double maxY = tag.getDouble("maxy");
            double maxZ = tag.getDouble("maxz");

            cornerOne = new BlockPos((int)minX, (int)minY, (int)minZ);
            cornerTwo = new BlockPos((int)maxX, (int)maxY, (int)maxZ);
            operatingArea = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("droplets", droplets);
        nbt.putBoolean("isOverlapping", isOverlapping);
        if(cornerOne != null)
            nbt.putLong("cornerOne", cornerOne.asLong());
        if(cornerTwo != null)
            nbt.putLong("cornerTwo", cornerTwo.asLong());
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
        this.droplets = nbt.getInt("droplets");
        if(nbt.contains("cornerOne"))
            cornerOne = BlockPos.of(nbt.getLong("cornerOne"));
        if(nbt.contains("cornerTwo"))
            cornerTwo = BlockPos.of(nbt.getLong("cornerTwo"));

        if(cornerOne != null && cornerTwo != null) {
            operatingArea = new AABB(cornerOne, cornerTwo);
        } else {
            operatingArea = new AABB(getBlockPos().offset(-8, -1, -8), getBlockPos().offset(8, 1, 8));
        }

        if(level != null && level.getServer() != null) {
            syncWithLevelData(level, getBlockPos().asLong(), this);
        }
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
        nbt.putInt("droplets", droplets);
        nbt.putBoolean("isOverlapping", isOverlapping);
        if(cornerOne != null)
            nbt.putLong("cornerOne", cornerOne.asLong());
        if(cornerTwo != null)
            nbt.putLong("cornerTwo", cornerTwo.asLong());
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();

        return super.getCapability(cap, side);
    }

    public int getOperatingAreaSize() {
        int deltaX = (int) operatingArea.getXsize() + 1;
        int deltaY = (int) operatingArea.getYsize() + 1;
        int deltaZ = (int) operatingArea.getZsize() + 1;

        return deltaX * deltaY * deltaZ;
    }

    public boolean isOperatingAreaTooSmall() {
        return getOperatingAreaSize() < 9;
    }

    public boolean isOperatingAreaTooLarge() {
        return getOperatingAreaSize() > 900;
    }

    public AABB getOperatingArea() {
        return operatingArea;
    }

    public int getDroplets() {
        return droplets;
    }

    public float getDropletsPercent() {
        return (float)droplets / (float)(ServerConfig.nourishingCenserMateriaUnitsPerDram * 5);
    }

    public int getTickModulus() {
        float lerp = (getOperatingAreaSize() - MINIMUM_OPERATING_AREA) / (float)(MAXIMUM_OPERATING_AREA - MINIMUM_OPERATING_AREA);
        lerp = Math.max(0, Math.min(1, lerp * 2));
        float weightedLerp = 1f - ((1f - lerp) * (1f - lerp) * (1f - lerp));

        final int rounded = ServerConfig.nourishingCenserMinimumTickDelay + Math.round(weightedLerp * (ServerConfig.nourishingCenserMaximumTickDelay - ServerConfig.nourishingCenserMinimumTickDelay));
        return Math.max(1, rounded);
    }

    public int getMateriaCost() {
        float lerp = (getOperatingAreaSize() - MINIMUM_OPERATING_AREA) / (float)(MAXIMUM_OPERATING_AREA - MINIMUM_OPERATING_AREA);
        lerp = Math.max(0, Math.min(1, lerp * 2));
        float weightedLerp = 1f - ((1f - lerp) * (1f - lerp) * (1f - lerp));

        final int rounded = ServerConfig.nourishingCenserMinimumMateriaCost + Math.round(weightedLerp * (ServerConfig.nourishingCenserMaximumMateriaCost - ServerConfig.nourishingCenserMinimumMateriaCost));
        return Math.max(1, rounded);
    }

    public boolean isOverlapping() {
        return isOverlapping;
    }

    @Override
    public void packDataToBlockItem() {

    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {

    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        return false;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return null;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void provide(ItemStack pStack) {

    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new NourishingCenserMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(0));
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof NourishingCenserBlockEntity entity) {
            if(!level.isClientSide()) {
                boolean changed = false;

                if(level.getGameTime() % 120 == 0) {
                    changed = syncWithLevelData(level, pos.asLong(), entity);
                }

                if(entity.droplets <= ServerConfig.nourishingCenserMateriaUnitsPerDram * 4) {
                    ItemStack inputQuery = entity.itemHandler.getStackInSlot(SLOT_MATERIA_INPUT);
                    if(!inputQuery.isEmpty()) {
                        if (inputQuery.getItem() != ItemRegistry.DEBUG_ORB.get()) {
                            inputQuery.shrink(1);
                        }
                        entity.droplets += ServerConfig.nourishingCenserMateriaUnitsPerDram;
                        changed = true;
                    }
                }

                if(entity.operatingArea == null) {
                    if(entity.cornerOne != null && entity.cornerTwo != null) {
                        entity.operatingArea = new AABB(entity.cornerOne.getX(), entity.cornerOne.getY(), entity.cornerOne.getZ(), entity.cornerTwo.getX(), entity.cornerTwo.getY(), entity.cornerTwo.getZ());
                        changed = true;
                    }
                    else {
                        entity.operatingArea = new AABB(entity.getBlockPos().offset(-8, -1, -8), entity.getBlockPos().offset(8, 1, 8));
                        changed = true;
                    }
                }

                if (level.getGameTime() % entity.getTickModulus() != 0) {
                    if(changed) entity.syncAndSave();
                    return;
                }

                if(entity.operatingArea != null){
                    //Fill the block positions list if it's empty
                    reshuffleRemainingTargets(entity);

                    //Pick a position and force a growth tick if it's the right type of block
                    boolean stillSearching = true;
                    int checkTally = 0;
                    while (stillSearching) {
                        if (entity.index < 0 || entity.index >= entity.remainingTargets.size()) {
                            entity.index = -1;
                            break;
                        }

                        final BlockPos posQuery = entity.remainingTargets.get(entity.index);
                        final BlockState stateQuery = level.getBlockState(posQuery);
                        if (stateQuery.getBlock() instanceof CropBlock crop) {
                            if (crop.getAge(stateQuery) < crop.getMaxAge()) {
                                crop.growCrops(level, posQuery, stateQuery);
                                MagiChemMod.CHANNEL.send(
                                        PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(posQuery.getX(), posQuery.getY(), posQuery.getZ(), 12f, level.dimension())),
                                        new ParticleSpawnAnointingS2CPacket(posQuery.getX(), posQuery.getY(), posQuery.getZ(), ESSENTIA_NOURISHING.getMateriaColor(), true));
                                break;
                            }
                        }

                        entity.index--;
                        if (entity.index < 0) break;

                        checkTally++;
                        if (checkTally > LIST_ITERATION_LIMIT) stillSearching = false;
                    }
                }
            }
        }
    }

    private static void reshuffleRemainingTargets(NourishingCenserBlockEntity entity) {
        entity.remainingTargets.clear();
        for (int y = (int) entity.operatingArea.minY; y <= (int) entity.operatingArea.maxY; y++) {
            for (int x = (int) entity.operatingArea.minX; x <= (int) entity.operatingArea.maxX; x++) {
                for (int z = (int) entity.operatingArea.minZ; z <= (int) entity.operatingArea.maxZ; z++) {
                    entity.remainingTargets.add(new BlockPos(x, y, z));
                }
            }
        }
        Collections.shuffle(entity.remainingTargets);
        entity.index = entity.remainingTargets.size() - 1;
    }

    private static boolean syncWithLevelData(Level pLevel, long pLongPos, NourishingCenserBlockEntity pEntity) {
        final CenserWorkingAreasSD censerData = CenserWorkingAreasSD.getCenserData(pLevel);
        boolean changed = false;
        if(censerData.hasCenserAt(pLongPos)) {
            final AABB query = censerData.getAABBForCenser(pLongPos);
            if(!query.equals(pEntity.operatingArea)) {
                censerData.addOrUpdateCenser(pLongPos, pEntity.operatingArea);
                pEntity.isOverlapping = censerData.checkOverlap(pLongPos);
                changed = true;
            }
        } else {
            censerData.addOrUpdateCenser(pLongPos, pEntity.operatingArea);
            pEntity.isOverlapping = censerData.checkOverlap(pLongPos);
            changed = true;
        }

        return changed;
    }
}
