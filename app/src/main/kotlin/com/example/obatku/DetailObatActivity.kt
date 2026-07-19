package com.example.obatku

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DetailObatActivity : AppCompatActivity() {

    private var tvNamaObat: TextView? = null
    private var tvDosisJenis: TextView? = null
    private var tvWaktu: TextView? = null
    private var tvAturan: TextView? = null
    private var tvDosis: TextView? = null
    private var tvHari: TextView? = null
    private var tvCatatan: TextView? = null
    private var tvStatusSudah: TextView? = null
    private var tvStatusBelum: TextView? = null
    private var btnToggleStatus: Button? = null
    private var btnEdit: Button? = null
    private var btnDelete: Button? = null
    private var btnBack: ImageButton? = null

    private var obatId: String? = null
    private var currentStatus = false

    // SQLite database helper
    private lateinit var db: ObatDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_obat)

        db = ObatDatabase(this)

        initViews()

        obatId = intent.getStringExtra("OBAT_ID")
        val nama = intent.getStringExtra("OBAT_NAMA")
        val waktu = intent.getStringExtra("OBAT_WAKTU")
        val jenis = intent.getStringExtra("OBAT_JENIS")
        val catatan = intent.getStringExtra("OBAT_CATATAN")
        val aturan = intent.getStringExtra("OBAT_ATURAN")
        val dosis = intent.getStringExtra("OBAT_DOSIS")
        currentStatus = intent.getBooleanExtra("OBAT_SUDAH", false)

        tvNamaObat?.text = nama
        tvDosisJenis?.text = "$dosis - $jenis"
        tvWaktu?.text = waktu
        tvAturan?.text = aturan
        tvDosis?.text = dosis
        tvHari?.text = "Senin - Minggu"
        tvCatatan?.text = catatan

        updateStatusUI()

        btnToggleStatus?.setOnClickListener { toggleStatus() }
        btnBack?.setOnClickListener { finish() }
        btnEdit?.setOnClickListener { startEditMode() }
        btnDelete?.setOnClickListener { confirmDelete() }

        tvStatusSudah?.setOnClickListener { if (!currentStatus) toggleStatus() }
        tvStatusBelum?.setOnClickListener { if (currentStatus) toggleStatus() }
    }

    private fun initViews() {
        tvNamaObat = findViewById(R.id.tvNamaObat)
        tvDosisJenis = findViewById(R.id.tvDosisJenis)
        tvWaktu = findViewById(R.id.tvWaktu)
        tvAturan = findViewById(R.id.tvAturan)
        tvDosis = findViewById(R.id.tvDosis)
        tvHari = findViewById(R.id.tvHari)
        tvCatatan = findViewById(R.id.tvCatatan)
        tvStatusSudah = findViewById(R.id.tvStatusSudah)
        tvStatusBelum = findViewById(R.id.tvStatusBelum)
        btnToggleStatus = findViewById(R.id.btnToggleStatus)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun updateStatusUI() {
        if (currentStatus) {
            tvStatusSudah?.setBackgroundResource(R.drawable.bg_badge_sudah)
            tvStatusSudah?.setTextColor(getColor(R.color.status_sudah_text))
            tvStatusBelum?.setBackgroundResource(R.drawable.bg_badge_inactive)
            tvStatusBelum?.setTextColor(getColor(R.color.white_darker))
            btnToggleStatus?.text = "Tandai Belum Diminum"
        } else {
            tvStatusSudah?.setBackgroundResource(R.drawable.bg_badge_inactive)
            tvStatusSudah?.setTextColor(getColor(R.color.white_darker))
            tvStatusBelum?.setBackgroundResource(R.drawable.bg_badge_belum)
            tvStatusBelum?.setTextColor(getColor(R.color.status_belum_text))
            btnToggleStatus?.text = "Tandai Sudah Diminum"
        }
    }

    private fun startEditMode() {
        val intent = android.content.Intent(this, TambahObatActivity::class.java).apply {
            putExtra("IS_EDIT", true)
            putExtra("OBAT_ID", obatId)
            putExtra("OBAT_NAMA", tvNamaObat?.text.toString())
            putExtra("OBAT_WAKTU", tvWaktu?.text.toString())
            putExtra("OBAT_JENIS", tvDosisJenis?.text.toString().split(" - ").getOrNull(1) ?: "Tablet")
            putExtra("OBAT_CATATAN", tvCatatan?.text.toString())
            putExtra("OBAT_ATURAN", tvAturan?.text.toString())
            putExtra("OBAT_DOSIS", tvDosis?.text.toString())
            putExtra("OBAT_SUDAH", currentStatus)
        }
        startActivity(intent)
        finish()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Obat")
            .setMessage("Apakah Anda yakin ingin menghapus ${tvNamaObat?.text}?")
            .setPositiveButton("Hapus") { _: DialogInterface, _: Int ->
                deleteObatFromDatabase()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteObatFromDatabase() {
        val id = obatId ?: return
        val berhasil = db.deleteObat(id)
        if (berhasil) {
            Toast.makeText(this, "Obat berhasil dihapus", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Gagal menghapus obat", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun toggleStatus() {
        val id = obatId ?: return
        currentStatus = !currentStatus

        // Update status di SQLite
        db.updateStatusSudah(id, currentStatus)
        updateStatusUI()

        val message = if (currentStatus) "Obat ditandai SUDAH diminum" else "Obat ditandai BELUM diminum"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
