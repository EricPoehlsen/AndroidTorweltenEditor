package de.aequinoktium.twedit

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.children
import androidx.core.view.size
import org.w3c.dom.Text

class ItemView: androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context?) : super(context) {
        init()
}
    constructor(context: Context?, attrs: AttributeSet): super(context, attrs){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr){
        init()
    }

    lateinit var item: Item

    fun init(){

    }
}