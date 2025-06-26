package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.recipe.IlluminationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.aranaira.magichem.util.render.ColorUtils.SIX_STEP_PARTICLE_COLORS;

public class AstralObserverBlockEntity extends BlockEntity {
    public static final float
        BEAM_FILL_SPEED = 0.06f;

    private LuminType luminType = LuminType.NONE;
    private int
            currentLumins = 0, luminsNeeded = 0;
    private IlluminationRecipe recipe = null;
    private ItemStack heldItem = ItemStack.EMPTY;
    private static final Random r = new Random();

    public float
        colorLerp = 0, beamLerp = 0;
    public boolean
        doColorLerp = false;

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(!simulate && level != null && !level.isClientSide()) {
                ItemStack stack = heldItem.copy();
                if(!stack.isEmpty()) {
                    CompoundTag nbt = new CompoundTag();
                    if(recipe != null && stack.getItem() != recipe.getResultItem().getItem()) {
                        if (stack.hasTag()) {
                            nbt = stack.getTag();
                        }
                        if (currentLumins > 0) {
                            CompoundTag luminsTag = new CompoundTag();
                            luminsTag.putInt("type", recipe.getLuminType().ordinal());
                            luminsTag.putInt("current", currentLumins);
                            luminsTag.putInt("needed", luminsNeeded);
                            nbt.put("magichemLumins", luminsTag);
                            stack.setTag(nbt);
                        }
                    }
                    currentLumins = 0;
                    luminsNeeded = 0;
                    heldItem = ItemStack.EMPTY;
                    recipe = null;
                    syncAndSave();
                    return stack;
                }
            }

            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if(level != null && !level.isClientSide()) {
                if(heldItem.isEmpty()) {
                    if (stack.hasTag()) {
                        final CompoundTag nbt = stack.getTag();
                        if (nbt != null && nbt.contains("magichemLumins")) {
                            CompoundTag luminsTag = nbt.getCompound("magichemLumins");
                            luminType = LuminType.luminTypeFromOrdinal(luminsTag.getInt("type"));
                            currentLumins = luminsTag.getInt("current");
                            luminsNeeded = luminsTag.getInt("needed");
                        }
                    }
                    if(!simulate) {
                        heldItem = stack.copy();
                        syncAndSave();
                    }
                    return ItemStack.EMPTY;
                }
            }

            return stack;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if(level != null && !level.isClientSide()) {
                if(heldItem.isEmpty()) {
                    if (stack.hasTag()) {
                        final CompoundTag nbt = stack.getTag();
                        if (nbt != null && nbt.contains("magichemLumins")) {
                            CompoundTag luminsTag = nbt.getCompound("magichemLumins");
                            luminType = LuminType.luminTypeFromOrdinal(luminsTag.getInt("type"));
                            currentLumins = luminsTag.getInt("current");
                            luminsNeeded = luminsTag.getInt("needed");
                        }
                    }
                    heldItem = stack;
                    syncAndSave();
                }
            }
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return heldItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    public AstralObserverBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ASTRAL_OBSERVER_BE.get(), pPos, pBlockState);
    }

    public void dropInventory() {
        SimpleContainer inventory = new SimpleContainer(1);
        inventory.setItem(0, itemHandler.getStackInSlot(0));
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public ItemStack getItem() {
        return itemHandler.getStackInSlot(0);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        lazyItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("heldItem", heldItem.serializeNBT());
        nbt.putInt("type", luminType.ordinal());
        nbt.putInt("current", currentLumins);
        nbt.putInt("needed", luminsNeeded);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        heldItem = ItemStack.of(nbt.getCompound("heldItem"));
        luminType = LuminType.luminTypeFromOrdinal(nbt.getInt("type"));
        currentLumins = nbt.getInt("current");
        luminsNeeded = nbt.getInt("needed");

        //If we have no item or if it changed, we need to reset the current recipe
        if(itemHandler.getStackInSlot(0).isEmpty() || recipe != null && recipe.getInputItem().getItem() != itemHandler.getStackInSlot(0).getItem()) {
            recipe = null;
            luminType = LuminType.NONE;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("heldItem", heldItem.serializeNBT());
        nbt.putInt("type", luminType.ordinal());
        nbt.putInt("current", currentLumins);
        nbt.putInt("needed", luminsNeeded);
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

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof AstralObserverBlockEntity entity) {
            entity.luminType = entity.getLuminPhase(true);

            if (level.canSeeSkyFromBelowWater(pos)) {
                //we should have a recipe if there's an item present
                boolean needsNewRecipe = entity.recipe == null && !entity.heldItem.isEmpty();

                if (needsNewRecipe) {
                    entity.recipe = IlluminationRecipe.getIlluminationRecipe(level, entity.heldItem.getItem(), entity.luminType);
                    if (entity.recipe != null) {
                        entity.luminsNeeded = entity.recipe.getCraftTime() * 1200;
                    }
                } else if (entity.recipe != null) {
                    //If we have the wrong type of lumins we need to start draining them
                    if (entity.luminType != entity.recipe.getLuminType()) {
                        entity.currentLumins = Math.max(0, entity.currentLumins - 2);
                        //reset the recipe once the lumins are empty
                        if (entity.currentLumins <= 0) entity.recipe = null;
                    } else {
                        if (entity.currentLumins < entity.luminsNeeded) {
                            entity.currentLumins++;
                        } else {
                            entity.heldItem = entity.recipe.getResultItem().copy();
                        }
                    }
                }

                //VFX and animation drivers
                if (level.isClientSide() && entity.recipe != null) {
                    if ((entity.luminType == entity.recipe.getLuminType()) && (entity.currentLumins < entity.luminsNeeded)) {
                        int[] primaryColor = LuminType.getParticleColor(entity.luminType);
                        int[] bleachedColor = new int[3];
                        bleachedColor[0] = (int) MathUtils.lerpf(primaryColor[0], 255, 0.425f);
                        bleachedColor[1] = (int) MathUtils.lerpf(primaryColor[1], 255, 0.425f);
                        bleachedColor[2] = (int) MathUtils.lerpf(primaryColor[2], 255, 0.425f);
                        int[] dimmedColor = new int[3];
                        dimmedColor[0] = (int) MathUtils.lerpf(primaryColor[0], 0, 0.75f);
                        dimmedColor[1] = (int) MathUtils.lerpf(primaryColor[1], 0, 0.75f);
                        dimmedColor[2] = (int) MathUtils.lerpf(primaryColor[2], 0, 0.75f);

                        Vector3 center = new Vector3(entity.getBlockPos().getX() + 0.5, entity.getBlockPos().getY() + 1.0625, entity.getBlockPos().getZ() + 0.5);

                        //sparks
                        level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setPhysics(false).setScale(0.0625f).setMaxAge(45).setPhysics(true).setGravity(0.02f)
                                        .setColor(bleachedColor[0], bleachedColor[1], bleachedColor[2], 196),
                                center.x, center.y, center.z,
                                r.nextDouble(0.1) - 0.05, 0.04 + r.nextDouble(0.12), r.nextDouble(0.1) - 0.05);

                        if (level.getGameTime() % 2 == 0) {
                            level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                            .setPhysics(false).setScale(0.0625f).setMaxAge(65).setPhysics(true)
                                            .setColor(primaryColor[0], primaryColor[1], primaryColor[2], 196),
                                    center.x, center.y, center.z,
                                    r.nextDouble(0.035) - 0.0175, 0.005 + r.nextDouble(0.02), r.nextDouble(0.035) - 0.0175);
                        }

                        //glow
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                        level.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(dimmedColor[0], dimmedColor[1], dimmedColor[2], 96)
                                        .setScale(0.18f).setMaxAge(24)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }
                }
            }
        }
    }

    public LuminType getLuminPhase(boolean pUpdateInternals) {
        if(level == null)
            return LuminType.NONE;

        float time = (level.getTimeOfDay(0) + 0.75f) % 1f;
        long day = (level.getDayTime() / 24000) % 8;
        boolean isStarTime = (day >= 3) && (day <= 5);

        //0.995..1.0 == reset
        if (time > 0.995f) {
            if(pUpdateInternals) {
                colorLerp = MathHelper.doubleExponentialSeat((time - 0.995f) / 0.005f, 2f);
                doColorLerp = true;
            }
            return isStarTime ? LuminType.SIDEREAL : LuminType.LUNAR;
        }
        //0.5-0.995 == day
        else if (time > 0.5f) {
            if(pUpdateInternals) {
                doColorLerp = false;
            }
            return LuminType.SOLAR;
        }
        //0.495..0.5 == reset
        else if (time > 0.495f) {
            if(pUpdateInternals) {
                colorLerp = MathHelper.doubleExponentialSeat((time - 0.495f) / 0.005f, 2f);
                doColorLerp = true;
            }
            return LuminType.SOLAR;
        }
        //0..0.495 == night
        else {
            if(pUpdateInternals) {
                doColorLerp = false;
            }
            return isStarTime ? LuminType.SIDEREAL : LuminType.LUNAR;
        }
    }

    public LuminType getInverseLuminPhase(boolean pUpdateInternals) {
        if(level == null)
            return LuminType.NONE;

        float time = (level.getTimeOfDay(0) + 0.75f) % 1f;
        long day = (level.getDayTime() / 24000) % 8;
        boolean isStarTime = (day >= 3) && (day <= 5);

        //0.995..1.0 == reset
        if (time > 0.995f) {
            if(pUpdateInternals) {
                colorLerp = MathHelper.doubleExponentialSeat((time - 0.995f) / 0.005f, 2f);
                doColorLerp = true;
            }
            return LuminType.SOLAR;
        }
        //0.5-0.995 == day
        else if (time > 0.5f) {
            if(pUpdateInternals) {
                doColorLerp = false;
            }
            return isStarTime ? LuminType.SIDEREAL : LuminType.LUNAR;
        }
        //0.495..0.5 == reset
        else if (time > 0.495f) {
            if(pUpdateInternals) {
                colorLerp = MathHelper.doubleExponentialSeat((time - 0.495f) / 0.005f, 2f);
                doColorLerp = true;
            }
            return isStarTime ? LuminType.SIDEREAL : LuminType.LUNAR;
        }
        //0..0.495 == night
        else {
            if(pUpdateInternals) {
                doColorLerp = false;
            }
            return LuminType.SOLAR;
        }
    }

    public float getProgressPercent() {
        if(luminsNeeded == 0) return 0;
        return (float)((float)currentLumins / (float)luminsNeeded);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3,3,3));
    }
}
