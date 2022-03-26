package com.github.burgerguy.recordable.server.database;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import org.lmdbjava.*;
import org.lwjgl.system.MemoryStack;

public class ScoreDatabase implements Closeable {
    public static final String DB_NAME = "64 Bit ID to Record Data";

    private final Env<ByteBuffer> dbEnv;
    private final Dbi<ByteBuffer> internalDb;
    private long nextScoreId;

    public ScoreDatabase(Path dbFile) {
        this.dbEnv = Env.create()
                    .setMaxDbs(1)
                    .setMapSize(134217728) // 2^27, 134mb ish
                    .open(dbFile.toFile(), EnvFlags.MDB_WRITEMAP, EnvFlags.MDB_NOSUBDIR);
        // use long keys for performance
        this.internalDb = dbEnv.openDbi(DB_NAME, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY);

        try (Txn<ByteBuffer> txn = dbEnv.txnRead()) {
            this.nextScoreId = internalDb.stat(txn).entries;
        }
    }

    /**
     * The buffer provided should be in big endian
     */
    public long storeScore(ByteBuffer value) {
        long id = nextScoreId;
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer idBuffer = memoryStack.malloc(8, 8).putLong(nextScoreId).flip();
            internalDb.put(idBuffer, value);
        }
        nextScoreId++;
        return id;
    }

    /**
     * If the entry doesn't exist, the data field will be null.
     */
    public ScoreRequest requestScore(long scoreId) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            // not in try-with-resources because returned value should close it
            Txn<ByteBuffer> readTxn = dbEnv.txnRead();
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer idBuffer = memoryStack.malloc(8, 8).putLong(scoreId).flip();

            ByteBuffer data = internalDb.get(readTxn, idBuffer).order(ByteOrder.BIG_ENDIAN);
            return new ScoreRequest(data, readTxn);
        }
    }

    public boolean deleteScore(long scoreId) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer idBuffer = memoryStack.malloc(8, 8).putLong(scoreId).flip();
            return internalDb.delete(idBuffer);
        }
    }

    @Override
    public void close() {
        dbEnv.close();
    }

    public static class ScoreRequest implements AutoCloseable {
        private final ByteBuffer data;
        private final Txn<ByteBuffer> txn;

        private ScoreRequest(ByteBuffer data, Txn<ByteBuffer> txn) {
            this.data = data;
            this.txn = txn;
        }

        public ByteBuffer getData() {
            return data;
        }

        @Override
        public void close() {
            txn.close();
        }
    }
}
