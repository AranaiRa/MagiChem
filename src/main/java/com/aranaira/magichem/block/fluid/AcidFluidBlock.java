package com.aranaira.magichem.block.fluid;

import com.aranaira.magichem.recipe.VitriolationRecipe;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public class AcidFluidBlock extends LiquidBlock {
    public AcidFluidBlock(FlowingFluid pFluid, Properties pProperties) {
        super(pFluid, pProperties);
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if(pLevel.getGameTime() % 10 == 0) {
            if (pEntity instanceof LivingEntity le) {
                final int fluidAcidStrength = VitriolationRecipe.getFluidAcidStrength(this.getFluid());
                le.addEffect(new MobEffectInstance(MobEffectsRegistry.DISSOLUTION.get(), 200, fluidAcidStrength - 1));
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        return true;
    }

    @Override
    public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    }
}
