package com.github.burgerguy.recordable.shared.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class LabelPrinterMenu extends AbstractContainerMenu {

    protected LabelPrinterMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
