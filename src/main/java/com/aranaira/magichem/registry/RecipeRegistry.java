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

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
