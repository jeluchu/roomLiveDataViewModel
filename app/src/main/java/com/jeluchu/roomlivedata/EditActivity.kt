package com.jeluchu.roomlivedata

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class EditActivity : AppCompatActivity() {


    lateinit var btnSave: Button
    lateinit var btnDelete: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
    }
}
