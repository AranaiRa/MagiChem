package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.foundation.IHasDeviceRecipeSlot;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.List;

public class ConstructDeviceRecipeMatches extends ConstructConditional<ConstructDeviceRecipeMatches> {
    private BlockPos targetApparatus = null;
    private BlockPos recipeTemplateTarget = null;
    private ItemStack recipeStack = null;

    public ConstructDeviceRecipeMatches(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        if(targetApparatus == null)
            return false;

        BlockEntity be = construct.asEntity().level().getBlockEntity(targetApparatus);

        if(be instanceof IHasDeviceRecipeSlot ihdrs) {
            if(recipeTemplateTarget != null) {
                BlockEntity tbe = construct.asEntity().level().getBlockEntity(recipeTemplateTarget);
                LazyOptional<IItemHandler> capQuery = tbe.getCapability(ForgeCapabilities.ITEM_HANDLER);
                MutableBoolean matches = new MutableBoolean(false);
                capQuery.ifPresent(cap -> {
                    matches.setValue(cap.getStackInSlot(0).equals(ihdrs.getRecipeItem(), true));
                });
                return matches.booleanValue();
            } else {
                return ihdrs.getRecipeItem().getItem() == recipeStack.getItem();
            }
        }

        return false;
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.QUERY_DEVICE_RECIPE_MATCHES);
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("query_device_recipe_matches.point"));
        parameters.add(new ConstructTaskPointParameter("query_device_recipe_matches.point2"));
        parameters.add(new ConstructTaskItemStackParameter("query_device_recipe_matches.item"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("query_device_recipe_matches.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                BlockPos targetPos = pointParam.getPosition();
                if(targetPos != null) {
                    targetApparatus = targetPos;
                }
            }
        });

        this.getParameter("query_device_recipe_matches.point2").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                BlockPos targetPos = pointParam.getPosition();
                if(targetPos != null) {
                    this.recipeTemplateTarget = pointParam.getPosition();
                }
            }
        });

        this.getParameter("query_device_recipe_matches.item").ifPresent((param) -> {
            if (param instanceof ConstructTaskItemStackParameter itemParam) {
                recipeStack = itemParam.getStack();
            }
        });
    }

    @Override
    public boolean isFullyConfigured() {
        return targetApparatus != null && (this.recipeTemplateTarget != null || this.recipeStack != null);
    }

    @Override
    public ConstructDeviceRecipeMatches copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructDeviceRecipeMatches task) {
            this.targetApparatus = task.targetApparatus;
            this.recipeTemplateTarget = task.recipeTemplateTarget;
            this.recipeStack = task.recipeStack;
        }

        return this;
    }

    @Override
    public ConstructDeviceRecipeMatches duplicate() {
        return new ConstructDeviceRecipeMatches(this.construct, this.guiIcon).copyFrom(this);
    }
}
