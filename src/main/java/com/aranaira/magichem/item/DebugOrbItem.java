package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.enums.ShlorpParticleMode;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.math.Vector3;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

                if(be instanceof AbstractMateriaStorageBlockEntity amsbe) {

                    if(amsbe.getMateriaType() == null)
                        return super.useOn(pContext);

                    ShlorpEntity se = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), level);
                    se.setPos(originPoint.x, originPoint.y, originPoint.z);

                    se.configure(
                            originPoint,
                            new Vector3(0, 0, 0), new Vector3(0, 2, 0),
                            new Vector3(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ()),
                            new Vector3(0.5, 0.5, 0.5), new Vector3(0, 2, 0),
                            0.125f, 0.125f, 18,
                            amsbe.getMateriaType(), 400, ShlorpParticleMode.NONE
                    );

                    level.addFreshEntity(se);
                    se.setPos(originPoint.x, originPoint.y, originPoint.z);
                }
            }
        }

        return super.useOn(pContext);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {


        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.debugorb")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
