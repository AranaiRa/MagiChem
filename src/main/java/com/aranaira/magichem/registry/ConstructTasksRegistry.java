package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.entities.constructs.ai.ConstructCheckVessel;
import com.aranaira.magichem.entities.constructs.ai.ConstructSortMateria;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.ai.ConstructTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ConstructTasksRegistry {

    public static final ConstructTask SORT_MATERIA = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/sort_materia.png"), ConstructSortMateria.class, true, false);
    public static final ConstructTask QUERY_CHECK_VESSEL = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/query_materia_vessel_fill.png"), ConstructCheckVessel.class, true, false);

    @SubscribeEvent
    public static void registerTasks(RegisterEvent event) {
        event.register(ManaAndArtificeMod.getConstructTaskRegistry().getRegistryKey(), (helper) -> {
            helper.register(new ResourceLocation(MagiChemMod.MODID, "sort_materia"), SORT_MATERIA);
            helper.register(new ResourceLocation(MagiChemMod.MODID, "query_check_vessel"), QUERY_CHECK_VESSEL);
        });
    }
}
