package com.aranaira.magichem.item;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.IInteractsWithBlock;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ForkOfTheGulaporrigoItem extends Item implements IInteractsWithBlock {
    public static final TagKey<Block> TAG_CAKES = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "cakes"));
    private static final ResourceLocation FACTION_COUNCIL = new ResourceLocation("mna:council");
    private static final ResourceLocation FACTION_FEY = new ResourceLocation("mna:fey");
    private static final ResourceLocation FACTION_DEMONS = new ResourceLocation("mna:demons");
    private static final Random r = new Random();

    public ForkOfTheGulaporrigoItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("tooltip.magichem.fork_of_the_gulaporrigo.line1").withStyle(ChatFormatting.DARK_GRAY));
        pTooltipComponents.add(Component.translatable("tooltip.magichem.fork_of_the_gulaporrigo.line2").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if(!pLevel.isClientSide()){
            final ItemStack itemInHand = pPlayer.getItemInHand(pUsedHand);
            if (InventoryHelper.hasCustomModelData(itemInHand)) {
                FoodData foodData = pPlayer.getFoodData();
                foodData.setFoodLevel(Math.min(20, foodData.getFoodLevel() + 6));
                foodData.setSaturation(Math.min(20, foodData.getFoodLevel() + 8));

                int duration = 24000;
                int amplifier = 1;

                if (pPlayer.hasEffect(EffectInit.CIRCLE_OF_POWER.get())) {
                    duration *= 3;
                    amplifier += 1;
                }
                if (pPlayer.hasEffect(MobEffectsRegistry.MEMORIES_OF_DECADENCE.get())) {
                    duration *= 3;
                    amplifier += 2;
                }

                final LazyOptional<IPlayerProgression> lazyProg = pPlayer.getCapability(PlayerProgressionProvider.PROGRESSION);
                if(lazyProg.isPresent()) {
                    final Optional<IPlayerProgression> optProg = lazyProg.resolve();
                    if(optProg.isPresent()) {
                        final IPlayerProgression prog = optProg.get();
                        if(prog.getAlliedFaction().is(FACTION_COUNCIL)) {
                            amplifier *= 4;
                        } else if(prog.getAlliedFaction().is(FACTION_FEY)) {
                            amplifier *= 2;
                        }
                    }
                }

                pPlayer.addEffect(
                        new MobEffectInstance(EffectInit.MANA_BOOST.get(), duration, amplifier, false, false)
                );
                pLevel.playSound((Player)null, pPlayer.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.3f, 0.85f + r.nextFloat(0.3f));
                pLevel.playSound((Player)null, pPlayer.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.BLOCKS, 0.3f, 0.7f + r.nextFloat(0.6f));

                final CompoundTag nbt = itemInHand.getOrCreateTag();
                nbt.remove("CustomModelData");
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        use(pContext.getLevel(), pContext.getPlayer(), pContext.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public Pair<Boolean, InteractionResult> shouldInterceptEvent(PlayerInteractEvent.RightClickBlock pEvent) {
        BlockState query = pEvent.getLevel().getBlockState(pEvent.getPos());
        if(!query.is(TAG_CAKES))
            return new Pair<>(false, InteractionResult.PASS);
        return new Pair<>(!InventoryHelper.hasCustomModelData(pEvent.getItemStack()), InteractionResult.CONSUME);
    }

    @Override
    public boolean interceptEvent(PlayerInteractEvent.RightClickBlock pEvent) {
        BlockPos pos = pEvent.getPos();
        BlockState state = pEvent.getLevel().getBlockState(pos);
        ItemStack itemInHand = pEvent.getItemStack();

        if(InventoryHelper.hasCustomModelData(itemInHand)) {
            if(!pEvent.getLevel().isClientSide()) {
                final CompoundTag nbt = itemInHand.getOrCreateTag();
                nbt.remove("CustomModelData");
            }

            return false;
        }

        if(state.hasProperty(BlockStateProperties.BITES) && state.is(TAG_CAKES)) {
            int newBites = state.getValue(BlockStateProperties.BITES) + 1;
            if(newBites < 7) {
                BlockState newState = state.setValue(BlockStateProperties.BITES, newBites);
                pEvent.getLevel().setBlock(pos, newState, 3);
            } else {
                pEvent.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }

            if(!pEvent.getLevel().isClientSide()) {
                final CompoundTag nbt = itemInHand.getOrCreateTag();
                nbt.putInt("CustomModelData", 1);
                itemInHand.setTag(nbt);
            }

            return true;
        }
        return false;
    }
}
