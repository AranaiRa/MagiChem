package com.aranaira.magichem.item;

import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.item.renderer.MateriaVesselItemRenderer;
import com.aranaira.magichem.item.renderer.SublimationPrimerItemRenderer;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.items.base.IRadialMenuItem;
import com.mna.items.renderers.books.AlterationBookRenderer;
import com.mna.tools.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;

import java.util.function.Consumer;

public class SublimationPrimerItem extends Item implements IRadialMenuItem {
    public SublimationPrimerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(() -> new SublimationPrimerItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()));

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer.get();
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
