package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.block.entity.PowerSpikeBlockEntity;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING_OMNI;

public class PowerSpikeBlock extends BaseEntityBlock {
    public PowerSpikeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING_OMNI, Direction.UP)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_DOWN = Block.box(7,13,7,9,16,9),
            VOXEL_SHAPE_UP = Block.box(7,0,7,9,3,9),
            VOXEL_SHAPE_NORTH = Block.box(7,7,13,9,9,16),
            VOXEL_SHAPE_SOUTH = Block.box(7,7,0,9,9,3),
            VOXEL_SHAPE_EAST = Block.box(0,7,7,3,9,9),
            VOXEL_SHAPE_WEST = Block.box(13,7,7,16,9,9);

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        return super.rotate(state, level, pos, direction);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return super.mirror(state, mirror);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        switch(state.getValue(FACING_OMNI)) {
            case UP: return VOXEL_SHAPE_UP;
            case NORTH: return VOXEL_SHAPE_NORTH;
            case SOUTH: return VOXEL_SHAPE_SOUTH;
            case EAST: return VOXEL_SHAPE_EAST;
            case WEST: return VOXEL_SHAPE_WEST;
            default: return VOXEL_SHAPE_DOWN;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction dir = context.getClickedFace();
        Vec3i targetCoords = new Vec3i(dir.getOpposite().getStepX(),dir.getOpposite().getStepY(),dir.getOpposite().getStepZ());
        BlockState state = context.getLevel().getBlockState(context.getClickedPos().offset(targetCoords));

        return state.is(this) && state.getValue(FACING_OMNI) == dir
                ? this.defaultBlockState().setValue(FACING_OMNI, dir.getOpposite())
                : this.defaultBlockState().setValue(FACING_OMNI, dir);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING_OMNI);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pBuilder) {
        ItemStack stack = new ItemStack(BlockRegistry.POWER_SPIKE.get(), 1);
        PowerSpikeBlockEntity psbe = (PowerSpikeBlockEntity) pBuilder.getParameter(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> output = new ArrayList<>();
        BlockPos pos = psbe.getPowerDrawPos();

        if(pos != null) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("magichem.powerspike.targetpos", pos.asLong());
            stack.setTag(tag);
        }

        output.add(stack);

        return output;
    }

    /* BLOCK ENTITY STUFF BELOW THIS POINT*/

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PowerSpikeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.POWER_SPIKE_BE.get(),
                PowerSpikeBlockEntity::tick);
    }
}
