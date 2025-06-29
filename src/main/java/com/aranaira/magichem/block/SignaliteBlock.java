package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.SignaliteBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.LEVER_SIGNAL;
import static net.minecraft.world.level.block.RedstoneTorchBlock.LIT;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED;

public class SignaliteBlock extends BaseEntityBlock {
    public static final VoxelShape
        VOXEL_SHAPE_CORE,
        VOXEL_SHAPE_NORTH, VOXEL_SHAPE_EAST, VOXEL_SHAPE_SOUTH, VOXEL_SHAPE_WEST, VOXEL_SHAPE_UP, VOXEL_SHAPE_DOWN,
        VOXEL_SHAPE_AGGREGATE;

    private final SignaliteBlockType type;
    private static final Random r = new Random();

    public SignaliteBlock(Properties pProperties, SignaliteBlockType pType) {
        super(pProperties);
        type = pType;
    }

    public SignaliteBlockType getType(){
        return type;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SignaliteBlockEntity(pPos, pState);
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
        if(be instanceof SignaliteBlockEntity sbe) {

            if(pPlayer.getItemInHand(pHand).getItem() instanceof MateriaItem mi) {
                if(mi.getMateriaName().equals("permanence") && !sbe.locked) {
                    sbe.locked = true;
                    sbe.syncAndSave();

                    if(!pPlayer.isCreative()) {
                        pPlayer.getItemInHand(pHand).shrink(1);
                        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE, 1);
                        ItemEntity bottleEntity = new ItemEntity(pLevel, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), bottle);
                        pLevel.addFreshEntity(bottleEntity);
                    }

                    MateriaItem.generateSuccessParticles(pPos.getX(), pPos.getY(), pPos.getZ(), mi.getMateriaColor());
                }
                else if(mi.getMateriaName().equals("lies") && !sbe.hidden) {
                    sbe.hidden = true;
                    sbe.syncAndSave();

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

            if(sbe.locked)
                return InteractionResult.PASS;

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
                        y > 0 ? Direction.DOWN :
                        y < 0 ? Direction.UP :
                        null;

                boolean isCenter = (x == 0) && (y == 0) && (z == 0);
                if (isCenter) {
                    if (type == SignaliteBlockType.DEVOURING || type == SignaliteBlockType.EQUATING || type == SignaliteBlockType.GATEKEEPING) {
                        if (pPlayer.isCrouching()) sbe.decrementSpecialSignalSetting(15);
                        else sbe.incrementSpecialSignalSetting(15);

                        if(!pPlayer.level().isClientSide()) {
                            MutableComponent text = Component.empty()
                                    .append(Component.translatable("feedback.block.signalite.target"))
                                    .append(Component.literal("" + sbe.specialSignalTarget).withStyle(ChatFormatting.BOLD, ChatFormatting.RED))
                                    .append(".");
                            pPlayer.sendSystemMessage(text);
                        }
                    }
                    if (type == SignaliteBlockType.METICULOUS) {
                        if (pPlayer.isCrouching()) sbe.decrementSpecialSignalSetting(4);
                        else sbe.incrementSpecialSignalSetting(4);

                        if(!pPlayer.level().isClientSide()) {
                            MutableComponent text = Component.empty()
                                    .append(Component.translatable(sbe.specialSignalTarget == 0 ? "feedback.block.signalite.no_bit_target" : "feedback.block.signalite.bit_target"))
                                    .append(Component.literal("" + (sbe.specialSignalTarget == 0 ? "" : sbe.specialSignalTarget)).withStyle(ChatFormatting.BOLD, ChatFormatting.RED))
                                    .append(".");
                            pPlayer.sendSystemMessage(text);
                        }
                    }
                } else if(!pPlayer.isCrouching()) {
                    sbe.toggle(dir);
                    updateSignalStrength(pLevel, pPos);
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return super.isSignalSource(pState);
    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        //Reminder, the direction in redstone signal requests are backwards
        if(be instanceof SignaliteBlockEntity sbe) {
            SignaliteBlockType type = SignaliteBlockType.STANDARD;
            if(pState.getBlock() instanceof SignaliteBlock sb)
                type = sb.getType();

            int signalStrength = pState.getValue(POWER);
            //We don't want signals cross-polluting if we're doing math on inputs
            if(type == SignaliteBlockType.AGGREGATING || type == SignaliteBlockType.BURNISHING || type == SignaliteBlockType.METICULOUS)
                signalStrength = 0;

            if(pDirection == Direction.NORTH && sbe.connectedSouth)
                return signalStrength;
            if(pDirection == Direction.SOUTH && sbe.connectedNorth)
                return signalStrength;
            if(pDirection == Direction.EAST && sbe.connectedWest)
                return signalStrength;
            if(pDirection == Direction.WEST && sbe.connectedEast)
                return signalStrength;
            if(pDirection == Direction.UP && sbe.connectedDown)
                return signalStrength;
            if(pDirection == Direction.DOWN && sbe.connectedUp)
                return signalStrength;

            if(pState.getBlock() instanceof SignaliteBlock sb) {
                if(sb.getType() != SignaliteBlockType.STANDARD) {
                    if(pDirection == Direction.NORTH && sbe.specialSouth)
                        return sbe.specialSignalStrength;
                    if(pDirection == Direction.SOUTH && sbe.specialNorth)
                        return sbe.specialSignalStrength;
                    if(pDirection == Direction.EAST && sbe.specialWest)
                        return sbe.specialSignalStrength;
                    if(pDirection == Direction.WEST && sbe.specialEast)
                        return sbe.specialSignalStrength;
                    if(pDirection == Direction.UP && sbe.specialDown)
                        return sbe.specialSignalStrength;
                    if(pDirection == Direction.DOWN && sbe.specialUp)
                        return sbe.specialSignalStrength;
                }
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

    public static void updateSignalStrength(Level pLevel, BlockPos pPos) {
        if(pLevel.getBlockEntity(pPos) instanceof SignaliteBlockEntity sbe) {
            int signalStrength = 0;
            BlockState myState = pLevel.getBlockState(pPos);
            SignaliteBlockType myType = ((SignaliteBlock) myState.getBlock()).getType();
            int oldSignalStrength = myState.getValue(POWER);
            sbe.clearLastInputSignals();
            boolean changed = false;

            for (Direction dir : sbe.getTransmittingDirections()) {
                if(myType != SignaliteBlockType.STANDARD && sbe.isTransmittingDirectionOneWay(dir)) {
                    continue;
                }

                BlockPos posQuery = pPos.offset(dir.getNormal());
                BlockState stateToCheck = pLevel.getBlockState(posQuery);

                int signalQuery = 0;
                if(stateToCheck.getBlock() == BlockRegistry.SIGNALITE_LISTENING.get() || stateToCheck.getBlock() == BlockRegistry.SIGNALITE_SEER.get()) {
                    signalQuery = stateToCheck.getBlock().getSignal(stateToCheck, pLevel, posQuery, dir);
                    sbe.setLastInputByDirection(dir, signalQuery + 1);
                }
                else {
                    signalQuery = stateToCheck.getBlock().getSignal(stateToCheck, pLevel, posQuery, dir);
                    sbe.setLastInputByDirection(dir, signalQuery);
                }

                if(stateToCheck.getBlock() == BlockRegistry.SIGNALITE.get() || stateToCheck.getBlock() == Blocks.REDSTONE_WIRE) {
                    signalQuery = Math.max(0, signalQuery - 1);
                }

                if (signalQuery > signalStrength) {
                    signalStrength = signalQuery;
                }
            }

            if(myState.getBlock() instanceof SignaliteBlock sb) {
                if(sb.getType() == SignaliteBlockType.AGGREGATING) {
                    sbe.specialSignalStrength = sbe.getLastInputSum();
                }
                else if(sb.getType() == SignaliteBlockType.BURNISHING) {
                    sbe.specialSignalStrength = sbe.getLastInputAverage();
                }
                else if(sb.getType() == SignaliteBlockType.CHAOTIC) {
                    sbe.specialSignalStrength = signalStrength == 0 ? 0 : 1 + Math.round(r.nextFloat() * signalStrength);
                }
                else if(sb.getType() == SignaliteBlockType.DEVOURING) {
                    sbe.specialSignalStrength = Math.min(signalStrength, sbe.specialSignalTarget);
                }
                else if(sb.getType() == SignaliteBlockType.EQUATING) {
                    sbe.specialSignalStrength = signalStrength == sbe.specialSignalTarget ? 15 : 0;
                }
                else if(sb.getType() == SignaliteBlockType.GATEKEEPING) {
                    sbe.specialSignalStrength = signalStrength >= sbe.specialSignalTarget ? signalStrength : 0;
                }
                else if(sb.getType() == SignaliteBlockType.METICULOUS) {
                    if(sbe.specialSignalTarget > 0) {
                        int filter = 1 << sbe.specialSignalTarget - 1;
                        boolean matches = filter == (signalStrength & filter);
                        sbe.specialSignalStrength = matches ? 15 : 0;
                    } else
                        sbe.specialSignalStrength = 0;
                }
                else if(sb.getType() == SignaliteBlockType.NEGATING) {
                    sbe.specialSignalStrength = 15 - signalStrength;
                }
            }

            if(signalStrength != oldSignalStrength) {
                pLevel.setBlock(pPos, myState.setValue(POWER, Math.min(15, signalStrength)), 3);
                pLevel.sendBlockUpdated(pPos, myState, myState.setValue(POWER, Math.min(15, signalStrength)), 2);
            }

            sbe.syncAndSave();
        }
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);

        updateSignalStrength(pLevel, pPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(POWER);
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

    public enum SignaliteBlockType {
        STANDARD,
        AGGREGATING,
        BURNISHING,
        CHAOTIC,
        DEVOURING,
        EQUATING,
        GATEKEEPING,
        METICULOUS,
        NEGATING
    }
}
