package com.aranaira.magichem.util;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class MathHelper {
    public static Vec3i V3toV3i(Vec3 input) {
        return new Vec3i((int)Math.floor(input.x), (int)Math.floor(input.y), (int)Math.floor(input.z));
    }
}
