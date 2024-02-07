package com.aranaira.magichem.util;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class MathHelper {
    public static Vec3i V3toV3i(Vec3 input) {
        return new Vec3i((int)Math.round(input.x), (int)Math.round(input.y), (int)Math.round(input.z));
    }
}
