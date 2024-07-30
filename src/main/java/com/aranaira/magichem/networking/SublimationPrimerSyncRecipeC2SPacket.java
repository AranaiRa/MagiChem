package com.aranaira.magichem.networking;

import com.aranaira.magichem.foundation.IBlockWithPowerLevel;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SublimationPrimerSyncRecipeC2SPacket {
    private final String recipeKey;

    public SublimationPrimerSyncRecipeC2SPacket(String pRecipeKey) {
        this.recipeKey = pRecipeKey;
    }

    public SublimationPrimerSyncRecipeC2SPacket(FriendlyByteBuf buf) {
        this.recipeKey = new String(buf.readByteArray());
    }

    public void toBytes(FriendlyByteBuf buf) {
        byte[] byteArray = this.recipeKey.getBytes();
        buf.writeByteArray(byteArray);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        ItemStack itemToModify = player.getInventory().getSelected();

        if(itemToModify.getItem() == ItemRegistry.SUBLIMATION_PRIMER.get()) {
            CompoundTag nbt = itemToModify.getOrCreateTag();
            nbt.putString("recipe", recipeKey);
        }

        return true;
    }
}
