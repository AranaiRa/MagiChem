package com.aranaira.magichem.entities;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.Quadlet;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.networking.ParticleSpawnAnointingS2CPacket;
import com.aranaira.magichem.registry.ItemRegistry;
import com.machinezoo.noexception.throwing.ThrowingConsumer;
import com.machinezoo.noexception.throwing.ThrowingRunnable;
import com.mna.tools.math.Vector3;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
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
    private static final HashMap<String, Quadlet<Integer, Integer, ThrowingConsumer<GnosticOrbExecutorEntity>, ThrowingConsumer<GnosticOrbExecutorEntity>>> PROPHECY_DATA = new HashMap<>();
    private static final Random r = new Random();

    public static final TagKey<Block> TAG_BOOKSHELVES = BlockTags.create(new ResourceLocation("forge", "bookshelves"));
    public static final TagKey<Block> TAG_CAKES = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "cakes"));
    public static final TagKey<Block> TAG_ORES = BlockTags.create(new ResourceLocation("forge", "ores_in_ground/stone"));
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
//            PROPHECY_DATA.put("creature",  new Quadlet<>(4, 20, GnosticOrbExecutorEntity::preCacheCreature, GnosticOrbExecutorEntity::prophecyEffectCreature));
            PROPHECY_DATA.put("delight",   new Quadlet<>(7, 40, GnosticOrbExecutorEntity::preCacheDelight, GnosticOrbExecutorEntity::prophecyEffectDelight));
            PROPHECY_DATA.put("disaster",  new Quadlet<>(4, 75, GnosticOrbExecutorEntity::preCacheDisaster, GnosticOrbExecutorEntity::prophecyEffectDisaster));
//            PROPHECY_DATA.put("exanimate", new Quadlet<>(4, 20, GnosticOrbExecutorEntity::preCacheExanimate, GnosticOrbExecutorEntity::prophecyEffectExanimate));
            PROPHECY_DATA.put("metal",     new Quadlet<>(3, 120, GnosticOrbExecutorEntity::preCacheMetal, GnosticOrbExecutorEntity::prophecyEffectMetal));
            PROPHECY_DATA.put("thought",   new Quadlet<>(1, 1, GnosticOrbExecutorEntity::preCacheThought, GnosticOrbExecutorEntity::prophecyEffectThought));
        }
    }

    public void configure(String pMateriaType, int pMateriaColor) {
        materiaType = pMateriaType;
        materiaColor = pMateriaColor;
        activationTickModulus = PROPHECY_DATA.get(materiaType).getFirst();
        iteratorLimit = PROPHECY_DATA.get(materiaType).getSecond();
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

            final Quadlet<Integer, Integer, ThrowingConsumer<GnosticOrbExecutorEntity>, ThrowingConsumer<GnosticOrbExecutorEntity>> data = PROPHECY_DATA.get(materiaType);

            if(hasPreCached) {
                if (level().getGameTime() % data.getFirst() == 0) {
                    try {
                        data.getFourth().accept(this);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    data.getThird().accept(this);
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

    public static void preCacheCreature(GnosticOrbExecutorEntity pEntity) {
        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectCreature(GnosticOrbExecutorEntity pEntity) {
        pEntity.iterator = pEntity.iteratorLimit;
    }

    public static void preCacheDelight(GnosticOrbExecutorEntity pEntity) {
        //Precalculate cake destinations
        int range = 20;
        for(int y = pEntity.blockPosition().getY()-(range/2); y<=pEntity.blockPosition().getX()+(range/2); y++) {
            for (int x = pEntity.blockPosition().getX()-range; x<=pEntity.blockPosition().getX()+range; x++) {
                for (int z = pEntity.blockPosition().getZ()-range; z<=pEntity.blockPosition().getZ()+range; z++) {
                    BlockPos posQuery = new BlockPos(x, y, z);
                    BlockState stateQuery = pEntity.level().getBlockState(posQuery);

                    boolean isTable = stateQuery.is(TAG_TABLES);
                    boolean hasOpenSpace = pEntity.level().getBlockState(posQuery.above()).isAir();

                    if(isTable && hasOpenSpace) {
                        pEntity.validBlockTargets.add(posQuery.above());
                    }
                }
            }
        }
        Collections.shuffle(pEntity.validBlockTargets);
        pEntity.iteratorLimit = Math.min(pEntity.validBlockTargets.size(), pEntity.iteratorLimit);

        //Precalculate list of potential cakes
        for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(TAG_CAKES)) {
            pEntity.validBlockStates.add(blockHolder.value().defaultBlockState());
        }

        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectDelight(GnosticOrbExecutorEntity pEntity) {
        BlockPos posQuery = pEntity.validBlockTargets.get(pEntity.iterator);
        BlockState stateQuery;
        if(pEntity.validBlockStates.size() == 1)
            stateQuery = pEntity.validBlockStates.get(0);
        else
            stateQuery = pEntity.validBlockStates.get(r.nextInt(pEntity.validBlockStates.size()));

        pEntity.level().setBlock(posQuery, stateQuery, 3);
        MagiChemMod.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(posQuery.getX(), posQuery.getY(), posQuery.getZ(), 20f, pEntity.level().dimension())),
                new ParticleSpawnAnointingS2CPacket(posQuery.getX(), posQuery.getY(), posQuery.getZ(), pEntity.materiaColor, true));

        pEntity.iterator++;
    }

    public static void preCacheDisaster(GnosticOrbExecutorEntity pEntity) {
        //Precalculate explosion locations
        int maxRange = 30;
        for(int i=0;i< pEntity.iteratorLimit;i++) {
            for(int j=0;j<3;j++) {
                float distPercent = r.nextFloat();
                distPercent = (1 - (distPercent * distPercent * distPercent)) * 0.8f + 0.2f;

                Vector3 pos = new Vector3(r.nextDouble() - 0.5, r.nextDouble() - 0.5, r.nextDouble() - 0.5).normalize().scale(distPercent * maxRange);
                BlockPos target = pEntity.blockPosition().offset(Math.round(pos.x), Math.round(pos.y), Math.round(pos.z));
                if(!pEntity.level().getBlockState(target).isAir() || j==2) {
                    pEntity.validBlockTargets.add(target);
                    break;
                }
            }
        }

        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectDisaster(GnosticOrbExecutorEntity pEntity) {
        BlockPos pos = pEntity.validBlockTargets.get(pEntity.iterator);

        pEntity.level().explode(null, pos.getX(), pos.getY(), pos.getZ(), r.nextInt(12) + 8, true, Level.ExplosionInteraction.BLOCK);

        pEntity.iterator++;
    }

    public static void preCacheExanimate(GnosticOrbExecutorEntity pEntity) {
        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectExanimate(GnosticOrbExecutorEntity pEntity) {
        pEntity.iterator = pEntity.iteratorLimit;
    }

    public static void preCacheMetal(GnosticOrbExecutorEntity pEntity) {
        //Precalculate places that could be ore
        int range = 10;
        for(int y = pEntity.blockPosition().getY()-(range/2); y<=pEntity.blockPosition().getX()+(range/2); y++) {
            for (int x = pEntity.blockPosition().getX()-range; x<=pEntity.blockPosition().getX()+range; x++) {
                for (int z = pEntity.blockPosition().getZ()-range; z<=pEntity.blockPosition().getZ()+range; z++) {
                    BlockPos posQuery = new BlockPos(x, y, z);
                    BlockState stateQuery = pEntity.level().getBlockState(posQuery);

                    boolean isStoneOreReplaceable = stateQuery.is(BlockTags.STONE_ORE_REPLACEABLES);

                    if(isStoneOreReplaceable) {
                        pEntity.validBlockTargets.add(posQuery.above());
                    }
                }
            }
        }
        Collections.shuffle(pEntity.validBlockTargets);
        pEntity.iteratorLimit = Math.min(pEntity.validBlockTargets.size(), pEntity.iteratorLimit);

        //Precalculate list of potential ores
        for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(TAG_ORES)) {
            pEntity.validBlockStates.add(blockHolder.value().defaultBlockState());
        }

        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectMetal(GnosticOrbExecutorEntity pEntity) {
        BlockPos posQuery = pEntity.validBlockTargets.get(pEntity.iterator);
        BlockState stateQuery;
        if(pEntity.validBlockStates.size() == 1)
            stateQuery = pEntity.validBlockStates.get(0);
        else
            stateQuery = pEntity.validBlockStates.get(r.nextInt(pEntity.validBlockStates.size()));

        pEntity.level().setBlock(posQuery, stateQuery, 3);
        MagiChemMod.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(posQuery.getX(), posQuery.getY(), posQuery.getZ(), 20f, pEntity.level().dimension())),
                new ParticleSpawnAnointingS2CPacket(posQuery.getX(), posQuery.getY(), posQuery.getZ(), pEntity.materiaColor, true));

        pEntity.iterator++;
    }

    public static void preCacheThought(GnosticOrbExecutorEntity pEntity) {
        //Precalculate places that could be ore
        int range = 6;
        for(int y = pEntity.blockPosition().getY()-(range/2); y<=pEntity.blockPosition().getX()+(range/2); y++) {
            for (int x = pEntity.blockPosition().getX()-range; x<=pEntity.blockPosition().getX()+range; x++) {
                for (int z = pEntity.blockPosition().getZ()-range; z<=pEntity.blockPosition().getZ()+range; z++) {
                    BlockPos posQuery = new BlockPos(x, y, z);
                    BlockState stateQuery = pEntity.level().getBlockState(posQuery);

                    boolean isBookshelf = stateQuery.is(TAG_BOOKSHELVES);

                    if(isBookshelf) {
                        pEntity.validBlockTargets.add(posQuery);
                    }
                }
            }
        }
        Collections.shuffle(pEntity.validBlockTargets);

        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectThought(GnosticOrbExecutorEntity pEntity) {
        BlockPos posQuery = pEntity.validBlockTargets.get(0);

        MagiChemMod.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(posQuery.getX(), posQuery.getY(), posQuery.getZ(), 20f, pEntity.level().dimension())),
                new ParticleSpawnAnointingS2CPacket(posQuery.getX(), posQuery.getY(), posQuery.getZ(), pEntity.materiaColor, true));

        pEntity.level().destroyBlock(posQuery, false);
        ItemEntity ie = new ItemEntity(pEntity.level(), posQuery.getX()+0.5, posQuery.getY()+0.5, posQuery.getZ()+0.5, new ItemStack(ItemRegistry.OBSCURE_PROGNOSTICATIONS.get()));
        ie.setDeltaMovement(0, 0.375, 0);
        pEntity.level().addFreshEntity(ie);

        pEntity.iterator++;
    }
}
