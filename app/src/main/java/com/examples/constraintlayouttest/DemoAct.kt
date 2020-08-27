package com.examples.constraintlayouttest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DemoAct : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, DemoAct::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_layout)
    }
}