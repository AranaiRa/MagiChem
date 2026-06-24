package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.block.CircleFabricationBlock;
import com.aranaira.magichem.block.entity.ext.AbstractFabricationBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.item.MateriaItem;
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
import com.mna.tools.math.Vector3;
import net.minecraft.ChatFormatting;
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

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class CircleFabricationBlockEntity extends AbstractFabricationBlockEntity implements MenuProvider, Consumer<FriendlyByteBuf>, IShlorpReceiver, IMateriaProvisionRequester, IMateriaSortingRequester, IRequiresRouterCleanupOnDestruction, IHasDeviceRecipeSlot {
    public static final int
            SLOT_COUNT = 21,
            SLOT_BOTTLES = 0,
            SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 10,
            SLOT_OUTPUT_START = 11, SLOT_OUTPUT_COUNT = 10;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();
    private BlockPos linkedCircleToil = null;
    private static final Random r = new Random();
    private int materiaToVent = 0;

    public CircleFabricationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_FABRICATION_BE.get(), pos, state);

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

                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
                    if (InventoryHelper.hasCustomModelData(itemHandler.getStackInSlot(slot)))
                        return ItemStack.EMPTY;
                }

                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
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
        return new CircleFabricationMenu(id, inventory, this);
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
        nbt.putInt("batchSize", this.batchSize);
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
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
        unpackInventoryFromNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        batchSize = nbt.getInt("batchSize");
        ENERGY_STORAGE.setEnergy(nbt.getInt("storedPower"));
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
        nbt.putInt("storedPower", this.ENERGY_STORAGE.getEnergyStored());
        nbt.putInt("batchSize", this.batchSize);
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

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        int size = pInventoryTag.getInt("Size");
        if(size == SLOT_COUNT) {
            itemHandler.deserializeNBT(pInventoryTag);
        } else if(getLevel() != null && getLevel().isClientSide()) {
            final LocalPlayer player = Minecraft.getInstance().player;
            if(player != null) {
                MutableComponent msg = Component.translatable("feedback.warning.inventory_size_mismatch.part1")
                        .append(Component.translatable("block.magichem.circle_fabrication").withStyle(ChatFormatting.GOLD))
                        .append(Component.translatable("feedback.warning.inventory_size_mismatch.part2"));
                player.displayClientMessage(msg, false);
            }
        }
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
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

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CircleFabricationBlockEntity pEntity) {
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

        if(!pLevel.isClientSide()) {
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

            //Try to find a circle of toil
            if(pEntity.linkedCircleToil == null) {
                //Only search once every 5 seconds to cut down on server load
                if(pLevel.getGameTime() % 100 == 0) {
                    final Direction facing = pState.getValue(FACING);
                    if(facing == Direction.NORTH) {
                        for(int z=-2; z>=-10; z--) {
                            BlockPos queryPos = pPos.offset(0, 0, z);
                            if(pLevel.getBlockState(queryPos).getBlock() == BlockRegistry.CIRCLE_TOIL.get()) {
                                pEntity.linkedCircleToil = queryPos;
                                break;
                            }
                        }
                    } else if(facing == Direction.EAST) {
                        for(int x=2; x<=8; x++) {
                            BlockPos queryPos = pPos.offset(x, 0, 0);
                            if(pLevel.getBlockState(queryPos).getBlock() == BlockRegistry.CIRCLE_TOIL.get()) {
                                pEntity.linkedCircleToil = queryPos;
                                break;
                            }
                        }
                    } else if(facing == Direction.SOUTH) {
                        for(int z=2; z<=8; z++) {
                            BlockPos queryPos = pPos.offset(0, 0, z);
                            if(pLevel.getBlockState(queryPos).getBlock() == BlockRegistry.CIRCLE_TOIL.get()) {
                                pEntity.linkedCircleToil = queryPos;
                                break;
                            }
                        }
                    } else if(facing == Direction.WEST) {
                        for(int x=-2; x>=-8; x--) {
                            BlockPos queryPos = pPos.offset(x, 0, 0);
                            if(pLevel.getBlockState(queryPos).getBlock() == BlockRegistry.CIRCLE_TOIL.get()) {
                                pEntity.linkedCircleToil = queryPos;
                                break;
                            }
                        }
                    }
                }
            } else {
                BlockEntity be = pLevel.getBlockEntity(pEntity.linkedCircleToil);
                if(be instanceof CircleToilBlockEntity ctbe) {
                    LazyOptional<IEnergyStorage> query = ctbe.getCapability(ForgeCapabilities.ENERGY);
                    if(query.isPresent()) {
                        IEnergyStorage cap = query.resolve().get();
                        int tryExtract = cap.extractEnergy(pEntity.ENERGY_STORAGE.getMaxEnergyStored(), true);
                        int insert = pEntity.ENERGY_STORAGE.receiveEnergy(tryExtract, false);
                        cap.extractEnergy(insert, false);
                    }
                } else {
                    pEntity.linkedCircleToil = null;
                }
            }
        }
        //particle work
        else if(pEntity.progress > 0 && pLevel.getGameTime() % 8 == 0) {
            Direction facing = pState.getValue(FACING);

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
                final ItemStack[] contentsOfInputSlots = pEntity.getContentsOfInputSlotsAsArray();
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

                    pEntity.generateMateriaCloud(bowlPositions[0], color);
                }

                if (has2) {
                    int[] color = ColorUtils.getRGBAIntTintFromPackedInt((
                            contentsOfInputSlots[2].isEmpty() ?
                                    (MateriaItem) contentsOfInputSlots[3].getItem() :
                                    (MateriaItem) contentsOfInputSlots[2].getItem()
                    ).getMateriaColor());

                    pEntity.generateMateriaCloud(bowlPositions[1], color);
                }

                if (has3) {
                    int[] color = ColorUtils.getRGBAIntTintFromPackedInt((
                            contentsOfInputSlots[4].isEmpty() ?
                                    (MateriaItem) contentsOfInputSlots[5].getItem() :
                                    (MateriaItem) contentsOfInputSlots[4].getItem()
                    ).getMateriaColor());

                    pEntity.generateMateriaCloud(bowlPositions[2], color);
                }

                if (has4) {
                    int[] color = ColorUtils.getRGBAIntTintFromPackedInt((
                            contentsOfInputSlots[6].isEmpty() ?
                                    (MateriaItem) contentsOfInputSlots[7].getItem() :
                                    (MateriaItem) contentsOfInputSlots[6].getItem()
                    ).getMateriaColor());

                    pEntity.generateMateriaCloud(bowlPositions[3], color);
                }

                if (has5) {
                    int[] color = ColorUtils.getRGBAIntTintFromPackedInt((
                            contentsOfInputSlots[8].isEmpty() ?
                                    (MateriaItem) contentsOfInputSlots[9].getItem() :
                                    (MateriaItem) contentsOfInputSlots[8].getItem()
                    ).getMateriaColor());

                    pEntity.generateMateriaCloud(bowlPositions[4], color);
                }

                if(has1 || has2 || has3 || has4 || has5) {
                    //ring sparkles
                    int total = 10;
                    for(int i=0; i<total; i++) {
                        double radianWiggle = ((Math.PI * 2) / (double)total) * (r.nextDouble() - 0.5);

                        double x = Math.cos((Math.PI * 2) * i / (double)total + radianWiggle);
                        double z = Math.sin((Math.PI * 2) * i / (double)total + radianWiggle);

                        double rx = r.nextDouble() - 0.5;
                        double ry = r.nextDouble();
                        double rz = r.nextDouble() - 0.5;

                        Vector3 mid = new Vector3(0.5, 0.015625, 0.5).add(new Vector3(x, 0, z).scale(0.9f));

                        pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setMaxAge(20 + r.nextInt(30)).setScale(0.03f + r.nextFloat() * 0.03f)
                                        .setColor(150+r.nextInt(75), 150+r.nextInt(75), 150+r.nextInt(75))
                                        .setMover(new ParticleVelocityMover(rx * 0.03, 0.01 + ry * 0.02, rz * 0.03, true)),
                                pPos.getX() + mid.x, pPos.getY() + mid.y, pPos.getZ() + mid.z,
                                0, 0, 0);
                    }

                    Vector3 outputCenter;
                    if(facing == Direction.NORTH) {
                        outputCenter = new Vector3(0.5, 0.25, 0.5 + 0.122994);
                    } else if(facing == Direction.EAST) {
                        outputCenter = new Vector3(0.5 - 0.122994, 0.25, 0.5);
                    } else if(facing == Direction.SOUTH) {
                        outputCenter = new Vector3(0.5, 0.25, 0.5 - 0.122994);
                    } else {
                        outputCenter = new Vector3(0.5 + 0.122994, 0.25, 0.5);
                    }

                    //item chunks
                    if(pEntity.currentItemRecipe != null) {
                        total = 8;
                        for (int i = 0; i < total; i++) {
                            Vector3 end = new Vector3(pPos.getX() + outputCenter.x, pPos.getY() + outputCenter.y, pPos.getZ() + outputCenter.z);
                            Vector3 start = end.add(new Vector3(r.nextDouble() * 2 - 1, r.nextDouble(), r.nextDouble() * 2 - 1).scale(0.5f));

                            pLevel.addParticle(new MAParticleType(ParticleInit.ITEM.get())
                                            .setScale(0.05f).setMaxAge(10 + r.nextInt(10)).setStack(pEntity.currentItemRecipe.getAlchemyObject())
                                            .setMover(new ParticleLerpMover(
                                                    start.x, start.y, start.z,
                                                    end.x, end.y, end.z)),
                                    start.x, start.y, start.z,
                                    0, 0, 0);
                        }
                    }
                }
            }
        }

        pEntity.operationTicks = pEntity.getOperationTicks();

        boolean changed = AbstractFabricationBlockEntity.tick(pLevel, pPos, pState, pEntity, CircleFabricationBlockEntity::getVar);

        if(changed)
            pEntity.syncAndSave();
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

    public ItemStack[] getContentsOfInputSlotsAsArray() {
        ItemStack[] out = new ItemStack[10];
        for(int i=SLOT_INPUT_START; i<SLOT_INPUT_START+SLOT_INPUT_COUNT; i++) {
            out[i-SLOT_INPUT_START] = itemHandler.getStackInSlot(i);
        }
        return out;
    }

    @Override
    public SimpleContainer getContentsOfInputSlots() {
        return super.getContentsOfInputSlots(CircleFabricationBlockEntity::getVar);
    }

    public ItemStack getOutputInLastSlot() {
        ItemStack out = null;
        for(int i=SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT-1; i>=SLOT_OUTPUT_START; i--) {
            if(!itemHandler.getStackInSlot(i).isEmpty())
                out = itemHandler.getStackInSlot(i);
        }
        return out;
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
        return Math.max(1,Math.round(1800f * batchModifier));
    }

    private static final AABB validVentingParticleZone = new AABB(0.375, 0.0625, 0.375, 0.625, 0.0625, 0.625);
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
            currentItemRecipe = DistillationFabricationRecipe.getFabricatingRecipe(level, pQuery);
            currentFluidRecipe = null;
            clearRecipeAfterNextProcess = false;

            if (currentItemRecipe != null) {
                batchSize = 1;
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
        doDeferredRecipeCheck = false;

        syncAndSave();
    }

    public void setCurrentRecipe(Fluid pQuery) {
        currentFluidRecipe = FluidDistillationFabricationRecipe.getFabricatingRecipe(level, pQuery);
        currentItemRecipe = null;
        clearRecipeAfterNextProcess = false;

        if (currentFluidRecipe != null) {
            batchSize = 1;
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

        if(!level.isClientSide()) {
            doDeferredRecipeCheck = false;
        }

        syncAndSave();
    }

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(60, 60) {
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
        if(currentItemRecipe == null && currentFluidRecipe == null)
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
        CircleFabricationBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    @Override
    public byte setRecipe(ItemStack pStack, Player player) {
        DistillationFabricationRecipe distillationFabricationRecipeQuery = DistillationFabricationRecipe.getFabricatingRecipe(getLevel(), pStack);
        if(distillationFabricationRecipeQuery == null)
            return ERROR_CODE_NO_SUCH_RECIPE;

        setCurrentRecipe(pStack.getItem());
        return ERROR_CODE_SUCCESS;
    }

    @Override
    public boolean needsSorting() {
        if(currentItemRecipe != null || currentFluidRecipe != null) return false;

        return !getContentsOfInputSlots(CircleFabricationBlockEntity::getVar).isEmpty();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-2, 0, -2), getBlockPos().offset(2,0,2));
    }

    @Override
    public int getTankCapacity(int tank) {
        return ServerConfig.circleFabricationTankCapacity;
    }

    @Override
    public List<AbstractDirectionalPluginBlockEntity> getPlugins() {
        return new ArrayList<>();
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
