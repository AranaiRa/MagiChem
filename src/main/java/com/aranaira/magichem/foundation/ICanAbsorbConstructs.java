package com.aranaira.magichem.foundation;

import com.aranaira.magichem.util.render.ConstructRenderHelper;
import com.mna.entities.EntityInit;
import com.mna.entities.constructs.animated.Construct;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public interface ICanAbsorbConstructs {

    boolean tryAbsorbConstruct(Player pPlayer);

    boolean hasConstruct();

    void ejectConstruct();

    CompoundTag getStoredConstructComposition();
}
