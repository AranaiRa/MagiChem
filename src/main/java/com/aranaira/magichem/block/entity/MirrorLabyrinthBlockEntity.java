package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.MirrorLabyrinthBlock;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeDynamicBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.gui.AlembicMenu;
import com.aranaira.magichem.gui.MirrorLabyrinthMenu;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.aranaira.magichem.util.render.ConstructRenderHelper;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstructConstruction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.EntityInit;
import com.mna.entities.constructs.animated.Construct;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.util.render.ColorUtils.SIX_STEP_PARTICLE_COLORS;

public class MirrorLabyrinthBlockEntity extends AbstractMateriaStorageMultiTypeDynamicBlockEntity implements MenuProvider, IShlorpReceiver, IRequiresRouterCleanupOnDestruction, ICanAbsorbConstructs {

    public static final Random r = new Random();
    public static final int
            SLOT_COUNT = 4,
            SLOT_INPUT = 0, SLOT_INPUT_RESULT = 1, SLOT_EXTRACT = 2, SLOT_EXTRACT_RESULT = 3,
            BASE_ENERGY_DRAIN = 2000;
    public static final float
            CIRCLE_FILL_RATE = 0.025f, PARTICLE_PERCENT_RATE = 0.05f,
            MATRIX_ACTIVATION_RATE = 0.0125f, CONSTRUCT_ACTIVATION_RATE = 0.0185f, MIRROR_ACTIVATION_RATE = 0.0215f;
    private boolean
            hasSufficientPower = false, redstonePaused = false;
    public boolean
            constructDataChanged = false, isCompactMode = true, needsGuiStorageUpdate = true;
    public float
            circlePercent = 1.0f, particlePercent = 1.0f,
            mirrorActivationPercent = 0.0f, mirrorActivationSpeed = 0.0f,
            matrixActivationPercent = 0.0f, matrixActivationSpeed = 0.0f,
            constructActivationPercent = 0.0f, constructActivationSpeed = 0.0f;
    private int
            powerLevel = 0;
    private CompoundTag storedConstruct = new CompoundTag();
    private ArrayList<MateriaItem> materiaTypesSorted = new ArrayList<>();
    private MateriaItem activeMateriaType = null;
    public Map<ConstructRenderHelper.ConstructPartType, Pair<ResourceLocation, Vector3>> renderData = new HashMap<>();
    private ContainerData data = new SimpleContainerData(0);
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_INPUT) {
                return stack.getItem() instanceof MateriaItem ||
                       stack.getItem() == BlockRegistry.MATERIA_JAR.get().asItem() ||
                       stack.getItem() == BlockRegistry.MATERIA_JAR_QUAD.get().asItem() ||
                       stack.getItem() == BlockRegistry.MATERIA_VESSEL.get().asItem();
            }
            else if(slot == SLOT_EXTRACT) {
                return stack.getItem() == Items.GLASS_BOTTLE ||
                       stack.getItem() == BlockRegistry.MATERIA_JAR.get().asItem() ||
                       stack.getItem() == BlockRegistry.MATERIA_VESSEL.get().asItem();
            }

            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    public static final int[][] TRAIL_PARTICLE_COLORS = {
            {101, 112, 120},
            {101, 112, 120},
            { 94,  86, 130},
            { 75,  84, 130},
            { 81, 107,  42},
            { 48, 113, 171},
            { 34, 115, 111}
    };
    /**
     * Used for generating the lightning bolts from the chimerite spikes to the mirrors as well as picking a random mirror position and tangent.
     * Direction is a flag for the lightning bolts to skip drawing if the block's facing matches.
     * First vector3 is the mirror's center
     * First two Vector3s in the triplet are the positions of the tips of the two chimerite spikes
     * Last Vector3 of the triplet is the mirror's tangent
     */
    public static final Triplet<Direction, Vector3, Triplet<Vector3, Vector3, Vector3>>[] MIRROR_BOLT_POSITION_DATA = new Triplet[]{
            new Triplet<>(null,
                    new Vector3(-1.41421, 2.5, -1.41421),
                    new Triplet<>(
                            new Vector3(-0.925698, 1.08956, -1.27925),
                            new Vector3(-1.27925, 1.08956, -0.925698),
                            new Vector3(1.414, 0, 1.414))),
            new Triplet<>(Direction.WEST,
                    new Vector3(-2, 2.5, 0),
                    new Triplet<>(
                            new Vector3(-1.55913, 1.08956, -0.25),
                            new Vector3(-1.55913, 1.08956, 0.25),
                            new Vector3(2, 0, 0))),
            new Triplet<>(null,
                    new Vector3(-1.41421, 2.5, 1.41421),
                    new Triplet<>(
                            new Vector3(-1.27925, 1.08956, 0.925698),
                            new Vector3(-0.925698, 1.08956, 1.27925),
                            new Vector3(1.414, 0, -1.414))),
            new Triplet<>(Direction.SOUTH,
                    new Vector3(0, 2.5, -2),
                    new Triplet<>(
                            new Vector3(-0.25, 1.08956, -1.55913),
                            new Vector3(0.25, 1.08956, -1.55913),
                            new Vector3(0, 0, 2))),
            new Triplet<>(null,
                    new Vector3(1.41421, 2.5, 1.41421),
                    new Triplet<>(
                            new Vector3(1.27925, 1.08956, 0.925698),
                            new Vector3(0.925698, 1.08956, 1.27925),
                            new Vector3(-1.414, 0, -1.414))),
            new Triplet<>(Direction.EAST,
                    new Vector3(2, 2.5, 0),
                    new Triplet<>(
                            new Vector3(1.55913, 1.08956, -0.25),
                            new Vector3(1.55913, 1.08956, 0.25),
                            new Vector3(-2, 0, 0))),
            new Triplet<>(null,
                    new Vector3(1.41421, 2.5, -1.41421),
                    new Triplet<>(
                            new Vector3(1.27925, 1.08956, -0.925698),
                            new Vector3(0.925698, 1.08956, -1.27925),
                            new Vector3(-1.414, 1.08956, 1.414))),
            new Triplet<>(Direction.NORTH,
                    new Vector3(0, 2.5, 2),
                    new Triplet<>(
                            new Vector3(-0.25, 1.08956, 1.55913),
                            new Vector3(0.25, 1.08956, 1.55913),
                            new Vector3(0, 0, -2))),
    };

    public MirrorLabyrinthBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MIRROR_LABYRINTH_BE.get(), pos, state);
    }

    @Override
    public boolean isBelowTypeLimit() {
        //Mirror Labyrinth is ALWAYS hangry
        return true;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new MirrorLabyrinthMenu(pContainerId, pPlayerInventory, this, this.data);
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

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void load(CompoundTag nbt) {
        if(nbt == null) nbt = new CompoundTag();

        CompoundTag pre = storedConstruct.copy();
        super.load(nbt);

        storedConstruct = nbt.getCompound("construct");
        if(!storedConstruct.equals(pre))
            constructDataChanged = true;

        itemHandler.deserializeNBT(nbt.getCompound("inventory"));

        powerLevel = nbt.getByte("powerLevel");

        updateSortedMateriaTypeList();
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("construct", storedConstruct);
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putByte("powerLevel", (byte)powerLevel);
        super.saveAdditional(nbt);
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        load(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        nbt.put("construct", storedConstruct);
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putByte("powerLevel", (byte)powerLevel);
        return nbt;
    }

    @Override
    public Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType) {
        final Direction facing = getBlockState().getValue(FACING);

        int[] randomTangent = new int[0];
        Vector3 offset = Vector3.zero();
        if(facing == Direction.NORTH) {
            randomTangent = new int[]{0, 1, 2, 3, 4, 5, 6};
        }
        else if(facing == Direction.EAST) {
            randomTangent = new int[]{0, 1, 2, 3, 4, 6, 7};
        }
        else if(facing == Direction.SOUTH) {
            randomTangent = new int[]{0,1,2,4,5,6,7};
            offset = new Vector3(0.5, r.nextDouble() - 0.5, 1.5);
        }
        else if(facing == Direction.WEST) {
            randomTangent = new int[]{0,2,3,4,5,6,7};
        }

        if(randomTangent.length > 0) {
            int index = randomTangent[r.nextInt(randomTangent.length)];
            return new Pair<>(MIRROR_BOLT_POSITION_DATA[index].getSecond().add(offset), MIRROR_BOLT_POSITION_DATA[index].getThird().getThird());
        }

        return new Pair<>(Vector3.zero(), Vector3.up());
    }

    @Override
    public void destroyRouters() {
        MirrorLabyrinthBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(FACING));
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-5, 0, -5), getBlockPos().offset(5,6,5));
    }

    public boolean tryAbsorbConstruct(Player pPlayer) {
        AABB zone = new AABB(getBlockPos().offset(-5, -5, -5), getBlockPos().offset(5, 5, 5));

        Construct targetConstruct = null;
        for (Construct constructInZone : pPlayer.level().getEntitiesOfClass(Construct.class, zone)) {
            if(constructInZone.isFollowing(pPlayer)) {
                final IConstructConstruction constructData = constructInZone.getConstructData();

                boolean noEnderLeggy = !constructData.isCapabilityEnabled(ConstructCapability.TELEPORT);
                boolean hasSmartHead = constructData.calculateIntelligence() > 9;
                boolean hasCasterArm = constructData.isCapabilityEnabled(ConstructCapability.CAST_SPELL);
                boolean otherArmValid =
                        constructData.isCapabilityEnabled(ConstructCapability.CARRY) ||
                        constructData.isCapabilityEnabled(ConstructCapability.SHEAR) ||
                        constructData.isCapabilityEnabled(ConstructCapability.CHOP_WOOD) ||
                        constructData.isCapabilityEnabled(ConstructCapability.SMITH) ||
                        constructData.isCapabilityEnabled(ConstructCapability.FLUID_DISPENSE);

                if(hasSmartHead && hasCasterArm && otherArmValid && noEnderLeggy) {
                    targetConstruct = constructInZone;
                    break;
                }
            }
        }

        if(targetConstruct == null)
            return false;

        storedConstruct = targetConstruct.serializeNBT();
        targetConstruct.remove(Entity.RemovalReason.DISCARDED);
        syncAndSave();

        return true;
    }

    public boolean hasConstruct() {
        return !storedConstruct.isEmpty();
    }

    public void ejectConstruct() {
        if(!storedConstruct.isEmpty()) {
            Construct construct = new Construct(EntityInit.ANIMATED_CONSTRUCT.get(), getLevel());
            construct.deserializeNBT(storedConstruct);
            construct.setPos(getBlockPos().getX(), getBlockPos().getY() + 1, getBlockPos().getZ());
            getLevel().addFreshEntity(construct);
            storedConstruct = new CompoundTag();
            syncAndSave();
        }
    }

    public CompoundTag getStoredConstructComposition() {
        if(storedConstruct.contains("animated_construct_composition"))
            return storedConstruct.getCompound("animated_construct_composition");
        else
            return new CompoundTag();
    }

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, MirrorLabyrinthBlockEntity pEntity) {
        if(pLevel.isClientSide()) {
            pEntity.handleAnimationDrivers();

            //particle work
            {
                //control dais
                if (pEntity.particlePercent > 0) {
                    Vector3 center = Vector3.zero();
                    Direction facing = pEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (facing == Direction.NORTH)
                        center = new Vector3(pPos.getX() + 0.5, pPos.getY() + 1.375, pPos.getZ() + 1.5);
                    else if (facing == Direction.EAST)
                        center = new Vector3(pPos.getX() - 0.5, pPos.getY() + 1.375, pPos.getZ() + 0.5);
                    else if (facing == Direction.SOUTH)
                        center = new Vector3(pPos.getX() + 0.5, pPos.getY() + 1.375, pPos.getZ() - 0.5);
                    else if (facing == Direction.WEST)
                        center = new Vector3(pPos.getX() + 1.5, pPos.getY() + 1.375, pPos.getZ() + 0.5);

                    int colorIndex = r.nextInt(6);
                    if (pEntity.getLevel().getGameTime() % 8 == 0) {
                        pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setColor(SIX_STEP_PARTICLE_COLORS[colorIndex][0], SIX_STEP_PARTICLE_COLORS[colorIndex][1], SIX_STEP_PARTICLE_COLORS[colorIndex][2])
                                        .setScale(0.4f * pEntity.particlePercent).setMaxAge(80),
                                center.x, center.y, center.z,
                                0, 0, 0);
                    }
                    pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(255, 255, 255).setScale(0.2f * pEntity.particlePercent),
                            center.x, center.y, center.z,
                            0, 0, 0);

                    if (pEntity.particlePercent == 1) {
                        for (int i = 0; i < 2; i++) {
                            Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                            pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                            .setColor(SIX_STEP_PARTICLE_COLORS[colorIndex][0], SIX_STEP_PARTICLE_COLORS[colorIndex][1], SIX_STEP_PARTICLE_COLORS[colorIndex][2], 128)
                                            .setScale(0.09f).setMaxAge(16)
                                            .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                    center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                    0, 0, 0);

                            pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                            .setScale(0.015f).setMaxAge(16)
                                            .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                    center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                    0, 0, 0);
                        }
                    }
                }

                //central column
                if (pEntity.matrixActivationPercent > 0.875f) {
                    Vector3 center = Vector3.zero();
                    Direction facing = pEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (facing == Direction.NORTH)
                        center = new Vector3(pPos.getX() + 0.5, pPos.getY() + 0.25, pPos.getZ() - 0.5);
                    else if (facing == Direction.EAST)
                        center = new Vector3(pPos.getX() + 1.5, pPos.getY() + 0.25, pPos.getZ() + 0.5);
                    else if (facing == Direction.SOUTH)
                        center = new Vector3(pPos.getX() + 0.5, pPos.getY() + 0.25, pPos.getZ() + 1.5);
                    else if (facing == Direction.WEST)
                        center = new Vector3(pPos.getX() - 0.5, pPos.getY() + 0.25, pPos.getZ() + 0.5);

                    if (pEntity.getLevel().getGameTime() % 6 == 0) {
                        int colorIndex = r.nextInt(TRAIL_PARTICLE_COLORS.length);

                        pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                                        .setPhysics(false).setScale(0.0625f).setMaxAge(60)
                                        .setColor(TRAIL_PARTICLE_COLORS[colorIndex][0], TRAIL_PARTICLE_COLORS[colorIndex][1], TRAIL_PARTICLE_COLORS[colorIndex][2], 128),
                                center.x, center.y, center.z,
                                r.nextDouble(0.125) + 0.025, 0.08, 0.425);
                    }
                    if (pEntity.getLevel().getGameTime() % 2 == 0) {
                        double theta = r.nextDouble() * Math.PI * 2;
                        double offsetX = Math.cos(theta) * 0.5;
                        double offsetZ = Math.sin(theta) * 0.5;
                        double offsetY = 1.75;

                        pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                        .setScale(0.1f).setMaxAge(64 + r.nextInt(32)).setColor(255, 255, 255, 72)
                                        .setMover(new ParticleLerpMover(
                                                center.x + offsetX, center.y, center.z + offsetZ,
                                                center.x + offsetX, center.y + offsetY, center.z + offsetZ)),
                                center.x + offsetX, center.y, center.z + offsetZ,
                                0, 0, 0);

                        offsetX = Math.cos(theta) * 0.375;
                        offsetZ = Math.sin(theta) * 0.375;
                        offsetY = 2.75;

                        pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                        .setScale(0.1f).setMaxAge(64 + r.nextInt(32)).setColor(255, 255, 255, 36)
                                        .setMover(new ParticleLerpMover(
                                                center.x + offsetX, center.y, center.z + offsetZ,
                                                center.x + offsetX, center.y + offsetY, center.z + offsetZ)),
                                center.x + offsetX, center.y, center.z + offsetZ,
                                0, 0, 0);
                    }
                }

                //mirror bolts
                if (pEntity.mirrorActivationPercent > 0.875) {
                    Vector3 center = Vector3.zero();
                    Direction facing = pEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (facing == Direction.NORTH)
                        center = new Vector3(pPos.getX() + 0.5, pPos.getY(), pPos.getZ() - 0.5);
                    else if (facing == Direction.EAST)
                        center = new Vector3(pPos.getX() + 1.5, pPos.getY(), pPos.getZ() + 0.5);
                    else if (facing == Direction.SOUTH)
                        center = new Vector3(pPos.getX() + 0.5, pPos.getY(), pPos.getZ() + 1.5);
                    else if (facing == Direction.WEST)
                        center = new Vector3(pPos.getX() - 0.5, pPos.getY(), pPos.getZ() + 0.5);

                    if (pEntity.getLevel().getGameTime() % 2 == 0) {
                        int index = r.nextInt(MIRROR_BOLT_POSITION_DATA.length);
                        final Triplet<Direction, Vector3, Triplet<Vector3, Vector3, Vector3>> data = MIRROR_BOLT_POSITION_DATA[index];
                        if(facing != data.getFirst()) {
                            Vector3 inner = r.nextBoolean() ? data.getThird().getFirst() : data.getThird().getSecond();
                            Vector3 outer = data.getSecond();

                            pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                            .setMaxAge(8 + r.nextInt(6)).setScale(20),
                                    center.x + inner.x, center.y + inner.y, center.z + inner.z,
                                    center.x + outer.x, center.y + outer.y + r.nextFloat() - 0.5, center.z + outer.z);
                        }
                    }
                }
            }
        }
        else {
            //handle insertion
            if(!pEntity.itemHandler.getStackInSlot(SLOT_INPUT).isEmpty()) {
                final ItemStack insertionStack = pEntity.itemHandler.getStackInSlot(SLOT_INPUT);
                final ItemStack insertionContainers = pEntity.itemHandler.getStackInSlot(SLOT_INPUT_RESULT);

                if(insertionStack.getItem() instanceof MateriaItem mi) {
                    int insertionLimit = 0;
                    if(insertionContainers.isEmpty()) insertionLimit = 64;
                    else if(insertionContainers.getItem() == Items.GLASS_BOTTLE) insertionLimit = 64 - insertionContainers.getCount();

                    int inserted = Math.min(insertionLimit, insertionStack.getCount());

                    if(inserted > 0) {
                        int extant = 0;
                        if(pEntity.materiaStorage.containsKey(mi)) {
                            extant = pEntity.materiaStorage.get(mi);
                        }
                        pEntity.materiaStorage.put(mi, Math.max(extant, Math.min(pEntity.getStorageLimit(mi), extant + inserted)));

                        insertionStack.shrink(inserted);
                        pEntity.itemHandler.setStackInSlot(SLOT_INPUT_RESULT, new ItemStack(Items.GLASS_BOTTLE, insertionContainers.getCount() + inserted));
                        pEntity.syncAndSave();
                    }
                }
                else if(insertionStack.getItem() == BlockRegistry.MATERIA_JAR.get().asItem() || insertionStack.getItem() == BlockRegistry.MATERIA_VESSEL.get().asItem()) {
                    int insertionLimit = 0;
                    if(insertionContainers.isEmpty()) insertionLimit = 64;
                    else if(insertionContainers.getItem() == insertionStack.getItem()) insertionLimit = 64 - insertionContainers.getCount();

                    if(insertionStack.hasTag() && insertionStack.getTag().contains("type") && insertionStack.getTag().contains("amount")) {

                        int inserted = insertionStack.getTag().getInt("amount");
                        MateriaItem mi = materiaMap.get(insertionStack.getTag().getString("type"));

                        if (inserted > 0 && insertionLimit > 0) {
                            int extant = 0;
                            if (pEntity.materiaStorage.containsKey(mi)) {
                                extant = pEntity.materiaStorage.get(mi);
                            }
                            pEntity.materiaStorage.put(mi, Math.max(extant, Math.min(pEntity.getStorageLimit(mi), extant + inserted)));

                            pEntity.itemHandler.setStackInSlot(SLOT_INPUT_RESULT, new ItemStack(insertionStack.getItem(), insertionContainers.getCount() + 1));
                            insertionStack.shrink(1);
                            pEntity.syncAndSave();
                        }
                    }
                }
                else if(insertionStack.getItem() == BlockRegistry.MATERIA_JAR_QUAD.get().asItem()) {
                    int insertionLimit = 0;
                    if (insertionContainers.isEmpty()) insertionLimit = 64;
                    else if (insertionContainers.getItem() == BlockRegistry.MATERIA_JAR.get().asItem())
                        insertionLimit = 64 - insertionContainers.getCount();

                    if(insertionLimit >= 4) {
                        for (int i = 0; i < 4; i++) {
                            if (insertionStack.hasTag() && insertionStack.getTag().contains("materiaType" + i)) {

                                int inserted = insertionStack.getTag().getCompound("materiaType"+i).getInt("count");
                                MateriaItem mi = materiaMap.get(insertionStack.getTag().getCompound("materiaType"+i).getString("type"));

                                if (inserted > 0) {
                                    int extant = 0;
                                    if (pEntity.materiaStorage.containsKey(mi)) {
                                        extant = pEntity.materiaStorage.get(mi);
                                    }
                                    pEntity.materiaStorage.put(mi, Math.min(pEntity.getStorageLimit(mi), extant + inserted));
                                }
                            }
                        }

                        pEntity.itemHandler.setStackInSlot(SLOT_INPUT_RESULT, new ItemStack(BlockRegistry.MATERIA_JAR.get().asItem(), insertionContainers.getCount() + 4));
                        insertionStack.shrink(1);
                        pEntity.syncAndSave();
                    }
                }
            }
            //handle extraction
            if(!pEntity.itemHandler.getStackInSlot(SLOT_EXTRACT).isEmpty() && pEntity.materiaStorage.containsKey(pEntity.activeMateriaType)) {
                final ItemStack extractionStack = pEntity.itemHandler.getStackInSlot(SLOT_EXTRACT);
                final ItemStack extractionContainers = pEntity.itemHandler.getStackInSlot(SLOT_EXTRACT_RESULT);

                if(extractionStack.getItem() == Items.GLASS_BOTTLE) {
                    if(extractionContainers.isEmpty() || extractionContainers.getItem() == pEntity.activeMateriaType) {
                        int extractionLimit = Math.min(
                                pEntity.materiaStorage.get(pEntity.activeMateriaType), Math.min(
                                extractionStack.getCount(), 64 - extractionContainers.getCount()));
                        if(extractionLimit > 0) {
                            extractionStack.shrink(extractionLimit);
                            pEntity.materiaStorage.put(pEntity.activeMateriaType, pEntity.materiaStorage.get(pEntity.activeMateriaType) - extractionLimit);
                            pEntity.itemHandler.setStackInSlot(SLOT_EXTRACT_RESULT, new ItemStack(pEntity.activeMateriaType, extractionContainers.getCount() + extractionLimit));
                            pEntity.syncAndSave();
                        }
                    }
                }
                else if(extractionStack.getItem() == BlockRegistry.MATERIA_JAR.get().asItem() || extractionStack.getItem() == BlockRegistry.MATERIA_VESSEL.get().asItem()) {
                    if(extractionStack.hasTag() && extractionStack.getTag().contains("type") && extractionStack.getTag().contains("amount")) {
                        MateriaItem mi = materiaMap.get(extractionStack.getTag().getString("type"));
                        if(mi == pEntity.activeMateriaType) {
                            int containerLimit = extractionStack.getItem() == BlockRegistry.MATERIA_JAR.get().asItem() ?
                                    (mi instanceof EssentiaItem ? ServerConfig.materiaJarEssentiaCapacity : ServerConfig.materiaJarAdmixtureCapacity) :
                                    (mi instanceof EssentiaItem ? ServerConfig.materiaVesselEssentiaCapacity : ServerConfig.materiaVesselAdmixtureCapacity);
                            int existing = extractionStack.getTag().getInt("amount");

                            if (pEntity.materiaStorage.containsKey(mi) && extractionContainers.isEmpty()) {
                                int extractionLimit = Math.min(
                                        containerLimit - existing,
                                        pEntity.materiaStorage.get(mi));
                                ItemStack newOutput = new ItemStack(extractionStack.getItem());
                                CompoundTag newOutputTag = new CompoundTag();
                                newOutputTag.putString("type", mi.getMateriaName());
                                newOutputTag.putInt("amount", extractionLimit + existing);

                                newOutput.setTag(newOutputTag);
                                pEntity.itemHandler.setStackInSlot(SLOT_EXTRACT_RESULT, newOutput);
                                extractionStack.shrink(1);

                                pEntity.materiaStorage.put(mi, pEntity.materiaStorage.get(mi) - extractionLimit);

                                pEntity.syncAndSave();
                            }
                        }
                    } else if(pEntity.activeMateriaType != null) {
                        int containerLimit = extractionStack.getItem() == BlockRegistry.MATERIA_JAR.get().asItem() ?
                                (pEntity.activeMateriaType instanceof EssentiaItem ? ServerConfig.materiaJarEssentiaCapacity : ServerConfig.materiaJarAdmixtureCapacity) :
                                (pEntity.activeMateriaType instanceof EssentiaItem ? ServerConfig.materiaVesselEssentiaCapacity : ServerConfig.materiaVesselAdmixtureCapacity);

                        if(pEntity.materiaStorage.containsKey(pEntity.activeMateriaType) && extractionContainers.isEmpty()) {
                            int extractionLimit = Math.min(
                                    containerLimit,
                                    pEntity.materiaStorage.get(pEntity.activeMateriaType));
                            ItemStack newOutput = new ItemStack(extractionStack.getItem());
                            CompoundTag newOutputTag = new CompoundTag();
                            newOutputTag.putString("type", pEntity.activeMateriaType.getMateriaName());
                            newOutputTag.putInt("amount", extractionLimit);

                            newOutput.setTag(newOutputTag);
                            pEntity.itemHandler.setStackInSlot(SLOT_EXTRACT_RESULT, newOutput);
                            extractionStack.shrink(1);

                            pEntity.materiaStorage.put(pEntity.activeMateriaType, pEntity.materiaStorage.get(pEntity.activeMateriaType) - extractionLimit);

                            pEntity.syncAndSave();
                        }
                    }
                }
            }
        }

        pEntity.hasSufficientPower = true;//pLevel.getGameTime() % 600 < 300;
    }

    public void handleAnimationDrivers() {
        if(particlePercent == 1) {
            circlePercent = Math.min(1, circlePercent + CIRCLE_FILL_RATE);
            matrixActivationSpeed = matrixActivationPercent == 1 ? 0 : MATRIX_ACTIVATION_RATE;
            if(matrixActivationPercent == 1) {
                constructActivationSpeed = constructActivationPercent == 1 ? 0 : CONSTRUCT_ACTIVATION_RATE;
                mirrorActivationSpeed = mirrorActivationPercent == 1 ? 0 : MIRROR_ACTIVATION_RATE;
            }
        } else if(particlePercent == 0) {
            circlePercent = Math.max(0, circlePercent - CIRCLE_FILL_RATE);
            constructActivationSpeed = constructActivationPercent == 0 ? 0 : -CONSTRUCT_ACTIVATION_RATE;
            mirrorActivationSpeed = mirrorActivationPercent == 0 ? 0 : -MIRROR_ACTIVATION_RATE;
            if(constructActivationPercent == 0 && mirrorActivationPercent == 0) {
                matrixActivationSpeed = matrixActivationPercent == 0 ? 0 : -MATRIX_ACTIVATION_RATE;
            }
        }

        matrixActivationPercent = MathUtils.clamp(matrixActivationPercent + matrixActivationSpeed, 0, 1);
        constructActivationPercent = MathUtils.clamp(constructActivationPercent + constructActivationSpeed, 0, 1);
        mirrorActivationPercent = MathUtils.clamp(mirrorActivationPercent + mirrorActivationSpeed, 0, 1);

        if(hasSufficientPower && !redstonePaused) {
            particlePercent = Math.min(1, particlePercent + PARTICLE_PERCENT_RATE);
        } else {
            particlePercent = Math.max(0, particlePercent - PARTICLE_PERCENT_RATE);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    public boolean hasItemInInsertResultSlot() {
        return !itemHandler.getStackInSlot(SLOT_INPUT_RESULT).isEmpty();
    }

    public boolean hasItemInExtractResultSlot() {
        return !itemHandler.getStackInSlot(SLOT_EXTRACT_RESULT).isEmpty();
    }

    public void updateSortedMateriaTypeList() {
        Set<MateriaItem> keySet = materiaStorage.keySet();
        keySet.remove(null);
        materiaTypesSorted.clear();
        materiaTypesSorted.addAll(keySet);

        materiaTypesSorted.sort(Comparator.comparing(MateriaItem::getMateriaSortingName));

        needsGuiStorageUpdate = true;
    }

    @Override
    public Collection<MateriaItem> getMateriaTypes() {
        if(hasConstruct())
            return super.getMateriaTypes();

        return new ArrayList<>();
    }

    public List<MateriaItem> getMateriaTypesSorted() {
        if(hasConstruct())
            return materiaTypesSorted;

        return new ArrayList<>();
    }

    public int getPowerUsageSetting() {
        return this.powerLevel;
    }

    public void incrementPowerUsageSetting() {
        this.powerLevel = Math.min(this.powerLevel + 1, 5);
        this.setChanged();
    }

    public void decrementPowerUsageSetting() {
        this.powerLevel = Math.max(this.powerLevel - 1, 0);
        this.setChanged();
    }

    public void setPowerUsageSetting(int pNewSetting) {
        this.powerLevel = pNewSetting;
        this.setChanged();
    }

    private static final float[] POWER_LEVEL_SCALARS = {0.5f, 0.6f, 0.7f, 0.8f, 0.9f};
    public int getEnergyConsumptionRate() {
        int drain = BASE_ENERGY_DRAIN;
        for(int i=0; i<this.powerLevel; i++) {
            float scalar = (POWER_LEVEL_SCALARS[i] * 0.5f) + 1f;
            drain = Math.round((float)drain * scalar);
            drain = Math.round((float)drain / 100f);
            drain *= 100;
        }
        return drain;
    }

    @Override
    public int getStorageLimit(MateriaItem pMateriaType) {
        return pMateriaType instanceof EssentiaItem ? getEssentiaStorageLimit() : getAdmixtureStorageLimit();
    }

    public int getEssentiaStorageLimit() {
        int limit = ServerConfig.materiaVesselEssentiaCapacity;

        for(int i=0; i<this.powerLevel; i++) {
            float scalar = (POWER_LEVEL_SCALARS[i]) + 1f;
            limit = Math.round((float)limit * scalar);
            limit = Math.round((float)limit / 100f);
            limit *= 100;
        }
        return limit;
    }

    public int getAdmixtureStorageLimit() {
        int limit = getEssentiaStorageLimit();
        limit = Math.round(limit * ((float)ServerConfig.materiaVesselAdmixtureCapacity / (float)ServerConfig.materiaVesselEssentiaCapacity));

        return limit;
    }

    public boolean hasSufficientPower() {
        return hasSufficientPower;
    }

    public MateriaItem getActiveMateriaType() {
        return activeMateriaType;
    }

    public void setActiveMateriaType(MateriaItem pSelection) {
        activeMateriaType = pSelection;
    }

    @Override
    public int fill(MateriaItem pMateriaType, int pAmount, boolean pVoidExcess) {
        if(hasSufficientPower)
            return super.fill(pMateriaType, pAmount, pVoidExcess);

        return pAmount;
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(hasSufficientPower)
            return super.canAcceptStackFromShlorp(pStack);

        return 0;
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(hasSufficientPower)
            return super.insertStackFromShlorp(pStack);

        return pStack.getCount();
    }
}
