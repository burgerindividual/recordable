package com.github.burgerguy.recordable.shared.util;

import com.github.burgerguy.recordable.shared.Recordable;
import com.kenai.jffi.MemoryIO;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * Server-compatible MemoryUtil replacement.
 * Uses MemoryUtil when possible, falls back to MemoryIO and heap buffers in server-only environments.
 */
public class SCMemUtil {

    private static final InternalMemoryUtil INTERNAL_INSTANCE = createInternalInstance();

    private static InternalMemoryUtil createInternalInstance() {
        try {
            // check if MemoryUtil loads correctly
            Class.forName("org.lwjgl.system.MemoryUtil", false, SCMemUtil.class.getClassLoader());
            return new LWJGL();
        } catch (Throwable t) {
            Recordable.LOGGER.info("Unable to use LWJGL MemoryUtil, falling back to JFFI + heap...");
            return new JFFI();
        }
    }

    public static void pushStack() {
        INTERNAL_INSTANCE.pushStack();
    }

    public static ByteBuffer mallocStack(int alignment, int size) {
        return INTERNAL_INSTANCE.mallocStack(alignment, size);
    }

    public static void popStack() {
        INTERNAL_INSTANCE.popStack();
    }

    public static ByteBuffer malloc(int size) {
        return INTERNAL_INSTANCE.malloc(size);
    }

    public static void free(Buffer buffer) {
        INTERNAL_INSTANCE.free(buffer);
    }

    public static long address(ByteBuffer buffer) {
        return INTERNAL_INSTANCE.address(buffer);
    }

    public static ByteBuffer slice(ByteBuffer buffer, int offset, int capacity) {
        return INTERNAL_INSTANCE.slice(buffer, offset, capacity);
    }

    private interface InternalMemoryUtil {
        void pushStack();
        ByteBuffer mallocStack(int alignment, int size);
        void popStack();
        ByteBuffer malloc(int size);
        void free(Buffer buffer);
        ByteBuffer slice(ByteBuffer buffer, int offset, int capacity);
        long address(ByteBuffer buffer);
    }

    private static class LWJGL implements InternalMemoryUtil {

        @Override
        public void pushStack() {
            MemoryStack.stackPush();
        }

        @Override
        public ByteBuffer mallocStack(int alignment, int size) {
            return MemoryStack.stackGet().malloc(alignment, size);
        }

        @Override
        public void popStack() {
            MemoryStack.stackPop();
        }

        @Override
        public ByteBuffer malloc(int size) {
            return MemoryUtil.memAlloc(size);
        }

        @Override
        public void free(Buffer buffer) {
            MemoryUtil.memFree(buffer);
        }

        @Override
        public ByteBuffer slice(ByteBuffer buffer, int offset, int capacity) {
            return MemoryUtil.memSlice(buffer, offset, capacity);
        }

        @Override
        public long address(ByteBuffer buffer) {
            return MemoryUtil.memAddress(buffer);
        }
    }

    private static class JFFI implements InternalMemoryUtil {

        @Override
        public void pushStack() {
            // noop
        }

        @Override
        public ByteBuffer mallocStack(int alignment, int size) {
            return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        }

        @Override
        public void popStack() {
            // noop
        }

        @Override
        public ByteBuffer malloc(int size) {
            return MemoryIO.getInstance().newDirectByteBuffer(MemoryIO.getInstance().allocateMemory(size, false), size).order(ByteOrder.nativeOrder());
        }

        @Override
        public void free(Buffer buffer) {
            MemoryIO.getInstance().freeMemory(MemoryIO.getInstance().getDirectBufferAddress(buffer));
        }

        @Override
        public ByteBuffer slice(ByteBuffer buffer, int offset, int capacity) {
            return buffer.slice(buffer.position() + offset, capacity);
        }

        @Override
        public long address(ByteBuffer buffer) {
            return MemoryIO.getInstance().getDirectBufferAddress(buffer) + buffer.position();
        }
    }
}
