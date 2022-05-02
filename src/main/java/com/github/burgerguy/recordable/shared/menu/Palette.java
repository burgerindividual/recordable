package com.github.burgerguy.recordable.shared.menu;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;

public class Palette {
    private final Int2ObjectMap<Pair<Component, Set<Item>>> rawColorMap;

    public Palette() {
        this.rawColorMap = new Int2ObjectOpenHashMap<>(16);
    }

    /**
     * Adds a new entry for the given color, or adds to an existing one.
     * If the entry already exists for the given raw color, the name will be ignored
     * and the dyeItems will be added to the existing entry.
     * If the entry does not exist, the name will be used and an entry will be created.
     */
    public void add(int rawColor, Component name, Set<Item> dyeItems) {
        this.getOrCreateKey(rawColor, name).second().addAll(dyeItems);
    }

    /**
     * Helper method for {@link Palette#add(int, Component, Set)}
     */
    public void add(int rawColor, Component name, Item dyeItem) {
        this.getOrCreateKey(rawColor, name).second().add(dyeItem);
    }

    /**
     * Helper method for {@link Palette#add(int, Component, Item)} to use built in values
     * from DyeColor
     */
    public void addFromDyeColor(DyeColor dyeColor) {
        this.add(dyeColor.getTextColor(), new TextComponent(dyeColor.getName()), DyeItem.byColor(dyeColor));
    }

    private Pair<Component, Set<Item>> getOrCreateKey(int rawColor, Component name) {
        return this.rawColorMap.computeIfAbsent(rawColor, ignored -> {
            Set<Item> itemSet = new HashSet<>();
            return new ObjectObjectImmutablePair<>(name, itemSet);
        });
    }

    public record Color(int rawColor, Component name, Item dyeItem) {}
}
