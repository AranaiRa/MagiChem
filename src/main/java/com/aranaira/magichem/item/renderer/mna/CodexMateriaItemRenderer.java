package com.aranaira.magichem.item.renderer.mna;

import com.aranaira.magichem.MagiChemMod;
import com.mna.items.renderers.books.ItemBookRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class CodexMateriaItemRenderer extends ItemBookRenderer {
    public static final ResourceLocation
            CODEX_MATERIA_OPEN   = new ResourceLocation(MagiChemMod.MODID, "obj/special/codex_materia_open"),
            CODEX_MATERIA_CLOSED = new ResourceLocation(MagiChemMod.MODID, "obj/special/codex_materia_closed");

    public CodexMateriaItemRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems, CODEX_MATERIA_OPEN, CODEX_MATERIA_CLOSED);
    }
}
