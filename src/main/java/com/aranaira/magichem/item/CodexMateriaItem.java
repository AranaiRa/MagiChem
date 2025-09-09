package com.aranaira.magichem.item;

import com.aranaira.magichem.block.LecternWithCodexMateriaBlock;
import com.aranaira.magichem.gui.CodexMateriaMenu;
import com.aranaira.magichem.gui.WisdomMenu;
import com.aranaira.magichem.item.renderer.mna.CodexMateriaItemRenderer;
import com.aranaira.magichem.item.renderer.mna.SublimationPrimerItemRenderer;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.api.blocks.WizardLabBlock;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.BookStandBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CodexMateriaItem extends Item {
    public CodexMateriaItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(() -> new CodexMateriaItemRenderer(
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
        if(!pLevel.isClientSide()){
            NetworkHooks.openScreen((ServerPlayer) pPlayer, new SimpleMenuProvider(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pInternalPlayer) {
                    ContainerData data = new SimpleContainerData(0);
                    return new CodexMateriaMenu(pContainerId, pPlayerInventory, data);
                }
            }, Component.empty()));
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
