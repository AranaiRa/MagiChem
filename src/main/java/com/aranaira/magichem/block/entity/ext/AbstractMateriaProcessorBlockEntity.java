package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AbstractMateriaProcessorBlockEntity extends BlockEntity implements IShlorpReceiver {

    protected NonNullList<Triplet<MateriaItem, Integer, Boolean>> satisfactionDemands = NonNullList.create();

    public AbstractMateriaProcessorBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    protected void setSatisfactionDemands(NonNullList<ItemStack> pDemands) {
        satisfactionDemands.clear();

        for(ItemStack is : pDemands) {
            if(is.getItem() instanceof MateriaItem mi) {
                satisfactionDemands.add(new Triplet<>(mi, is.getCount(), false));
            }
        }
    }

    protected void clearSatisfactionDemands() {
        satisfactionDemands.clear();
    }

    protected boolean isFullySatisfied(){
        for(Triplet<MateriaItem, Integer, Boolean> demand : satisfactionDemands) {
            if(demand.getSecond() > 0)
                return false;
        }
        return true;
    }

    public int satisfy(ItemStack pQuery) {
        if(pQuery.getItem() instanceof MateriaItem mi) {

            for (int i = 0; i < satisfactionDemands.size(); i++) {
                Triplet<MateriaItem, Integer, Boolean> demand = satisfactionDemands.get(i);
                if (demand.getFirst() == pQuery.getItem()) {
                    satisfactionDemands.set(i, new Triplet<>(mi, Math.max(0, demand.getSecond() - pQuery.getCount()), false));
                    return i;
                }
            }
        }

        return -1;
    }

    public void markInTransit(MateriaItem pQuery) {
        for (int i = 0; i < satisfactionDemands.size(); i++) {
            Triplet<MateriaItem, Integer, Boolean> demand = satisfactionDemands.get(i);
            if (demand.getFirst() == pQuery) {
                satisfactionDemands.set(i, new Triplet<>(demand.getFirst(), demand.getSecond(), true));
                break;
            }
        }
    }

    public NonNullList<MateriaItem> getDemandedMateriaNotInTransit() {
        NonNullList<MateriaItem> output = NonNullList.create();
        for (Triplet<MateriaItem, Integer, Boolean> demand : satisfactionDemands) {
            if(!demand.getThird() && demand.getSecond() > 0) {
                output.add(demand.getFirst());
            }
        }
        return output;
    }

    @Override
    public int canAcceptStack(ItemStack pStack) {
        for(Triplet<MateriaItem, Integer, Boolean> pair : satisfactionDemands) {
            if(pStack.getItem() instanceof MateriaItem mi) {
                if(mi == pair.getFirst())
                    return pair.getSecond();
            }
        }

        return 0;
    }

    @Override
    public int insertStack(ItemStack pStack) {
        satisfy(pStack);

        return -1;
    }
}
