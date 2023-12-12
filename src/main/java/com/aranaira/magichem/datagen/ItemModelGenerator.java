package com.aranaira.magichem.datagen;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {

    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MagiChemMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        generateEssentiaModels();
    }

    private void generateEssentiaModels() {
        for(EssentiaItem item : ItemRegistry.getEssentia()) {
            withExistingParent(String.format("item/essentia_%s", item.getMateriaName()), mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/phial_essentia_fill"))
                    .texture("layer1", modLoc("item/phial_essentia"));
        }
    }
}
