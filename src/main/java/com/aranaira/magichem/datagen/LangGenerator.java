package com.aranaira.magichem.datagen;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;

public class LangGenerator extends LanguageProvider {

    public LangGenerator(DataGenerator generator, String locale) {
        super(generator, MagiChemMod.MODID, locale);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void addTranslations() {
        ItemRegistry.getEssentia().forEach(essentia -> add(String.format("item.magichem.essentia_%s", essentia.getMateriaName()), WordUtils.capitalize(essentia.getMateriaName()) + " Essentia"));
        ItemRegistry.getAdmixtures().forEach(essentia -> add(String.format("item.magichem.admixture_%s", essentia.getMateriaName()), "Admixture of " + WordUtils.capitalize(essentia.getMateriaName())));
    }
}
