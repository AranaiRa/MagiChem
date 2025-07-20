package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ConstructIsItemDistillable extends ConstructConditional<ConstructIsItemDistillable> {
    private boolean useLeftHand = true;

    public ConstructIsItemDistillable(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        ItemStack itemInHand = construct.asEntity().getItemInHand(useLeftHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if(itemInHand.isEmpty()) return false;

        DistillationFabricationRecipe recipeQuery = DistillationFabricationRecipe.getDistillingRecipe(construct.asEntity().level(), itemInHand);

        return recipeQuery != null;
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.QUERY_IS_ITEM_DISTILLABLE);
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskBooleanParameter("query_is_item_distillable.boolean"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("query_is_item_distillable.boolean").ifPresent((param) -> {
            if (param instanceof ConstructTaskBooleanParameter booleanParam) {
                useLeftHand = booleanParam.getValue();
            }
        });
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }

    @Override
    public ConstructIsItemDistillable copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructIsItemDistillable task) {
            this.useLeftHand = task.useLeftHand;
        }

        return this;
    }

    @Override
    public ConstructIsItemDistillable duplicate() {
        return new ConstructIsItemDistillable(this.construct, this.guiIcon).copyFrom(this);
    }
}
