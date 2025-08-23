package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.DistillationSourceCategory;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.recipe.VitriolationRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
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
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class FabricationRecipeCategory implements IRecipeCategory<DistillationFabricationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "fabrication");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_01.png");

    private final IDrawable background;
    private final IDrawable icon;

    public FabricationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 0, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_FABRICATION.get()));
    }

    @Override
    public RecipeType<DistillationFabricationRecipe> getRecipeType() {
        return JEIPlugin.FABRICATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.fabrication");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DistillationFabricationRecipe recipe, IFocusGroup group) {

        builder.addSlot(RecipeIngredientRole.OUTPUT,40,88).addItemStack(recipe.getAlchemyObject());

        int i=0;
        for(ItemStack stack : recipe.getComponentMateria()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 4 + i*18, 4).addItemStack(stack);
            i++;
        }

        if(recipe.getWisdom() < 6 && recipe.getWisdom() > 0) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 51, 48).addItemStack(getStackForWisdom(recipe.getWisdom()));
        }
    }

    public void draw(DistillationFabricationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            if(recipe.getOutputRate() < 1.0f) {
                int amt = (int)Math.round(1f / recipe.getOutputRate());

                Component oRateComponent = Component.literal("x"+amt);

                gui.drawString(mc.font, oRateComponent, 62, 91, 0x000000, false);
            }
        }

        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);

        boolean hasStone = recipe.getWisdom() > 0 && recipe.getWisdom() < 6;
        boolean hasAdvancement = recipe.isAdvancementRequired() || recipe.isForbiddenByAdvancement();
        if(hasStone || hasAdvancement) {
            gui.blit(TEXTURE, 50, 47, 96, 21, 24, 18);
            if(recipe.isAdvancementRequired() || recipe.isForbiddenByAdvancement()) {
                gui.blit(TEXTURE, 67 - (hasStone ? 0 : 8), 52, 230, 248, 8, 8);
                if(recipe.isAdvancementRequired() && recipe.isForbiddenByAdvancement()) {
                    gui.blit(TEXTURE, 75, 27, 238, 220, 18, 18);
                    gui.blit(TEXTURE, 75, 47, 238, 238, 18, 18);
                }
                else if(recipe.isAdvancementRequired())
                    gui.blit(TEXTURE, 75, 47, 238, 220, 18, 18);
                else if(recipe.isForbiddenByAdvancement())
                    gui.blit(TEXTURE, 75, 47, 238, 238, 18, 18);
            }
        }
    }

    @Override
    public List<Component> getTooltipStrings(DistillationFabricationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> in = IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
        ArrayList<Component> out = new ArrayList<>();

        if(in.size() > 0) {
            for (Object o : in.stream().toArray()) {
                if (o instanceof Component c) {
                    out.add(c);
                }
            }
        }

        if(recipe.isAdvancementRequired() && recipe.isForbiddenByAdvancement()) {
            //forbidden
            {
                boolean xCoord = mouseX >= 75 && mouseX <= 93;
                boolean yCoord = mouseY >= 47 && mouseY <= 65;

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
                        final Component chatComponent = advancement.getChatComponent();

                        out.add(Component.empty()
                                .append(advancement.getDisplay().getTitle().copy().withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                                .append(advancement.getDisplay().getDescription().copy().withStyle(ChatFormatting.WHITE))
                        );
                    }
                }
            }
            //required
            {
                boolean xCoord = mouseX >= 75 && mouseX <= 93;
                boolean yCoord = mouseY >= 27 && mouseY <= 45;

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
                        final Component chatComponent = advancement.getChatComponent();

                        out.add(Component.empty()
                                .append(advancement.getDisplay().getTitle().copy().withStyle(ChatFormatting.GOLD))
                                .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                                .append(advancement.getDisplay().getDescription().copy().withStyle(ChatFormatting.WHITE))
                        );
                    }
                }
            }
        }
        else if(recipe.isAdvancementRequired()) {
            boolean xCoord = mouseX >= 75 && mouseX <= 93;
            boolean yCoord = mouseY >= 47 && mouseY <= 65;

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
                    final Component chatComponent = advancement.getChatComponent();

                    out.add(Component.empty()
                            .append(advancement.getDisplay().getTitle().copy().withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(advancement.getDisplay().getDescription().copy().withStyle(ChatFormatting.WHITE))
                    );
                }
            }
        }
        else if(recipe.isForbiddenByAdvancement()) {
            boolean xCoord = mouseX >= 75 && mouseX <= 93;
            boolean yCoord = mouseY >= 47 && mouseY <= 65;

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
                    final Component chatComponent = advancement.getChatComponent();

                    out.add(Component.empty()
                            .append(advancement.getDisplay().getTitle().copy().withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(advancement.getDisplay().getDescription().copy().withStyle(ChatFormatting.WHITE))
                    );
                }
            }
        }

        return out.stream().toList();
    }

    private static ItemStack
            ASHEN = ItemStack.EMPTY,
            BLEACHED = ItemStack.EMPTY,
            YELLOWED = ItemStack.EMPTY,
            FLUSHED = ItemStack.EMPTY,
            PHILOSOPHERS = ItemStack.EMPTY;
    public static ItemStack getStackForWisdom(int pWisdom) {
        if(pWisdom == 1) {
            if(ASHEN.isEmpty()) {
                ASHEN = new ItemStack(ItemRegistry.ASHEN_WISDOM_STONE.get());
            }
            return ASHEN;
        }
        else if(pWisdom == 2) {
            if(BLEACHED.isEmpty()) {
                BLEACHED = new ItemStack(ItemRegistry.BLEACHED_WISDOM_STONE.get());
            }
            return BLEACHED;
        }
        else if(pWisdom == 3) {
            if(YELLOWED.isEmpty()) {
                YELLOWED = new ItemStack(ItemRegistry.YELLOWED_WISDOM_STONE.get());
            }
            return YELLOWED;
        }
        else if(pWisdom == 4) {
            if(FLUSHED.isEmpty()) {
                FLUSHED = new ItemStack(ItemRegistry.FLUSHED_WISDOM_STONE.get());
            }
            return FLUSHED;
        }
        else if(pWisdom == 5) {
            if(PHILOSOPHERS.isEmpty()) {
                PHILOSOPHERS = new ItemStack(ItemRegistry.PHILOSOPHERS_STONE.get());
            }
            return PHILOSOPHERS;
        }

        return ItemStack.EMPTY;
    }
}
