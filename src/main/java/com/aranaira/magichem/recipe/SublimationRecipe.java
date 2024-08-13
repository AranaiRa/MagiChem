package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * This recipe type is used by the Ritual of the Balanced Scales.
 */
public class SublimationRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final int tier, wisdom;
    private final ItemStack alchemyObject;
    private final NonNullList<InfusionStage> stages;
    private static final NonNullList<ItemStack> allPossibleOutputs = NonNullList.create();

    public SublimationRecipe(ResourceLocation pID, int pTier, int pWisdom, ItemStack pAlchemyObject,
                             NonNullList<InfusionStage> pStages) {
        this.id = pID;
        this.stages = pStages;
        this.tier = pTier;
        this.wisdom = pWisdom;
        this.alchemyObject = pAlchemyObject;
    }

    /**
     * Unused by this mod's devices, but has to be here for Recipe inheritance.
     * @param pContainer
     * @param pLevel
     * @return always false
     */
    @Deprecated
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public NonNullList<InfusionStage> getStages(boolean copy) {
        if(!copy)
            return stages;

        NonNullList<InfusionStage> out = NonNullList.create();
        for(InfusionStage is : stages) {
            out.add(is.copy());
        }
        return out;
    }

    public int getTier() {
        return this.tier;
    }

    public int getWisdom() {
        return this.wisdom;
    }

    public ItemStack getAlchemyObject() {
        return alchemyObject;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return alchemyObject;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return alchemyObject;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static SublimationRecipe getInfusionRecipe(Level level, ItemStack query) {
        SublimationRecipe result = null;
        List<SublimationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(SublimationRecipe airr : allRecipes) {
            if(airr.alchemyObject.getItem() == query.getItem()) {
                result = airr;
                break;
            }
        }

        return result;
    }

    public static NonNullList<ItemStack> getAllOutputs(Level pLevel) {
        if(allPossibleOutputs.size() > 0)
            return allPossibleOutputs;

        List<SublimationRecipe> allRecipes = pLevel.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
        for(SublimationRecipe airr : allRecipes) {
            allPossibleOutputs.add(airr.alchemyObject.copy());
        }

        return allPossibleOutputs;
    }

    public static class Type implements RecipeType<SublimationRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "alchemical_infusion_ritual";
    }


    public static class Serializer implements RecipeSerializer<SublimationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MagiChemMod.MODID, "alchemical_infusion_ritual");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public SublimationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack recipeObject = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "object"));
            if(recipeObject.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:air")))
                recipeObject = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:barrier")));

            int tier = GsonHelper.getAsInt(pSerializedRecipe, "tier");
            int wisdom = GsonHelper.getAsInt(pSerializedRecipe, "wisdom");

            JsonArray stages = GsonHelper.getAsJsonArray(pSerializedRecipe, "stages");
            NonNullList<InfusionStage> extractedStages = NonNullList.create();
            stages.forEach(element -> {
                JsonObject asJsonObject = element.getAsJsonObject();

                int experienceThisStage = GsonHelper.getAsInt(asJsonObject, "experience");
                JsonArray itemArray = GsonHelper.getAsJsonArray(asJsonObject, "components");
                JsonArray materiaArray = GsonHelper.getAsJsonArray(asJsonObject, "materia");

                NonNullList<ItemStack> items = NonNullList.create();
                NonNullList<ItemStack> materia = NonNullList.create();

                itemArray.forEach(itemElement -> {
                    String key = itemElement.getAsJsonObject().get("item").getAsString();

                    Item itemQuery = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
                    if(itemQuery != null) {
                        items.add(new ItemStack(itemQuery));
                    } else {
                        MagiChemMod.LOGGER.warn("&&& Couldn't find item \""+key+"\" for alchemical_infusion recipe \""+pRecipeId);
                    }
                });

                materiaArray.forEach(materiaElement -> {
                    String key = materiaElement.getAsJsonObject().get("item").getAsString();
                    int count = materiaElement.getAsJsonObject().get("count").getAsInt();

                    Item materiaQuery = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
                    if(materiaQuery != null) {
                        materia.add(new ItemStack(materiaQuery, count));
                    } else {
                        MagiChemMod.LOGGER.warn("&&& Couldn't find materia \""+key+"\" for alchemical_infusion recipe \""+pRecipeId);
                    }
                });

                extractedStages.add(new InfusionStage(experienceThisStage, items, materia));
            });

            return new SublimationRecipe(pRecipeId, tier, wisdom, recipeObject, extractedStages);
        }

        @Override
        public @Nullable SublimationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CompoundTag nbt = buf.readNbt();
            CompoundTag nbtAlchemyObject = nbt.getCompound("alchemyObject");
            CompoundTag nbtStages = nbt.getCompound("stages");

            int tier = nbt.getInt("tier");
            int wisdom = nbt.getInt("wisdom");

            //alchemy object
            ResourceLocation alchemyObjectRL = new ResourceLocation(nbtAlchemyObject.getString("item"));
            Item alchemyObjectItem = ForgeRegistries.ITEMS.getValue(alchemyObjectRL);
            ItemStack alchemyObject = ItemStack.EMPTY;
            if(alchemyObjectItem != null) {
                if(nbtAlchemyObject.contains("count"))
                    alchemyObject = new ItemStack(alchemyObjectItem, nbtAlchemyObject.getInt("count"));
                else
                    alchemyObject = new ItemStack(alchemyObjectItem, 1);
            }

            int stagesCount = nbtStages.getInt("count");
            NonNullList<InfusionStage> infusionStages = NonNullList.create();
            for(int stage=0; stage<stagesCount; stage++) {
                CompoundTag nbtThisStage = nbtStages.getCompound("stage"+stage);
                int experienceThisStage = nbtThisStage.getInt("experience");
                CompoundTag nbtThisStageIngredients = nbtThisStage.getCompound("ingredients");
                CompoundTag nbtThisStageMateria = nbtThisStage.getCompound("materia");

                //ingredients
                NonNullList<ItemStack> componentItems = NonNullList.create();
                {
                    for(int i=0; i<nbtThisStageIngredients.getInt("ingredientTotal"); i++) {
                        CompoundTag nbtThisIngredient = nbtThisStageIngredients.getCompound("ingredient"+i);
                        ResourceLocation rloc = new ResourceLocation(nbtThisIngredient.getString("item"));
                        Item item = ForgeRegistries.ITEMS.getValue(rloc);
                        int count = nbtThisIngredient.getInt("count");

                        if(item == null)
                            MagiChemMod.LOGGER.warn("&&& Couldn't find item \""+rloc+"\" for alchemical_infusion recipe \""+id+"\"");
                        else
                            componentItems.add(new ItemStack(item, count));
                    }
                }

                //materia
                NonNullList<ItemStack> componentMateria = NonNullList.create();
                {
                    for(int i=0; i<nbtThisStageMateria.getInt("materiaTotal"); i++) {
                        CompoundTag nbtThisMateria = nbtThisStageMateria.getCompound("materia"+i);
                        ResourceLocation rloc = new ResourceLocation(nbtThisMateria.getString("item"));
                        Item item = ForgeRegistries.ITEMS.getValue(rloc);
                        int count = nbtThisMateria.getInt("count");

                        if(item == null)
                            MagiChemMod.LOGGER.warn("&&& Couldn't find materia \""+rloc+"\" for alchemical_infusion recipe \""+id+"\"");
                        else
                            componentMateria.add(new ItemStack(item, count));
                    }
                }

                InfusionStage is = new InfusionStage(experienceThisStage, componentItems, componentMateria);
                infusionStages.add(is);
            }

            return new SublimationRecipe(id, tier, wisdom, alchemyObject, infusionStages);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SublimationRecipe recipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putInt("tier", recipe.getTier());
            nbt.putInt("wisdom", recipe.getWisdom());

            CompoundTag nbtAlchemyObject = new CompoundTag();
            nbtAlchemyObject.putString("item", ForgeRegistries.ITEMS.getKey(recipe.getAlchemyObject().getItem()).toString());
            nbtAlchemyObject.putInt("count", recipe.getAlchemyObject().getCount());
            nbt.put("alchemyObject", nbtAlchemyObject);

            CompoundTag nbtStages = new CompoundTag();
            nbtStages.putInt("count", recipe.getStages(false).size());

            for(int stage=0; stage<recipe.getStages(false).size(); stage++)
            {
                InfusionStage infusionStage = recipe.getStages(false).get(stage);
                CompoundTag nbtThisStage = new CompoundTag();
                CompoundTag nbtThisStageIngredients = new CompoundTag();
                CompoundTag nbtThisStageMateria = new CompoundTag();

                nbtThisStage.putInt("experience", infusionStage.experience);

                //component items
                {
                    nbtThisStageIngredients.putInt("ingredientTotal", infusionStage.componentItems.size());

                    for(int i=0; i<infusionStage.componentItems.size(); i++) {
                        ItemStack is = infusionStage.componentItems.get(i);

                        CompoundTag nbtThisIngredient = new CompoundTag();
                        nbtThisIngredient.putString("item", ForgeRegistries.ITEMS.getKey(is.getItem()).toString());
                        nbtThisIngredient.putInt("count", is.getCount());

                        nbtThisStageIngredients.put("ingredient"+i, nbtThisIngredient);
                    }

                    nbtThisStage.put("ingredients", nbtThisStageIngredients);
                }

                //component materia
                {
                    nbtThisStageMateria.putInt("materiaTotal", infusionStage.componentMateria.size());

                    for(int i=0; i<infusionStage.componentMateria.size(); i++) {
                        ItemStack is = infusionStage.componentMateria.get(i);

                        CompoundTag nbtThisMateria = new CompoundTag();
                        nbtThisMateria.putString("item", ForgeRegistries.ITEMS.getKey(is.getItem()).toString());
                        nbtThisMateria.putInt("count", is.getCount());

                        nbtThisStageMateria.put("materia"+i, nbtThisMateria);
                    }

                    nbtThisStage.put("materia", nbtThisStageMateria);
                }

                nbtStages.put("stage"+stage, nbtThisStage);
            }

            nbt.put("stages", nbtStages);

            buf.writeNbt(nbt);
        }
    }
}