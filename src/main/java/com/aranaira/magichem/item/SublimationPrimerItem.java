package com.aranaira.magichem.item;

import com.aranaira.magichem.item.renderer.SublimationPrimerItemRenderer;
import com.aranaira.magichem.networking.SublimationPrimerSyncRecipeC2SPacket;
import com.aranaira.magichem.recipe.SublimationRitualRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.aranaira.magichem.util.ClientUtil;
import com.mna.KeybindInit;
import com.mna.items.base.IRadialInventorySelect;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class SublimationPrimerItem extends Item implements IRadialInventorySelect {

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
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        //Had to copy this in from the interface because I'm adding more text myself
        String txt = I18n.get(((KeyMapping) KeybindInit.RadialMenuOpen.get()).getKey().getDisplayName().getString(), new Object[0]);
        pTooltipComponents.add(Component.translatable("item.mna.item-with-gui.radial-open", new Object[]{txt}).withStyle(ChatFormatting.AQUA));

        //My tooltip
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.sublimationprimer")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        MutableComponent recipeName = Component.empty();
        if(pStack.getOrCreateTag().contains("recipe")) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(pStack.getTag().getString("recipe")));
            if(item != null)
                recipeName = (MutableComponent) item.getName(new ItemStack(item));
        }

        if(!recipeName.equals(Component.empty())) {
            pTooltipComponents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.sublimationprimer.currentrecipe").withStyle(ChatFormatting.DARK_GRAY))
                    .append(recipeName.withStyle(ChatFormatting.DARK_AQUA))
            );
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public int capacity() {
        if(FMLEnvironment.dist.isClient()) {
            return ClientUtil.getInfusionRitualRecipeCount();
        }
        return 3;
    }

    @Override
    public void setIndex(ItemStack itemStack, int i) {
        if(FMLEnvironment.dist.isClient()) {
            Level level = ClientUtil.tryGetClientLevel();

            if(level != null) {
                NonNullList<ItemStack> allOutputs = SublimationRitualRecipe.getAllOutputs(level);
                String key = ForgeRegistries.ITEMS.getKey(allOutputs.get(i).getItem()).toString();
                CompoundTag nbt = itemStack.getOrCreateTag();
                nbt.putString("recipe", key);
                itemStack.setTag(nbt);

                PacketRegistry.sendToServer(new SublimationPrimerSyncRecipeC2SPacket(
                        key
                ));
            }
        }
    }

    @Override
    public int getIndex(ItemStack itemStack) {
        return 0;
    }

    @Override
    public IItemHandlerModifiable getInventory(ItemStack stackEquipped) {
        return IRadialInventorySelect.super.getInventory(stackEquipped);
    }

    @Override
    public IItemHandlerModifiable getInventory(ItemStack stackEquipped, @Nullable Player player) {
        return IRadialInventorySelect.super.getInventory(stackEquipped, player);
    }
}
