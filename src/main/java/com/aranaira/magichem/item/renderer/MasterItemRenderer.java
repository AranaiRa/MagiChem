package com.aranaira.magichem.item.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.MateriaVesselContentsRenderUtil;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.util.NonNullLazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.aranaira.magichem.block.entity.renderer.MateriaJarQuadBlockEntityRenderer.X_OFFSET;
import static com.aranaira.magichem.block.entity.renderer.MateriaJarQuadBlockEntityRenderer.Z_OFFSET;

public class MasterItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static NonNullLazy<BlockEntityWithoutLevelRenderer> MASTER_RENDERER = null;

    public static NonNullLazy<BlockEntityWithoutLevelRenderer> getOrCreateMasterRenderer() {
        if(MASTER_RENDERER == null) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("CustomModelData",1);
            STACK_PHILOSOPHERS_CONCOCTION_UNBOTTLED_DUMMY.setTag(nbt);

            MASTER_RENDERER = NonNullLazy.of(() -> new MasterItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()));
        }
        return MASTER_RENDERER;
    }

    //Special models
    public static final ResourceLocation RENDERER_JAR = new ResourceLocation(MagiChemMod.MODID, "item/special/materia_jar");
    public static final ResourceLocation RENDERER_JAR_QUAD = new ResourceLocation(MagiChemMod.MODID, "item/special/materia_jar_quad");
    public static final ResourceLocation RENDERER_VESSEL = new ResourceLocation(MagiChemMod.MODID, "item/special/materia_vessel");
    private static final ItemStack STACK_PHILOSOPHERS_STONE_DUMMY = new ItemStack(ItemRegistry.PHILOSOPHERS_STONE_DUMMY.get());
    public static final ResourceLocation TEXTURE_PHILOSOPHERS_STONE_DUMMY = new ResourceLocation(MagiChemMod.MODID, "item/philosophers_stone");
    private static final ItemStack STACK_PHILOSOPHERS_CONCOCTION_DUMMY = new ItemStack(ItemRegistry.PHILOSOPHERS_STONE_DUMMY.get());
    public static final ResourceLocation TEXTURE_PHILOSOPHERS_CONCOCTION_DUMMY = new ResourceLocation(MagiChemMod.MODID, "item/admixture_philosophers_concoction");
    private static final ItemStack STACK_PHILOSOPHERS_CONCOCTION_UNBOTTLED_DUMMY = new ItemStack(ItemRegistry.PHILOSOPHERS_STONE_DUMMY.get());
    public static final ResourceLocation TEXTURE_PHILOSOPHERS_CONCOCTION_UNBOTTLED_DUMMY = new ResourceLocation(MagiChemMod.MODID, "item/materia_unbottled_philosophers_concoction");
    private BakedModel bakedModel;

    private final List<Direction> sides = Util.make(new ArrayList<>(), c -> {
        Collections.addAll(c, Direction.values());
        c.add(null);
    });

    public MasterItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pStack.getItem() == BlockRegistry.MATERIA_JAR.get().asItem())
            renderMateriaJar(pStack, pDisplayContext, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        else if(pStack.getItem() == BlockRegistry.MATERIA_JAR_QUAD.get().asItem())
            renderMateriaJarQuad(pStack, pDisplayContext, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        else if(pStack.getItem() == BlockRegistry.MATERIA_VESSEL.get().asItem())
            renderMateriaVessel(pStack, pDisplayContext, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        else if(pStack.getItem() == ItemRegistry.PHILOSOPHERS_STONE.get())
            renderPhilosophersStone(pStack, pDisplayContext, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        else if(pStack.getItem() instanceof AdmixtureItem ai) {
            renderPhilosophersConcoction(pStack, pDisplayContext, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, InventoryHelper.isMateriaUnbottled(pStack));
        }

        super.renderByItem(pStack, pDisplayContext, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderMateriaJar(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.bakedModel = Minecraft.getInstance().getModelManager().getModel(RENDERER_JAR);

        PoseStack.Pose last = pPoseStack.last();
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.solid());

        //ghz version
        BakedModel jarModel = bakedModel.getOverrides().resolve(bakedModel, pStack, null, null, 0);
        if (jarModel == null)
            jarModel = bakedModel;

        //TODO: Figure out how to get handedness from a displaycontext
        boolean leftHand = false;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5D, 0.5D, 0.5D);
        jarModel = ForgeHooksClient.handleCameraTransforms(pPoseStack, jarModel, pDisplayContext, leftHand);

        CompoundTag nbt = pStack.getTag();
        if(nbt != null) {
            if(nbt.contains("type")) {
                MateriaItem materia = ItemRegistry.getMateriaMap(false, false)
                        .get(nbt.getString("type"));
                int cap = materia instanceof EssentiaItem ? ServerConfig.materiaJarEssentiaCapacity : ServerConfig.materiaJarAdmixtureCapacity;
                float fill = (float)nbt.getInt("amount") / (float)cap;

                MateriaVesselContentsRenderUtil.renderJarFluidContents(last.pose(), last.normal(), buffer, fill, materia.getMateriaColor(), pPackedLight);
            }
        }

        pPoseStack.translate(-0.5D, -0.5D, -0.5D);

        buffer = pBuffer.getBuffer(ForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
        RandomSource rnd = RandomSource.create();
        for (Direction side : sides)
        {
            rnd.setSeed(42);
            for (BakedQuad quad : jarModel.getQuads(null, side, rnd, ModelData.EMPTY, null))
            {
                buffer.putBulkData(pPoseStack.last(), quad, 1.0f, 1.0f, 1.0f, pPackedLight, pPackedOverlay);
            }
        }

        pPoseStack.popPose();
    }

    private void renderMateriaJarQuad(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.bakedModel = Minecraft.getInstance().getModelManager().getModel(RENDERER_JAR_QUAD);

        PoseStack.Pose last = pPoseStack.last();
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.solid());

        //ghz version
        BakedModel jarModel = bakedModel.getOverrides().resolve(bakedModel, pStack, null, null, 0);
        if (jarModel == null)
            jarModel = bakedModel;

        //TODO: Figure out how to get handedness from a displaycontext
        boolean leftHand = false;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5D, 0.5D, 0.5D);
        jarModel = ForgeHooksClient.handleCameraTransforms(pPoseStack, jarModel, pDisplayContext, leftHand);

        CompoundTag nbt = pStack.getTag();
        if(nbt != null) {
            for(int i=0; i<4; i++) {
                if (nbt.contains("materiaType"+i)) {
                    CompoundTag entry = nbt.getCompound("materiaType"+i);
                    String type = entry.getString("type");

                    if(type.equals("empty")) continue;

                    MateriaItem materia = ItemRegistry.getMateriaMap(false, false)
                            .get(type);
                    int cap = materia instanceof EssentiaItem ? ServerConfig.materiaJarEssentiaCapacity : ServerConfig.materiaJarAdmixtureCapacity;
                    float fill = (float) entry.getInt("count") / (float) cap;

                    MateriaVesselContentsRenderUtil.renderJarFluidContentsWithXZOffset(last.pose(), last.normal(), buffer, fill, materia.getMateriaColor(), pPackedLight, X_OFFSET[i], Z_OFFSET[i]);
                }
            }
        }

        pPoseStack.translate(-0.5D, -0.5D, -0.5D);

        buffer = pBuffer.getBuffer(ForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
        RandomSource rnd = RandomSource.create();
        for (Direction side : sides)
        {
            rnd.setSeed(42);
            for (BakedQuad quad : jarModel.getQuads(null, side, rnd, ModelData.EMPTY, null))
            {
                buffer.putBulkData(pPoseStack.last(), quad, 1.0f, 1.0f, 1.0f, pPackedLight, pPackedOverlay);
            }
        }

        pPoseStack.popPose();
    }

    private void renderMateriaVessel(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.bakedModel = Minecraft.getInstance().getModelManager().getModel(RENDERER_VESSEL);

        PoseStack.Pose last = pPoseStack.last();
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.solid());

        //ghz version
        BakedModel jarModel = bakedModel.getOverrides().resolve(bakedModel, pStack, null, null, 0);
        if (jarModel == null)
            jarModel = bakedModel;

        //TODO: Figure out how to get handedness from a displaycontext
        boolean leftHand = false;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5D, 0.5D, 0.5D);
        jarModel = ForgeHooksClient.handleCameraTransforms(pPoseStack, jarModel, pDisplayContext, leftHand);

        CompoundTag nbt = pStack.getTag();
        if(nbt != null) {
            if(nbt.contains("type")) {
                MateriaItem materia = ItemRegistry.getMateriaMap(false, false)
                        .get(nbt.getString("type"));
                int cap = materia instanceof EssentiaItem ? ServerConfig.materiaVesselEssentiaCapacity : ServerConfig.materiaVesselAdmixtureCapacity;
                float fill = (float)nbt.getInt("amount") / (float)cap;

                MateriaVesselContentsRenderUtil.renderVesselFluidContents(last.pose(), last.normal(), buffer, fill, materia.getMateriaColor(), pPackedLight);
                if(materia instanceof EssentiaItem ei)
                    MateriaVesselContentsRenderUtil.renderVesselEssentiaLabel(last.pose(), last.normal(), buffer, ei, Direction.NORTH, pPackedLight);
            }
        }

        pPoseStack.translate(-0.5D, -0.5D, -0.5D);

        buffer = pBuffer.getBuffer(ForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
        RandomSource rnd = RandomSource.create();
        for (Direction side : sides)
        {
            rnd.setSeed(42);
            for (BakedQuad quad : jarModel.getQuads(null, side, rnd, ModelData.EMPTY, null))
            {
                buffer.putBulkData(pPoseStack.last(), quad, 1.0f, 1.0f, 1.0f, pPackedLight, pPackedOverlay);
            }
        }

        pPoseStack.popPose();
    }

    private void renderPhilosophersStone(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        PoseStack.Pose last = pPoseStack.last();
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.solid());
        final Minecraft instance = Minecraft.getInstance();

        if(instance.player != null) {
            int period = 100;
            int gameTime = (int)(instance.player.level().getGameTime() % period);

            int colorFromTime = ColorUtils.getLerpedRainbowColor((float) gameTime  / (float) period);
            int[] rgba = ColorUtils.getRGBAIntTintFromPackedInt(colorFromTime);

            pPoseStack.pushPose();

            if (pDisplayContext == ItemDisplayContext.GUI) {
                TextureAtlasSprite texture = instance.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_PHILOSOPHERS_STONE_DUMMY);
                int[] white = new int[]{255, 255, 255};
                gameTime = (int)(instance.player.level().getGameTime() % (period * 5));
                boolean flip = (int)(instance.player.level().getGameTime() % (period * 10)) > gameTime;
                int pingPongTime = flip ? (period * 5) - gameTime : gameTime;

                pPoseStack.translate(0.5, 0.5, 0.5);
                WorldRenderUtils.renderRadiant(pingPongTime, pPoseStack, pBuffer, white, white, 128, 3.75f, false);
                WorldRenderUtils.renderRadiant(pingPongTime, pPoseStack, pBuffer, rgba, white, 255, 4f, false);

                RenderUtils.renderFaceWithUV(Direction.SOUTH, pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.cutout()), texture,
                    -0.5f, -0.5f, 10.0f, 1.0f, 1.0f,
                    0f,1f,
                    1f,0f,
                    0xffffffff, pPackedLight);
            } else {
                gameTime = (int)(instance.player.level().getGameTime() % (period * 5));
                boolean flip = (int)(instance.player.level().getGameTime() % (period * 10)) > gameTime;
                int pingPongTime = flip ? (period * 5) - gameTime : gameTime;

                pPoseStack.translate(0.5, 0.625, 0.5);
                WorldRenderUtils.renderRadiant(pingPongTime, pPoseStack, pBuffer, rgba, new int[]{255, 255, 255}, 255, 4f, false);

                pPoseStack.translate(0.0, -0.125, 0.0);
                instance.getItemRenderer().renderStatic(STACK_PHILOSOPHERS_STONE_DUMMY, pDisplayContext, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, null, 0);
            }
            pPoseStack.popPose();
        }
    }

    private void renderPhilosophersConcoction(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, boolean pUnbottled) {
        PoseStack.Pose last = pPoseStack.last();
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.solid());
        final Minecraft instance = Minecraft.getInstance();

        if(instance.player != null) {
            long gameTime = instance.player.level().getGameTime();
            int period = 100;

            int colorFromTime = ColorUtils.getLerpedRainbowColor((float) (gameTime % period) / (float) period);
            int[] rgba = ColorUtils.getRGBAIntTintFromPackedInt(colorFromTime);

            pPoseStack.pushPose();

            if (pDisplayContext == ItemDisplayContext.GUI) {
                TextureAtlasSprite texture = instance.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(pUnbottled ? TEXTURE_PHILOSOPHERS_CONCOCTION_UNBOTTLED_DUMMY : TEXTURE_PHILOSOPHERS_CONCOCTION_DUMMY);
                int[] white = new int[]{255, 255, 255};

                pPoseStack.translate(0.5, 0.5, 0.5);
                WorldRenderUtils.renderRadiant(gameTime, pPoseStack, pBuffer, white, white, 128, 3.75f, false);
                WorldRenderUtils.renderRadiant(gameTime, pPoseStack, pBuffer, rgba, white, 255, 4f, false);

                RenderUtils.renderFaceWithUV(Direction.SOUTH, pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.cutout()), texture,
                        -0.5f, -0.5f, 10.0f, 1.0f, 1.0f,
                        0f,1f,
                        1f,0f,
                        0xffffffff, pPackedLight);
            } else {
                pPoseStack.translate(0.5, 0.625, 0.5);
                WorldRenderUtils.renderRadiant(gameTime, pPoseStack, pBuffer, rgba, new int[]{255, 255, 255}, 255, 4f, false);

                pPoseStack.translate(0.0, -0.125, 0.0);
                instance.getItemRenderer().renderStatic(pUnbottled ? STACK_PHILOSOPHERS_CONCOCTION_UNBOTTLED_DUMMY : STACK_PHILOSOPHERS_CONCOCTION_DUMMY, pDisplayContext, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, null, 0);
            }
            pPoseStack.popPose();
        }
    }
}
