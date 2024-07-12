package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.block.entity.routers.ActuatorFireRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.DistilleryRouterType;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import com.mna.items.base.INoCreativeTab;
import com.mna.spells.SpellsInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DistilleryRouterBlock extends BaseEntityBlock implements INoCreativeTab, ISpellInteractibleBlock<DistilleryRouterBlock> {
    public DistilleryRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    public static VoxelShape
            VOXEL_SHAPE_LEFT_PLUG_NORTH, VOXEL_SHAPE_LEFT_BODY_NORTH, VOXEL_SHAPE_LEFT_MOUNT_NORTH, VOXEL_SHAPE_LEFT_FURNACE_NORTH,
            VOXEL_SHAPE_LEFT_TANK_NORTH, VOXEL_SHAPE_LEFT_PIPE_NORTH,

            VOXEL_SHAPE_ABOVE_PIPE_LEFT_NORTH, VOXEL_SHAPE_ABOVE_PIPE_RIGHT_NORTH, VOXEL_SHAPE_ABOVE_PIPE_TOP_NORTH,
            VOXEL_SHAPE_ABOVE_TANK_NORTH,

            VOXEL_SHAPE_ABOVE_LEFT_TANK_NORTH, VOXEL_SHAPE_ABOVE_LEFT_PIPE_FRONT_NORTH, VOXEL_SHAPE_ABOVE_LEFT_PIPE_BACK_NORTH,
            VOXEL_SHAPE_ABOVE_LEFT_PIPE_TOP_NORTH,

            VOXEL_SHAPE_LEFT_AGGREGATE_NORTH, VOXEL_SHAPE_ABOVE_AGGREGATE_NORTH, VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_NORTH,
            VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH, VOXEL_SHAPE_ABOVE_AGGREGATE_SOUTH, VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_SOUTH,
            VOXEL_SHAPE_LEFT_AGGREGATE_EAST, VOXEL_SHAPE_ABOVE_AGGREGATE_EAST, VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_EAST,
            VOXEL_SHAPE_LEFT_AGGREGATE_WEST, VOXEL_SHAPE_ABOVE_AGGREGATE_WEST, VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_WEST;
    private static final float FUZZ_FACTOR = 0.000625f;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DistilleryRouterBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        DistilleryRouterBlockEntity drbe = (DistilleryRouterBlockEntity) pLevel.getBlockEntity(pPos);
        if(drbe != null) {
            DistilleryRouterType routerType = drbe.getRouterType();
            Direction facing = drbe.getFacing();

            //Again, switch statements always default here and I have no idea why
            if(routerType == DistilleryRouterType.PLUG_LEFT) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_LEFT_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_LEFT_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_LEFT_AGGREGATE_WEST;
            } else if(drbe.getRouterType() == DistilleryRouterType.ABOVE) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_ABOVE_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_ABOVE_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_ABOVE_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_ABOVE_AGGREGATE_WEST;
            } else if(drbe.getRouterType() == DistilleryRouterType.ABOVE_LEFT) {
                if(facing == Direction.NORTH) return VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_NORTH;
                else if(facing == Direction.EAST) return VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_EAST;
                else if(facing == Direction.SOUTH) return VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_SOUTH;
                else if(facing == Direction.WEST) return VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_WEST;
            }
        }
        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof DistilleryRouterBlockEntity drbe) {
            DistilleryBlockEntity master = drbe.getMaster();
            pPlayer.swing(InteractionHand.MAIN_HAND);
            return master.getBlockState().getBlock().use(master.getBlockState(), pLevel, master.getBlockPos(), pPlayer, pHand, pHit);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    static {
        VOXEL_SHAPE_LEFT_BODY_NORTH = Block.box(0 + FUZZ_FACTOR, 0, 2, 16, 8, 14);
        VOXEL_SHAPE_LEFT_PLUG_NORTH = Block.box(0 + FUZZ_FACTOR, 0 + FUZZ_FACTOR, 0 + FUZZ_FACTOR, 4, 16 - FUZZ_FACTOR, 16 - FUZZ_FACTOR);
        VOXEL_SHAPE_LEFT_TANK_NORTH = Block.box(5, 13, 4, 13, 16, 12);
        VOXEL_SHAPE_LEFT_MOUNT_NORTH = Block.box(4, 8, 5, 14, 12, 11);
        VOXEL_SHAPE_LEFT_FURNACE_NORTH = Block.box(10, 0, 1, 16, 10, 15);
        VOXEL_SHAPE_LEFT_PIPE_NORTH = Block.box(14, 10, 9, 16, 16, 11);

        VOXEL_SHAPE_LEFT_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_LEFT_BODY_NORTH,
                VOXEL_SHAPE_LEFT_PLUG_NORTH,
                VOXEL_SHAPE_LEFT_TANK_NORTH,
                VOXEL_SHAPE_LEFT_MOUNT_NORTH,
                VOXEL_SHAPE_LEFT_FURNACE_NORTH,
                VOXEL_SHAPE_LEFT_PIPE_NORTH
        );

        VOXEL_SHAPE_LEFT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PLUG_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_TANK_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_MOUNT_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_FURNACE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PIPE_NORTH, 1)
        );

        VOXEL_SHAPE_LEFT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PLUG_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_TANK_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_MOUNT_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_FURNACE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PIPE_NORTH, 2)
        );

        VOXEL_SHAPE_LEFT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PLUG_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_TANK_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_MOUNT_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_FURNACE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LEFT_PIPE_NORTH, 3)
        );

        VOXEL_SHAPE_ABOVE_TANK_NORTH = Block.box(3, 0, 6, 7, 6, 10);
        VOXEL_SHAPE_ABOVE_PIPE_TOP_NORTH = Block.box(4, 6, 7, 6, 8, 9);
        VOXEL_SHAPE_ABOVE_PIPE_LEFT_NORTH = Block.box(0, 0, 5, 2, 6, 7);
        VOXEL_SHAPE_ABOVE_PIPE_RIGHT_NORTH = Block.box(7, 0, 7, 10, 4, 9);

        VOXEL_SHAPE_ABOVE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_ABOVE_TANK_NORTH,
                VOXEL_SHAPE_ABOVE_PIPE_TOP_NORTH,
                VOXEL_SHAPE_ABOVE_PIPE_LEFT_NORTH,
                VOXEL_SHAPE_ABOVE_PIPE_RIGHT_NORTH
        );

        VOXEL_SHAPE_ABOVE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_TANK_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_PIPE_TOP_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_PIPE_LEFT_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_PIPE_RIGHT_NORTH, 1)
        );

        VOXEL_SHAPE_ABOVE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_TANK_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_PIPE_TOP_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_PIPE_LEFT_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_PIPE_RIGHT_NORTH, 2)
        );

        VOXEL_SHAPE_ABOVE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_TANK_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_PIPE_TOP_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_PIPE_LEFT_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_PIPE_RIGHT_NORTH, 3)
        );

        VOXEL_SHAPE_ABOVE_LEFT_TANK_NORTH = Block.box(5,0,4,13,11,12);
        VOXEL_SHAPE_ABOVE_LEFT_PIPE_TOP_NORTH = Block.box(8,11,7,10,14,9);
        VOXEL_SHAPE_ABOVE_LEFT_PIPE_BACK_NORTH = Block.box(12,4,5,16,6,7);
        VOXEL_SHAPE_ABOVE_LEFT_PIPE_FRONT_NORTH = Block.box(12,0,9,16,2,11);

        VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_ABOVE_LEFT_TANK_NORTH,
                VOXEL_SHAPE_ABOVE_LEFT_PIPE_TOP_NORTH,
                VOXEL_SHAPE_ABOVE_LEFT_PIPE_BACK_NORTH,
                VOXEL_SHAPE_ABOVE_LEFT_PIPE_FRONT_NORTH
        );

        VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_TANK_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_PIPE_TOP_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_PIPE_BACK_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_PIPE_FRONT_NORTH, 1)
        );

        VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_TANK_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_PIPE_TOP_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_PIPE_BACK_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_PIPE_FRONT_NORTH, 2)
        );

        VOXEL_SHAPE_ABOVE_LEFT_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_TANK_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_PIPE_TOP_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_PIPE_BACK_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_ABOVE_LEFT_PIPE_FRONT_NORTH, 3)
        );
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.DISTILLERY.get());
    }

    @Override
    public boolean onHitBySpell(Level level, BlockPos blockPos, ISpellDefinition iSpellDefinition) {
        for(IModifiedSpellPart isp : iSpellDefinition.getComponents()){
            if(isp.getPart().equals(Components.FIRE_DAMAGE)) {
                float damage = isp.getValue(Attribute.DAMAGE);
                float duration = isp.getValue(Attribute.DURATION);
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof DistilleryRouterBlockEntity drbe) {
                    BlockEntity mbe = level.getBlockEntity(drbe.getMasterPos());
                    if(mbe instanceof DistilleryBlockEntity dbe) {
                        dbe.setHeat(Math.round(damage * duration * 20));
                        return true;
                    }
                }
            }
        }
        return false;
    }
}