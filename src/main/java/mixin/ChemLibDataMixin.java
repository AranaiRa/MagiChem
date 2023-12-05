package mixin;

import com.google.gson.JsonObject;
import com.smashingmods.chemlib.registry.ChemicalRegistry;
import com.smashingmods.chemlib.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChemicalRegistry.class)
public class ChemLibDataMixin {
    @Shadow @Final public static JsonObject ELEMENTS_JSON = Registry.getStreamAsJsonObject("/data/magichem/mixin/elements.json");
    @Shadow @Final public static JsonObject COMPOUNDS_JSON = Registry.getStreamAsJsonObject("/data/magichem/mixin/compounds.json");
}
