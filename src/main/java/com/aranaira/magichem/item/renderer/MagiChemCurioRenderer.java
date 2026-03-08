package com.aranaira.magichem.item.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import static net.minecraft.world.item.ItemDisplayContext.HEAD;

public class MagiChemCurioRenderer implements ICurioRenderer {
    public static final ResourceLocation RENDERER_CURIO_MODEL_CROWN_OF_GLORY = new ResourceLocation(MagiChemMod.MODID, "obj/special/crown_of_glory");

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int packedLight, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if(itemStack.getItem() == ItemRegistry.CROWN_OF_GLORY.get())
            renderCrownOfGlory(itemStack, slotContext, poseStack, multiBufferSource, packedLight, netHeadYaw, headPitch);
    }

    private void renderCrownOfGlory(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, float netHeadYaw, float headPitch) {
        if (itemStack.getItem() == ItemRegistry.CROWN_OF_GLORY.get() && slotContext.entity() instanceof Player player) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(netHeadYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            ModelUtils.renderEntityModel(multiBufferSource.getBuffer(RenderType.cutout()), player.level(), RENDERER_CURIO_MODEL_CROWN_OF_GLORY, poseStack, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}
