package com.aranaira.magichem.foundation;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CachedArmorData {
    private Item headPiece, bodyPiece, legPiece, footPiece;
    private int headValue, bodyValue, legValue, footValue;

    public void snapshot(Player pPlayer) {
        final Inventory inventory = pPlayer.getInventory();
        for(ItemStack armorStack : inventory.armor) {
            if(armorStack.getItem() instanceof ArmorItem armor)
                setPrevious(armor, armorStack.getDamageValue());
        }
    }

    public boolean matchesPrevious(Type pType, Item pItem) {
        if(pType == Type.HELMET) return headPiece == pItem;
        if(pType == Type.CHESTPLATE) return bodyPiece == pItem;
        if(pType == Type.LEGGINGS) return legPiece == pItem;
        if(pType == Type.BOOTS) return footPiece == pItem;
        return false;
    }

    public boolean matchesPrevious(ArmorItem pItem) {
        return matchesPrevious(pItem.getType(), pItem);
    }

    public void setPrevious(ArmorItem pItem, int pDurability) {
        setPrevious(pItem.getType(), pItem, pDurability);
    }

    public void setPrevious(Type pType, Item pItem, int pDurability) {
        if(pType == Type.HELMET) {
            headPiece = pItem;
            headValue = pDurability;
        }
        if(pType == Type.CHESTPLATE) {
            bodyPiece = pItem;
            bodyValue = pDurability;
        }
        if(pType == Type.LEGGINGS) {
            legPiece = pItem;
            legValue = pDurability;
        }
        if(pType == Type.BOOTS) {
            footPiece = pItem;
            footValue = pDurability;
        }
    }

    public void restore(ItemStack pStack) {
        if(pStack.getItem() instanceof ArmorItem armor) {
            final Type type = armor.getType();

            if(type == Type.HELMET) pStack.setDamageValue(headValue);
            if(type == Type.CHESTPLATE) pStack.setDamageValue(bodyValue);
            if(type == Type.LEGGINGS) pStack.setDamageValue(legValue);
            if(type == Type.BOOTS) pStack.setDamageValue(footValue);
        }
    }
}
