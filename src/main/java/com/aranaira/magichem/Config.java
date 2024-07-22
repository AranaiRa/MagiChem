package com.aranaira.magichem;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = MagiChemMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    //----------------GENERAL

    private static final ForgeConfigSpec.IntValue GRIME_PER_WASTE = BUILDER
            .comment("How much Grime is consumed to make a single unit of Alchemical Waste when cleaning an alchemical device.")
            .defineInRange("grimePerWaste", 250, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue GRIME_PENALTY_POINT = BUILDER
            .comment("At what percent full of Grime that alchemical devices start to lose Efficiency and gain Operation Time.")
            .defineInRange("grimePenaltyPoint", 50, 1, 100);

    private static final ForgeConfigSpec.IntValue FLUID_PER_XP_POINT = BUILDER
            .comment("How many mB of Academic Slurry constitutes one experience point.")
            .defineInRange("fluidPerXPPoint", 40, 1, 1000);

    private static final ForgeConfigSpec.IntValue CONSTRUCT_STUDY_EXPERIENCE_SIMPLE = BUILDER
            .comment("How many experience points a Studying construct generates from simple study materials.")
            .defineInRange("constructStudyExperienceSimple", 2, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CONSTRUCT_STUDY_EXPERIENCE_ADVANCED = BUILDER
            .comment("How many experience points a Studying construct generates from advanced study materials.")
            .defineInRange("constructStudyExperienceAdvanced", 6, 2, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CONSTRUCT_STUDY_EXPERIENCE_MASTER = BUILDER
            .comment("How many experience points a Studying construct generates from master study materials.")
            .defineInRange("constructStudyExperienceMaster", 18, 4, Integer.MAX_VALUE);

    //----------------FIXATION PROCESS

    private static final ForgeConfigSpec.IntValue FIXATION_BASE_SLURRY_COST = BUILDER
            .comment("The amount of Academic Slurry, in mB, consumed to perform Fixation on a first-order Admixture.")
            .defineInRange("fixationBaseSlurryCost", 40, 10, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FIXATION_SLURRY_COMPOUND_COST = BUILDER
            .comment("The percent cost increase over base that higher order Admixtures take to perform Fixation. This cost increase is multiplicative.")
            .defineInRange("fixationSlurryCompoundCost", 300, 10, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FIXATION_FAILURE_REFUND = BUILDER
            .comment("The percent of the experience cost to perform Fixation that is reduced if the process fails.")
            .defineInRange("fixationFailureRefund", 70, 0, 100);

    //----------------ALEMBIC

    private static final ForgeConfigSpec.IntValue ALEMBIC_EFFICIENCY = BUILDER
            .comment("The baseline efficiency of an Alembic.")
            .defineInRange("alembicEfficiency", 30, 1, 100);

    private static final ForgeConfigSpec.IntValue ALEMBIC_OPERATION_TIME = BUILDER
            .comment("The time, in ticks, that it takes for an Alembic to process one object.")
            .defineInRange("alembicOperationTime", 200, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ALEMBIC_MAXIMUM_GRIME = BUILDER
            .comment("The maximum Grime level of the Alembic. ")
            .defineInRange("alembicGrimeMaximum", 24000, 100, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ALEMBIC_GRIME_ON_SUCCESS = BUILDER
            .comment("The amount of Grime generated by a dram of materia that successfully distills.")
            .defineInRange("alembicGrimeOnSuccess", 75, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ALEMBIC_GRIME_ON_FAILURE = BUILDER
            .comment("The amount of Grime generated by a dram of materia that fails to distill.")
            .defineInRange("alembicGrimeOnFailure", 10, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ALEMBIC_PASSIVE_HEAT_MULTIPLIER = BUILDER
            .comment("The percentage increase in operation time the Alembic requires if using a passive heat source.")
            .defineInRange("alembicPassiveHeatMultiplier", 100, 0, Integer.MAX_VALUE);

    //----------------DISTILLERY

    private static final ForgeConfigSpec.IntValue DISTILLERY_EFFICIENCY = BUILDER
            .comment("The baseline efficiency of a Distillery.")
            .defineInRange("distilleryEfficiency", 35, 1, 100);

    private static final ForgeConfigSpec.IntValue DISTILLERY_OPERATION_TIME = BUILDER
            .comment("The time, in ticks, that it takes for a Distillery to process one object.")
            .defineInRange("distilleryOperationTime", 160, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue DISTILLERY_MAXIMUM_GRIME = BUILDER
            .comment("The maximum Grime level of the Distillery. ")
            .defineInRange("distilleryGrimeMaximum", 40000, 100, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue DISTILLERY_GRIME_ON_SUCCESS = BUILDER
            .comment("The amount of Grime generated by a dram of materia that successfully distills.")
            .defineInRange("distilleryGrimeOnSuccess", 75, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue DISTILLERY_GRIME_ON_FAILURE = BUILDER
            .comment("The amount of Grime generated by a dram of materia that fails to distill.")
            .defineInRange("distilleryGrimeOnFailure", 10, 0, Integer.MAX_VALUE);

    //----------------CENTRIFUGE

    private static final ForgeConfigSpec.IntValue CENTRIFUGE_EFFICIENCY = BUILDER
            .comment("The baseline efficiency of a Centrifuge.")
            .defineInRange("centrifugeEfficiency", 35, 1, 100);

    private static final ForgeConfigSpec.IntValue CENTRIFUGE_OPERATION_TIME = BUILDER
            .comment("The time, in ticks, that it takes for a Centrifuge to separate one admixture into component materia.")
            .defineInRange("centrifugeOperationTime", 160, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CENTRIFUGE_MAXIMUM_GRIME = BUILDER
            .comment("The maximum Grime level of the Centrifuge. ")
            .defineInRange("centrifugeGrimeMaximum", 40000, 100, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CENTRIFUGE_GRIME_ON_SUCCESS = BUILDER
            .comment("The amount of Grime generated by an admixture that successfully separates in a Centrifuge.")
            .defineInRange("centrifugeGrimeOnSuccess", 75, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CENTRIFUGE_GRIME_ON_FAILURE = BUILDER
            .comment("The amount of Grime generated by an admixture that fails to separate in a Centrifuge.")
            .defineInRange("centrifugeGrimeOnFailure", 10, 0, Integer.MAX_VALUE);

    //----------------FUSERY

    private static final ForgeConfigSpec.IntValue FUSERY_EFFICIENCY = BUILDER
            .comment("The baseline efficiency of a Fusery.")
            .defineInRange("fuseryEfficiency", 35, 1, 100);

    private static final ForgeConfigSpec.IntValue FUSERY_OPERATION_TIME = BUILDER
            .comment("The time, in ticks, that it takes for an Admixer to make one admixture from its component materia.")
            .defineInRange("fuseryOperationTime", 160, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FUSERY_MAXIMUM_GRIME = BUILDER
            .comment("The maximum Grime level of the Fusery. ")
            .defineInRange("fuseryGrimeMaximum", 40000, 100, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FUSERY_GRIME_ON_SUCCESS = BUILDER
            .comment("The amount of Grime generated by an admixture that successfully combines in a Fusery.")
            .defineInRange("fuseryGrimeOnSuccess", 75, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FUSERY_GRIME_ON_FAILURE = BUILDER
            .comment("The amount of Grime generated by an admixture that fails to combine in a Fusery.")
            .defineInRange("fuseryGrimeOnFailure", 10, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FUSERY_TANK_CAPACITY = BUILDER
            .comment("The amount of Academic Slurry, in mB, the Fusery's internal tank can hold.")
            .defineInRange("fuseryTankCapacity", 4000, 500, Integer.MAX_VALUE);

    //----------------ALCHEMICAL NEXUS

    private static final ForgeConfigSpec.IntValue ALCHEMICAL_NEXUS_TANK_CAPACITY = BUILDER
            .comment("The amount of Academic Slurry, in mB, the Alchemical Nexus's internal tank can hold.")
            .defineInRange("alchemicalNexusTankCapacity", 12000, 500, Integer.MAX_VALUE);

    //----------------CIRCLE OF POWER

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_GEN_1_REAGENT = BUILDER
            .comment("How much FE/tick the Circle of Power generates when it has one reagent")
            .defineInRange("circlePowerGen1", 3, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_GEN_2_REAGENT = BUILDER
            .comment("How much FE/tick the Circle of Power generates when it has two reagents")
            .defineInRange("circlePowerGen2", 36, 2, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_GEN_3_REAGENT = BUILDER
            .comment("How much FE/tick the Circle of Power generates when it has three reagents")
            .defineInRange("circlePowerGen3", 432, 3, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_GEN_4_REAGENT = BUILDER
            .comment("How much FE/tick the Circle of Power generates when it has all four reagents")
            .defineInRange("circlePowerGen4", 5184, 4, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_BUFFER = BUILDER
            .comment("How many ticks of activity the Circle of Power stores at once")
            .defineInRange("circlePowerBuffer", 3, 1, 72000);

    //----------------CIRCLE OF FABRICATION

    //----------------MATERIA JAR

    private static final ForgeConfigSpec.IntValue MATERIA_JAR_ESSENTIA_CAPACITY = BUILDER
            .comment("How much of one type of Essentia can be stored inside of a Materia Jar.")
            .defineInRange("materiaJarEssentiaCapacity", 360, 64, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MATERIA_JAR_ADMIXTURE_CAPACITY = BUILDER
            .comment("How much of one type of Admixture can be stored inside of a Materia Jar.")
            .defineInRange("materiaJarAdmixtureCapacity", 180, 64, Integer.MAX_VALUE);

    //----------------MATERIA VESSEL

    private static final ForgeConfigSpec.IntValue MATERIA_VESSEL_ESSENTIA_CAPACITY = BUILDER
            .comment("How much of one type of Essentia can be stored inside of a Materia Vessel.")
            .defineInRange("materiaVesselEssentiaCapacity", 3600, 64, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MATERIA_VESSEL_ADMIXTURE_CAPACITY = BUILDER
            .comment("How much of one type of Admixture can be stored inside of a Materia Vessel.")
            .defineInRange("materiaVesselAdmixtureCapacity", 1800, 64, Integer.MAX_VALUE);

    //----------------DELUGE PURIFIER

    private static final ForgeConfigSpec.IntValue DELUGE_PURIFIER_OPERATION_TIME = BUILDER
            .comment("The amount of time, in ticks, a Deluge Purifier goes between drawing more Eldrin power.")
            .defineInRange("delugePurifierOperationTime", 300, 1, 6000);

    private static final ForgeConfigSpec.IntValue DELUGE_PURIFIER_TANK_CAPACITY = BUILDER
            .comment("The amount of Water and Steam, in mB, the Deluge Purifier's internal tanks can hold.")
            .defineInRange("delugePurifierTankCapacity", 2000, 500, Integer.MAX_VALUE);

    //----------------INFERNO ENGINE

    private static final ForgeConfigSpec.IntValue INFERNO_ENGINE_OPERATION_TIME = BUILDER
            .comment("The amount of time, in ticks, an Inferno Engine goes between drawing more Eldrin power.")
            .defineInRange("infernoEngineOperationTime", 300, 1, 6000);

    private static final ForgeConfigSpec.IntValue INFERNO_ENGINE_TANK_CAPACITY = BUILDER
            .comment("The amount of Smoke, in mB, the Inferno Engine's internal tank can hold.")
            .defineInRange("infernoEngineTankCapacity", 2000, 500, Integer.MAX_VALUE);

    //----------------QUAKE REFINERY

    private static final ForgeConfigSpec.IntValue QUAKE_REFINERY_OPERATION_TIME = BUILDER
            .comment("The amount of time, in ticks, a Quake Refinery goes between drawing more Eldrin power.")
            .defineInRange("quakeRefineryOperationTime", 300, 1, 6000);

    private static final ForgeConfigSpec.IntValue QUAKE_REFINERY_SAND_CAPACITY = BUILDER
            .comment("The amount of sand that can be stored in the Quake Refinery's internal buffer.")
            .defineInRange("quakeRefinerySandCapacity", 16000, 500, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue QUAKE_REFINERY_GRIME_CAPACITY = BUILDER
            .comment("The amount of Grime that can be stored in the Quake Refinery's internal buffer.")
            .defineInRange("quakeRefineryGrimeCapacity", 128000, 500, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue QUAKE_REFINERY_RAREFIED_RATE = BUILDER
            .comment("What percentage of Grime reduced by a Quake Refinery becomes Rarefied Waste instead of Alchemical Waste.")
            .defineInRange("quakeRefineryRarefiedRate", 20, 1, 100);

    //----------------GALE PRESSURIZER

    private static final ForgeConfigSpec.IntValue GALE_PRESSURIZER_OPERATION_TIME = BUILDER
            .comment("The amount of time, in ticks, a Gale Pressurizer goes between drawing more Eldrin power.")
            .defineInRange("galePressurizerOperationTime", 300, 1, 6000);

    private static final ForgeConfigSpec.IntValue GALE_PRESSURIZER_TANK_CAPACITY = BUILDER
            .comment("The amount of Smoke and Steam, in mB, the Gale Pressurizer's internal tanks can hold.")
            .defineInRange("galePressurizerTankCapacity", 2000, 500, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int
        grimePerWaste,
        grimePenaltyPoint,
        fluidPerXPPoint,
        constructStudyExperienceSimple,
        constructStudyExperienceAdvanced,
        constructStudyExperienceMaster,
        fixationBaseSlurryCost,
        fixationSlurryCompoundCost,
        fixationFailureRefund,
        alembicEfficiency,
        alembicOperationTime,
        alembicMaximumGrime,
        alembicGrimeOnSuccess,
        alembicGrimeOnFailure,
        alembicPassiveHeatMultiplier,
        distilleryEfficiency,
        distilleryOperationTime,
        distilleryMaximumGrime,
        distilleryGrimeOnSuccess,
        distilleryGrimeOnFailure,
        centrifugeEfficiency,
        centrifugeOperationTime,
        centrifugeMaximumGrime,
        centrifugeGrimeOnSuccess,
        centrifugeGrimeOnFailure,
        fuseryEfficiency,
        fuseryOperationTime,
        fuseryMaximumGrime,
        fuseryGrimeOnSuccess,
        fuseryGrimeOnFailure,
        fuseryTankCapacity,
        alchemicalNexusTankCapacity,
        circlePowerGen1Reagent,
        circlePowerGen2Reagent,
        circlePowerGen3Reagent,
        circlePowerGen4Reagent,
        circlePowerBuffer,
        materiaJarEssentiaCapacity,
        materiaJarAdmixtureCapacity,
        materiaVesselEssentiaCapacity,
        materiaVesselAdmixtureCapacity,
        delugePurifierOperationTime,
        delugePurifierTankCapacity,
        infernoEngineOperationTime,
        infernoEngineTankCapacity,
        quakeRefineryOperationTime,
        quakeRefinerySandCapacity,
        quakeRefineryGrimeCapacity,
        quakeRefineryRarefiedRate,
        galePressurizerOperationTime,
        galePressurizerTankCapacity;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        grimePerWaste = GRIME_PER_WASTE.get();
        grimePenaltyPoint = GRIME_PENALTY_POINT.get();
        fluidPerXPPoint = FLUID_PER_XP_POINT.get();
        constructStudyExperienceSimple = CONSTRUCT_STUDY_EXPERIENCE_SIMPLE.get();
        constructStudyExperienceAdvanced = CONSTRUCT_STUDY_EXPERIENCE_ADVANCED.get();
        constructStudyExperienceMaster = CONSTRUCT_STUDY_EXPERIENCE_MASTER.get();
        fixationBaseSlurryCost = FIXATION_BASE_SLURRY_COST.get();
        fixationSlurryCompoundCost = FIXATION_SLURRY_COMPOUND_COST.get();
        fixationFailureRefund = FIXATION_FAILURE_REFUND.get();
        alembicEfficiency = ALEMBIC_EFFICIENCY.get();
        alembicOperationTime = ALEMBIC_OPERATION_TIME.get();
        alembicMaximumGrime = ALEMBIC_MAXIMUM_GRIME.get();
        alembicGrimeOnSuccess = ALEMBIC_GRIME_ON_SUCCESS.get();
        alembicGrimeOnFailure = ALEMBIC_GRIME_ON_FAILURE.get();
        alembicPassiveHeatMultiplier = ALEMBIC_PASSIVE_HEAT_MULTIPLIER.get();
        distilleryEfficiency = DISTILLERY_EFFICIENCY.get();
        distilleryOperationTime = DISTILLERY_OPERATION_TIME.get();
        distilleryMaximumGrime = DISTILLERY_MAXIMUM_GRIME.get();
        distilleryGrimeOnSuccess = DISTILLERY_GRIME_ON_SUCCESS.get();
        distilleryGrimeOnFailure = DISTILLERY_GRIME_ON_FAILURE.get();
        centrifugeEfficiency = CENTRIFUGE_EFFICIENCY.get();
        centrifugeOperationTime = CENTRIFUGE_OPERATION_TIME.get();
        centrifugeMaximumGrime = CENTRIFUGE_MAXIMUM_GRIME.get();
        centrifugeGrimeOnSuccess = CENTRIFUGE_GRIME_ON_SUCCESS.get();
        centrifugeGrimeOnFailure = CENTRIFUGE_GRIME_ON_FAILURE.get();
        fuseryEfficiency = FUSERY_EFFICIENCY.get();
        fuseryOperationTime = FUSERY_OPERATION_TIME.get();
        fuseryMaximumGrime = FUSERY_MAXIMUM_GRIME.get();
        fuseryGrimeOnSuccess = FUSERY_GRIME_ON_SUCCESS.get();
        fuseryGrimeOnFailure = FUSERY_GRIME_ON_FAILURE.get();
        fuseryTankCapacity = FUSERY_TANK_CAPACITY.get();
        alchemicalNexusTankCapacity = ALCHEMICAL_NEXUS_TANK_CAPACITY.get();
        circlePowerGen1Reagent = CIRCLE_OF_POWER_GEN_1_REAGENT.get();
        circlePowerGen2Reagent = CIRCLE_OF_POWER_GEN_2_REAGENT.get();
        circlePowerGen3Reagent = CIRCLE_OF_POWER_GEN_3_REAGENT.get();
        circlePowerGen4Reagent = CIRCLE_OF_POWER_GEN_4_REAGENT.get();
        circlePowerBuffer = CIRCLE_OF_POWER_BUFFER.get();
        materiaJarEssentiaCapacity = MATERIA_JAR_ESSENTIA_CAPACITY.get();
        materiaJarAdmixtureCapacity = MATERIA_JAR_ADMIXTURE_CAPACITY.get();
        materiaVesselEssentiaCapacity = MATERIA_VESSEL_ESSENTIA_CAPACITY.get();
        materiaVesselAdmixtureCapacity = MATERIA_VESSEL_ADMIXTURE_CAPACITY.get();
        delugePurifierOperationTime = DELUGE_PURIFIER_OPERATION_TIME.get();
        delugePurifierTankCapacity = DELUGE_PURIFIER_TANK_CAPACITY.get();
        infernoEngineOperationTime = INFERNO_ENGINE_OPERATION_TIME.get();
        infernoEngineTankCapacity = INFERNO_ENGINE_TANK_CAPACITY.get();
        quakeRefineryOperationTime = QUAKE_REFINERY_OPERATION_TIME.get();
        quakeRefinerySandCapacity = QUAKE_REFINERY_SAND_CAPACITY.get();
        quakeRefineryGrimeCapacity = QUAKE_REFINERY_GRIME_CAPACITY.get();
        quakeRefineryRarefiedRate = QUAKE_REFINERY_RAREFIED_RATE.get();
        galePressurizerOperationTime = GALE_PRESSURIZER_OPERATION_TIME.get();
        galePressurizerTankCapacity = GALE_PRESSURIZER_TANK_CAPACITY.get();
    }
}
