package com.github.burgerguy.recordable.shared.entrypoint;

import com.github.burgerguy.recordable.shared.menu.ColorPalette;

public interface RecordableApi {

    /**
     * Implement this if you need to modify the color palette, including accepted items,
     * level overrides, etc.
     * @param colorPalette The color palette to modify
     */
    void modifyColorPalette(ColorPalette colorPalette);
}
