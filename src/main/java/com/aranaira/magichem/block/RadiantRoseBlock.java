package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.RadiantRoseBlockEntity;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.FactionIDs;
import com.mna.api.faction.IFaction;
import com.mna.api.faction.IFactionHelper;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class RadiantRoseBlock extends BaseEntityBlock {
    public static final TagKey<Block> DIRT_TAG = BlockTags.DIRT;
    public static final VoxelShape VOXEL_SHAPE = Block.box(5,0,5,11,11,11);

    public RadiantRoseBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RadiantRoseBlockEntity(pPos, pState);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if(!isBlockBelowDirt(pContext.getLevel(), pContext.getClickedPos()))
            return null;

        return super.getStateForPlacement(pContext);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        if(isBlockBelowDirt(pLevel, pPos)) {
            super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
        } else {
            pLevel.destroyBlock(pPos, true);
        }
    }

    private boolean isBlockBelowDirt(Level pLevel, BlockPos pPos) {
        final BlockState stateQuery = pLevel.getBlockState(pPos.below());
        return stateQuery.is(DIRT_TAG);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE;
    }

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (!pLevel.isClientSide) {
            if (pEntity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)pEntity;
                Set<MobEffect> activeEffects = livingentity.getActiveEffectsMap().keySet();

                if(!activeEffects.contains(MobEffectsRegistry.RADIANT_RESOLVE.get())) {
                    final LazyOptional<IPlayerProgression> capQuery = pEntity.getCapability(PlayerProgressionProvider.PROGRESSION);
                    MutableInt duration = new MutableInt(240);
                    capQuery.ifPresent(cap -> {
                        final IFaction alliedFaction = cap.getAlliedFaction();
                        if(alliedFaction != null && alliedFaction.is(new ResourceLocation("mna:fey"))) {
                            duration.setValue(720);
                        }
                    });

                    livingentity.addEffect(new MobEffectInstance(MobEffectsRegistry.RADIANT_RESOLVE.get(), duration.getValue()));
                }
            }
        }
    }
}
