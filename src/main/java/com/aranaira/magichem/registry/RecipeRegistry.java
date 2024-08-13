package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.recipe.SublimationRecipe;
import com.aranaira.magichem.recipe.SublimationRitualRecipe;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MagiChemMod.MODID);

    public static final RegistryObject<RecipeSerializer<DistillationFabricationRecipe>> ALCHEMICAL_COMPOSITION_SERIALIZER =
            SERIALIZERS.register("distillation_fabrication", () -> DistillationFabricationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<FixationSeparationRecipe>> FIXATION_SEPARATION_SERIALIZER =
            SERIALIZERS.register("fixation_separation", () -> FixationSeparationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<SublimationRitualRecipe>> ALCHEMICAL_INFUSION_RITUAL =
            SERIALIZERS.register("alchemical_infusion_ritual", () -> SublimationRitualRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<SublimationRecipe>> ALCHEMICAL_INFUSION =
            SERIALIZERS.register("alchemical_infusion", () -> SublimationRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
