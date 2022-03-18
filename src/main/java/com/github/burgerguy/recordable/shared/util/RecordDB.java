package com.github.burgerguy.recordable.shared.util;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import org.lmdbjava.*;
import org.lwjgl.system.MemoryStack;

public class RecordDB implements Closeable {
    public static final String DB_NAME = "ID64-RECORDBIN";

    private final Env<ByteBuffer> dbEnv;
    private final Dbi<ByteBuffer> internalDb;
    private long nextId;

    public RecordDB(Path dbFile) {
        this.dbEnv = Env.create()
                    .setMaxDbs(1)
                    .setMapSize(134217728) // 2^27, 134mb ish
                    .open(dbFile.toFile(), EnvFlags.MDB_WRITEMAP);
        // use long keys for performance
        this.internalDb = dbEnv.openDbi(DB_NAME, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY);

        try (Txn<ByteBuffer> txn = dbEnv.txnRead()) {
            this.nextId = internalDb.stat(txn).entries;
        }
    }

    public void storeRecord(ByteBuffer value) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer uuidBuffer = memoryStack.malloc(8, 8);
            uuidBuffer.putLong(nextId++);
            internalDb.put(uuidBuffer, value);
        }
    }

    @Override
    public void close() {
        dbEnv.close();
    }
}
