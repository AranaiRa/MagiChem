package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.gui.AstralObserverMenu;
import com.aranaira.magichem.recipe.IlluminationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.NEEDS_HARD_UPDATE;
import static com.aranaira.magichem.util.render.ColorUtils.SIX_STEP_PARTICLE_COLORS;

public class AstralObserverBlockEntity extends BlockEntity implements MenuProvider {
    private LuminType
            luminTypeThisTick = LuminType.NONE,
            luminTypeInItem = LuminType.NONE;
    private int
            currentLumins = 0, luminsNeeded = 0, lastComparatorOutput = 0;
    private boolean
            holdingCompletedCraft = false, recheckRecipe = false;
    private IlluminationRecipe
            solarRecipe = null, lunarRecipe = null, siderealRecipe = null;
    private ItemStack heldItem = ItemStack.EMPTY;
    private static final Random r = new Random();
    public static final TagKey<Item> ASTRAL_OBSERVER_LENSES = ItemTags.create(new ResourceLocation(MagiChemMod.MODID, "astral_observer_lenses"));

    public float
        colorLerp = 0, beamLerp = 0;
    public boolean
        doColorLerp = false;

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(level != null && !level.isClientSide()) {
                ItemStack stack = heldItem.copy();
                if(!stack.isEmpty()) {
                    CompoundTag nbt = new CompoundTag();
                    if (stack.hasTag()) {
                        nbt = stack.getTag();
                    }
                    if (currentLumins > 0 && !holdingCompletedCraft) {
                        CompoundTag luminsTag = new CompoundTag();
                        luminsTag.putInt("type", luminTypeInItem.ordinal());
                        luminsTag.putInt("current", currentLumins);
                        luminsTag.putInt("needed", luminsNeeded);
                        nbt.put("magichemLumins", luminsTag);
                        stack.setTag(nbt);
                    }
                    if(!simulate) {
                        currentLumins = 0;
                        luminsNeeded = 0;
                        heldItem = ItemStack.EMPTY;
                        solarRecipe = null;
                        lunarRecipe = null;
                        siderealRecipe = null;
                        luminTypeInItem = LuminType.NONE;
                        holdingCompletedCraft = false;
                        syncAndSave();
                    }
                    return stack;
                }
            }

            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if(level != null && !level.isClientSide()) {
                if(heldItem.isEmpty()) {
                    solarRecipe = IlluminationRecipe.getIlluminationRecipe(level, stack.getItem(), LuminType.SOLAR);
                    lunarRecipe = IlluminationRecipe.getIlluminationRecipe(level, stack.getItem(), LuminType.LUNAR);
                    siderealRecipe = IlluminationRecipe.getIlluminationRecipe(level, stack.getItem(), LuminType.SIDEREAL);

                    if (stack.hasTag()) {
                        final CompoundTag nbt = stack.getTag();
                        if (!simulate && nbt != null && nbt.contains("magichemLumins")) {
                            CompoundTag luminsTag = nbt.getCompound("magichemLumins");
                            luminTypeInItem = LuminType.luminTypeFromOrdinal(luminsTag.getInt("type"));
                            currentLumins = luminsTag.getInt("current");
                            luminsNeeded = luminsTag.getInt("needed");
                            nbt.remove("magichemLumins");
                            heldItem.setTag(nbt);
                        }
                    }
                    if(!simulate) {
                        heldItem = stack.copy();
                        heldItem.setCount(1);
                        syncAndSave();
                    }

                    if(stack.getCount() == 1)
                        return ItemStack.EMPTY;
                    else {
                        return stack.copyWithCount(stack.getCount()-1);
                    }
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
                            solarRecipe = IlluminationRecipe.getIlluminationRecipe(level, stack.getItem(), LuminType.SOLAR);
                            lunarRecipe = IlluminationRecipe.getIlluminationRecipe(level, stack.getItem(), LuminType.LUNAR);
                            siderealRecipe = IlluminationRecipe.getIlluminationRecipe(level, stack.getItem(), LuminType.SIDEREAL);
                            luminTypeInItem = LuminType.luminTypeFromOrdinal(luminsTag.getInt("type"));
                            currentLumins = luminsTag.getInt("current");
                            luminsNeeded = luminsTag.getInt("needed");
                            holdingCompletedCraft = false;
                            nbt.remove("magichemLumins");
                            heldItem.setTag(nbt);
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
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }
    };

    protected LazyOptional<IItemHandler> lazyLensItemHandler = LazyOptional.empty();
    private final ItemStackHandler lensItemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(ASTRAL_OBSERVER_LENSES) || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
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

    public ItemStack getLens() {
        return lensItemHandler.getStackInSlot(0);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        lazyItemHandler.invalidate();
        lazyLensItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            if(side == Direction.UP || side == Direction.DOWN)
                return lazyItemHandler.cast();
            else
                return lazyLensItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    public @NotNull LazyOptional<ItemStackHandler> getLensItemCapability() {
        return lazyLensItemHandler.cast();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyLensItemHandler = LazyOptional.of(() -> lensItemHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("heldItem", heldItem.serializeNBT());
        nbt.put("lensInventory", lensItemHandler.serializeNBT());
        nbt.putInt("type", luminTypeInItem.ordinal());
        nbt.putInt("current", currentLumins);
        nbt.putInt("needed", luminsNeeded);
        nbt.putBoolean("holdingCompletedCraft", holdingCompletedCraft);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        heldItem = ItemStack.of(nbt.getCompound("heldItem"));
        lensItemHandler.deserializeNBT(nbt.getCompound("lensInventory"));
        luminTypeInItem = LuminType.luminTypeFromOrdinal(nbt.getInt("type"));
        currentLumins = nbt.getInt("current");
        luminsNeeded = nbt.getInt("needed");
        holdingCompletedCraft = nbt.getBoolean("holdingCompletedCraft");

        recheckRecipe = true;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("heldItem", heldItem.serializeNBT());
        nbt.put("lensInventory", lensItemHandler.serializeNBT());
        nbt.putInt("type", luminTypeInItem.ordinal());
        nbt.putInt("current", currentLumins);
        nbt.putInt("needed", luminsNeeded);
        nbt.putBoolean("holdingCompletedCraft", holdingCompletedCraft);
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

    private static boolean processLuminsForPhase(AstralObserverBlockEntity pEntity, LuminType pType)
    {
        if(!pEntity.getItem().isEmpty() && !pEntity.getLens().isEmpty()){
            IlluminationRecipe recipe = pEntity.getRecipeForPhase(pType);
            if (recipe != null) {
                if(pEntity.getLens().getItem() == ItemRegistry.DEBUG_ORB.get()) {
                    IlluminationRecipe recipeFallback = pEntity.getRecipeForPhase(pEntity.luminTypeInItem);
                    pEntity.heldItem = (recipeFallback == null ? recipe : recipeFallback).getResultItem().copy();
                    pEntity.luminTypeInItem = (recipeFallback == null ? recipe : recipeFallback).getLuminType();
                    pEntity.currentLumins = (recipeFallback == null ? recipe : recipeFallback).getCraftTime() * 1200 * ServerConfig.astralObserverLuminGainStandard;
                    pEntity.luminsNeeded = pEntity.currentLumins;
                    pEntity.holdingCompletedCraft = true;
                    pEntity.solarRecipe = null;
                    pEntity.lunarRecipe = null;
                    pEntity.siderealRecipe = null;
                    return true;
                }

                if (pEntity.currentLumins <= 0) {
                    pEntity.luminsNeeded = recipe.getCraftTime() * 1200 * ServerConfig.astralObserverLuminGainStandard;
                    pEntity.luminTypeInItem = recipe.getLuminType();
                }

                if (pEntity.getLens().getItem() == ItemRegistry.GLASS_LENS.get() && pEntity.luminTypeInItem == recipe.getLuminType()) {
                    pEntity.currentLumins = Math.min(pEntity.luminsNeeded, pEntity.currentLumins + ServerConfig.astralObserverLuminGainStandard);
                    return true;
                } else if (pEntity.isMatchingCloister(pType)) {
                    pEntity.currentLumins = Math.min(pEntity.luminsNeeded, pEntity.currentLumins + ServerConfig.astralObserverLuminGainCloister);
                    return true;
                } else if (pEntity.isMatchingFarsight(pEntity.luminTypeInItem)) {
                    pEntity.currentLumins = Math.min(pEntity.luminsNeeded, pEntity.currentLumins + ServerConfig.astralObserverLuminGainFarsight);
                    return true;
                } else if (pEntity.isNonMatchingCloister(pType)) {
                    return true;
                } else {
                    pEntity.currentLumins = Math.max(0, pEntity.currentLumins - ServerConfig.astralObserverLuminLoss);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMatchingCloister(LuminType pType) {
        if(pType == LuminType.SOLAR) return getLens().getItem() == ItemRegistry.SOLAR_CLOISTER_LENS.get();
        else if(pType == LuminType.LUNAR) return getLens().getItem() == ItemRegistry.LUNAR_CLOISTER_LENS.get();
        else if(pType == LuminType.SIDEREAL) return getLens().getItem() == ItemRegistry.SIDEREAL_CLOISTER_LENS.get();
        return false;
    }

    private boolean isNonMatchingCloister(LuminType pType) {
        if(pType == LuminType.SOLAR)
            return (getLens().getItem() == ItemRegistry.LUNAR_CLOISTER_LENS.get()) || (getLens().getItem() == ItemRegistry.SIDEREAL_CLOISTER_LENS.get());
        else if(pType == LuminType.LUNAR)
            return (getLens().getItem() == ItemRegistry.SOLAR_CLOISTER_LENS.get()) || (getLens().getItem() == ItemRegistry.SIDEREAL_CLOISTER_LENS.get());
        else if(pType == LuminType.SIDEREAL)
            return (getLens().getItem() == ItemRegistry.SOLAR_CLOISTER_LENS.get()) || (getLens().getItem() == ItemRegistry.LUNAR_CLOISTER_LENS.get());
        return false;
    }

    private boolean isMatchingFarsight(LuminType pType) {
        if(pType == LuminType.SOLAR) return getLens().getItem() == ItemRegistry.SOLAR_FARSIGHT_LENS.get();
        else if(pType == LuminType.LUNAR) return getLens().getItem() == ItemRegistry.LUNAR_FARSIGHT_LENS.get();
        else if(pType == LuminType.SIDEREAL) return getLens().getItem() == ItemRegistry.SIDEREAL_FARSIGHT_LENS.get();
        return false;
    }

    public static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pState, T t) {
        if(t instanceof AstralObserverBlockEntity entity) {
            LuminType previousType = entity.luminTypeThisTick;
            entity.luminTypeThisTick = entity.getLuminPhase(true);

            //Recipe recheck
            if(entity.recheckRecipe || previousType != entity.luminTypeThisTick) {
                if(!entity.heldItem.isEmpty()) {
                    entity.solarRecipe = IlluminationRecipe.getIlluminationRecipe(pLevel, entity.heldItem.getItem(), LuminType.SOLAR);
                    entity.lunarRecipe = IlluminationRecipe.getIlluminationRecipe(pLevel, entity.heldItem.getItem(), LuminType.LUNAR);
                    entity.siderealRecipe = IlluminationRecipe.getIlluminationRecipe(pLevel, entity.heldItem.getItem(), LuminType.SIDEREAL);
                } else {
                    entity.solarRecipe = null;
                    entity.lunarRecipe = null;
                    entity.siderealRecipe = null;
                }
                entity.recheckRecipe = false;
            }
            final IlluminationRecipe recipeThisPhase = entity.getRecipeForPhase(entity.luminTypeThisTick);

            //Progress lumins
            if(!entity.holdingCompletedCraft) {
                if (processLuminsForPhase(entity, entity.luminTypeThisTick) && pLevel.getGameTime() % 80 == 0) {
                    final ItemStack lens = entity.getLens();

                    //Damage lenses
                    if (lens.getItem() != ItemRegistry.GLASS_LENS.get() && lens.getItem() != ItemRegistry.DEBUG_ORB.get()) {
                        if (r.nextFloat() <= 1f / (float) (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.UNBREAKING, lens) + 1)) {
                            lens.setDamageValue(lens.getDamageValue() + 1);
                            if (lens.getDamageValue() >= lens.getMaxDamage())
                                entity.lensItemHandler.setStackInSlot(0, ItemStack.EMPTY);
                        }
                    }

                    //Clear lumin data if lumin stuff is empty
                    if (entity.currentLumins <= 0) {
                        entity.luminTypeInItem = LuminType.NONE;
                        entity.luminsNeeded = 1;
                        CompoundTag nbt = entity.heldItem.getTag();
                        if (nbt != null) {
                            nbt.remove("magichemLumins");
                            entity.syncAndSave();
                        }
                    } else if (recipeThisPhase != null && entity.currentLumins >= entity.luminsNeeded) {
                        entity.heldItem = recipeThisPhase.getResultItem().copy();
                        entity.holdingCompletedCraft = true;
                        entity.solarRecipe = null;
                        entity.lunarRecipe = null;
                        entity.siderealRecipe = null;
                        entity.syncAndSave();
                    }
                }
            }

            if (true) { //sky check later
                //VFX and animation drivers
                if (pLevel.isClientSide() && recipeThisPhase != null) {
                    if ((entity.luminTypeInItem == recipeThisPhase.getLuminType()) && (entity.currentLumins < entity.luminsNeeded)) {
                        int[] primaryColor = LuminType.getParticleColor(entity.luminTypeInItem);
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
                        pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setPhysics(false).setScale(0.0625f).setMaxAge(45).setPhysics(true).setGravity(0.02f)
                                        .setColor(bleachedColor[0], bleachedColor[1], bleachedColor[2], 196),
                                center.x, center.y, center.z,
                                r.nextDouble(0.1) - 0.05, 0.04 + r.nextDouble(0.12), r.nextDouble(0.1) - 0.05);

                        if (pLevel.getGameTime() % 2 == 0) {
                            pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                            .setPhysics(false).setScale(0.0625f).setMaxAge(65).setPhysics(true)
                                            .setColor(primaryColor[0], primaryColor[1], primaryColor[2], 196),
                                    center.x, center.y, center.z,
                                    r.nextDouble(0.035) - 0.0175, 0.005 + r.nextDouble(0.02), r.nextDouble(0.035) - 0.0175);
                        }

                        //glow
                        Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                        pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(dimmedColor[0], dimmedColor[1], dimmedColor[2], 96)
                                        .setScale(0.18f).setMaxAge(24)
                                        .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                                0, 0, 0);
                    }
                }
            }

            if(!pLevel.isClientSide()) {

                if(pState.getValue(NEEDS_HARD_UPDATE)) {
                    pLevel.setBlock(pPos, pState.setValue(NEEDS_HARD_UPDATE, false), 3);
                    pLevel.sendBlockUpdated(pPos, pState, pState.setValue(NEEDS_HARD_UPDATE, false), 3);
                }
            }
        }
    }

    @Nullable
    private IlluminationRecipe getRecipeForPhase(LuminType pLuminType) {
        if(getLens().getItem() == ItemRegistry.SOLAR_FARSIGHT_LENS.get()) return solarRecipe;
        if(getLens().getItem() == ItemRegistry.LUNAR_FARSIGHT_LENS.get()) return lunarRecipe;
        if(getLens().getItem() == ItemRegistry.SIDEREAL_FARSIGHT_LENS.get()) return siderealRecipe;

        return switch(pLuminType) {
            case SOLAR -> solarRecipe;
            case LUNAR -> lunarRecipe;
            case SIDEREAL -> siderealRecipe;
            default -> null;
        };
    }

    public LuminType getLuminTypeInItem() {
        return luminTypeInItem;
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

    public int getComparatorOutput() {
        int out = 0;

        if(holdingCompletedCraft) return 15;

        final IlluminationRecipe recipe = getRecipeForPhase(luminTypeInItem);
        if(recipe != null && !heldItem.isEmpty()) {
            //done
            if(recipe.getResultItem().getItem() == heldItem.getItem()) out = 15;

            //gaining lumins
            else if(getLuminPhase(false) == recipe.getLuminType()) out = 4;

            //losing lumins
            else out = 8;
        }

        if(out != lastComparatorOutput && level != null) {
            level.setBlock(getBlockPos(), getBlockState().setValue(NEEDS_HARD_UPDATE, true), 3);
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState().setValue(NEEDS_HARD_UPDATE, true), 3);
        }

        lastComparatorOutput = out;
        return out;
    }

    public void skipToFullCharge() {
        currentLumins = luminsNeeded - 5;
        syncAndSave();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AstralObserverMenu(pContainerId, pPlayerInventory, this, new SimpleContainerData(0));
    }
}
