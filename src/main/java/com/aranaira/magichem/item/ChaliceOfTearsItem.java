package com.aranaira.magichem.item;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ChaliceOfTearsItem extends Item {
    private static final ResourceLocation FACTION_COUNCIL = new ResourceLocation("mna:council");
    private static final ResourceLocation FACTION_FEY = new ResourceLocation("mna:fey");
    private static final ResourceLocation FACTION_DEMONS = new ResourceLocation("mna:demons");
    private static final ResourceLocation FACTION_UNDEAD = new ResourceLocation("mna:undead");

    public ChaliceOfTearsItem(Properties pProperties) {
        super(pProperties);
    }

    public static int getDamageAccumulationLimit() {
        if(!ServerConfig.HAS_CONFIG_LOADED) return Integer.MAX_VALUE;

        int a = ServerConfig.chaliceOfTearsThreshold;
        int b = ServerConfig.chaliceOfTearsThreshold + Math.round((ServerConfig.chaliceOfTearsEscalation / 100f) + 1f);
        int c = ServerConfig.chaliceOfTearsThreshold + Math.round((ServerConfig.chaliceOfTearsEscalation / 100f) * 2 + 1f);
        int d = ServerConfig.chaliceOfTearsThreshold + Math.round((ServerConfig.chaliceOfTearsEscalation / 100f) * 3 + 1f);
        int e = ServerConfig.chaliceOfTearsThreshold + Math.round((ServerConfig.chaliceOfTearsEscalation / 100f) * 4 + 1f);

        return a+b+c+d+e;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        processDrinkChalice(pLevel, pPlayer, pUsedHand);

        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        return processDrinkChalice(pContext.getLevel(), pContext.getPlayer(), pContext.getHand()) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    private boolean processDrinkChalice(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if(pPlayer.getCooldowns().isOnCooldown(ItemRegistry.CHALICE_OF_TEARS.get())) return false;

        pPlayer.getCooldowns().addCooldown(ItemRegistry.CHALICE_OF_TEARS.get(), ServerConfig.chaliceOfTearsCooldown * 20);

        return true;
    }
}
