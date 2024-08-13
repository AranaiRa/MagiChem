package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import com.mna.spells.SpellsInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.HAS_PASSIVE_HEAT;

public class AlembicBlock extends BaseEntityBlock implements ISpellInteractibleBlock<AlembicBlock> {
    public AlembicBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_PASSIVE_HEAT, false)
        );
    }

    private static final VoxelShape VOXEL_SHAPE = Block.box(4,0,4,12,10,12);
    public static final TagKey<Block> PASSIVE_HEAT_TAG = BlockTags.create(new ResourceLocation("minecraft", "alembic_passive_heat_source"));

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return VOXEL_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState state = this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
        BlockState below = pContext.getLevel().getBlockState(pContext.getClickedPos().below());

        if(below.getBlockHolder().is(PASSIVE_HEAT_TAG))
            state = state.setValue(HAS_PASSIVE_HEAT, true);

        return state;
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return super.mirror(pState, pMirror);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return super.rotate(pState, pRotation);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, HAS_PASSIVE_HEAT);
    }

    /* BLOCK ENTITY STUFF BELOW THIS POINT*/

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof AlembicBlockEntity) {
                ((AlembicBlockEntity) blockEntity).packInventoryToBlockItem();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void onNeighborChange(BlockState pState, LevelReader pLevelReader, BlockPos pPos, BlockPos pNeighborPos) {
        boolean hasPassiveHeat = false;

        Stream<TagKey<Block>> tags = pLevelReader.getBlockState(pPos.below()).getBlockHolder().tags();

        if (pLevelReader.getBlockState(pPos.below()).getBlockHolder().is(new ResourceLocation("minecraft:alembic_passive_heat_source"))) {
            hasPassiveHeat = true;
        }

        pState = pState.setValue(HAS_PASSIVE_HEAT, hasPassiveHeat);
        super.onNeighborChange(pState, pLevelReader, pPos, pNeighborPos);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
        boolean hasPassiveHeat = false;

        for(TagKey<Block> key : pLevel.getBlockState(pPos.below()).getBlockHolder().getTagKeys().collect(Collectors.toList())) {
            ResourceLocation location = key.location();

            int a = 0;
        }

        if (pLevel.getBlockState(pPos.below()).getBlockHolder().is(PASSIVE_HEAT_TAG)) {
            hasPassiveHeat = true;
        }

        pState = pState.setValue(HAS_PASSIVE_HEAT, hasPassiveHeat);
        pLevel.setBlock(pPos, pState, 3);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            boolean holdingCleaningBrush = player.getInventory().getSelected().getItem() == ItemRegistry.CLEANING_BRUSH.get();

            if(!holdingCleaningBrush) {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof AlembicBlockEntity) {
                    NetworkHooks.openScreen((ServerPlayer) player, (AlembicBlockEntity) entity, pos);
                } else {
                    throw new IllegalStateException("AlembicBlockEntity container provider is missing!");
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlembicBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.ALEMBIC_BE.get(),
                AlembicBlockEntity::tick);
    }

    @Override
    public boolean onHitBySpell(Level level, BlockPos blockPos, ISpellDefinition iSpellDefinition) {
        for(IModifiedSpellPart isp : iSpellDefinition.getComponents()){
            if(isp.getPart().equals(Components.FIRE_DAMAGE)) {
                float damage = isp.getValue(Attribute.DAMAGE);
                float duration = isp.getValue(Attribute.DURATION);
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof AlembicBlockEntity abe) {
                    abe.setHeat(Math.round(damage * duration * 20));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }
}
