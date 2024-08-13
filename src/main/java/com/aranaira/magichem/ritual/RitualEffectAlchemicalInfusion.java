package com.aranaira.magichem.ritual;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.entities.InfusionRitualVFXEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.foundation.VesselData;
import com.aranaira.magichem.foundation.enums.ShlorpParticleMode;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.SublimationRitualRecipe;
import com.aranaira.magichem.registry.EntitiesRegistry;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RitualEffectAlchemicalInfusion extends RitualEffect {

    private SublimationRitualRecipe recipe;

    public static final int RITUAL_LIFESPAN = 160;
    private static final Vec3i
            OFFSET_N = new Vec3i(0, 0, -3),
            OFFSET_E = new Vec3i(3, 0, 0),
            OFFSET_S = new Vec3i(0, 0, 3),
            OFFSET_W = new Vec3i(-3, 0, 0);
    public static final float
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

        Pair<VesselData, VesselData> vesselData = getVesselPositions(context.getLevel(), context.getCenter(), recipe);
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
        Pair<VesselData, VesselData> vesselData = getVesselPositions(context.getLevel(), context.getCenter(), recipe);
        VesselData lv = vesselData.getFirst();
        VesselData rv = vesselData.getSecond();

        InfusionRitualVFXEntity irve = new InfusionRitualVFXEntity(EntitiesRegistry.INFUSION_RITUAL_VFX_ENTITY.get(), context.getLevel());
        irve.configure(context.getCenter(), recipe);
        irve.setPos(context.getCenter().getX() + 0.5, context.getCenter().getY(), context.getCenter().getZ() + 0.5);
        context.getLevel().addFreshEntity(irve);

        //Materia vessels missing at one or both spots, we should inform the player
        if (lv.vesselBlockEntity == null || rv.vesselBlockEntity == null) {
            context.getCaster().sendSystemMessage(Component.translatable("feedback.ritual.sublimation.novessel"));

            if(!context.getCaster().isCreative())
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
                .append(Component.translatable("feedback.ritual.sublimation.insufficient.a"))
                .append(Component.literal(recipe.getComponentMateria().getFirst().getCount() + "").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" x "))
                .append(Component.translatable("item.magichem." + ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getFirst().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("feedback.ritual.sublimation.insufficient.and"))
                .append(Component.literal(recipe.getComponentMateria().getSecond().getCount() + "").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" x "))
                .append(Component.translatable("item.magichem." + ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getSecond().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(".")
                );

        if(missingFirst > 0 && missingSecond > 0) {
            errorMessage.append("\n\n")
                    .append(Component.translatable("feedback.ritual.sublimation.insufficient.b"))
                    .append(Component.literal(missingFirst+"").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.sublimation.insufficient.more"))
                    .append(Component.translatable("item.magichem."+ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getFirst().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.sublimation.insufficient.and"))
                    .append(Component.literal(missingSecond+"").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.sublimation.insufficient.more"))
                    .append(Component.translatable("item.magichem."+ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getSecond().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(".")
                    );
        }
        else if(missingFirst > 0) {
            errorMessage.append("\n\n")
                    .append(Component.translatable("feedback.ritual.sublimation.insufficient.b"))
                    .append(Component.literal(missingFirst+"").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.sublimation.insufficient.more"))
                    .append(Component.translatable("item.magichem."+ForgeRegistries.ITEMS.getKey(recipe.getComponentMateria().getFirst().getItem()).getPath()).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(".")
                    );
        }
        else if(missingSecond > 0) {
            errorMessage.append("\n\n")
                    .append(Component.translatable("feedback.ritual.sublimation.insufficient.b"))
                    .append(Component.literal(missingSecond+"").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("feedback.ritual.sublimation.insufficient.more"))
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

    public static Pair<VesselData, VesselData> getVesselPositions(Level pLevel, BlockPos pRitualCenter, SublimationRitualRecipe pRecipe) {
        Vector3 originLeft = Vector3.zero();
        Vector3 tangentLeftVessel = Vector3.zero();
        Vector3 tangentLeftCenter = Vector3.zero();
        Vector3 originRight = Vector3.zero();
        Vector3 tangentRightVessel = Vector3.zero();
        Vector3 tangentRightCenter = Vector3.zero();
        int mAmountLeft = 0;
        int mAmountRight = 0;
        MateriaItem mTypeLeft = null;
        MateriaItem mTypeRight = null;

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
                originLeft = new Vector3(queryLeft).add(new Vector3(0.5, 0.5, 0.5));
                if(beLeft != null) {
                    tangentLeftVessel = new Vector3(0, 4, 0);
                    tangentLeftCenter = new Vector3(3, -1, -2);
                    mTypeLeft = ((AbstractMateriaStorageBlockEntity)beLeft).getMateriaType();
                    mAmountLeft = mTypeLeft == pRecipe.getComponentMateria().getFirst().getItem() ?
                            pRecipe.getComponentMateria().getFirst().getCount() : pRecipe.getComponentMateria().getSecond().getCount();
                }

                BlockEntity beRight = pLevel.getBlockEntity(queryRight);
                originRight = new Vector3(queryRight).add(new Vector3(0.5, 0.5, 0.5));
                if(beRight != null) {
                    tangentRightVessel = new Vector3(0, 4, 0);
                    tangentRightCenter = new Vector3(-3, -1, 2);
                    mTypeRight = ((AbstractMateriaStorageBlockEntity) beRight).getMateriaType();
                    mAmountRight = mTypeRight == pRecipe.getComponentMateria().getFirst().getItem() ?
                            pRecipe.getComponentMateria().getFirst().getCount() : pRecipe.getComponentMateria().getSecond().getCount();
                }

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
                originLeft = new Vector3(queryLeft).add(new Vector3(0.5, 0.5, 0.5));
                if(beLeft != null) {
                    tangentLeftVessel = new Vector3(0, 4, 0);
                    tangentLeftCenter = new Vector3(-2, -1, 3);
                    mTypeLeft = ((AbstractMateriaStorageBlockEntity) beLeft).getMateriaType();
                    mAmountLeft = mTypeLeft == pRecipe.getComponentMateria().getFirst().getItem() ?
                            pRecipe.getComponentMateria().getFirst().getCount() : pRecipe.getComponentMateria().getSecond().getCount();
                }

                BlockEntity beRight = pLevel.getBlockEntity(queryRight);
                originRight = new Vector3(queryRight).add(new Vector3(0.5, 0.5, 0.5));
                if(beRight != null) {
                    tangentRightVessel = new Vector3(0, 4, 0);
                    tangentRightCenter = new Vector3(2, -1, -3);
                    mTypeRight = ((AbstractMateriaStorageBlockEntity) beRight).getMateriaType();
                    mAmountRight = mTypeRight == pRecipe.getComponentMateria().getFirst().getItem() ?
                            pRecipe.getComponentMateria().getFirst().getCount() : pRecipe.getComponentMateria().getSecond().getCount();
                }

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
                originLeft = new Vector3(queryLeft).add(new Vector3(0.5, 0.5, 0.5));
                if(beLeft != null) {
                    tangentLeftVessel = new Vector3(0, 4, 0);
                    tangentLeftCenter = new Vector3(-3, -1, 2);
                    mTypeLeft = ((AbstractMateriaStorageBlockEntity) beLeft).getMateriaType();
                    mAmountLeft = mTypeLeft == pRecipe.getComponentMateria().getFirst().getItem() ?
                            pRecipe.getComponentMateria().getFirst().getCount() : pRecipe.getComponentMateria().getSecond().getCount();
                }

                BlockEntity beRight = pLevel.getBlockEntity(queryRight);
                originRight = new Vector3(queryRight).add(new Vector3(0.5, 0.5, 0.5));
                if(beRight != null) {
                    tangentRightVessel = new Vector3(0, 4, 0);
                    tangentRightCenter = new Vector3(3, -1, -2);
                    mTypeRight = ((AbstractMateriaStorageBlockEntity) beRight).getMateriaType();
                    mAmountRight = mTypeRight == pRecipe.getComponentMateria().getFirst().getItem() ?
                            pRecipe.getComponentMateria().getFirst().getCount() : pRecipe.getComponentMateria().getSecond().getCount();
                }

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
                originLeft = new Vector3(queryLeft).add(new Vector3(0.5, 0.5, 0.5));
                if(beLeft != null) {
                    tangentLeftVessel = new Vector3(0, 4, 0);
                    tangentLeftCenter = new Vector3(2, -1, -3);
                    mTypeLeft = ((AbstractMateriaStorageBlockEntity) beLeft).getMateriaType();
                    mAmountLeft = mTypeLeft == pRecipe.getComponentMateria().getFirst().getItem() ?
                            pRecipe.getComponentMateria().getFirst().getCount() : pRecipe.getComponentMateria().getSecond().getCount();
                }

                BlockEntity beRight = pLevel.getBlockEntity(queryRight);
                originRight = new Vector3(queryRight).add(new Vector3(0.5, 0.5, 0.5));
                if(beRight != null) {
                    tangentRightVessel = new Vector3(0, 4, 0);
                    tangentRightCenter = new Vector3(-2, -1, 3);
                    mTypeRight = ((AbstractMateriaStorageBlockEntity) beRight).getMateriaType();
                    mAmountRight = mTypeRight == pRecipe.getComponentMateria().getFirst().getItem() ?
                            pRecipe.getComponentMateria().getFirst().getCount() : pRecipe.getComponentMateria().getSecond().getCount();
                }

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
        Pair<VesselData, VesselData> vesselData = getVesselPositions(context.getLevel(), context.getCenter(), recipe);
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

        //Queue up the shlorps
        DelayedEventQueue.pushEvent(context.getLevel(),
                new TimedDelayedEvent<DelayDataShlorps>("sublimation_ritual_shlorps", 60,
                        new DelayDataShlorps(context, vesselData),
                        this::createShlorps
                        )
                );

        //Queue up the item craft
        Vec3 itemPos = new Vec3(context.getCenter().getX()+0.5, context.getCenter().getY()+RITUAL_VFX_HEIGHT, context.getCenter().getZ()+0.5);
        DelayedEventQueue.pushEvent(context.getLevel(),
                new TimedDelayedEvent<DelayDataOutputItem>("sublimation_ritual_output", RITUAL_LIFESPAN + 160,
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
        boolean isFirstVessel;

        ShlorpEntity shlorpLeft = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), pContext.getLevel());
        isFirstVessel = lv.vesselBlockEntity.getMateriaType() == pVesselData.getFirst().type;
        shlorpLeft.setPos(new Vec3(lv.origin.x, lv.origin.y, lv.origin.z));
        shlorpLeft.configure(
                lv.origin, Vector3.zero(), lv.tangentVessel,
                centerPos, Vector3.zero(), lv.tangentCenter,
                0.035f, 0.03125f,
                2 + Math.min(40, isFirstVessel ? pVesselData.getFirst().amount : pVesselData.getSecond().amount),
                isFirstVessel ? pVesselData.getFirst().type : pVesselData.getSecond().type,
                isFirstVessel ? pVesselData.getFirst().amount : pVesselData.getSecond().amount,
                ShlorpParticleMode.INVERSE_ENTRY_TANGENT);
        pContext.getLevel().addFreshEntity(shlorpLeft);

        ShlorpEntity shlorpRight = new ShlorpEntity(EntitiesRegistry.SHLORP_ENTITY.get(), pContext.getLevel());
        isFirstVessel = rv.vesselBlockEntity.getMateriaType() == pVesselData.getFirst().type;
        shlorpRight.setPos(new Vec3(rv.origin.x, rv.origin.y, rv.origin.z));
        shlorpRight.configure(
                rv.origin, Vector3.zero(), rv.tangentVessel,
                centerPos, Vector3.zero(), rv.tangentCenter,
                0.035f, 0.03125f,
                2 + Math.min(40, isFirstVessel ? pVesselData.getFirst().amount : pVesselData.getSecond().amount),
                pVesselData.getSecond().type, pVesselData.getSecond().amount,
                ShlorpParticleMode.INVERSE_ENTRY_TANGENT);
        pContext.getLevel().addFreshEntity(shlorpRight);
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
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

        String key = nbt.getString("recipe");
        Item query = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
        if(query == null)
            return false;

        recipe = SublimationRitualRecipe.getInfusionRitualRecipe(context.getLevel(), new ItemStack(query));
        if(recipe == null)
            return false;

        NonNullList<ResourceLocation> locations = NonNullList.create();
        for(ItemStack is : recipe.getIngredientItemStacks()) {
            locations.add(ForgeRegistries.ITEMS.getKey(is.getItem()));
        }

        context.replaceReagents(new ResourceLocation(MagiChemMod.MODID, "dynamic_infusion"), locations);

        return true;
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
