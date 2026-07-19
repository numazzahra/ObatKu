package com.example.obatku

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class TambahObatActivity : AppCompatActivity() {

    private var etNamaObat: EditText? = null
    private var etDosis: EditText? = null
    private var etCatatan: EditText? = null
    private var spinnerWaktu: Spinner? = null
    private var timePicker: TimePicker? = null
    private var btnSimpan: Button? = null
    private var btnBack: ImageButton? = null

    private var isEditMode = false
    private var editObatId: String? = null
    private var editObatSudahDiminum = false

    // SQLite database helper
    private lateinit var db: ObatDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_obat)

        db = ObatDatabase(this)

        initViews()
        setupSpinner()
        setupNavbar()
        checkEditMode()

        btnSimpan?.setOnClickListener { saveObat() }
        btnBack?.setOnClickListener { finish() }
    }

    private fun checkEditMode() {
        if (intent.getBooleanExtra("IS_EDIT", false)) {
            isEditMode = true
            editObatId = intent.getStringExtra("OBAT_ID")
            editObatSudahDiminum = intent.getBooleanExtra("OBAT_SUDAH", false)

            findViewById<android.widget.TextView>(R.id.tvTitle)?.text = "Edit Obat"
            btnSimpan?.text = "Update Obat"

            etNamaObat?.setText(intent.getStringExtra("OBAT_NAMA"))

            val dosisStr = intent.getStringExtra("OBAT_DOSIS") ?: ""
            etDosis?.setText(dosisStr.replace(" Kapsul", "").replace(" Tablet", "").trim())

            etCatatan?.setText(intent.getStringExtra("OBAT_CATATAN"))

            // Set spinner sesuai aturan makan
            val aturan = intent.getStringExtra("OBAT_ATURAN") ?: ""
            val spinnerItems = resources.getStringArray(R.array.waktu_konsumsi_array)
            for (i in spinnerItems.indices) {
                if (aturan.contains("Malam", ignoreCase = true) && spinnerItems[i].contains("Malam")) {
                    spinnerWaktu?.setSelection(i)
                    break
                }
            }

            // Set TimePicker sesuai waktu tersimpan
            val waktu = intent.getStringExtra("OBAT_WAKTU")
            if (!waktu.isNullOrEmpty() && waktu.contains(":")) {
                try {
                    val parts = waktu.split(":")
                    val hour = parts[0].trim().toInt()
                    val minute = parts[1].trim().toInt()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        timePicker?.hour = hour
                        timePicker?.minute = minute
                    } else {
                        @Suppress("DEPRECATION")
                        timePicker?.currentHour = hour
                        @Suppress("DEPRECATION")
                        timePicker?.currentMinute = minute
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initViews() {
        etNamaObat = findViewById(R.id.etNamaObat)
        etDosis = findViewById(R.id.etDosis)
        etCatatan = findViewById(R.id.etCatatan)
        spinnerWaktu = findViewById(R.id.spinnerWaktu)
        timePicker = findViewById(R.id.timePicker)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.waktu_konsumsi_array, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWaktu?.adapter = adapter
    }

    private fun setupNavbar() {
        findViewById<View>(R.id.navBeranda).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.navTambah).setOnClickListener {
            Toast.makeText(this, "Anda sudah di halaman Tambah Obat", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.navDetail).setOnClickListener {
            Toast.makeText(this, "Klik salah satu obat untuk melihat detail", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.navTentang).setOnClickListener {
            startActivity(Intent(this, TentangActivity::class.java))
            finish()
        }
    }

    private val timeFromPicker: String
        get() {
            val hour: Int
            val minute: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = timePicker!!.hour
                minute = timePicker!!.minute
            } else {
                @Suppress("DEPRECATION")
                hour = timePicker!!.currentHour
                @Suppress("DEPRECATION")
                minute = timePicker!!.currentMinute
            }
            return String.format("%02d:%02d", hour, minute)
        }

    private val aturanMakan: String
        get() {
            val selected = spinnerWaktu?.selectedItem?.toString() ?: ""
            return if (selected.contains("Malam", ignoreCase = true)) "Sesudah Makan" else "Sebelum Makan"
        }

    private fun saveObat() {
        val nama = etNamaObat?.text.toString().trim()
        val dosis = etDosis?.text.toString().trim()
        val catatan = etCatatan?.text.toString().trim()

        if (nama.isEmpty()) {
            etNamaObat?.error = "Nama obat wajib diisi"
            return
        }
        if (dosis.isEmpty()) {
            etDosis?.error = "Dosis wajib diisi"
            return
        }

        val waktu = timeFromPicker
        val aturanMakan = aturanMakan
        val jenis = if (dosis.contains("Kapsul", ignoreCase = true)) "Kapsul" else "Tablet"
        val catatanFinal = if (catatan.isEmpty()) "-" else catatan

        if (isEditMode) {
            // UPDATE data di SQLite
            val updatedObat = ObatModel(
                id = editObatId ?: UUID.randomUUID().toString(),
                nama = nama,
                waktu = waktu,
                jenis = jenis,
                catatan = catatanFinal,
                aturanMakan = aturanMakan,
                dosis = dosis,
                isSudahDiminum = editObatSudahDiminum,
                timestamp = System.currentTimeMillis()
            )
            db.updateObat(updatedObat)
            Toast.makeText(this, "Obat berhasil diupdate", Toast.LENGTH_SHORT).show()

            // Reschedule notifikasi
            scheduleNotification(updatedObat.id ?: "", nama, waktu)
        } else {
            // INSERT data baru ke SQLite
            val newId = UUID.randomUUID().toString()
            val newObat = ObatModel(
                id = newId,
                nama = nama,
                waktu = waktu,
                jenis = jenis,
                catatan = catatanFinal,
                aturanMakan = aturanMakan,
                dosis = dosis,
                isSudahDiminum = false,
                timestamp = System.currentTimeMillis()
            )
            db.insertObat(newObat)
            Toast.makeText(this, "Obat berhasil ditambahkan", Toast.LENGTH_SHORT).show()

            // Schedule notifikasi
            scheduleNotification(newId, nama, waktu)
        }

        finish()
    }

    private fun scheduleNotification(id: String, nama: String, waktuStr: String) {
        try {
            val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("OBAT_ID", id)
                putExtra("OBAT_NAMA", nama)
                putExtra("OBAT_WAKTU", waktuStr)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                this, id.hashCode(), intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val parts = waktuStr.split(":")
            val hour = parts[0].trim().toInt()
            val minute = parts[1].trim().toInt()

            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                if (before(java.util.Calendar.getInstance())) {
                    add(java.util.Calendar.DATE, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
