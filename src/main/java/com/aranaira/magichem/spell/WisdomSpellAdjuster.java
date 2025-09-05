package com.aranaira.magichem.spell;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
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

    public static boolean checkSpellDamageAttribute(SpellAdjustingContext context) {
        if(context.stage != SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)
            return checkSpellAttribute(context, Attribute.DAMAGE);
        return false;
    }

    public static boolean checkSpellDelayAttribute(SpellAdjustingContext context) {
        if(context.stage != SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)
            return checkSpellAttribute(context, Attribute.DELAY);
        return false;
    }

    public static boolean checkSpellDurationAttribute(SpellAdjustingContext context) {
        if(context.stage != SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)
            return checkSpellAttribute(context, Attribute.DURATION);
        return false;
    }

    public static boolean checkSpellLesserMagnitudeAttribute(SpellAdjustingContext context) {
        if(context.stage != SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)
            return checkSpellAttribute(context, Attribute.LESSER_MAGNITUDE);
        return false;
    }

    public static boolean checkSpellMagnitudeAttribute(SpellAdjustingContext context) {
        if(context.stage != SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)
            return checkSpellAttribute(context, Attribute.MAGNITUDE);
        return false;
    }

    public static boolean checkSpellSpeedAttribute(SpellAdjustingContext context) {
        if(context.stage != SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)
            return checkSpellAttribute(context, Attribute.RADIUS);
        return false;
    }

    public static boolean checkSpellRadiusAttribute(SpellAdjustingContext context) {
        if(context.stage != SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)
            return checkSpellAttribute(context, Attribute.RADIUS);
        return false;
    }

    public static boolean checkSpellRangeAttribute(SpellAdjustingContext context) {
        if(context.stage != SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)
            return checkSpellAttribute(context, Attribute.RANGE);
        return false;
    }

    // MODIFY METHODS

    public static void modifySpellDamageAttribute(SpellAdjustingContext context) {
        if(context.caster instanceof Player p) {
            modifySpellAttribute(context, Attribute.DAMAGE, getWisdomAdjustmentForAttribute(p, Attribute.DAMAGE));
        }
    }

    public static void modifySpellDelayAttribute(SpellAdjustingContext context) {
        if(context.caster instanceof Player p) {
            modifySpellAttribute(context, Attribute.DELAY, getWisdomAdjustmentForAttribute(p, Attribute.DELAY));
        }
    }

    public static void modifySpellDurationAttribute(SpellAdjustingContext context) {
        if(context.caster instanceof Player p) {
            modifySpellAttribute(context, Attribute.DURATION, getWisdomAdjustmentForAttribute(p, Attribute.DURATION));
        }
    }

    public static void modifySpellLesserMagnitudeAttribute(SpellAdjustingContext context) {
        if(context.caster instanceof Player p) {
            modifySpellAttribute(context, Attribute.LESSER_MAGNITUDE, getWisdomAdjustmentForAttribute(p, Attribute.LESSER_MAGNITUDE));
        }
    }

    public static void modifySpellMagnitudeAttribute(SpellAdjustingContext context) {
        if(context.caster instanceof Player p) {
            modifySpellAttribute(context, Attribute.MAGNITUDE, getWisdomAdjustmentForAttribute(p, Attribute.MAGNITUDE));
        }
    }

    public static void modifySpellSpeedAttribute(SpellAdjustingContext context) {
        if(context.caster instanceof Player p) {
            modifySpellAttribute(context, Attribute.SPEED, getWisdomAdjustmentForAttribute(p, Attribute.SPEED));
        }
    }

    public static void modifySpellRadiusAttribute(SpellAdjustingContext context) {
        if(context.caster instanceof Player p) {
            modifySpellAttribute(context, Attribute.RADIUS, getWisdomAdjustmentForAttribute(p, Attribute.RADIUS));
            modifySpellAttribute(context, Attribute.DEPTH, getWisdomAdjustmentForAttribute(p, Attribute.DEPTH));
            modifySpellAttribute(context, Attribute.WIDTH, getWisdomAdjustmentForAttribute(p, Attribute.WIDTH));
            modifySpellAttribute(context, Attribute.HEIGHT, getWisdomAdjustmentForAttribute(p, Attribute.HEIGHT));
        }
    }

    public static void modifySpellRangeAttribute(SpellAdjustingContext context) {
        if(context.caster instanceof Player p) {
            modifySpellAttribute(context, Attribute.RANGE, getWisdomAdjustmentForAttribute(p, Attribute.RANGE));
        }
    }

    // BASE METHODS

    private static int getWisdomAdjustmentForAttribute(Player pPlayer, Attribute pAttribute) {
        final IWisdomCapability capability = WisdomProvider.getCapability(pPlayer);

        return capability.getValue(pAttribute);
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

        if(hasWisdom.booleanValue() && context.spell.getShape() != null) {
            if (context.caster instanceof Player && (context.stage == SpellCastStage.CASTING || context.stage == SpellCastStage.SPELL_TOOLTIP || context.stage == SpellCastStage.CALCULATING_MANA_COST || context.stage == SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE)) {
                final List<IModifiedSpellPart<SpellEffect>> components = context.spell.getComponents();

                ImmutableList<Attribute> shapeAttributes = context.spell.getShape().getContainedAttributes();
                if(shapeAttributes.contains(attribute)) return true;

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
                    if (attributeQuery == pAttribute) {
//                        c.setValue(attributeQuery, Math.max(0, (c.getValue(attributeQuery) + pSteps)));
                        for(int i=0;i<pSteps;i++)
                            c.stepUpIgnoreMax(pAttribute);
                    }
                }
            });
        }
    }
}
