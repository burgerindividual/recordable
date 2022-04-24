package com.github.burgerguy.recordable.shared.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;

public record PaintColor(int rawColor, Component name, Item dyeItem) {
    public static PaintColor fromDyeColor(DyeColor dyeColor) {
        return new PaintColor(dyeColor.getTextColor(), new TextComponent(dyeColor.getName()), DyeItem.byColor(dyeColor));
    }
}
