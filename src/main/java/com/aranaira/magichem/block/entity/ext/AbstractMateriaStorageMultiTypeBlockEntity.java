package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;

/**
 * This class exists primarily to define common behaviors between multi storage blocks regardless of if they have a static number of types or a dynamic number of types.
 * If you're instanceof-ing to handle shlorps or what have you, use this one over the Dynamic or Static subclasses.
 */
public abstract class AbstractMateriaStorageMultiTypeBlockEntity extends BlockEntity implements IShlorpReceiver {
    public static HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(false, false);

    public AbstractMateriaStorageMultiTypeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract int getCurrentStock(MateriaItem pMateriaType);

    public abstract float getCurrentStockPercent(MateriaItem pMateriaType);

    public abstract boolean containsMateriaType(MateriaItem pMateriaType);

    public abstract Collection<MateriaItem> getMateriaTypes();

    public abstract void setContents(MateriaItem pMateriaType, int pCount);

    public abstract void setContents(int pSlot, MateriaItem pMateriaType, int pCount);

    public abstract int fill(MateriaItem pMateriaType, int pAmount, boolean pVoidExcess);

    public abstract int drain(MateriaItem pMateriaType, int pAmount, boolean pKeepOne);

    public abstract int getStorageLimit(MateriaItem pMateriaType);

    @Override
    public abstract void load(CompoundTag nbt);

    @Override
    protected abstract void saveAdditional(CompoundTag nbt);

    @Override
    public abstract void handleUpdateTag(CompoundTag nbt);

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
        super.onDataPacket(net, pkt);
    }

    @Override
    public abstract CompoundTag getUpdateTag();

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public abstract int canAcceptStackFromShlorp(ItemStack pStack);

    @Override
    public abstract int insertStackFromShlorp(ItemStack pStack);

    public abstract Pair<Vector3, Vector3> getDefaultOriginAndTangent(MateriaItem pMateriaType);
}
