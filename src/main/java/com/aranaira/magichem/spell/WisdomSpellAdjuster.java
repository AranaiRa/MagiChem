package com.aranaira.magichem.spell;

import com.aranaira.magichem.MagiChemMod;
import com.google.common.collect.ImmutableList;
import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.mutable.MutableBoolean;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

public class WisdomSpellAdjuster {
    private static final TagKey<Item>
            TAG_MAGICHEM_WISDOM_STONES = ItemTags.create(new ResourceLocation(MagiChemMod.MODID, "wisdom_stones"));

    // CHECK METHODS

    public static boolean checkSpellRadiusAttribute(SpellAdjustingContext context) {
        return true;
//        return checkSpellAttribute(context, Attribute.RADIUS);
    }

    // MODIFY METHODS

    public static void modifySpellRadiusAttribute(SpellAdjustingContext context) {
        if(context.caster instanceof Player p) {
            modifySpellAttribute(context, Attribute.RADIUS, getWisdomAdjustmentForAttribute(p, Attribute.RADIUS));
        }
    }

    // BASE METHODS

    private static int getWisdomAdjustmentForAttribute(Player player, Attribute attribute) {
        return 1;
    }

    private static boolean checkSpellAttribute(SpellAdjustingContext context, Attribute attribute) {
        MutableBoolean hasWisdom = new MutableBoolean(false);
        CuriosApi.getCuriosInventory(context.caster).ifPresent(curiosInventory -> {
            curiosInventory.getStacksHandler("wisdom").ifPresent(slotsInventory -> {
                for(int i=0; i<slotsInventory.getStacks().getSlots(); i++) {
                    hasWisdom.setValue(slotsInventory.getStacks().getStackInSlot(i).is(TAG_MAGICHEM_WISDOM_STONES));
                    if(hasWisdom.getValue()) break;
                }
            });
        });

        if(hasWisdom.booleanValue()) {
            if (context.caster instanceof Player && (context.stage == SpellCastStage.CASTING || context.stage == SpellCastStage.SPELL_TOOLTIP || context.stage == SpellCastStage.CALCULATING_MANA_COST || context.stage == SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)) {
                final List<IModifiedSpellPart<SpellEffect>> components = context.spell.getComponents();

                for (IModifiedSpellPart<SpellEffect> component : components) {
                    final ImmutableList<Attribute> containedAttributes = component.getContainedAttributes();
                    if (containedAttributes.contains(attribute)) return true;
                }

            }
        }
        return false;
    }

    private static final void modifySpellAttribute(SpellAdjustingContext pContext, Attribute pAttribute, int pSteps){
        if(pContext.caster instanceof Player p) {
            IModifiedSpellPart<Shape> shape = pContext.spell.getShape();
            if (shape != null && shape.getContainedAttributes() != null) {
                if(shape.getContainedAttributes().contains(pAttribute))
                {
                    for (Attribute attributeQuery : shape.getContainedAttributes()) {
                        if (attributeQuery == pAttribute) {
                            shape.setValue(attributeQuery, Math.max(0, (shape.getValue(attributeQuery) + pSteps)));
                        }
                    }
                }
            }

            pContext.spell.iterateComponents((c) -> {
                for (Attribute attributeQuery : c.getContainedAttributes()) {
                    if (attributeQuery == Attribute.DAMAGE) {
                        c.setValue(attributeQuery, Math.max(0, (c.getValue(attributeQuery) + pSteps)));
                    }
                }
            });
        }
    }
}
