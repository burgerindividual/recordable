package com.github.burgerguy.recordable.server.database;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import org.lmdbjava.*;
import org.lwjgl.system.MemoryStack;

public class RecordDatabase implements Closeable {
    public static final String DB_NAME = "64 Bit ID to Record Data";

    private final Env<ByteBuffer> dbEnv;
    private final Dbi<ByteBuffer> internalDb;
    private long nextId;

    public RecordDatabase(Path dbFile) {
        this.dbEnv = Env.create()
                    .setMaxDbs(1)
                    .setMapSize(134217728) // 2^27, 134mb ish
                    .open(dbFile.toFile(), EnvFlags.MDB_WRITEMAP, EnvFlags.MDB_NOSUBDIR);
        // use long keys for performance
        this.internalDb = dbEnv.openDbi(DB_NAME, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY);

        try (Txn<ByteBuffer> txn = dbEnv.txnRead()) {
            this.nextId = internalDb.stat(txn).entries;
        }
    }

    public long storeRecord(ByteBuffer value) {
        long id = nextId;
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer idBuffer = memoryStack.malloc(8, 8).putLong(nextId).flip();
            internalDb.put(idBuffer, value);
        }
        nextId++;
        return id;
    }

    public ByteBuffer getRecord(long id) {
        try (MemoryStack memoryStack = MemoryStack.stackPush();
            Txn<ByteBuffer> readTxn = dbEnv.txnRead()) {
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer idBuffer = memoryStack.malloc(8, 8).putLong(nextId);
            return internalDb.get(readTxn, idBuffer);
        }
    }

    public boolean deleteRecord(long id) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer idBuffer = memoryStack.malloc(8, 8).putLong(nextId);
            return internalDb.delete(idBuffer);
        }
    }

    @Override
    public void close() {
        dbEnv.close();
    }
}
