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
        PresentItem catalystCore = new PresentItem(iRitualContext.getLevel(), iRitualContext.getCenter().getX(), iRitualContext.getCenter().getY(), iRitualContext.getCenter().getZ());
        catalystCore.setItem(new ItemStack(ItemRegistry.CATALYST_CORE.get()));
        catalystCore.setDeltaMovement(0.0d,0.0d,0.0d);
        iRitualContext.getLevel().addFreshEntity(catalystCore);

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
