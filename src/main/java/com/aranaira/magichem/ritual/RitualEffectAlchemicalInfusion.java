package com.aranaira.magichem.ritual;

import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.entities.utility.PresentItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RitualEffectAlchemicalInfusion extends RitualEffect {

    public RitualEffectAlchemicalInfusion(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext iRitualContext) {

        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext iRitualContext) {
        return 10;
    }

    @Override
    protected boolean modifyRitualReagentsAndPatterns(ItemStack dataStack, IRitualContext context) {
        //context.replaceReagents();

        return super.modifyRitualReagentsAndPatterns(dataStack, context);
    }
}
