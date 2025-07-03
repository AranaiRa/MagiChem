package com.aranaira.magichem.entities;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.Quadlet;
import com.aranaira.magichem.networking.ParticleSpawnAnointingS2CPacket;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.SummonUtils;
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
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Consumer;

public class GnosticOrbExecutorEntity extends Entity implements IEntityAdditionalSpawnData {
    //First int is the activation tick modulus
    //Second int is the iterator limit
    //Third ThrowingRunnable is the function that precaches the correct arraylist
    //Fourth ThrowingRunnable is the function that executes on tick
    private static final HashMap<String, Quadlet<Integer, Integer, Consumer<GnosticOrbExecutorEntity>, Consumer<GnosticOrbExecutorEntity>>> PROPHECY_DATA = new HashMap<>();
    private static final Random r = new Random();

    public static final TagKey<Block> TAG_BOOKSHELVES = BlockTags.create(new ResourceLocation("forge", "bookshelves"));
    public static final TagKey<Block> TAG_CAKES = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "cakes"));
    public static final TagKey<Block> TAG_FLOWER_GENERATING_BLACKLIST = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "flower_spawning_blacklist"));
    public static final TagKey<Block> TAG_ORES = BlockTags.create(new ResourceLocation("forge", "ores_in_ground/stone"));
    public static final TagKey<Block> TAG_TABLES = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "tables"));

    private ArrayList<BlockPos> validBlockTargets = new ArrayList<>();
    private ArrayList<BlockState> validBlockStates = new ArrayList<>();
    private ArrayList<LivingEntity> validEntityTargets = new ArrayList<>();

    private String materiaType = "";
    private int materiaColor = 0, iterator = 0, iteratorLimit = 0, activationTickModulus = 0;
    private Boolean hasPreCached = false;
    private Player activatingPlayer = null;

    public GnosticOrbExecutorEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        if(PROPHECY_DATA.size() == 0) {
            PROPHECY_DATA.put("construct", new Quadlet<>(12, 8, GnosticOrbExecutorEntity::preCacheConstruct, GnosticOrbExecutorEntity::prophecyEffectConstruct));
//            PROPHECY_DATA.put("creature",  new Quadlet<>(4, 20, GnosticOrbExecutorEntity::preCacheCreature, GnosticOrbExecutorEntity::prophecyEffectCreature));
            PROPHECY_DATA.put("delight",   new Quadlet<>(7, 40, GnosticOrbExecutorEntity::preCacheDelight, GnosticOrbExecutorEntity::prophecyEffectDelight));
            PROPHECY_DATA.put("disaster",  new Quadlet<>(4, 75, GnosticOrbExecutorEntity::preCacheDisaster, GnosticOrbExecutorEntity::prophecyEffectDisaster));
            PROPHECY_DATA.put("exanimate", new Quadlet<>(4, 20, GnosticOrbExecutorEntity::preCacheExanimate, GnosticOrbExecutorEntity::prophecyEffectExanimate));
            PROPHECY_DATA.put("metal",     new Quadlet<>(2, 160, GnosticOrbExecutorEntity::preCacheMetal, GnosticOrbExecutorEntity::prophecyEffectMetal));
            PROPHECY_DATA.put("odors",     new Quadlet<>(1, 240, GnosticOrbExecutorEntity::preCacheOdors, GnosticOrbExecutorEntity::prophecyEffectOdors));
            PROPHECY_DATA.put("thought",   new Quadlet<>(1, 1, GnosticOrbExecutorEntity::preCacheThought, GnosticOrbExecutorEntity::prophecyEffectThought));
        }
    }

    public void configure(String pMateriaType, int pMateriaColor, Player pActivatingPlayer) {
        materiaType = pMateriaType;
        materiaColor = pMateriaColor;
        activationTickModulus = PROPHECY_DATA.get(materiaType).getFirst();
        iteratorLimit = PROPHECY_DATA.get(materiaType).getSecond();
        activatingPlayer = pActivatingPlayer;
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
        if(level() != null && !level().isClientSide()) {
            final UUID uuidQuery = UUID.fromString(pCompound.getString("activatingPlayer"));
            activatingPlayer = level().getServer().getPlayerList().getPlayer(uuidQuery);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putString("materiaType",materiaType);
        pCompound.putInt("materiaColor",materiaColor);
        pCompound.putInt("iterator",iterator);
        pCompound.putInt("iteratorLimit",iteratorLimit);
        pCompound.putInt("activationTickModulus",activationTickModulus);
        pCompound.putString("activatingPlayer",activatingPlayer.getStringUUID());
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

            final Quadlet<Integer, Integer, Consumer<GnosticOrbExecutorEntity>, Consumer<GnosticOrbExecutorEntity>> data = PROPHECY_DATA.get(materiaType);

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

    public static void preCacheConstruct(GnosticOrbExecutorEntity pEntity) {
        //Precalculate places that are iron blocks
        int range = 8;
        for(int y = pEntity.blockPosition().getY()-(range/2); y<=pEntity.blockPosition().getX()+(range/2); y++) {
            for (int x = pEntity.blockPosition().getX()-range; x<=pEntity.blockPosition().getX()+range; x++) {
                for (int z = pEntity.blockPosition().getZ()-range; z<=pEntity.blockPosition().getZ()+range; z++) {
                    BlockPos posQuery = new BlockPos(x, y, z);
                    BlockState stateQuery = pEntity.level().getBlockState(posQuery);

                    boolean isIronBlock = stateQuery.getBlock() == Blocks.IRON_BLOCK;

                    if(isIronBlock) {
                        pEntity.validBlockTargets.add(posQuery);
                    }
                }
            }
        }
        Collections.shuffle(pEntity.validBlockTargets);
        pEntity.iteratorLimit = Math.min(pEntity.iteratorLimit, pEntity.validBlockTargets.size());

        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectConstruct(GnosticOrbExecutorEntity pEntity) {
        BlockPos posQuery = pEntity.validBlockTargets.get(pEntity.iterator);

        MagiChemMod.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(posQuery.getX(), posQuery.getY(), posQuery.getZ(), 20f, pEntity.level().dimension())),
                new ParticleSpawnAnointingS2CPacket(posQuery.getX(), posQuery.getY(), posQuery.getZ(), pEntity.materiaColor, true));

        pEntity.level().destroyBlock(posQuery, false);

        IronGolem ig = new IronGolem(EntityType.IRON_GOLEM, pEntity.level());
        SummonUtils.setSummon(ig, pEntity.activatingPlayer, Integer.MAX_VALUE);
        pEntity.level().addFreshEntity(ig);
        ig.setPos(posQuery.getX()+0.5, posQuery.getY()+1, posQuery.getZ()+0.5);

        pEntity.iterator++;
    }

    public static void preCacheCreature(GnosticOrbExecutorEntity pEntity) {
        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectCreature(Pair<GnosticOrbExecutorEntity, Player> data) {
        GnosticOrbExecutorEntity pEntity = data.getFirst();
        Player pActivatingPlayer = data.getSecond();
        pEntity.iterator = pEntity.iteratorLimit;
    }

    public static void preCacheDelight(GnosticOrbExecutorEntity pEntity) {
        //Precalculate cake destinations
        int range = 20;
        for(int y = pEntity.blockPosition().getY()-(range/2); y<=pEntity.blockPosition().getY()+(range/2); y++) {
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

        pEntity.level().explode(pEntity.activatingPlayer, pos.getX(), pos.getY(), pos.getZ(), r.nextInt(12) + 8, true, Level.ExplosionInteraction.BLOCK);

        pEntity.iterator++;
    }

    public static void preCacheExanimate(GnosticOrbExecutorEntity pEntity) {
        int range = 30;
        AABB bounds = new AABB(
                pEntity.getX() - range, pEntity.getY() - (range / 3f), pEntity.getZ() - range,
                pEntity.getX() + range, pEntity.getY() + (range / 3f), pEntity.getZ() + range
        );
        for(Entity e : pEntity.level().getEntities(null, bounds)) {
            if(e instanceof Villager v) {
                pEntity.validEntityTargets.add(v);
            }
        }
        pEntity.iteratorLimit = Math.min(pEntity.iteratorLimit, pEntity.validEntityTargets.size());

        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectExanimate(GnosticOrbExecutorEntity pEntity) {
        Entity e = pEntity.validEntityTargets.get(pEntity.iterator);
        if(e instanceof Villager v) {
            v.convertTo(EntityType.ZOMBIE_VILLAGER, true);
            MagiChemMod.CHANNEL.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(v.getX(), v.getY(), v.getZ(), 20f, pEntity.level().dimension())),
                    new ParticleSpawnAnointingS2CPacket((int)v.getX(), (int)v.getY(), (int)v.getZ(), pEntity.materiaColor, true));
        }

        pEntity.iterator++;
    }

    public static void preCacheMetal(GnosticOrbExecutorEntity pEntity) {
        //Precalculate places that could be ore
        int range = 10;
        for(int y = pEntity.blockPosition().getY()-(range/2); y<=pEntity.blockPosition().getY()+(range/2); y++) {
            for (int x = pEntity.blockPosition().getX()-range; x<=pEntity.blockPosition().getX()+range; x++) {
                for (int z = pEntity.blockPosition().getZ()-range; z<=pEntity.blockPosition().getZ()+range; z++) {
                    BlockPos posQuery = new BlockPos(x, y, z);
                    BlockState stateQuery = pEntity.level().getBlockState(posQuery);

                    boolean isStoneOreReplaceable = stateQuery.is(BlockTags.STONE_ORE_REPLACEABLES);

                    if(isStoneOreReplaceable) {
                        pEntity.validBlockTargets.add(posQuery);
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

    public static void preCacheOdors(GnosticOrbExecutorEntity pEntity) {
        //Precalculate places that are plantable
        int range = 20;
        int height = 5;

        for (int x = pEntity.blockPosition().getX()-range; x<=pEntity.blockPosition().getX()+range; x++) {
            for (int z = pEntity.blockPosition().getZ()-range; z<=pEntity.blockPosition().getZ()+range; z++) {
                float xQ = x - pEntity.blockPosition().getX();
                float zQ = z - pEntity.blockPosition().getZ();
                Vec2 v = new Vec2(xQ, zQ).normalized().scale(range);
                if(Math.abs(xQ) > Math.abs(v.x) && Math.abs(zQ) > Math.abs(v.y)) continue;

                for(int y = pEntity.blockPosition().getY()-(height/2); y<=pEntity.blockPosition().getY()+(height/2); y++) {
                    BlockPos posQuery = new BlockPos(x, y, z);
                    BlockState stateQuery = pEntity.level().getBlockState(posQuery);
                    BlockState stateAboveQuery = pEntity.level().getBlockState(posQuery.above());

                    boolean isDirt = stateQuery.is(BlockTags.DIRT);
                    boolean isOpenAbove = stateAboveQuery.isAir();

                    if(isDirt && isOpenAbove) {
                        pEntity.validBlockTargets.add(posQuery.above());
                    }
                }
            }
        }
        Collections.shuffle(pEntity.validBlockTargets);
        pEntity.iteratorLimit = Math.min(pEntity.validBlockTargets.size(), pEntity.iteratorLimit);

        //Precalculate list of flowers
        for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTags.FLOWERS)) {
            if(!blockHolder.value().defaultBlockState().is(TAG_FLOWER_GENERATING_BLACKLIST))
            pEntity.validBlockStates.add(blockHolder.value().defaultBlockState());
        }

        pEntity.hasPreCached = true;
    }

    public static void prophecyEffectOdors(GnosticOrbExecutorEntity pEntity) {
        BlockPos posQuery = pEntity.validBlockTargets.get(pEntity.iterator);
        BlockState stateQuery;
        if(pEntity.validBlockStates.size() == 1)
            stateQuery = pEntity.validBlockStates.get(0);
        else
            stateQuery = pEntity.validBlockStates.get(r.nextInt(pEntity.validBlockStates.size()));

        pEntity.level().setBlock(posQuery, stateQuery, 3);
        if(stateQuery.is(BlockTags.TALL_FLOWERS)) {
            BlockState bottom = stateQuery.getBlock().defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
            BlockState top = stateQuery.getBlock().defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            pEntity.level().setBlock(posQuery.above(), top, 3);
            pEntity.level().setBlock(posQuery, bottom, 3);
        } else {
            pEntity.level().setBlock(posQuery, stateQuery, 3);
        }
        MagiChemMod.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(posQuery.getX(), posQuery.getY(), posQuery.getZ(), 20f, pEntity.level().dimension())),
                new ParticleSpawnAnointingS2CPacket(posQuery.getX(), posQuery.getY(), posQuery.getZ(), pEntity.materiaColor, true));

        pEntity.iterator++;
    }

    public static void preCacheThought(GnosticOrbExecutorEntity pEntity) {
        //Precalculate places that could be ore
        int range = 6;
        for(int y = pEntity.blockPosition().getY()-(range/2); y<=pEntity.blockPosition().getY()+(range/2); y++) {
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
