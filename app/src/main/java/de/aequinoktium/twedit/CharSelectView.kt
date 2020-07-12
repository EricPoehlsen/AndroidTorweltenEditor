package de.aequinoktium.twedit

import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View.MeasureSpec.getSize
import android.widget.CursorAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CharSelectView: ConstraintLayout {
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

    var name: TextView = TextView(context)
    var xp: TextView = TextView(context)
    var concept: TextView = TextView(context)

    fun init() {
        var constraints = ConstraintSet()
        constraints.clone(context, R.layout.view_char_select_button)
        this.setConstraintSet(constraints)
        name.id = R.id.v_csb_name
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        name.setTypeface(null, Typeface.BOLD)

        xp.id = R.id.v_csb_xp
        concept.id = R.id.v_csb_concept
        this.addView(name)
        this.addView(xp)
        this.addView(concept)
        this.background = ResourcesCompat.getDrawable(resources, R.drawable.simple_border, null)
    }


}