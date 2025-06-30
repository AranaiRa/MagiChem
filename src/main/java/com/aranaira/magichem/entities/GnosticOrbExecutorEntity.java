package com.aranaira.magichem.entities;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.Quadlet;
import com.aranaira.magichem.foundation.Triplet;
import com.machinezoo.noexception.throwing.ThrowingRunnable;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class GnosticOrbExecutorEntity extends Entity implements IEntityAdditionalSpawnData {
    //First int is the activation tick modulus
    //Second int is the iterator limit
    //Third ThrowingRunnable is the function that precaches the correct arraylist
    //Fourth ThrowingRunnable is the function that executes on tick
    private static final HashMap<String, Quadlet<Integer, Integer, ThrowingRunnable, ThrowingRunnable>> PROPHECY_DATA = new HashMap<>();
    private static final Random r = new Random();

    public static final TagKey<Block> TAG_CAKES = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "cakes"));
    public static final TagKey<Block> TAG_TABLES = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "tables"));

    private ArrayList<BlockPos> validBlockTargets = new ArrayList<>();
    private ArrayList<BlockState> validBlockStates = new ArrayList<>();
    private ArrayList<LivingEntity> validEntityTargets = new ArrayList<>();

    private String materiaType = "";
    private int materiaColor = 0, iterator = 0, iteratorLimit = 0, activationTickModulus = 0;
    private Boolean hasPreCached = false;

    public GnosticOrbExecutorEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        if(PROPHECY_DATA.size() == 0) {
            PROPHECY_DATA.put("creature",  new Quadlet<>(4, 20, this::preCacheCreature, this::prophecyEffectCreature));
            PROPHECY_DATA.put("delight",   new Quadlet<>(4, 40, this::preCacheDelight, this::prophecyEffectDelight));
            PROPHECY_DATA.put("disaster",  new Quadlet<>(20, 60, this::preCacheDisaster, this::prophecyEffectDisaster));
            PROPHECY_DATA.put("exanimate", new Quadlet<>(4, 20, this::preCacheExanimate, this::prophecyEffectExanimate));
            PROPHECY_DATA.put("metal",     new Quadlet<>(3, 120, this::preCacheMetal, this::prophecyEffectMetal));
            PROPHECY_DATA.put("thought",   new Quadlet<>(1, 1, this::preCacheThought, this::prophecyEffectThought));
        }
    }

    public void configure(String pMateriaType, int pMateriaColor) {
        materiaType = pMateriaType;
        materiaColor = pMateriaColor;
        activationTickModulus = PROPHECY_DATA.get(materiaType).getFirst();
        iteratorLimit = PROPHECY_DATA.get(materiaType).getSecond();

        int a = 0;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        materiaType = pCompound.getString("materiaType");
        materiaColor = pCompound.getInt("materiaColor");
        iterator = pCompound.getInt("iterator");
        iteratorLimit = pCompound.getInt("iteratorLimit");
        activationTickModulus = pCompound.getInt("activationTickModulus");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putString("materiaType",materiaType);
        pCompound.putInt("materiaColor",materiaColor);
        pCompound.putInt("iterator",iterator);
        pCompound.putInt("iteratorLimit",iteratorLimit);
        pCompound.putInt("activationTickModulus",activationTickModulus);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
    }

    @Override
    public void tick() {
        super.tick();

        if(!level().isClientSide()) {
            if(materiaType.equals(""))
                kill();

            final Quadlet<Integer, Integer, ThrowingRunnable, ThrowingRunnable> data = PROPHECY_DATA.get(materiaType);

            if(hasPreCached) {
                if (level().getGameTime() % data.getFirst() == 0) {
                    try {
                        data.getFourth().run();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    data.getThird().run();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            if(iterator >= iteratorLimit)
                kill();
        }
    }

    /////////////////////
    // PROPHECY EFFECTS
    /////////////////////

    public void preCacheCreature() {
        hasPreCached = true;
    }

    public void prophecyEffectCreature() {
        iterator = iteratorLimit;
    }

    public void preCacheDelight() {
        validBlockTargets.clear();
        validBlockStates.clear();

        //Precalculate cake destinations
        int range = 20;
        for(int y = blockPosition().getY()-(range/2); y<=blockPosition().getX()+(range/2); y++) {
            for (int x = blockPosition().getX()-range; x<=blockPosition().getX()+range; x++) {
                for (int z = blockPosition().getZ()-range; z<=blockPosition().getZ()+range; z++) {
                    BlockPos posQuery = new BlockPos(x, y, z);
                    BlockState stateQuery = level().getBlockState(posQuery);

                    boolean isTable = stateQuery.is(TAG_TABLES);
                    boolean hasOpenSpace = level().getBlockState(posQuery.above()).isAir();

                    if(isTable && hasOpenSpace) {
                        validBlockTargets.add(posQuery.above());
                    }
                }
            }
        }
        Collections.shuffle(validBlockTargets);
        iteratorLimit = Math.min(validBlockTargets.size(), iteratorLimit);

        //Precalculate list of potential cakes
        for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(TAG_CAKES)) {
            validBlockStates.add(blockHolder.value().defaultBlockState());
        }

        hasPreCached = true;
    }

    public void prophecyEffectDelight() {
        BlockPos posQuery = validBlockTargets.get(iterator);
        BlockState stateQuery;
        if(validBlockStates.size() == 1)
            stateQuery = validBlockStates.get(0);
        else
            stateQuery = validBlockStates.get(r.nextInt(validBlockStates.size()));

        level().setBlock(posQuery, stateQuery, 3);

        iterator++;
    }

    public void preCacheDisaster() {
        hasPreCached = true;
    }

    public void prophecyEffectDisaster() {
        iterator = iteratorLimit;
    }

    public void preCacheExanimate() {
        hasPreCached = true;
    }

    public void prophecyEffectExanimate() {
        iterator = iteratorLimit;
    }

    public void preCacheMetal() {
        hasPreCached = true;
    }

    public void prophecyEffectMetal() {
        iterator = iteratorLimit;
    }

    public void preCacheThought() {
        hasPreCached = true;
    }

    public void prophecyEffectThought() {
        iterator = iteratorLimit;
    }
}
