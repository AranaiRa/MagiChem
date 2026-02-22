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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class BossTrophyBlock extends BaseEntityBlock {
    private static final VoxelShape
            DEMON_VOXEL_SHAPE_NORTH, DEMON_VOXEL_SHAPE_EAST, DEMON_VOXEL_SHAPE_SOUTH, DEMON_VOXEL_SHAPE_WEST,

            UNDEAD_VOXEL_SHAPE_BOWL_BODY, UNDEAD_VOXEL_SHAPE_BOWL_LIP, UNDEAD_VOXEL_SHAPE_SKULL, UNDEAD_VOXEL_SHAPE_LEG_BACK, UNDEAD_VOXEL_SHAPE_PLATE_BACK, UNDEAD_VOXEL_SHAPE_LEG1, UNDEAD_VOXEL_SHAPE_LEG2, UNDEAD_VOXEL_SHAPE_LEG3, UNDEAD_VOXEL_SHAPE_LEG4,
            UNDEAD_VOXEL_SHAPE_AGGREGATE_NORTH, UNDEAD_VOXEL_SHAPE_AGGREGATE_EAST, UNDEAD_VOXEL_SHAPE_AGGREGATE_SOUTH, UNDEAD_VOXEL_SHAPE_AGGREGATE_WEST;
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
        final Block block = pState.getBlock();

        if(block == BlockRegistry.BOSS_TROPHY_DEMONS.get()) {
            if (dir == Direction.NORTH) return DEMON_VOXEL_SHAPE_NORTH;
            else if (dir == Direction.EAST) return DEMON_VOXEL_SHAPE_EAST;
            else if (dir == Direction.SOUTH) return DEMON_VOXEL_SHAPE_SOUTH;
            else if (dir == Direction.WEST) return DEMON_VOXEL_SHAPE_WEST;
        }
        if(block == BlockRegistry.BOSS_TROPHY_UNDEAD.get()) {
            if (dir == Direction.NORTH) return UNDEAD_VOXEL_SHAPE_AGGREGATE_NORTH;
            else if (dir == Direction.EAST) return UNDEAD_VOXEL_SHAPE_AGGREGATE_EAST;
            else if (dir == Direction.SOUTH) return UNDEAD_VOXEL_SHAPE_AGGREGATE_SOUTH;
            else if (dir == Direction.WEST) return UNDEAD_VOXEL_SHAPE_AGGREGATE_WEST;
        }

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(pHand == InteractionHand.MAIN_HAND && pPlayer.hasEffect(EffectInit.CIRCLE_OF_POWER.get())) {
            final LazyOptional<IPlayerProgression> progressionCapability = pPlayer.getCapability(PlayerProgressionProvider.PROGRESSION);
            final Optional<IEnhancementCapability> enhancementCapability = EnhancementProvider.getCapability(pPlayer);
            final Block block = pState.getBlock();
            progressionCapability.ifPresent(pCap -> {
                enhancementCapability.ifPresent(eCap -> {
                    final IFaction alliedFaction = pCap.getAlliedFaction();
                    if(alliedFaction != null) {
                        if (block == BlockRegistry.BOSS_TROPHY_COUNCIL.get() && alliedFaction.is(FACTION_COUNCIL))
                            handleCouncilEffect(pLevel, pPos, pPlayer, eCap);
                        else if (block == BlockRegistry.BOSS_TROPHY_DEMONS.get() && alliedFaction.is(FACTION_DEMONS))
                            handleDemonsEffect(pLevel, pPos, pPlayer, eCap);
                        else if (block == BlockRegistry.BOSS_TROPHY_FEY.get() && alliedFaction.is(FACTION_FEY))
                            handleFeyEffect(pLevel, pPos, pPlayer, eCap);
                        else if (block == BlockRegistry.BOSS_TROPHY_UNDEAD.get() && alliedFaction.is(FACTION_UNDEAD))
                            handleUndeadEffect(pLevel, pPos, pPlayer, eCap);
                    }
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
        DEMON_VOXEL_SHAPE_NORTH = Block.box(2,0, 3, 14, 15, 13);
        DEMON_VOXEL_SHAPE_EAST = MathHelper.rotateVoxelShape(DEMON_VOXEL_SHAPE_NORTH, 1);
        DEMON_VOXEL_SHAPE_SOUTH = MathHelper.rotateVoxelShape(DEMON_VOXEL_SHAPE_NORTH, 2);
        DEMON_VOXEL_SHAPE_WEST = MathHelper.rotateVoxelShape(DEMON_VOXEL_SHAPE_NORTH, 3);

        UNDEAD_VOXEL_SHAPE_BOWL_BODY = Block.box(2,2,4.108, 14,6,14.5);
        UNDEAD_VOXEL_SHAPE_BOWL_LIP = Block.box(1.134,6,3.108, 14.866,7,15.5);
        UNDEAD_VOXEL_SHAPE_SKULL = Block.box(5,7.240,-1.778, 11,15.683,5.853);
        UNDEAD_VOXEL_SHAPE_PLATE_BACK = Block.box(5.5,6,0.107,10.5,7,3.107);
        UNDEAD_VOXEL_SHAPE_LEG_BACK = Block.box(7.5,0,0.107,8.5,6,5.107);
        UNDEAD_VOXEL_SHAPE_LEG1 = Block.box(0.634,0,8.804,3.634,10,9.804);
        UNDEAD_VOXEL_SHAPE_LEG2 = Block.box(3.884,0,12.835,6.25,10,15.933);
        UNDEAD_VOXEL_SHAPE_LEG3 = Block.box(9.75,0,12.835,12.156,10,15.933);
        UNDEAD_VOXEL_SHAPE_LEG4 = Block.box(12.366,0,8.804,15.366,10,9.804);

        UNDEAD_VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                UNDEAD_VOXEL_SHAPE_BOWL_BODY,
                UNDEAD_VOXEL_SHAPE_BOWL_LIP,
                UNDEAD_VOXEL_SHAPE_SKULL,
                UNDEAD_VOXEL_SHAPE_PLATE_BACK,
                UNDEAD_VOXEL_SHAPE_LEG_BACK,
                UNDEAD_VOXEL_SHAPE_LEG1,
                UNDEAD_VOXEL_SHAPE_LEG2,
                UNDEAD_VOXEL_SHAPE_LEG3,
                UNDEAD_VOXEL_SHAPE_LEG4
        );
        UNDEAD_VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_BOWL_BODY,1),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_BOWL_LIP,1),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_SKULL,1),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_PLATE_BACK,1),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG_BACK,1),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG1,1),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG2,1),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG3,1),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG4,1)
        );
        UNDEAD_VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_BOWL_BODY,2),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_BOWL_LIP,2),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_SKULL,2),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_PLATE_BACK,2),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG_BACK,2),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG1,2),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG2,2),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG3,2),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG4,2)
        );
        UNDEAD_VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_BOWL_BODY,3),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_BOWL_LIP,3),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_SKULL,3),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_PLATE_BACK,3),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG_BACK,3),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG1,3),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG2,3),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG3,3),
                MathHelper.rotateVoxelShape(UNDEAD_VOXEL_SHAPE_LEG4,3)
        );
    }
}
