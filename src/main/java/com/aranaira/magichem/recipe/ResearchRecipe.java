package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.ResearchRecipeType;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mna.api.recipes.IMARecipe;
import com.mna.items.ItemInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResearchRecipe implements Recipe<SimpleContainer>, IMARecipe {
    private final ResourceLocation id;
    private final int tier, wisdom, xp, vellum, ink;
    private final boolean usesIlluminink, isAdvancementResearch, isItemResearch, isSilverSpellResearch;
    private final NonNullList<Pair<String, Byte>> researchRequirements;
    private final NonNullList<ItemStack> componentItems;
    private final NonNullList<ItemStack> componentMateria;
    private final ResourceLocation resultAdvancementLocation;
    private final ItemStack resultItem, resultThesis, decorationByproduct;

    public ResearchRecipe(ResourceLocation pID, int pTier, int pWisdom, int pXP, int pVellum, int pInk, boolean pUsesIlluminink, NonNullList<Pair<String, Byte>> pResearchRequirements, boolean pIsAdvancementResearch, boolean pIsItemResearch, boolean pIsSilverSpellResearch, NonNullList<ItemStack> pComponentItems, NonNullList<ItemStack> pComponentMateria, ResourceLocation pResultAdvancementLocation, ItemStack pResultItem, ItemStack pResultThesis, ItemStack pDecorationByproduct) {
        this.id = pID;
        tier = pTier;
        wisdom = pWisdom;
        xp = pXP;
        vellum = pVellum;
        ink = pInk;
        usesIlluminink = pUsesIlluminink;
        researchRequirements = pResearchRequirements;
        isAdvancementResearch = pIsAdvancementResearch;
        isItemResearch = pIsItemResearch;
        isSilverSpellResearch = pIsSilverSpellResearch;
        componentItems = pComponentItems;
        componentMateria = pComponentMateria;
        resultAdvancementLocation = pResultAdvancementLocation;
        resultItem = pResultItem;
        resultThesis = pResultThesis;
        decorationByproduct = pDecorationByproduct;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        if(isItemResearch) return resultItem.copy();
        else if(isSilverSpellResearch) return resultThesis.copy();
        return new ItemStack(Items.DIRT);
    }

    public boolean isItemResearch() {
        return isItemResearch;
    }

    public boolean isAdvancementResearch() {
        return isAdvancementResearch;
    }

    public boolean isSilverSpellResearch() {
        return isSilverSpellResearch;
    }

    public boolean usesIlluminink() {
        return usesIlluminink;
    }

    public int getWisdomRequirement() {
        return wisdom;
    }

    public int getXPRequired() {
        return xp;
    }

    public int getVellumRequired() {
        return vellum;
    }

    public int getInkRequired() {
        return ink;
    }

    public NonNullList<Pair<String, Byte>> getResearchRequirements() {
        return researchRequirements;
    }

    public NonNullList<ItemStack> getComponentItems() {
        return componentItems;
    }

    public NonNullList<ItemStack> getComponentMateria() {
        return componentMateria;
    }

    public ResearchRecipeType getResearchType() {
        if(resultItem != null && !resultItem.isEmpty()) return ResearchRecipeType.ITEM;
        if(resultThesis != null && !resultThesis.isEmpty()) return ResearchRecipeType.SILVER_SPELL;
        if(resultAdvancementLocation != null) return ResearchRecipeType.ADVANCEMENT;

        return ResearchRecipeType.NONE;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        if(isItemResearch) return resultItem.copy();
        else if(isSilverSpellResearch) return resultThesis.copy();
        return new ItemStack(Items.DIRT);
    }

    public ItemStack getOutputItem() {
        return resultItem;
    }

    public ItemStack getOutputThesis() {
        return resultThesis;
    }

    public boolean hasDecorationByproduct() {
        if(decorationByproduct != null)
            return !decorationByproduct.isEmpty();
        return false;
    }

    public ItemStack getDecorationByproduct() {
        return decorationByproduct;
    }

    public ResourceLocation getOutputAdvancementLocation() {
        return resultAdvancementLocation;
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

    public static ResearchRecipe getItemResearchRecipe(Level level, Item query) {
        ResearchRecipe recipeResult = null;
        List<ResearchRecipe> filteredRecipes = getAllResearchRecipesOfType(level, ResearchRecipeType.ITEM);

        for(ResearchRecipe fr : filteredRecipes) {
            if(fr.getOutputItem().getItem() == query) {
                recipeResult = fr;
                break;
            }
        }

        return recipeResult;
    }

    public static ResearchRecipe getSilverSpellResearchRecipe(Level level, ResourceLocation query) {
        ResearchRecipe recipeResult = null;
        List<ResearchRecipe> filteredRecipes = getAllResearchRecipesOfType(level, ResearchRecipeType.SILVER_SPELL);

        for(ResearchRecipe fr : filteredRecipes) {
            final ItemStack thesis = fr.getOutputThesis();
            if(thesis.hasTag()) {
                final CompoundTag tag = thesis.getTag();
                if(tag.contains("component") && tag.getString("component").equals(query.toString())) {
                    recipeResult = fr;
                    break;
                }
            }
        }

        return recipeResult;
    }

    public static ResearchRecipe getAdvancementResearchRecipe(Level level, ResourceLocation query) {
        ResearchRecipe recipeResult = null;
        List<ResearchRecipe> filteredRecipes = getAllResearchRecipesOfType(level, ResearchRecipeType.ADVANCEMENT);

        for(ResearchRecipe fr : filteredRecipes) {
            final ResourceLocation loc = fr.getOutputAdvancementLocation();
            if(loc.toString().equals(query.toString())) {
                recipeResult = fr;
                break;
            }
        }

        return recipeResult;
    }

    public static List<ResearchRecipe> getAllResearchRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
    }

    public static List<ResearchRecipe> getAllResearchRecipesOfType(Level level, ResearchRecipeType type) {
        final List<ResearchRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
        List<ResearchRecipe> filteredRecipes = new ArrayList<>();

        for (ResearchRecipe query : allRecipes) {
            if(query.getResearchType() == type) {
                filteredRecipes.add(query);
            }
        }

        return filteredRecipes;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.id;
    }

    @Override
    public ItemStack getResultItem() {
        return resultItem.copy();
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        if(isItemResearch) return resultItem;
        else if(isSilverSpellResearch) return resultThesis;
        return new ItemStack(Items.DIRT); //TODO: Replace with advancement dummy
    }

    @Override
    public int getTier() {
        return tier;
    }

    public static class Type implements RecipeType<ResearchRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "research";
    }

    public static class Serializer implements RecipeSerializer<ResearchRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(MagiChemMod.MODID, "research");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);
        private static final byte
                RECIPE_TYPE_NONE = 0, RECIPE_TYPE_ITEM = 1, RECIPE_TYPE_SILVER = 2, RECIPE_TYPE_ADVANCEMENT = 3;

        @Override
        public ResearchRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            int tier = GsonHelper.getAsInt(pSerializedRecipe, "tier");
            int wisdom = GsonHelper.getAsInt(pSerializedRecipe, "wisdom");
            int xp = GsonHelper.getAsInt(pSerializedRecipe, "xp");
            int vellum = GsonHelper.getAsInt(pSerializedRecipe, "vellum");
            int ink = GsonHelper.getAsInt(pSerializedRecipe, "ink");
            boolean usesIlluminink = GsonHelper.getAsBoolean(pSerializedRecipe, "usesIlluminink");

            NonNullList<Pair<String, Byte>> researchRequirements = NonNullList.create();
            if(pSerializedRecipe.has("researchRequirements")) {
                JsonArray requirements = GsonHelper.getAsJsonArray(pSerializedRecipe, "researchRequirements");
                requirements.forEach(element -> {
                    String key = element.getAsJsonObject().get("key").getAsString();
                    byte strength = (byte)Math.max(1,element.getAsJsonObject().get("strength").getAsInt());

                    researchRequirements.add(new Pair<>(key, strength));
                });
            }

            JsonArray componentItems = GsonHelper.getAsJsonArray(pSerializedRecipe, "components");
            NonNullList<ItemStack> extractedItems = NonNullList.create();
            componentItems.forEach(element -> {
                String key = element.getAsJsonObject().get("item").getAsString();

                ItemStack itemQuery = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "object"));
                if(itemQuery.getItem() == Items.AIR) {
                    itemQuery = new ItemStack(ItemRegistry.PROBLEMITE.get());
                    MagiChemMod.LOGGER.warn("&&& Couldn't find component item \""+key+"\" for research recipe \""+pRecipeId);
                }

                if(element.getAsJsonObject().has("count"))
                    itemQuery.setCount(element.getAsJsonObject().get("count").getAsInt());
                extractedItems.add(itemQuery);
            });

            JsonArray componentMateria = GsonHelper.getAsJsonArray(pSerializedRecipe, "componentMateria");
            NonNullList<ItemStack> extractedMateria = NonNullList.create();
            componentMateria.forEach(element -> {
                String key = element.getAsJsonObject().get("item").getAsString();

                ItemStack ing = ItemStack.EMPTY;

                MateriaItem matQuery = materiaMap.get(key);
                if(matQuery != null) {
                    ing = new ItemStack(matQuery);
                } else {
                    ing = new ItemStack(ItemRegistry.ADMIXTURE_PROBLEMS.get());
                    MagiChemMod.LOGGER.warn("&&& Couldn't find component componentMateria \""+key+"\" for distillation_fabrication recipe \""+pRecipeId);
                }

                if(element.getAsJsonObject().has("count"))
                    ing.setCount(element.getAsJsonObject().get("count").getAsInt());
                extractedMateria.add(ing);
            });

            ItemStack decorationByproduct = null;
            if(pSerializedRecipe.has("decorationByproduct")) {
                ItemStack itemQuery = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "decorationByproduct"));
                if (itemQuery.getItem() == Items.AIR) {
                    itemQuery = new ItemStack(ItemRegistry.PROBLEMITE.get());
                    MagiChemMod.LOGGER.warn("&&& Couldn't find decoration byproduct item \"" + GsonHelper.getAsString(GsonHelper.getAsJsonObject(pSerializedRecipe, "decorationByproduct"), "item") + "\" for research recipe \"" + pRecipeId);
                }
                decorationByproduct = itemQuery;
            }

            ItemStack itemResult = null, thesisResult = null;
            ResourceLocation advancementLocation = null;
            boolean isItemResearch = false, isSilverResearch = false, isAdvancementResearch = false;
            if(pSerializedRecipe.has("itemResult")) {
                isItemResearch = true;
                final JsonObject dataQuery = GsonHelper.getAsJsonObject(pSerializedRecipe, "itemResult");
                String key = dataQuery.getAsJsonObject().get("item").getAsString();

                ItemStack itemQuery = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "object"));
                if(itemQuery.getItem() == Items.AIR) {
                    itemQuery = new ItemStack(ItemRegistry.PROBLEMITE.get());
                    MagiChemMod.LOGGER.warn("&&& Couldn't find result item \""+key+"\" for research recipe \""+pRecipeId);
                }

                if(dataQuery.getAsJsonObject().has("count"))
                    itemQuery.setCount(dataQuery.getAsJsonObject().get("count").getAsInt());
                itemResult = itemQuery;
            } else if(pSerializedRecipe.has("silverResult")) {
                isSilverResearch = true;
                final JsonObject dataQuery = GsonHelper.getAsJsonObject(pSerializedRecipe, "silverResult");
                thesisResult = new ItemStack(ItemInit.SPELL_PART_THESIS.get());
                CompoundTag tag = new CompoundTag();
                if(dataQuery.has("part")) {
                    tag.putString("part", GsonHelper.getAsString(dataQuery, "part"));
                    if(dataQuery.has("thesis")) {
                        tag.putBoolean("thesis", GsonHelper.getAsBoolean(dataQuery,"thesis"));
                    } else {
                        tag.putBoolean("thesis", true);
                    }
                    tag.putBoolean("doHackyOverride", true); //TODO: Remove this bit once MnA updates
                    thesisResult.setTag(tag);
                } else {
                    thesisResult = new ItemStack(ItemRegistry.PROBLEMITE.get());
                    MagiChemMod.LOGGER.warn("&&& Silver spell result for research recipe \""+pRecipeId+"\" is missing a spell part!");
                }
            } else if(pSerializedRecipe.has("advancementResult")) {
                isAdvancementResearch = true;
                advancementLocation = new ResourceLocation(GsonHelper.getAsString(pSerializedRecipe, "advancementResult"));
            }

            return new ResearchRecipe(pRecipeId, tier, wisdom, xp, vellum, ink, usesIlluminink, researchRequirements, isAdvancementResearch, isItemResearch, isSilverResearch, extractedItems, extractedMateria, advancementLocation, itemResult, thesisResult, decorationByproduct);
        }

        @Override
        public @Nullable ResearchRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CompoundTag nbt = pBuffer.readNbt();
            if(nbt == null) return null;

            byte recipeType = nbt.getByte("recipeType");
            if(recipeType == RECIPE_TYPE_NONE) return null;

            int tier = nbt.getByte("tier");
            int wisdom = nbt.getByte("wisdom");
            int xp = nbt.getInt("xp");
            int vellum = nbt.getInt("vellum");
            int ink = nbt.getInt("ink");
            boolean usesIlluminink = nbt.getBoolean("usesIlluminink");

            NonNullList<Pair<String, Byte>> researchRequirements = NonNullList.create();
            {
                CompoundTag requirementsTag = nbt.getCompound("researchRequirements");
                byte count = requirementsTag.getByte("requirementsCount");
                for(int i=0;i<count;i++) {
                    CompoundTag thisRequirementTag = requirementsTag.getCompound(""+i);
                    String key = thisRequirementTag.getString("key");
                    byte strength = thisRequirementTag.getByte("strength");
                    researchRequirements.add(new Pair<>(key, strength));
                }
            }

            NonNullList<ItemStack> componentItems = NonNullList.create();
            {
                CompoundTag itemsTag = nbt.getCompound("componentItems");
                byte count = itemsTag.getByte("componentItemsCount");
                for(int i=0;i<count;i++) {
                    ItemStack query = ItemStack.EMPTY.copy();
                    query.deserializeNBT(itemsTag.getCompound(""+i));
                    componentItems.add(query);
                }
            }

            NonNullList<ItemStack> componentMateria = NonNullList.create();
            {
                CompoundTag itemsTag = nbt.getCompound("componentMateria");
                byte count = itemsTag.getByte("componentMateriaCount");
                for(int i=0;i<count;i++) {
                    ItemStack query = ItemStack.EMPTY.copy();
                    query.deserializeNBT(itemsTag.getCompound(""+i));
                    componentMateria.add(query);
                }
            }

            ItemStack decorationByproduct = null;
            if(nbt.contains("decorationByproduct")) {
                decorationByproduct = ItemStack.EMPTY.copy();
                decorationByproduct.deserializeNBT(nbt.getCompound("decorationByproduct"));
            }

            boolean isItemResearch = false, isSilverResearch = false, isAdvancementResearch = false;
            ItemStack itemResult = null, thesisResult = null;
            ResourceLocation advancementLocation = null;
            if(recipeType == RECIPE_TYPE_ITEM) {
                isItemResearch = true;
                itemResult = ItemStack.EMPTY.copy();
                itemResult.deserializeNBT(nbt.getCompound("outputItem"));
            }
            else if(recipeType == RECIPE_TYPE_SILVER) {
                isSilverResearch = true;
                thesisResult = new ItemStack(ItemInit.SPELL_PART_THESIS.get());
                CompoundTag thesisTag = nbt.getCompound("outputThesis");
                thesisResult.setTag(thesisTag);
            }
            else if(recipeType == RECIPE_TYPE_ADVANCEMENT) {
                isAdvancementResearch = true;
                advancementLocation = new ResourceLocation(nbt.getString("outputAdvancement"));
            }

            return new ResearchRecipe(pRecipeId, tier, wisdom, xp, vellum, ink, usesIlluminink, researchRequirements, isAdvancementResearch, isItemResearch, isSilverResearch, componentItems, componentMateria, advancementLocation, itemResult, thesisResult, decorationByproduct);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ResearchRecipe pRecipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putByte("tier", (byte) pRecipe.tier);
            nbt.putByte("wisdom", (byte) pRecipe.wisdom);
            nbt.putInt("xp", pRecipe.xp);
            nbt.putInt("vellum", pRecipe.vellum);
            nbt.putInt("ink", pRecipe.ink);
            nbt.putBoolean("usesIlluminink", pRecipe.usesIlluminink);

            if(pRecipe.researchRequirements.size() > 0) {
                CompoundTag requirementsTag = new CompoundTag();

                nbt.putByte("requirementsCount", (byte) pRecipe.researchRequirements.size());
                for(int i=0;i<pRecipe.researchRequirements.size();i++) {
                    CompoundTag requirementElement = new CompoundTag();
                    requirementElement.putString("key",pRecipe.researchRequirements.get(i).getFirst());
                    if(pRecipe.researchRequirements.get(i).getSecond() > 1)
                        requirementElement.putByte("strength",pRecipe.researchRequirements.get(i).getSecond());
                    requirementsTag.put(""+i,requirementElement);
                }

                nbt.put("requirements", requirementsTag);
            }

            if(pRecipe.componentItems.size() > 0) {
                CompoundTag itemsTag = new CompoundTag();

                nbt.putByte("componentItemsCount", (byte) pRecipe.componentItems.size());
                for(int i=0;i<pRecipe.componentItems.size();i++) {
                    itemsTag.put(""+i,pRecipe.componentItems.get(i).serializeNBT());
                }

                nbt.put("componentItems", itemsTag);
            }

            if(pRecipe.componentMateria.size() > 0) {
                CompoundTag materiaTag = new CompoundTag();

                nbt.putByte("componentMateriaCount", (byte) pRecipe.componentMateria.size());
                for(int i=0;i<pRecipe.componentMateria.size();i++) {
                    materiaTag.put(""+i,pRecipe.componentMateria.get(i).serializeNBT());
                }

                nbt.put("componentMateria", materiaTag);
            }

            if(pRecipe.hasDecorationByproduct()) {
                nbt.put("decorationByproduct", pRecipe.getDecorationByproduct().serializeNBT());
            }

            if(pRecipe.isItemResearch) {
                nbt.putByte("recipeType", RECIPE_TYPE_ITEM);
                nbt.put("outputItem",pRecipe.getOutputItem().serializeNBT());
            }
            else if(pRecipe.isSilverSpellResearch) {
                nbt.putByte("recipeType", RECIPE_TYPE_SILVER);
                final CompoundTag thesisTag = pRecipe.resultThesis.getOrCreateTag();
                nbt.put("outputThesis", thesisTag);
            }
            else if(pRecipe.isAdvancementResearch) {
                nbt.putByte("recipeType", RECIPE_TYPE_ADVANCEMENT);
                nbt.putString("outputAdvancement",pRecipe.resultAdvancementLocation.toString());
            }
            else {
                nbt.putByte("recipeType", RECIPE_TYPE_NONE);
            }

            pBuffer.writeNbt(nbt);
        }
    }
}
