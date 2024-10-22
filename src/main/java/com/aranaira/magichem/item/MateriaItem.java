package com.aranaira.magichem.item;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.networking.ParticleSpawnAnointingS2CPacket;
import com.aranaira.magichem.recipe.AnointingRecipe;
import com.aranaira.magichem.util.render.ColorUtils;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.tools.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class MateriaItem extends Item {
    private final String name;
    private int color;
    private static List<AnointingRecipe> allAnointingRecipes = new ArrayList<>();
    private static final Random r = new Random();

    public MateriaItem(String name, String color, Item.Properties properties) {
        super(properties);
        this.name = name;
        this.color = Integer.parseInt(color, 16) | 0xFF000000;
    }

    public String getMateriaName() {
        return this.name;
    }

    public int getMateriaColor() {
        return this.color;
    }

    public String getDisplayFormula() { return "?"; }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if(!pContext.getLevel().isClientSide()) {
            if (allAnointingRecipes.size() == 0)
                allAnointingRecipes = AnointingRecipe.getAllAnointingRecipes(pContext.getLevel());

            BlockState targetState = pContext.getLevel().getBlockState(pContext.getClickedPos());
            ItemStack itemInHand = pContext.getItemInHand();

            for (AnointingRecipe ar : allAnointingRecipes) {
                if (ar.getMateria() == itemInHand.getItem()) {
                    if (ar.getTarget() == targetState.getBlock()) {

                        if (ar.getChance() >= 100f || r.nextFloat(100) <= ar.getChance()) {
                            BlockState newState = ar.getResult().defaultBlockState();

                            if (targetState.hasProperty(FACING))
                                newState = newState.setValue(FACING, targetState.getValue(FACING));
                            if (targetState.hasProperty(HORIZONTAL_FACING))
                                newState = newState.setValue(HORIZONTAL_FACING, targetState.getValue(HORIZONTAL_FACING));
                            if (targetState.hasProperty(HALF))
                                newState = newState.setValue(HALF, targetState.getValue(HALF));
                            if (targetState.hasProperty(STAIRS_SHAPE))
                                newState = newState.setValue(STAIRS_SHAPE, targetState.getValue(STAIRS_SHAPE));
                            if (targetState.hasProperty(WATERLOGGED))
                                newState = newState.setValue(WATERLOGGED, targetState.getValue(WATERLOGGED));

                            pContext.getLevel().setBlock(pContext.getClickedPos(), newState, 3);
                            pContext.getLevel().sendBlockUpdated(pContext.getClickedPos(), targetState, newState, 3);

                            int x = pContext.getClickedPos().getX();
                            int y = pContext.getClickedPos().getY();
                            int z = pContext.getClickedPos().getZ();
                            final ResourceKey<Level> dim = pContext.getLevel().dimension();

                            MagiChemMod.CHANNEL.send(
                                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 12f, dim)),
                                    new ParticleSpawnAnointingS2CPacket(x, y, z, color, true));

                        } else {
                            int x = pContext.getClickedPos().getX();
                            int y = pContext.getClickedPos().getY();
                            int z = pContext.getClickedPos().getZ();
                            final ResourceKey<Level> dim = pContext.getLevel().dimension();

                            MagiChemMod.CHANNEL.send(
                                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 12f, dim)),
                                    new ParticleSpawnAnointingS2CPacket(x, y, z, color, false));

                        }
                        itemInHand.shrink(1);
                        if(!pContext.getPlayer().isCreative()) {
                            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                            Vec3 pos = pContext.getPlayer().getPosition(0);
                            ItemEntity ie = new ItemEntity(pContext.getLevel(), pos.x, pos.y, pos.z, bottle);
                            pContext.getLevel().addFreshEntity(ie);
                        }
                        pContext.getPlayer().setItemInHand(pContext.getHand(), itemInHand);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }

        return super.useOn(pContext);
    }

    public static void generateSuccessParticles(int pX, int pY, int pZ, int pColor) {
        if(Minecraft.getInstance().level != null) {
            int[] color = ColorUtils.getRGBAIntTintFromPackedInt(pColor);

            double spreadRadius = 0.375d;

            for (int i = 0; i < 12; i++) {

                double x = r.nextDouble() * spreadRadius * 2 - spreadRadius;
                double z = r.nextDouble() * spreadRadius * 2 - spreadRadius;

                Vector3 pos = (new Vector3(pX + x + 0.5, pY + 0.5, pZ + z + 0.5));

                Minecraft.getInstance().level.addParticle(new MAParticleType(ParticleInit.DUST_LERP.get())
                                .setScale(0.15f).setMaxAge(32 + r.nextInt(32))
                                .setMover(new ParticleLerpMover(pos.x, pos.y, pos.z, pos.x, pos.y + 1.0, pos.z))
                                .setColor(color[0], color[1], color[2], 128),
                        pos.x, pos.y, pos.z,
                        0, 0, 0);
            }

            spreadRadius = 0.0625;
            double speed = 0.15;

            for (int i = 0; i < 40; i++) {

                double x = r.nextDouble() * spreadRadius * 2 - spreadRadius;
                double z = r.nextDouble() * spreadRadius * 2 - spreadRadius;

                Vector3 pos = (new Vector3(pX + x + 0.5, pY + 0.5, pZ + z + 0.5));

                Minecraft.getInstance().level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setScale(0.10f).setMaxAge(16 + r.nextInt(32))
                                .setColor(color[0], color[1], color[2], 255)
                                .setGravity(0.0075f),
                        pos.x, pos.y, pos.z,
                        (r.nextDouble() - 0.5) * speed, r.nextDouble() * speed, (r.nextDouble() - 0.5) * speed);
            }
        }
    }

    public static void generateFailureParticles(int pX, int pY, int pZ, int pColor) {
        if(Minecraft.getInstance().level != null) {
            int[] color = ColorUtils.getRGBAIntTintFromPackedInt(pColor);

            double spreadRadius = 0.0625;
            double speed = 0.10;

            for (int i = 0; i < 10; i++) {

                double x = r.nextDouble() * spreadRadius * 2 - spreadRadius;
                double z = r.nextDouble() * spreadRadius * 2 - spreadRadius;

                Vector3 pos = (new Vector3(pX + x + 0.5, pY + 0.5, pZ + z + 0.5));

                Minecraft.getInstance().level.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setScale(0.10f).setMaxAge(16 + r.nextInt(32))
                                .setColor(color[0], color[1], color[2], 255)
                                .setGravity(0.0075f),
                        pos.x, pos.y, pos.z,
                        (r.nextDouble() - 0.5) * speed, r.nextDouble() * speed, (r.nextDouble() - 0.5) * speed);
            }
        }
    }
}
