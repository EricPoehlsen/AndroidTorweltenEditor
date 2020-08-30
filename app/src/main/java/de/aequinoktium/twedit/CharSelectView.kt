package de.aequinoktium.twedit

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

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
    var delete: ImageView

    init {
        inflate(context, R.layout.view_char_select_button, this)
        name = this.findViewById(R.id.v_csb_name)
        xp = this.findViewById(R.id.v_csb_xp)
        concept = this.findViewById(R.id.v_csb_concept)
        delete = this.findViewById(R.id.v_csb_delete)
    }
}