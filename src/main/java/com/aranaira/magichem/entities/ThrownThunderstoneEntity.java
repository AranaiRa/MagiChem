package com.aranaira.magichem.entities;

import com.aranaira.magichem.block.entity.SkywrathAltarBlockEntity;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.FactionIDs;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.LazyOptional;

public class ThrownThunderstoneEntity extends ThrowableItemProjectile {
    public static final ItemStack DISPLAY_STACK = new ItemStack(ItemRegistry.THUNDERSTONE.get());
    private Player sourcePlayer = null;

    public ThrownThunderstoneEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
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

    private int getTierAndFactionDamage() {
        int damage = 0;
        if(sourcePlayer != null && !sourcePlayer.isCrouching()) {
            final LazyOptional<IPlayerProgression> lazyProg = sourcePlayer.getCapability(PlayerProgressionProvider.PROGRESSION);
            if(lazyProg.isPresent()) {
                final IPlayerProgression cap = lazyProg.resolve().get();
                damage = 5 + (cap.getTier() * 5);
                final IFaction alliedFaction = cap.getAlliedFaction();

                if(alliedFaction != null && alliedFaction.getCastingResources()[0].equals(new ResourceLocation("mna:brimstone"))) {
                    damage *= 2;
                }
            }
        }
        return damage;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if(!level().isClientSide()) {
            BlockPos tPos = new BlockPos(pResult.getEntity().getBlockX(), pResult.getEntity().getBlockY(), pResult.getEntity().getBlockZ());

            int damage = getTierAndFactionDamage();

            if (level().getBlockState(tPos).isAir()) {
                LightningBolt lb = new LightningBolt(EntityType.LIGHTNING_BOLT, level());
                lb.setPos(tPos.getX(), tPos.getY(), tPos.getZ());
                if(damage > 0)
                    lb.setDamage(damage);
                level().addFreshEntity(lb);
            } else {
                Vector3 pos = new Vector3(pResult.getEntity().getX(), pResult.getEntity().getY(), pResult.getEntity().getZ());

                ItemEntity ie = new ItemEntity(level(), pos.x, pos.y, pos.z, DISPLAY_STACK.copy());
                level().addFreshEntity(ie);
            }
        }

        super.onHitEntity(pResult);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if(!level().isClientSide()){
            BlockPos oPos = pResult.getBlockPos();
            BlockState oState = level().getBlockState(oPos);
            BlockPos tPos = oPos.above();
            BlockState tState = level().getBlockState(tPos);

            if (tState.isAir() || (oState.getBlock() == Blocks.LIGHTNING_ROD || tState.getBlock() == Blocks.LIGHTNING_ROD)) {
                boolean isAltarInRange = false;
                for(int y=-2;y<=2;y++) {
                    for (int x=-2;x<=2;x++) {
                        for (int z=-2;z<=2;z++) {
                            isAltarInRange = level().getBlockState(tPos.offset(x, y, z)).getBlock() == BlockRegistry.SKYWRATH_ALTAR.get();
                            if(isAltarInRange) break;
                        }
                        if(isAltarInRange) break;
                    }
                    if(isAltarInRange) break;
                }

                LightningBolt lb = new LightningBolt(EntityType.LIGHTNING_BOLT, level());
                if(!isAltarInRange) {
                    int damage = getTierAndFactionDamage();
                    if(damage > 0) lb.setDamage(damage);
                } else {
                    lb.setVisualOnly(true);
                }
                lb.setPos(tPos.getX()+0.5, tPos.getY(), tPos.getZ()+0.5);
                level().addFreshEntity(lb);
            } else {
                Vector3 pos = new Vector3(pResult.getBlockPos().getX(), pResult.getBlockPos().getY(), pResult.getBlockPos().getZ()).add(new Vector3(0.5f, 0.5f, 0.5f));

                ItemEntity ie = new ItemEntity(level(), pos.x, pos.y, pos.z, DISPLAY_STACK.copy());
                level().addFreshEntity(ie);
            }
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
