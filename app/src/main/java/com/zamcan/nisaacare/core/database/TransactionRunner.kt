package com.zamcan.nisaacare.core.database

import android.database.sqlite.SQLiteDatabase

object TransactionRunner {
    fun <T> run(db: SQLiteDatabase, block: () -> T): T {
        db.beginTransaction()
        return try {
            val result = block()
            db.setTransactionSuccessful()
            result
        } finally {
            db.endTransaction()
        }
    }
}
