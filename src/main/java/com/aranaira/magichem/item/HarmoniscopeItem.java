package com.aranaira.magichem.item;

import com.aranaira.magichem.entities.DestructiveHarmonicsEntity;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.registry.EntityRegistry;

import java.util.List;
import java.util.Random;

public class HarmoniscopeItem extends Item {
    private static final Random r = new Random();

    public HarmoniscopeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.harmoniscope.line1")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.harmoniscope.line2")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        pContext.getPlayer().swing(pContext.getHand());

        if(pContext.getPlayer().isCrouching()) {
            BlockState stateQuery = pContext.getLevel().getBlockState(pContext.getClickedPos());
            if(stateQuery.is(DestructiveHarmonicsEntity.TAG_LOW_PRIORITY) || stateQuery.is(DestructiveHarmonicsEntity.TAG_HIGH_PRIORITY)) {
                float f = 0.2f + r.nextFloat() * 0.1f;
                pContext.getLevel().playSound((Player) null, pContext.getPlayer().blockPosition(), SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 0.75F, f);
                pContext.getLevel().playSound((Player) null, pContext.getPlayer().blockPosition(), SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 0.75F, f * 0.5f);

                ItemEntity ie = new ItemEntity(pContext.getLevel(), pContext.getClickedPos().getX(), pContext.getClickedPos().getY(), pContext.getClickedPos().getZ(), new ItemStack(stateQuery.getBlock().asItem()));
                pContext.getLevel().destroyBlock(pContext.getClickedPos(), false);
                pContext.getLevel().addFreshEntity(ie);

                pContext.getPlayer().getCooldowns().addCooldown(ItemRegistry.HARMONISCOPE.get().asItem(), 6);
            }
        } else {
            DestructiveHarmonicsEntity dhe = new DestructiveHarmonicsEntity(EntitiesRegistry.DESTRUCTIVE_HARMONICS_ENTITY.get(), pContext.getLevel());
            dhe.setTargetPos(pContext.getClickedPos());
            dhe.setPos(pContext.getPlayer().getX(), pContext.getPlayer().getY(), pContext.getPlayer().getZ());
            pContext.getLevel().addFreshEntity(dhe);

            pContext.getPlayer().getCooldowns().addCooldown(ItemRegistry.HARMONISCOPE.get().asItem(), 96);

            float f = 0.2f + r.nextFloat() * 0.1f;
            pContext.getLevel().playSound((Player) null, pContext.getPlayer().blockPosition(), SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 2.0F, f);
            pContext.getLevel().playSound((Player) null, pContext.getPlayer().blockPosition(), SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 2.0F, f * 0.5f);
            pContext.getLevel().playSound((Player) null, pContext.getPlayer().blockPosition(), SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 2.0F, f * 0.25f);
        }

        return super.useOn(pContext);
    }
}
