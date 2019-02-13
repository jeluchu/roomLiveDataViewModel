package com.jeluchu.roomlivedata

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface WordDao {

    // MOSTRAR DATOS Y ORDENARLOS
    @Query("SELECT * from word_table ORDER BY word ASC")
    fun getAllWords(): LiveData<List<Word>>

    // AÑADIR DATOS
    @Insert
    fun insert(word: Word)

    // ELIMINAR ALL DATA
    @Query("DELETE FROM word_table")
    fun deleteAll()

    // ACTUALIZAR DATOS
    @Update
    fun update(word: Word)

    // BORRAR ITEM
    @Delete
    fun deleteWord(word: Word)
}