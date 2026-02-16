package com.aranaira.magichem.block;

import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class BossTrophyBlock extends Block {
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
        final LazyOptional<IPlayerProgression> capability = pPlayer.getCapability(PlayerProgressionProvider.PROGRESSION);
        capability.ifPresent(cap -> {
            final IFaction alliedFaction = cap.getAlliedFaction();
            if(alliedFaction.is(FACTION_COUNCIL)) handleCouncilEffect(pLevel, pPos, pPlayer);
            else if(alliedFaction.is(FACTION_DEMONS)) handleDemonsEffect(pLevel, pPos, pPlayer);
            else if(alliedFaction.is(FACTION_FEY)) handleFeyEffect(pLevel, pPos, pPlayer);
            else if(alliedFaction.is(FACTION_UNDEAD)) handleUndeadEffect(pLevel, pPos, pPlayer);
        });

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    private void handleCouncilEffect(Level pLevel, BlockPos pPos, Player pPlayer) {
        if(pLevel.isClientSide()) {
            //VFX
        } else {
            if(!pPlayer.hasEffect(MobEffectsRegistry.CHAINSPELL.get()))
                pPlayer.addEffect(new MobEffectInstance(
                        MobEffectsRegistry.CHAINSPELL.get(), -1, 0, false, true
                ));
        }
    }

    private void handleDemonsEffect(Level pLevel, BlockPos pPos, Player pPlayer) {

    }

    private void handleFeyEffect(Level pLevel, BlockPos pPos, Player pPlayer) {

    }

    private void handleUndeadEffect(Level pLevel, BlockPos pPos, Player pPlayer) {

    }

    static {
        VOXEL_SHAPE_NORTH = Block.box(2,0, 3, 14, 15, 13);
        VOXEL_SHAPE_EAST = MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH, 1);
        VOXEL_SHAPE_SOUTH = MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH, 2);
        VOXEL_SHAPE_WEST = MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH, 3);
    }
}
