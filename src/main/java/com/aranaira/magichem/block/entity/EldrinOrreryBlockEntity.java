package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.EldrinOrreryBlock;
import com.aranaira.magichem.foundation.IKeepsInventoryOnBreak;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IRequiresRouterCleanupOnDestruction;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.saveddata.EldrinOrreryLimiterSD;
import com.aranaira.magichem.gui.EldrinOrreryMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.aranaira.magichem.block.entity.renderer.EldrinOrreryBlockEntityRenderer.WELLSPRING_COLORS;
import static com.aranaira.magichem.block.entity.renderer.EldrinOrreryBlockEntityRenderer.WELLSPRING_STARTS;

public class EldrinOrreryBlockEntity extends BlockEntity implements MenuProvider, IShlorpReceiver, IMateriaProvisionRequester, IRequiresRouterCleanupOnDestruction, IKeepsInventoryOnBreak {
    public static final int
        SLOT_COUNT = 10, SLOT_INPUT_START = 0, SLOT_INPUT_COUNT = 5, SLOT_OUTPUT_START = 5, SLOT_OUTPUT_COUNT = 5,
        SLOT_SOLAR_INPUT = 0, SLOT_LUNAR_INPUT = 1, SLOT_SIDEREAL_INPUT = 2, SLOT_FIRMAMENT_INPUT = 3, SLOT_REALM_INPUT = 4,
        SLOT_SOLAR_OUTPUT = 5, SLOT_LUNAR_OUTPUT = 6, SLOT_SIDEREAL_OUTPUT = 7, SLOT_FIRMAMENT_OUTPUT = 8, SLOT_REALM_OUTPUT = 9,
        DATA_COUNT = 5,
        DATA_SOLAR = 0, DATA_LUNAR = 1, DATA_SIDEREAL = 2, DATA_FIRMAMENT = 3, DATA_REALM = 4,
        CHARGE_SOLAR = 120000, CHARGE_LUNAR = 192000, CHARGE_SIDEREAL = 288000, CHARGE_ADMIXTURES = 200,
        CHARGE_CAP_MULT_ORBS = 2, CHARGE_CAP_MULT_ADMIXTURES = 10;
    public static final float
            GEN_RATE = 0.0025f, GEN_BOOST_SOLAR = 3.0f, GEN_BOOST_LUNAR = 4.0f, GEN_BOOST_SIDEREAL = 5.0f,
        GEN_XMULT_REALM = 1.5f, GEN_XMULT_FIRMAMENT = 2.0f;
    public static final MateriaItem ADMIXTURE_FIRMAMENT = ItemRegistry.getMateriaMap(false, false).get("firmament");
    public static final MateriaItem ADMIXTURE_REALM = ItemRegistry.getMateriaMap(false, false).get("realm");

    private UUID placedBy;
    private Player playerRef;
    private int solar, lunar, sidereal, firmament, realm;
    private static final Random r = new Random();
    private boolean doWorldDataWrite = false;

    //animation drivers
    public float
        sunPercent = 0, moonPercent = 0,
        innerRingActivation = 0, outerRingActivation = 0,
        wellspringPercent = 0;

    private ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            switch(pIndex) {
                case DATA_SOLAR: {
                    return solar;
                }
                case DATA_LUNAR: {
                    return lunar;
                }
                case DATA_SIDEREAL: {
                    return sidereal;
                }
                case DATA_FIRMAMENT: {
                    return firmament;
                }
                case DATA_REALM: {
                    return realm;
                }
                default: return -1;
            }
        }

        @Override
        public void set(int pIndex, int pValue) {
            switch(pIndex) {
                case DATA_SOLAR: {
                    solar = pValue;
                }
                case DATA_LUNAR: {
                    lunar = pValue;
                }
                case DATA_SIDEREAL: {
                    sidereal = pValue;
                }
                case DATA_FIRMAMENT: {
                    firmament = pValue;
                }
                case DATA_REALM: {
                    realm = pValue;
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };
    private ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_SOLAR_INPUT)
                return stack.getItem() == ItemRegistry.SOLAR_ORB.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
            else if (slot == SLOT_LUNAR_INPUT)
                return stack.getItem() == ItemRegistry.LUNAR_ORB.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
            else if (slot == SLOT_SIDEREAL_INPUT)
                return stack.getItem() == ItemRegistry.SIDEREAL_ORB.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
            else if (slot == SLOT_FIRMAMENT_INPUT)
                return (stack.getItem() instanceof MateriaItem mi && mi.getMateriaName().equals("firmament")) || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
            else if (slot == SLOT_REALM_INPUT)
                return (stack.getItem() instanceof MateriaItem mi && mi.getMateriaName().equals("realm")) || stack.getItem() == ItemRegistry.DEBUG_ORB.get();

            return false;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(slot == SLOT_FIRMAMENT_INPUT || slot == SLOT_REALM_INPUT) {
                if(InventoryHelper.hasCustomModelData(getStackInSlot(slot)))
                    return ItemStack.EMPTY;
            }

            return super.extractItem(slot, amount, simulate);
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public EldrinOrreryBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ELDRIN_ORRERY_BE.get(), pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    public void setPlacedBy(Player player) {
        playerRef = player;
        placedBy = player.getUUID();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new EldrinOrreryMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    private static final Affinity[] AFFINITIES = {Affinity.ENDER, Affinity.EARTH, Affinity.WATER, Affinity.WIND, Affinity.FIRE, Affinity.ARCANE};
    private void injectPower() {
        if (!this.getLevel().isClientSide()) {
            this.getLevel().getCapability(WorldMagicProvider.MAGIC).ifPresent((m) -> {
                for(Affinity aff : AFFINITIES) {
                    for (int i = 0; i < AFFINITIES.length; i++) {
                        float amount = this.getPowerPerTick(aff);
                        m.getWellspringRegistry().insertPower(this.placedBy, this.level, aff, amount);
                    }
                }
            });
        }
    }

    private boolean canInjectPower() {
        MutableBoolean hasSpaceForGenTick = new MutableBoolean(false);
        if (!this.getLevel().isClientSide()) {
            if (placedBy == null) return false;
            if (playerRef == null) {
                playerRef = getLevel().getPlayerByUUID(placedBy);
            }
        }

        this.getLevel().getCapability(WorldMagicProvider.MAGIC).ifPresent((m) -> {
            IWellspringNodeRegistry wsRegistry = m.getWellspringRegistry();
            HashMap<Affinity, Float> nodeAmounts = wsRegistry.getNodeNetworkAmountFor(playerRef);

            if(nodeAmounts != null) {
                for (Affinity aff : AFFINITIES) {
                    for (int i = 0; i < AFFINITIES.length; i++) {
                        Float amountInNetwork = nodeAmounts.get(aff);
                        if(amountInNetwork != null) {
                            float spaceInNetwork = 5000 - amountInNetwork;

                            float mult = wsRegistry.getEldrinGenerationMultiplierFor(playerRef, aff);

                            float genThisTick = getBaseGenerationRate() * mult * getXMultRate();
                            if (spaceInNetwork >= genThisTick) {
                                hasSpaceForGenTick.setValue(true);
                                break;
                            }
                        }
                    }
                }
            }
        });

        return hasSpaceForGenTick.booleanValue();
    }

    public float getBaseGenerationRate() {
        float out = GEN_RATE;
        out += solar > 0 ? GEN_RATE * GEN_BOOST_SOLAR : 0;
        out += lunar > 0 ? GEN_RATE * GEN_BOOST_LUNAR : 0;
        out += sidereal > 0 ? GEN_RATE * GEN_BOOST_SIDEREAL : 0;
        return out;
    }

    public float getXMultRate() {
        float out = 1.0f;
        out *= realm > 0 ? GEN_XMULT_REALM : 1.0f;
        out *= firmament > 0 ? GEN_XMULT_FIRMAMENT : 1.0f;
        return out;
    }

    public float getPowerPerTick(Affinity aff) {
        MutableFloat powerPerTick = new MutableFloat(0.0F);
        this.getLevel().getCapability(WorldMagicProvider.MAGIC).ifPresent((m) -> {
            IWellspringNodeRegistry wsRegistry = m.getWellspringRegistry();

            float mult = wsRegistry.getEldrinGenerationMultiplierFor(playerRef, aff);
            float genThisTick = getBaseGenerationRate() * mult * getXMultRate();

            powerPerTick.setValue(genThisTick);
        });
        return powerPerTick.floatValue();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();

        return super.getCapability(cap, side);
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, E e) {
        if(e instanceof EldrinOrreryBlockEntity entity) {
            if(!level.isClientSide()) {
                if(entity.doWorldDataWrite) {
                    final EldrinOrreryLimiterSD eldrinOrreryData = level.getServer().overworld().getDataStorage().computeIfAbsent(EldrinOrreryLimiterSD::load, EldrinOrreryLimiterSD::create, "eldrinOrreryData");

                    if(entity.playerRef == null) {
                        entity.playerRef = level.getPlayerByUUID(entity.placedBy);
                    }

                    if(entity.playerRef != null && !eldrinOrreryData.playerHasOrrery(entity.playerRef)) {
                        eldrinOrreryData.addOrrery(entity.playerRef);
                    }

                    entity.doWorldDataWrite = false;
                }

                boolean changed = false;
                if(entity.solar <= CHARGE_SOLAR * CHARGE_CAP_MULT_ORBS - CHARGE_SOLAR) {
                    ItemStack inStack = entity.itemHandler.getStackInSlot(SLOT_SOLAR_INPUT);
                    ItemStack outStack = entity.itemHandler.getStackInSlot(SLOT_SOLAR_OUTPUT);
                    if(!inStack.isEmpty() && outStack.getCount() < outStack.getMaxStackSize()) {
                        entity.solar += CHARGE_SOLAR;
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get()) {
                            if (outStack.isEmpty()) {
                                entity.itemHandler.setStackInSlot(SLOT_SOLAR_OUTPUT, new ItemStack(ItemRegistry.GLASS_ORB.get()));
                            } else {
                                outStack.grow(1);
                            }
                        }
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get()) inStack.shrink(1);
                        changed = true;
                    }
                }
                if(entity.lunar <= CHARGE_LUNAR * CHARGE_CAP_MULT_ORBS - CHARGE_LUNAR) {
                    ItemStack inStack = entity.itemHandler.getStackInSlot(SLOT_LUNAR_INPUT);
                    ItemStack outStack = entity.itemHandler.getStackInSlot(SLOT_LUNAR_OUTPUT);
                    if (!inStack.isEmpty() && outStack.getCount() < outStack.getMaxStackSize()) {
                        entity.lunar += CHARGE_LUNAR;
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get()) {
                            if (outStack.isEmpty()) {
                                entity.itemHandler.setStackInSlot(SLOT_LUNAR_OUTPUT, new ItemStack(ItemRegistry.GLASS_ORB.get()));
                            } else {
                                outStack.grow(1);
                            }
                        }
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get()) inStack.shrink(1);
                        changed = true;
                    }
                }
                if(entity.sidereal <= CHARGE_SIDEREAL * CHARGE_CAP_MULT_ORBS - CHARGE_SIDEREAL) {
                    ItemStack inStack = entity.itemHandler.getStackInSlot(SLOT_SIDEREAL_INPUT);
                    ItemStack outStack = entity.itemHandler.getStackInSlot(SLOT_SIDEREAL_OUTPUT);
                    if (!inStack.isEmpty() && outStack.getCount() < outStack.getMaxStackSize()) {
                        entity.sidereal += CHARGE_SIDEREAL;
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get()) {
                            if (outStack.isEmpty()) {
                                entity.itemHandler.setStackInSlot(SLOT_SIDEREAL_OUTPUT, new ItemStack(ItemRegistry.GLASS_ORB.get()));
                            } else {
                                outStack.grow(1);
                            }
                        }
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get()) inStack.shrink(1);
                        changed = true;
                    }
                }
                if(entity.realm <= CHARGE_ADMIXTURES * CHARGE_CAP_MULT_ADMIXTURES - CHARGE_ADMIXTURES) {
                    ItemStack inStack = entity.itemHandler.getStackInSlot(SLOT_REALM_INPUT);
                    ItemStack outStack = entity.itemHandler.getStackInSlot(SLOT_REALM_OUTPUT);
                    if(!inStack.isEmpty() && outStack.getCount() < outStack.getMaxStackSize()) {
                        entity.realm += CHARGE_ADMIXTURES;
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get() && !InventoryHelper.hasCustomModelData(inStack)) {
                            if (outStack.isEmpty()) {
                                entity.itemHandler.setStackInSlot(SLOT_REALM_OUTPUT, new ItemStack(Items.GLASS_BOTTLE));
                            } else {
                                outStack.grow(1);
                            }
                        }
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get()) inStack.shrink(1);
                        changed = true;
                    }
                }
                if(entity.firmament <= CHARGE_ADMIXTURES * CHARGE_CAP_MULT_ADMIXTURES - CHARGE_ADMIXTURES) {
                    ItemStack inStack = entity.itemHandler.getStackInSlot(SLOT_FIRMAMENT_INPUT);
                    ItemStack outStack = entity.itemHandler.getStackInSlot(SLOT_FIRMAMENT_OUTPUT);
                    if(!inStack.isEmpty() && outStack.getCount() < outStack.getMaxStackSize()) {
                        entity.firmament += CHARGE_ADMIXTURES;
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get() && !InventoryHelper.hasCustomModelData(inStack)) {
                            if (outStack.isEmpty()) {
                                entity.itemHandler.setStackInSlot(SLOT_FIRMAMENT_OUTPUT, new ItemStack(Items.GLASS_BOTTLE));
                            } else {
                                outStack.grow(1);
                            }
                        }
                        if(inStack.getItem() != ItemRegistry.DEBUG_ORB.get()) inStack.shrink(1);
                        changed = true;
                    }
                }

                if(changed) entity.syncAndSave();
            }
            else {
                //animation drivers
                {
                    entity.wellspringPercent = Math.min(1, entity.wellspringPercent + 0.0075f);

                    entity.sunPercent = Math.max(0,Math.min(1, entity.sunPercent + 0.0125f * (entity.solar > 0 ? 1 : -1)));
                    entity.moonPercent = Math.max(0,Math.min(1, entity.moonPercent + 0.0125f * (entity.lunar > 0 ? 1 : -1)));

                    entity.innerRingActivation = Math.max(0,Math.min(1, entity.innerRingActivation + 0.0125f * ((entity.solar > 0 || entity.lunar > 0) ? 1 : -1)));
                    entity.outerRingActivation = Math.max(0,Math.min(1, entity.outerRingActivation + 0.0125f * ((entity.sidereal > 0) ? 1 : -1)));
                }

                //particle work
                {
                    //stars
                    if(entity.outerRingActivation > 0.95f) {
                        final float STAR_RADIUS_SCALAR = 0.669441f;

                        for(int i=0; i<6; i++) {
                            Vec3 point = new Vec3(r.nextDouble(10)-5, r.nextDouble(10)-5, r.nextDouble(10)-5)
                                    .normalize()
                                    .scale(STAR_RADIUS_SCALAR)
                                    .add(entity.getBlockPos().getX()+0.5, entity.getBlockPos().getY()+2.0, entity.getBlockPos().getZ()+0.5);

                            level.addParticle(new MAParticleType(ParticleInit.SPARKLE_STATIONARY.get())
                                            .setScale(0.03f).setMaxAge(48 + r.nextInt(48)),
                                    point.x, point.y, point.z,
                                    0, 0, 0);
                        }
                    }

                    //wellspring spirals
                    if(entity.wellspringPercent > 0.375f) {
                        Vec3 origin = new Vec3(entity.getBlockPos().getX(), entity.getBlockPos().getY()+0.5, entity.getBlockPos().getZ());

                        int spawnPeriod = 6;
                        int timeSlice = (int) (level.getGameTime() % (spawnPeriod * 6));
                        if(timeSlice % spawnPeriod == 0) {
                            int i = timeSlice / spawnPeriod;
                            level.addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                                            .setPhysics(false).setScale(0.015f).setMaxAge(80)
                                            .setColor(WELLSPRING_COLORS[i][0], WELLSPRING_COLORS[i][1], WELLSPRING_COLORS[i][2]),
                                    origin.x + WELLSPRING_STARTS[i].x, origin.y, origin.z + WELLSPRING_STARTS[i].y,
                                    (0.025 + r.nextDouble(0.1)) * (r.nextBoolean() ? 1 : -1), 0.02 + r.nextDouble() * 0.01, -0.1250);
                        }
                    }
                }
            }

            if(entity.canInjectPower()) {
                entity.solar = Math.max(0, entity.solar-1);
                entity.lunar = Math.max(0, entity.lunar-1);
                entity.sidereal = Math.max(0, entity.sidereal-1);
                entity.realm = Math.max(0, entity.realm-1);
                entity.firmament = Math.max(0, entity.firmament-1);

                entity.injectPower();
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        doWorldDataWrite = true;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putString("uuid", placedBy.toString());
        nbt.putLong("solar", solar);
        nbt.putLong("lunar", lunar);
        nbt.putLong("sidereal", sidereal);
        nbt.putInt("firmament", firmament);
        nbt.putInt("realm", realm);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        placedBy = UUID.fromString(nbt.getString("uuid"));
        solar = (int)nbt.getLong("solar");
        lunar = (int)nbt.getLong("lunar");
        sidereal = (int)nbt.getLong("sidereal");
        firmament = nbt.getInt("firmament");
        realm = nbt.getInt("realm");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putString("uuid", placedBy.toString());
        nbt.putLong("solar", solar);
        nbt.putLong("lunar", lunar);
        nbt.putLong("sidereal", sidereal);
        nbt.putInt("firmament", firmament);
        nbt.putInt("realm", realm);
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
    public void packDataToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.ELDRIN_ORRERY.get());

        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("solar", solar);
        nbt.putInt("lunar", lunar);
        nbt.putInt("sidereal", sidereal);
        nbt.putInt("realm", realm);
        nbt.putInt("firmament", firmament);

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {
        if(pNBT.contains("inventory")) {
            itemHandler.deserializeNBT(pNBT.getCompound("inventory"));
            solar = pNBT.getInt("solar");
            lunar = pNBT.getInt("lunar");
            sidereal = pNBT.getInt("sidereal");
            realm = pNBT.getInt("realm");
            firmament = pNBT.getInt("firmament");
        }
    }

    public float getSolarFillPercent(){
        return (float)solar / (float)(CHARGE_SOLAR * CHARGE_CAP_MULT_ORBS);
    }

    public float getLunarFillPercent(){
        return (float)lunar / (float)(CHARGE_LUNAR * CHARGE_CAP_MULT_ORBS);
    }

    public float getSiderealFillPercent(){
        return (float)sidereal / (float)(CHARGE_SIDEREAL * CHARGE_CAP_MULT_ORBS);
    }

    public float getRealmFillPercent(){
        return (float)realm / (float)(CHARGE_ADMIXTURES * CHARGE_CAP_MULT_ADMIXTURES);
    }

    public float getFirmamentFillPercent(){
        return (float)firmament / (float)(CHARGE_ADMIXTURES * CHARGE_CAP_MULT_ADMIXTURES);
    }

    public int getSolarFill(){
        return solar;
    }

    public int getLunarFill(){
        return lunar;
    }

    public float getSiderealFill(){
        return sidereal;
    }

    public float getRealmFill(){
        return realm;
    }

    public float getFirmamentFill(){
        return firmament;
    }

    public UUID getPlacedBy() {
        return placedBy;
    }

    private final NonNullList<MateriaItem> activeProvisionRequests = NonNullList.create();

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        if(activeProvisionRequests.size() >= 2)
            return false;

        ItemStack firmamentStack = itemHandler.getStackInSlot(SLOT_FIRMAMENT_INPUT);
        boolean needsFirmament = false;
        if(firmamentStack.isEmpty() || InventoryHelper.hasCustomModelData(firmamentStack)) {
            needsFirmament = firmamentStack.getCount() < 32;
        }

        ItemStack realmStack = itemHandler.getStackInSlot(SLOT_REALM_INPUT);
        boolean needsRealm = false;
        if(realmStack.isEmpty() || InventoryHelper.hasCustomModelData(realmStack)) {
            needsRealm = realmStack.getCount() < 32;
        }

        return needsFirmament || needsRealm;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        if(!activeProvisionRequests.contains(ADMIXTURE_FIRMAMENT)){
            ItemStack firmamentStack = itemHandler.getStackInSlot(SLOT_FIRMAMENT_INPUT);
            if (firmamentStack.isEmpty()) {
                result.put(ADMIXTURE_FIRMAMENT, 32);
            } else if (InventoryHelper.hasCustomModelData(firmamentStack)) {
                if (firmamentStack.getCount() <= 32) {
                    result.put(ADMIXTURE_FIRMAMENT, 32);
                }
            }
        }

        if(!activeProvisionRequests.contains(ADMIXTURE_REALM)) {
            ItemStack realmStack = itemHandler.getStackInSlot(SLOT_REALM_INPUT);
            if (realmStack.isEmpty()) {
                result.put(ADMIXTURE_REALM, 32);
            } else if (InventoryHelper.hasCustomModelData(realmStack)) {
                if (realmStack.getCount() <= 32) {
                    result.put(ADMIXTURE_REALM, 32);
                }
            }
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(pMateriaItem == ADMIXTURE_FIRMAMENT || pMateriaItem == ADMIXTURE_REALM) {
            activeProvisionRequests.add(pMateriaItem);
        }
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        if(pStack.getItem() == ADMIXTURE_FIRMAMENT) {
            ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_FIRMAMENT_INPUT);

            if(insertionStack.isEmpty()) {
                insertionStack = pStack.copy();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("CustomModelData", 1);
                insertionStack.setTag(nbt);
            } else {
                insertionStack.grow(pStack.getCount());
            }
            itemHandler.setStackInSlot(SLOT_FIRMAMENT_INPUT, insertionStack);

            syncAndSave();

            activeProvisionRequests.remove((MateriaItem)pStack.getItem());
        }
        else if(pStack.getItem() == ADMIXTURE_REALM) {
            ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_REALM_INPUT);

            if(insertionStack.isEmpty()) {
                insertionStack = pStack.copy();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("CustomModelData", 1);
                insertionStack.setTag(nbt);
            } else {
                insertionStack.grow(pStack.getCount());
            }
            itemHandler.setStackInSlot(SLOT_REALM_INPUT, insertionStack);

            syncAndSave();

            activeProvisionRequests.remove((MateriaItem)pStack.getItem());
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() == ADMIXTURE_FIRMAMENT || pStack.getItem() == ADMIXTURE_REALM) {
            return 0;
        }
        return pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);
        return 0;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3,6,3));
    }

    @Override
    public void destroyRouters() {
        EldrinOrreryBlock.destroyRouters(getLevel(), getBlockPos());
    }
}
