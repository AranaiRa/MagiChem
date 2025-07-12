package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.EldrinOrreryBlockEntity;
import com.aranaira.magichem.block.entity.routers.EldrinOrreryRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.enums.EldrinOrreryRouterType;
import com.aranaira.magichem.foundation.saveddata.EldrinOrreryLimiterSD;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.enums.EldrinOrreryRouterType.*;

public class EldrinOrreryBlock extends BaseEntityBlock {
    public EldrinOrreryBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EldrinOrreryBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof EldrinOrreryBlockEntity orrery) {
                NetworkHooks.openScreen((ServerPlayer)player, (EldrinOrreryBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("EldrinOrreryBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if(pContext.getPlayer() != null && !pContext.getLevel().isClientSide()) {

            BlockPos pos = pContext.getClickedPos();
            for(Pair<BlockPos, EldrinOrreryRouterType> posAndType : getRouterOffsets()) {
                if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                    return null;
                }
            }

            final EldrinOrreryLimiterSD eldrinOrreryData = pContext.getLevel().getServer().overworld().getDataStorage().computeIfAbsent(EldrinOrreryLimiterSD::load, EldrinOrreryLimiterSD::create, "eldrinOrreryData");
            if(!eldrinOrreryData.playerHasOrrery(pContext.getPlayer())) {
                eldrinOrreryData.addOrrery(pContext.getPlayer());
                return super.getStateForPlacement(pContext);
            }
            else
                pContext.getPlayer().sendSystemMessage(Component.translatable("feedback.block.eldrin_orrery.over_limit"));
        }
        return null;
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos) {
        for(Pair<BlockPos, EldrinOrreryRouterType> posAndType : getRouterOffsets()) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
    }

    public static List<Pair<BlockPos, EldrinOrreryRouterType>> getRouterOffsets() {
        List<Pair<BlockPos, EldrinOrreryRouterType>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);

            offsets.add(new Pair<>(origin.north(), NORTH));
            offsets.add(new Pair<>(origin.north().east(), NORTH_EAST));
            offsets.add(new Pair<>(origin.east(), EAST));
            offsets.add(new Pair<>(origin.south().east(), SOUTH_EAST));
            offsets.add(new Pair<>(origin.south(), SOUTH));
            offsets.add(new Pair<>(origin.south().west(), SOUTH_WEST));
            offsets.add(new Pair<>(origin.west(), WEST));
            offsets.add(new Pair<>(origin.north().west(), NORTH_WEST));
            offsets.add(new Pair<>(origin.above(), ABOVE));
            offsets.add(new Pair<>(origin.above().above(), DOUBLE_ABOVE));

        return offsets;
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.ELDRIN_ORRERY_ROUTER.get().defaultBlockState();

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Pair<BlockPos, EldrinOrreryRouterType> posAndType : getRouterOffsets()) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                int routerType = mapRouterTypeToInt(posAndType.getSecond());

                pLevel.setBlock(
                        targetPos,
                        state.setValue(MagiChemBlockStateProperties.ROUTER_TYPE_ELDRIN_ORRERY, routerType),
                        3);
                ((EldrinOrreryRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if(level.getBlockEntity(pos) instanceof EldrinOrreryBlockEntity orrery) {
                orrery.packInventoryToBlockItem();
                if(!level.isClientSide()) {
                    final EldrinOrreryLimiterSD eldrinOrreryData = level.getServer().overworld().getDataStorage().computeIfAbsent(EldrinOrreryLimiterSD::load, EldrinOrreryLimiterSD::create, "eldrinOrreryData");
                    eldrinOrreryData.removeOrrery(orrery.getPlacedBy().toString());
                    destroyRouters(level, pos);
                }

            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);

        if(pLevel.getBlockEntity(pPos) instanceof EldrinOrreryBlockEntity orrery) {
            if(pPlacer instanceof Player p) {
                orrery.setPlacedBy(p);
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.ELDRIN_ORRERY_BE.get(),
                EldrinOrreryBlockEntity::tick);
    }

    public static int mapRouterTypeToInt(EldrinOrreryRouterType pRouterType) {
        if(pRouterType == null)
            return 0;

        return switch(pRouterType) {
            case NORTH -> 1;
            case NORTH_EAST -> 2;
            case EAST -> 3;
            case SOUTH_EAST -> 4;
            case SOUTH -> 5;
            case SOUTH_WEST -> 6;
            case WEST -> 7;
            case NORTH_WEST -> 8;
            case ABOVE -> 9;
            case DOUBLE_ABOVE -> 10;
            default -> 0;
        };
    }

    public static EldrinOrreryRouterType unmapRouterTypeFromInt(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> NORTH;
            case 2 -> NORTH_EAST;
            case 3 -> EAST;
            case 4 -> SOUTH_EAST;
            case 5 -> SOUTH;
            case 6 -> SOUTH_WEST;
            case 7 -> WEST;
            case 8 -> NORTH_WEST;
            case 9 -> ABOVE;
            case 10 -> DOUBLE_ABOVE;
            default -> NONE;
        };
    }
}
