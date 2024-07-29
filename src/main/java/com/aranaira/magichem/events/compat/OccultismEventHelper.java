package com.aranaira.magichem.events.compat;

import com.aranaira.magichem.item.compat.occultism.OccultRitualTalismanItem;
import com.aranaira.magichem.registry.compat.OccultismItemRegistry;
import com.klikli_dev.occultism.registry.OccultismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.ModList;

public class OccultismEventHelper {

    public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        BlockState targetState = event.getLevel().getBlockState(event.getPos());

        event.setCanceled(OccultismEventHelper.handleRitualTalismanOnSacrificialBowl(event.getLevel(), event.getEntity(), event.getPos(), targetState, stack));
    }

    public static boolean handleRitualTalismanOnSacrificialBowl(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pTarget, ItemStack pStack) {
//        if(pTarget.getBlock() == OccultismBlocks.GOLDEN_SACRIFICIAL_BOWL.get()) {
//            if(pStack.getItem() == OccultismItemRegistry.OCCULT_RITUAL_TALISMAN.get()) {
//                OccultRitualTalismanItem.storePentacleInTalisman(pLevel, pPlayer, pStack, pPos, pLevel.getBlockState(pPos));
//                return true;
//            }
//        }
        return false;
    }
}
