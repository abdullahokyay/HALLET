package com.example.hallet

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GorevDao {

    @Query("SELECT * FROM gorevler ORDER BY tamamlandiMi ASC, tarihSaatMillis ASC, id DESC")
    fun tumGorevleriGetir(): Flow<List<GorevEntity>>

    @Insert
    suspend fun gorevEkle(gorev: GorevEntity): Long

    @Update
    suspend fun gorevGuncelle(gorev: GorevEntity)

    @Delete
    suspend fun gorevSil(gorev: GorevEntity)
}