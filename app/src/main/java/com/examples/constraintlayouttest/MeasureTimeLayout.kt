package com.examples.constraintlayouttest

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout

class MeasureTimeLayout : ConstraintLayout {

    private var start = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (start) Log.i("sss_ttt_ddd", "----------------")
        start = false
        val time = System.currentTimeMillis()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.i("sss_ttt_ddd", "time: ${System.currentTimeMillis() - time}")
    }
}