package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.recipe.FulminationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class SkywrathAltarBlockEntity extends BlockEntity {
    private ItemStack heldItem = ItemStack.EMPTY;
    private int craftCountdown = -1;

    public static final int CRAFT_COUNTDOWN_LENGTH = 90;
    public static final Random r = new Random();

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(level != null && !level.isClientSide()) {
                ItemStack out = heldItem.copy();
                if(!simulate) {
                    heldItem = ItemStack.EMPTY;
                    syncAndSave();
                }
                return out;
            }

            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if(level != null && !level.isClientSide()) {
                if(heldItem.isEmpty()) {
                    if(!simulate) {
                        heldItem = stack;
                        syncAndSave();
                    }
                    return ItemStack.EMPTY;
                }
            }

            return stack;
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

    public SkywrathAltarBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.SKYWRATH_ALTAR_BE.get(), pPos, pBlockState);
    }

    public void tryCraftItem() {
        final FulminationRecipe recipe = FulminationRecipe.getFulminationRecipe(getLevel(), heldItem.getItem());
        boolean hasValidRecipe = recipe != null && heldItem.getCount() >= recipe.getInput().getCount();
        boolean canStoreRF = heldItem.getCapability(ForgeCapabilities.ENERGY).isPresent();
        boolean isEnchantedBook = heldItem.getItem() == Items.ENCHANTED_BOOK;

        if(hasValidRecipe || canStoreRF || isEnchantedBook) {
            craftCountdown = CRAFT_COUNTDOWN_LENGTH;
            syncAndSave();
        }
    }

    private boolean craftItem() {
        final FulminationRecipe recipe = FulminationRecipe.getFulminationRecipe(getLevel(), heldItem.getItem());
        if(recipe != null) {
            int minRequired = recipe.getInput().getCount();
            if(heldItem.getCount() >= minRequired) {
                int remainder = heldItem.getCount() - minRequired;

                if(remainder > 0) {
                    ItemEntity ie = new ItemEntity(level,
                            getBlockPos().getX() + 0.5, getBlockPos().getY() + 1, getBlockPos().getZ() + 0.5,
                            new ItemStack(recipe.getInput().getItem(), remainder),
                            (r.nextDouble() - 0.5) * 0.6, 0.2, (r.nextDouble() - 0.5) * 0.6);

                    level.addFreshEntity(ie);
                }

                heldItem = recipe.getResult().copy();
                return true;
            }
        }
        return false;
    }

    private boolean chargeItem() {
        final LazyOptional<IEnergyStorage> energyCapHolder = heldItem.getCapability(ForgeCapabilities.ENERGY);
        if(energyCapHolder.isPresent()) {
            final IEnergyStorage cap = energyCapHolder.resolve().get();
            int max = cap.getMaxEnergyStored();

            int chargeUncapped = Math.round((float)max * (float)ServerConfig.skywrathAltarFERechargePercentage / 100f);
            cap.receiveEnergy(Math.min(chargeUncapped, ServerConfig.skywrathAltarFERechargeLimit), false);

            return true;
        }
        return false;
    }

    private boolean scrapEnchantedBook() {
        if(heldItem.getItem() == Items.ENCHANTED_BOOK && heldItem.hasTag() && heldItem.getTag().contains("StoredEnchantments")) {
            final ListTag nbt = heldItem.getTag().getList("StoredEnchantments", ListTag.TAG_COMPOUND);

            int highestLevel = 0;
            int totalLevelsExpo = 0;

            for(int i=0; i<nbt.size(); i++) {
                final CompoundTag thisEntry = nbt.getCompound(i);
                int lvlCapped = Math.min(10, thisEntry.getInt("lvl"));

                highestLevel = Math.max(highestLevel, lvlCapped);
                totalLevelsExpo += ((lvlCapped*3) * (lvlCapped*3));
            }

            float divisor = Math.max(1, 10 - highestLevel);
            float chance = (totalLevelsExpo / divisor);
            if(r.nextFloat(100) <= chance) {
                heldItem = new ItemStack(ItemRegistry.SCORCHED_PROFUNDITY.get(), 1);
            } else {
                heldItem = new ItemStack(ItemRegistry.SCORCHED_THEOREM.get(), Math.round(totalLevelsExpo / 10f));
            }

            syncAndSave();
            return true;
        }

        return false;
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(Player pPlayer, ItemStack pNewItemStack) {
        if(!heldItem.isEmpty()) {
            ItemEntity ie = new ItemEntity(level, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), heldItem.copy());
            level.addFreshEntity(ie);
        }

        heldItem = pNewItemStack;

        syncAndSave();
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
        nbt.putString("heldItem", ForgeRegistries.ITEMS.getKey(heldItem.getItem()).toString());
        nbt.putInt("heldItemCount", heldItem.getCount());
        nbt.putInt("craftCountdown", craftCountdown);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        craftCountdown = nbt.getInt("craftCountdown");

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("heldItem")));
        if(item != null) {
            heldItem = new ItemStack(item, nbt.getInt("heldItemCount"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("heldItem", ForgeRegistries.ITEMS.getKey(heldItem.getItem()).toString());
        nbt.putInt("heldItemCount", heldItem.getCount());
        nbt.putInt("craftCountdown", craftCountdown);
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

    public static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, T t) {
        if(t instanceof SkywrathAltarBlockEntity entity)
        if(entity.craftCountdown >= 0) {
            if(entity.craftCountdown == 0) {
                if(!entity.chargeItem())
                    if(!entity.craftItem())
                        entity.scrapEnchantedBook();
            }

            //Particle work
            if(pLevel.isClientSide()) {
                int period = 850;
                int gt = (int)(pLevel.getGameTime() % (period * 2.75 + 0.25));
                double posBob = Math.sin((((gt) % period) / (double)period) * (Math.PI * 2) * Math.PI * 2) * 0.03125 * 0.707 + 0.1875;
                float vectorScale = ((float) entity.craftCountdown / (float) CRAFT_COUNTDOWN_LENGTH) * 1.5f;
                Vector3 origin = new Vector3(0.5, 1.1 + posBob, 0.5);

                for(int i=0; i<4; i++) {
                    Vector3 extent = new Vector3(r.nextDouble() - 0.5, r.nextDouble() - 0.5, r.nextDouble() - 0.5)
                            .normalize().scale(vectorScale * 2)
                            .add(origin);

                    pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                    .setMaxAge(8 + r.nextInt(6)).setScale(20),
                            pPos.getX() + origin.x, pPos.getY() + origin.y, pPos.getZ() + origin.z,
                            pPos.getX() + extent.x, pPos.getY() + extent.y, pPos.getZ() + extent.z);
                }

                int radialCount = 4;
                for(int i=0; i<radialCount; i++) {
                    double cos = Math.cos((6.2832d / radialCount) * i);
                    double sin = Math.sin((6.2832d / radialCount) * i);
                    Vector3 start = new Vector3(cos, 0, sin).scale(2 * vectorScale).add(new Vector3(0.5, 0, 0.5));
                    Vector3 end = new Vector3(cos, 0, sin).scale(2 * vectorScale).add(new Vector3(0.5, 4, 0.5));

                    pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                    .setMaxAge(3).setScale(20)
                                    .setColor(128, 144, 255, 255),
                            pPos.getX() + start.x, pPos.getY() + start.y, pPos.getZ() + start.z,
                            pPos.getX() + end.x, pPos.getY() + end.y, pPos.getZ() + end.z);
                }

                double speed = 0.175;
                pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setMaxAge(8 + r.nextInt(10)).setScale(0.04f),
                        pPos.getX() + origin.x, pPos.getY() + origin.y, pPos.getZ() + origin.z,
                        (r.nextFloat() - 0.5) * speed, (r.nextFloat() - 0.5) * speed, (r.nextFloat() - 0.5) * speed);

                if(entity.craftCountdown == 0) {
                    speed = 0.25;
                    for(int i=0; i<96; i++) {
                        int c = r.nextInt(32)+190;
                        pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setMaxAge(28 + r.nextInt(42)).setGravity(0.01f).setPhysics(true)
                                        .setColor(c, c, 255, 196).setScale(0.08f+r.nextFloat(0.04f)),
                                pPos.getX() + origin.x, pPos.getY() + origin.y, pPos.getZ() + origin.z,
                                (r.nextFloat() - 0.5) * speed, (r.nextFloat()) * speed * 0.625, (r.nextFloat() - 0.5) * speed);
                    }

                    for(int i=0;i<5;i++) {
                        pLevel.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())
                                        .setMaxAge(40).setScale(20)
                                        .setColor(128, 144, 255, 255),
                                pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5,
                                pPos.getX() + 0.5, pPos.getY() + 12, pPos.getZ() + 0.5);
                    }
                }
            }

            if(entity.heldItem.isEmpty()) {
                entity.craftCountdown = -1;
            } else {
                entity.craftCountdown--;
            }
        }
    }

    public void dropInventory() {
        ItemEntity ie = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), heldItem);
        getLevel().addFreshEntity(ie);
    }
}
