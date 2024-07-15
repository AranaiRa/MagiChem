package com.aranaira.magichem.block.fluid;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;

public class LiquidLightFluidBlock extends LiquidBlock {
    public LiquidLightFluidBlock(FlowingFluid pFluid, Properties pProperties) {
        super(pFluid, pProperties);
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if(pLevel.getGameTime() % 10 == 0) {
            if (pEntity instanceof LivingEntity le) {
                if (le.getMobType() == MobType.UNDEAD) {
                    le.hurt(le.damageSources().magic(), 2.0f);
                    le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600));
                } else {
                    le.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600));
                    le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600));
                    le.removeEffect(MobEffects.BLINDNESS);
                    le.removeEffect(MobEffects.DARKNESS);
                }
            }
        }

        super.entityInside(pState, pLevel, pPos, pEntity);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    }
}
