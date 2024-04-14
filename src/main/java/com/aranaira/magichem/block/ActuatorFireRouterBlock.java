package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.block.entity.routers.ActuatorFireRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.ActuatorWaterRouterBlockEntity;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.items.base.INoCreativeTab;
import com.mna.spells.SpellsInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;

public class ActuatorFireRouterBlock extends BaseActuatorRouterBlock implements INoCreativeTab, ISpellInteractibleBlock<ActuatorFireRouterBlock> {

    public ActuatorFireRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ActuatorFireRouterBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof ActuatorFireRouterBlockEntity router) {
            BlockState masterState = router.getMaster().getBlockState();
            pLevel.levelEvent(pPlayer, 2001, pPos, getId(masterState));
        }
    }

    @Override
    public boolean onHitBySpell(Level level, BlockPos blockPos, ISpellDefinition iSpellDefinition) {
        for(IModifiedSpellPart isp : iSpellDefinition.getComponents()){
            if(isp.getPart().getRegistryName().equals(SpellsInit.FIRE_DAMAGE.getRegistryName())) {
                float damage = isp.getValue(Attribute.DAMAGE);
                float duration = isp.getValue(Attribute.DURATION);
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof ActuatorFireRouterBlockEntity afrbe) {
                    BlockEntity mbe = level.getBlockEntity(afrbe.getMasterPos());
                    if(mbe instanceof ActuatorFireBlockEntity afbe) {
                        afbe.setFuelDuration(Math.round(damage * duration * 20), iSpellDefinition.getHighestAffinity() == Affinity.HELLFIRE);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }
}