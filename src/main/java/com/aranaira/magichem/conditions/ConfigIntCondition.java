package com.aranaira.magichem.conditions;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.config.ServerConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ConfigIntCondition implements ICondition {
    public static final ResourceLocation ID = new ResourceLocation(MagiChemMod.MODID, "config_int_condition");
    private final String configID;
    private final int value;

    public ConfigIntCondition(String pConfigID, int pValue) {
        configID = pConfigID;
        value = pValue;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        final int valueInConfig = ServerConfig.getIntValue(configID);

        return value == valueInConfig;
    }

    public static class Serializer implements IConditionSerializer<ConfigIntCondition> {
        public static final ConfigIntCondition.Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, ConfigIntCondition value) {
            json.addProperty("configID", value.configID);
            json.addProperty("value", value.value);
        }

        @Override
        public ConfigIntCondition read(JsonObject json) {
            final String configID = GsonHelper.getAsString(json, "configID");
            final int value = GsonHelper.getAsInt(json, "value");

            return new ConfigIntCondition(configID, value);
        }

        @Override
        public ResourceLocation getID() {
            return ConfigIntCondition.ID;
        }
    }
}
