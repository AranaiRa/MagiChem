package com.aranaira.magichem.item;

import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.math.Vector3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DebugOrbItem extends Item {
    public DebugOrbItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Player player = pContext.getPlayer();
        Level level = pContext.getLevel();
        if(!level.isClientSide()) {

            BlockEntity be = level.getBlockEntity(pContext.getClickedPos());

            if (be instanceof IShlorpReceiver isr) {

                Vector3 originPoint = new Vector3(player.getPosition(0).x, player.getPosition(0).y, player.getPosition(0).z);

                ShlorpEntity se = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), level);
                se.setPos(originPoint.x, originPoint.y, originPoint.z);

                se.configure(
                        originPoint,
                        new Vector3(0, 0, 0), new Vector3(0, 2, 0),
                        new Vector3(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ()),
                        new Vector3(0.5, 0.5, 0.5), new Vector3(0, 2, 0),
                        0.125f, 0.125f, 18,
                        ItemRegistry.getMateriaMap(false, true).get("admixture_metal"), 64
                );

                level.addFreshEntity(se);
                se.setPos(originPoint.x, originPoint.y, originPoint.z);
            }
        }

        return super.useOn(pContext);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {


        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
