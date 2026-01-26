package com.aranaira.magichem.interop;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.gui.AlchemicalNexusScreen;
import com.aranaira.magichem.interop.jei.*;
import com.aranaira.magichem.recipe.*;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.blocks.BlockInit;
import com.mna.items.ItemInit;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static RecipeType<FulminationRecipe> FULMINATION_TYPE =
            new RecipeType<>(FulminationRecipeCategory.UID, FulminationRecipe.class);
    public static RecipeType<DistillationFabricationRecipe> DISTILLATION_TYPE =
            new RecipeType<>(DistillationRecipeCategory.UID, DistillationFabricationRecipe.class);
    public static RecipeType<FluidDistillationFabricationRecipe> FLUID_DISTILLATION_TYPE =
            new RecipeType<>(FluidDistillationRecipeCategory.UID, FluidDistillationFabricationRecipe.class);
    public static RecipeType<DistillationFabricationRecipe> FABRICATION_TYPE =
            new RecipeType<>(FabricationRecipeCategory.UID, DistillationFabricationRecipe.class);
    public static RecipeType<FluidDistillationFabricationRecipe> FLUID_FABRICATION_TYPE =
            new RecipeType<>(FluidFabricationRecipeCategory.UID, FluidDistillationFabricationRecipe.class);
    public static RecipeType<FixationSeparationRecipe> FIXATION_TYPE =
            new RecipeType<>(FixationRecipeCategory.UID, FixationSeparationRecipe.class);
    public static RecipeType<FixationSeparationRecipe> SEPARATION_TYPE =
            new RecipeType<>(SeparationRecipeCategory.UID, FixationSeparationRecipe.class);
    public static RecipeType<SublimationRitualRecipe> SUBLIMATION_RITUAL_TYPE =
            new RecipeType<>(SublimationRitualRecipeCategory.UID, SublimationRitualRecipe.class);
    public static RecipeType<SublimationRecipe> SUBLIMATION_TYPE =
            new RecipeType<>(SublimationRecipeCategory.UID, SublimationRecipe.class);
    public static RecipeType<ExaltationRecipe> EXALTATION_TYPE =
            new RecipeType<>(ExaltationRecipeCategory.UID, ExaltationRecipe.class);
    public static RecipeType<ColorationRecipe> COLORATION_TYPE =
            new RecipeType<>(ColorationRecipeCategory.UID, ColorationRecipe.class);
    public static RecipeType<ConjurationRecipe> CONJURATION_TYPE =
            new RecipeType<>(ConjurationRecipeCategory.UID, ConjurationRecipe.class);
    public static RecipeType<AnointingRecipe> ANOINTING_TYPE =
            new RecipeType<>(AnointingRecipeCategory.UID, AnointingRecipe.class);
    public static RecipeType<IlluminationRecipe> ILLUMINATION_TYPE =
            new RecipeType<>(IlluminationRecipeCategory.UID, IlluminationRecipe.class);
    public static RecipeType<VitriolationRecipe> VITRIOLATION_TYPE =
            new RecipeType<>(VitriolationRecipeCategory.UID, VitriolationRecipe.class);
    public static RecipeType<InfoPanelRecipe> INFO_PANEL_TYPE =
            new RecipeType<>(InfoPanelRecipeCategory.UID, InfoPanelRecipe.class);
    public static RecipeType<ConstructStudyMaterialRecipe> CONSTRUCT_STUDY_MATERIAL_TYPE =
            new RecipeType<>(ConstructStudyMaterialRecipeCategory.UID, ConstructStudyMaterialRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MagiChemMod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new
                FulminationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                DistillationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                FluidDistillationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                FabricationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                FluidFabricationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                FixationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                SeparationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                SublimationRitualRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                SublimationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                ExaltationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                ColorationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                ConjurationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                AnointingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                InfoPanelRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                IlluminationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                VitriolationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                ConstructStudyMaterialRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        List<FulminationRecipe> recipesFulmination = rm.getAllRecipesFor(FulminationRecipe.Type.INSTANCE);
        registration.addRecipes(FULMINATION_TYPE, recipesFulmination);

        List<DistillationFabricationRecipe> recipesDistillation = rm.getAllRecipesFor(DistillationFabricationRecipe.Type.INSTANCE);
        registration.addRecipes(DISTILLATION_TYPE, recipesDistillation);

        List<FluidDistillationFabricationRecipe> recipesFluidDistillation = rm.getAllRecipesFor(FluidDistillationFabricationRecipe.Type.INSTANCE);
        registration.addRecipes(FLUID_DISTILLATION_TYPE, recipesFluidDistillation);

        List<DistillationFabricationRecipe> recipesFabrication = new ArrayList<>();
        for(DistillationFabricationRecipe acr : recipesDistillation) {
            if(!acr.getIsDistillOnly())  recipesFabrication.add(acr);
        }
        registration.addRecipes(FABRICATION_TYPE, recipesFabrication);

        List<FluidDistillationFabricationRecipe> recipesFluidFabrication = new ArrayList<>();
        for(FluidDistillationFabricationRecipe acr : recipesFluidDistillation) {
            if(!acr.getIsDistillOnly())  recipesFluidFabrication.add(acr);
        }
        registration.addRecipes(FLUID_FABRICATION_TYPE, recipesFluidFabrication);

        List<FixationSeparationRecipe> recipesFixationSeparation = rm.getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE);
        registration.addRecipes(FIXATION_TYPE, recipesFixationSeparation);
        registration.addRecipes(SEPARATION_TYPE, recipesFixationSeparation);

        List<SublimationRitualRecipe> recipesSublimationRitual = rm.getAllRecipesFor(SublimationRitualRecipe.Type.INSTANCE);
        registration.addRecipes(SUBLIMATION_RITUAL_TYPE, recipesSublimationRitual);

        List<SublimationRecipe> recipesSublimation = rm.getAllRecipesFor(SublimationRecipe.Type.INSTANCE);
        registration.addRecipes(SUBLIMATION_TYPE, recipesSublimation);

        List<ExaltationRecipe> recipesExaltation = rm.getAllRecipesFor(ExaltationRecipe.Type.INSTANCE);
        registration.addRecipes(EXALTATION_TYPE, recipesExaltation);

        List<ColorationRecipe> recipesColoration = rm.getAllRecipesFor(ColorationRecipe.Type.INSTANCE);
        registration.addRecipes(COLORATION_TYPE, recipesColoration);

        List<ConjurationRecipe> recipesConjuration = rm.getAllRecipesFor(ConjurationRecipe.Type.INSTANCE);
        registration.addRecipes(CONJURATION_TYPE, recipesConjuration);

        List<AnointingRecipe> recipesAnointing = rm.getAllRecipesFor(AnointingRecipe.Type.INSTANCE);
        registration.addRecipes(ANOINTING_TYPE, recipesAnointing);

        List<InfoPanelRecipe> recipesInfoPanel = rm.getAllRecipesFor(InfoPanelRecipe.Type.INSTANCE);
        registration.addRecipes(INFO_PANEL_TYPE, recipesInfoPanel);

        List<ConstructStudyMaterialRecipe> recipesConstructStudyMaterial = rm.getAllRecipesFor(ConstructStudyMaterialRecipe.Type.INSTANCE);
        registration.addRecipes(CONSTRUCT_STUDY_MATERIAL_TYPE, recipesConstructStudyMaterial);

        List<IlluminationRecipe> recipesIllumination = rm.getAllRecipesFor(IlluminationRecipe.Type.INSTANCE);
        registration.addRecipes(ILLUMINATION_TYPE, recipesIllumination);

        List<VitriolationRecipe> recipesVitriolation = rm.getAllRecipesFor(VitriolationRecipe.Type.INSTANCE);
        registration.addRecipes(VITRIOLATION_TYPE, recipesVitriolation);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.SKYWRATH_ALTAR.get(), 1), FULMINATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.ALEMBIC.get(), 1), DISTILLATION_TYPE, FLUID_DISTILLATION_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.DISTILLERY.get(), 1), DISTILLATION_TYPE, FLUID_DISTILLATION_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.GRAND_DISTILLERY.get(), 1), DISTILLATION_TYPE, FLUID_DISTILLATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.CENTRIFUGE.get(), 1), SEPARATION_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.GRAND_CENTRIFUGE.get(), 1), SEPARATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.FUSERY.get(), 1), FIXATION_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.GRAND_FUSERY.get(), 1), FIXATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.CIRCLE_FABRICATION.get(), 1), FABRICATION_TYPE, FLUID_FABRICATION_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.GRAND_CIRCLE_FABRICATION.get(), 1), FABRICATION_TYPE, FLUID_FABRICATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ItemInit.RUNE_RITUAL_METAL.get(), 1).setHoverName(Component.translatable("magichem:rituals/balanced_scales")), SUBLIMATION_RITUAL_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.ALCHEMICAL_NEXUS.get(), 1), SUBLIMATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.COLORING_CAULDRON.get(), 1), COLORATION_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.VARIEGATOR.get(), 1), COLORATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.CONJURER.get(), 1), CONJURATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ItemRegistry.DUMMY_INFO_PANEL.get(), 1), INFO_PANEL_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.ACID_BASIN.get(), 1), VITRIOLATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.ASTRAL_OBSERVER.get(), 1), ILLUMINATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.PRIME_AGGREGATOR.get(), 1), EXALTATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockInit.STUDY_DESK.get(), 1), CONSTRUCT_STUDY_MATERIAL_TYPE);

        IModPlugin.super.registerRecipeCatalysts(registration);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registerGuiHandler(registration, AlchemicalNexusScreen.class, AlchemicalNexusScreen::getGuiExtraAreas);
    }
    // class gen for handlers with only
    private <T extends AbstractContainerScreen<?>> void registerGuiHandler(IGuiHandlerRegistration registration, Class<T> cls, Function<T, List<Rect2i>> extraAreaRectGetter) {
        registration.addGenericGuiContainerHandler(cls, new IGuiContainerHandler<T>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(T screen) {
                return extraAreaRectGetter.apply(screen);
            }
        });
    }
}
