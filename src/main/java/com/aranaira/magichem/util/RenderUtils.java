/**
 * This class largely adapted from a class from AlchemyLib. See the original here:
 * https://github.com/SmashingMods/AlchemyLib/blob/1.19.x/src/main/java/com/smashingmods/alchemylib/api/blockentity/container/FakeItemRenderer.java
 */

package com.aranaira.magichem.util;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class RenderUtils {

    public static final Minecraft MINECRAFT_REF = Minecraft.getInstance();
    public static final ItemRenderer ITEM_RENDERER = MINECRAFT_REF.getItemRenderer();
    public static final TextureManager TEXTURE_MANAGER = MINECRAFT_REF.getTextureManager();

    public static void RenderGhostedItemStack(ItemStack stack, int x, int y, float a) {
        BakedModel bakedModel = getBakedModel(stack);

        TEXTURE_MANAGER.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(true, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableBlend();

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(x + 8.0D, y + 8.0D, 0f);
        modelViewStack.scale(16.0f, -16.0f, 16.0f);
        RenderSystem.applyModelViewMatrix();

        if (!bakedModel.usesBlockLight()) {
            Lighting.setupForFlatItems();
        }

        MultiBufferSource.BufferSource bufferSource = MINECRAFT_REF.renderBuffers().bufferSource();
        ITEM_RENDERER.render(stack,
                ItemTransforms.TransformType.GUI,
                false,
                new PoseStack(),
                getWrappedBuffer(bufferSource, a),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                bakedModel);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();

        if (!bakedModel.usesBlockLight()) {
            Lighting.setupFor3DItems();
        }

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static BakedModel getBakedModel(ItemStack stack) {
        ModelResourceLocation resourceLocation = getResourceLocation(stack);
        return ITEM_RENDERER.getItemModelShaper().getModelManager().getModel(resourceLocation);
    }

    private static ModelResourceLocation getResourceLocation(ItemStack stack) {
        ModelResourceLocation output = null;

        if (stack.getItem() instanceof MateriaItem) {
            output = new ModelResourceLocation(new ResourceLocation(MagiChemMod.MODID, stack.getItem().toString()), "inventory");
        }

        return output;
    }

    private static MultiBufferSource getWrappedBuffer(MultiBufferSource bufferSource, float a) {
        return renderType -> new WrappedVertexConsumer(bufferSource.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS)), 1f, 1f, 1f, a);
    }
}

/**
 * This class taken directly from AlchemyLib, see link below for the original.
 * https://github.com/SmashingMods/AlchemyLib/blob/1.19.x/src/main/java/com/smashingmods/alchemylib/api/blockentity/container/FakeItemRenderer.java
 *
 * This is a custom wrapper implementation of {@link VertexConsumer} for the purpose of adding an alpha channel for optional
 * transparency when rendering ItemStacks.
 */
class WrappedVertexConsumer implements VertexConsumer {

    protected final VertexConsumer consumer;
    protected final float red;
    protected final float green;
    protected final float blue;
    protected final float alpha;

    public WrappedVertexConsumer(VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, float pAlpha) {
        this.consumer = pConsumer;
        this.red = pRed;
        this.green = pGreen;
        this.blue = pBlue;
        this.alpha = pAlpha;
    }

    @Override
    public VertexConsumer vertex(double pX, double pY, double pZ) {
        return consumer.vertex(pX, pY, pZ);
    }

    @Override
    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
        return consumer.color((int)(pRed * red), (int)(pGreen * green), (int)(pBlue * blue), (int)(pAlpha * alpha));
    }

    @Override
    public VertexConsumer uv(float pU, float pV) {
        return consumer.uv(pU, pV);
    }

    @Override
    public VertexConsumer overlayCoords(int pU, int pV) {
        return consumer.overlayCoords(pU, pV);
    }

    @Override
    public VertexConsumer uv2(int pU, int pV) {
        return consumer.uv2(pU, pV);
    }

    @Override
    public VertexConsumer normal(float pX, float pY, float pZ) {
        return consumer.normal(pX, pY, pZ);
    }

    @Override
    public void endVertex() {
        consumer.endVertex();
    }

    @Override
    public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
        consumer.defaultColor(pDefaultR, pDefaultG, pDefaultB, pDefaultA);
    }

    @Override
    public void unsetDefaultColor() {
        consumer.unsetDefaultColor();
    }
}
