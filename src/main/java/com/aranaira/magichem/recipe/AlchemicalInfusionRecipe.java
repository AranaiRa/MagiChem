package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
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
public class AlchemicalInfusionRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final int tier, wisdom;
    private final ItemStack alchemyObject;
    private final NonNullList<InfusionStage> stages;
    private static final NonNullList<ItemStack> allPossibleOutputs = NonNullList.create();

    public AlchemicalInfusionRecipe(ResourceLocation pID, int pTier, int pWisdom, ItemStack pAlchemyObject,
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

    public static AlchemicalInfusionRecipe getInfusionRecipe(Level level, ItemStack query) {
        AlchemicalInfusionRecipe result = null;
        List<AlchemicalInfusionRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(AlchemicalInfusionRecipe airr : allRecipes) {
            if(airr.alchemyObject.getItem() == query.getItem()) {
                result = airr;
                break;
            }
        }

        return result;
    }

    public static NonNullList<ItemStack> getAllOutputs() {
        if(allPossibleOutputs.size() > 0)
            return allPossibleOutputs;

        List<AlchemicalInfusionRecipe> allRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
        for(AlchemicalInfusionRecipe airr : allRecipes) {
            allPossibleOutputs.add(airr.alchemyObject.copy());
        }

        return allPossibleOutputs;
    }

    public static class Type implements RecipeType<AlchemicalInfusionRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "alchemical_infusion_ritual";
    }


    public static class Serializer implements RecipeSerializer<AlchemicalInfusionRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MagiChemMod.MODID, "alchemical_infusion_ritual");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public AlchemicalInfusionRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
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

            return new AlchemicalInfusionRecipe(pRecipeId, tier, wisdom, recipeObject, extractedStages);
        }

        @Override
        public @Nullable AlchemicalInfusionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CompoundTag nbt = buf.readNbt();

            int tier = nbt.getInt("tier");
            int wisdom = nbt.getInt("wisdom");

            ResourceLocation alchemyObjectRL = new ResourceLocation(nbt.getString("alchemyObjectItem"));
            Item alchemyObjectItem = ForgeRegistries.ITEMS.getValue(alchemyObjectRL);
            ItemStack alchemyObject = ItemStack.EMPTY;
            if(alchemyObjectItem != null) {
                if(nbt.contains("alchemyObjectCount"))
                    alchemyObject = new ItemStack(alchemyObjectItem, nbt.getInt("alchemyObjectCount"));
                else
                    alchemyObject = new ItemStack(alchemyObjectItem, 1);
            }

            int stagesCount = nbt.getInt("stagesCount");
            NonNullList<InfusionStage> infusionStages = NonNullList.create();
            for(int stage=0; stage<stagesCount; stage++) {
                int experienceThisStage = nbt.getInt("stage"+stage+"Experience");

                NonNullList<ItemStack> componentItems = NonNullList.create();
                {
                    for(int i=0; i<nbt.getInt("stage"+stage+"IngredientCount"); i++) {
                        ResourceLocation rloc = new ResourceLocation(nbt.getString("stage"+stage+"Ingredient"+i+"Item"));
                        Item item = ForgeRegistries.ITEMS.getValue(rloc);
                        int count = nbt.getInt("stage"+stage+"Ingredient"+i+"Count");

                        if(item == null)
                            MagiChemMod.LOGGER.warn("&&& Couldn't find item \""+rloc+"\" for alchemical_infusion recipe \""+id+"\"");
                        else
                            componentItems.add(new ItemStack(item, count));
                    }
                }

                NonNullList<ItemStack> componentMateria = NonNullList.create();
                {
                    for(int i=0; i<nbt.getInt("stage"+stage+"IngredientCount"); i++) {
                        ResourceLocation rloc = new ResourceLocation(nbt.getString("stage"+stage+"Ingredient"+i+"Item"));
                        Item item = ForgeRegistries.ITEMS.getValue(rloc);
                        int count = nbt.getInt("stage"+stage+"Ingredient"+i+"Count");

                        if(item == null)
                            MagiChemMod.LOGGER.warn("&&& Couldn't find materia \""+rloc+"\" for alchemical_infusion recipe \""+id+"\"");
                        else
                            componentMateria.add(new ItemStack(item, count));
                    }
                }

                InfusionStage is = new InfusionStage(experienceThisStage, componentItems, componentMateria);
                infusionStages.add(is);
            }

            return new AlchemicalInfusionRecipe(id, tier, wisdom, alchemyObject, infusionStages);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AlchemicalInfusionRecipe recipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putInt("tier", recipe.getTier());
            nbt.putInt("wisdom", recipe.getWisdom());

            nbt.putString("alchemyObjectItem", ForgeRegistries.ITEMS.getKey(recipe.getAlchemyObject().getItem()).toString());
            nbt.putInt("alchemyObjectCount", recipe.getAlchemyObject().getCount());

            nbt.putInt("stagesCount", recipe.getStages(false).size());

            {
                int stage = 0;
                for(InfusionStage infusionStage : recipe.getStages(false)) {
                    nbt.putInt("stage"+stage+"Experience", infusionStage.experience);

                    //component items
                    {
                        nbt.putInt("stage"+stage+"IngredientCount", infusionStage.componentItems.size());

                        int i = 0;
                        for(ItemStack is : infusionStage.componentItems) {
                            nbt.putString("stage"+stage+"Ingredient"+i+"Item", ForgeRegistries.ITEMS.getKey(is.getItem()).toString());
                            nbt.putInt("stage"+stage+"Ingredient"+i+"Count", is.getCount());
                            i++;
                        }
                    }

                    //component materia
                    {
                        nbt.putInt("stage"+stage+"MateriaCount", infusionStage.componentMateria.size());

                        int i = 0;
                        for(ItemStack is : infusionStage.componentMateria) {
                            nbt.putString("stage"+stage+"Materia"+i+"Item", ForgeRegistries.ITEMS.getKey(is.getItem()).toString());
                            nbt.putInt("stage"+stage+"Materia"+i+"Count", is.getCount());
                            i++;
                        }
                    }
                }
            }

            buf.writeNbt(nbt);
        }
    }
}