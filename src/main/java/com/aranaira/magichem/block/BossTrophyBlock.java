package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.BossTrophyBlockEntity;
import com.aranaira.magichem.capabilities.enhancement.EnhancementProvider;
import com.aranaira.magichem.capabilities.enhancement.IEnhancementCapability;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.faction.DemonImp;
import com.mna.tools.SummonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class BossTrophyBlock extends BaseEntityBlock {
    private static final VoxelShape
        VOXEL_SHAPE_NORTH, VOXEL_SHAPE_EAST, VOXEL_SHAPE_SOUTH, VOXEL_SHAPE_WEST;
    private static final ResourceLocation FACTION_COUNCIL = new ResourceLocation("mna:council");
    private static final ResourceLocation FACTION_FEY = new ResourceLocation("mna:fey");
    private static final ResourceLocation FACTION_DEMONS = new ResourceLocation("mna:demons");
    private static final ResourceLocation FACTION_UNDEAD = new ResourceLocation("mna:undead");

    public BossTrophyBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        super.createBlockStateDefinition(pBuilder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if(state.getBlock() == BlockRegistry.BOSS_TROPHY_DEMONS.get()) return 15;

        return super.getLightEmission(state, level, pos);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        final Direction dir = pState.getValue(FACING);

        if(dir == Direction.NORTH) return VOXEL_SHAPE_NORTH;
        else if(dir == Direction.EAST) return VOXEL_SHAPE_EAST;
        else if(dir == Direction.SOUTH) return VOXEL_SHAPE_SOUTH;
        else if(dir == Direction.WEST) return VOXEL_SHAPE_WEST;

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(pHand == InteractionHand.MAIN_HAND) {
            final LazyOptional<IPlayerProgression> progressionCapability = pPlayer.getCapability(PlayerProgressionProvider.PROGRESSION);
            final Optional<IEnhancementCapability> enhancementCapability = EnhancementProvider.getCapability(pPlayer);
            final Block block = pState.getBlock();
            progressionCapability.ifPresent(pCap -> {
                enhancementCapability.ifPresent(eCap -> {
                    final IFaction alliedFaction = pCap.getAlliedFaction();
                    if (block == BlockRegistry.BOSS_TROPHY_COUNCIL.get() && alliedFaction.is(FACTION_COUNCIL)) handleCouncilEffect(pLevel, pPos, pPlayer, eCap);
                    else if (block == BlockRegistry.BOSS_TROPHY_DEMONS.get() && alliedFaction.is(FACTION_DEMONS)) handleDemonsEffect(pLevel, pPos, pPlayer, eCap);
                    else if (block == BlockRegistry.BOSS_TROPHY_FEY.get() && alliedFaction.is(FACTION_FEY)) handleFeyEffect(pLevel, pPos, pPlayer, eCap);
                    else if (block == BlockRegistry.BOSS_TROPHY_UNDEAD.get() && alliedFaction.is(FACTION_UNDEAD)) handleUndeadEffect(pLevel, pPos, pPlayer, eCap);
                });
            });
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    private void handleCouncilEffect(Level pLevel, BlockPos pPos, Player pPlayer, IEnhancementCapability pCap) {
        if(!pLevel.isClientSide()) {
            if (pLevel.getGameTime() >= pCap.getBossTrophyUseTargetTime()) {
                if (!pPlayer.hasEffect(MobEffectsRegistry.CHAINSPELL.get())) {
                    pPlayer.sendSystemMessage(Component.translatable("feedback.trophy.council.success"));
                    pPlayer.addEffect(new MobEffectInstance(
                            MobEffectsRegistry.CHAINSPELL.get(), -1, 0, false, false, true
                    ));
                    pCap.setBossTrophyUseTargetTime(pLevel.getGameTime() + 36000);
                }
            } else {
                pPlayer.sendSystemMessage(Component.translatable("feedback.trophy.council.failure"));
            }
        }
    }

    private void handleDemonsEffect(Level pLevel, BlockPos pPos, Player pPlayer, IEnhancementCapability pCap) {
        if (!pLevel.isClientSide()) {
            pPlayer.sendSystemMessage(Component.translatable("feedback.trophy.demons.success"));

            int existingImps = 0;
            boolean hasCommander = false;
            for (Mob mob : SummonUtils.getSummons(pPlayer)) {
                if(mob instanceof DemonImp imp) {
                    existingImps++;
                    if (imp.hasEffect(MobEffects.MOVEMENT_SPEED)) {
                        if (imp.hasEffect(EffectInit.ENLARGE.get())) {
                            if (imp.hasEffect(MobEffectsRegistry.GIGANTIC_VIGOR.get())) {
                                if (imp.hasEffect(MobEffectsRegistry.BRUTALITY.get()))
                                    hasCommander = true;
                            }
                        }
                    }
                }
            }

            for(int i=0; i<Math.max(0,4-existingImps); i++) {
                DemonImp imp = new DemonImp(EntityInit.DEMON_IMP.get(), pLevel);
                imp.setPos(pPlayer.blockPosition().getCenter());
                imp.setTier(2);
                SummonUtils.setSummon(imp, pPlayer, true, 0);
                pLevel.addFreshEntity(imp);
                if(i==0 && !hasCommander) {
                    imp.setCustomName(Component.translatable("entity.magichem.imp_legion_commander"));
                    imp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, false, false));
                    imp.addEffect(new MobEffectInstance(MobEffectsRegistry.GIGANTIC_VIGOR.get(), -1, 0, false, false));
                    imp.addEffect(new MobEffectInstance(EffectInit.ENLARGE.get(), -1, 3, false, false));
                    imp.addEffect(new MobEffectInstance(MobEffectsRegistry.BRUTALITY.get(), -1, 1, false, false));
                    imp.addEffect(new MobEffectInstance(MobEffects.HEAL, 3, 6, false, false));
                } else {
                    imp.setCustomName(Component.translatable("entity.magichem.imp_legion_subordinate"));
                }
            }
        }
    }

    private void handleFeyEffect(Level pLevel, BlockPos pPos, Player pPlayer, IEnhancementCapability pCap) {
        if (!pLevel.isClientSide()) {
            if(pLevel.getGameTime() >= pCap.getBossTrophyUseTargetTime()) {
                if (!pPlayer.hasEffect(MobEffectsRegistry.REGAL_TWILIGHT.get())) {
                    pPlayer.sendSystemMessage(Component.translatable("feedback.trophy.fey.success"));
                    pCap.setBossTrophyUseTargetTime(pLevel.getGameTime() + 36000);

                    pPlayer.addEffect(new MobEffectInstance(
                            MobEffectsRegistry.REGAL_TWILIGHT.get(), 36000, 0, false, false, true
                    ));
                }
            }
            else {
                pPlayer.sendSystemMessage(Component.translatable("feedback.trophy.fey.failure"));
            }
        }
    }

    private void handleUndeadEffect(Level pLevel, BlockPos pPos, Player pPlayer, IEnhancementCapability pCap) {
        if (!pLevel.isClientSide()) {
            if (pCap.hasLastDeathTargetLocation()) {

            } else {
                pPlayer.sendSystemMessage(Component.translatable("feedback.trophy.undead.failure"));
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BossTrophyBlockEntity(pPos, pState);
    }

    static {
        VOXEL_SHAPE_NORTH = Block.box(2,0, 3, 14, 15, 13);
        VOXEL_SHAPE_EAST = MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH, 1);
        VOXEL_SHAPE_SOUTH = MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH, 2);
        VOXEL_SHAPE_WEST = MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH, 3);
    }
}
