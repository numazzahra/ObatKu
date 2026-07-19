package com.example.obatku

data class ObatModel(
    var id: String? = null,
    var nama: String? = null,
    var waktu: String? = null,
    var jenis: String? = null,
    var catatan: String? = null,
    var aturanMakan: String? = null,
    var dosis: String? = null,
    var isSudahDiminum: Boolean = false,
    var timestamp: Long = 0
)