package com.example.obatku

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ObatAdapter(
    private val context: Context,
    obatList: MutableList<com.example.obatku.ObatModel>
) : RecyclerView.Adapter<ObatAdapter.ViewHolder?>() {
    private var obatList: MutableList<com.example.obatku.ObatModel>
    private var deleteListener: OnItemDeleteListener? = null

    // Interface untuk delete
    interface OnItemDeleteListener {
        fun onDelete(position: Int)
    }

    init {
        this.obatList = obatList
    }

    fun setOnItemDeleteListener(listener: OnItemDeleteListener?) {
        this.deleteListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_obat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obat: com.example.obatku.ObatModel = obatList.get(position)

        holder.tvNamaObat.text = obat.nama
        holder.tvWaktu.text = obat.waktu + " • " + obat.aturanMakan
        holder.tvJenis.text = "Jenis: " + obat.jenis

        if (obat.catatan != null && obat.catatan!!.isNotEmpty() && obat.catatan != "-") {
            holder.tvCatatan.text = "Catatan: " + obat.catatan
            holder.tvCatatan.visibility = View.VISIBLE
        } else {
            holder.tvCatatan.visibility = View.GONE
        }

        // Set badge status
        if (obat.isSudahDiminum) {
            holder.tvStatus.text = "Sudah"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_sudah)
            setTextColorCompat(holder.tvStatus, R.color.status_sudah_text)
        } else {
            holder.tvStatus.text = "Belum"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_belum)
            setTextColorCompat(holder.tvStatus, R.color.status_belum_text)
        }

        // Click listener untuk buka detail
        holder.cardView.setOnClickListener {
            val intent = Intent(context, com.example.obatku.DetailObatActivity::class.java)
            intent.putExtra("OBAT_ID", obat.id)
            intent.putExtra("OBAT_NAMA", obat.nama)
            intent.putExtra("OBAT_WAKTU", obat.waktu)
            intent.putExtra("OBAT_JENIS", obat.jenis)
            intent.putExtra("OBAT_CATATAN", obat.catatan)
            intent.putExtra("OBAT_ATURAN", obat.aturanMakan)
            intent.putExtra("OBAT_DOSIS", obat.dosis)
            intent.putExtra("OBAT_SUDAH", obat.isSudahDiminum)
            context.startActivity(intent)
        }
    }

    private fun setTextColorCompat(textView: TextView, colorResId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextColor(context.getColor(colorResId))
        } else {
            textView.setTextColor(context.getResources().getColor(colorResId))
        }
    }

    fun removeItem(position: Int) {
        if (deleteListener != null) {
            deleteListener!!.onDelete(position)
        }
    }

    override fun getItemCount(): Int {
        return obatList.size
    }

    fun updateList(newList: MutableList<com.example.obatku.ObatModel>) {
        this.obatList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardView: CardView
        var tvNamaObat: TextView
        var tvStatus: TextView
        var tvWaktu: TextView
        var tvJenis: TextView
        var tvCatatan: TextView

        init {
            cardView = itemView as CardView
            tvNamaObat = itemView.findViewById<TextView>(R.id.tvNamaObat)
            tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            tvWaktu = itemView.findViewById<TextView>(R.id.tvWaktu)
            tvJenis = itemView.findViewById<TextView>(R.id.tvJenis)
            tvCatatan = itemView.findViewById<TextView>(R.id.tvCatatan)
        }
    }
}