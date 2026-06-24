package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.block.GrandCircleFabricationBlock;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractFabricationBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandCircleFabricationRouterBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.gui.GrandCircleFabricationMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.item.PhilosophersStoneItem;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.recipe.FluidDistillationFabricationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.aranaira.magichem.util.InventoryHelper;
import com.aranaira.magichem.util.render.ColorUtils;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static com.aranaira.magichem.util.render.ColorUtils.SIX_STEP_PARTICLE_COLORS;

public class GrandCircleFabricationBlockEntity extends AbstractFabricationBlockEntity implements MenuProvider, Consumer<FriendlyByteBuf>, IShlorpReceiver, IMateriaProvisionRequester, IMateriaSortingRequester, IRequiresRouterCleanupOnDestruction, IHasDeviceRecipeSlot, IKeepsInventoryOnBreak {
    public static final int
            SLOT_COUNT = 22,
            SLOT_BOTTLES = 0, SLOT_WISDOM = 21,
            SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 10,
            SLOT_OUTPUT_START = 11, SLOT_OUTPUT_COUNT = 10;
    public static final float
            CIRCLE_FILL_RATE = 0.025f, PARTICLE_PERCENT_RATE = 0.05f, PROJECTOR_PERCENT_RATE = 0.05f;

    private static final int[] POWER_DRAW = { //TODO: Convert this to config
            50, 60, 75, 95, 115, 145, 180, 220, 280, 350,
            435, 545, 680, 845, 1055, 1320, 1650, 2065, 2580, 3220,
            4030, 5035, 6295, 7865, 9830, 12290, 15360, 19200, 24000, 30000
    };

    private static final float[] WISDOM_REDUCTION = { //TODO: Convert this to config
            1.0f, 0.920f, 0.815f, 0.676f, 0.500f, 0.250f
    };

    private static final int[] OPERATION_TICKS = { //TODO: Convert this to config
            1232, 1005, 820, 669, 546, 445, 363, 296, 241, 196,
            160, 130, 106, 86, 70, 57, 46, 37, 30, 24,
            19, 15, 12, 9, 7, 5, 4, 3, 2, 1
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();
    private static final Random r = new Random();

    private int
            powerUsageSetting = 1, materiaToVent = 0;
    private boolean
            redstonePaused = false;

    public float
            particlePercent = 0, daisCirclePercent = 0, projectorPercent = 0, mainCirclePercent = 0, itemLerp = 0;
    public boolean
            forceDisplayedRecipeUpdate = false;

    public GrandCircleFabricationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.GRAND_CIRCLE_FABRICATION_BE.get(), pos, state);

        itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    if(currentItemRecipe != null) {
                        if(((slot - SLOT_INPUT_START) / 2) >= currentItemRecipe.getComponentMateria().size())
                            return false;
                        ItemStack component = currentItemRecipe.getComponentMateria().get((slot - SLOT_INPUT_START) / 2);
                        return stack.getItem() == component.getItem();
                    } else if(currentFluidRecipe != null) {
                        if(((slot - SLOT_INPUT_START) / 2) >= currentFluidRecipe.getComponentMateria().size())
                            return false;
                        ItemStack component = currentFluidRecipe.getComponentMateria().get((slot - SLOT_INPUT_START) / 2);
                        return stack.getItem() == component.getItem();
                    } else {
                        return false;
                    }
                }
                if(slot == SLOT_WISDOM) {
                    return false;
                }

                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    if (InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(slot)))
                        return ItemStack.EMPTY;
                } else if(slot == SLOT_WISDOM) {
                    return ItemStack.EMPTY;
                }

                return super.extractItem(slot, amount, simulate);
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if(slot == SLOT_WISDOM) {
                    setStackInSlot(slot, stack);
                    return ItemStack.EMPTY;
                }

                return super.insertItem(slot, stack, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                DistillationFabricationRecipe recipePre = currentItemRecipe;
                if(slot == SLOT_WISDOM) {
                    forceDisplayedRecipeUpdate = true;
                }
            }
        };
    }

    @Nullable
    public Recipe<SimpleContainer> getCurrentRecipe() {
        return currentItemRecipe == null ? currentFluidRecipe : currentItemRecipe;
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
        else if(cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        else if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> this);
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
        linkPlugins();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putInt("materiaToVent", this.materiaToVent);
        this.materiaToVent = 0;
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putInt("batchSize", this.batchSize);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        nbt.putBoolean("redstonePaused", this.redstonePaused);
        nbt.putBoolean("isFESatisfied", this.isFESatisfied);
        nbt.putBoolean("clearRecipeAfterNextProcess", this.clearRecipeAfterNextProcess);

        if(!outputTank.isEmpty()) {
            CompoundTag inputTankTag = new CompoundTag();
            inputTankTag.putString("fluid", ForgeRegistries.FLUIDS.getKey(outputTank.getFluid()).toString());
            inputTankTag.putInt("amount",outputTank.getAmount());
            nbt.put("outputTank",inputTankTag);
        }

        if(currentItemRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.ITEMS.getKey(currentItemRecipe.getAlchemyObject().getItem());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
            nbt.putBoolean("recipeIsFluid", false);
        }
        else if(currentFluidRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.FLUIDS.getKey(currentFluidRecipe.getAlchemyFluid().getFluid());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
            nbt.putBoolean("recipeIsFluid", true);
        }

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if(nbt.contains("materiaToVent"))
            ventMateria(nbt.getInt("materiaToVent"));
        unpackDataFromNBT(nbt);
        progress = nbt.getInt("craftingProgress");
        powerUsageSetting = nbt.getInt("powerUsageSetting");
        batchSize = nbt.getInt("batchSize");
        ENERGY_STORAGE.setEnergy(nbt.getInt("storedPower"));
        redstonePaused = nbt.getBoolean("redstonePaused");
        isFESatisfied = nbt.getBoolean("isFESatisfied");
        clearRecipeAfterNextProcess = nbt.getBoolean("clearRecipeAfterNextProcess");

        if(nbt.contains("outputTank")) {
            CompoundTag inputTankTag = nbt.getCompound("outputTank");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputTankTag.getString("fluid")));
            if(fluid != null)
                outputTank = new FluidStack(fluid, inputTankTag.getInt("amount"));
        } else {
            outputTank = FluidStack.EMPTY;
        }

        if(nbt.contains("recipe")) {
            deferredRecipeQuery = new ResourceLocation(nbt.getString("recipe"));
            deferredRecipeIsFluid = nbt.getBoolean("recipeIsFluid");
        }
        else
            deferredRecipeQuery = null;
        doDeferredRecipeCheck = true;

        if(getLevel() != null)
            getCurrentRecipe();
    }

    public static int getScaledProgress(GrandCircleFabricationBlockEntity entity) {
        return Math.min(entity.getCraftingProgress() * 28 / entity.getOperationTicks(), 28);
    }

    public int getCraftingProgress(){
        return progress;
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, GrandCircleFabricationBlockEntity pEntity) {
        if(pEntity.doDeferredRecipeCheck) {
            if(pEntity.deferredRecipeIsFluid) {
                Fluid fluidQuery = ForgeRegistries.FLUIDS.getValue(pEntity.deferredRecipeQuery);
                if(fluidQuery != null) {
                    pEntity.currentFluidRecipe = pEntity.getRecipeForFluid(fluidQuery);
                    pEntity.currentItemRecipe = null;
                }
            } else {
                Item itemQuery = ForgeRegistries.ITEMS.getValue(pEntity.deferredRecipeQuery);
                if (itemQuery != null) {
                    pEntity.currentItemRecipe = pEntity.getRecipeForItem(itemQuery);
                    pEntity.currentFluidRecipe = null;
                }
            }
            pEntity.doDeferredRecipeCheck = false;
        }

        boolean wasFESatisfied = pEntity.isFESatisfied;
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
            if(pEntity.progress > 0 && pEntity.currentItemRecipe != null && pEntity.mainCirclePercent > 0.9) {
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
                                        .setScale(0.05f).setMaxAge(10 + r.nextInt(10)).setStack(pEntity.currentItemRecipe.getAlchemyObject())
                                        .setMover(new ParticleLerpMover(
                                                start.x, start.y, start.z,
                                                end.x, end.y, end.z)),
                                start.x, start.y, start.z,
                                0, 0, 0);
                    }
                }

                //Materia Gas
                if(pLevel.getGameTime() % 8 == 0){
                    final ItemStack[] contentsOfInputSlots = pEntity.getContentsOfInputSlotsAsArray();
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

        if(wasFESatisfied != pEntity.isFESatisfied) {
            changed = true;
        }

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

        if(daisCirclePercent > 0.9f) {
            itemLerp = Math.min(1, itemLerp + PARTICLE_PERCENT_RATE);
        } else {
            itemLerp = Math.max(0, itemLerp - PARTICLE_PERCENT_RATE);
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
        nbt.putInt("materiaToVent", this.materiaToVent);
        this.materiaToVent = 0;
        nbt.put("inventory", this.itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putInt("batchSize", this.batchSize);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        nbt.putBoolean("redstonePaused", this.redstonePaused);
        nbt.putBoolean("isFESatisfied", this.isFESatisfied);
        nbt.putBoolean("clearRecipeAfterNextProcess", this.clearRecipeAfterNextProcess);

        if(!outputTank.isEmpty()) {
            CompoundTag inputTankTag = new CompoundTag();
            inputTankTag.putString("fluid", ForgeRegistries.FLUIDS.getKey(outputTank.getFluid()).toString());
            inputTankTag.putInt("amount",outputTank.getAmount());
            nbt.put("outputTank",inputTankTag);
        }

        if(currentItemRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.ITEMS.getKey(currentItemRecipe.getAlchemyObject().getItem());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
            nbt.putBoolean("recipeIsFluid", false);
        }
        else if(currentFluidRecipe != null) {
            ResourceLocation keyQuery = ForgeRegistries.FLUIDS.getKey(currentFluidRecipe.getAlchemyFluid().getFluid());
            if(keyQuery != null)
                nbt.putString("recipe", keyQuery.toString());
            nbt.putBoolean("recipeIsFluid", true);
        }

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

    public boolean isRedstonePaused() {
        return redstonePaused;
    }

    public void checkPaused() {
        boolean shouldPause = false;
        BlockPos myPos = getBlockPos();

        for (Triplet<BlockPos, Integer, DevicePlugDirection> query : GrandCircleFabricationBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            for(Direction dir : Direction.values()) {
                BlockPos posQuery = myPos.offset(query.getFirst()).offset(dir.getNormal());
                BlockState stateQuery = getLevel().getBlockState(posQuery);
                if(stateQuery.getBlock() != BlockRegistry.GRAND_CIRCLE_FABRICATION.get() && stateQuery.getBlock() != BlockRegistry.GRAND_CIRCLE_FABRICATION_ROUTER.get()) {
                    int signal = getLevel().getBlockState(posQuery).getSignal(getLevel(), posQuery, dir);
                    if (signal > 0) {
                        shouldPause = true;
                        break;
                    }
                }
            }
            if(shouldPause) break;
        }

        setPaused(shouldPause);
    }

    public void setPaused(boolean pNewPauseState) {
        redstonePaused = pNewPauseState;
        for (AbstractDirectionalPluginBlockEntity pluginDevice : pluginDevices) {
            pluginDevice.setDevicePaused(pNewPauseState);
        }
        syncAndSave();
    }

    public int getPowerUsageSetting() {
        return powerUsageSetting;
    }

    public int getPowerDraw() {
        float baseRate = POWER_DRAW[MathUtils.clamp(powerUsageSetting, 1, 30)-1];
        float stoneReduction = 1.0f;
        if(getStoneItem().getItem() instanceof PhilosophersStoneItem pi) {
            stoneReduction = WISDOM_REDUCTION[pi.getWisdom()];
        }
        return Math.round(baseRate * stoneReduction);
    }

    public int getOperationTicks() {
        float batchModifier = 1f;
        if(getCurrentRecipe() instanceof DistillationFabricationRecipe item) {
            if (item.getBatchSize() > 0) {
                batchModifier = (float) batchSize / (float) item.getBatchSize();
            }
        } else if(getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluid) {
            if (fluid.getBatchSize() > 0) {
                batchModifier = (float) batchSize / (float) fluid.getBatchSize();
            }
        }
        return Math.max(1,Math.round((float)OPERATION_TICKS[MathUtils.clamp(powerUsageSetting, 1, 30)-1] * batchModifier));
    }

    public int setPowerUsageSetting(int pPowerUsageSetting) {
        this.powerUsageSetting = pPowerUsageSetting;
        this.resetProgress();
        if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * ServerConfig.circlePowerBuffer)
            ENERGY_STORAGE.setEnergy(getPowerDraw() * ServerConfig.circlePowerBuffer);
        return this.powerUsageSetting;
    }

    public int incrementPowerUsageSetting() {
        if(powerUsageSetting + 1 < 31) {
            this.powerUsageSetting++;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * ServerConfig.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * ServerConfig.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    public int decrementPowerUsageSetting() {
        if(powerUsageSetting - 1 > 0) {
            this.powerUsageSetting--;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * ServerConfig.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * ServerConfig.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    private static final AABB validVentingParticleZone = new AABB(0.375, 0.9375, 0.375, 0.625, 0.9375, 0.625);
    private void ventMateria(int pPackedSlots) {
        for (int i = 0; i < SLOT_INPUT_COUNT; i++) {
            if((pPackedSlots & (1 << i)) >> i == 1) {
                ItemStack stackToCheck = itemHandler.getStackInSlot(SLOT_INPUT_START + i);

                if (stackToCheck.getItem() instanceof MateriaItem mi) {
                    InventoryHelper.generateMateriaVentingCloud(level, mi, validVentingParticleZone.move(getBlockPos()), 0.0625);
                }
            }
        }

        materiaToVent = 0;
    }

    public void setCurrentRecipe(Item pQuery) {
        if(!level.isClientSide()) {
            DistillationFabricationRecipe previousItemRecipe = currentItemRecipe;
            currentItemRecipe = DistillationFabricationRecipe.getFabricatingRecipe(level, pQuery);
            currentFluidRecipe = null;
            clearRecipeAfterNextProcess = false;
            batchSize = 1;

            if (currentItemRecipe != null && (previousItemRecipe == null || previousItemRecipe.getAlchemyObject().getItem() != pQuery)) {
                ItemStack[] componentMateria = new ItemStack[5];
                currentItemRecipe.getComponentMateria().toArray(componentMateria);

                for(int i=0; i<SLOT_INPUT_COUNT; i++) {
                    if(componentMateria[i/2] != null) {
                        if (componentMateria[i / 2].getItem() instanceof MateriaItem mi) {
                            ItemStack query = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
                            if(InventoryHelper.hasCustomModelData(query) && query.getItem() != componentMateria[i/2].getItem()) {
                                materiaToVent = materiaToVent | (1 << i);
                                itemHandler.setStackInSlot(SLOT_INPUT_START + i, ItemStack.EMPTY.copy());
                                continue;
                            }
                        }
                    } else {
                        ItemStack query = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
                        if(InventoryHelper.hasCustomModelData(query) && query.getItem() != componentMateria[i/2].getItem()) {
                            materiaToVent = materiaToVent | (1 << i);
                            itemHandler.setStackInSlot(SLOT_INPUT_START + i, ItemStack.EMPTY.copy());
                            continue;
                        }
                    }
                    materiaToVent = materiaToVent & ~(1 << i);
                }
            }
        }
        else {
            currentItemRecipe = DistillationFabricationRecipe.getFabricatingRecipe(level, pQuery);
            currentFluidRecipe = null;
            clearRecipeAfterNextProcess = false;
        }
        doDeferredRecipeCheck = false;

        syncAndSave();
    }

    public void setCurrentRecipe(Fluid pQuery) {
        if(!level.isClientSide()) {
            FluidDistillationFabricationRecipe previousFluidRecipe = currentFluidRecipe;
            currentFluidRecipe = getRecipeForFluid(pQuery);
            currentItemRecipe = null;
            clearRecipeAfterNextProcess = false;
            batchSize = 1;

            if (currentFluidRecipe != null && (previousFluidRecipe == null || previousFluidRecipe.getAlchemyFluid().getFluid() != pQuery)) {
                ItemStack[] componentMateria = new ItemStack[5];
                currentFluidRecipe.getComponentMateria().toArray(componentMateria);

                for(int i=0; i<SLOT_INPUT_COUNT; i++) {
                    if(componentMateria[i/2] != null) {
                        if (componentMateria[i / 2].getItem() instanceof MateriaItem mi) {
                            ItemStack query = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
                            if(InventoryHelper.hasCustomModelData(query) && query.getItem() != componentMateria[i/2].getItem()) {
                                materiaToVent = materiaToVent | (1 << i);
                                itemHandler.setStackInSlot(SLOT_INPUT_START + i, ItemStack.EMPTY.copy());
                                continue;
                            }
                        }
                    } else {
                        ItemStack query = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
                        if(InventoryHelper.hasCustomModelData(query) && query.getItem() != componentMateria[i/2].getItem()) {
                            materiaToVent = materiaToVent | (1 << i);
                            itemHandler.setStackInSlot(SLOT_INPUT_START + i, ItemStack.EMPTY.copy());
                            continue;
                        }
                    }
                    materiaToVent = materiaToVent & ~(1 << i);
                }
            }
        }
        else {
            currentFluidRecipe = getRecipeForFluid(pQuery);
            currentItemRecipe = null;
            clearRecipeAfterNextProcess = false;
        }
        doDeferredRecipeCheck = false;

        syncAndSave();
    }

    public ItemStack[] getContentsOfInputSlotsAsArray() {
        ItemStack[] out = new ItemStack[10];
        for(int i=SLOT_INPUT_START; i<SLOT_INPUT_START+SLOT_INPUT_COUNT; i++) {
            out[i-SLOT_INPUT_START] = itemHandler.getStackInSlot(i);
        }
        return out;
    }

    @Override
    public SimpleContainer getContentsOfInputSlots() {
        return super.getContentsOfInputSlots(GrandCircleFabricationBlockEntity::getVar);
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

            int powerToLimit = Math.max(0, (getPowerDraw() * ServerConfig.circlePowerBuffer) - getEnergyStored());
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

    public void setWisdomStone(ItemStack pStone) {
        itemHandler.setStackInSlot(SLOT_WISDOM, pStone.copy());
        syncAndSave();
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
            case SLOT_STONE -> SLOT_WISDOM;

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
        if((currentItemRecipe == null && currentFluidRecipe == null) || !isFESatisfied || redstonePaused)
            return false;

        return getProvisioningNeeds().size() > 0;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        if(currentItemRecipe != null) {
            for (ItemStack recipeMateria : currentItemRecipe.getComponentMateria()) {
                if(activeProvisionRequests.contains((MateriaItem)recipeMateria.getItem()))
                    continue;

                int amountToAdd = recipeMateria.getCount() * batchSize;
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
        else if(currentFluidRecipe != null) {
            for (ItemStack recipeMateria : currentFluidRecipe.getComponentMateria()) {
                if(activeProvisionRequests.contains((MateriaItem)recipeMateria.getItem()))
                    continue;

                int amountToAdd = recipeMateria.getCount() * batchSize;
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

    @Override
    public void packDataToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.GRAND_CIRCLE_FABRICATION.get());

        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("powerUsageSetting", powerUsageSetting);

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {
        if(pNBT.contains("inventory")){
            CompoundTag pInventoryTag = pNBT.getCompound("inventory");
            int size = pInventoryTag.getInt("Size");
            if (size == SLOT_COUNT) {
                itemHandler.deserializeNBT(pInventoryTag);
            } else if (getLevel() != null && getLevel().isClientSide()) {
                final LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    MutableComponent msg = Component.translatable("feedback.warning.inventory_size_mismatch.part1")
                            .append(Component.translatable("block.magichem.grand_circle_fabrication").withStyle(ChatFormatting.GOLD))
                            .append(Component.translatable("feedback.warning.inventory_size_mismatch.part2"));
                    player.displayClientMessage(msg, false);
                }
            }
        }
        if (pNBT.contains("powerUsageSetting")) {
            setPowerUsageSetting(pNBT.getInt("powerUsageSetting"));
        }
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();

        List<BlockEntity> query = new ArrayList<>();
        for (Triplet<BlockPos, Integer, DevicePlugDirection> posAndType : GrandCircleFabricationBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof GrandCircleFabricationRouterBlockEntity gcfrbe) {
                BlockEntity pe = gcfrbe.getPlugEntity();
                if(pe instanceof AbstractDirectionalPluginBlockEntity dpbe) {
                    final ICanTakePlugins targetMachine = dpbe.getTargetMachine();
                    if(targetMachine instanceof GrandCircleFabricationRouterBlockEntity router) {
                        GrandCircleFabricationBlockEntity master = router.getMaster();
                        if(master == this) pluginDevices.add(dpbe);
                    } else if (targetMachine == this) {
                        pluginDevices.add(dpbe);
                    }
                }
            }
        }
    }

    @Override
    public List<AbstractDirectionalPluginBlockEntity> getPlugins() {
        return pluginDevices;
    }

    @Override
    public byte setRecipe(ItemStack pStack, Player player) {
        final DistillationFabricationRecipe distillationRecipeQuery = DistillationFabricationRecipe.getDistillingRecipe(getLevel(), pStack);
        if(distillationRecipeQuery == null || distillationRecipeQuery.getWisdom() == 6)
            return ERROR_CODE_NO_SUCH_RECIPE;

        if(distillationRecipeQuery.getWisdom() > getCurrentWisdom(GrandCircleFabricationBlockEntity::getVar))
            return ERROR_CODE_INSUFFICIENT_WISDOM;

        if(distillationRecipeQuery.isAdvancementRequired() || distillationRecipeQuery.isForbiddenByAdvancement()) {
            if(player instanceof ServerPlayer sp) {
                final PlayerAdvancements playerAdvancements = sp.getAdvancements();
                final ServerAdvancementManager serverAdvancementManager = sp.getServer().getAdvancements();

                final Advancement requiredAdvancement = serverAdvancementManager.getAdvancement(distillationRecipeQuery.getRequiredAdvancement());
                final Advancement forbiddenAdvancement = serverAdvancementManager.getAdvancement(distillationRecipeQuery.getForbiddenAdvancement());

                if(distillationRecipeQuery.isAdvancementRequired() && requiredAdvancement != null) {
                    if(!playerAdvancements.getOrStartProgress(requiredAdvancement).isDone()) return ERROR_CODE_REQUIRED_ADVANCEMENT_MISSING;
                }
                if(distillationRecipeQuery.isForbiddenByAdvancement() && forbiddenAdvancement != null) {
                    if(playerAdvancements.getOrStartProgress(forbiddenAdvancement).isDone()) return ERROR_CODE_FORBIDDEN_ADVANCEMENT_PRESENT;
                }
            }
        }

        setCurrentRecipe(pStack.getItem());
        return ERROR_CODE_SUCCESS;
    }

    public ItemStack getStoneItem() {
        return itemHandler.getStackInSlot(SLOT_WISDOM);
    }

    public ItemStack getStoneItem(boolean pMakeCopy) {
        return pMakeCopy ? itemHandler.getStackInSlot(SLOT_WISDOM).copy() : itemHandler.getStackInSlot(SLOT_WISDOM);
    }

    @Override
    public boolean needsSorting() {
        if(currentItemRecipe != null || currentFluidRecipe != null) return false;

        return !getContentsOfInputSlots(CircleFabricationBlockEntity::getVar).isEmpty();
    }

    @Override
    public int getTankCapacity(int tank) {
        return ServerConfig.grandCircleFabricationTankCapacity;
    }

    @Override
    public ItemStack tryExtractUnbottled(ItemStack pBottlesInHand) {
        int limit = pBottlesInHand.getCount();
        ItemStack extractQuery = null;

        for(int i=SLOT_INPUT_START;i<SLOT_INPUT_START+SLOT_INPUT_COUNT;i++) {
            if(!itemHandler.getStackInSlot(i).isEmpty() && InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(i))) {
                extractQuery = itemHandler.getStackInSlot(i);
                break;
            }
        }

        if(extractQuery != null) {
            int extracted = Math.min(limit, extractQuery.getCount());
            pBottlesInHand.shrink(extracted);
            ItemStack output = new ItemStack(extractQuery.getItem(), extracted);
            extractQuery.shrink(extracted);
            return output;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean isClogged() {
        if (currentItemRecipe == null && currentFluidRecipe == null) {
            for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
                if (!itemHandler.getStackInSlot(i).isEmpty() && InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
