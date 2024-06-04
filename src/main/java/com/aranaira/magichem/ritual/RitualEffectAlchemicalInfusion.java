package com.aranaira.magichem.ritual;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.AlchemicalInfusionRitualRecipe;
import com.aranaira.magichem.registry.EntitiesRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.items.ItemInit;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RitualEffectAlchemicalInfusion extends RitualEffect {

    private AlchemicalInfusionRitualRecipe recipe;
    private int remainingTicks = Integer.MAX_VALUE;
    private boolean isDoingWindup = true;
    private boolean isErrorState = false;

    private static final int RITUAL_LIFESPAN = 160;
    private static final Vec3i
            OFFSET_N = new Vec3i(0, 0, -3),
            OFFSET_E = new Vec3i(3, 0, 0),
            OFFSET_S = new Vec3i(0, 0, 3),
            OFFSET_W = new Vec3i(-3, 0, 0);
    private static final float
            RITUAL_VFX_HEIGHT = 2.25f;
    private static final Random r = new Random();

    public RitualEffectAlchemicalInfusion(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Nullable
    @Override
    public Component canRitualStart(IRitualContext context) {
        if (recipe == null)
            return null;

        Pair<VesselData, VesselData> vesselData = getVesselPositions(context.getLevel(), context.getCenter());
        VesselData lv = vesselData.getFirst();
        VesselData rv = vesselData.getSecond();

        if (lv.vesselBlockEntity == null || rv.vesselBlockEntity == null)
            return null;

        boolean firstVesselSufficient = false;
        if (recipe.getComponentMateria().getFirst().getItem() == lv.vesselBlockEntity.getMateriaType())
            firstVesselSufficient = lv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getFirst().getCount();
        else if (recipe.getComponentMateria().getFirst().getItem() == rv.vesselBlockEntity.getMateriaType())
            firstVesselSufficient = rv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getFirst().getCount();

        boolean secondVesselSufficient = false;
        if (recipe.getComponentMateria().getSecond().getItem() == lv.vesselBlockEntity.getMateriaType())
            secondVesselSufficient = lv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getSecond().getCount();
        else if (recipe.getComponentMateria().getSecond().getItem() == rv.vesselBlockEntity.getMateriaType())
            secondVesselSufficient = rv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getSecond().getCount();

        if (firstVesselSufficient && secondVesselSufficient) {
            return super.canRitualStart(context);
        }
        return null;
    }

    @Override
    protected boolean matchReagents(IRitualContext context) {
        Pair<VesselData, VesselData> vesselData = getVesselPositions(context.getLevel(), context.getCenter());
        VesselData lv = vesselData.getFirst();
        VesselData rv = vesselData.getSecond();

        //Materia vessels missing at one or both spots, we should inform the player
        if (lv.vesselBlockEntity == null || rv.vesselBlockEntity == null) {
            context.getCaster().sendSystemMessage(Component.translatable("feedback.ritual.alchemical_infusion.novessel"));
            isErrorState = true;
            returnReagentsToWorld(context);
            return false;
        }

        boolean firstVesselSufficient = false;
        if (recipe.getComponentMateria().getFirst().getItem() == lv.vesselBlockEntity.getMateriaType())
            firstVesselSufficient = lv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getFirst().getCount();
        else if (recipe.getComponentMateria().getFirst().getItem() == rv.vesselBlockEntity.getMateriaType())
            firstVesselSufficient = rv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getFirst().getCount();

        boolean secondVesselSufficient = false;
        if (recipe.getComponentMateria().getSecond().getItem() == lv.vesselBlockEntity.getMateriaType())
            secondVesselSufficient = lv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getSecond().getCount();
        else if (recipe.getComponentMateria().getSecond().getItem() == rv.vesselBlockEntity.getMateriaType())
            secondVesselSufficient = rv.vesselBlockEntity.getCurrentStock() >= recipe.getComponentMateria().getSecond().getCount();

        if (firstVesselSufficient && secondVesselSufficient) {
            return true;
        }

        //Vessels didn't have enough materia inside, so we should inform the player.
        int missingFirst = recipe.getComponentMateria().getFirst().getCount();
        if (recipe.getComponentMateria().getFirst().getItem() == lv.vesselBlockEntity.getMateriaType())
            missingFirst -= lv.vesselBlockEntity.getCurrentStock();
        if (recipe.getComponentMateria().getFirst().getItem() == rv.vesselBlockEntity.getMateriaType())
            missingFirst -= rv.vesselBlockEntity.getCurrentStock();

        int missingSecond = recipe.getComponentMateria().getSecond().getCount();
        if (recipe.getComponentMateria().getSecond().getItem() == lv.vesselBlockEntity.getMateriaType())
            missingSecond -= lv.vesselBlockEntity.getCurrentStock();
        if (recipe.getComponentMateria().getSecond().getItem() == rv.vesselBlockEntity.getMateriaType())
            missingSecond -= rv.vesselBlockEntity.getCurrentStock();

        MutableComponent errorMessage = Component.empty()
                .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.a"))
                .append(Component.literal(recipe.getComponentMateria().getFirst().getCount() + "").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" x "))
                .append(Component.translatable("item.magichem." + ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getFirst().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.and"))
                .append(Component.literal(recipe.getComponentMateria().getSecond().getCount() + "").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" x "))
                .append(Component.translatable("item.magichem." + ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getSecond().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(".")
                );

        if(missingFirst > 0 && missingSecond > 0) {
            errorMessage.append("\n\n")
                    .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.b"))
                    .append(Component.literal(missingFirst+"").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.more"))
                    .append(Component.translatable("item.magichem."+ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getFirst().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.and"))
                    .append(Component.literal(missingSecond+"").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.more"))
                    .append(Component.translatable("item.magichem."+ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getSecond().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(".")
                    );
        }
        else if(missingFirst > 0) {
            errorMessage.append("\n\n")
                    .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.b"))
                    .append(Component.literal(missingFirst+"").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.more"))
                    .append(Component.translatable("item.magichem."+ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getFirst().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(".")
                    );
        }
        else if(missingSecond > 0) {
            errorMessage.append("\n\n")
                    .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.b"))
                    .append(Component.literal(missingSecond+"").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.alchemical_infusion.insufficient.more"))
                    .append(Component.translatable("item.magichem."+ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getSecond().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(".")
                    );
        }

        context.getCaster().sendSystemMessage(errorMessage);

        //Refund ritual items if the player isn't in creative
        if(!context.getCaster().isCreative()) {
            returnReagentsToWorld(context);
        }

        return false;
    }

    private void returnReagentsToWorld(IRitualContext context) {
        for (ItemStack is : recipe.getIngredientItemStacks()) {
            ItemEntity ie = new ItemEntity(EntityType.ITEM, context.getLevel());
            ie.setItem(is.copy());
            ie.setPos(context.getCenter().getX() + 0.5, context.getCenter().getY() + 1, context.getCenter().getZ() + 0.5);
            context.getLevel().addFreshEntity(ie);
        }
        //Also refund PVD
        ItemEntity ie = new ItemEntity(EntityType.ITEM, context.getLevel());
        ie.setItem(new ItemStack(ItemInit.PURIFIED_VINTEUM_DUST.get()));
        ie.setPos(context.getCenter().getX() + 0.5, context.getCenter().getY() + 1, context.getCenter().getZ() + 0.5);
        context.getLevel().addFreshEntity(ie);
    }

    public Pair<VesselData, VesselData> getVesselPositions(Level pLevel, BlockPos pRitualCenter) {
        //Check north facing
        {
            BlockPos queryLeft = pRitualCenter.offset(OFFSET_W);
            BlockPos queryRight = pRitualCenter.offset(OFFSET_E);
            Block left  = pLevel.getBlockState(queryLeft.south()).getBlock();
            Block right = pLevel.getBlockState(queryLeft.north()).getBlock();
            boolean leftIsRune  = left instanceof ChalkRuneBlock;
            boolean rightIsRune = right instanceof ChalkRuneBlock;

            if((leftIsRune && !rightIsRune) || (rightIsRune && !leftIsRune)) {
                BlockEntity beLeft = pLevel.getBlockEntity(queryLeft);
                Vector3 originLeft = new Vector3(queryLeft).add(new Vector3(0.5, 0.5, 0.5));
                Vector3 tangentLeftVessel = new Vector3(0, 4, 0);
                Vector3 tangentLeftCenter = new Vector3(3, -1, -2);
                MateriaItem mTypeLeft = ((AbstractMateriaStorageBlockEntity)beLeft).getMateriaType();
                int mAmountLeft = mTypeLeft == recipe.getComponentMateria().getFirst().getItem() ?
                        recipe.getComponentMateria().getFirst().getCount() : recipe.getComponentMateria().getSecond().getCount();

                BlockEntity beRight = pLevel.getBlockEntity(queryRight);
                Vector3 originRight = new Vector3(queryRight).add(new Vector3(0.5, 0.5, 0.5));
                Vector3 tangentRightVessel = new Vector3(0, 4, 0);
                Vector3 tangentRightCenter = new Vector3(-3, -1, 2);
                MateriaItem mTypeRight = ((AbstractMateriaStorageBlockEntity)beRight).getMateriaType();
                int mAmountRight = mTypeRight == recipe.getComponentMateria().getFirst().getItem() ?
                        recipe.getComponentMateria().getFirst().getCount() : recipe.getComponentMateria().getSecond().getCount();

                return new Pair<>(
                        new VesselData((AbstractMateriaStorageBlockEntity)beLeft, originLeft, tangentLeftVessel, tangentLeftCenter, mTypeLeft, mAmountLeft),
                        new VesselData((AbstractMateriaStorageBlockEntity)beRight, originRight, tangentRightVessel, tangentRightCenter, mTypeRight, mAmountRight)
                );
            }
        }
        //Check east facing
        {
            BlockPos queryLeft = pRitualCenter.offset(OFFSET_N);
            BlockPos queryRight = pRitualCenter.offset(OFFSET_S);
            Block left  = pLevel.getBlockState(queryLeft.west()).getBlock();
            Block right = pLevel.getBlockState(queryLeft.east()).getBlock();
            boolean leftIsRune  = left instanceof ChalkRuneBlock;
            boolean rightIsRune = right instanceof ChalkRuneBlock;

            if((leftIsRune && !rightIsRune) || (rightIsRune && !leftIsRune)) {
                BlockEntity beLeft = pLevel.getBlockEntity(queryLeft);
                Vector3 originLeft = new Vector3(queryLeft).add(new Vector3(0.5, 0.5, 0.5));
                Vector3 tangentLeftVessel = new Vector3(0, 4, 0);
                Vector3 tangentLeftCenter = new Vector3(-2, -1, 3);
                MateriaItem mTypeLeft = ((AbstractMateriaStorageBlockEntity)beLeft).getMateriaType();
                int mAmountLeft = mTypeLeft == recipe.getComponentMateria().getFirst().getItem() ?
                        recipe.getComponentMateria().getFirst().getCount() : recipe.getComponentMateria().getSecond().getCount();

                BlockEntity beRight = pLevel.getBlockEntity(queryRight);
                Vector3 originRight = new Vector3(queryRight).add(new Vector3(0.5, 0.5, 0.5));
                Vector3 tangentRightVessel = new Vector3(0, 4, 0);
                Vector3 tangentRightCenter = new Vector3(2, -1, -3);
                MateriaItem mTypeRight = ((AbstractMateriaStorageBlockEntity)beRight).getMateriaType();
                int mAmountRight = mTypeRight == recipe.getComponentMateria().getFirst().getItem() ?
                        recipe.getComponentMateria().getFirst().getCount() : recipe.getComponentMateria().getSecond().getCount();

                return new Pair<>(
                        new VesselData((AbstractMateriaStorageBlockEntity)beLeft, originLeft, tangentLeftVessel, tangentLeftCenter, mTypeLeft, mAmountLeft),
                        new VesselData((AbstractMateriaStorageBlockEntity)beRight, originRight, tangentRightVessel, tangentRightCenter, mTypeRight, mAmountRight)
                );
            }
        }
        //Check south facing
        {
            BlockPos queryLeft = pRitualCenter.offset(OFFSET_E);
            BlockPos queryRight = pRitualCenter.offset(OFFSET_W);
            Block left  = pLevel.getBlockState(queryLeft.north()).getBlock();
            Block right = pLevel.getBlockState(queryLeft.south()).getBlock();
            boolean leftIsRune  = left instanceof ChalkRuneBlock;
            boolean rightIsRune = right instanceof ChalkRuneBlock;

            if((leftIsRune && !rightIsRune) || (rightIsRune && !leftIsRune)) {
                BlockEntity beLeft = pLevel.getBlockEntity(queryLeft);
                Vector3 originLeft = new Vector3(queryLeft).add(new Vector3(0.5, 0.5, 0.5));
                Vector3 tangentLeftVessel = new Vector3(0, 4, 0);
                Vector3 tangentLeftCenter = new Vector3(-3, -1, 2);
                MateriaItem mTypeLeft = ((AbstractMateriaStorageBlockEntity)beLeft).getMateriaType();
                int mAmountLeft = mTypeLeft == recipe.getComponentMateria().getFirst().getItem() ?
                        recipe.getComponentMateria().getFirst().getCount() : recipe.getComponentMateria().getSecond().getCount();

                BlockEntity beRight = pLevel.getBlockEntity(queryRight);
                Vector3 originRight = new Vector3(queryRight).add(new Vector3(0.5, 0.5, 0.5));
                Vector3 tangentRightVessel = new Vector3(0, 4, 0);
                Vector3 tangentRightCenter = new Vector3(3, -1, -2);
                MateriaItem mTypeRight = ((AbstractMateriaStorageBlockEntity)beRight).getMateriaType();
                int mAmountRight = mTypeRight == recipe.getComponentMateria().getFirst().getItem() ?
                        recipe.getComponentMateria().getFirst().getCount() : recipe.getComponentMateria().getSecond().getCount();

                return new Pair<>(
                        new VesselData((AbstractMateriaStorageBlockEntity)beLeft, originLeft, tangentLeftVessel, tangentLeftCenter, mTypeLeft, mAmountLeft),
                        new VesselData((AbstractMateriaStorageBlockEntity)beRight, originRight, tangentRightVessel, tangentRightCenter, mTypeRight, mAmountRight)
                );
            }
        }
        //Check west facing
        {
            BlockPos queryLeft = pRitualCenter.offset(OFFSET_S);
            BlockPos queryRight = pRitualCenter.offset(OFFSET_N);
            Block left  = pLevel.getBlockState(queryLeft.east()).getBlock();
            Block right = pLevel.getBlockState(queryLeft.west()).getBlock();
            boolean leftIsRune  = left instanceof ChalkRuneBlock;
            boolean rightIsRune = right instanceof ChalkRuneBlock;

            if((leftIsRune && !rightIsRune) || (rightIsRune && !leftIsRune)) {
                BlockEntity beLeft = pLevel.getBlockEntity(queryLeft);
                Vector3 originLeft = new Vector3(queryLeft).add(new Vector3(0.5, 0.5, 0.5));
                Vector3 tangentLeftVessel = new Vector3(0, 4, 0);
                Vector3 tangentLeftCenter = new Vector3(2, -1, -3);
                MateriaItem mTypeLeft = ((AbstractMateriaStorageBlockEntity)beLeft).getMateriaType();
                int mAmountLeft = mTypeLeft == recipe.getComponentMateria().getFirst().getItem() ?
                        recipe.getComponentMateria().getFirst().getCount() : recipe.getComponentMateria().getSecond().getCount();

                BlockEntity beRight = pLevel.getBlockEntity(queryRight);
                Vector3 originRight = new Vector3(queryRight).add(new Vector3(0.5, 0.5, 0.5));
                Vector3 tangentRightVessel = new Vector3(0, 4, 0);
                Vector3 tangentRightCenter = new Vector3(-2, -1, 3);
                MateriaItem mTypeRight = ((AbstractMateriaStorageBlockEntity)beRight).getMateriaType();
                int mAmountRight = mTypeRight == recipe.getComponentMateria().getFirst().getItem() ?
                        recipe.getComponentMateria().getFirst().getCount() : recipe.getComponentMateria().getSecond().getCount();

                return new Pair<>(
                        new VesselData((AbstractMateriaStorageBlockEntity)beLeft, originLeft, tangentLeftVessel, tangentLeftCenter, mTypeLeft, mAmountLeft),
                        new VesselData((AbstractMateriaStorageBlockEntity)beRight, originRight, tangentRightVessel, tangentRightCenter, mTypeRight, mAmountRight)
                );
            }
        }
        return new Pair<>(null, null);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        Pair<VesselData, VesselData> vesselData = getVesselPositions(context.getLevel(), context.getCenter());
        VesselData lv = vesselData.getFirst();
        VesselData rv = vesselData.getSecond();

        //Deduct materia from vessels
        if(recipe.getComponentMateria().getFirst().getItem() == lv.vesselBlockEntity.getMateriaType())
            lv.vesselBlockEntity.extractMateria(recipe.getComponentMateria().getFirst().getCount());
        else if(recipe.getComponentMateria().getFirst().getItem() == rv.vesselBlockEntity.getMateriaType())
            rv.vesselBlockEntity.extractMateria(recipe.getComponentMateria().getFirst().getCount());

        if(recipe.getComponentMateria().getSecond().getItem() == lv.vesselBlockEntity.getMateriaType())
            lv.vesselBlockEntity.extractMateria(recipe.getComponentMateria().getSecond().getCount());
        else if(recipe.getComponentMateria().getSecond().getItem() == rv.vesselBlockEntity.getMateriaType())
            rv.vesselBlockEntity.extractMateria(recipe.getComponentMateria().getSecond().getCount());

        //Set remaining ticks until the big ending kaboom
        remainingTicks = RITUAL_LIFESPAN - 2;
        isDoingWindup = false;

        //Queue up the shlorps
        DelayedEventQueue.pushEvent(context.getLevel(),
                new TimedDelayedEvent<DelayDataShlorps>("alchemical_infusion_ritual_shlorps", 60,
                        new DelayDataShlorps(context, vesselData),
                        this::createShlorps
                        )
                );

        //Queue up the item craft
        Vec3 itemPos = new Vec3(context.getCenter().getX()+0.5, context.getCenter().getY()+RITUAL_VFX_HEIGHT, context.getCenter().getZ()+0.5);
        DelayedEventQueue.pushEvent(context.getLevel(),
                new TimedDelayedEvent<DelayDataOutputItem>("alchemical_infusion_ritual_output", RITUAL_LIFESPAN + 160,
                        new DelayDataOutputItem(context.getLevel(), itemPos, recipe.getAlchemyObject().copy()),
                        this::createOutputItem
                        )
                );

        return true;
    }

    private void createOutputItem(String identifier, DelayDataOutputItem data) {
        ItemEntity ie = new ItemEntity(EntityType.ITEM, data.level);
        ie.setItem(data.itemToCreate);
        ie.setPos(data.creationPos);
        ie.setDeltaMovement(0, 0.375, 0);
        data.level.addFreshEntity(ie);
    }

    private void createShlorps(String identifier, DelayDataShlorps data) {
        createShlorps(data.context, data.vesselData);
    }

    private void createShlorps(IRitualContext pContext, Pair<VesselData, VesselData> pVesselData) {
        VesselData lv = pVesselData.getFirst();
        VesselData rv = pVesselData.getSecond();
        Vector3 centerPos = new Vector3(pContext.getCenter()).add(new Vector3(0.5, RITUAL_VFX_HEIGHT, 0.5));

        ShlorpEntity shlorpLeft = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), pContext.getLevel());
        shlorpLeft.setPos(new Vec3(lv.origin.x, lv.origin.y, lv.origin.z));
        shlorpLeft.configure(
                lv.origin, Vector3.zero(), lv.tangentVessel,
                centerPos, Vector3.zero(), lv.tangentCenter,
                0.0175f, 0.0625f, 2 + Math.min(20, recipe.getComponentMateria().getFirst().getCount() / 2),
                pVesselData.getFirst().type, pVesselData.getSecond().amount);
        pContext.getLevel().addFreshEntity(shlorpLeft);

        ShlorpEntity shlorpRight = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), pContext.getLevel());
        shlorpRight.setPos(new Vec3(rv.origin.x, rv.origin.y, rv.origin.z));
        shlorpRight.configure(
                rv.origin, Vector3.zero(), rv.tangentVessel,
                centerPos, Vector3.zero(), rv.tangentCenter,
                0.0175f, 0.0625f, 2 + Math.min(18, recipe.getComponentMateria().getSecond().getCount() / 2),
                pVesselData.getSecond().type, pVesselData.getSecond().amount);
        pContext.getLevel().addFreshEntity(shlorpRight);
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        Vec3 center = new Vec3(context.getCenter().getX() + 0.5D, context.getCenter().getY() + RITUAL_VFX_HEIGHT, context.getCenter().getZ() + 0.5D);

        //putter about for a bit
        if(isErrorState) {
            Pair<VesselData, VesselData> vesselData = getVesselPositions(context.getLevel(), context.getCenter());
            VesselData lv = vesselData.getFirst();
            VesselData rv = vesselData.getSecond();

            //left side
            if(lv.vesselBlockEntity == null) {
                context.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                        .setColor(128, 10, 10).setScale(1.5f),
                        lv.origin.x, lv.origin.y, lv.origin.z,
                        0, 0, 0);
            }
            //right side
            if(rv.vesselBlockEntity == null) {
                context.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                        .setColor(128, 10, 10).setScale(1.5f),
                        rv.origin.x, rv.origin.y, rv.origin.z,
                        0, 0, 0);
            }
        }
        else if(isDoingWindup) {

        }
        //build up the visuals
        else if(remainingTicks > 0) {
            //center orb
            float centerScale = (float)(RITUAL_LIFESPAN - remainingTicks) / (float)RITUAL_LIFESPAN;
            context.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                    .setColor(22, 94, 69).setScale(0.2f + 2.2f * centerScale),
                    center.x, center.y, center.z,
                    0, 0, 0);

            //sparks
            if(remainingTicks % 10 == 0) {
                for(int i=0; i<r.nextInt(7); i++) {
                    Vec3 vector = new Vec3(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, r.nextFloat() - 0.5f).normalize();
                    final float speed = 0.08f;
                    context.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                    .setColor(36, 151, 110).setScale(0.2f).setGravity(0.02f).setMaxAge(30).setPhysics(true),
                            center.x, center.y, center.z,
                            vector.x * speed, vector.y * speed + (r.nextFloat() * 0.125f), vector.z * speed);
                }
            }

            remainingTicks--;
        }
        //kaboom!
        else {
            for(int i=0; i<40; i++) {
                Vec3 vector = new Vec3(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, r.nextFloat() - 0.5f).normalize();
                float speed = 0.09f;

                //big seafoam
                context.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setColor(36, 151, 110).setScale(0.4f).setGravity(0.01f).setMaxAge(35).setPhysics(true),
                        center.x, center.y, center.z,
                        vector.x * speed, vector.y * speed + (r.nextFloat() * 0.125f), vector.z * speed);

                //small white
                speed = 0.12f;
                context.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setColor(255, 255, 255).setScale(0.15f).setGravity(0.005f).setMaxAge(35).setPhysics(true),
                        center.x, center.y, center.z,
                        vector.x * speed, vector.y * speed + (r.nextFloat() * 0.125f), vector.z * speed);
            }
        }
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext iRitualContext) {
        return RITUAL_LIFESPAN;
    }

    @Override
    protected boolean modifyRitualReagentsAndPatterns(ItemStack dataStack, IRitualContext context) {
        CompoundTag nbt = dataStack.getOrCreateTag();
        if(!nbt.contains("recipe"))
            return false;

        int index = nbt.getInt("recipe");
        ItemStack query = AlchemicalInfusionRitualRecipe.getAllOutputs().get(index);
        recipe = AlchemicalInfusionRitualRecipe.getInfusionRitualRecipe(context.getLevel(), query);

        NonNullList<ResourceLocation> locations = NonNullList.create();
        for(ItemStack is : recipe.getIngredientItemStacks()) {
            locations.add(ForgeRegistries.ITEMS.getKey(is.getItem()));
        }

        context.replaceReagents(new ResourceLocation(MagiChemMod.MODID, "dynamic_infusion"), locations);
        remainingTicks = Integer.MAX_VALUE;
        isDoingWindup = true;
        isErrorState = false;

        return true;
    }

    private class VesselData {
        public final AbstractMateriaStorageBlockEntity vesselBlockEntity;
        public final Vector3 origin, tangentVessel, tangentCenter;
        public final MateriaItem type;
        public final int amount;

        public VesselData(AbstractMateriaStorageBlockEntity pEntity, Vector3 pOrigin, Vector3 pTangentVessel, Vector3 pTangentCenter, MateriaItem pType, int pAmount) {
            this.vesselBlockEntity = pEntity;
            this.origin = pOrigin;
            this.tangentVessel = pTangentVessel;
            this.tangentCenter = pTangentCenter;
            this.type = pType;
            this.amount = pAmount;
        }
    }

    private class DelayDataOutputItem {
        Level level;
        Vec3 creationPos;
        ItemStack itemToCreate;

        public DelayDataOutputItem(Level pLevel, Vec3 pCreationPos, ItemStack pItemToCreate) {
            this.level = pLevel;
            this.creationPos = pCreationPos;
            this.itemToCreate = pItemToCreate;
        }
    }

    private class DelayDataShlorps {
        IRitualContext context;
        Pair<VesselData, VesselData> vesselData;

        public DelayDataShlorps(IRitualContext pContext, Pair<VesselData, VesselData> pVesselData) {
            this.context = pContext;
            this.vesselData = pVesselData;
        }
    }
}
