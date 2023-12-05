package mixin;

import com.google.gson.JsonObject;
import com.smashingmods.chemlib.registry.ChemicalRegistry;
import com.smashingmods.chemlib.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChemicalRegistry.class)
public class ChemLibDataMixin {
    //private static final JsonObject ELEMENTS_JSON = Registry.getStreamAsJsonObject("/data/magichem/elements.json");
    //private static final JsonObject COMPOUNDS_JSON = Registry.getStreamAsJsonObject("/data/magichem/compounds.json");
}
