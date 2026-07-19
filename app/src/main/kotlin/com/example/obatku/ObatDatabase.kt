package com.example.obatku

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ObatDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "obatku.db"
        const val DATABASE_VERSION = 1

        // Nama tabel
        const val TABLE_OBAT = "obat"

        // Kolom-kolom tabel
        const val COL_ID = "id"
        const val COL_NAMA = "nama"
        const val COL_WAKTU = "waktu"
        const val COL_JENIS = "jenis"
        const val COL_CATATAN = "catatan"
        const val COL_ATURAN_MAKAN = "aturan_makan"
        const val COL_DOSIS = "dosis"
        const val COL_SUDAH_DIMINUM = "sudah_diminum" // 0 = belum, 1 = sudah
        const val COL_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_OBAT (
                $COL_ID TEXT PRIMARY KEY,
                $COL_NAMA TEXT NOT NULL,
                $COL_WAKTU TEXT,
                $COL_JENIS TEXT,
                $COL_CATATAN TEXT,
                $COL_ATURAN_MAKAN TEXT,
                $COL_DOSIS TEXT,
                $COL_SUDAH_DIMINUM INTEGER DEFAULT 0,
                $COL_TIMESTAMP INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_OBAT")
        onCreate(db)
    }

    // ── INSERT ──────────────────────────────────────────────────────────────
    fun insertObat(obat: ObatModel): Boolean {
        val db = writableDatabase
        val values = obatToContentValues(obat)
        val result = db.insert(TABLE_OBAT, null, values)
        db.close()
        return result != -1L
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────
    fun updateObat(obat: ObatModel): Boolean {
        val db = writableDatabase
        val values = obatToContentValues(obat)
        val rows = db.update(TABLE_OBAT, values, "$COL_ID = ?", arrayOf(obat.id))
        db.close()
        return rows > 0
    }

    // ── UPDATE STATUS SUDAH DIMINUM ─────────────────────────────────────────
    fun updateStatusSudah(id: String, sudah: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SUDAH_DIMINUM, if (sudah) 1 else 0)
        }
        val rows = db.update(TABLE_OBAT, values, "$COL_ID = ?", arrayOf(id))
        db.close()
        return rows > 0
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    fun deleteObat(id: String): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_OBAT, "$COL_ID = ?", arrayOf(id))
        db.close()
        return rows > 0
    }

    // ── GET SINGLE ───────────────────────────────────────────────────────────
    fun getObatById(id: String): ObatModel? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_OBAT, null,
            "$COL_ID = ?", arrayOf(id),
            null, null, null
        )
        var obat: ObatModel? = null
        if (cursor.moveToFirst()) {
            obat = cursorToObat(cursor)
        }
        cursor.close()
        db.close()
        return obat
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────
    fun getAllObat(): MutableList<ObatModel> {
        val list = mutableListOf<ObatModel>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_OBAT, null,
            null, null,
            null, null,
            "$COL_TIMESTAMP ASC"
        )
        while (cursor.moveToNext()) {
            list.add(cursorToObat(cursor))
        }
        cursor.close()
        db.close()
        return list
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private fun obatToContentValues(obat: ObatModel): ContentValues {
        return ContentValues().apply {
            put(COL_ID, obat.id)
            put(COL_NAMA, obat.nama)
            put(COL_WAKTU, obat.waktu)
            put(COL_JENIS, obat.jenis)
            put(COL_CATATAN, obat.catatan)
            put(COL_ATURAN_MAKAN, obat.aturanMakan)
            put(COL_DOSIS, obat.dosis)
            put(COL_SUDAH_DIMINUM, if (obat.isSudahDiminum) 1 else 0)
            put(COL_TIMESTAMP, obat.timestamp)
        }
    }

    private fun cursorToObat(cursor: android.database.Cursor): ObatModel {
        return ObatModel(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)),
            nama = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA)),
            waktu = cursor.getString(cursor.getColumnIndexOrThrow(COL_WAKTU)),
            jenis = cursor.getString(cursor.getColumnIndexOrThrow(COL_JENIS)),
            catatan = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATATAN)),
            aturanMakan = cursor.getString(cursor.getColumnIndexOrThrow(COL_ATURAN_MAKAN)),
            dosis = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOSIS)),
            isSudahDiminum = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SUDAH_DIMINUM)) == 1,
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
        )
    }
}
