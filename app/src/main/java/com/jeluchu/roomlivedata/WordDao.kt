package com.jeluchu.roomlivedata

import androidx.lifecycle.LiveData
import androidx.room.*

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
    //Sin Query
    @Update
    fun update(word: Word)
 
    //Con Query
    @Query("UPDATE word_table SET word = :word WHERE id == :id")
    fun updateItem(word: String, id: Int)

    // BORRAR ITEM
    @Delete
    fun deleteWord(word: Word)
}