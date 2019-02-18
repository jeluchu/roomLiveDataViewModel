# ROOM LIVE DATA VIEW MODEL
[![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15)
[![Download](https://img.shields.io/badge/Kotlin-1.3.20-brightgreen.svg?style=flat&logo=kotlin)](https://kotlinlang.org/docs/reference/whatsnew13.html)
[![Download](https://img.shields.io/badge/Gradle-4.10.1-brightgreen.svg?style=flat&logo=android)](https://services.gradle.org/distributions/gradle-4.10.1-all.zip)
[![API](https://img.shields.io/badge/J%C3%A9luchu-1.0.0-blue.svg?style=flat&logo=ello)](https://play.google.com/store/apps/dev?id=7449422814338081261&hl=es_ES)

### FEATURES OF ROOM DATABASE APP

 - [x] Read Data
 - [x] Add Data
 - [x] Delete All Data
 - [x] Delete One Item Data
 - [x] Edit Data
 - [x] Swipe Actions

### INTRODUCTION
The purpose of [Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html) is to provide guidance on app architecture, with libraries for common tasks like lifecycle management and data persistence. That's why to facilitate it I made an example in Kotlin 100%.

In your  `build.gradle`  (Module: app) make the following changes:

**On top:**
```
apply plugin: 'kotlin-kapt'
```
**In the dependencies:**

```
// ROOM COMPONENTS
implementation "android.arch.persistence.room:runtime:1.1.1"
kapt "android.arch.persistence.room:compiler:1.1.1"
androidTestImplementation "android.arch.persistence.room:testing:1.1.1"

// LIFECYCLE COMPONENTS
implementation "android.arch.lifecycle:extensions:1.1.1"
kapt "android.arch.lifecycle:compiler:1.1.1"

// COROUTINES
api "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1"
api "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1"
```


### WEATHER SERVICE

In the **WeatherService** (interface), you GET de data, doing the query to the data that you need
```
@GET("data/2.5/weather?")
fun getCurrentWeatherData(@Query("lat") lat: String, @Query("lon") lon: String, @Query("APPID") app_id: String): Call<WeatherResponse>
```

**¡Not all API's have the same pattern, so keep that in mind!**


### WORD CLASS

To make the  `Word`  class meaningful to a Room database, you need to annotate it. Annotations identify how each part of this class relates to an entry in the database. Room uses this information to generate code.
```
@Entity(tableName = "word_table")
class Word(@PrimaryKey @ColumnInfo(name = "word") val word: String)
```

### WORDAO INTERFACE

The DAO for this example is basic and provides queries for getting all the words, inserting a word, and deleting all the words.

```
@Dao
interface WordDao {

@Query("SELECT * from word_table ORDER BY word ASC")
fun getAllWords(): LiveData<List<Word>>

@Insert
fun insert(word: Word)

@Query("DELETE FROM word_table")
fun deleteAll()
}
```


### WORDROOMDATABASE CLASS

This Room database class must be abstract and extend `RoomDatabase`. Usually, you only need one instance of a Room database for the whole app.

```
@Database(entities = [Word::class], version = 1, exportSchema = false)
abstract class WordRoomDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: WordRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WordRoomDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        WordRoomDatabase::class.java,
                        "word_database"
                )

                        .fallbackToDestructiveMigration()
                        .addCallback(WordDatabaseCallback(scope))
                        .build()
                INSTANCE = instance
                instance
            }
        }

        private class WordDatabaseCallback(
                private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)

                INSTANCE?.let {
                    scope.launch(Dispatchers.IO) {
                    }
                }
            }
        }

    }

}

```

### WORDREPOSITORY CLASS

A `Repository` class abstracts access to multiple data sources. It use for manages queries and allows you to use multiple backends. In the most common example, the Repository implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
```
class WordRepository(private val wordDao: WordDao) {

    val allWords: LiveData<List<Word>> = wordDao.getAllWords()

    @WorkerThread
    suspend fun insert(word: Word) {
        wordDao.insert(word)
    }
}
```

### WORDVIEWMODEL CLASS

A `ViewModel` holds your app's UI data in a lifecycle-conscious way that survives configuration changes. Separating your app's UI data from your `Activity` and `Fragment` classes lets you better follow the single responsibility principle: Your activities and fragments are responsible for drawing data to the screen, while your `ViewModel` can take care of holding and processing all the data needed for the UI.

```
class WordViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private val repository: WordRepository
    val allWords: LiveData<List<Word>>

    init {
        val wordsDao = WordRoomDatabase.getDatabase(application, scope).wordDao()
        repository = WordRepository(wordsDao)
        allWords = repository.allWords
    }

    fun insert(word: Word) = scope.launch(Dispatchers.IO) {
        repository.insert(word)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
```

### WORDLISTADAPTER CLASS

You are going to display the data in a `RecyclerView`, which is a little nicer than just throwing the data in a `TextView` and `ImageView`.

```
class WordListAdapter internal constructor(context: Context) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var words = emptyList<Word>() // Cached copy of words

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordItemView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_word, parent, false)
        return WordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val current = words[position]
        holder.wordItemView.text = current.word
    }

    internal fun setWords(words: List<Word>) {
        this.words = words
        notifyDataSetChanged()
    }

    override fun getItemCount() = words.size
}
```
