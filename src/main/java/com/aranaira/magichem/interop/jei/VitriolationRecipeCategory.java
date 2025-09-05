package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.VitriolationRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mutable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VitriolationRecipeCategory implements IRecipeCategory<VitriolationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "vitriolation");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_04.png");

    private final IDrawable background;
    private final IDrawable icon;

    public VitriolationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 0, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_VITRIOLATION.get()));
    }

    @Override
    public RecipeType<VitriolationRecipe> getRecipeType() {
        return JEIPlugin.VITRIOLATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.vitriolation");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VitriolationRecipe recipe, IFocusGroup group) {
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 4).addItemStack(recipe.getInputItem());
        if(recipe.hasInputFluidOverride()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 73, 4).addFluidStack(recipe.getInputFluidOverride(), recipe.getBaseFluidConsumed());
            builder.addSlot(RecipeIngredientRole.INPUT, 73, 4096).addItemStack(recipe.getInputFluidOverride().getBucket().getDefaultInstance()); // hidden bucket item for reference
        } else {
            int baseSize = recipe.getBaseFluidConsumed();
            IRecipeSlotBuilder fluidSlot = builder.addSlot(RecipeIngredientRole.INPUT, 73, 4);
            IRecipeSlotBuilder hiddenItemSlot = builder.addSlot(RecipeIngredientRole.INPUT, 73, 4096);
            int baseStrength = recipe.getMinimumAcidStrength();
            for (Fluid fluid : VitriolationRecipe.getAllFluidsOfAcidStrength(baseStrength)) {
                if (!fluid.isSource(fluid.defaultFluidState())) continue;
                fluidSlot.addFluidStack(fluid, baseSize);
                hiddenItemSlot.addItemStack(new ItemStack(fluid.getBucket()));
            }
            if (baseStrength < 5) {
                for (Fluid fluid : VitriolationRecipe.getAllFluidsOfAcidStrength(baseStrength + 1)) {
                    if (!fluid.isSource(fluid.defaultFluidState())) continue;
                    fluidSlot.addFluidStack(fluid, baseSize / 4);
                    hiddenItemSlot.addItemStack(new ItemStack(fluid.getBucket()));
                }
            }
            if (baseStrength < 4) {
                for (int level = baseStrength + 2; level <= 5; level++) {
                    for (Fluid fluid : VitriolationRecipe.getAllFluidsOfAcidStrength(level)) {
                        if (!fluid.isSource(fluid.defaultFluidState())) continue;
                        fluidSlot.addFluidStack(fluid, 1);
                        hiddenItemSlot.addItemStack(new ItemStack(fluid.getBucket()));
                    }
                }
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 88).addItemStack(recipe.getResultItem());
        if(recipe.hasResultFluid()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 88).addFluidStack(recipe.getResultFluid().getFluid(), recipe.getResultFluid().getAmount());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 4096).addItemStack(new ItemStack(recipe.getResultFluid().getFluid().getBucket()));
        }
    }

    public void draw(VitriolationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);
        Minecraft mc = Minecraft.getInstance();

        if(recipe.hasResultFluid())
            gui.blit(TEXTURE, 0, 82, 192, 0, 48, 28);
        if(recipe.hasResultItem())
            gui.blit(TEXTURE, 48, 82, 192, 28, 48, 28);
        if(recipe.hasInputFluidOverride())
            gui.blit(TEXTURE, 72, 3, 135, 3, 18, 18);

        if (mc.font != null) {
            gui.drawString(mc.font, recipe.hasInputFluidOverride() ? "-" : ""+recipe.getMinimumAcidStrength(), 20, 8, 0xff000000, false);

            int opTicks = recipe.getCraftTicks();
            int secWhole = opTicks / 20;
            int secPartial = (opTicks % 20) * 5;
            String out = "?";
            if(secWhole >= 60) {
                int minWhole = secWhole / 60;
                if(minWhole >= 60) {
                    int hourWhole = minWhole / 60;
                    out = hourWhole+"h" + (minWhole % 60 > 0 ? " "+(minWhole % 60)+"m" : "");
                } else {
                    out = minWhole+"m" + (secWhole % 60 > 0 ? " "+(secWhole % 60)+"s" : "");
                }
            } else {
                if(secPartial > 0) {
                    out = secWhole + "." + (secPartial < 10 ? "0" + secPartial : secPartial) + "s";
                } else {
                    out = secWhole + "s";
                }
            }
            gui.drawString(mc.font ,out, 7, 23, 0xff000000, false);
        }
    }

    @Override
    public List<Component> getTooltipStrings(VitriolationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> in = IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
        ArrayList<Component> out = new ArrayList<>();

        boolean xCoord = mouseX >= 4 && mouseX <= 30;
        boolean yCoord = mouseY >= 4 && mouseY <= 20;
        if(in.size() > 0) {
            for (Object o : in.stream().toArray()) {
                if (o instanceof Component c) {
                    out.add(c);
                }
            }
        }

        if(xCoord && yCoord) {
            if(recipe.hasInputFluidOverride()) {
                ResourceLocation key = ForgeRegistries.FLUIDS.getKey(recipe.getInputFluidOverride());
                out.add(Component.translatable("tooltip.magichem.jei.vitriolation.override.part1")
                        .append(Component.literal(recipe.getFluidConsumed(recipe.getMinimumAcidStrength()) + "mB").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.jei.vitriolation.override.part2"))
                        .append(Component.translatable(key.toString()).withStyle(ChatFormatting.GOLD))
                );
            } else {
                out.add(Component.translatable("tooltip.magichem.jei.vitriolation.acid.line1.part1")
                        .append(Component.literal(recipe.getFluidConsumed(recipe.getMinimumAcidStrength()) + "mB").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.jei.vitriolation.acid.line1.part2"))
                );

                MutableComponent formattedEqualStrength = Component.empty();
                boolean first = true;
                for(FluidType ft : VitriolationRecipe.getAllFluidTypesOfAcidStrength(recipe.getMinimumAcidStrength())) {
                    if(!first) formattedEqualStrength.append(", ");
                    ResourceLocation key = ForgeRegistries.FLUID_TYPES.get().getKey(ft);
                    formattedEqualStrength.append(Component.translatable(key.getPath()).withStyle(first ? ChatFormatting.GOLD : ChatFormatting.WHITE));
                    first = false;
                }

                out.add(formattedEqualStrength);
                out.add(Component.empty());

                if(recipe.getMinimumAcidStrength() < 5) {
                    out.add(Component.translatable("tooltip.magichem.jei.vitriolation.acid.line2.part1")
                            .append(Component.literal((recipe.getFluidConsumed(recipe.getMinimumAcidStrength()) / 4) + "mB").withStyle(ChatFormatting.DARK_AQUA))
                            .append(Component.translatable("tooltip.magichem.jei.vitriolation.acid.line2.part2"))
                    );

                    MutableComponent formattedOneHigher = Component.empty();
                    first = true;
                    for (FluidType ft : VitriolationRecipe.getAllFluidTypesOfAcidStrength(recipe.getMinimumAcidStrength() + 1)) {
                        if (!first) formattedOneHigher.append(", ");
                        ResourceLocation key = ForgeRegistries.FLUID_TYPES.get().getKey(ft);
                        formattedOneHigher.append(Component.translatable(key.getPath()).withStyle(first ? ChatFormatting.GOLD : ChatFormatting.WHITE));
                        first = false;
                    }
                    out.add(formattedOneHigher);
                    out.add(Component.empty());
                }

                if(recipe.getMinimumAcidStrength() < 4) {
                    out.add(Component.translatable("tooltip.magichem.jei.vitriolation.acid.line3"));

                    MutableComponent formattedTwoHigher = Component.empty();
                    first = true;
                    for(int strength=recipe.getMinimumAcidStrength()+2; strength<=5; strength++) {
                        for (FluidType ft : VitriolationRecipe.getAllFluidTypesOfAcidStrength(strength)) {
                            if (!first) formattedTwoHigher.append(",  ").withStyle(ChatFormatting.DARK_GRAY);
                            ResourceLocation key = ForgeRegistries.FLUID_TYPES.get().getKey(ft);
                            formattedTwoHigher.append(Component.translatable(key.getPath()).withStyle(ChatFormatting.GOLD));
                            first = false;
                        }
                    }
                    out.add(formattedTwoHigher);
                }
            }
        }

        return out.stream().toList();
    }
}
