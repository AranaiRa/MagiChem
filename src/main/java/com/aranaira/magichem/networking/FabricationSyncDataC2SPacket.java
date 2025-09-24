package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.GrandCircleFabricationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class FabricationSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final ResourceLocation registryKey;
    private final boolean isRegistryKeyFluid;
    private final byte powerUsageSetting;

    public FabricationSyncDataC2SPacket(BlockPos pBlockPos, ResourceLocation pResourceLocation, boolean pIsFluid, int pPowerLevel) {
        this.blockPos = pBlockPos;
        this.registryKey = pResourceLocation;
        this.isRegistryKeyFluid = pIsFluid;
        this.powerUsageSetting = (byte)pPowerLevel;
    }

    public FabricationSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.powerUsageSetting = buf.readByte();
        this.isRegistryKeyFluid = buf.readBoolean();
        this.registryKey = buf.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeByte(powerUsageSetting);
        buf.writeBoolean(isRegistryKeyFluid);
        buf.writeResourceLocation(registryKey);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof GrandCircleFabricationBlockEntity gcfbe) {
                gcfbe.setPowerUsageSetting(powerUsageSetting);
                if(isRegistryKeyFluid)
                    gcfbe.setCurrentRecipe(ForgeRegistries.FLUIDS.getValue(registryKey));
                else
                    gcfbe.setCurrentRecipe(ForgeRegistries.ITEMS.getValue(registryKey));
            }
            else if(entity instanceof CircleFabricationBlockEntity cfbe) {
                if(isRegistryKeyFluid)
                    cfbe.setCurrentRecipe(ForgeRegistries.FLUIDS.getValue(registryKey));
                else
                    cfbe.setCurrentRecipe(ForgeRegistries.ITEMS.getValue(registryKey));
            }
        });

        return true;
    }
}
