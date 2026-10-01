package com.aranaira.magichem.util;

import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

public class WastePollutionUtils {
    static int getChunkRandomOffset() {
        int r = ChunkManhattanRange();
        if (r <= 0) return 0;
        int range = r * 2 + 1;
        return -r + (int) Math.floor(Math.random() * range);
    }
    static void gen(Level level, BlockPos pos, float amount, int repeat) {
        if (!Enabled()) return;
        int cx = SectionPos.blockToSectionCoord(pos.getX());
        int cz = SectionPos.blockToSectionCoord(pos.getZ());
        for (; repeat > 0; repeat--) {
            int ccx = cx + getChunkRandomOffset();
            int ccz = cz + getChunkRandomOffset();
            var chunk = level.getChunk(ccx, ccz);
            chunk.getCapability(ChunkMagicProvider.MAGIC).ifPresent(cm -> {
                cm.addResidualMagic(amount);
            });
        }
    }
    public static void GenOnManualClean(Level level, BlockPos pos, int repeat) {
        gen(level, pos, ResidualAmountManual(), repeat);
    }
    public static void GenOnActuatorNormal(Level level, BlockPos pos, int repeat) {
        gen(level, pos, ResidualAmountActuator(), repeat);
    }
    public static void GenOnActuatorRare(Level level, BlockPos pos, int repeat) {
        gen(level, pos, ResidualAmountActuatorRare(), repeat);
    }

    // stub for loading configs
    public static boolean Enabled() {
        return true;
    }
    public static int ChunkManhattanRange() {
        return 7;
    }
    public static float ResidualAmountManual() {
        return 20;
    }
    public static float ResidualAmountActuator() {
        return 10;
    }
    public static float ResidualAmountActuatorRare() {
        return 30;
    }
}
