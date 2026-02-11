package com.aranaira.magichem.capabilities.enhancement;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.capabilities.enhancement.IEnhancementCapability.EnhancedHeartType;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomCapability;
import com.mna.api.spells.attributes.Attribute;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EnhancementProvider implements ICapabilitySerializable<Tag> {
    public static final Capability<IEnhancementCapability> ENHANCEMENT = CapabilityManager.get(new CapabilityToken<>() {} );

    private final LazyOptional<IEnhancementCapability> holder = LazyOptional.of(EnhancementCapability::new);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ENHANCEMENT.orEmpty(cap, this.holder);
    }

    @Override
    public Tag serializeNBT() {
        IEnhancementCapability instance = this.holder.orElse(new EnhancementCapability());
        CompoundTag nbt = new CompoundTag();

        nbt.putString("heart", instance.getHeart().name());

        return nbt;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        IEnhancementCapability instance = this.holder.orElse(new EnhancementCapability());
        if(nbt instanceof CompoundTag ct) {
            instance.setHeart(EnhancedHeartType.valueOf(ct.getString("heart")));
        }
    }

    public static Optional<IEnhancementCapability> getCapability(Player entity) {
        Optional<IEnhancementCapability> enhancementCapability = entity.getCapability(EnhancementProvider.ENHANCEMENT).resolve();
        if(enhancementCapability.isEmpty()) {
            String errorMessage = "Player \""+entity.getDisplayName()+"\" had no Enhancement capability!";
            MagiChemMod.LOGGER.error(errorMessage);
        }
        return enhancementCapability;
    }
}
