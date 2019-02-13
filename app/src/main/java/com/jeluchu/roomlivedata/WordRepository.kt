package com.jeluchu.roomlivedata

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import android.os.AsyncTask



class WordRepository(private val wordDao: WordDao) {

    val allWords: LiveData<List<Word>> = wordDao.getAllWords()

    @WorkerThread
    fun insert(word: Word) {
        wordDao.insert(word)
    }


    /* --------------- BORRAR TODOS LOS DATOS -------------- */

    fun deleteAll() {
        deleteAllWordsAsyncTask(wordDao).execute()
    }

    private class deleteAllWordsAsyncTask internal constructor(private val mAsyncTaskDao: WordDao) :
        AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {
            mAsyncTaskDao.deleteAll()
            return null
        }
    }

    /* ---------------- BORRAR UN SOLO DATO ---------------- */

    fun deleteWord(word: Word) {
        deleteWordAsyncTask(wordDao).execute(word)
    }

    private class deleteWordAsyncTask internal constructor(private val mAsyncTaskDao: WordDao) :
        AsyncTask<Word, Void, Void>() {

        override fun doInBackground(vararg params: Word): Void? {
            mAsyncTaskDao.deleteWord(params[0])
            return null
        }
    }

}