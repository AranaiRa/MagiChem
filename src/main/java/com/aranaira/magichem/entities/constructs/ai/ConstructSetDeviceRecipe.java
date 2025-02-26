package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.events.CommonEventHelper;
import com.aranaira.magichem.foundation.IHasDeviceRecipeSlot;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

import static com.aranaira.magichem.foundation.IHasDeviceRecipeSlot.*;

public class ConstructSetDeviceRecipe extends ConstructAITask<ConstructSetDeviceRecipe> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos deviceTarget, recipeTemplateTarget;
    private ItemStack recipeStack;
    private ETaskPhase phase = ETaskPhase.SETUP;
    private int waitTimer;

    public ConstructSetDeviceRecipe(IConstruct<?> construct, ResourceLocation guiIcon) {
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
            switch (this.phase) {
                case SETUP -> {
                    this.setMoveTarget(deviceTarget);
                    this.phase = ETaskPhase.MOVE_TO_DEVICE;
                }
                case MOVE_TO_DEVICE -> {
                    if (doMove(2.0F)) {
                        this.phase = ETaskPhase.WAIT_AT_DEVICE;
                        this.waitTimer = 6;
                        BlockEntity be = construct.asEntity().level().getBlockEntity(deviceTarget);
                        if (be instanceof IHasDeviceRecipeSlot ihdrs) {
                            if(recipeTemplateTarget != null) {
                                BlockEntity targetBE = construct.asEntity().level().getBlockEntity(recipeTemplateTarget);
                                LazyOptional<IItemHandler> itemHandler = targetBE.getCapability(ForgeCapabilities.ITEM_HANDLER);
                                if(itemHandler.isPresent()) {
                                    itemHandler.ifPresent(cap -> {
                                        ItemStack newRecipe = cap.getStackInSlot(0);
                                        ItemStack oldRecipe = ihdrs.getRecipeItem();
                                        swingHandWithCapability(ConstructCapability.CARRY);

                                        if (!oldRecipe.equals(newRecipe, true)) {
                                            byte result = ihdrs.setRecipe(newRecipe);
                                            if (newRecipe.isEmpty())
                                                this.pushDiagnosticMessage("I cleared the device's recipe, boss.", false);
                                            else {
                                                if(result == ERROR_CODE_SUCCESS)
                                                    this.pushDiagnosticMessage("I updated the device's recipe, boss.", false);
                                                else if(result == ERROR_CODE_NO_BLOCK_ENTITY)
                                                    this.pushDiagnosticMessage("I can only set the recipe of an alchemical device. Sorry, boss!", false);
                                                else if(result == ERROR_CODE_MUST_BE_ADMIXTURE)
                                                    this.pushDiagnosticMessage("The device needs an Admixture as a recipe item. Sorry, boss!", false);
                                                else if(result == ERROR_CODE_NO_SUCH_RECIPE)
                                                    this.pushDiagnosticMessage("The device doesn't have a recipe that results in that item. Sorry, boss!", false);
                                            }
                                        }
                                    });
                                } else {
                                    this.phase = ETaskPhase.WAIT_TO_FAIL;
                                    swingHandWithCapability(ConstructCapability.CARRY);
                                    this.pushDiagnosticMessage("That block doesn't accept recipe changes. Sorry, boss!", false);
                                }
                            } else {
                                ItemStack oldRecipe = ihdrs.getRecipeItem();

                                if (!oldRecipe.equals(recipeStack, true)) {
                                    byte result = ihdrs.setRecipe(recipeStack);
                                    swingHandWithCapability(ConstructCapability.CARRY);
                                    if (recipeStack.isEmpty())
                                        this.pushDiagnosticMessage("I cleared the device's recipe, boss.", false);
                                    else {
                                        if(result == ERROR_CODE_SUCCESS)
                                            this.pushDiagnosticMessage("I updated the device's recipe, boss.", false);
                                        else if(result == ERROR_CODE_NO_BLOCK_ENTITY)
                                            this.pushDiagnosticMessage("I can only set the recipe of an alchemical device. Sorry, boss!", false);
                                        else if(result == ERROR_CODE_MUST_BE_ADMIXTURE)
                                            this.pushDiagnosticMessage("The device needs an Admixture as a recipe item. Sorry, boss!", false);
                                        else if(result == ERROR_CODE_NO_SUCH_RECIPE)
                                            this.pushDiagnosticMessage("The device doesn't have a recipe that results in that item. Sorry, boss!", false);
                                    }
                                }
                            }
                        } else {
                            this.phase = ETaskPhase.WAIT_TO_FAIL;
                            swingHandWithCapability(ConstructCapability.CARRY);
                            this.pushDiagnosticMessage("That block doesn't accept recipe changes. Sorry, boss.", false);
                        }
                    }
                }
                case WAIT_AT_DEVICE -> {
                    this.waitTimer--;
                    if(this.waitTimer == 0) {
                        this.setSuccessCode();
                    }
                }
                case WAIT_TO_FAIL -> {
                    this.waitTimer--;
                    if(this.waitTimer == 0) {
                        this.forceFail();
                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.SET_DEVICE_RECIPE);
    }

    @Override
    public ConstructSetDeviceRecipe duplicate() {
        return new ConstructSetDeviceRecipe(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructSetDeviceRecipe copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructSetDeviceRecipe task) {
            this.deviceTarget = task.deviceTarget;
            this.recipeTemplateTarget = task.recipeTemplateTarget;
            this.recipeStack = task.recipeStack;
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
        parameters.add(new ConstructTaskPointParameter("set_device_recipe.point1"));
        parameters.add(new ConstructTaskPointParameter("set_device_recipe.point2"));
        parameters.add(new ConstructTaskItemStackParameter("set_device_recipe.stack"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("set_device_recipe.point1").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.deviceTarget = pointParam.getPosition();
            }
        });
        this.getParameter("set_device_recipe.point2").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.recipeTemplateTarget = pointParam.getPosition();
            }
        });
        this.getParameter("set_device_recipe.stack").ifPresent((param) -> {
            if (param instanceof ConstructTaskItemStackParameter stackParam) {
                this.recipeStack = stackParam.getStack();
            }
        });
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.deviceTarget != null && (this.recipeTemplateTarget != null || this.recipeStack != null);
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    }

    enum ETaskPhase {
        SETUP,
        MOVE_TO_DEVICE,
        WAIT_AT_DEVICE,
        WAIT_TO_FAIL
    }
}
