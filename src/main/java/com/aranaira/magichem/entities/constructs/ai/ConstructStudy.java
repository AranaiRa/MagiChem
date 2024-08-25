package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.Animations;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.blocks.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ConstructStudy extends ConstructAITask<ConstructStudy> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos deskPos;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer, studyCyclesRemaining, learningItemExperience;
    private Optional<InteractionHand>  learningItemHand;
    private static final Random random = new Random();
    private static final TagKey<Item>
        studyMaterialSimple = ItemTags.create(new ResourceLocation("minecraft", "construct_study_materials_simple")),
        studyMaterialAdvanced = ItemTags.create(new ResourceLocation("minecraft", "construct_study_materials_advanced")),
        studyMaterialMaster = ItemTags.create(new ResourceLocation("minecraft", "construct_study_materials_master"));

    public ConstructStudy(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void tick() {
        super.tick();
        if(isFullyConfigured()) {
            if(construct.getIntelligence() < 16) {
                pushDiagnosticMessage("All these words are confusing me... I don't think I'm smart enough for this whole \"studying\" thing, boss.", false);
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
                                } else {
                                    learningItemExperience = 0;
                                    if (learningItem.is(studyMaterialSimple)) learningItemExperience = Config.constructStudyExperienceSimple;
                                    if (learningItem.is(studyMaterialAdvanced)) learningItemExperience = Config.constructStudyExperienceAdvanced;
                                    if (learningItem.is(studyMaterialMaster)) learningItemExperience = Config.constructStudyExperienceMaster;

                                    this.setMoveTarget(deskPos);
                                    this.phase = ETaskPhase.MOVE_TO_DESK;
                                    pushDiagnosticMessage("Found my desk! I'll get started reading right away.", false);
                                }
                            } else {
                                pushDiagnosticMessage("I can't study if I can't hold things to read!", false);
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
                        this.waitTimer = 85 - construct.getIntelligence();
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
                    ExperienceOrb eo = new ExperienceOrb(
                            construct.asEntity().level(),
                            construct.asEntity().position().x,
                            construct.asEntity().position().y + 1.25f,
                            construct.asEntity().position().z,
                            learningItemExperience);
                    construct.asEntity().level().addFreshEntity(eo);
                    ItemStack learningItem = construct.asEntity().getItemInHand(learningItemHand.get());
                    learningItem.shrink(1);
                    construct.asEntity().setItemInHand(learningItemHand.get(), learningItem.isEmpty() ? ItemStack.EMPTY : learningItem);
                    construct.clearForcedAnimation();
                    pushDiagnosticMessage("What an interesting read! I brokeded the book though...", false);
                    setSuccessCode();

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

}