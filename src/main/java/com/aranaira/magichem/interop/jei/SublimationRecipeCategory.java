package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.SublimationRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
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
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.interop.jei.FabricationRecipeCategory.getStackForWisdom;

public class SublimationRecipeCategory implements IRecipeCategory<SublimationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "sublimation");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_03.png");

    private IDrawable background;
    private final IDrawable icon;

    public SublimationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 180, 211);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_SUBLIMATION.get()));
    }

    @Override
    public RecipeType<SublimationRecipe> getRecipeType() {
        return JEIPlugin.SUBLIMATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.sublimation");
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
    public void draw(SublimationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        int stages = recipe.getStages(false).size();
        int verticalShift = (int)(21f * (5 - stages));
        int padding = new int[]{43, 22, 0, 0, 0}[stages - 1];

        int totalXP = 0;
        for(InfusionStage stage : recipe.getStages(false)) {
            totalXP += stage.experience;
        }

        //Slot backgrounds
        {
            guiGraphics.blit(TEXTURE, 11, 13 + verticalShift + padding, 112, 243, 8, 13);
            guiGraphics.blit(TEXTURE, 21, 1 + verticalShift + padding, 22, 219, 90, 37);

            if (stages >= 2) {
                guiGraphics.blit(TEXTURE, 7, 56 + verticalShift + padding, 120, 243, 12, 13);
                guiGraphics.blit(TEXTURE, 21, 44 + verticalShift + padding, 22, 219, 90, 37);
            }

            if (stages >= 3) {
                guiGraphics.blit(TEXTURE, 5, 99 + verticalShift + padding, 132, 243, 14, 13);
                guiGraphics.blit(TEXTURE, 21, 87 + verticalShift + padding, 22, 219, 90, 37);
            }

            if (stages >= 4) {
                guiGraphics.blit(TEXTURE, 1, 142 + verticalShift + padding, 146, 243, 18, 13);
                guiGraphics.blit(TEXTURE, 21, 130 + verticalShift + padding, 22, 219, 90, 37);
            }

            if (stages >= 5) {
                guiGraphics.blit(TEXTURE, 4, 185 + verticalShift + padding, 164, 243, 15 , 13);
                guiGraphics.blit(TEXTURE, 21, 173 + verticalShift + padding, 22, 219, 90, 37);
            }
        }

        //Output area
        {
            boolean hasStone = recipe.getWisdom() > 0 && recipe.getWisdom() < 6;
            boolean hasAdvancement = recipe.isAdvancementRequired() || recipe.isForbiddenByAdvancement();

            guiGraphics.blit(TEXTURE, 135, 87 - verticalShift + padding, 0, 234, 22, 22);
            guiGraphics.blit(TEXTURE, 112, 112 - verticalShift + padding, 189, 0, 67, 83);

            guiGraphics.blit(TEXTURE, 112, 112 - verticalShift + padding, 189, 0, 67, 83);

            if(hasAdvancement) {
                guiGraphics.blit(TEXTURE, 165, 94 - verticalShift + padding, 230, 248, 8, 8);
                if(recipe.isAdvancementRequired()) {
                    guiGraphics.blit(TEXTURE, 160, 75 - verticalShift + padding, 238, 220, 18, 18);
                }
                if(recipe.isForbiddenByAdvancement()) {
                    guiGraphics.blit(TEXTURE, 160, 104 - verticalShift + padding, 238, 238, 18, 18);
                }
            }

            if(hasStone) {
                guiGraphics.blit(TEXTURE, 148, 141 - verticalShift + padding, 0, 0, 24, 18);
            }

            if(totalXP > 0) {
                guiGraphics.blit(TEXTURE, 114, 200 - verticalShift + padding, 112, 236, 7, 7);
                guiGraphics.drawString(Minecraft.getInstance().font, Component.literal(totalXP + "xp"), 124, 196 - verticalShift + padding, 0x21761f, false);
                guiGraphics.drawString(Minecraft.getInstance().font, Component.literal((totalXP * ServerConfig.fluidPerXPPoint) + "mB"), 124, 206 - verticalShift + padding, 0x21761f, false);
            }
        }

        //Tier label; stolen from MnA code
        {
            Minecraft mc = Minecraft.getInstance();
            int tier = recipe.getTier();
            int playerTier = ((IPlayerProgression)mc.player.getCapability(PlayerProgressionProvider.PROGRESSION).resolve().get()).getTier();
            int color = tier <= playerTier ? FastColor.ARGB32.color(255, 0, 128, 0) : FastColor.ARGB32.color(255, 255, 0, 0);

            Component name = Component.translatable(recipe.getAlchemyObject().getDescriptionId().toString());
            Component tierPrompt = Component.translatable("gui.mna.item-tier", new Object[]{tier});

            int stringWidth = mc.font.width(name);
            int textX = this.getWidth() / 2 - stringWidth / 2;
            int textY = 2;

            if(recipe.getStages(false).size() == 5)
                guiGraphics.drawString(mc.font, tierPrompt, this.getWidth() / 2 - mc.font.width(tierPrompt) / 2, -12, color, false);
            else {
                guiGraphics.drawString(mc.font, name, textX, textY, FastColor.ARGB32.color(255, 255, 255, 255), false);
                guiGraphics.drawString(mc.font, tierPrompt, this.getWidth() / 2 - mc.font.width(tierPrompt) / 2, 12, color, false);
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SublimationRecipe recipe, IFocusGroup group) {

        int stages = recipe.getStages(false).size();
        int verticalShift = (int)(21f * (5 - stages));
        int padding = new int[]{43, 22, 0, 0, 0}[stages - 1];

        builder.addSlot(RecipeIngredientRole.OUTPUT,138,90 - verticalShift + padding).addItemStack(recipe.getAlchemyObject());

        for(int si=0; si<stages; si++) {
            InfusionStage stage = recipe.getStages(false).get(si);

            for(int ci=0; ci<stage.componentItems.size(); ci++) {
                builder.addSlot(RecipeIngredientRole.INPUT, 22 + (ci * 18), 2 + (si * 43) + verticalShift + padding).addItemStack(stage.componentItems.get(ci));
            }

            for(int ci=0; ci<stage.componentMateria.size(); ci++) {
                builder.addSlot(RecipeIngredientRole.INPUT, 22 + (ci * 18), 21 + (si * 43) + verticalShift + padding).addItemStack(stage.componentMateria.get(ci));
            }
        }

        if(recipe.getWisdom() < 6 && recipe.getWisdom() > 0) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 149 ,142 - verticalShift + padding).addItemStack(getStackForWisdom(recipe.getWisdom()));
        }
    }

    @Override
    public List<Component> getTooltipStrings(SublimationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> in = IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
        ArrayList<Component> out = new ArrayList<>();

        if(in.size() > 0) {
            for (Object o : in.stream().toArray()) {
                if (o instanceof Component c) {
                    out.add(c);
                }
            }
        }

        int stages = recipe.getStages(false).size();
        int verticalShift = (int)(21f * (5 - stages));
        int padding = new int[]{43, 22, 0, 0, 0}[stages - 1];

        if(recipe.isAdvancementRequired()) {
            boolean xCoord = mouseX >= 160 && mouseX <= 178;
            boolean yCoord = mouseY >= 75 - verticalShift + padding && mouseY <= 93 - verticalShift + padding;

            if(xCoord && yCoord) {
                out.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.jei.advancement_required.part1"))
                        .append(Component.translatable("tooltip.magichem.jei.advancement_required.required").withStyle(ChatFormatting.GREEN))
                        .append(Component.translatable("tooltip.magichem.jei.advancement_required.part2"))
                );
                out.add(Component.empty());

                final ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if(connection != null) {
                    final Advancement advancement = connection.getAdvancements().getAdvancements().get(recipe.getRequiredAdvancement());

                    if(advancement != null) {
                        out.add(Component.empty()
                                .append(advancement.getDisplay().getTitle().copy().withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                                .append(advancement.getDisplay().getDescription().copy().withStyle(ChatFormatting.WHITE))
                        );
                    } else {
                        out.add(Component.empty()
                                .append(Component.literal("ERROR: Advancement not found!").withStyle(ChatFormatting.RED))
                        );
                    }
                }
            }
        }
        if(recipe.isForbiddenByAdvancement()) {
            boolean xCoord = mouseX >= 160 && mouseX <= 178;
            boolean yCoord = mouseY >= 104 - verticalShift + padding && mouseY <= 122 - verticalShift + padding;

            if(xCoord && yCoord) {
                out.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.jei.advancement_forbidden.part1"))
                        .append(Component.translatable("tooltip.magichem.jei.advancement_forbidden.forbidden").withStyle(ChatFormatting.RED))
                        .append(Component.translatable("tooltip.magichem.jei.advancement_forbidden.part2"))
                );
                out.add(Component.empty());

                final ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if(connection != null) {
                    final Advancement advancement = connection.getAdvancements().getAdvancements().get(recipe.getForbiddenAdvancement());

                    if(advancement != null) {
                        out.add(Component.empty()
                                .append(advancement.getDisplay().getTitle().copy().withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                                .append(advancement.getDisplay().getDescription().copy().withStyle(ChatFormatting.WHITE))
                        );
                    } else {
                        out.add(Component.empty()
                                .append(Component.literal("ERROR: Advancement not found!").withStyle(ChatFormatting.RED))
                        );
                    }
                }
            }
        }

        return out.stream().toList();
    }
}
