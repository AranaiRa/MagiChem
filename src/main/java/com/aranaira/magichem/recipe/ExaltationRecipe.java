package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonObject;
import com.mna.api.affinity.Affinity;
import com.mna.api.recipes.IMARecipe;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExaltationRecipe implements Recipe<SimpleContainer>, IMARecipe {
    public static final int
        ENDER = 1, EARTH = 2, WATER = 4, AIR = 8, FIRE = 16, ARCANE = 32;

    private final ResourceLocation id;
    private final ItemStack result;
    private final Item itemType;
    private final MateriaItem materiaType;
    private final int itemsRequired, materiaRequired, slurryRequired, eldrinRequired;
    private final byte tier, eldrinType;
    private ItemStack itemAsStack = ItemStack.EMPTY, materiaAsStack = ItemStack.EMPTY;

    public ExaltationRecipe(ResourceLocation pID, ItemStack pResult, byte pTier, Item pItemType, int pItemsRequired, MateriaItem pMateriaType, int pMateriaRequired, byte pEldrinType, int pEldrinRequired, int pSlurryRequired) {
        this.id = pID;
        this.result = pResult;
        this.tier = pTier;
        this.itemType = pItemType;
        this.itemsRequired = pItemsRequired;
        this.materiaType = pMateriaType;
        this.materiaRequired = pMateriaRequired;
        this.eldrinType = pEldrinType;
        this.eldrinRequired = pEldrinRequired;
        this.slurryRequired = pSlurryRequired;

        this.itemAsStack = new ItemStack(pItemType);
        this.materiaAsStack = new ItemStack(pMateriaType);
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public MateriaItem getMateriaType() {
        return materiaType;
    }

    public int getMateriaRequired() {
        return materiaRequired;
    }

    public Item getItemType() {
        return itemType;
    }

    public int getItemsRequired() {
        return itemsRequired;
    }

    public int getEldrinTypeBitpack() {
        return eldrinType;
    }

    public ArrayList<Affinity> getEldrinTypes() {
        ArrayList<Affinity> out = new ArrayList<>();

        if((eldrinType & ENDER) == ENDER) out.add(Affinity.ENDER);
        if((eldrinType & EARTH) == EARTH) out.add(Affinity.EARTH);
        if((eldrinType & WATER) == WATER) out.add(Affinity.WATER);
        if((eldrinType & AIR) == AIR) out.add(Affinity.WIND);
        if((eldrinType & FIRE) == FIRE) out.add(Affinity.FIRE);
        if((eldrinType & ARCANE) == ARCANE) out.add(Affinity.ARCANE);

        return out;
    }

    public boolean usesEldrinType(Affinity pAffinity) {
        if(pAffinity == Affinity.ENDER && (eldrinType & ENDER) == ENDER) return true;
        if(pAffinity == Affinity.EARTH && (eldrinType & EARTH) == EARTH) return true;
        if(pAffinity == Affinity.WATER && (eldrinType & WATER) == WATER) return true;
        if(pAffinity == Affinity.WIND && (eldrinType & AIR) == AIR) return true;
        if(pAffinity == Affinity.FIRE && (eldrinType & FIRE) == FIRE) return true;
        if(pAffinity == Affinity.ARCANE && (eldrinType & ARCANE) == ARCANE) return true;

        return false;
    }

    public int getEldrinTypeCount() {
        int out = 0;

        if((eldrinType & ENDER) == ENDER) out++;
        if((eldrinType & EARTH) == EARTH) out++;
        if((eldrinType & WATER) == WATER) out++;
        if((eldrinType & AIR) == AIR) out++;
        if((eldrinType & FIRE) == FIRE) out++;
        if((eldrinType & ARCANE) == ARCANE) out++;

        return out;
    }

    public int getEldrinRequired() {
        return eldrinRequired;
    }

    public int getSlurryRequired() {
        return slurryRequired;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return result;
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

    public static ExaltationRecipe getExaltationRecipe(Level level, Item query) {
        ExaltationRecipe recipeResult = null;
        List<ExaltationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(ExaltationRecipe ar : allRecipes) {
            if(ar.result.getItem() == query) {
                recipeResult = ar;
                break;
            }
        }

        return recipeResult;
    }

    public static List<ExaltationRecipe> getAllExaltationRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.id;
    }

    @Override
    public ItemStack getResultItem() {
        return result;
    }

    public ItemStack getInputItemAsStack() {
        return itemAsStack;
    }

    public ItemStack getMateriaTypeAsStack() {
        return materiaAsStack;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return result;
    }

    @Override
    public int getTier() {
        return tier;
    }

    public static class Type implements RecipeType<ExaltationRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "exaltation";
    }

    public static class Serializer implements RecipeSerializer<ExaltationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(MagiChemMod.MODID, "exaltation");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public ExaltationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            JsonObject resultObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "result", null);
            JsonObject itemsObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "items", null);
            JsonObject eldrinObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "eldrin", null);
            JsonObject materiaObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "materia", null);
            int slurryRequired = GsonHelper.getAsInt(pSerializedRecipe, "slurry");
            byte tier = GsonHelper.getAsByte(pSerializedRecipe, "tier");

            ItemStack result = ItemStack.EMPTY;
            if(resultObject != null) {
                String itemRL = GsonHelper.getAsString(resultObject, "item");
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemRL));
                int count = GsonHelper.getAsInt(resultObject, "count");

                if(item == null) MagiChemMod.LOGGER.warn("&&&&& Exaltation recipe couldn't find output item \""+itemRL.toString()+"\"!");
                else {
                    result = new ItemStack(item, count);
                }
            }

            Item itemType = null;
            int itemsRequired = 0;
            if(itemsObject != null) {
                String typeRL = GsonHelper.getAsString(itemsObject, "type");
                itemType = ForgeRegistries.ITEMS.getValue(new ResourceLocation(typeRL));
                itemsRequired = GsonHelper.getAsInt(itemsObject, "required");
                if(itemType == null) MagiChemMod.LOGGER.warn("&&&&& Exaltation recipe couldn't find required item \""+typeRL.toString()+"\"!");
            }

            MateriaItem materiaType = null;
            int materiaRequired = 0;
            if(materiaObject != null) {
                String typeRL = GsonHelper.getAsString(materiaObject, "type");
                Item materiaTypeQuery = ForgeRegistries.ITEMS.getValue(new ResourceLocation(typeRL));
                materiaRequired = GsonHelper.getAsInt(materiaObject, "required");
                if(materiaTypeQuery instanceof MateriaItem mi) materiaType = mi;
                if(materiaType == null) MagiChemMod.LOGGER.warn("&&&&& Exaltation recipe couldn't find materia type \""+typeRL.toString()+"\"!");
            }

            byte eldrinType = 0;
            int eldrinRequired = 0;
            if(eldrinObject != null) {
                eldrinType = GsonHelper.getAsByte(eldrinObject, "type");
                eldrinRequired = GsonHelper.getAsInt(eldrinObject, "required");
            }

            return new ExaltationRecipe(pRecipeId, result, tier, itemType, itemsRequired, materiaType, materiaRequired, eldrinType, eldrinRequired, slurryRequired);
        }

        @Override
        public @Nullable ExaltationRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CompoundTag nbt = pBuffer.readNbt();
            CompoundTag itemTag = nbt.getCompound("items");
            CompoundTag materiaTag = nbt.getCompound("materia");
            CompoundTag eldrinTag = nbt.getCompound("eldrin");

            ItemStack result = ItemStack.of(nbt.getCompound("result"));
            byte tier = nbt.getByte("tier");

            Item itemType = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemTag.getString("type")));
            int itemsRequired = itemTag.getInt("required");

            MateriaItem materiaType = null;
            Item materiaTypeQuery = ForgeRegistries.ITEMS.getValue(new ResourceLocation(materiaTag.getString("type")));
            int materiaRequired = materiaTag.getInt("required");
            if(materiaTypeQuery instanceof MateriaItem mi) materiaType = mi;

            byte eldrinType = eldrinTag.getByte("type");
            int eldrinRequired = eldrinTag.getInt("required");

            int slurryRequired = nbt.getInt("slurry");

            return new ExaltationRecipe(pRecipeId, result, tier, itemType, itemsRequired, materiaType, materiaRequired, eldrinType, eldrinRequired, slurryRequired);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ExaltationRecipe pRecipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.put("result", pRecipe.result.serializeNBT());
            nbt.putByte("tier", pRecipe.tier);

            CompoundTag itemTag = new CompoundTag();
            itemTag.putString("type", ForgeRegistries.ITEMS.getKey(pRecipe.itemType).toString());
            itemTag.putInt("required", pRecipe.itemsRequired);
            nbt.put("items", itemTag);

            CompoundTag materiaTag = new CompoundTag();
            materiaTag.putString("type", ForgeRegistries.ITEMS.getKey(pRecipe.materiaType).toString());
            materiaTag.putInt("required", pRecipe.materiaRequired);
            nbt.put("materia", materiaTag);

            CompoundTag eldrinTag = new CompoundTag();
            eldrinTag.putByte("type", pRecipe.eldrinType);
            eldrinTag.putInt("required", pRecipe.eldrinRequired);
            nbt.put("eldrin", eldrinTag);

            nbt.putInt("slurry", pRecipe.slurryRequired);

            pBuffer.writeNbt(nbt);
        }
    }
}
