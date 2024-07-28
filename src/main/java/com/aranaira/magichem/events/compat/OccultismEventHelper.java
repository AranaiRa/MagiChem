package com.aranaira.magichem.events.compat;

import com.klikli_dev.occultism.registry.OccultismBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

public class OccultismEventHelper {
    public static boolean isBlockSacrificialBowl(BlockState pTarget) {
        if(ModList.get().isLoaded("occultism")) {
            if(pTarget.getBlock() == OccultismBlocks.GOLDEN_SACRIFICIAL_BOWL.get()) {
                return true;
            }
        }
        return false;
    }
}
