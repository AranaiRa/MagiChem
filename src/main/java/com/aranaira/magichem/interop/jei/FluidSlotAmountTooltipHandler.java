package com.aranaira.magichem.interop.jei;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class FluidSlotAmountTooltipHandler implements IRecipeSlotRichTooltipCallback {
    private static FluidSlotAmountTooltipHandler _instance;
    public static FluidSlotAmountTooltipHandler getInstance() {
        if (_instance == null) _instance = new FluidSlotAmountTooltipHandler();
        return _instance;
    }

    @Override
    public void onRichTooltip(IRecipeSlotView view, ITooltipBuilder builder) {
        Optional<FluidStack> displayed = view.getDisplayedIngredient(ForgeTypes.FLUID_STACK);
        if (displayed.isEmpty())
            return;
        FluidStack fluidStack = displayed.get();
        builder.add(Component.literal(fluidStack.getAmount() + "mB").withStyle(ChatFormatting.GRAY));
    }
}
