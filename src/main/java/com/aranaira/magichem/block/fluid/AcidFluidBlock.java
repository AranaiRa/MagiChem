package com.aranaira.magichem.block.fluid;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.fluid.AcidFluidType;
import com.aranaira.magichem.recipe.VitriolationRecipe;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Collections;

public class AcidFluidBlock extends LiquidBlock {
    public static final TagKey<Block> TAG_ACID_2_VULNERABLE = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "acid_2_vulnerable"));
    public static final TagKey<Block> TAG_ACID_3_VULNERABLE = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "acid_3_vulnerable"));
    public static final TagKey<Block> TAG_ACID_4_VULNERABLE = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "acid_4_vulnerable"));
    public static final TagKey<Block> TAG_ACID_5_INVULNERABLE = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "acid_5_invulnerable"));

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
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        super.randomTick(pState, pLevel, pPos, pRandom);

        if(getFluid().getFluidType() instanceof AcidFluidType aft) {
            ArrayList<Pair<BlockPos,BlockState>> queries = new ArrayList();
            queries.add(new Pair<>(pPos.below(),pLevel.getBlockState(pPos.below())));
            queries.add(new Pair<>(pPos.north(),pLevel.getBlockState(pPos.north())));
            queries.add(new Pair<>(pPos.south(),pLevel.getBlockState(pPos.south())));
            queries.add(new Pair<>(pPos.east(),pLevel.getBlockState(pPos.east())));
            queries.add(new Pair<>(pPos.west(),pLevel.getBlockState(pPos.west())));
            Collections.shuffle(queries);

            final int acidStrength = aft.getAcidStrength();

            for(int i=0; i<acidStrength-1; i++) {
                boolean doDestruction = false;
                final BlockPos posQuery = queries.get(i).getFirst();
                final BlockState stateQuery = queries.get(i).getSecond();

                if (acidStrength == 5) {
                    doDestruction = !stateQuery.is(TAG_ACID_5_INVULNERABLE);
                }
                if (!doDestruction && acidStrength >= 4) {
                    doDestruction = stateQuery.is(TAG_ACID_4_VULNERABLE);
                }
                if (!doDestruction && acidStrength >= 3) {
                    doDestruction = stateQuery.is(TAG_ACID_3_VULNERABLE);
                }
                if (!doDestruction && acidStrength >= 2) {
                    doDestruction = stateQuery.is(TAG_ACID_2_VULNERABLE);
                }

                if (doDestruction) {
                    pLevel.destroyBlock(posQuery, true);
                }
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
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
