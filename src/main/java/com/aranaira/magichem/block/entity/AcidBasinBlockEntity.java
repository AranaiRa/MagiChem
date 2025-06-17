package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.AcidBasinBlock;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.IRequiresRouterCleanupOnDestruction;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.recipe.VitriolationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import team.chisel.ctm.client.util.Dir;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AcidBasinBlockEntity extends BlockEntity implements IFluidHandler, IRequiresRouterCleanupOnDestruction {
    public static final int
            SLOT_COUNT = 2,
            SLOT_INPUT = 0, SLOT_OUTPUT = 1,
            TANK_COUNT = 2,
            TANK_INPUT = 0, TANK_OUTPUT = 1;
    private int
        progress = 0;
    private VitriolationRecipe recipe;
    private boolean reCheckRecipe = false;
    private static final Random r = new Random();

    public AcidBasinBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACID_BASIN_BE.get(), pPos, pBlockState);
        this.lazyFluidHandler = LazyOptional.of(() -> this);
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot != SLOT_OUTPUT;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if(slot == SLOT_INPUT) {
                if(level != null && !level.isClientSide()) {
                    getRecipe();
                } else {
                    reCheckRecipe = true;
                }
            }

            syncAndSave();
        }
    };

    private final LazyOptional<IFluidHandler> lazyFluidHandler;
    private FluidStack
            inputTank = FluidStack.EMPTY,
            outputTank = FluidStack.EMPTY;

    @Override
    public void destroyRouters() {
        AcidBasinBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(MagiChemBlockStateProperties.FACING));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        else if(cap == ForgeCapabilities.FLUID_HANDLER) return lazyFluidHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    public ItemStack getInputItem() {
        return itemHandler.getStackInSlot(SLOT_INPUT);
    }

    public ItemStack getOutputItem() {
        return itemHandler.getStackInSlot(SLOT_OUTPUT);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put("inventory",itemHandler.serializeNBT());
        nbt.putInt("progress",progress);
        if(!inputTank.isEmpty()) {
            CompoundTag inputTankTag = new CompoundTag();
            inputTankTag.putString("fluid",ForgeRegistries.FLUIDS.getKey(inputTank.getFluid()).toString());
            inputTankTag.putInt("amount",inputTank.getAmount());
            nbt.put("inputTank",inputTankTag);
        }
        if(!outputTank.isEmpty()) {
            CompoundTag outputTankTag = new CompoundTag();
            outputTankTag.putString("fluid",ForgeRegistries.FLUIDS.getKey(outputTank.getFluid()).toString());
            outputTankTag.putInt("amount",outputTank.getAmount());
            nbt.put("outputTank",outputTankTag);
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("progress");
        if(nbt.contains("inputTank")) {
            CompoundTag inputTankTag = nbt.getCompound("inputTank");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputTankTag.getString("fluid")));
            if(fluid != null)
                inputTank = new FluidStack(fluid, inputTankTag.getInt("amount"));
        } else {
            inputTank = FluidStack.EMPTY;
        }
        if(nbt.contains("outputTank")) {
            CompoundTag outputTankTag = nbt.getCompound("outputTank");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(outputTankTag.getString("fluid")));
            if(fluid != null)
                outputTank = new FluidStack(fluid, outputTankTag.getInt("amount"));
        } else {
            outputTank = FluidStack.EMPTY;
        }

        if(progress > -1 && level != null) {
            reCheckRecipe = true;
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory",itemHandler.serializeNBT());
        nbt.putInt("progress",progress);
        if(!inputTank.isEmpty()) {
            CompoundTag inputTankTag = new CompoundTag();
            inputTankTag.putString("fluid",ForgeRegistries.FLUIDS.getKey(inputTank.getFluid()).toString());
            inputTankTag.putInt("amount",inputTank.getAmount());
            nbt.put("inputTank",inputTankTag);
        }
        if(!outputTank.isEmpty()) {
            CompoundTag outputTankTag = new CompoundTag();
            outputTankTag.putString("fluid",ForgeRegistries.FLUIDS.getKey(outputTank.getFluid()).toString());
            outputTankTag.putInt("amount",outputTank.getAmount());
            nbt.put("outputTank",outputTankTag);
        }
        return nbt;
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    ////////////////
    //FLUID HANDLING
    ////////////////

    @Override
    public int getTanks() {
        return TANK_COUNT;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if(tank == TANK_INPUT)
            return inputTank;
        else if(tank == TANK_OUTPUT)
            return outputTank;
        else
            return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return ServerConfig.acidBasinTankCapacity;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if(tank == TANK_OUTPUT) return false;
        else if(inputTank.isEmpty()) return true;
        else return stack.getFluid() == inputTank.getFluid();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if(inputTank.isEmpty()) {
            int inserted = Math.min(ServerConfig.acidBasinTankCapacity, resource.getAmount());
            if(action == FluidAction.EXECUTE) {
                inputTank = new FluidStack(resource.getFluid(), inserted);
                syncAndSave();
            }
            return inserted;
        } else if(resource.getFluid() == inputTank.getFluid()) {
            int capacity = ServerConfig.acidBasinTankCapacity - inputTank.getAmount();
            int inserted = Math.min(resource.getAmount(), capacity);
            if(action == FluidAction.EXECUTE) {
                inputTank.grow(inserted);
                syncAndSave();
            }
            reCheckRecipe = true;
            return inserted;
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if(resource.getFluid() == outputTank.getFluid()) return drainFromTank(TANK_OUTPUT, resource.getAmount(), action);
        else if(resource.getFluid() == inputTank.getFluid()) return drainFromTank(TANK_INPUT, resource.getAmount(), action);

        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return drainFromTank(TANK_OUTPUT, maxDrain, action);
    }

    public @NotNull FluidStack drainFromTank(int tank, int maxDrain, FluidAction action) {
        if(tank == TANK_INPUT && !inputTank.isEmpty()) {
            int extracted = Math.min(maxDrain, inputTank.getAmount());
            FluidStack output = inputTank.copy();
            output.setAmount(extracted);
            if(action == FluidAction.EXECUTE) {
                inputTank.shrink(extracted);
                syncAndSave();
            }
            return output;
        } else if(tank == TANK_OUTPUT && !outputTank.isEmpty()) {
            int extracted = Math.min(maxDrain, outputTank.getAmount());
            FluidStack output = new FluidStack(outputTank.getFluid(), extracted);
            if(action == FluidAction.EXECUTE) {
                outputTank.shrink(extracted);
                syncAndSave();
            }
            reCheckRecipe = true;
            return output;
        }

        return FluidStack.EMPTY;
    }

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, E e) {
        if(e instanceof AcidBasinBlockEntity entity) {
            if(pLevel != null && entity.reCheckRecipe) {
                entity.getRecipe();
                entity.reCheckRecipe = false;
            }

            if(entity.recipe != null && entity.canCraftItem()) {
                //Particle work
                if(pLevel.isClientSide() && pLevel.getGameTime() % 2 == 0) {
                    Vector3f mid = new Vector3f(0.5f, 0.75f, 0.5f);

                    int dX = 0, dZ = 0;
                    Direction dir = pBlockState.getValue(MagiChemBlockStateProperties.FACING);
                    if(dir == Direction.NORTH) {
                        dX = -1;
                    } else if(dir == Direction.EAST) {
                        dZ = -1;
                    } else if(dir == Direction.SOUTH) {
                        dX = 1;
                    } else if(dir == Direction.WEST) {
                        dZ = 1;
                    }

                    Vector3f offset = new Vector3f((r.nextFloat() - 0.5f) * 0.125f + dX, 0.0f, (r.nextFloat() - 0.5f) * 0.125f + dZ);

                    Vector3 pos = new Vector3(entity.getBlockPos().getX() + mid.x + offset.x,
                            entity.getBlockPos().getY() + mid.y + offset.y,
                            entity.getBlockPos().getZ() + mid.z + offset.z);
                    Vector3 speed = new Vector3(0, 0.05, 0);

                    pLevel.addParticle(new MAParticleType(ParticleInit.DUST.get())
                                    .setScale(0.10f).setMaxAge(30).setGravity(0)
                                    .setMover(new ParticleVelocityMover(speed.x, speed.y, speed.z, true))
                                    .setColor(128, 134, 144, 48),
                            pos.x, pos.y+0.375, pos.z,
                            0, 0, 0);
                }

                entity.progress--;

                if(entity.progress <= 0) {
                    entity.craftItem();
                }
            }
        }
    }

    private boolean canCraftItem() {
        boolean hasInputFluid = !inputTank.isEmpty();
        if(hasInputFluid) {
            if(recipe.hasInputFluidOverride()) {

            } else {
                int dAcid = VitriolationRecipe.getFluidAcidStrengthDifference(inputTank.getFluid(), recipe.getMinimumAcidStrength());
                if (dAcid == 0) {
                    return inputTank.getAmount() >= recipe.getBaseFluidConsumed();
                } else if (dAcid == 1) {
                    return inputTank.getAmount() >= recipe.getBaseFluidConsumed() / 4;
                } else if (dAcid >= 2) {
                    return true;
                }
            }
        }

        boolean hasSpaceForOutputFluid = true;
        if(recipe.hasResultFluid()) {
            int tankCapacity = ServerConfig.acidBasinTankCapacity - outputTank.getAmount();
            boolean fluidMatches = outputTank.isEmpty() || (outputTank.getFluid() == recipe.getResultFluid().getFluid());

            hasSpaceForOutputFluid = (fluidMatches && tankCapacity >= recipe.getResultFluid().getAmount());
        }

        boolean hasSpaceForOutputItem = true;
        if(recipe.hasResultItem()) {
            int itemCapacity = recipe.getResultItem().getMaxStackSize() - getOutputItem().getCount();
            boolean itemMatches = getOutputItem().isEmpty() || (getOutputItem().getItem() == recipe.getResultItem().getItem());

            hasSpaceForOutputItem = (itemMatches && itemCapacity >= recipe.getResultItem().getCount());
        }

        return hasInputFluid && hasSpaceForOutputFluid && hasSpaceForOutputItem;
    }

    private void craftItem() {
        if(recipe.hasResultFluid()) {
            if(outputTank.isEmpty()) {
                outputTank = recipe.getResultFluid().copy();
            } else {
                outputTank.grow(recipe.getResultFluid().getAmount());
            }
        }

        if(recipe.hasResultItem()) {
            if(getOutputItem().isEmpty()) {
                itemHandler.setStackInSlot(SLOT_OUTPUT, recipe.getResultItem().copy());
            } else {
                getOutputItem().grow(recipe.getResultItem().getCount());
            }
        }

        inputTank.shrink(recipe.getBaseFluidConsumed());
        getInputItem().shrink(recipe.getInputItem().getCount());

        if(getInputItem().getCount() < recipe.getInputItem().getCount()) {
            recipe = null;
            progress = -1;
        } else {
            progress = recipe.getCraftTicks();
        }

        syncAndSave();
    }

    private void getRecipe() {
        ArrayList<VitriolationRecipe> validRecipes = new ArrayList<>();
        for(VitriolationRecipe vr : VitriolationRecipe.getAllVitriolationRecipes(level)) {
            if(vr.getInputItem().getItem() == itemHandler.getStackInSlot(SLOT_INPUT).getItem()) {
                validRecipes.add(vr);
            }
        }

        if(validRecipes.size() == 0 || inputTank.isEmpty()) return;

        boolean foundRecipe = false;
        for(VitriolationRecipe recipeQuery : validRecipes) {
            if (recipeQuery.hasInputFluidOverride()) {
                if (recipeQuery.getInputFluidOverride() == inputTank.getFluid()) {
                    recipe = recipeQuery;
                    progress = recipeQuery.getCraftTicks();
                    foundRecipe = true;
                }
            } else if (VitriolationRecipe.isFluidAcid(inputTank.getFluid())) {
                if (VitriolationRecipe.getFluidAcidStrengthDifference(inputTank.getFluid(), recipeQuery.getMinimumAcidStrength()) >= 0) {
                    recipe = recipeQuery;
                    progress = recipeQuery.getCraftTicks();
                    foundRecipe = true;
                }
            }
            if (foundRecipe) break;
        }
    }

    public void packInventoryToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.ACID_BASIN.get());

        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("progress", -1);
        if(!inputTank.isEmpty()) {
            CompoundTag inputTankTag = new CompoundTag();
            inputTankTag.putString("fluid",ForgeRegistries.FLUIDS.getKey(inputTank.getFluid()).toString());
            inputTankTag.putInt("amount",inputTank.getAmount());
            nbt.put("inputTank",inputTankTag);
        }
        if(!outputTank.isEmpty()) {
            CompoundTag outputTankTag = new CompoundTag();
            outputTankTag.putString("fluid",ForgeRegistries.FLUIDS.getKey(outputTank.getFluid()).toString());
            outputTankTag.putInt("amount",outputTank.getAmount());
            nbt.put("outputTank",outputTankTag);
        }

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,1,1));
    }
}
