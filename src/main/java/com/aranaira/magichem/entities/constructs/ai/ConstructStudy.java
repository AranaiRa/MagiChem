package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.item.FractallinePuzzleBoxItem;
import com.aranaira.magichem.recipe.ConstructStudyMaterialRecipe;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.Animations;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.blocks.BlockInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ConstructStudy extends ConstructAITask<ConstructStudy> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos deskPos;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer, studyCyclesRemaining, learningItemExperience;
    private Optional<InteractionHand>  learningItemHand;
    private static final Random random = new Random();
    private static final HashMap<Item, ConstructStudyMaterialRecipe> recipeData = new HashMap<>();

    public ConstructStudy(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        if(recipeData.size() == 0) {
            for(ConstructStudyMaterialRecipe recipe : ConstructStudyMaterialRecipe.getAllConstructStudyMaterialRecipes(construct.asEntity().level())) {
                recipeData.put(recipe.getItem(), recipe);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            if(construct.getIntelligence() < 16) {
                pushDiagnosticMessage("This thing I'm looking at is confusing me... I don't think I'm smart enough for this whole \"studying\" thing, boss.", false);
                forceFail();
            }

            switch (this.phase) {
                case SETUP -> {
                    if(isFullyConfigured()) {
                        if (construct.asEntity().level().getBlockState(deskPos).getBlock() == BlockInit.STUDY_DESK.get()) {
                            learningItemHand = construct.getHandWithCapability(ConstructCapability.CARRY);
                            if (learningItemHand.isPresent()) {
                                ItemStack learningItem = construct.asEntity().getItemInHand(learningItemHand.get());
                                if (learningItem.isEmpty()) {
                                    pushDiagnosticMessage("I need something to study, boss!", false);
                                    forceFail();
                                } else if(learningItem.getItem() == ItemRegistry.FRACTALLINE_PUZZLE_BOX.get()) {
                                    learningItemExperience = FractallinePuzzleBoxItem.getExperienceGain(learningItem);

                                    this.setMoveTarget(deskPos);
                                    this.phase = ETaskPhase.MOVE_TO_DESK;
                                    pushDiagnosticMessage("Found my desk! I'll figure out this puzzle this time, you'll see!", false);
                                } else if(learningItem.hasTag() && learningItem.getTag().contains("alreadyStudied")) {
                                    pushDiagnosticMessage("This item has already been studied, sorry boss!", false);
                                    forceFail();
                                } else if(recipeData.containsKey(learningItem.getItem())) {
                                    learningItemExperience = recipeData.get(learningItem.getItem()).getExperience();

                                    this.setMoveTarget(deskPos);
                                    this.phase = ETaskPhase.MOVE_TO_DESK;
                                    pushDiagnosticMessage("Found my desk! I'll get started learning right away.", false);
                                } else {
                                    pushDiagnosticMessage("I can't study this item. Sorry, boss.", false);
                                    forceFail();
                                }
                            } else {
                                pushDiagnosticMessage("I can't study if I can't hold onto things!", false);
                                forceFail();
                            }
                        } else {
                            pushDiagnosticMessage("I need a Study Desk to work at, boss!", false);
                        }
                    }
                }
                case MOVE_TO_DESK -> {
                    if(this.doMove(2.5f)) {
                        this.studyCyclesRemaining = 6;
                        ItemStack learningItem = construct.asEntity().getItemInHand(construct.getHandWithCapability(ConstructCapability.CARRY).get());
                        this.waitTimer = (85 - construct.getIntelligence()) * (learningItem.getItem() == ItemRegistry.FRACTALLINE_PUZZLE_BOX.get() ? 3 : 1);
                        this.phase = ETaskPhase.STUDY_CYCLE;
                        construct.forceAnimation(Animations.READING, true);
                    }
                }
                case STUDY_CYCLE -> {
                    if (studyCyclesRemaining > 0) {
                        if (this.waitTimer > 0)
                            this.waitTimer--;
                        else {
                            if(studyCyclesRemaining % 2 == 0) {
                                switch (random.nextInt(4)) {
                                    case 0 -> construct.setHappy(40);
                                    case 1 -> construct.setConcerned(40);
                                    case 2 -> construct.setAngry(40);
                                    case 3 -> construct.setUnimpressed(40);
                                }
                            }
                            this.studyCyclesRemaining--;
                            this.waitTimer = 40 + 2 * (60 - construct.getIntelligence());
                        }
                    } else {
                        this.phase = ETaskPhase.GENERATE_ORB;
                    }
                }
                case GENERATE_ORB -> {
                    ItemStack learningItem = construct.asEntity().getItemInHand(learningItemHand.get());
                    construct.clearForcedAnimation();
                    if(learningItem.isEmpty()) {
                        pushDiagnosticMessage("Hey, where did that thing I was holding go...?", false);
                        forceFail();
                    } else {
                        ExperienceOrb eo = new ExperienceOrb(
                                construct.asEntity().level(),
                                construct.asEntity().position().x,
                                construct.asEntity().position().y + 1.25f,
                                construct.asEntity().position().z,
                                learningItemExperience);
                        construct.asEntity().level().addFreshEntity(eo);
                        if (learningItem.getItem() != ItemRegistry.FRACTALLINE_PUZZLE_BOX.get()) {
                            if (recipeData.get(learningItem.getItem()).isConsumed()) {
                                learningItem.shrink(1);
                            } else {
                                CompoundTag nbt;
                                if (learningItem.hasTag()) {
                                    nbt = learningItem.getTag();
                                } else {
                                    nbt = new CompoundTag();
                                }
                                nbt.putBoolean("alreadyStudied", true);
                                learningItem.setTag(nbt);
                            }
                        }
                        construct.asEntity().setItemInHand(learningItemHand.get(), learningItem.isEmpty() ? ItemStack.EMPTY : learningItem);
                        if(learningItem.getItem() == ItemRegistry.FRACTALLINE_PUZZLE_BOX.get()) {
                            if(FractallinePuzzleBoxItem.trySolvePuzzle(learningItem))
                                pushDiagnosticMessage("Take THAT, puzzle! ...Wow, there's another puzzle inside! Today is the best.", false);
                            else
                                pushDiagnosticMessage("Hmm, that didn't work, but I bet I could get it with one more try...", false);
                        } else if(recipeData.get(learningItem.getItem()).isConsumed()){
                            pushDiagnosticMessage("What an interesting thingie! I learned a lot, but I brokeded it...", false);
                        } else {
                            pushDiagnosticMessage("What an interesting doodad! I learned a lot, boss!", false);
                        }
                        setSuccessCode();
                    }
                    this.phase = ETaskPhase.SETUP;
                }
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.COLLECT_EXPERIENCE);
    }

    @Override
    public ConstructStudy duplicate() {
        return new ConstructStudy(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructStudy copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructStudy task) {
            this.deskPos = task.deskPos;
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
        parameters.add(new ConstructTaskPointParameter("study.point"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("study.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                if(pointParam.getPoint() != null)
                    this.deskPos = pointParam.getPoint().getPosition();
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.deskPos != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    }

    enum ETaskPhase {
        SETUP,
        MOVE_TO_DESK,
        STUDY_CYCLE,
        GENERATE_ORB,
        WAIT_TO_FAIL
    }

    public void stop() {
        super.stop();
        construct.clearForcedAnimation(); // fallback cancelling
    }
}