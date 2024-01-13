package com.aranaira.magichem.block;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.common.collect.ImmutableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MateriaVesselBlock extends BaseEntityBlock {
    public MateriaVesselBlock(Properties properties) {
        super(properties);
    }

    private static final VoxelShape SHAPE_JAR;
    private static final VoxelShape SHAPE_LID;
    private static final VoxelShape SHAPE_AGGREGATE;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MateriaVesselBlockEntity(pos, state);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        if(pStack.hasTag()) {
            if (pStack.getTag().contains("type")) {
                MateriaItem materia = ItemRegistry.getMateriaMap(false, false)
                        .get(pStack.getTag().getString("type"));
                int count = pStack.getTag().getInt("amount");
                String translationID = "";
                if(materia instanceof AdmixtureItem)
                    translationID = "item.magichem.admixture_"+materia.getMateriaName();
                else
                    translationID = "item.magichem.essentia_"+materia.getMateriaName();

                pTooltip.add(
                        Component.translatable(translationID)
                                .withStyle(ChatFormatting.GRAY)
                );
                pTooltip.add(
                        Component.literal(count + " / " + Config.materiaVesselCapacity)
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
                pTooltip.add(
                        Component.literal(materia.getDisplayFormula())
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
        }
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        ItemStack stack = new ItemStack(BlockRegistry.MATERIA_VESSEL.get(), 1);
        MateriaVesselBlockEntity mvbe = (MateriaVesselBlockEntity) pBuilder.getParameter(LootContextParams.BLOCK_ENTITY);

        CompoundTag tag = new CompoundTag();
        tag.putString("type", mvbe.getMateriaType().getMateriaName());
        tag.putInt("amount", mvbe.getCurrentStock());
        stack.setTag(tag);

        List<ItemStack> output = new ArrayList<>();
        output.add(stack);
        return output;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_AGGREGATE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    static {
        SHAPE_JAR = Block.box(3.0D, 0.0D,  3.0D, 13.0D, 14.0D, 13.0D);
        SHAPE_LID = Block.box(5.0D, 12.5D, 5.0D, 11.0D, 15.5D, 11.0D);
        SHAPE_AGGREGATE = Shapes.or(SHAPE_JAR, new VoxelShape[]{SHAPE_LID});
    }
}
