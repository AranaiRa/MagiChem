package com.aranaira.magichem.entities;

import com.aranaira.magichem.MagiChemMod;
import com.mna.api.items.IPositionalItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.items.ItemInit;
import com.mna.items.runes.ItemRuneMarking;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.mutable.MutableBoolean;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.*;

public class DestructiveHarmonicsEntity extends Entity implements IEntityAdditionalSpawnData {
    public DestructiveHarmonicsEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private static final Random r = new Random();
    private BlockPos targetPos = null;
    private Phase phase = Phase.SETUP_AND_SCAN;
    private ArrayList<BlockPos> scannedHighPriorityBlocks = new ArrayList();
    private ArrayList<BlockPos> scannedLowPriorityBlocks = new ArrayList();
    private int phaseTimer = 0;
    private UUID initiatingPlayer = null;
    private Player initiatingPlayerResolved = null;
    private LazyOptional<ICuriosItemHandler> curiosInventory = null;

    public static final TagKey<Block> TAG_HIGH_PRIORITY = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "harmoniscope_high_priority"));
    public static final TagKey<Block> TAG_LOW_PRIORITY = BlockTags.create(new ResourceLocation(MagiChemMod.MODID, "harmoniscope_low_priority"));

    public void setTargetPos(BlockPos pTargetPos) {
        targetPos = pTargetPos;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if(pCompound.contains("targetPos"))
            targetPos = BlockPos.of(pCompound.getLong("targetPos"));
        if(pCompound.contains("initiatingPlayer"))
            initiatingPlayer = pCompound.getUUID("initiatingPlayer");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        if(targetPos != null)
            pCompound.putLong("targetPos", targetPos.asLong());
        if(initiatingPlayer != null)
            pCompound.putUUID("initiatingPlayer", initiatingPlayer);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeLong(targetPos != null ? targetPos.asLong() : 0);
        buffer.writeUUID(initiatingPlayer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        long pos = additionalData.readLong();
        targetPos = BlockPos.of(pos);
        initiatingPlayer = additionalData.readUUID();
    }

    public void setInitiatingPlayer(Player pPlayer){
        initiatingPlayer = pPlayer.getUUID();
        initiatingPlayerResolved = pPlayer;
    }

    @Override
    public void tick() {
        super.tick();

        if(initiatingPlayer != null && initiatingPlayerResolved == null) {
            initiatingPlayerResolved = level().getPlayerByUUID(initiatingPlayer);
        }
        if(initiatingPlayerResolved != null && curiosInventory == null) {
            curiosInventory = CuriosApi.getCuriosInventory(initiatingPlayerResolved);
        }

        if(level().getGameTime() % 2 > 0) return;

        if(phase == Phase.SETUP_AND_SCAN) {
            if(!level().isClientSide())
                scanBlocksAroundTarget();
            phaseTimer = 22;
            phase = Phase.PARTICLES;
        }
        else if(phase == Phase.PARTICLES) {
            if(level().isClientSide())
            {
                Vector3 center = new Vector3(targetPos.getX()+0.5, targetPos.getY()+0.5, targetPos.getZ()+0.5);
                int ts = (level().getDayTime() < 12000 || level().getDayTime() > 23000) ? 3 : 1;

                if(phaseTimer == 20) {
                    for (int i = 0; i < 70; i++) {
                        double x = (r.nextDouble() - 0.5) * 4;
                        double y = (r.nextDouble() - 0.5) * 4;
                        double z = (r.nextDouble() - 0.5) * 4;
                        Vector3 speed = new Vector3(x, y, z).normalize().scale(8.0f);

                        int c = r.nextInt(55);
                        level().addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor(200+c, 200+c, 200+c, 255)
                                        .setScale(0.04f).setMaxAge(16),
                                center.x+speed.x, center.y+speed.y, center.z+speed.z,
                                center.x, center.y, center.z);
                    }
                }
                if(phaseTimer > 13) {
                    for (int i = 0; i < 10; i++) {
                        double x = (r.nextDouble() - 0.5) * 4;
                        double y = (r.nextDouble() - 0.5) * 4;
                        double z = (r.nextDouble() - 0.5) * 4;
                        Vector3 point = new Vector3(x, y, z).normalize().scale(2.0f).add(center);

                        int c = r.nextInt(155);
                        level().addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                        .setColor((100+c) / ts, (100+c) / ts, 255 / ts, 48)
                                        .setScale(1.6f).setMaxAge(132),
                                point.x, point.y, point.z,
                                point.x, point.y, point.z);
                    }
                }
            }

            phaseTimer--;
            if(phaseTimer <= 0)
                phase = Phase.SHATTER_HIGH_PRIORITY;
        }
        else if(phase == Phase.SHATTER_HIGH_PRIORITY) {
            if(scannedHighPriorityBlocks.size() > 0) {
                BlockPos posQuery = scannedHighPriorityBlocks.remove(r.nextInt(scannedHighPriorityBlocks.size()));
                BlockState stateQuery = level().getBlockState(posQuery);
                dropItemResult(level(), posQuery.getCenter(), new ItemStack(stateQuery.getBlock().asItem()));
                level().destroyBlock(posQuery, false);
            } else {
                phase = Phase.SHATTER_LOW_PRIORITY;
            }
        }
        else if(phase == Phase.SHATTER_LOW_PRIORITY) {
            if(scannedLowPriorityBlocks.size() > 0) {
                BlockPos posQuery = scannedLowPriorityBlocks.remove(r.nextInt(scannedLowPriorityBlocks.size()));
                BlockState stateQuery = level().getBlockState(posQuery);
                dropItemResult(level(), posQuery.getCenter(), new ItemStack(stateQuery.getBlock().asItem()));
                level().destroyBlock(posQuery, false);
            } else {
                phase = Phase.CLEANUP;
            }
        }
        else if(phase == Phase.CLEANUP) {
            kill();
        }
    }

    private void scanBlocksAroundTarget() {
        int tX = targetPos.getX();
        int tY = targetPos.getY();
        int tZ = targetPos.getZ();

        for(int y=tY-2;y<=tY+2;y++) {
            for(int z=tZ-2;z<=tZ+2;z++) {
                for (int x=tX-2;x<=tX+2;x++) {
                    boolean bT = y == tY + 2;
                    boolean bB = y == tY - 2;
                    boolean bL = z == tZ + 2;
                    boolean bR = z == tZ - 2;
                    boolean bF = x == tX + 2;
                    boolean bK = x == tX - 2;

                    //Skip the border BlockPos to make this look a bit more spherical
                    if((bT && bF) || (bT && bK) || (bT && bL) || (bT && bR) ||
                       (bB && bF) || (bB && bK) || (bB && bL) || (bB && bR) ||
                       (bF && bL) || (bF && bR) || (bK && bL) || (bK && bR))
                        continue;

                    BlockPos posQuery = new BlockPos(x,y,z);
                    BlockState stateQuery = level().getBlockState(posQuery);
                    if(stateQuery.is(TAG_HIGH_PRIORITY))
                        scannedHighPriorityBlocks.add(posQuery);
                    else if(stateQuery.is(TAG_LOW_PRIORITY))
                        scannedLowPriorityBlocks.add(posQuery);
                }
            }
        }
    }

    private void dropItemResult(Level pLevel, Vec3 pPos, ItemStack pStack) {
        if(initiatingPlayerResolved != null && curiosInventory != null) {
            curiosInventory.ifPresent(cap -> {
                cap.getStacksHandler("ring").ifPresent(slotsInventory -> {
                    boolean isLesser = false;
                    boolean isGreater = false;
                    boolean hasRune = false;
                    BlockPos targetInventoryPos = null;
                    if(initiatingPlayerResolved.getOffhandItem().getItem() instanceof IPositionalItem ipi) {
                        hasRune = true;
                        targetInventoryPos = ipi.getLocation(initiatingPlayerResolved.getOffhandItem());
                    }

                    for (int i = 0; i < slotsInventory.getStacks().getSlots(); i++) {
                        ItemStack stack = slotsInventory.getStacks().getStackInSlot(i);
                        isLesser = isLesser || stack.getItem() == ItemInit.COLLECTOR_RING_LESSER.get();
                        isGreater = isGreater || stack.getItem() == ItemInit.COLLECTOR_RING_GREATER.get();
                    }

                    MutableBoolean sentToInventory = new MutableBoolean(false);
                    if (isGreater && hasRune && targetInventoryPos != null) {
                        BlockEntity targetInventory = pLevel.getBlockEntity(targetInventoryPos);
                        if(targetInventory != null) {
                            LazyOptional<IItemHandler> targetInventoryCap = targetInventory.getCapability(ForgeCapabilities.ITEM_HANDLER);
                            targetInventoryCap.ifPresent(tCap -> {
                                ItemStack query = pStack;
                                for(int i=0; i<tCap.getSlots(); i++) {
                                    query = tCap.insertItem(i, query, false);
                                    if(query.isEmpty())
                                        break;
                                }
                                if(!query.isEmpty()) {
                                    final Vec3 playerPos = initiatingPlayerResolved.position();
                                    ItemEntity ie = new ItemEntity(level(), playerPos.x(), playerPos.y(), playerPos.z(), pStack);
                                    pLevel.addFreshEntity(ie);
                                }
                                sentToInventory.setValue(true);
                            });
                        }
                    }
                    if(!sentToInventory.booleanValue()){
                        if (isLesser || isGreater) {
                            final Vec3 playerPos = initiatingPlayerResolved.position();
                            ItemEntity ie = new ItemEntity(level(), playerPos.x(), playerPos.y(), playerPos.z(), pStack);
                            pLevel.addFreshEntity(ie);
                        } else {
                            ItemEntity ie = new ItemEntity(level(), pPos.x(), pPos.y(), pPos.z(), pStack);
                            pLevel.addFreshEntity(ie);
                        }
                    }
                });
            });
        }
        else {
            ItemEntity ie = new ItemEntity(level(), pPos.x(), pPos.y(), pPos.z(), pStack);
            pLevel.addFreshEntity(ie);
        }
    }

    private enum Phase {
        SETUP_AND_SCAN,
        PARTICLES,
        SHATTER_HIGH_PRIORITY,
        SHATTER_LOW_PRIORITY,
        CLEANUP
    }
}
