package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class HeftyHopperBlockEntity extends BlockEntity {
    public HeftyHopperBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.HEFTY_HOPPER_BE.get(), pPos, pBlockState);
    }

    public static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pState, T t) {
        if(!pLevel.isClientSide() && t instanceof HeftyHopperBlockEntity entity && pLevel.getGameTime() % 10 == 0) {
            final Direction facing = pState.getValue(MagiChemBlockStateProperties.FACING_OMNI);
            final Vec3i fwd = facing.getNormal();

            final BlockEntity beSuck = pLevel.getBlockEntity(pPos.offset(fwd));
            final BlockEntity bePush = pLevel.getBlockEntity(pPos.offset(fwd.multiply(-1)));

            if(beSuck != null && bePush != null) {
                final LazyOptional<IItemHandler> suckCapability = beSuck.getCapability(ForgeCapabilities.ITEM_HANDLER);
                final LazyOptional<IItemHandler> pushCapability = bePush.getCapability(ForgeCapabilities.ITEM_HANDLER);

                suckCapability.ifPresent(suck -> {
                    pushCapability.ifPresent(push -> {
                        ItemStack querySuck = ItemStack.EMPTY;
                        int querySuckIndex = -1;
                        for(int i=0; i<suck.getSlots(); i++) {
                            if(!suck.getStackInSlot(i).isEmpty()) {
                                querySuckIndex = i;
                                querySuck = suck.extractItem(i, 64, true);
                                break;
                            }
                        }
                        int initial = querySuck.getCount();

                        if(querySuckIndex > -1 && querySuck.getCount() > 0) {
                            int extracted = 0;
                            for(int i=0;i<push.getSlots();i++) {
                                ItemStack queryPush = push.insertItem(i, querySuck, false);
                                if(queryPush.isEmpty()) {
                                    ItemStack extractedStack = suck.extractItem(i, suck.getStackInSlot(i).getMaxStackSize(), false);
                                    extracted += extractedStack.getCount();

                                    if(extracted >= initial)
                                        break;
                                } else {
                                    querySuck = queryPush;
                                }
                            }
                            if(extracted > 0) {
                                suck.extractItem(querySuckIndex, extracted, false);
                            }
                        }
                    });
                });
            }
        }
    }
}
