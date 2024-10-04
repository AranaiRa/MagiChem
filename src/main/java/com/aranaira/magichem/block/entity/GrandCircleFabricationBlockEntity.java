package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.GrandCircleFabricationBlock;
import com.aranaira.magichem.block.entity.ext.AbstractFabricationBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IRequiresRouterCleanupOnDestruction;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.gui.GrandCircleFabricationMenu;
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
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static com.aranaira.magichem.util.render.ColorUtils.SIX_STEP_PARTICLE_COLORS;

public class GrandCircleFabricationBlockEntity extends AbstractFabricationBlockEntity implements MenuProvider, Consumer<FriendlyByteBuf>, IShlorpReceiver, IMateriaProvisionRequester, IRequiresRouterCleanupOnDestruction {
    public static final int
            SLOT_COUNT = 22,
            SLOT_BOTTLES = 0, SLOT_RECIPE = 21,
            SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 10,
            SLOT_OUTPUT_START = 11, SLOT_OUTPUT_COUNT = 10;
    public static final float
            CIRCLE_FILL_RATE = 0.025f, PARTICLE_PERCENT_RATE = 0.05f, PROJECTOR_PERCENT_RATE = 0.05f;

    private static final int[] POWER_DRAW = { //TODO: Convert this to config
            70, 85, 105, 130, 160, 200, 250, 310, 390, 490,
            610, 760, 950, 1185, 1480, 1850, 2310, 2890, 3610, 4510,
            5640, 7050, 8810, 11010, 13765, 17205, 21505, 26880, 33600, 42000
    };

    private static final int[] OPERATION_TICKS = { //TODO: Convert this to config
            1232, 1005, 820, 669, 546, 445, 363, 296, 241, 196,
            160, 130, 106, 86, 70, 57, 46, 37, 30, 24,
            19, 15, 12, 9, 7, 5, 4, 3, 2, 1
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();
    private static final Random r = new Random();

    private int
            powerUsageSetting = 1;
    private boolean
            redstonePaused = false;

    public float
            particlePercent = 0, daisCirclePercent = 0, projectorPercent = 0, mainCirclePercent = 0;

    public GrandCircleFabricationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.GRAND_CIRCLE_FABRICATION_BE.get(), pos, state);

        itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
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
        return new GrandCircleFabricationMenu(id, inventory, this);
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
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        nbt.putBoolean("redstonePaused", this.redstonePaused);
        nbt.putBoolean("isFESatisfied", this.isFESatisfied);
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
        powerUsageSetting = nbt.getInt("powerUsageSetting");
        ENERGY_STORAGE.setEnergy(nbt.getInt("storedPower"));
        redstonePaused = nbt.getBoolean("redstonePaused");
        isFESatisfied = nbt.getBoolean("isFESatisfied");

        if(getLevel() != null)
            getCurrentRecipe();
    }

    public static int getScaledProgress(GrandCircleFabricationBlockEntity entity) {
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

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, GrandCircleFabricationBlockEntity pEntity) {
        if(!pLevel.isClientSide() && !pEntity.redstonePaused) {
            //Power check
            if(pEntity.operationTicks > 0) {
                int cost = pEntity.getPowerDraw();
                if (pEntity.ENERGY_STORAGE.getEnergyStored() >= cost) {
                    pEntity.ENERGY_STORAGE.extractEnergy(cost, false);
                    pEntity.isFESatisfied = true;
                } else {
                    pEntity.isFESatisfied = false;
                }
            } else {
                pEntity.isFESatisfied = true;
            }
        }
        pEntity.handleAnimationDrivers();

        //particle stuff
        if(pLevel.isClientSide()) {
            //Control Dais Orb
            if(pEntity.particlePercent > 0) {
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
                if (pLevel.getGameTime() % 8 == 0) {
                    pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(SIX_STEP_PARTICLE_COLORS[colorIndex][0], SIX_STEP_PARTICLE_COLORS[colorIndex][1], SIX_STEP_PARTICLE_COLORS[colorIndex][2])
                                    .setScale(0.4f * pEntity.particlePercent).setMaxAge(80),
                            center.x, center.y, center.z,
                            0, 0, 0);
                }
                pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setColor(255, 255, 255).setScale(0.2f * pEntity.particlePercent),
                        center.x, center.y, center.z,
                        0, 0, 0);

                if (pEntity.particlePercent == 1) {
                    for (int i = 0; i < 2; i++) {
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                        pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(SIX_STEP_PARTICLE_COLORS[colorIndex][0], SIX_STEP_PARTICLE_COLORS[colorIndex][1], SIX_STEP_PARTICLE_COLORS[colorIndex][2], 128)
                                        .setScale(0.09f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);

                        pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                        .setScale(0.015f).setMaxAge(16)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }
                }
            }

            //Crafting
            if(pEntity.progress > 0 && pEntity.recipe != null && pEntity.mainCirclePercent > 0.9) {
                Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                double mainCircleBob = Math.sin((((pLevel.getGameTime()) % 450d) / 450d) * (Math.PI * 2) * Math.PI * 2) * 0.03125 * 0.707;
                double itemBob = mainCircleBob + 0.1875;

                Vector3 left, right, inner;

                if(facing == Direction.NORTH || facing == Direction.SOUTH) {
                    left = new Vector3(-0.2734, 0.9766, 0.5);
                    right = new Vector3(1.2734, 0.9766, 0.5);
                    inner = facing == Direction.NORTH ? new Vector3(0.5, 1.697 + itemBob, 0.5 + itemBob) : new Vector3(0.5, 1.697 + itemBob, 0.5 - itemBob);
                } else {
                    left = new Vector3(0.5, 0.9766, -0.2734);
                    right = new Vector3(0.5, 0.9766, 1.2734);
                    inner = facing == Direction.EAST ? new Vector3(0.5 - itemBob, 1.697 + itemBob, 0.5) : new Vector3(0.5 + itemBob, 1.697 + itemBob, 0.5);
                }

                //Lightning
                if(pLevel.getGameTime() % 2 == 0 && pEntity.mainCirclePercent > 0.95){
                    pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                    .setMaxAge(8 + r.nextInt(6)).setScale(20),
                            pPos.getX() + inner.x, pPos.getY() + inner.y, pPos.getZ() + inner.z,
                            pPos.getX() + left.x, pPos.getY() + left.y, pPos.getZ() + left.z);

                    pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                    .setMaxAge(8 + r.nextInt(6)),
                            pPos.getX() + inner.x, pPos.getY() + inner.y, pPos.getZ() + inner.z,
                            pPos.getX() + right.x, pPos.getY() + right.y, pPos.getZ() + right.z);
                }

                //Item Chunkies
                {
                    int total = 3;
                    for (int i = 0; i < total; i++) {
                        Vector3 end = new Vector3(pPos.getX() + inner.x, pPos.getY() + inner.y, pPos.getZ() + inner.z);
                        Vector3 start = end.add(new Vector3(r.nextDouble() * 2 - 1, r.nextDouble() * 2 - 1, r.nextDouble() * 2 - 1).scale(0.5f));

                        pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.ITEM.get())
                                        .setScale(0.05f).setMaxAge(10 + r.nextInt(10)).setStack(pEntity.recipe.getAlchemyObject())
                                        .setMover(new ParticleLerpMover(
                                                start.x, start.y, start.z,
                                                end.x, end.y, end.z)),
                                start.x, start.y, start.z,
                                0, 0, 0);
                    }
                }

                //Materia Gas
                if(pLevel.getGameTime() % 8 == 0){
                    final ItemStack[] contentsOfInputSlots = pEntity.getContentsOfInputSlots();
                    boolean has1 = !contentsOfInputSlots[0].isEmpty() || !contentsOfInputSlots[1].isEmpty();
                    boolean has2 = !contentsOfInputSlots[2].isEmpty() || !contentsOfInputSlots[3].isEmpty();
                    boolean has3 = !contentsOfInputSlots[4].isEmpty() || !contentsOfInputSlots[5].isEmpty();
                    boolean has4 = !contentsOfInputSlots[6].isEmpty() || !contentsOfInputSlots[7].isEmpty();
                    boolean has5 = !contentsOfInputSlots[8].isEmpty() || !contentsOfInputSlots[9].isEmpty();
                    List<Pair<Vector3, Integer>> cloudData = new ArrayList<>();

                    if (has1) {
                        int color = contentsOfInputSlots[0].isEmpty() ?
                                ((MateriaItem)contentsOfInputSlots[1].getItem()).getMateriaColor() :
                                ((MateriaItem)contentsOfInputSlots[0].getItem()).getMateriaColor();

                        if(facing == Direction.NORTH)
                            cloudData.add(new Pair<>(new Vector3(0.5, 2.625, -0.625), color));
                        else if(facing == Direction.EAST)
                            cloudData.add(new Pair<>(new Vector3(1.625, 2.625, 0.5), color));
                        else if(facing == Direction.SOUTH)
                            cloudData.add(new Pair<>(new Vector3(0.5, 2.625, 1.625), color));
                        else if(facing == Direction.WEST)
                            cloudData.add(new Pair<>(new Vector3(-0.625, 2.625, 0.5), color));
                    }

                    if (has2) {
                        int color = contentsOfInputSlots[2].isEmpty() ?
                                ((MateriaItem)contentsOfInputSlots[3].getItem()).getMateriaColor() :
                                ((MateriaItem)contentsOfInputSlots[2].getItem()).getMateriaColor();

                        if(facing == Direction.NORTH)
                            cloudData.add(new Pair<>(new Vector3(-0.295495, 2.375, -0.295495), color));
                        else if(facing == Direction.EAST)
                            cloudData.add(new Pair<>(new Vector3(1.2955, 2.375, 1.2955), color));
                        else if(facing == Direction.SOUTH)
                            cloudData.add(new Pair<>(new Vector3(1.295495, 2.375, 1.2955), color));
                        else if(facing == Direction.WEST)
                            cloudData.add(new Pair<>(new Vector3(-0.2955, 2.375, 1.2955), color));
                    }

                    if (has3) {
                        int color = contentsOfInputSlots[4].isEmpty() ?
                                ((MateriaItem)contentsOfInputSlots[5].getItem()).getMateriaColor() :
                                ((MateriaItem)contentsOfInputSlots[4].getItem()).getMateriaColor();

                        if(facing == Direction.NORTH)
                            cloudData.add(new Pair<>(new Vector3(1.295495, 2.375, -0.295495), color));
                        else if(facing == Direction.EAST)
                            cloudData.add(new Pair<>(new Vector3(1.2955, 2.375, -0.2955), color));
                        else if(facing == Direction.SOUTH)
                            cloudData.add(new Pair<>(new Vector3(-0.295495, 2.375, 1.295495), color));
                        else if(facing == Direction.WEST)
                            cloudData.add(new Pair<>(new Vector3(-0.2955, 2.375, -0.2955), color));
                    }

                    if (has4) {
                        int color = contentsOfInputSlots[6].isEmpty() ?
                                ((MateriaItem)contentsOfInputSlots[7].getItem()).getMateriaColor() :
                                ((MateriaItem)contentsOfInputSlots[6].getItem()).getMateriaColor();

                        if(facing == Direction.NORTH)
                            cloudData.add(new Pair<>(new Vector3(-0.625, 1.75, 0.5), color));
                        else if(facing == Direction.EAST)
                            cloudData.add(new Pair<>(new Vector3(0.5, 1.75, -0.625), color));
                        else if(facing == Direction.SOUTH)
                            cloudData.add(new Pair<>(new Vector3(1.625, 1.75, 0.5), color));
                        else if(facing == Direction.WEST)
                            cloudData.add(new Pair<>(new Vector3(0.5, 1.75, 1.625), color));
                    }

                    if (has5) {
                        int color = contentsOfInputSlots[8].isEmpty() ?
                                ((MateriaItem)contentsOfInputSlots[9].getItem()).getMateriaColor() :
                                ((MateriaItem)contentsOfInputSlots[8].getItem()).getMateriaColor();

                        if(facing == Direction.NORTH)
                            cloudData.add(new Pair<>(new Vector3(1.625, 1.75, 0.5), color));
                        else if(facing == Direction.EAST)
                            cloudData.add(new Pair<>(new Vector3(0.5, 1.75, 1.625), color));
                        else if(facing == Direction.SOUTH)
                            cloudData.add(new Pair<>(new Vector3(-0.625, 1.75, 0.5), color));
                        else if(facing == Direction.WEST)
                            cloudData.add(new Pair<>(new Vector3(0.5, 1.75, -0.625), color));
                    }

                    for (Pair<Vector3, Integer> cloudDatum : cloudData) {
                        int[] color = ColorUtils.getRGBAIntTintFromPackedInt(cloudDatum.getSecond());
                        Vector3 origin = new Vector3(pPos.getX(), pPos.getY(), pPos.getZ()).add(cloudDatum.getFirst());

                        double spreadRadius = 0.015625d;

                        double x = r.nextDouble() * spreadRadius * 2 - spreadRadius;
                        double z = r.nextDouble() * spreadRadius * 2 - spreadRadius;

                        Vector3 pos = origin.add(new Vector3(x, 0.325, z));

                        pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.DUST_LERP.get())
                                        .setScale(0.05f).setMaxAge(64)
                                        .setMover(new ParticleLerpMover(pos.x, pos.y, pos.z, pos.x, pos.y + 0.5, pos.z))
                                        .setColor(color[0], color[1], color[2], 128),
                                pos.x, pos.y, pos.z,
                                0, 0, 0);
                    }
                }

                //Circle Sparkles
                {
                    int total = 5;
                    for(int i=0; i<total; i++) {
                        double radianWiggle = ((Math.PI * 2) / (double)total) * (r.nextDouble() - 0.5);

                        double x = Math.cos((Math.PI * 2) * i / (double)total + radianWiggle);
                        double z = Math.sin((Math.PI * 2) * i / (double)total + radianWiggle);

                        double rx = r.nextDouble() - 0.5;
                        double ry = r.nextDouble();
                        double rz = r.nextDouble() - 0.5;

                        double yTilt = 0;

                        Vector3 mid = Vector3.zero();
                        if(facing == Direction.NORTH) {
                            yTilt = -z * 0.5;
                            mid = new Vector3(0.5, 1.697 + yTilt + mainCircleBob, 0.5 + mainCircleBob).add(new Vector3(x, 0, z * 0.675).scale(0.667f));
                        }
                        else if(facing == Direction.EAST) {
                            yTilt = x * 0.5;
                            mid = new Vector3(0.5 + mainCircleBob, 1.697 + yTilt + mainCircleBob, 0.5).add(new Vector3(x * 0.675, 0, z).scale(0.667f));
                        }
                        else if(facing == Direction.SOUTH) {
                            yTilt = z * 0.5;
                            mid = new Vector3(0.5, 1.697 + yTilt - mainCircleBob, 0.5 - mainCircleBob).add(new Vector3(x, 0, z * 0.675).scale(0.667f));
                        }
                        else if(facing == Direction.WEST) {
                            yTilt = -x * 0.5;
                            mid = new Vector3(0.5 - mainCircleBob, 1.697 + yTilt + mainCircleBob, 0.5).add(new Vector3(x * 0.675, 0, z).scale(0.667f));
                        }

                        pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setMaxAge(20 + r.nextInt(30)).setScale(0.03f + r.nextFloat() * 0.03f)
                                        .setColor(150+r.nextInt(75), 150+r.nextInt(75), 150+r.nextInt(75))
                                        .setMover(new ParticleVelocityMover(rx * 0.03, 0.01 + ry * 0.02, rz * 0.03, true)),
                                pPos.getX() + mid.x, pPos.getY() + mid.y, pPos.getZ() + mid.z,
                                0, 0, 0);
                    }
                }
            }
        }

        pEntity.operationTicks = pEntity.getOperationTicks();

        boolean changed = false;
        if(!pEntity.redstonePaused)
            changed = AbstractFabricationBlockEntity.tick(pLevel, pPos, pState, pEntity, GrandCircleFabricationBlockEntity::getVar);

        if(changed)
            pEntity.syncAndSave();
    }

    private void handleAnimationDrivers() {
        if(particlePercent == 1) {
            daisCirclePercent = Math.min(1, daisCirclePercent + CIRCLE_FILL_RATE);
        } else if(particlePercent == 0) {
            daisCirclePercent = Math.max(0, daisCirclePercent - CIRCLE_FILL_RATE);
        }

        if(hasSufficientPower() && !redstonePaused) {
            particlePercent = Math.min(1, particlePercent + PARTICLE_PERCENT_RATE);
        } else {
            particlePercent = Math.max(0, particlePercent - PARTICLE_PERCENT_RATE);
        }

        if(daisCirclePercent == 1) {
            projectorPercent = Math.min(1, projectorPercent + PROJECTOR_PERCENT_RATE);
        } else if(daisCirclePercent == 0) {
            projectorPercent = Math.max(0, projectorPercent - PROJECTOR_PERCENT_RATE);
        }

        if(projectorPercent > 0.5f) {
            mainCirclePercent = Math.min(1, mainCirclePercent + CIRCLE_FILL_RATE);
        } else if(projectorPercent == 0) {
            mainCirclePercent = Math.max(0, mainCirclePercent - CIRCLE_FILL_RATE);
        }
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
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        nbt.putBoolean("redstonePaused", this.redstonePaused);
        nbt.putBoolean("isFESatisfied", this.isFESatisfied);
        return nbt;
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @NotNull
    private static SimpleContainer getOutputAsContainer(GrandCircleFabricationBlockEntity entity) {
        SimpleContainer insert = new SimpleContainer(SLOT_OUTPUT_COUNT);
        int slotID = 0;
        //Add output item
        for (int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT; i++) {
            insert.setItem(slotID++, entity.itemHandler.getStackInSlot(i));
        }
        return insert;
    }

    public void setRedstonePaused(boolean pPaused) {
        redstonePaused = pPaused;
        syncAndSave();
    }

    public int getPowerUsageSetting() {
        return powerUsageSetting;
    }

    public int getPowerDraw() {
        return POWER_DRAW[MathUtils.clamp(powerUsageSetting, 1, 30)-1];
    }

    public int getOperationTicks() {
        return OPERATION_TICKS[MathUtils.clamp(powerUsageSetting, 1, 30)-1];
    }

    public int setPowerUsageSetting(int pPowerUsageSetting) {
        this.powerUsageSetting = pPowerUsageSetting;
        this.resetProgress();
        if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
            ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        return this.powerUsageSetting;
    }

    public int incrementPowerUsageSetting() {
        if(powerUsageSetting + 1 < 31) {
            this.powerUsageSetting++;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    public int decrementPowerUsageSetting() {
        if(powerUsageSetting - 1 > 0) {
            this.powerUsageSetting--;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    public void setCurrentRecipe(ItemStack pQuery) {
        itemHandler.setStackInSlot(SLOT_RECIPE, pQuery);

        syncAndSave();
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

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(Integer.MAX_VALUE, Integer.MAX_VALUE) {
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
    public GrandCircleFabricationBlockEntity readFrom(FriendlyByteBuf friendlyByteBuf){
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

    @Override
    public void destroyRouters() {
        GrandCircleFabricationBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3,4,3));
    }

    public void packInventoryToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.GRAND_CIRCLE_FABRICATION.get());

        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("powerUsageSetting", powerUsageSetting);

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        itemHandler.deserializeNBT(pInventoryTag);
    }
}
