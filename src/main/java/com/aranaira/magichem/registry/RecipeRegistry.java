package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.recipe.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MagiChemMod.MODID);

    public static final RegistryObject<RecipeSerializer<DistillationFabricationRecipe>> DISTILLATION_FABRICATION_SERIALIZER =
            SERIALIZERS.register("distillation_fabrication", () -> DistillationFabricationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<FluidDistillationFabricationRecipe>> FLUID_DISTILLATION_FABRICATION_SERIALIZER =
            SERIALIZERS.register("fluid_distillation_fabrication", () -> FluidDistillationFabricationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<FixationSeparationRecipe>> FIXATION_SEPARATION_SERIALIZER =
            SERIALIZERS.register("fixation_separation", () -> FixationSeparationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<SublimationRitualRecipe>> SUBLIMATION_RITUAL_SERIALIZER =
            SERIALIZERS.register("sublimation_ritual", () -> SublimationRitualRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<SublimationRecipe>> SUBLIMATION_SERIALIZER =
            SERIALIZERS.register("sublimation", () -> SublimationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ColorationRecipe>> COLORATION_SERIALIZER =
            SERIALIZERS.register("coloration", () -> ColorationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ConjurationRecipe>> CONJURATION_SERIALIZER =
            SERIALIZERS.register("conjuration", () -> ConjurationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<AnointingRecipe>> ANOINTING_SERIALIZER =
            SERIALIZERS.register("anointing", () -> AnointingRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<FulminationRecipe>> FULMINATION_SERIALIZER =
            SERIALIZERS.register("fulmination", () -> FulminationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<VitriolationRecipe>> VITRIOLATION_SERIALIZER =
            SERIALIZERS.register("vitriolation", () -> VitriolationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<IlluminationRecipe>> ILLUMINATION_SERIALIZER =
            SERIALIZERS.register("illumination", () -> IlluminationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<InfoPanelRecipe>> INFO_PANEL_SERIALIZER =
            SERIALIZERS.register("info_panel", () -> InfoPanelRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ProphecyErosionRecipe>> PROPHECY_EROSION_SERIALIZER =
            SERIALIZERS.register("prophecy_erosion", () -> ProphecyErosionRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ExaltationRecipe>> EXALTATION_SERIALIZER =
            SERIALIZERS.register("exaltation", () -> ExaltationRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
