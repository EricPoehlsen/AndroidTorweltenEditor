package de.aequinoktium.twedit

import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.MeasureSpec.getSize
import android.view.ViewGroup
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
    }
    constructor(context: Context?, attrs: AttributeSet): super(context, attrs){
    }
    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr){
    }

    var name: TextView
    var xp: TextView
    var concept: TextView

    init {
        inflate(context, R.layout.view_char_select_button, this)
        name = this.findViewById(R.id.v_csb_name)
        xp = this.findViewById(R.id.v_csb_xp)
        concept = this.findViewById(R.id.v_csb_concept)
    }
}