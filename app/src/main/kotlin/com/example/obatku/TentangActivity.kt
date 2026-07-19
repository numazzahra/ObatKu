package com.example.obatku

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TentangActivity : AppCompatActivity() {
    private var btnBack: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tentang)

        btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack!!.setOnClickListener(View.OnClickListener { v: View? -> finish() })

        setupNavbar()
    }

    private fun setupNavbar() {
        // Nav 1: Beranda
        findViewById<View?>(R.id.navBeranda).setOnClickListener(View.OnClickListener { v: View? ->
            val intent: Intent =
                Intent(this@TentangActivity, com.example.obatku.MainActivity::class.java)
            startActivity(intent)
            finish()
        })

        // Nav 2: Tambah
        findViewById<View?>(R.id.navTambah).setOnClickListener(View.OnClickListener { v: View? ->
            val intent: Intent =
                Intent(this@TentangActivity, com.example.obatku.TambahObatActivity::class.java)
            startActivity(intent)
            finish()
        })

        // Nav 3: Detail
        findViewById<View?>(R.id.navDetail).setOnClickListener(View.OnClickListener { v: View? ->
            Toast.makeText(
                this@TentangActivity,
                "Klik salah satu obat untuk melihat detail",
                Toast.LENGTH_SHORT
            ).show()
        })

        // Nav 4: Tentang (sedang di halaman ini)
        findViewById<View?>(R.id.navTentang).setOnClickListener(View.OnClickListener { v: View? ->
            // sudah di halaman tentang
            Toast.makeText(
                this@TentangActivity,
                "Anda sudah di halaman Tentang",
                Toast.LENGTH_SHORT
            ).show()
        })
    }
}