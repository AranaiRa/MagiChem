package com.aranaira.magichem.item;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class WinterChargeItem extends Item {
    public static final Random r = new Random();

    public WinterChargeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        boolean commitChange = !pContext.getLevel().isClientSide();
        boolean changed = false;
        BlockPos center = pContext.getClickedPos();

        for(int x=-1; x<=1; x++) {
            for(int y=-1; y<=2; y++) {
                for(int z=-1; z<=1; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState oldState = pContext.getLevel().getBlockState(pos);
                    BlockState underneath = pContext.getLevel().getBlockState(pos.below());

                    if(oldState.getBlock() == Blocks.SNOW) {
                        int layers = oldState.getValue(BlockStateProperties.LAYERS);

                        layers = MathUtils.clamp(layers + r.nextInt(2) + 1, 1, 8);

                        if(commitChange) {
                            BlockState newState = oldState.setValue(BlockStateProperties.LAYERS, layers);
                            pContext.getLevel().setBlock(pos, newState, 3);
                            pContext.getLevel().sendBlockUpdated(pos, oldState, newState, 3);
                        }

                        changed = true;
                    } else if(oldState.getBlock() == Blocks.WATER) {
                        if(commitChange) {
                            BlockState newState = Blocks.ICE.defaultBlockState();
                            pContext.getLevel().setBlock(pos, newState, 3);
                            pContext.getLevel().sendBlockUpdated(pos, oldState, newState, 3);
                        }

                        changed = true;
                    } else if(oldState.canBeReplaced()
                            && underneath.isCollisionShapeFullBlock(pContext.getLevel(), pos)
                            && underneath.getBlock() != Blocks.ICE
                            && underneath.getBlock() != Blocks.PACKED_ICE
                            && underneath.getBlock() != Blocks.BLUE_ICE
                            && underneath.getBlock() != Blocks.FROSTED_ICE
                            ) {
                        if(commitChange) {
                            BlockState newState = Blocks.SNOW.defaultBlockState();
                            pContext.getLevel().setBlock(pos, newState, 3);
                            pContext.getLevel().sendBlockUpdated(pos, oldState, newState, 3);
                        }

                        changed = true;
                    }
                }
            }
        }

        if(changed) {
            Player player = pContext.getPlayer();

            if(player == null)
                pContext.getItemInHand().shrink(1);
            else if(!player.isCreative())
                pContext.getItemInHand().shrink(1);

            spawnParticles(pContext.getLevel(), center);
        }

        return super.useOn(pContext);
    }

    private static final int PARTICLE_COUNT = 80;
    private static final float VELOCITY_MODIFIER = 8.0f;

    public void spawnParticles(Level pLevel, BlockPos pCenter) {
        //Stolen MERCILESSLY from the frost impact particle poof
        Vec3 pos;
        if(pLevel.getBlockState(pCenter).isCollisionShapeFullBlock(pLevel, pCenter))
            pos = pCenter.getCenter().add(0,1,0);
        else
            pos = pCenter.getCenter();
        float spread = 0.01f;

        int j;
        Vec3 particlePos;
        Vec3 velocity;
        for(j = 0; j < PARTICLE_COUNT / 4; ++j) {
            particlePos = pos.add(new Vec3(0,2,0).xRot((float)(Math.random() * 360.0D)).yRot((float)(Math.random() * 360.0D)).zRot((float)(Math.random() * 360.0D))
                    .scale(Math.random() * (double)VELOCITY_MODIFIER * 0.1f)
            );
            velocity = particlePos.subtract(pos).normalize().scale(Math.random() * (double)spread * (double)VELOCITY_MODIFIER);

            pLevel.addParticle(new MAParticleType(ParticleInit.FROST.get())
                    .setScale(0.05f).setMaxAge(60).setGravity(0.01f).setPhysics(true),
                    particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }

        for(j = 0; j < PARTICLE_COUNT / 2; ++j) {
            particlePos = pos.add(new Vec3(0,2,0).xRot((float)(Math.random() * 360.0D)).yRot((float)(Math.random() * 360.0D)).zRot((float)(Math.random() * 360.0D)).scale(Math.random() * VELOCITY_MODIFIER * 0.1f));
            velocity = particlePos.subtract(pos).normalize().scale(Math.random() * (double)spread * (double)VELOCITY_MODIFIER).add(0,0.2,0);

            pLevel.addParticle(ParticleTypes.SNOWFLAKE,
                    particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }

        for(j = 0; j < PARTICLE_COUNT / 4; ++j) {
            particlePos = pos.add((new Vec3(-0.5D + Math.random(), 0.0D, -0.5D + Math.random())).scale(0.5f * VELOCITY_MODIFIER));

            pLevel.addParticle(new MAParticleType(ParticleInit.FROST.get())
                    .setScale(0.05F * VELOCITY_MODIFIER).setColor(140, 150, 160, 64).setMaxAge(60),
                    particlePos.x, particlePos.y, particlePos.z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.wintercharge")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
