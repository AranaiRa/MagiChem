package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class MirrorLabyrinthBlockEntity extends BlockEntity {
    public static HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(false, false);
    private HashMap<MateriaItem, Integer> materiaStorage = new HashMap<>();

    public MirrorLabyrinthBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        CompoundTag materiaStorageTag = new CompoundTag();
        for(MateriaItem mi : materiaStorage.keySet()) {
            materiaStorageTag.putInt(mi.getMateriaName(), materiaStorage.get(mi));
        }

        nbt.put("materiaStorage", materiaStorageTag);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        if(nbt.contains("materiaStorage")) {
            materiaStorage.clear();
            CompoundTag materiaStorageTag = nbt.getCompound("materiaStorage");
            for(String key : materiaStorageTag.getAllKeys()) {
                MateriaItem mi = materiaMap.get(key);
                materiaStorage.put(mi, materiaStorageTag.getInt(key));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        return nbt;
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
