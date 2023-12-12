package com.aranaira.magichem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = MagiChemMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    //----------------ALEMBIC

    private static final ForgeConfigSpec.IntValue ALEMBIC_EFFICIENCY = BUILDER
            .comment("The baseline efficiency of an Alembic.")
            .defineInRange("alembicEfficiency", 50, 1, 100);

    private static final ForgeConfigSpec.IntValue ALEMBIC_OPERATION_TIME = BUILDER
            .comment("The time, in ticks, that it takes for an Alembic to process one object.")
            .defineInRange("alembicOperationTime", 1200, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue ALEMBIC_NEEDS_HEAT = BUILDER
            .comment("The time, in ticks, that it takes for an Alembic to process one object.")
            .define("alembicNeedsHeat", true);

    //----------------CIRCLE OF POWER

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_GEN_1_REAGENT = BUILDER
            .comment("How much FE/tick the Circle of Power generates when it has one reagent")
            .defineInRange("circlePowerGen1", 3, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_GEN_2_REAGENT = BUILDER
            .comment("How much FE/tick the Circle of Power generates when it has two reagents")
            .defineInRange("circlePowerGen2", 12, 2, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_GEN_3_REAGENT = BUILDER
            .comment("How much FE/tick the Circle of Power generates when it has three reagents")
            .defineInRange("circlePowerGen3", 48, 3, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_GEN_4_REAGENT = BUILDER
            .comment("How much FE/tick the Circle of Power generates when it has all four reagents")
            .defineInRange("circlePowerGen4", 200, 4, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CIRCLE_OF_POWER_BUFFER = BUILDER
            .comment("How many ticks of activity the Circle of Power stores at once")
            .defineInRange("circlePowerBuffer", 3, 1, 72000);

    //----------------DISTILLERY

    private static final ForgeConfigSpec.IntValue DISTILLERY_EFFICIENCY = BUILDER
            .comment("The baseline efficiency of an Distillery.")
            .defineInRange("distilleryEfficiency", 75, 1, 100);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int
        alembicEfficiency,
        alembicOperationTime,
        circlePowerGen1Reagent,
        circlePowerGen2Reagent,
        circlePowerGen3Reagent,
        circlePowerGen4Reagent,
        circlePowerBuffer,
        distilleryEfficiency;

    public static boolean
        alembicNeedsHeat;


    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        alembicEfficiency = ALEMBIC_EFFICIENCY.get();
        alembicOperationTime = ALEMBIC_OPERATION_TIME.get();
        alembicNeedsHeat = ALEMBIC_NEEDS_HEAT.get();
        circlePowerGen1Reagent = CIRCLE_OF_POWER_GEN_1_REAGENT.get();
        circlePowerGen2Reagent = CIRCLE_OF_POWER_GEN_2_REAGENT.get();
        circlePowerGen3Reagent = CIRCLE_OF_POWER_GEN_3_REAGENT.get();
        circlePowerGen4Reagent = CIRCLE_OF_POWER_GEN_4_REAGENT.get();
        circlePowerBuffer = CIRCLE_OF_POWER_BUFFER.get();
        distilleryEfficiency = DISTILLERY_EFFICIENCY.get();

        // convert the list of strings into a set of items
        /*items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());*/
    }
}
