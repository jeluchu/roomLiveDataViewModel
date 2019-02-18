package com.jeluchu.roomlivedata

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.graphics.RectF
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.DrawableCompat
import android.os.Build
import android.support.v4.content.ContextCompat
import android.graphics.drawable.Drawable
import android.text.Editable
import android.view.Gravity
import android.widget.EditText
import kotlinx.android.synthetic.main.recyclerview_word.*


class MainActivity : AppCompatActivity() {

    private val newWordActivityRequestCode = 1
    private lateinit var wordViewModel: WordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = WordListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        wordViewModel = ViewModelProviders.of(this).get(WordViewModel::class.java)

        wordViewModel.allWords.observe(this, Observer { words ->
            words?.let { adapter.setWords(it) }
        })

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewWordActivity::class.java)
            startActivityForResult(intent, newWordActivityRequestCode)
        }

        // OPTIONS FOR SWIPE RECYCLERVIEW
        val helper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val myWord = adapter.getWordAtPosition(position)

                    if (direction == ItemTouchHelper.LEFT) {
                        wordViewModel.deleteWord(myWord)
                        wordViewModel.allWords
                    } else {
                       /* val builder = AlertDialog.Builder(applicationContext)
                        val userNameEdit = EditText(this@MainActivity)

                        builder.setTitle("Androidly Alert")
                            .setView(userNameEdit)
                        userNameEdit.hint = "Introduce una palabra"

                        builder.setMessage("We have a message")
                        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

                        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                            Toast.makeText(applicationContext,
                                android.R.string.yes, Toast.LENGTH_SHORT).show()

                            wordViewModel.updateWord(myWord)
                            wordViewModel.allWords

                        }

                        builder.setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }

                        builder.show() */

                        val builder = AlertDialog.Builder(this@MainActivity)

                        val userEdit = EditText(this@MainActivity)
                        userEdit.hint = "Introduce un nombre"
                        userEdit.gravity = Gravity.CENTER_HORIZONTAL
                        userEdit.ellipsize

                        // CREAR EL DIALOGO
                        builder.setTitle("Actualizar")
                            .setView(userEdit)
                            .setMessage("Edita tu palabra y actualizala dándole al botón de 'Guardar'\n")
                            .setPositiveButton("Guardar") { _, _ ->
                                val regionName = userEdit.text.toString()

                                if (regionName.isEmpty()) {
                                    Toast.makeText(this@MainActivity, "El campo no puede estar vacío", Toast.LENGTH_SHORT).show()
                                } else {
                                    var word = Word(position, regionName)
                                    wordViewModel.updateWord(word)
                                    wordViewModel.allWords
                                }
                            }
                            .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

                        // MOSTRAR
                        builder.show()
                    }
                }

                // ACTION SWIPE RECYCLERVIEW
                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val icon: Bitmap

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                        val itemView = viewHolder.itemView

                        val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                        val width = height / 3

                        val p = Paint()
                        if (dX > 0) {

                            p.color = Color.parseColor("#1A7DCB")
                            val background = RectF(
                                itemView.left.toFloat(),
                                itemView.top.toFloat(),
                                dX,
                                itemView.bottom.toFloat())
                            c.drawRect(background, p)

                            val left = itemView.left.toFloat() + width
                            val top = itemView.top.toFloat() + width
                            val right = itemView.left.toFloat() + 2 * width
                            val bottom = itemView.bottom.toFloat() - width

                            icon = getBitmapFromVectorDrawable(applicationContext, R.drawable.ic_edit)
                            val iconDest = RectF(left, top, right, bottom)

                            c.drawBitmap(icon,null,iconDest,p)

                        } else {

                            p.color = Color.parseColor("#CB1A1A")

                            val background = RectF(
                                itemView.right.toFloat() + dX,
                                itemView.top.toFloat(),
                                itemView.right.toFloat(),
                                itemView.bottom.toFloat()
                            )
                            c.drawRect(background,p)


                            icon = getBitmapFromVectorDrawable(applicationContext, R.drawable.ic_delete_one)

                            val left = itemView.right.toFloat() - 2 * width
                            val top = itemView.top.toFloat() + width
                            val right = itemView.right.toFloat() - width
                            val bottom = itemView.bottom.toFloat() - width
                            val iconDest = RectF(left, top, right, bottom)

                            c.drawBitmap(icon,null,iconDest,p)
                        }

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    }
                }

            })

        helper.attachToRecyclerView(recyclerView)

    }

    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {
            intentData?.let { data ->
                val word = Word(0,data.getStringExtra(NewWordActivity.EXTRA_REPLY))
                wordViewModel.insert(word)
            }
        } else {
            Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteall -> {
                    wordViewModel.deleteAll()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

    /*private fun itemClicked(){

        // CREACIÓN DEL ALERT VIEW
        val builder = AlertDialog.Builder(this@MainActivity)


            builder.setTitle("Préstamo")
            builder.setMessage("¿Vas coger este teléfono?")

            builder.setPositiveButton("Sí"){
                    dialog, which ->
                Toast.makeText(applicationContext, "${device.modelo} prestado correctamente", Toast.LENGTH_SHORT).show()
            }

            builder.setNegativeButton("No"){
                    dialog, which ->
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()

    }
} */

