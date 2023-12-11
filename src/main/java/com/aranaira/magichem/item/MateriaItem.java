package com.aranaira.magichem.item;

import com.aranaira.magichem.foundation.Essentia;
import com.aranaira.magichem.foundation.Materia;
import com.aranaira.magichem.foundation.enums.EEssentiaHouse;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class MateriaItem extends Item implements Materia {
    private final Materia materia;

    public MateriaItem(Materia materia, Item.Properties properties) {
        super(properties);
        this.materia = materia;
    }

    public MateriaItem(ResourceLocation resourceLocation, Item.Properties properties) {
        this((Materia) Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(resourceLocation)), properties);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if(getMateria() instanceof Essentia essentia) {
            tooltipComponents.add(MutableComponent.create(new LiteralContents(String.format("%s (%d)", getAbbreviation()))).withStyle(ChatFormatting.DARK_AQUA));
            String house = getTranslationKeyByHouse(essentia.getHouse());
            if(house != null)
                tooltipComponents.add(MutableComponent.create(new TranslatableContents(house)).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(MutableComponent.create(new LiteralContents(getAbbreviation())).withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    private String getTranslationKeyByHouse(EEssentiaHouse house) {
        switch(house) {
            case ELEMENTS: return "essentia.house.elements";
            case QUALITIES: return "essentia.house.qualities";
            case ALCHEMY: return "essentia.house.alchemy";
            default: return null;
        }
    }

    public Materia getMateria() {
        return materia;
    }

    @Override
    public Item asItem() {
        return null;
    }

    @Override
    public String getMateriaName() {
        return null;
    }

    @Override
    public String getAbbreviation() {
        return null;
    }

    @Override
    public int getColor() {
        return 0;
    }
}
