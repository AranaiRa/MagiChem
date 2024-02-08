/**
 * This class largely adapted from a class from AlchemyLib. See the original here:
 * https://github.com/SmashingMods/AlchemyLib/blob/1.19.x/src/main/java/com/smashingmods/alchemylib/api/blockentity/container/FakeItemRenderer.java
 */

package com.aranaira.magichem.util.render;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class RenderUtils {

    public static final Minecraft MINECRAFT_REF = Minecraft.getInstance();
    public static final ItemRenderer ITEM_RENDERER = MINECRAFT_REF.getItemRenderer();

    private static ModelResourceLocation getResourceLocation(ItemStack stack) {
        ModelResourceLocation output = null;

        if (stack.getItem() instanceof MateriaItem) {
            output = new ModelResourceLocation(new ResourceLocation(MagiChemMod.MODID, stack.getItem().toString()), "inventory");
        }

        return output;
    }

    //This function taken from EnderIO. See the original here: https://github.com/Team-EnderIO/EnderIO/blob/dev/1.19.x/src/core/java/com/enderio/core/client/RenderUtil.java
    public static void renderFace(Direction face, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, TextureAtlasSprite texture,
                                  float x, float y, float z, float w, float h, int tint, int packedLight) {
        switch (face) {
            case DOWN -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, 1.0f - z, 1.0f - z, y, y, y + h, y + h, x, x + w, y, y + h, 0, -1, 0);
            case UP -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, z, z, y + h, y + h, y, y, x, x + w, y, y + h, 0, 1, 0);
            case NORTH -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, y + h, y, z, z, z, z, x, x + w, y, y + h, 0, 0, -1);
            case SOUTH -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, y, y + h, 1.0f - z, 1.0f - z, 1.0f - z, 1.0f - z, x + w, x, y + h, y, 0, 0, 1);
            case WEST -> renderFace(pose, normal, consumer, texture, tint, packedLight, 1.0f - z, 1.0f - z, y + h, y, x, x + w, x + w, x, x, x + w, y, y + h, 1, 0, 0);
            case EAST -> renderFace(pose, normal, consumer, texture, tint, packedLight, z, z, y, y + h, x, x + w, x + w, x, x + w, x, y + h, y, -1, 0, 0);
        }
    }

    public static void renderFaceWithUV(Direction face, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, TextureAtlasSprite texture,
                                        float x, float y, float z, float w, float h,
                                        float u, float uw, float v, float vh,
                                        int tint, int packedLight) {
        switch (face) {
            case DOWN -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, 1.0f - z, 1.0f - z, y, y, y + h, y + h, u, uw, v, vh, 0, -1, 0);
            case UP -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, z, z, y + h, y + h, y, y, u, uw, v, vh, 0, 1, 0);
            case NORTH -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, y + h, y, z, z, z, z, u, uw, v, vh, 0, 0, -1);
            case SOUTH -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, y, y + h, 1.0f - z, 1.0f - z, 1.0f - z, 1.0f - z, u, uw, v, vh, 0, 0, 1);
            case WEST -> renderFace(pose, normal, consumer, texture, tint, packedLight, 1.0f - z, 1.0f - z, y + h, y, x, x + w, x + w, x, u, uw, v, vh, 1, 0, 0);
            case EAST -> renderFace(pose, normal, consumer, texture, tint, packedLight, z, z, y, y + h, x, x + w, x + w, x, u, uw, v, vh, -1, 0, 0);
        }
    }

    //This function taken from EnderIO. See the original here: https://github.com/Team-EnderIO/EnderIO/blob/dev/1.20.1/src/core/java/com/enderio/core/client/RenderUtil.java
    private static void renderFace(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, TextureAtlasSprite texture, int tint, int packedLight,
                                   float x0, float x1, float y0, float y1, float z0, float z1, float z2,
                                   float z3, float u0, float u1, float v0, float v1,
                                   float normalX, float normalY, float normalZ) {
        float minU = u0 * (float)texture.contents().width();
        float maxU = u1 * (float)texture.contents().width();
        float minV = v0 * (float)texture.contents().height();
        float maxV = v1 * (float)texture.contents().height();

        consumer.vertex(pose, x0, y0, z0).color(tint).uv(texture.getU(minU), texture.getV(minV)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, normalX, normalY, normalZ).endVertex();
        consumer.vertex(pose, x1, y0, z1).color(tint).uv(texture.getU(maxU), texture.getV(minV)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, normalX, normalY, normalZ).endVertex();
        consumer.vertex(pose, x1, y1, z2).color(tint).uv(texture.getU(maxU), texture.getV(maxV)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, normalX, normalY, normalZ).endVertex();
        consumer.vertex(pose, x0, y1, z3).color(tint).uv(texture.getU(minU), texture.getV(maxV)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, normalX, normalY, normalZ).endVertex();
    }
}