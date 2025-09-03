package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.blocks.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class EnchantedFertilizerItem extends Item {
    private static final int RADIUS = 4;
    private static final Random r = new Random();

    public EnchantedFertilizerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if(!level.isClientSide()) {
            int numGenerated = 0;

            for(int y=-RADIUS; y<=RADIUS; y++) {
                for(int x=-RADIUS; x<=RADIUS; x++) {
                    for (int z = -RADIUS; z <= RADIUS; z++) {
                        if(x*x + z*z <= RADIUS * RADIUS) {
                            BlockPos posQuery = new BlockPos(
                                    pContext.getClickedPos().getX()+x,
                                    pContext.getClickedPos().getY()+y,
                                    pContext.getClickedPos().getZ()+z
                            );
                            BlockState stateQuery = level.getBlockState(posQuery);
                            BlockState flower = null;
                            if(stateQuery.getBlock() == BlockRegistry.ENCHANTED_SOIL_DEPTHS.get()) {
                                flower = BlockInit.WAKEBLOOM.get().defaultBlockState();
                            } else if(stateQuery.getBlock() == BlockRegistry.ENCHANTED_SOIL_FOREST.get()) {
                                flower = BlockInit.AUM.get().defaultBlockState();
                            } else if(stateQuery.getBlock() == BlockRegistry.ENCHANTED_SOIL_PLAINS.get()) {
                                flower = BlockInit.CERUBLOSSOM.get().defaultBlockState();
                            } else if(stateQuery.getBlock() == BlockRegistry.ENCHANTED_SOIL_SWAMPS.get()) {
                                flower = BlockInit.TARMA_ROOT.get().defaultBlockState();
                            } else if(stateQuery.getBlock() == BlockRegistry.ENCHANTED_SOIL_WASTES.get()) {
                                flower = BlockInit.DESERT_NOVA.get().defaultBlockState();
                            }

                            if(flower != null && level.getBlockState(posQuery.above()).isAir()) {
                                float chance = Math.min(0.1f, (float)(x*x + z*z) / (float)(RADIUS * RADIUS));
                                if(r.nextFloat() <= chance) {
                                    numGenerated++;
                                    level.setBlock(posQuery.above(), flower, 3);
                                }
                            }
                        }
                    }
                }
            }

            if(numGenerated > 0) {
                pContext.getItemInHand().shrink(1);
                return InteractionResult.CONSUME;
            }
        }
        return super.useOn(pContext);
    }
}
