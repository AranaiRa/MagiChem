package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.foundation.IScannableByMateriaManifest;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.gui.MateriaManifestMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MateriaManifestBlockEntity extends BlockEntity implements MenuProvider {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return 0;
        }

        @Override
        public void set(int pIndex, int pValue) {

        }

        @Override
        public int getCount() {
            return 0;
        }
    };

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() == ItemInit.RUNE_MARKING_PAIR.get();
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    private List<Triplet<MateriaItem, BlockPos, AbstractMateriaStorageBlockEntity>> materiaStorageInZone = new ArrayList<>();

    public MateriaManifestBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MATERIA_MANIFEST_BE.get(), pos, state);
    }

    public MateriaManifestBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.materia_manifest");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new MateriaManifestMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    public void scanMateriaInZone() {
        if(level != null) {
            int dr = Config.materiaManifestDefaultRange;
            AABB zone;
            if (itemHandler.getStackInSlot(0).isEmpty())
                zone = new AABB(getBlockPos().offset(-dr, -dr, -dr), getBlockPos().offset(dr, dr, dr));
            else
                zone = new AABB(getBlockPos().offset(-dr, -dr, -dr), getBlockPos().offset(dr, dr + 1, dr)); //replace with rune of marking area

            materiaStorageInZone.clear();

            for(int z = (int) zone.minZ; z<=zone.maxZ; z++) {
                for (int y = (int) zone.minY; y <= zone.maxY; y++) {
                    for (int x = (int) zone.minX; x <= zone.maxX; x++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if(level.getBlockState(pos).getBlock() instanceof IScannableByMateriaManifest) {
                            BlockEntity be = level.getBlockEntity(pos);
                            if(be instanceof AbstractMateriaStorageBlockEntity amsbe) {
                                if(amsbe.getMateriaType() != null)
                                    materiaStorageInZone.add(new Triplet<>(amsbe.getMateriaType(), pos, amsbe));
                            }
                        }
                    }
                }
            }

            Collections.sort(materiaStorageInZone, Comparator.comparing(o -> o.getFirst().getMateriaName()));

            int a = 0;
        }
    }

    public List<Triplet<MateriaItem, BlockPos, AbstractMateriaStorageBlockEntity>> getMateriaStorageInZone() {
        return materiaStorageInZone;
    }
}
