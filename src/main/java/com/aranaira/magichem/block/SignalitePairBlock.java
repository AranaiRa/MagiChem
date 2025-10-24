package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.SignalitePairBlockEntity;
import com.aranaira.magichem.foundation.IHasNonStandardTooltipLore;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.items.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING_OMNI;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER;

public class SignalitePairBlock extends BaseEntityBlock implements IHasNonStandardTooltipLore {
    public static final VoxelShape
        VOXEL_SHAPE_CORE,
        VOXEL_SHAPE_NORTH, VOXEL_SHAPE_EAST, VOXEL_SHAPE_SOUTH, VOXEL_SHAPE_WEST, VOXEL_SHAPE_UP, VOXEL_SHAPE_DOWN,
        VOXEL_SHAPE_AGGREGATE;

    private final SignalitePairType type;
    private static final Random r = new Random();

    public SignalitePairBlock(Properties pProperties, SignalitePairType pType) {
        super(pProperties);
        type = pType;
    }

    public SignalitePairType getType(){
        return type;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SignalitePairBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof SignalitePairBlockEntity spbe) {

            if(pPlayer.getItemInHand(pHand).getItem() instanceof MateriaItem mi) {
                if(mi.getMateriaName().equals("permanence") && !spbe.locked) {
                    spbe.locked = true;
                    spbe.syncAndSave();

                    if(!pPlayer.isCreative()) {
                        pPlayer.getItemInHand(pHand).shrink(1);
                        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE, 1);
                        ItemEntity bottleEntity = new ItemEntity(pLevel, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), bottle);
                        pLevel.addFreshEntity(bottleEntity);
                    }

                    MateriaItem.generateSuccessParticles(pPos.getX(), pPos.getY(), pPos.getZ(), mi.getMateriaColor());
                }
                else if(mi.getMateriaName().equals("lies") && !spbe.hidden) {
                    spbe.hidden = true;
                    spbe.syncAndSave();

                    if(!pPlayer.isCreative()) {
                        pPlayer.getItemInHand(pHand).shrink(1);
                        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE, 1);
                        ItemEntity bottleEntity = new ItemEntity(pLevel, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), bottle);
                        pLevel.addFreshEntity(bottleEntity);
                    }

                    MateriaItem.generateSuccessParticles(pPos.getX(), pPos.getY(), pPos.getZ(), mi.getMateriaColor());
                }

                return InteractionResult.PASS;
            }

            if(spbe.locked)
                return InteractionResult.PASS;

            if(pState.getBlock() instanceof SignalitePairBlock spb && spb.getType() == SignalitePairType.LISTENING) {
                ItemStack query = pPlayer.getItemInHand(pHand);
                if (query.getItem() == ItemInit.RUNE_MARKING.get() && query.hasTag()) {
                    if (!pPlayer.level().isClientSide()) {
                        CompoundTag tagQuery = query.getTag();
                        if (tagQuery.contains("mark")) {
                            CompoundTag mark = tagQuery.getCompound("mark");
                            int x = mark.getInt("x");
                            int y = mark.getInt("y");
                            int z = mark.getInt("z");

                            spbe.setMonitoringTarget(new BlockPos(x, y, z));
                            MutableComponent out = Component.translatable("feedback.block.signalite_pair.target").append("(" + x + ", " + y + ", " + z + ")");
                            pPlayer.displayClientMessage(out, false);
                        }
                    }
                    return InteractionResult.CONSUME;
                }
            }

            if(!pLevel.isClientSide() && pHand == InteractionHand.MAIN_HAND) {
                double rawX = ((pHit.getLocation().x % 1) + 2) % 1;
                double rawY = ((pHit.getLocation().y % 1) + 2) % 1;
                double rawZ = ((pHit.getLocation().z % 1) + 2) % 1;

                int x = rawX > 0.625 ? 1 : rawX < 0.375 ? -1 : 0;
                int y = rawY > 0.625 ? 1 : rawY < 0.375 ? -1 : 0;
                int z = rawZ > 0.625 ? 1 : rawZ < 0.375 ? -1 : 0;

                Direction dir =
                        z < 0 ? Direction.NORTH :
                        z > 0 ? Direction.SOUTH :
                        x < 0 ? Direction.WEST :
                        x > 0 ? Direction.EAST :
                        y < 0 ? Direction.DOWN :
                        y > 0 ? Direction.UP :
                        null;

                boolean isCenter = (x == 0) && (y == 0) && (z == 0);
                if (!isCenter) {
                    pLevel.setBlock(pPos, pState.setValue(FACING_OMNI, dir), 3);
                    updateSignalStrength(pLevel, pPos);
                    pLevel.sendBlockUpdated(pPos, pState, pState.setValue(FACING_OMNI, dir), 3);
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return getType() == SignalitePairType.LISTENING;
    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        //Reminder, the direction in redstone signal requests are backwards
        if(pState.getBlock() instanceof SignalitePairBlock spb) {
            if(spb.getType() == SignalitePairType.LISTENING) {
                if(pDirection.getOpposite() == pState.getValue(FACING_OMNI)) {
                    int signal = pState.getValue(POWER);
                    return signal;
                }
            } else {
                //Singing Signalite can't transmit, only receive
                return 0;
            }
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return super.getDirectSignal(pState, pLevel, pPos, pDirection);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);

        updateSignalStrength(pLevel, pPos);
    }

    private void updateSignalStrength(Level pLevel, BlockPos pPos) {
        if(pLevel.getBlockState(pPos).getBlock() instanceof SignalitePairBlock spb &&
           pLevel.getBlockEntity(pPos) instanceof SignalitePairBlockEntity spbe) {

            if(spb.getType() == SignalitePairType.LISTENING)
                return;

            int signalStrength = 0;
            BlockState myState = pLevel.getBlockState(pPos);
            int oldSignalStrength = myState.getValue(POWER);
            Direction dir = spbe.getBlockState().getValue(FACING_OMNI);

            BlockPos posQuery = pPos.offset(dir.getNormal());
            BlockState stateToCheck = pLevel.getBlockState(posQuery);

            signalStrength = stateToCheck.getBlock().getSignal(stateToCheck, pLevel, posQuery, dir);

            if(signalStrength != oldSignalStrength) {
                pLevel.setBlock(pPos, myState.setValue(POWER, signalStrength), 3);
                pLevel.sendBlockUpdated(pPos, myState, myState.setValue(POWER, signalStrength), 2);
            }
        }
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);

        if(getType() == SignalitePairType.SINGING)
            updateSignalStrength(pLevel, pPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        final Direction clickedFace = pContext.getClickedFace();

        return defaultBlockState().setValue(FACING_OMNI, clickedFace.getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(POWER, FACING_OMNI);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlockEntitiesRegistry.SIGNALITE_PAIR_BE.get(),
                SignalitePairBlockEntity::tick);
    }

    static {
        VOXEL_SHAPE_CORE = Block.box(6, 6, 6, 10, 10, 10);

        VOXEL_SHAPE_NORTH = Block.box(7, 7, 0.5, 9, 9, 8);
        VOXEL_SHAPE_SOUTH = Block.box(7, 7, 8, 9, 9, 15.5);
        VOXEL_SHAPE_EAST  = Block.box(0.5, 7, 7, 8, 9, 9);
        VOXEL_SHAPE_WEST  = Block.box(8, 7, 7, 15.5, 9, 9);
        VOXEL_SHAPE_UP    = Block.box(7, 0.5, 7, 9, 8, 9);
        VOXEL_SHAPE_DOWN  = Block.box(7, 8, 7, 9, 15.5, 9);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_CORE,
                VOXEL_SHAPE_NORTH,
                VOXEL_SHAPE_EAST,
                VOXEL_SHAPE_SOUTH,
                VOXEL_SHAPE_WEST,
                VOXEL_SHAPE_UP,
                VOXEL_SHAPE_DOWN
        );
    }

    @Override
    public void addTooltipComponents(List<Component> pTooltipComponents) {
        if(this.asItem() == BlockRegistry.SIGNALITE_SINGING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.device")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.singing.line1")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.singing.line2")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(this.asItem() == BlockRegistry.SIGNALITE_LISTENING.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.device")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.listening.line1")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.signalite.listening.line2")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    public enum SignalitePairType {
        SINGING,
        LISTENING
    }
}
