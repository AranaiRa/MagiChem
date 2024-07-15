package com.aranaira.magichem.entities;

import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownThunderstoneEntity extends ThrowableItemProjectile {
    public static final ItemStack DISPLAY_STACK = new ItemStack(ItemRegistry.THUNDERSTONE.get());

    public ThrownThunderstoneEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
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
            BlockPos tPos = new BlockPos(pResult.getEntity().getBlockX(), pResult.getEntity().getBlockY(), pResult.getEntity().getBlockZ());

            if (level().getBlockState(tPos).isAir()) {
                LightningBolt lb = new LightningBolt(EntityType.LIGHTNING_BOLT, level());
                lb.setPos(tPos.getX(), tPos.getY(), tPos.getZ());
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
            BlockPos tPos = pResult.getBlockPos().above();

            if (level().getBlockState(tPos).isAir()) {
                LightningBolt lb = new LightningBolt(EntityType.LIGHTNING_BOLT, level());
                lb.setPos(tPos.getX(), tPos.getY(), tPos.getZ());
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
}
