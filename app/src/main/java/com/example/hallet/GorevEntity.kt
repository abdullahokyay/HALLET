package com.example.hallet

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gorevler")
data class GorevEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val baslik: String,
    val aciklama: String = "",
    val tarihSaatMillis: Long? = null,
    val bildirimAktifMi: Boolean = false,
    val tamamlandiMi: Boolean = false
)