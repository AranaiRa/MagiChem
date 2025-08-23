package com.aranaira.magichem.util;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Random;

public class InteropUtil {
    private static final TagKey<Block>
            TAG_MAGICHEM_COPPER_EXPOSED = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "copper_exposed")),
            TAG_MAGICHEM_COPPER_WEATHERED = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "copper_weathered")),
            TAG_MAGICHEM_COPPER_OXIDIZED = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "copper_oxidized"));
    private static final Random r = new Random();

    public static void tryGenerateVerdigris(Level level, BlockPos pos, BlockHitResult result) {
        BlockState targetState = level.getBlockState(pos);

        int chance = 0, additional = 0;
        if(targetState.is(TAG_MAGICHEM_COPPER_OXIDIZED)) {
            chance = 45;
            additional = r.nextInt(3);
        }
        else if(targetState.is(TAG_MAGICHEM_COPPER_WEATHERED)) {
            chance = 25;
            additional = r.nextInt(2);
        }
        else if(targetState.is(TAG_MAGICHEM_COPPER_EXPOSED)) {
            chance = 10;
        }

        if(r.nextInt(100) < chance) {
            ItemStack verdigris = new ItemStack(ItemRegistry.VERDIGRIS.get(), 1 + additional);
            ItemEntity ie = new ItemEntity(level,
                    result.getLocation().x, result.getLocation().y, result.getLocation().z,
                    verdigris);
            level.addFreshEntity(ie);
        }
    }
}
