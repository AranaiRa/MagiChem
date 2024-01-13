package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MateriaVesselItem extends BlockItem {
    public MateriaVesselItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        CompoundTag tag = null;
        if(context.getItemInHand().hasTag()) {
            tag = context.getItemInHand().getTag();
        }
        InteractionResult result = super.place(context);

        if(!context.getLevel().isClientSide()) {
            BlockPos clickedPos = context.getClickedPos();
            BlockEntity entity = context.getLevel().getBlockEntity(clickedPos);

            if(entity != null) {
                if(entity instanceof MateriaVesselBlockEntity mvbe && tag != null) {
                    if(tag.contains("type") && tag.contains("amount")) {
                        MateriaItem materia = ItemRegistry.getMateriaMap(false, false)
                                .get(tag.getString("type"));

                        mvbe.setContents(materia, tag.getInt("amount"));
                    }
                }
            }
        }

        return result;
    }
}
