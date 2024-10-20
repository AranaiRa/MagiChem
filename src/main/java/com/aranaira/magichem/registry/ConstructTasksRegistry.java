package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.entities.constructs.ai.*;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.entities.constructs.ai.ConstructHarvest;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ConstructTasksRegistry {

    public static final ConstructTask SORT_MATERIA_FROM_DEVICE = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/sort_materia_from_device.png"), ConstructSortMateriaFromDevice.class, true, false);
    public static final ConstructTask SORT_MATERIA_FROM_CONTAINER = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/sort_materia_from_container.png"), ConstructSortMateriaFromContainer.class, true, false);
    public static final ConstructTask PROVIDE_MATERIA = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/provide_materia.png"), ConstructProvideMateria.class, true, false);
    public static final ConstructTask QUERY_CHECK_VESSEL = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/query_materia_vessel_fill.png"), ConstructCheckVessel.class, true, false, true);
    public static final ConstructTask COLLECT_EXPERIENCE = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/collect_experience.png"), ConstructCollectExperience.class, true, false);
    public static final ConstructTask STUDY = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/study.png"), ConstructStudy.class, true, false);
    public static final ConstructTask CLEAN_ALCHEMICAL_APPARATUS = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/clean_alchemical_apparatus.png"), ConstructCleanAlchemicalApparatus.class, true, false);
    public static final ConstructTask QUERY_HAS_GRIME_LEVEL = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/query_has_grime_level.png"), ConstructHasGrimeLevel.class, true, false, true);
    public static final ConstructTask HARVEST_CRYSTALS = new ConstructTask(new ResourceLocation(MagiChemMod.MODID, "textures/gui/construct/task/harvest_crystals.png"), ConstructHarvestCrystals.class, true, false, false);

    @SubscribeEvent
    public static void registerTasks(RegisterEvent event) {
        event.register(ManaAndArtificeMod.getConstructTaskRegistry().getRegistryKey(), (helper) -> {
            helper.register(new ResourceLocation(MagiChemMod.MODID, "sort_materia"), SORT_MATERIA_FROM_DEVICE);
            helper.register(new ResourceLocation(MagiChemMod.MODID, "provide_materia"), PROVIDE_MATERIA);
            helper.register(new ResourceLocation(MagiChemMod.MODID, "query_check_vessel"), QUERY_CHECK_VESSEL);
            helper.register(new ResourceLocation(MagiChemMod.MODID, "collect_experience"), COLLECT_EXPERIENCE);
            helper.register(new ResourceLocation(MagiChemMod.MODID, "study"), STUDY);
            helper.register(new ResourceLocation(MagiChemMod.MODID, "harvest_crystals"), HARVEST_CRYSTALS);
            helper.register(new ResourceLocation(MagiChemMod.MODID, "clean_alchemical_apparatus"), CLEAN_ALCHEMICAL_APPARATUS);
            helper.register(new ResourceLocation(MagiChemMod.MODID, "query_has_grime_level"), QUERY_HAS_GRIME_LEVEL);
        });
    }
}
