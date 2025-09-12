package com.aranaira.magichem.capabilities.wisdom;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
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

public class WisdomProvider implements ICapabilitySerializable<Tag> {
    public static final Capability<IWisdomCapability> WISDOM = CapabilityManager.get(new CapabilityToken<>() {} );

    private final LazyOptional<IWisdomCapability> holder = LazyOptional.of(WisdomCapability::new);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return WISDOM.orEmpty(cap, this.holder);
    }

    @Override
    public Tag serializeNBT() {
        IWisdomCapability instance = this.holder.orElse(new WisdomCapability());
        CompoundTag nbt = new CompoundTag();
        short cardinal = 0, intercardinal = 0;

        cardinal |= (instance.getValue(Attribute.RADIUS) & 0b1111);
        cardinal |= (instance.getValue(Attribute.DURATION) & 0b1111) << 4;
        cardinal |= (instance.getValue(Attribute.DAMAGE) & 0b1111) << 8;
        cardinal |= (instance.getValue(Attribute.DELAY) & 0b1111) << 12;

        intercardinal |= (instance.getValue(Attribute.RANGE) & 0b1111);
        intercardinal |= (instance.getValue(Attribute.MAGNITUDE) & 0b1111) << 4;
        intercardinal |= (instance.getValue(Attribute.LESSER_MAGNITUDE) & 0b1111) << 8;
        intercardinal |= (instance.getValue(Attribute.SPEED) & 0b1111) << 12;

        nbt.putShort("wisdomCardinal", cardinal);
        nbt.putShort("wisdomIntercardinal", intercardinal);

        return nbt;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        IWisdomCapability instance = this.holder.orElse(new WisdomCapability());
        if(nbt instanceof CompoundTag ct) {
            short cardinal = ct.getShort("wisdomCardinal");
            short intercardinal = ct.getShort("wisdomIntercardinal");

            instance.setValue(Attribute.RADIUS,
                    (cardinal & 0b1111));
            instance.setValue(Attribute.DURATION,
                    (cardinal & 0b11110000) >> 4);
            instance.setValue(Attribute.DAMAGE,
                    (cardinal & 0b111100000000) >> 8);
            instance.setValue(Attribute.DELAY,
                    (cardinal & 0b1111000000000000) >> 12);

            instance.setValue(Attribute.RANGE,
                    (intercardinal & 0b1111));
            instance.setValue(Attribute.MAGNITUDE,
                    (intercardinal & 0b11110000) >> 4);
            instance.setValue(Attribute.LESSER_MAGNITUDE,
                    (intercardinal & 0b111100000000) >> 8);
            instance.setValue(Attribute.SPEED,
                    (intercardinal & 0b1111000000000000) >> 12);
        }
    }

    public static Pair<Short, Short> serializeShorts(IWisdomCapability pInstance) {
        short cardinal = 0, intercardinal = 0;

        cardinal |= (pInstance.getValue(Attribute.RADIUS) & 0b1111);
        cardinal |= (pInstance.getValue(Attribute.DURATION) & 0b1111) << 4;
        cardinal |= (pInstance.getValue(Attribute.DAMAGE) & 0b1111) << 8;
        cardinal |= (pInstance.getValue(Attribute.DELAY) & 0b1111) << 12;

        intercardinal |= (pInstance.getValue(Attribute.RANGE) & 0b1111);
        intercardinal |= (pInstance.getValue(Attribute.MAGNITUDE) & 0b1111) << 4;
        intercardinal |= (pInstance.getValue(Attribute.LESSER_MAGNITUDE) & 0b1111) << 8;
        intercardinal |= (pInstance.getValue(Attribute.SPEED) & 0b1111) << 12;

        return new Pair<>(cardinal, intercardinal);
    }

    public static void deserializeShorts(IWisdomCapability pInstance, short pCardinal, short pIntercardinal) {
        pInstance.setValue(Attribute.RADIUS,
                (pCardinal & 0b1111));
        pInstance.setValue(Attribute.DURATION,
                (pCardinal & 0b11110000) >> 4);
        pInstance.setValue(Attribute.DAMAGE,
                (pCardinal & 0b111100000000) >> 8);
        pInstance.setValue(Attribute.DELAY,
                (pCardinal & 0b1111000000000000) >> 12);

        pInstance.setValue(Attribute.RANGE,
                (pIntercardinal & 0b1111));
        pInstance.setValue(Attribute.MAGNITUDE,
                (pIntercardinal & 0b11110000) >> 4);
        pInstance.setValue(Attribute.LESSER_MAGNITUDE,
                (pIntercardinal & 0b111100000000) >> 8);
        pInstance.setValue(Attribute.SPEED,
                (pIntercardinal & 0b1111000000000000) >> 12);
    }

    public static Optional<IWisdomCapability> getCapability(Player entity) {
        Optional<IWisdomCapability> wisdomCapability = entity.getCapability(WisdomProvider.WISDOM).resolve();
        if(wisdomCapability.isEmpty()) {
            String errorMessage = "Player \""+entity.getDisplayName()+"\" had no Wisdom capability!";
            MagiChemMod.LOGGER.error(errorMessage);
        }
        return wisdomCapability;
    }
}
