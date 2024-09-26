package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.Animations;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.blocks.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ConstructHarvestCrystals extends ConstructAITask<ConstructHarvestCrystals> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos targetCrystal;
    private AABB area;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer;
    private static final Random random = new Random();
    private static final TagKey<Block> harvestableCrystal = BlockTags.create(new ResourceLocation("minecraft", "harvestable_crystal"));

    public ConstructHarvestCrystals(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        targetCrystal = null;
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            switch (this.phase) {
                case SETUP -> {
                    for(int y=(int)area.minY; y<=area.maxY; y++) {
                        for (int x = (int) area.minX; x <= area.maxX; x++) {
                            for (int z = (int) area.minZ; z <= area.maxZ; z++) {
                                BlockPos pos = new BlockPos(x, y, z);
                                BlockState state = construct.asEntity().level().getBlockState(pos);
                                if(state.is(harvestableCrystal)) {
                                    targetCrystal = pos;
                                    break;
                                }
                            }

                            if(targetCrystal != null) break;
                        }

                        if(targetCrystal != null) break;
                    }

                    if(targetCrystal != null) {
                        setMoveTarget(targetCrystal);
                        this.phase = ETaskPhase.MOVE_TO_CRYSTAL;
                    } else {
                        pushDiagnosticMessage("I couldn't find a crystal to break off. I'm sure they'll grow eventually.", false);
                        this.waitTimer = 61;
                        this.phase = ETaskPhase.WAIT_TO_FAIL;
                    }
                }
                case MOVE_TO_CRYSTAL -> {
                    if(this.doMove(7.5f)) {
                        this.waitTimer = 21;
                        this.phase = ETaskPhase.BREAK_CRYSTAL;
                        final Optional<InteractionHand> handWithCapability = construct.getHandWithCapability(ConstructCapability.RANGED_ATTACK);
                        if(handWithCapability.isPresent()) {
                            if (handWithCapability.get() == InteractionHand.MAIN_HAND)
                                construct.forceAnimation(Animations.SHOOT_LEFT, true);
                            else
                                construct.forceAnimation(Animations.SHOOT_RIGHT, true);
                        }
                    }
                }
                case BREAK_CRYSTAL -> {
                    if(this.waitTimer > 0) {
                        this.waitTimer --;
                    } else {
                        construct.asEntity().level().destroyBlock(targetCrystal, true, construct.asEntity());
                        this.waitTimer = 21;
                        this.phase = ETaskPhase.WAIT_TO_RESTART;
                        pushDiagnosticMessage("That one looks shiny enough! Bzzow!", false);
                    }
                }
                case WAIT_TO_RESTART -> {
                    if(this.waitTimer > 0) {
                        this.waitTimer --;
                    } else {
                        construct.clearForcedAnimation();
                        this.phase = ETaskPhase.SETUP;
                        setSuccessCode();
                    }
                }
                case WAIT_TO_FAIL -> {
                    if(this.waitTimer > 0) {
                        this.waitTimer --;
                    } else {
                        construct.clearForcedAnimation();
                        this.phase = ETaskPhase.SETUP;
                        forceFail();
                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.HARVEST_CRYSTALS);
    }

    @Override
    public ConstructHarvestCrystals duplicate() {
        return new ConstructHarvestCrystals(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructHarvestCrystals copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructHarvestCrystals task) {
            this.area = task.area;
        }

        return this;
    }

    @Override
    public void readNBT(CompoundTag compoundTag) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag compoundTag) {
        return compoundTag;
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskAreaParameter("harvest_crystals.area"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("harvest_crystals.area").ifPresent((param) -> {
            if (param instanceof ConstructTaskAreaParameter areaParam) {
                if(areaParam.getPoints() != null) {
                    if (areaParam.getArea() != null)
                        this.area = areaParam.getArea();
                }
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.area != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.RANGED_ATTACK};
    }

    enum ETaskPhase {
        SETUP,
        MOVE_TO_CRYSTAL,
        BREAK_CRYSTAL,
        WAIT_TO_FAIL,
        WAIT_TO_RESTART
    }

}