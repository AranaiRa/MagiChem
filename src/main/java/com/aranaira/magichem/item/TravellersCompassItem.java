package com.aranaira.magichem.item;

import com.aranaira.magichem.gui.TravellersCompassMenu;
import com.aranaira.magichem.networking.TravellersCompassSyncC2SPacket;
import com.aranaira.magichem.registry.PacketRegistry;
import com.aranaira.magichem.util.ClientUtil;
import com.mna.KeybindInit;
import com.mna.items.artifice.ItemThaumaturgicCompass;
import com.mna.items.base.IRadialInventorySelect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TravellersCompassItem extends ItemThaumaturgicCompass implements IRadialInventorySelect {
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        if(!pLevel.isClientSide()) {
            if(KeybindInit.InventoryItemOpen.get().isDown()) {
                NetworkHooks.openScreen((ServerPlayer)pPlayer, new SimpleMenuProvider(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.empty();
                    }

                    @Nullable
                    @Override
                    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pInternalPlayer) {
                        ItemStack itemInHand = pInternalPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                        int slot = pPlayerInventory.findSlotMatchingItem(itemInHand);

                        ContainerData data = new SimpleContainerData(1);
                        data.set(0, slot);

                        return new TravellersCompassMenu(pContainerId, pPlayerInventory, data);
                    }
                }, Component.empty()));
            }

        }
        return super.use(pLevel, pPlayer, pHand);
    }

    @Override
    public int capacity() {
        return 12;
    }

    @Override
    public void setIndex(ItemStack itemStack, int i) {
        if(FMLEnvironment.dist.isClient()) {
            Level level = ClientUtil.tryGetClientLevel();

            if(level != null) {
                CompoundTag nbt = itemStack.getOrCreateTag();
                nbt.putInt("index", i);
                itemStack.setTag(nbt);

                PacketRegistry.sendToServer(new TravellersCompassSyncC2SPacket(
                        i
                ));
            }
        }
    }

    @Override
    public int getIndex(ItemStack itemStack) {
        if(itemStack.hasTag()) {
            if(itemStack.getTag().contains("index"))
                return itemStack.getTag().getInt("index");
        }

        return 0;
    }
}
