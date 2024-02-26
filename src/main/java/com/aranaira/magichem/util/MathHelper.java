package com.aranaira.magichem.util;

import com.sun.jna.platform.win32.WinBase;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class MathHelper {
    public static Vec3i V3toV3i(Vec3 input) {
        return new Vec3i((int)Math.floor(input.x), (int)Math.floor(input.y), (int)Math.floor(input.z));
    }

    public static VoxelShape rotateVoxelShape(VoxelShape pShape, int pRot90DegreeSteps) {
        AABB bounds = pShape.bounds();

        Point2D minInput = new Point2D.Double(bounds.minX, bounds.minZ);
        Point2D maxInput = new Point2D.Double(bounds.maxX, bounds.maxZ);
        Point2D minResult = new Point2D.Double();
        Point2D maxResult = new Point2D.Double();
        AffineTransform rotation = new AffineTransform();
        rotation.rotate(Math.PI / 2 * pRot90DegreeSteps, 0.5, 0.5);
        rotation.transform(minInput, minResult);
        rotation.transform(maxInput, maxResult);

        VoxelShape output = Block.box(
                Math.min(minResult.getX(), maxResult.getX()) * 16, bounds.minY * 16, Math.min(minResult.getY(), maxResult.getY()) * 16,
                Math.max(minResult.getX(), maxResult.getX()) * 16, bounds.maxY * 16, Math.max(minResult.getY(), maxResult.getY()) * 16
        );

        return output;
    }
}
