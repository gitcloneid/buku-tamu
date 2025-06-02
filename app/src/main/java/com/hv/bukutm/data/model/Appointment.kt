package com.hv.bukutm.domain.model

data class Appointment(
    val idJanjiTemu: Int,
    val tanggal: String,
    val waktu: String,
    val status: String,
    val keperluan: String,
    val kodeQr: String,
    val tamu: Tamu,
    val guru: User
)

data class Tamu(
    val idTamu: Int,
    val nama: String,
    val telepon: String
)