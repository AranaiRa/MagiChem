package com.aranaira.magichem.datagen;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {

    public ItemModelGenerator(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        generateEssentiaModels();
        generateAdmixtureModels();
    }

    private void generateEssentiaModels() {
        for(EssentiaItem item : ItemRegistry.getEssentia()) {
            withExistingParent(String.format("item/essentia_%s", item.getMateriaName()), mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/phial_essentia_fill"))
                    .texture("layer1", modLoc("item/phial_essentia"))
                    .texture("layer2", modLoc("item/overlays/essentiaoverlay_"+item.getMateriaName()));
        }
    }

    private void generateAdmixtureModels() {
        for(AdmixtureItem item : ItemRegistry.getAdmixtures()) {
            withExistingParent(String.format("item/admixture_%s", item.getMateriaName()), mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/phial_admixture_fill"))
                    .texture("layer1", modLoc("item/phial_admixture"));
        }
    }
}
