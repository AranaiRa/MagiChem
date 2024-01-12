package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MateriaVesselBlockEntity extends BlockEntity {

    public MateriaVesselBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.MATERIA_VESSEL_BE.get(), pos, state);
    }

    private int currentStock;
    private MateriaItem currentMateriaType;

    public int getCurrentStock() {
        return currentStock;
    }

    public float getCurrentStockPercent() {
        return (float) currentStock / (float) Config.materiaVesselCapacity;
    }

    public MateriaItem getMateriaType() {
        return currentMateriaType;
    }

    @Override
    public void load(CompoundTag nbt) {
        String regName = nbt.getString("type");
        if(!regName.equals("empty"))
            currentMateriaType = ItemRegistry.getMateriaMap(false, false).get(regName);
        currentStock = nbt.getInt("amount");
        super.load(nbt);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putString("type",
                this.currentMateriaType == null ? "empty" : this.currentMateriaType.getMateriaName());
        nbt.putInt("amount", this.currentStock);
        super.saveAdditional(nbt);
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Try to insert a stack into the materia vessel.
     * @param stack The materia to insert. Must be some kind of MateriaItem.
     * @return The number of materia items inserted. Use this to create bottles.
     */
    public int insertMateria(ItemStack stack) {
        if(currentMateriaType == null) {
            currentMateriaType = (MateriaItem) stack.getItem();
            currentStock = stack.getCount();
            this.syncAndSave();
            return stack.getCount();
        } else if(stack.getItem() != currentMateriaType) {
            return 0;
        } else {
            int tryStock = currentStock + stack.getCount();

            int increase = tryStock - Config.materiaVesselCapacity >= 0 ?
                    tryStock - Config.materiaVesselCapacity :
                    stack.getCount();

            currentStock += increase;

            this.syncAndSave();
            return increase;
        }
    }

    public ItemStack extractMateria(int amount) {
        if(currentMateriaType == null)
            return ItemStack.EMPTY;

        int extractedAmount = Math.min(currentStock, amount);
        ItemStack extractedMateria = new ItemStack(currentMateriaType, extractedAmount);

        if(currentStock - extractedAmount <= 0) {
            currentMateriaType = null;
            currentStock = 0;
        } else {
            currentStock -= extractedAmount;
        }

        this.syncAndSave();
        return extractedMateria;
    }
}
