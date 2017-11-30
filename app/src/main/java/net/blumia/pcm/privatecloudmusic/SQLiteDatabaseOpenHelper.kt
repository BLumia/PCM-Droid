package net.blumia.pcm.privatecloudmusic

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

/**
 * Created by wzc78 on 2017/11/26.
 */

class SQLiteDatabaseOpenHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "PCM_SRV_DB", null, 1) {

    companion object {

        const val DB_TABLE_SRV_LIST : String = "SrvList"

        private var instance: SQLiteDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): SQLiteDatabaseOpenHelper {
            if (instance == null) {
                instance = SQLiteDatabaseOpenHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        db.createTable(DB_TABLE_SRV_LIST, true,
                "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                "name" to TEXT,
                "api_url" to TEXT,
                "file_root_url" to TEXT,
                "password" to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable(DB_TABLE_SRV_LIST, true)
    }
}

// Access property for Context
val Context.database: SQLiteDatabaseOpenHelper
    get() = SQLiteDatabaseOpenHelper.getInstance(applicationContext)