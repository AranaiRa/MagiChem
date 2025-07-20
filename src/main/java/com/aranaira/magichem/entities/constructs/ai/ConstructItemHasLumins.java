package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ConstructItemHasLumins extends ConstructConditional<ConstructItemHasLumins> {
    private boolean useLeftHand = true;
    private int checkMode = -1;

    public ConstructItemHasLumins(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        ItemStack itemInHand = construct.asEntity().getItemInHand(useLeftHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if(itemInHand.isEmpty()) return false;
        LuminType type = InventoryHelper.checkLuminTypeOnStack(itemInHand);

        if(checkMode == 0) return type != LuminType.NONE;
        if(checkMode == 1) return type == LuminType.SOLAR;
        if(checkMode == 2) return type == LuminType.LUNAR;
        if(checkMode == 3) return type == LuminType.SIDEREAL;

        return false;
    }

    @Override
    public ResourceLocation getType() {
        return ManaAndArtificeMod.getConstructTaskRegistry().getKey(ConstructTasksRegistry.QUERY_DOES_ITEM_HAVE_LUMINS);
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskIntegerParameter("query_item_has_lumins.int", 0, 3));
        parameters.add(new ConstructTaskBooleanParameter("query_item_has_lumins.boolean", true));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("query_item_has_lumins.int").ifPresent((param) -> {
            if (param instanceof ConstructTaskIntegerParameter intParam) {
                checkMode = intParam.getValue();
            }
        });
        this.getParameter("query_item_has_lumins.boolean").ifPresent((param) -> {
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
    public ConstructItemHasLumins copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructItemHasLumins task) {
            this.useLeftHand = task.useLeftHand;
            this.checkMode = task.checkMode;
        }

        return this;
    }

    @Override
    public ConstructItemHasLumins duplicate() {
        return new ConstructItemHasLumins(this.construct, this.guiIcon).copyFrom(this);
    }
}
