package com.examples.constraintlayouttest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Demo3Act : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, Demo3Act::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test3_layout)
    }
}