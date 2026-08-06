package com.aranaira.magichem.recipe;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface NbtPreservingRecipe {
    @Nullable
    Item getNbtSource();
}
