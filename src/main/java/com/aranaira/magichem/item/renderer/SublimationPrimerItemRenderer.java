package com.aranaira.magichem.item.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.mna.items.renderers.books.ItemBookRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class SublimationPrimerItemRenderer extends ItemBookRenderer {
    public static final ResourceLocation
            SUBLIMATION_PRIMER_OPEN   = new ResourceLocation(MagiChemMod.MODID, "obj/special/sublimation_primer_open"),
            SUBLIMATION_PRIMER_CLOSED = new ResourceLocation(MagiChemMod.MODID, "obj/special/sublimation_primer_closed");

    public SublimationPrimerItemRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems, SUBLIMATION_PRIMER_OPEN, SUBLIMATION_PRIMER_CLOSED);
    }
}
