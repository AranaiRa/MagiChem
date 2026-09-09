package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Matching, validation, and transition semantics for NBT-aware coloration recipes. */
public final class ColorationNbtHelper {
    private static final Set<String> REPORTED_AMBIGUITIES =
            Collections.synchronizedSet(new HashSet<>());

    private ColorationNbtHelper() {
    }

    public static boolean hasNbtAwareRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(ColorationRecipe.Type.INSTANCE)
                .stream().anyMatch(ColorationRecipe::isNbtAware);
    }

    @Nullable
    public static ColorationRecipe findRecipe(Level level, ItemStack input,
                                              @Nullable Boolean forVariegator) {
        List<ColorationRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(ColorationRecipe.Type.INSTANCE);
        Candidate best = null;
        boolean tied = false;
        for (ColorationRecipe recipe : recipes) {
            if (Boolean.TRUE.equals(forVariegator) && !recipe.isValidOnVariegator()) continue;
            if (Boolean.FALSE.equals(forVariegator) && !recipe.isValidOnCauldron()) continue;
            Match match = match(recipe, input);
            if (match == null) continue;

            Candidate candidate = new Candidate(recipe, match.specificity, recipe.isNbtAware());
            if (best == null || candidate.specificity > best.specificity) {
                best = candidate;
                tied = false;
            } else if (candidate.specificity == best.specificity
                    && (candidate.nbtAware || best.nbtAware)) {
                tied = true;
            }
        }
        if (best == null) return null;
        if (tied) {
            String key = input.getItem().toString() + ":" + input.getTag();
            if (REPORTED_AMBIGUITIES.add(key)) {
                MagiChemMod.LOGGER.error("Ambiguous NBT-aware coloration recipes for input {}", input);
            }
            return null;
        }
        return best.recipe;
    }

    @Nullable
    public static ItemStack createResult(ColorationRecipe recipe, ItemStack input,
                                         @Nullable DyeColor targetColor) {
        ItemStack target = targetColor == null ? recipe.getColorlessDefault()
                : recipe.getResultsAsMap(false).get(targetColor);
        if (target == null) return null;
        if (!recipe.isNbtAware()) return target.copy();

        Match source = match(recipe, input);
        if (source == null) return null;
        boolean preserve = targetColor == null
                ? recipe.isPreserveNbtDefault() : recipe.isPreserveNbt(targetColor);
        DyeColor alias = findColorlessAlias(recipe);
        if (targetColor == null && alias != null) {
            preserve = recipe.isPreserveNbt(alias);
        }

        ItemStack result = target.copy();
        if (!preserve) return result;
        CompoundTag merged = input.getTag() == null ? new CompoundTag() : input.getTag().copy();
        for (ItemStack sourceStack : source.stacks) {
            removeDeclared(merged, normalizedTag(sourceStack));
        }
        CompoundTag targetNbt = normalizedTag(target);
        if (targetNbt != null) mergeDeclared(merged, targetNbt);
        result.setTag(merged.isEmpty() ? null : merged);
        return result;
    }

    @Nullable
    public static ItemStack createResultForTemplate(ColorationRecipe recipe, ItemStack input,
                                                    ItemStack targetTemplate) {
        if (!recipe.isNbtAware()) return targetTemplate.copy();
        for (Map.Entry<DyeColor, ItemStack> entry : recipe.getResultsAsMap(false).entrySet()) {
            if (samePredicate(entry.getValue(), targetTemplate)) {
                return createResult(recipe, input, entry.getKey());
            }
        }
        return null;
    }

    public static boolean isNoOp(ItemStack input, ItemStack result) {
        return result.getCount() == 1 && ItemStack.isSameItemSameTags(input, result);
    }

    @Nullable
    public static DyeColor findColorlessAlias(ColorationRecipe recipe) {
        if (!recipe.isNbtAware()) return null;
        for (Map.Entry<DyeColor, ItemStack> entry : recipe.getResultsAsMap(false).entrySet()) {
            if (samePredicate(recipe.getColorlessDefault(), entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void validate(ColorationRecipe recipe) {
        if (!recipe.isNbtAware()) return;
        List<Map.Entry<DyeColor, ItemStack>> colored =
                new ArrayList<>(recipe.getResultsAsMap(false).entrySet());
        for (int i = 0; i < colored.size(); i++) {
            for (int j = i + 1; j < colored.size(); j++) {
                Map.Entry<DyeColor, ItemStack> left = colored.get(i);
                Map.Entry<DyeColor, ItemStack> right = colored.get(j);
                if (samePredicate(left.getValue(), right.getValue())) {
                    throw RecipeNbtHelper.error(recipe.getId(), "colored states '"
                            + left.getKey().getName() + "' and '" + right.getKey().getName()
                            + "' have identical input predicates");
                }
            }
        }

        int aliases = 0;
        for (Map.Entry<DyeColor, ItemStack> entry : colored) {
            if (samePredicate(recipe.getColorlessDefault(), entry.getValue())) aliases++;
        }
        if (aliases > 1) {
            throw RecipeNbtHelper.error(recipe.getId(),
                    "colorless_default aliases more than one colored state");
        }
    }

    @Nullable
    private static Match match(ColorationRecipe recipe, ItemStack input) {
        if (!recipe.isNbtAware()) {
            if (recipe.getColorlessDefault().getItem() == input.getItem()) {
                return new Match(List.of(recipe.getColorlessDefault()), 0);
            }
            for (ItemStack stack : recipe.getResultsAsMap(false).values()) {
                if (stack.getItem() == input.getItem()) return new Match(List.of(stack), 0);
            }
            return null;
        }

        List<ItemStack> qualifiedMatches = new ArrayList<>();
        ItemStack unqualifiedMatch = null;
        int maximumSpecificity = 0;
        for (ItemStack stack : recipe.getResultsAsMap(false).values()) {
            if (matches(stack, input)) {
                CompoundTag predicate = normalizedTag(stack);
                if (predicate == null) {
                    unqualifiedMatch = stack;
                } else {
                    qualifiedMatches.add(stack);
                    maximumSpecificity = Math.max(maximumSpecificity, specificity(predicate));
                }
            }
        }
        if (!qualifiedMatches.isEmpty()) {
            return new Match(List.copyOf(qualifiedMatches), maximumSpecificity);
        }
        if (unqualifiedMatch != null) {
            return new Match(List.of(unqualifiedMatch), 0);
        }
        ItemStack colorless = recipe.getColorlessDefault();
        return matches(colorless, input)
                ? new Match(List.of(colorless), specificity(normalizedTag(colorless))) : null;
    }

    private static boolean matches(ItemStack predicate, ItemStack input) {
        if (predicate.getItem() != input.getItem()) return false;
        CompoundTag required = normalizedTag(predicate);
        return required == null || NbtUtils.compareNbt(required, input.getTag(), true);
    }

    private static boolean samePredicate(ItemStack left, ItemStack right) {
        return left.getItem() == right.getItem()
                && Objects.equals(normalizedTag(left), normalizedTag(right));
    }

    private static int specificity(@Nullable CompoundTag tag) {
        if (tag == null) return 0;
        int count = 0;
        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            count += value instanceof CompoundTag compound ? specificity(compound) : 1;
        }
        return count;
    }

    private static void removeDeclared(CompoundTag target, @Nullable CompoundTag declared) {
        if (declared == null) return;
        for (String key : declared.getAllKeys()) {
            Tag declaration = declared.get(key);
            Tag existing = target.get(key);
            if (declaration instanceof CompoundTag declarationCompound
                    && existing instanceof CompoundTag existingCompound) {
                removeDeclared(existingCompound, declarationCompound);
                if (existingCompound.isEmpty()) target.remove(key);
            } else if (declaration instanceof ListTag declarationList
                    && existing instanceof ListTag existingList) {
                removeDeclared(existingList, declarationList);
                if (existingList.isEmpty()) target.remove(key);
            } else {
                target.remove(key);
            }
        }
    }

    private static void removeDeclared(ListTag target, ListTag declared) {
        for (Tag declaration : declared) {
            for (int i = target.size() - 1; i >= 0; i--) {
                if (NbtUtils.compareNbt(declaration, target.get(i), true)) {
                    target.remove(i);
                }
            }
        }
    }

    private static void mergeDeclared(CompoundTag target, CompoundTag declared) {
        for (String key : declared.getAllKeys()) {
            Tag addition = declared.get(key);
            Tag existing = target.get(key);
            if (addition instanceof CompoundTag additionCompound
                    && existing instanceof CompoundTag existingCompound) {
                mergeDeclared(existingCompound, additionCompound);
            } else if (addition instanceof ListTag additionList
                    && existing instanceof ListTag existingList
                    && (existingList.isEmpty() || additionList.isEmpty()
                    || existingList.getElementType() == additionList.getElementType())) {
                mergeDeclared(existingList, additionList);
            } else if (addition != null) {
                target.put(key, addition.copy());
            }
        }
    }

    private static void mergeDeclared(ListTag target, ListTag declared) {
        for (Tag addition : declared) {
            boolean alreadyPresent = false;
            for (Tag existing : target) {
                if (Objects.equals(existing, addition)) {
                    alreadyPresent = true;
                    break;
                }
            }
            if (!alreadyPresent) target.add(addition.copy());
        }
    }

    @Nullable
    private static CompoundTag normalizedTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || tag.isEmpty() ? null : tag;
    }

    private record Match(List<ItemStack> stacks, int specificity) {
    }

    private record Candidate(ColorationRecipe recipe, int specificity, boolean nbtAware) {
    }
}
