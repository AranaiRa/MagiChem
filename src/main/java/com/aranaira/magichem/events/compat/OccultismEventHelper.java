package com.aranaira.magichem.events.compat;

import com.aranaira.magichem.item.compat.occultism.OccultRitualTalismanItem;
import com.aranaira.magichem.registry.compat.OccultismItemRegistry;
import com.klikli_dev.modonomicon.api.ModonomiconAPI;
import com.klikli_dev.modonomicon.api.multiblock.Multiblock;
import com.klikli_dev.occultism.common.block.ChalkGlyphBlock;
import com.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.klikli_dev.occultism.registry.OccultismBlocks;
import com.klikli_dev.occultism.registry.OccultismItems;
import com.klikli_dev.occultism.registry.OccultismRecipes;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OccultismEventHelper {
    private static final Random r = new Random();

    public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        BlockState targetState = event.getLevel().getBlockState(event.getPos());

        event.setCanceled(OccultismEventHelper.handleRitualTalismanOnSacrificialBowl(event.getLevel(), event.getEntity(), event.getPos(), targetState, stack));
    }

    public static boolean handleRitualTalismanOnSacrificialBowl(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pTarget, ItemStack pStack) {
        if(pTarget.getBlock() == OccultismBlocks.GOLDEN_SACRIFICIAL_BOWL.get()) {
            if(pStack.getItem() == OccultismItemRegistry.OCCULT_RITUAL_TALISMAN.get()) {
                storePentacleInTalisman(pLevel, pPlayer, pStack, pPos, pLevel.getBlockState(pPos));
                return true;
            }
        }
        return false;
    }

    public static void handleRitualTalismanPlacement(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pentacleCenter = pContext.getClickedPos();
        BlockState pentacleCenterState = level.getBlockState(pentacleCenter);

        //Place the stored pentacle
        if(pentacleCenterState.getBlock() != OccultismBlocks.GOLDEN_SACRIFICIAL_BOWL.get()){
            CompoundTag nbt = pContext.getItemInHand().getOrCreateTag();

            if(nbt.contains("pentacleID")) {
                List<Pair<ResourceLocation, Multiblock>> allPentacles = getAllPentacles(level);
                String pentacleID = nbt.getString("pentacleID");

                for(Pair<ResourceLocation, Multiblock> pair : allPentacles) {
                    if(pair.getFirst().getPath().equals(pentacleID)) {
                        Multiblock mb = pair.getSecond();

                        int sx = mb.getSize().getX();
                        int sz = mb.getSize().getZ();
                        int span = (sx - 1) / 2;
                        ItemStackHandler ish = new ItemStackHandler(sx * sz);
                        ish.deserializeNBT(nbt.getCompound("spec"));

                        //Check to see if we CAN place the whole pentacle first
                        boolean collisionDetected = false;
                        int slotID = 0;
                        for (int z = -span; z <= span; z++) {
                            for (int x = -span; x <= span; x++) {
                                ItemStack is = ish.getStackInSlot(slotID);

                                if (is != ItemStack.EMPTY) {
                                    BlockPos scanPos = pentacleCenter.offset(x, 1, z);
                                    BlockState scanState = level.getBlockState(scanPos);

                                    if (!scanState.canBeReplaced()) {
                                        collisionDetected = true;
                                        break;
                                    }
                                }

                                slotID++;
                            }
                            if (collisionDetected)
                                break;
                        }

                        //If we found a collision, we're just done here
                        if (collisionDetected)
                            break;

                        //Now we actually DO place things
                        if (true) {
                            slotID = 0;
                            for (int z = -span; z <= span; z++) {
                                for (int x = -span; x <= span; x++) {
                                    ItemStack is = ish.getStackInSlot(slotID);

                                    if (is != ItemStack.EMPTY) {
                                        BlockPos scanPos = pentacleCenter.offset(x, 1, z);
                                        BlockState scanState = level.getBlockState(scanPos);

                                        if (!is.isEmpty()) {
                                            Item surrogate = is.getItem();
                                            BlockState newState = Blocks.AIR.defaultBlockState();

                                            if (surrogate == OccultismItems.CHALK_WHITE.get())
                                                newState = OccultismBlocks.CHALK_GLYPH_WHITE.get().defaultBlockState().setValue(ChalkGlyphBlock.SIGN, r.nextInt(ChalkGlyphBlock.MAX_SIGN));
                                            else if (surrogate == OccultismItems.CHALK_GOLD.get())
                                                newState = OccultismBlocks.CHALK_GLYPH_GOLD.get().defaultBlockState().setValue(ChalkGlyphBlock.SIGN, r.nextInt(ChalkGlyphBlock.MAX_SIGN));
                                            else if (surrogate == OccultismItems.CHALK_PURPLE.get())
                                                newState = OccultismBlocks.CHALK_GLYPH_PURPLE.get().defaultBlockState().setValue(ChalkGlyphBlock.SIGN, r.nextInt(ChalkGlyphBlock.MAX_SIGN));
                                            else if (surrogate == OccultismItems.CHALK_RED.get())
                                                newState = OccultismBlocks.CHALK_GLYPH_RED.get().defaultBlockState().setValue(ChalkGlyphBlock.SIGN, r.nextInt(ChalkGlyphBlock.MAX_SIGN));
                                            else if (surrogate instanceof BlockItem bi)
                                                newState = bi.getBlock().defaultBlockState();

                                            if(!pContext.getLevel().isClientSide()) {
                                                level.setBlock(scanPos, newState, 3);
                                                level.sendBlockUpdated(scanPos, scanState, newState, 3);
                                            } else {
                                                spawnInParticles(level, scanPos);
                                            }
                                        }

                                    }
                                    slotID++;
                                }
                            }

                            if(!pContext.getPlayer().isCreative()) {
                                nbt.remove("pentacleID");
                                nbt.remove("spec");
                                pContext.getItemInHand().setTag(nbt);
                            }
                        }

                        break;
                    }
                }
            }
        }
    }

    private static List<Pair<ResourceLocation, Multiblock>> ALL_PENTACLES = new ArrayList<>();
    private static List<Pair<ResourceLocation, Multiblock>> getAllPentacles(Level pLevel) {
        if(ALL_PENTACLES.size() > 0)
            return ALL_PENTACLES;

        List<RitualRecipe> allPentacles = pLevel.getRecipeManager().getAllRecipesFor(OccultismRecipes.RITUAL_TYPE.get());

        for(RitualRecipe ritualRecipe : allPentacles) {
            ResourceLocation pentacleId = ritualRecipe.getPentacleId();
            Multiblock multiblock = ModonomiconAPI.get().getMultiblock(pentacleId);

            boolean alreadyContained = false;
            for(Pair<ResourceLocation, Multiblock> prm : ALL_PENTACLES) {
                if(pentacleId.equals(prm.getFirst())) {
                    alreadyContained = true;
                    break;
                }
            }

            if(!alreadyContained) {
                ALL_PENTACLES.add(new Pair<>(pentacleId, multiblock));
            }
        }

        return ALL_PENTACLES;
    }

    private static void storePentacleInTalisman(Level pLevel, Player pPlayer, ItemStack pItemStack, BlockPos pPentacleCenter, BlockState pPentacleBlockState) {
        if(pPentacleBlockState.getBlock() == OccultismBlocks.GOLDEN_SACRIFICIAL_BOWL.get() && !pItemStack.getOrCreateTag().contains("pentacleID")) {
            List<Pair<ResourceLocation, Multiblock>> allPentacles = getAllPentacles(pLevel);

            for(Pair<ResourceLocation, Multiblock> pair : allPentacles) {
                Multiblock mb = pair.getSecond();

                Rotation validation = mb.validate(pLevel, pPentacleCenter);
                if(validation != null) {
                    CompoundTag nbt = new CompoundTag();
                    nbt.putString("pentacleID", pair.getFirst().getPath());

                    int sx = mb.getSize().getX();
                    int sz = mb.getSize().getZ();
                    int span = (sx - 1) / 2;
                    ItemStackHandler ish = new ItemStackHandler(sx * sz);

                    int slotID = 0;
                    for (int z = -span; z <= span; z++) {
                        for(int x=-span; x<=span; x++) {
                            BlockPos scanPos = pPentacleCenter.offset(x, 0, z);
                            BlockState scanState = pLevel.getBlockState(scanPos);
                            boolean changed = false;

                            //Chalk glyphs
                            if(scanState.getBlock() == OccultismBlocks.CHALK_GLYPH_WHITE.get()) {
                                ish.setStackInSlot(slotID, new ItemStack(OccultismItems.CHALK_WHITE.get()));
                                changed = true;
                            }
                            else if(scanState.getBlock() == OccultismBlocks.CHALK_GLYPH_GOLD.get()) {
                                ish.setStackInSlot(slotID, new ItemStack(OccultismItems.CHALK_GOLD.get()));
                                changed = true;
                            }
                            else if(scanState.getBlock() == OccultismBlocks.CHALK_GLYPH_PURPLE.get()) {
                                ish.setStackInSlot(slotID, new ItemStack(OccultismItems.CHALK_PURPLE.get()));
                                changed = true;
                            }
                            else if(scanState.getBlock() == OccultismBlocks.CHALK_GLYPH_RED.get()) {
                                ish.setStackInSlot(slotID, new ItemStack(OccultismItems.CHALK_RED.get()));
                                changed = true;
                            }

                            //Bowls, Skulls, and Crystals
                            else if(scanState.getBlock() == OccultismBlocks.SACRIFICIAL_BOWL.get() ||
                                    scanState.getBlock() == OccultismBlocks.GOLDEN_SACRIFICIAL_BOWL.get() ||
                                    scanState.getBlock() == OccultismBlocks.SPIRIT_ATTUNED_CRYSTAL.get() ||
                                    scanState.getBlock() == Blocks.SKELETON_SKULL ||
                                    scanState.getBlock() == Blocks.SKELETON_WALL_SKULL ||
                                    scanState.getBlock() == Blocks.WITHER_SKELETON_SKULL ||
                                    scanState.getBlock() == Blocks.WITHER_SKELETON_WALL_SKULL
                            ) {
                                ish.setStackInSlot(slotID, new ItemStack(scanState.getBlock().asItem()));
                                changed = true;
                            }

                            //I have no goddamned clue why this tag shows up in F3 but resolves false, have to go manual for now
                            else if(scanState.getBlock() == OccultismBlocks.CANDLE_WHITE.get() ||
                                    scanState.getBlock() == Blocks.CANDLE ||
                                    scanState.getBlock() == Blocks.RED_CANDLE ||
                                    scanState.getBlock() == Blocks.ORANGE_CANDLE ||
                                    scanState.getBlock() == Blocks.YELLOW_CANDLE ||
                                    scanState.getBlock() == Blocks.LIME_CANDLE ||
                                    scanState.getBlock() == Blocks.GREEN_CANDLE ||
                                    scanState.getBlock() == Blocks.CYAN_CANDLE ||
                                    scanState.getBlock() == Blocks.LIGHT_BLUE_CANDLE ||
                                    scanState.getBlock() == Blocks.BLUE_CANDLE ||
                                    scanState.getBlock() == Blocks.PURPLE_CANDLE ||
                                    scanState.getBlock() == Blocks.MAGENTA_CANDLE ||
                                    scanState.getBlock() == Blocks.PINK_CANDLE ||
                                    scanState.getBlock() == Blocks.BROWN_CANDLE ||
                                    scanState.getBlock() == Blocks.WHITE_CANDLE ||
                                    scanState.getBlock() == Blocks.GRAY_CANDLE ||
                                    scanState.getBlock() == Blocks.LIGHT_GRAY_CANDLE ||
                                    scanState.getBlock() == Blocks.BLACK_CANDLE
                            ) {
                                Item item = scanState.getBlock().asItem();
                                ish.setStackInSlot(slotID, new ItemStack(item));
                                changed = true;
                            }

                            if(changed) {
                                if(!pLevel.isClientSide()) {
                                    pLevel.setBlock(scanPos, Blocks.AIR.defaultBlockState(), 3);
                                } else {
                                    spawnOutParticles(pLevel, scanPos);
                                }
                            }

                            slotID++;
                        }
                    }

                    nbt.put("spec", ish.serializeNBT());
                    pItemStack.shrink(1);
                    ItemStack droppedItem = new ItemStack(OccultismItemRegistry.OCCULT_RITUAL_TALISMAN.get());
                    droppedItem.setTag(nbt);
                    ItemEntity ie = new ItemEntity(pLevel, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), droppedItem);
                    pLevel.addFreshEntity(ie);
                }
            }
        }
    }

    private static void spawnInParticles(Level pLevel, BlockPos pPos) {
        for(int i=0; i<10; i++) {
            Vec3 startOffset = new Vec3(r.nextFloat()-.5, r.nextFloat()*.5, r.nextFloat()-.5).normalize().scale(2.5);
            Vec3 end = new Vec3(pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5);

            pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_RANDOM.get())
                            .setPhysics(true).setScale(0.125f).setMaxAge(16+r.nextInt(16))
                            .setMover(new ParticleLerpMover(
                                    end.x + startOffset.x, end.y + startOffset.y, end.z + startOffset.z,
                                    end.x, end.y, end.z
                            )),
                    pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5,
                    0, 0.05, 0.05);
        }

        for(int i=0; i<10; i++) {
            pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_RANDOM.get())
                            .setScale(0.5f).setMaxAge(35 + r.nextInt(20))
                            .setColor(64, 96 + r.nextInt(32), 96 + r.nextInt(32)),
                    pPos.getX() + 0.5, pPos.getY() + 0.125, pPos.getZ() + 0.5,
                    0.05, 0.05, 0.05);
        }
    }

    private static void spawnOutParticles(Level pLevel, BlockPos pPos) {
        for(int i=0; i<10; i++) {
            pLevel.addParticle(new MAParticleType(ParticleInit.ARCANE_RANDOM.get())
                            .setPhysics(true).setScale(0.0625f).setMaxAge(25+r.nextInt(50)),
                    pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5,
                    0, 0.05, 0.05);
        }

        pLevel.addParticle(new MAParticleType(ParticleInit.ENDER.get())
                        .setScale(0.5f).setMaxAge(35+r.nextInt(20))
                        .setMover(new ParticleLerpMover(pPos.getX()+.5, pPos.getY()+.125, pPos.getZ()+.5, pPos.getX()+.5, pPos.getY()+.625, pPos.getZ()+.5)),
                pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5,
                0, 0, 0);
    }
}
