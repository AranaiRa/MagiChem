package com.aranaira.magichem.item;

import com.aranaira.magichem.gui.TravellersCompassMenu;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.mna.KeybindInit;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.artifice.ThaumaturgicCompass;
import com.mna.items.base.IRadialInventorySelect;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TravellersCompassItem extends ThaumaturgicCompass implements IRadialInventorySelect {
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {

            if(!pLevel.isClientSide()) {
                Optional<IPlayerMagic> magic = pPlayer.getCapability(PlayerMagicProvider.MAGIC).resolve();
                if(magic.isPresent() && magic.get().isModifierPressed()) {
                    final int slot = pHand == InteractionHand.MAIN_HAND ? pPlayer.getInventory().selected : Inventory.SLOT_OFFHAND;

                    NetworkHooks.openScreen((ServerPlayer)pPlayer, new SimpleMenuProvider(new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.empty();
                        }

                        @Nullable
                        @Override
                        public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pInternalPlayer) {
                            return new TravellersCompassMenu(pContainerId, pPlayerInventory, slot);
                        }
                    }, Component.empty()), buf -> buf.writeInt(slot));
                    return InteractionResultHolder.sidedSuccess(pPlayer.getItemInHand(pHand), pLevel.isClientSide());
                }
            }

        //Let's make sure it actually does what it's supposed to and not change modes magically.
        ItemStack stack = pPlayer.getItemInHand(pHand);
        boolean isResearch = !pLevel.isClientSide() && pPlayer.isCrouching();
        boolean modeBefore = ThaumaturgicCompass.getTrackMode(stack);
        ThaumaturgicCompass.TrackType typeBefore = ThaumaturgicCompass.getTrackType(stack);

        InteractionResultHolder<ItemStack> result = super.use(pLevel, pPlayer, pHand);

        if (isResearch && typeBefore != ThaumaturgicCompass.TrackType.Unset && stack.hasTag()) {
            stack.getTag().putBoolean("tracking_mode", modeBefore);
            stack.getTag().putInt("tracking_type", typeBefore.ordinal());
        }

        if(!pLevel.isClientSide())
            updateSelectedRadialItemFromTag(stack);

        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final InteractionResult result = super.useOn(context);
        if(result == InteractionResult.SUCCESS)
            use(context.getLevel(), context.getPlayer(), context.getHand());

        return result;
    }

    @Override
    public int capacity() {
        return 12;
    }

    @Override
    public void setIndex(ItemStack itemStack, int i) {
        itemStack.getOrCreateTag().putInt("index", i);
        updateTagFromSelectedRadialItem(itemStack);

    }

    @Override
    public int getIndex(ItemStack itemStack) {
        if(itemStack.hasTag()) {
            if(itemStack.getTag().contains("index"))
                return itemStack.getTag().getInt("index");
        }

        return 0;
    }

    public static void updateTagFromSelectedRadialItem(ItemStack pStack) {
        if(pStack.hasTag()) {
            CompoundTag nbt = pStack.getTag();

            ItemStackHandler deserializedInventory = new ItemStackHandler(24);
            deserializedInventory.deserializeNBT(nbt.getCompound("inventory"));
            ItemStack compassToCopyFrom = deserializedInventory.getStackInSlot(pStack.getTag().getInt("index"));

            if(compassToCopyFrom.hasTag()) {
                CompoundTag nbtCompass = compassToCopyFrom.getTag();
                nbt.putString("LodestoneDimension", nbtCompass.getString("LodestoneDimension"));
                nbt.putInt("tracking_type", nbtCompass.getInt("tracking_type"));
                nbt.put("LodestonePos", nbtCompass.getCompound("LodestonePos"));
                nbt.putByte("tracking_mode", nbtCompass.getByte("tracking_mode"));
                nbt.putByte("LodestoneTracked", nbtCompass.getByte("LodestoneTracked"));
                nbt.putString("tracking_key", nbtCompass.getString("tracking_key"));
                nbt.putInt("CustomModelData", 1);
                pStack.setTag(nbt);
            } else {
                CompoundTag nbtCompass = compassToCopyFrom.getTag();
                nbt.remove("LodestoneDimension");
                nbt.remove("tracking_type");
                nbt.remove("LodestonePos");
                nbt.remove("tracking_mode");
                nbt.remove("LodestoneTracked");
                nbt.remove("tracking_key");
                nbt.remove("CustomModelData");
                pStack.setTag(nbt);
            }
        }
    }

    public static void updateSelectedRadialItemFromTag(ItemStack pStack) {
        if(!pStack.hasTag()) return;
        CompoundTag nbt = pStack.getTag();

        ItemStackHandler deserializedInventory = new ItemStackHandler(24);
        deserializedInventory.deserializeNBT(nbt.getCompound("inventory"));
        ItemStack compassToCopyTo = deserializedInventory.getStackInSlot(pStack.getTag().getInt("index"));

        if(compassToCopyTo.hasTag()) {
            CompoundTag nbtCompass = compassToCopyTo.getTag();
            nbtCompass.putString("LodestoneDimension", nbt.getString("LodestoneDimension"));
            nbtCompass.putInt("tracking_type",nbt.getInt("tracking_type"));
            nbtCompass.put("LodestonePos", nbt.getCompound("LodestonePos"));
            nbtCompass.putByte("tracking_mode", nbt.getByte("tracking_mode"));
            nbtCompass.putByte("LodestoneTracked", nbt.getByte("LodestoneTracked"));
            nbtCompass.putString("tracking_key", nbt.getString("tracking_key"));

            nbt.put("inventory", deserializedInventory.serializeNBT());
        }

    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);

        if(level.isClientSide())
            return;

        if(level.getGameTime() % 200 == 0 && player instanceof ServerPlayer sp) {
            final BlockPos respawnPosition = sp.getRespawnPosition();
            if(respawnPosition != null) {
                String respawnDimensionKey = sp.getRespawnDimension().location().toString();

                if (stack.hasTag()) {
                    CompoundTag nbtCompass = stack.getTag();
                    nbtCompass.putLong("respawnPosition", respawnPosition.asLong());
                    nbtCompass.putString("respawnDimension", respawnDimensionKey);
                    stack.setTag(nbtCompass);
                }
            } else {
                CompoundTag nbtCompass = stack.getTag();
                if(nbtCompass != null && nbtCompass.contains("respawnPosition")) {
                    nbtCompass.remove("respawnPosition");
                }
                if(nbtCompass != null && nbtCompass.contains("respawnDimension")) {
                    nbtCompass.remove("respawnDimension");
                }
                stack.setTag(nbtCompass);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(entityIn instanceof Player player) {
            if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == ItemRegistry.TRAVELLERS_COMPASS.get() ||
               player.getItemInHand(InteractionHand.OFF_HAND).getItem() == ItemRegistry.TRAVELLERS_COMPASS.get()) {
                if(worldIn.getGameTime() % 59 == 0)
                    player.addEffect(new MobEffectInstance(MobEffectsRegistry.SIXFOLD_PATH.get(), 80, 0, false, false, true));
            }
        }

        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> pTooltipComponents, TooltipFlag flagIn) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.travellers_compass")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        String textGui = I18n.get(((KeyMapping)KeybindInit.InventoryItemOpen.get()).getKey().getName(), new Object[0]);
        pTooltipComponents.add(Component.translatable("item.mna.item-with-gui.open-with", new Object[]{textGui}).withStyle(ChatFormatting.AQUA));

        String textRadial = I18n.get(((KeyMapping)KeybindInit.RadialMenuOpen.get()).getKey().getDisplayName().getString(), new Object[0]);
        pTooltipComponents.add(Component.translatable("item.mna.item-with-gui.radial-open", new Object[]{textRadial}).withStyle(ChatFormatting.AQUA));

        super.appendHoverText(stack, worldIn, pTooltipComponents, flagIn);
    }
}
