package com.aranaira.magichem.foundation.saveddata;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;

public class CenserWorkingAreasSD extends SavedData {
    private final HashMap<Long, AABB> censerData = new HashMap<>();

    protected CenserWorkingAreasSD() {
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        CompoundTag out = new CompoundTag();

        ListTag list = new ListTag();

        for(long id : censerData.keySet()) {
            CompoundTag entry = new CompoundTag();
            final AABB aabb = censerData.get(id);
            out.putLong("id", id);
            out.putLong("min", BlockPos.asLong((int)aabb.minX, (int)aabb.minY, (int)aabb.minZ));
            out.putLong("max", BlockPos.asLong((int)aabb.maxX, (int)aabb.maxY, (int)aabb.maxZ));
            list.add(entry);
        }
        pCompoundTag.put("censerData", out);

        return pCompoundTag;
    }

    public static CenserWorkingAreasSD load(CompoundTag pCompoundTag) {
        CenserWorkingAreasSD out = create();

        out.censerData.clear();
        if(pCompoundTag.contains("censerData")) {
            ListTag list = pCompoundTag.getList("censerData", 9);

            for(int i=0; i<list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                BlockPos min = BlockPos.of(entry.getLong("min"));
                BlockPos max = BlockPos.of(entry.getLong("max"));
                final AABB aabb = new AABB(min, max);
                out.censerData.put(entry.getLong("id"), aabb);
            }
        }

        return out;
    }

    public static CenserWorkingAreasSD create() {
        return new CenserWorkingAreasSD();
    }

    public boolean checkOverlap(long pos) {
        AABB query = censerData.get(pos);

        for(Long id : censerData.keySet()) {
            if(id == pos) continue;

            if(censerData.get(id).intersects(query))
                return true;
        }
        return false;
    }

    public boolean hasCenserAt(long pos) {
        return censerData.containsKey(pos);
    }

    public AABB getAABBForCenser(long pos) {
        return censerData.get(pos);
    }

    public void addOrUpdateCenser(long pos, AABB workingArea) {
        censerData.put(pos, workingArea);
        setDirty();
    }

    public void removeCenser(long query) {
        if(censerData.containsKey(query)) {
            censerData.remove(query);
            setDirty();
        }
    }

    public static CenserWorkingAreasSD getCenserData(Level pLevel) {
        if(pLevel == null || pLevel.getServer() == null) return null;

        return pLevel.getServer().overworld().getDataStorage().computeIfAbsent(CenserWorkingAreasSD::load, CenserWorkingAreasSD::create, "censerData");
    }
}
