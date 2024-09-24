package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.foundation.IRequiresRouterCleanupOnDestruction;
import com.aranaira.magichem.foundation.IScannableByMateriaManifest;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.gui.MateriaManifestMenu;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.mna.items.ItemInit;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MateriaManifestBlockEntity extends BlockEntity implements MenuProvider, IRequiresRouterCleanupOnDestruction {

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
    public AbstractMateriaStorageBlockEntity tetherTarget = null;
    public AABB RENDER_BOUNDING_BOX;

    public MateriaManifestBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MATERIA_MANIFEST_BE.get(), pos, state);

        int distLimit = Config.materiaManifestDistanceLimit;
        int sizeLimit = Config.materiaManifestSizeConstraint;
        BlockPos offset = new BlockPos(distLimit+sizeLimit, distLimit+sizeLimit, distLimit+sizeLimit);
        RENDER_BOUNDING_BOX = new AABB(getBlockPos().offset(offset), getBlockPos().offset(offset.multiply(-1)));
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
            AABB zone = getExtents();

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

            Collections.sort(materiaStorageInZone, Comparator.comparing(o -> (o.getFirst() instanceof EssentiaItem ? "a_" : "z_") + o.getFirst().getMateriaName()));
        }
    }

    public List<Triplet<MateriaItem, BlockPos, AbstractMateriaStorageBlockEntity>> getMateriaStorageInZone() {
        return materiaStorageInZone;
    }

    /**
     * @param pStack
     * @return 0: Success, 1: Too far, 2: Too big
     */
    public int setMarkingPair(ItemStack pStack) {
        AABB query = extractExtentsFromStoredMark(pStack);
        if(query != null) {
            Vec3 center = query.getCenter();
            Vec3 myPos = Vec3.atCenterOf(getBlockPos());

            double dist = myPos.distanceTo(center);
            if(dist >= Config.materiaManifestDistanceLimit)
                return 1;

            //TODO: size limit
//            boolean sizeCheckX = query.getXsize() <= Config.materiaManifestSizeConstraint;
//            boolean sizeCheckY = query.getYsize() <= Config.materiaManifestSizeConstraint;
//            boolean sizeCheckZ = query.getZsize() <= Config.materiaManifestSizeConstraint;
//            if(!(sizeCheckX && sizeCheckY && sizeCheckZ))
//                return 2;
        }

        itemHandler.setStackInSlot(0, pStack.copy());
        syncAndSave();
        return 0;
    }

    private AABB extractExtentsFromStoredMark(ItemStack pStack) {
        if(!pStack.isEmpty()) {
            ItemStack stack = itemHandler.getStackInSlot(0);
            if(stack.hasTag()) {
                CompoundTag nbt = stack.getTag();
                if(nbt.contains("mark")) {
                    final CompoundTag coords = nbt.getCompound("mark");
                    int minX = (int)Math.round(coords.getDouble("minx"));
                    int maxX = (int)Math.round(coords.getDouble("maxx"));
                    int minY = (int)Math.round(coords.getDouble("miny"));
                    int maxY = (int)Math.round(coords.getDouble("maxy"));
                    int minZ = (int)Math.round(coords.getDouble("minz"));
                    int maxZ = (int)Math.round(coords.getDouble("maxz"));

                    return new AABB(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
                }
            }
        }
        return null;
    }

    private AABB getExtents() {
        if(!itemHandler.getStackInSlot(0).isEmpty()) {
            AABB query = extractExtentsFromStoredMark(itemHandler.getStackInSlot(0));
            if(query != null)
                return query;
        }

        int minX = getBlockPos().getX() - Config.materiaManifestDefaultRange;
        int maxX = getBlockPos().getX() + Config.materiaManifestDefaultRange;
        int minY = getBlockPos().getY() - Config.materiaManifestDefaultRange;
        int maxY = getBlockPos().getY() + Config.materiaManifestDefaultRange;
        int minZ = getBlockPos().getZ() - Config.materiaManifestDefaultRange;
        int maxZ = getBlockPos().getZ() + Config.materiaManifestDefaultRange;

        return new AABB(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if(nbt.getCompound("inventory").getInt("Size") != itemHandler.getSlots()) {
            ItemStackHandler temp = new ItemStackHandler(nbt.getCompound("inventory").size());
            temp.deserializeNBT(nbt.getCompound("inventory"));
            for(int i=0; i<temp.getSlots(); i++) {
                itemHandler.setStackInSlot(i, temp.getStackInSlot(i));
            }
        } else {
            this.itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return RENDER_BOUNDING_BOX;
    }

    @Override
    public void destroyRouters() {
        getLevel().destroyBlock(getBlockPos().above(), true);
    }
}
