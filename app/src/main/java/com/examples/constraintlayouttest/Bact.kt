package com.examples.constraintlayouttest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.a_act.*
import kotlin.concurrent.thread

class Bact : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_act)
        startBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }


        startBtn2.setOnClickListener {
            DemoAct.start(this@Bact)
        }

        startBtn3.setOnClickListener {
            Demo3Act.start(this@Bact)
        }

        thread {

            repeat(30) {
                val view = LayoutInflater.from(this)
                    .inflate(R.layout.test4_layout, null, false) as? ConstraintLayout

                val m = ConstraintLayout::class.java.getDeclaredMethod(
                    "onMeasure",
                    Int::class.java,
                    Int::class.java
                )

                m.isAccessible = true

                val m2 = ConstraintLayout::class.java.getDeclaredMethod(
                    "onLayout",
                    Boolean::class.java,
                    Int::class.java,
                    Int::class.java,
                    Int::class.java,
                    Int::class.java
                )

                m2.isAccessible = true


                m.invoke(
                    view,
                    View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST)
                )
                m2.invoke(view, true, 0, 0, 2000, 2000)
            }
        }

    }

}