package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.ExaltationRecipe;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ExaltationRecipeCategory implements IRecipeCategory<ExaltationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "exaltation");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_04.png");

    private final IDrawable background;
    private final IDrawable icon;

    public ExaltationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 110, 96, 121);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_EXALTATION.get()));
    }

    @Override
    public RecipeType<ExaltationRecipe> getRecipeType() {
        return JEIPlugin.EXALTATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.exaltation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ExaltationRecipe recipe, IFocusGroup group) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 15).addItemStack(new ItemStack(recipe.getItemType()));
        builder.addSlot(RecipeIngredientRole.INPUT, 76, 15).addItemStack(new ItemStack(recipe.getMateriaType()));
        builder.addSlot(RecipeIngredientRole.OUTPUT,40,99).addItemStack(recipe.getResultItem());
        builder.addSlot(RecipeIngredientRole.INPUT, 4096, 4096).addFluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), recipe.getSlurryRequired());
    }

    public void draw(ExaltationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);

        int shift = 0;
        if(recipe.usesEldrinType(Affinity.ENDER)) {
            gui.blit(TEXTURE, 3, 35, 192, 56, 7, 7);
            shift += 8;
        }
        if(recipe.usesEldrinType(Affinity.EARTH)) {
            gui.blit(TEXTURE, 3+shift, 35, 199, 56, 7, 7);
            shift += 8;
        }
        if(recipe.usesEldrinType(Affinity.WATER)) {
            gui.blit(TEXTURE, 3+shift, 35, 206, 56, 7, 7);
            shift += 8;
        }
        if(recipe.usesEldrinType(Affinity.WIND)) {
            gui.blit(TEXTURE, 3+shift, 35, 213, 56, 7, 7);
            shift += 8;
        }
        if(recipe.usesEldrinType(Affinity.FIRE)) {
            gui.blit(TEXTURE, 3+shift, 35, 220, 56, 7, 7);
            shift += 8;
        }
        if(recipe.usesEldrinType(Affinity.ARCANE)) {
            gui.blit(TEXTURE, 3+shift, 35, 227, 56, 7, 7);
        }
        final Font font = Minecraft.getInstance().font;

        gui.drawString(font, "x"+recipe.getItemsRequired(), 22, 15, 0xff000000, false);
        String materiaText = recipe.getMateriaRequired()+"x";
        gui.drawString(font, materiaText, 75 - font.width(materiaText), 24, 0xff000000, false);

        gui.drawString(font, recipe.getEldrinRequired()+"", 3, 44, 0xff000000, false);
        String slurryText = recipe.getSlurryRequired()+"";
        gui.drawString(font, slurryText, 94 - font.width(slurryText), 44, 0xff000000, false);
        gui.drawString(font, "mB", 94 - font.width("mB"), 54, 0xff000000, false);

        //Tier label; stolen from MnA code
        {
            Minecraft mc = Minecraft.getInstance();
            int tier = recipe.getTier();
            int playerTier = ((IPlayerProgression)mc.player.getCapability(PlayerProgressionProvider.PROGRESSION).resolve().get()).getTier();
            int color = tier <= playerTier ? FastColor.ARGB32.color(255, 0, 128, 0) : FastColor.ARGB32.color(255, 255, 0, 0);

            Component tierPrompt = Component.translatable("gui.mna.item-tier", new Object[]{tier});

            int stringWidth = mc.font.width(tierPrompt);
            int textX = this.getWidth() / 2 - stringWidth / 2;
            int textY = 2;

            gui.drawString(mc.font, tierPrompt, this.getWidth() / 2 - mc.font.width(tierPrompt) / 2, 2, color, false);
        }
    }

    @Override
    public List<Component> getTooltipStrings(ExaltationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> in = IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
        ArrayList<Component> out = new ArrayList<>();

        if(in.size() > 0) {
            for (Object o : in.stream().toArray()) {
                if (o instanceof Component c) {
                    out.add(c);
                }
            }
        }

        boolean xCoord = mouseX >= 3 && mouseX <= 11 + (recipe.getEldrinTypeCount() - 1) * 8;
        boolean yCoord = mouseY >= 24 && mouseY <= 32;

        if(xCoord && yCoord) {
            MutableComponent tt = Component.empty()
                    .append(Component.translatable("tooltip.magichem.jei.exaltation.eldrin.part1"));

            int count = 0;
            for(Affinity aff : recipe.getEldrinTypes()) {
                if(count > 0)  tt.append(" + ");

                String elem = switch(aff) {
                    case ENDER -> "ender";
                    case EARTH -> "earth";
                    case WATER -> "water";
                    case WIND -> "air";
                    case FIRE -> "fire";
                    case ARCANE -> "arcane";
                    default -> "what";
                };

                tt.append(Component.translatable("tooltip.magichem.jei.exaltation.eldrin."+elem).withStyle(ChatFormatting.GOLD));

                count++;
            }
            tt.append(Component.translatable("tooltip.magichem.jei.exaltation.eldrin.part2"));

            out.add(tt);
        }

        xCoord = mouseX >= 86 && mouseX <= 94;

        if(xCoord && yCoord) {
            out.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.jei.exaltation.slurry.part1"))
                    .append(Component.translatable("tooltip.magichem.jei.exaltation.slurry.part2").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("tooltip.magichem.jei.exaltation.slurry.part3"))
            );
        }

        return out.stream().toList();
    }
}
