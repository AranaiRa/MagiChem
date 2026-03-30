package com.aranaira.magichem.item;

import com.aranaira.magichem.foundation.NameCountPair;
import com.aranaira.magichem.item.renderer.MasterItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public class AdmixtureItemWithSpecialRenderer extends AdmixtureItem {
    public AdmixtureItemWithSpecialRenderer(String pName, String pColor, int pDepth, List<NameCountPair> pFormulaEssentia, List<NameCountPair> pFormulaAdmixtures, int pInitialBatchSize, int pBatchSizeCap) {
        super(pName, pColor, pDepth, pFormulaEssentia, pFormulaAdmixtures, pInitialBatchSize, pBatchSizeCap);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return MasterItemRenderer.getOrCreateMasterRenderer().get();
            }
        });
    }
}
