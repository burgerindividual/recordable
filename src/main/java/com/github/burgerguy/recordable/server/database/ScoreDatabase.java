package com.github.burgerguy.recordable.server.database;

import com.github.burgerguy.recordable.shared.util.SCMemUtil;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import org.lmdbjava.*;

public class ScoreDatabase implements Closeable {
    public static final String DB_NAME = "64 Bit ID to Record Data";

    private final Env<ByteBuffer> dbEnv;
    private final Dbi<ByteBuffer> internalDb;
    private long nextScoreId;

    public ScoreDatabase(Path dbFile) {
        this.dbEnv = Env.create()
                    .setMaxDbs(1)
                    .setMapSize(67108864) // 2^26, 64MiB
                    .open(dbFile.toFile(), EnvFlags.MDB_WRITEMAP, EnvFlags.MDB_NOSUBDIR);
        // use long keys for performance
        this.internalDb = this.dbEnv.openDbi(DB_NAME, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY);

        try (Txn<ByteBuffer> txn = this.dbEnv.txnRead()) {
            this.nextScoreId = this.internalDb.stat(txn).entries;
        }
    }

    /**
     * The buffer provided should be in big endian
     */
    public long storeScore(ByteBuffer value) {
        long id = this.nextScoreId;
        try {
            SCMemUtil.pushStack();
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer idBuffer = SCMemUtil.mallocStack(8, 8).putLong(this.nextScoreId).flip();
            this.internalDb.put(idBuffer, value);
        } finally {
            SCMemUtil.popStack();
        }
        this.nextScoreId++;
        return id;
    }

    /**
     * If the entry doesn't exist, the data field will be null.
     */
    public ScoreRequest requestScore(long scoreId) {
        try {
            SCMemUtil.pushStack();
            // not in try-with-resources because returned value should close it
            Txn<ByteBuffer> readTxn = this.dbEnv.txnRead();
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer idBuffer = SCMemUtil.mallocStack(8, 8).putLong(scoreId).flip();

            ByteBuffer data = this.internalDb.get(readTxn, idBuffer).order(ByteOrder.BIG_ENDIAN);
            return new ScoreRequest(data, readTxn);
        } finally {
            SCMemUtil.popStack();
        }
    }

    public boolean deleteScore(long scoreId) {
        try {
            SCMemUtil.pushStack();
            // LMDB usually expects big endian, but because we're using direct long keys, we can keep it as native
            ByteBuffer idBuffer = SCMemUtil.mallocStack(8, 8).putLong(scoreId).flip();
            return this.internalDb.delete(idBuffer);
        } finally {
            SCMemUtil.popStack();
        }
    }

    @Override
    public void close() {
        this.dbEnv.close();
    }

    public static class ScoreRequest implements AutoCloseable {
        private final ByteBuffer data;
        private final Txn<ByteBuffer> txn;

        private ScoreRequest(ByteBuffer data, Txn<ByteBuffer> txn) {
            this.data = data;
            this.txn = txn;
        }

        public ByteBuffer getData() {
            return this.data;
        }

        @Override
        public void close() {
            this.txn.close();
        }
    }
}
