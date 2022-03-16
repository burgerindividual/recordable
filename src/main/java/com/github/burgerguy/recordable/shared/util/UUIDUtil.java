package com.github.burgerguy.recordable.shared.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.lmdbjava.Dbi;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class UUIDUtil {

    public static void storeUUIDKeyBufferValue(Dbi<Buffer> database, UUID key, Buffer value) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer uuidBuffer = MemoryUtil.memByteBuffer(memoryStack.longs(key.getMostSignificantBits(), key.getLeastSignificantBits());
            database.put(uuidBuffer, value);
        }
    }
}
