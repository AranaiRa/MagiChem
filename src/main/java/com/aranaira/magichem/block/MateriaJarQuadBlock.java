package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.MateriaJarBlockEntity;
import com.aranaira.magichem.block.entity.MateriaJarQuadBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MateriaJarQuadBlock extends BaseEntityBlock {

    private static final VoxelShape
            SHAPE_JAR_1, SHAPE_LID_1, SHAPE_JAR_2, SHAPE_LID_2, SHAPE_JAR_3, SHAPE_LID_3, SHAPE_JAR_4, SHAPE_LID_4, SHAPE_AGGREGATE;

    public MateriaJarQuadBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MateriaJarQuadBlockEntity(pPos, pState);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
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
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        BlockEntity be = pLevel.getBlockEntity(pPos);

        if(be instanceof MateriaJarQuadBlockEntity mjqbe && !pLevel.isClientSide()) {
            double hx = (((pHit.getLocation().x) % 1) + 2) % 1;
            double hz = (((pHit.getLocation().z) % 1) + 2) % 1;
            int ix = hx > 0.5 ? 1 : 0;
            int iz = hz > 0.5 ? 2 : 0;
            int slot = ix + iz;

            MateriaItem tis = mjqbe.getMateriaTypeInSlot(slot);

            if(stack.getItem() == Items.GLASS_BOTTLE && tis != null) {
                int extracted = mjqbe.drain(tis, stack.getCount(), false);
                stack.shrink(extracted);

                ItemEntity ie = new ItemEntity(pLevel,
                        pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                        new ItemStack(tis, extracted));
                pLevel.addFreshEntity(ie);
            }
            else if(stack.getItem() instanceof MateriaItem mi) {
                boolean quadHasType = mjqbe.getMateriaTypes().contains(mi);
                boolean slotMatchesType = mi == tis;

                if(!quadHasType || slotMatchesType) {
                    int inserted = mjqbe.fillSlot(slot, mi, stack.getCount(), false);
                    stack.shrink(inserted);

                    ItemEntity ie = new ItemEntity(pLevel,
                            pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                            new ItemStack(Items.GLASS_BOTTLE, inserted));
                    pLevel.addFreshEntity(ie);
                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pBuilder) {
        ItemStack stack = new ItemStack(BlockRegistry.MATERIA_JAR_QUAD.get(), 1);
        MateriaJarQuadBlockEntity mjqbe = (MateriaJarQuadBlockEntity) pBuilder.getParameter(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> output = new ArrayList<>();

        CompoundTag tag = mjqbe.getUpdateTag();
        stack.setTag(tag);

        output.add(stack);
        return output;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        if(pStack.hasTag()) {
            for(int i=0; i<4; i++) {
                if (pStack.getTag().contains("materiaType"+i)) {
                    CompoundTag entry = pStack.getTag().getCompound("materiaType"+i);

                    if(entry.getString("type").equals("empty")) continue;

                    MateriaItem materia = ItemRegistry.getMateriaMap(false, false)
                            .get(entry.getString("type"));
                    int count = entry.getInt("count");
                    String translationID = "";
                    if (materia instanceof AdmixtureItem)
                        translationID = "item.magichem.admixture_" + materia.getMateriaName();
                    else
                        translationID = "item.magichem.essentia_" + materia.getMateriaName();

                    pTooltip.add(
                            Component.translatable(translationID)
                                    .withStyle(ChatFormatting.GRAY)
                    );
                    pTooltip.add(
                            Component.literal(count + " / " + (materia instanceof EssentiaItem ? ServerConfig.materiaJarEssentiaCapacity : ServerConfig.materiaJarAdmixtureCapacity))
                                    .withStyle(ChatFormatting.DARK_GRAY)
                    );
                    pTooltip.add(
                            Component.literal(materia.getDisplayFormula())
                                    .withStyle(ChatFormatting.DARK_AQUA)
                    );
                }
            }
        }
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }

    static {
        SHAPE_JAR_1 = Block.box(1.0D, 0.0D,  1.0D, 7.0D, 8.0D, 7.0D);
        SHAPE_LID_1 = Block.box(2.0D, 6.5D, 2.0D, 6.0D, 9.5D, 6.0D);
        SHAPE_JAR_2 = Block.box(9.0D, 0.0D,  1.0D, 15.0D, 8.0D, 7.0D);
        SHAPE_LID_2 = Block.box(10.0D, 6.5D, 2.0D, 14.0D, 9.5D, 6.0D);
        SHAPE_JAR_3 = Block.box(1.0D, 0.0D,  9.0D, 7.0D, 8.0D, 15.0D);
        SHAPE_LID_3 = Block.box(2.0D, 6.5D, 10.0D, 6.0D, 9.5D, 14.0D);
        SHAPE_JAR_4 = Block.box(9.0D, 0.0D,  9.0D, 15.0D, 8.0D, 15.0D);
        SHAPE_LID_4 = Block.box(10.0D, 6.5D, 10.0D, 14.0D, 9.5D, 14.0D);
        SHAPE_AGGREGATE = Shapes.or(
                SHAPE_JAR_1, SHAPE_LID_1,
                SHAPE_JAR_2, SHAPE_LID_2,
                SHAPE_JAR_3, SHAPE_LID_3,
                SHAPE_JAR_4, SHAPE_LID_4);
    }
}
