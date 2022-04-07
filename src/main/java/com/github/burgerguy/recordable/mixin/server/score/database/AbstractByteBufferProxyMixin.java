package com.github.burgerguy.recordable.mixin.server.score.database;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Workaround to LMDBJava crash when launching without --add-opens command line argument.
 */
@Mixin(targets = "org/lmdbjava/ByteBufferProxy$AbstractByteBufferProxy")
public class AbstractByteBufferProxyMixin {

    @Redirect(method = "findField", at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Field;setAccessible(Z)V"), remap = false)
    private static void handleSetAccessible(Field field, boolean accessible) {
        try {
            field.setAccessible(accessible);
        } catch (Throwable t) {
            // if this happens, the reflection proxy won't work, but the unsafe proxy will if it's available.
        }
    }

    /**
     * @reason Don't use the hidden DirectBuffer api.
     * @author burgerdude
     */
    @Overwrite(remap = false)
    protected final long address(final ByteBuffer buffer) {
        return MemoryUtil.memAddress(buffer);
    }
}
