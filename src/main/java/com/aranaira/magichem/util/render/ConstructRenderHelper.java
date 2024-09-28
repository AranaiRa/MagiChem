package com.aranaira.magichem.util.render;

import com.mna.api.tools.RLoc;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ConstructRenderHelper {
    public static final Vector3
        RENDER_OFFSET_HEAD = new Vector3(0, -1.3125, 0),
        RENDER_OFFSET_TORSO = new Vector3(0, -0.75, 0),
        RENDER_OFFSET_ARM_LEFT = new Vector3(0.375, -1.15625, 0),
        RENDER_OFFSET_ARM_RIGHT = new Vector3(-0.375, -1.15625, 0),
        RENDER_OFFSET_LEG_LEFT = new Vector3(0.125, -0.716506, -0.029006),
        RENDER_OFFSET_LEG_RIGHT = new Vector3(-0.125, -0.716506, -0.029006);

    public static Map<ConstructPartType, Pair<ResourceLocation, Vector3>> getRenderDataFromTag(CompoundTag pConstructConfig) {
        Map<ConstructPartType, Pair<ResourceLocation, Vector3>> out = new HashMap<>();

        if(pConstructConfig.contains("HEAD")) {
            final Pair<ResourceLocation, Vector3> head = tagKeyToRenderData(pConstructConfig.getString("HEAD"), false);
            out.put(ConstructPartType.HEAD, head);
            out.put(ConstructPartType.EYES, new Pair<>(tagKeyToEyeResourceLocation(pConstructConfig.getString("HEAD")), head.getSecond()));
        }
        if(pConstructConfig.contains("TORSO")) {
            out.put(ConstructPartType.TORSO, tagKeyToRenderData(pConstructConfig.getString("TORSO"), false));
        }
        if(pConstructConfig.contains("LEFT_ARM")) {
            out.put(ConstructPartType.ARM_LEFT, tagKeyToRenderData(pConstructConfig.getString("LEFT_ARM"), false));
        }
        if(pConstructConfig.contains("RIGHT_ARM")) {
            out.put(ConstructPartType.ARM_RIGHT, tagKeyToRenderData(pConstructConfig.getString("RIGHT_ARM"), false));
        }
        if(pConstructConfig.contains("LEGS")) {
            out.put(ConstructPartType.LEG_LEFT, tagKeyToRenderData(pConstructConfig.getString("LEGS"), false));
            out.put(ConstructPartType.LEG_RIGHT, tagKeyToRenderData(pConstructConfig.getString("LEGS"), true));
        }

        return out;
    }

    public static Pair<ResourceLocation, Vector3> tagKeyToRenderData(String pTag, boolean pFlipLeg) {
        final String[] parsedString = pTag.split("_");
        String outPath = "construct/";
        Vector3 outOffset = Vector3.zero();

        if(parsedString[2].equals("head")) {
            outPath += parsedString[3]+"/head_"+parsedString[1];
            outOffset = RENDER_OFFSET_HEAD;
        } else if(parsedString[2].equals("arm")) {
            boolean isLeftSide = parsedString[3].equals("left");
            outPath += parsedString[4]+"/arm_"+parsedString[1]+"_"+(isLeftSide ? "l" : "r");
            outOffset = isLeftSide ? RENDER_OFFSET_ARM_LEFT : RENDER_OFFSET_ARM_RIGHT;
        } else if(parsedString[2].equals("torso")) {
            outPath += parsedString[3]+"/torso_"+parsedString[1];
            outOffset = RENDER_OFFSET_TORSO;
        } else if(parsedString[2].equals("legs")) {
            outPath += parsedString[3]+"/leg_"+parsedString[1]+"_"+(pFlipLeg ? "r" : "l");
            outOffset = pFlipLeg ? RENDER_OFFSET_LEG_RIGHT : RENDER_OFFSET_LEG_LEFT;
        }

        ResourceLocation loc = new ResourceLocation("mna", outPath);
        return new Pair<>(loc, outOffset);
    }

    public static ResourceLocation tagKeyToEyeResourceLocation(String pTag) {
        final String[] parsedString = pTag.split("_");
        String outPath = "construct/";

        if(parsedString[2].equals("head")) {
            if(parsedString[3].equals("wickerwood"))
                return null;
            else if(parsedString[3].equals("bone"))
                outPath += "bone/eyes_angry";
            else
                outPath += "common/eyes_angry";
        }

        ResourceLocation loc = new ResourceLocation("mna", outPath);
        return loc;
    }

    public static double mappedSinusoidalAngle(long pGameTime, float pPartialTicks, double pPeriod, double pTickOffset, double pMinAngle, double pMaxAngle) {
        double loopingTime = ((pGameTime + pTickOffset + pPartialTicks) % pPeriod) / pPeriod;
        double circleTime = (Math.sin(loopingTime * Math.PI * 2) + 1) * 0.5;

        double range = pMaxAngle - pMinAngle;

        return circleTime * range + pMinAngle;
    }

    public enum ConstructPartType {
        HEAD,
        EYES,
        TORSO,
        ARM_LEFT,
        ARM_RIGHT,
        LEG_LEFT,
        LEG_RIGHT
    }
}
