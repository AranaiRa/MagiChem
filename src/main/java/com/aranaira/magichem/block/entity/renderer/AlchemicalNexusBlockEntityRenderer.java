package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.foundation.AlchemicalNexusAnimSpec;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.api.ManaAndArtificeMod;
import com.mna.items.ItemInit;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

public class AlchemicalNexusBlockEntityRenderer implements BlockEntityRenderer<AlchemicalNexusBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_CRYSTAL = new ResourceLocation(MagiChemMod.MODID, "obj/special/alchemical_nexus_crystal");
    public static final ResourceLocation RENDERER_MODEL_BOOKMARKS = new ResourceLocation("mna", "item/special/mark_book_open");
    public static final ResourceLocation FLUID_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/fluid/experience_still");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public static final float ITEM_HOVER_RADIUS = 0.5f;

    public static final int
            CIRCLE_1_ROTATION_PERIOD = 200, CIRCLE_2_ROTATION_PERIOD = 300, CIRCLE_3_ROTATION_PERIOD = 400, CIRCLE_4_ROTATION_PERIOD = 700, CIRCLE_5_ROTATION_PERIOD = 600;

    public AlchemicalNexusBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderCrystal(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderItems(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);

        this.renderBeam(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderCircle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);

        float fluidPercent = (float)pBlockEntity.getFluidInTank(0).getAmount() / (float) Config.fuseryTankCapacity;

        if(fluidPercent > 0) {
            this.renderTankFluid(fluidPercent, pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        }
    }

    private void renderTankFluid(float pFluidPercent, AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction renderDir = dir;

        float mappedFluidH = Math.min(Math.max(pFluidPercent - 0.25f, 0), 0.75f) / 0.75f;
        float fluidHVariance = 0.5625f;
        Direction facing = Direction.UP;
        float
                y = 0.0625f,
                fx1 = -1f, fz1 = -1f,
                fxt = 0, fyt = 0, fzt = 0,
                fw = 0.125f, fh = fluidHVariance * mappedFluidH,
                fwt = 1, fht = 1,

                fu1 = 0.25f, fu2 = 0.375f, fut = 0.0f,
                fv1 = 0.0f, fv2 = fluidHVariance * mappedFluidH, fvt = 0.25f;

        if(dir == Direction.NORTH) {
            facing = Direction.NORTH;
            fx1 = 0.4375f;
            fz1 = -0.90625f;
            fxt = 0.4375f;
            fyt = -0.90625f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.125f;
            fht = 0.3125f;
            fut = fwt;
            fvt = fht;
        } else if(dir == Direction.EAST) {
            facing = Direction.WEST;
            fx1 = 0.4375f;
            fz1 = -0.90625f;
            fxt = 1.59375f;
            fyt = 0.4375f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.3125f;
            fht = 0.125f;
            fut = fwt;
            fvt = fht;
        } else if(dir == Direction.SOUTH) {
            facing = Direction.SOUTH;
            fx1 = 0.4375f;
            fz1 = -0.90625f;
            fxt = 0.4375f;
            fyt = 1.59375f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.125f;
            fht = 0.3125f;
            fut = fwt;
            fvt = fht;
        } else if(dir == Direction.WEST) {
            facing = Direction.EAST;
            fx1 = 0.4375f;
            fz1 = -0.90625f;
            fxt = -0.90625f;
            fyt = 0.4375f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.3125f;
            fht = 0.125f;
            fut = fwt;
            fvt = fht;
        }

        RenderUtils.renderFaceWithUV(facing, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.cutout()),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE),
                fx1, y, fz1, fw, fh, fu1, fu2, fv1, fv2, 0xFFFFFFFF, pPackedLight);

        RenderUtils.renderFaceWithUV(Direction.UP, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.cutout()),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE),
                fxt, fyt, fzt, fwt, fht, 0, fut, 0, fvt, 0xFFFFFFFF, pPackedLight);
    }

    private void renderCrystal(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        pPoseStack.pushPose();

        float loopingTime = ((world.getGameTime() + pPartialTick) % (AlchemicalNexusBlockEntity.CRYSTAL_BOB_PERIOD * 20)) / AlchemicalNexusBlockEntity.CRYSTAL_BOB_PERIOD;
        float height = (float)((Math.sin(loopingTime * Math.PI) + 1.0) * 0.5);
        float heightOffsetCrystal = AlchemicalNexusBlockEntity.CRYSTAL_BOB_HEIGHT_MAX * height;
        float heightOffsetMark    = AlchemicalNexusBlockEntity.CRYSTAL_BOB_HEIGHT_MAX * (1 - height);

        pPoseStack.translate(0.5f, 1.375f + heightOffsetCrystal, 0.5f);
        pPoseStack.mulPose(Axis.YP.rotationDegrees((pBlockEntity.crystalAngle + (pPartialTick * pBlockEntity.crystalRotSpeed))));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CRYSTAL, pPoseStack, pPackedLight, pPackedOverlay);

        pPoseStack.popPose();

        if(pBlockEntity.getMarkItem().getItem() == ItemInit.BOOK_MARKS.get()) {
            pPoseStack.pushPose();
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            if(facing == Direction.NORTH) {
                pPoseStack.translate(0.75f, 0.625f + heightOffsetMark, 1.75f);
                pPoseStack.mulPose(Axis.YP.rotation(3.14159f * 1.5f));
                pPoseStack.mulPose(Axis.ZP.rotation(3.14159f * 0.25f));
            } else if(facing == Direction.EAST) {
                pPoseStack.translate(-0.75f, 0.625f + heightOffsetMark, 0.75f);
                pPoseStack.mulPose(Axis.YP.rotation(3.14159f));
                pPoseStack.mulPose(Axis.ZP.rotation(3.14159f * 0.25f));
            } else if(facing == Direction.SOUTH) {
                pPoseStack.translate(0.25f, 0.625f + heightOffsetMark, -0.75f);
                pPoseStack.mulPose(Axis.YP.rotation(3.14159f * 0.5f));
                pPoseStack.mulPose(Axis.ZP.rotation(3.14159f * 0.25f));
            } else if(facing == Direction.WEST) {
                pPoseStack.translate(1.75f, 0.625f + heightOffsetMark, 0.25f);
                pPoseStack.mulPose(Axis.ZP.rotation(3.14159f * 0.25f));
            }
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BOOKMARKS, pPoseStack, pPackedLight, pPackedOverlay);

            pPoseStack.popPose();
        }
    }

    private void renderItems(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        List<ItemStack> inputItems = pBlockEntity.getInputItems();
        float angleShift = 0f;
        if(inputItems.size() > 0)
             angleShift = (float)(Math.PI * 2) / inputItems.size();

        pPoseStack.pushPose();

        {
            int i = 0;
            for (ItemStack is : inputItems) {
                pPoseStack.pushPose();

                float x = (float) Math.cos(pBlockEntity.itemAngle + (pBlockEntity.itemRotSpeed * pPartialTick) + (i * angleShift)) * ITEM_HOVER_RADIUS;
                float z = (float) Math.sin(pBlockEntity.itemAngle + (pBlockEntity.itemRotSpeed * pPartialTick) + (i * angleShift)) * ITEM_HOVER_RADIUS;
                float rotAmount = ((pBlockEntity.itemAngle + (pBlockEntity.itemRotSpeed * pPartialTick) + (float)(Math.PI * i * 0.375)) % (float)(Math.PI * 8));

                float s = Math.max(0,Math.min(1, pBlockEntity.itemScale));

                Vector3 origin = new Vector3(0.5f, 1.9375f, 0.5f);
                Vector3 translated = new Vector3(origin.x + x, origin.y, origin.z + z);

                pPoseStack.translate(translated.x, translated.y, translated.z);
                pPoseStack.scale(0.375f * s, 0.375f * s, 0.375f * s);
                pPoseStack.mulPose(Axis.YP.rotation(rotAmount));
                pPoseStack.mulPose(Axis.XP.rotation(rotAmount * 0.25f));
                pPoseStack.mulPose(Axis.ZP.rotation(rotAmount * 0.5f));

                itemRenderer.renderStatic(is, ItemDisplayContext.FIXED, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, world, 0);

                pPoseStack.popPose();
                i++;
            }
        }

        pPoseStack.popPose();
    }

    private void renderBeam(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        float percent = 0f;
        int animStage = pBlockEntity.getAnimStage();

        if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_RAMP_CRAFTING) {
            percent = (float)pBlockEntity.getProgress() / (float)AlchemicalNexusBlockEntity.getAnimSpec(pBlockEntity.getPowerLevel()).ticksInRampBeam;
        }
        else if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CANCEL_CRAFTING ||
                animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CANCEL_CRAFTING_ADVANCED) {
            percent = 1 - ((float)pBlockEntity.getProgress() / (float)AlchemicalNexusBlockEntity.getAnimSpec(pBlockEntity.getPowerLevel()).ticksInRampBeam);
        }
        else if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CRAFTING) {
            percent = 1f;
        }

        //do render
        if(percent > 0f) {
            pPoseStack.pushPose();
            Vec3 start = Vec3.upFromBottomCenterOf(pBlockEntity.getBlockPos(), 4.25);
            Vec3 end = Vec3.atCenterOf(pBlockEntity.getBlockPos());

            pPoseStack.translate(0.5D, 3.5D, 0.5D);

            WorldRenderUtils.renderBeam(pBlockEntity.getLevel(), pPartialTick, pPoseStack, pBuffer, pPackedLight,
                    start, end, percent, new int[]{255, 255, 255}, 255, 0.0625f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);

            pPoseStack.popPose();
        }
    }

    private void renderCircle(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        float percentFill = 0f;
        int animStage = pBlockEntity.getAnimStage();
        boolean doAll = false;
        boolean doNext = true;
        final TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);

        if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_RAMP_CIRCLE) {
            AlchemicalNexusAnimSpec spec = AlchemicalNexusBlockEntity.getAnimSpec(pBlockEntity.getPowerLevel());
            percentFill = (pBlockEntity.getProgress() + pPartialTick) / (float)spec.ticksInRampCircle;
        }
        else if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CANCEL_CIRCLE) {
            AlchemicalNexusAnimSpec spec = AlchemicalNexusBlockEntity.getAnimSpec(pBlockEntity.getPowerLevel());
            percentFill = 1 - ((pBlockEntity.getProgress() + pPartialTick) / (float)spec.ticksInRampCircle);
        }
        else if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_RAMP_CRAFTING_CIRCLE) {
            AlchemicalNexusAnimSpec spec = AlchemicalNexusBlockEntity.getAnimSpec(pBlockEntity.getPowerLevel());
            percentFill = (pBlockEntity.getProgress() + pPartialTick) / (float)spec.ticksInRampCircle;
        }
        else if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CANCEL_CRAFTING_CIRCLE) {
            AlchemicalNexusAnimSpec spec = AlchemicalNexusBlockEntity.getAnimSpec(pBlockEntity.getPowerLevel());
            percentFill = 1 - ((pBlockEntity.getProgress() + pPartialTick) / (float)spec.ticksInRampCircle);
            if(pBlockEntity.getProgressHolderItem().isEmpty())
                doAll = true;
        }
        else if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CRAFTING_IDLE ||
                animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_RAMP_CRAFTING_SPEEDUP) {
            percentFill = 1;
            doNext = false;
        }
        else if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CANCEL_CRAFTING_SPEEDUP) {
            percentFill = 1;
            doNext = false;
        }
        else if(animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_SHLORPS ||
                animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CRAFTING ||
                animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_RAMP_CRAFTING ||
                animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CANCEL_CRAFTING ||
                animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CANCEL_CRAFTING_ADVANCED) {
            percentFill = 1;
        }

        if(percentFill > 0) {
            float previousCircles = doAll ? percentFill : 1f;
            int craftingStage = pBlockEntity.getCraftingStage();

            if(craftingStage == 0) {
                renderStage1Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, percentFill, texture);
            }
            else if(craftingStage == 1) {
                renderStage1Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                if(doNext) renderStage2Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, percentFill, texture);
            }
            else if(craftingStage == 2) {
                renderStage1Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                renderStage2Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                if(doNext) renderStage3Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, percentFill, texture);
            }
            else if(craftingStage == 3) {
                renderStage1Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                renderStage2Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                renderStage3Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                if(doNext) renderStage4Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, percentFill, texture);
            }
            else if(craftingStage == 4) {
                renderStage1Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                renderStage2Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                renderStage3Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                renderStage4Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, previousCircles, texture);
                if(doNext) renderStage5Circle(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, percentFill, texture);
            }
        }
    }

    private void renderStage1Circle(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, float pPercentFill, TextureAtlasSprite pTexture) {
        Vector3 center = new Vector3(0.5, 3.5, 0.5);
        float rotation = -(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % CIRCLE_1_ROTATION_PERIOD) / CIRCLE_1_ROTATION_PERIOD) * (float)Math.PI * 2;

        RenderUtils.generateMagicCircleRing(center,
                3, 0.9375f, 0.125f, rotation + (float)Math.PI, pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center,
                6, 1.375f, 0.375f, rotation, pTexture,
                new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);
    }

    private void renderStage2Circle(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, float pPercentFill, TextureAtlasSprite pTexture) {
        Vector3 center = new Vector3(0.5, 3.5, 0.5);
        float rotation = -(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % CIRCLE_2_ROTATION_PERIOD) / CIRCLE_2_ROTATION_PERIOD) * (float)Math.PI * 2;

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.125, 0)),
                12, 1.5f, 0.125f, -rotation + (float)Math.PI, pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -2.53125, 0)),
                5, 1.0f, 0.1875f, -rotation + (float)Math.PI, pTexture,
                new Vec2(0, 3f), new Vec2(12, 4.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);
    }

    private void renderStage3Circle(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, float pPercentFill, TextureAtlasSprite pTexture) {
        Vector3 center = new Vector3(0.5, 3.5, 0.5);
        float rotation = -(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % CIRCLE_3_ROTATION_PERIOD) / CIRCLE_3_ROTATION_PERIOD) * (float)Math.PI * 2;

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.28125, 0)),
                4, 2.625f, 0.375f, rotation, pTexture,
                new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.21875, 0)),
                4, 2.625f, 0.375f, rotation + (float)(Math.PI * 5 / 4), pTexture,
                new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -2.625, 0)),
                5, 1.0f, 0.125f, rotation, pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);
    }

    private void renderStage4Circle(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, float pPercentFill, TextureAtlasSprite pTexture) {
        Vector3 center = new Vector3(0.5, 3.5, 0.5);
        float rotation = -(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % CIRCLE_4_ROTATION_PERIOD) / CIRCLE_4_ROTATION_PERIOD) * (float)Math.PI * 2;
        float rotation5 = -(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % CIRCLE_5_ROTATION_PERIOD) / CIRCLE_5_ROTATION_PERIOD) * (float)Math.PI * 2;

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.375, 0)),
                12, 2.825f, 0.125f, -rotation + (float)(Math.PI / 2), pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.375, 0)),
                12, 3.075f, 0.1875f, -rotation + (float)(Math.PI / 2) * 3, pTexture,
                new Vec2(0, 3f), new Vec2(12, 4.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.375, 0)),
                12, 3.2625f, 0.125f, -rotation + (float)(Math.PI / 2) * 2, pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -3.375f, 0)),
                12, 1.825f, 0.125f, rotation5 + (float)(Math.PI / 2), pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);
    }

    private void renderStage5Circle(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, float pPercentFill, TextureAtlasSprite pTexture) {
        Vector3 center = new Vector3(0.5, 3.5, 0.5);
        float rotation = -(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % CIRCLE_5_ROTATION_PERIOD) / CIRCLE_5_ROTATION_PERIOD) * (float)Math.PI * 2;

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -1.140625, 0)),
                4, 1.75f, 0.125f, rotation + (float)(Math.PI / 3), pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -1.125, 0)),
                4, 1.75f, 0.125f, rotation + (float)(Math.PI / 3) * 2, pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -1.109375, 0)),
                4, 1.75f, 0.125f, rotation + (float)(Math.PI / 2) * 3, pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -3.375f, 0)),
                12, 2.075f, 0.1875f, rotation + (float)(Math.PI / 2) * 3, pTexture,
                new Vec2(0, 3f), new Vec2(12, 4.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -3.375f, 0)),
                12, 2.2625f, 0.125f, rotation + (float)(Math.PI / 2) * 2, pTexture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                pPercentFill, pPoseStack, pBuffer, pPackedLight);
    }
}
