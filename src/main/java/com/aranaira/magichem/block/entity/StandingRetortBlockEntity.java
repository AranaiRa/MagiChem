package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.gui.ConjurerMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StandingRetortBlockEntity extends BlockEntity implements /*MenuProvider<StandingRetortMenu>,*/ IShlorpReceiver, IMateriaProvisionRequester {
    private int element = 0;
    private boolean provisioningInProgress = false;

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    public static HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(false, true);

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(stack.getItem() instanceof MateriaItem mi)
                return mi.getMateriaName().equals(getMateriaType());

            return false;
        }
    };

    public StandingRetortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.STANDING_RETORT_BE.get(), pPos, pBlockState);
    }

    public String getMateriaType() {
        if(element == 1) return "essentia_ender";
        else if(element == 2) return "essentia_earth";
        else if(element == 3) return "essentia_water";
        else if(element == 4) return "essentia_air";
        else if(element == 5) return "essentia_fire";
        else if(element == 6) return "essentia_arcane";

        return "";
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

//    @Nullable
//    @Override
//    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
//        return new ConjurerMenu(pContainerId, pPlayerInventory, this, this.data);
//    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("element", this.element);
        nbt.putBoolean("provisioningInProgress", this.provisioningInProgress);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        element = nbt.getInt("element");
        provisioningInProgress = nbt.getBoolean("provisioningInProgress");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("element", this.element);
        nbt.putBoolean("provisioningInProgress", this.provisioningInProgress);

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
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        return itemHandler.getStackInSlot(0).getCount() < 16;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        HashMap<MateriaItem, Integer> needs = new HashMap<>();

        int currentCount = itemHandler.getStackInSlot(0).getCount();
        if(currentCount < 16) {
            needs.put(materiaMap.get(getMateriaType()), 64 - currentCount);
        }

        return needs;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(pMateriaItem.getMateriaName().equals(getMateriaType())) {
            provisioningInProgress = true;
        }
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        provisioningInProgress = false;
    }

    @Override
    public void provide(ItemStack pStack) {
        provisioningInProgress = false;
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() == materiaMap.get(getMateriaType()))
            return Math.min(pStack.getCount(), 64 - itemHandler.getStackInSlot(0).getCount());

        return 0;
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return 0;
    }
}
