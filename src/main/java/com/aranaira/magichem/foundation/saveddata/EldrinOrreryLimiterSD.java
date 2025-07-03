package com.aranaira.magichem.foundation.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;

public class EldrinOrreryLimiterSD extends SavedData {
    private final ArrayList<String> playersWithEldrinOrrery = new ArrayList<>();

    protected EldrinOrreryLimiterSD() {
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        CompoundTag list = new CompoundTag();

        for(String uuid : playersWithEldrinOrrery) {
            list.putBoolean(uuid, true);
        }
        pCompoundTag.put("playersWithEldrinOrrery", list);

        return pCompoundTag;
    }

    public static EldrinOrreryLimiterSD load(CompoundTag pCompoundTag) {
        EldrinOrreryLimiterSD out = create();

        out.playersWithEldrinOrrery.clear();
        if(pCompoundTag.contains("playersWithEldrinOrrery")) {
            CompoundTag list = pCompoundTag.getCompound("playersWithEldrinOrrery");
            out.playersWithEldrinOrrery.addAll(list.getAllKeys());
        }

        return out;
    }

    public static EldrinOrreryLimiterSD create() {
        return new EldrinOrreryLimiterSD();
    }

    public boolean playerHasOrrery(Player pPlayer) {
        return playersWithEldrinOrrery.contains(pPlayer.getUUID().toString());
    }

    public void addOrrery(Player pPlayer) {
        playersWithEldrinOrrery.add(pPlayer.getUUID().toString());
        setDirty();
    }

    public void removeOrrery(Player pPlayer) {
        String query = pPlayer.getUUID().toString();
        if(playersWithEldrinOrrery.contains(query)) {
            playersWithEldrinOrrery.remove(query);
            setDirty();
        }
    }

    public void removeOrrery(String pQuery) {
        if(playersWithEldrinOrrery.contains(pQuery)) {
            playersWithEldrinOrrery.remove(pQuery);
            setDirty();
        }
    }
}
