package com.aranaira.magichem.entities;

import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.LazyOptional;

public class ThrownSonicBombEntity extends ThrowableItemProjectile {
    public static final ItemStack DISPLAY_STACK = new ItemStack(ItemRegistry.SONIC_BOMB.get());
    private Player sourcePlayer = null;

    public ThrownSonicBombEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void setSourcePlayer(Player pPlayer) {
        sourcePlayer = pPlayer;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        kill();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if(!level().isClientSide()) {
            DestructiveHarmonicsEntity dhe = new DestructiveHarmonicsEntity(EntitiesRegistry.DESTRUCTIVE_HARMONICS_ENTITY.get(), level());
            dhe.setInitiatingPlayer(sourcePlayer);
            dhe.setTargetPos(new BlockPos(pResult.getEntity().getBlockX(), pResult.getEntity().getBlockY(), pResult.getEntity().getBlockZ()));
            dhe.setPos(pResult.getLocation().x(), pResult.getLocation().y(), pResult.getLocation().z());
            level().addFreshEntity(dhe);
        }

        super.onHitEntity(pResult);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if(!level().isClientSide()){
            DestructiveHarmonicsEntity dhe = new DestructiveHarmonicsEntity(EntitiesRegistry.DESTRUCTIVE_HARMONICS_ENTITY.get(), level());
            dhe.setInitiatingPlayer(sourcePlayer);
            dhe.setTargetPos(new BlockPos((int)Math.round(pResult.getLocation().x()), (int)Math.round(pResult.getLocation().y()), (int)Math.round(pResult.getLocation().z())));
            dhe.setPos(pResult.getLocation().x(), pResult.getLocation().y(), pResult.getLocation().z());
            level().addFreshEntity(dhe);
        }

        super.onHitBlock(pResult);
    }

    @Override
    public ItemStack getItem() {
        return DISPLAY_STACK;
    }

    @Override
    protected ItemStack getItemRaw() {
        return DISPLAY_STACK;
    }

    @Override
    protected Item getDefaultItem() {
        return DISPLAY_STACK.getItem();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(pSource.typeHolder().is(DamageTypes.FELL_OUT_OF_WORLD))
            return super.hurt(pSource, pAmount);
        return false;
    }
}
