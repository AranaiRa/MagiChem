package com.aranaira.magichem.capabilities.enhancement;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.capabilities.enhancement.IEnhancementCapability.EnhancedHeartType;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomCapability;
import com.mna.api.spells.attributes.Attribute;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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

        CompoundTag bossTrophyTag = new CompoundTag();
        if(instance.getBossTrophyUseTargetTime() != 0) {
            bossTrophyTag.putLong("useTargetTime", instance.getBossTrophyUseTargetTime());
        }
        if(instance.hasLastDeathTargetLocation()) {
            final Pair<BlockPos, ResourceLocation> deathData = instance.getLastDeathTargetLocation();
            if(deathData.getFirst() != null && deathData.getSecond() != null){
                CompoundTag deathTag = new CompoundTag();
                deathTag.putLong("pos", deathData.getFirst().asLong());
                deathTag.putString("dim", deathData.getSecond().toString());
                bossTrophyTag.put("deathData", deathTag);
            }
        }
        if(bossTrophyTag.size() > 0) {
            nbt.put("bossTrophyData", bossTrophyTag);
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        IEnhancementCapability instance = this.holder.orElse(new EnhancementCapability());
        if(nbt instanceof CompoundTag ct) {
            instance.setHeart(EnhancedHeartType.valueOf(ct.getString("heart")));

            if(ct.contains("bossTrophyData")) {
                CompoundTag bossTrophyTag = ct.getCompound("bossTrophyData");
                if(bossTrophyTag.contains("useTargetTime")) instance.setBossTrophyUseTargetTime(bossTrophyTag.getLong("useTargetTime"));
                if(bossTrophyTag.contains("deathData")) {
                    CompoundTag deathTag = bossTrophyTag.getCompound("deathData");
                    instance.setLastDeathTargetLocation(
                            BlockPos.of(deathTag.getLong("pos")),
                            new ResourceLocation(deathTag.getString("dim"))
                    );
                }
            }
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
