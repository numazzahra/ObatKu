package com.example.obatku

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var rvObat: RecyclerView? = null
    private var adapter: ObatAdapter? = null
    private var obatList: MutableList<ObatModel> = mutableListOf()
    private var progressBar: ProgressBar? = null
    private var tvProgressText: TextView? = null
    private var tvDate: TextView? = null
    private var fabTambah: FloatingActionButton? = null
    private var emptyStateLayout: LinearLayout? = null

    // SQLite database helper
    private lateinit var db: ObatDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = ObatDatabase(this)

        initViews()
        setupRecyclerView()
        setupFab()
        setupNavbar()
        loadDataFromDatabase()
        updateProgressAndDate()
        updateRealTimeDate()
        setupSwipeToDelete()
    }

    private fun initViews() {
        rvObat = findViewById(R.id.rvObatList)
        progressBar = findViewById(R.id.progressBar)
        tvProgressText = findViewById(R.id.tvProgressText)
        tvDate = findViewById(R.id.tvDate)
        fabTambah = findViewById(R.id.fabTambahObat)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
    }

    private fun setupRecyclerView() {
        adapter = ObatAdapter(this, obatList)
        rvObat!!.layoutManager = LinearLayoutManager(this)
        rvObat!!.adapter = adapter
    }

    private fun updateEmptyState() {
        if (obatList.isEmpty()) {
            rvObat?.visibility = View.GONE
            emptyStateLayout?.visibility = View.VISIBLE
        } else {
            rvObat?.visibility = View.VISIBLE
            emptyStateLayout?.visibility = View.GONE
        }
    }

    private fun updateRealTimeDate() {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        tvDate?.text = sdf.format(Date())
    }

    private fun setupSwipeToDelete() {
        val simpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val obatToDelete = obatList[position]

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Hapus Obat")
                    .setMessage("Apakah Anda yakin ingin menghapus ${obatToDelete.nama}?")
                    .setPositiveButton("Hapus") { _: DialogInterface, _: Int ->
                        deleteObat(position)
                    }
                    .setNegativeButton("Batal") { _: DialogInterface, _: Int ->
                        adapter?.notifyItemChanged(position)
                        updateEmptyState()
                    }
                    .show()
            }
        }
        ItemTouchHelper(simpleCallback).attachToRecyclerView(rvObat)
    }

    private fun deleteObat(position: Int) {
        val obat = obatList[position]
        // Hapus dari SQLite
        db.deleteObat(obat.id ?: return)
        obatList.removeAt(position)
        adapter?.notifyItemRemoved(position)
        updateProgressAndDate()
        updateEmptyState()
        Toast.makeText(this, "${obat.nama} telah dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun setupFab() {
        fabTambah?.setOnClickListener {
            startActivity(Intent(this, TambahObatActivity::class.java))
        }
    }

    private fun setupNavbar() {
        findViewById<View>(R.id.navBeranda).setOnClickListener {
            loadDataFromDatabase()
            updateProgressAndDate()
            updateRealTimeDate()
            updateEmptyState()
        }
        findViewById<View>(R.id.navTambah).setOnClickListener {
            startActivity(Intent(this, TambahObatActivity::class.java))
        }
        findViewById<View>(R.id.navDetail).setOnClickListener {
            Toast.makeText(this, "Klik salah satu obat untuk melihat detail", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.navTentang).setOnClickListener {
            startActivity(Intent(this, TentangActivity::class.java))
        }
    }

    // ── Ambil semua data dari SQLite ────────────────────────────────────────
    private fun loadDataFromDatabase() {
        obatList = db.getAllObat()
        adapter?.updateList(obatList)
        updateEmptyState()
    }

    private fun updateProgressAndDate() {
        val sudah = obatList.count { it.isSudahDiminum }
        val total = obatList.size
        val progress = if (total > 0) (sudah * 100 / total) else 0

        progressBar?.progress = progress

        tvProgressText?.text = if (total == 0) {
            "Belum ada obat. Silakan tambah obat baru."
        } else {
            "$sudah dari $total obat sudah diminum tepat waktu."
        }
    }

    override fun onResume() {
        super.onResume()
        loadDataFromDatabase()
        handleIntentAction()
        updateProgressAndDate()
        updateRealTimeDate()
        updateEmptyState()
    }

    private fun handleIntentAction() {
        if (intent.hasExtra("MARK_SUDAH")) {
            val obatId = intent.getStringExtra("MARK_SUDAH")
            if (!obatId.isNullOrEmpty()) {
                // Update status di SQLite
                db.updateStatusSudah(obatId, true)
                // Update list lokal
                obatList.forEach { if (it.id == obatId) it.isSudahDiminum = true }
                adapter?.notifyDataSetChanged()
                Toast.makeText(this, "Obat ditandai sudah diminum", Toast.LENGTH_SHORT).show()
                intent.removeExtra("MARK_SUDAH")
            }
        }
    }
}
