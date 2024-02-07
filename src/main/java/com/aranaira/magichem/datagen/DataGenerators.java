package com.aranaira.magichem.datagen;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MagiChemMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new ItemModelGenerator(output, MagiChemMod.MODID, event.getExistingFileHelper()));
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(true, new ItemModelGenerator(output, MagiChemMod.MODID, existingFileHelper));
        //generator.addProvider(true, new LangGenerator(generator, "en_us"));
    }
}
