package de.aequinoktium.twedit

import android.content.Context
import android.database.Cursor
import android.util.AttributeSet
import android.util.Log
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

class TraitView: ConstraintLayout {
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

    val scope = CoroutineScope(Job() + Dispatchers.IO)

    var trait_id: Int = 0
        get() {
            return trait_id
        }
        set(value: Int) {
            field = value
            scope.launch{
                loadTrait(value)
            }
        }


    private var trait_txt: String = ""


    var title: TextView = TextView(context)
    var xp: TextView = TextView(context)


    fun init() {
                var c = ConstraintSet()
        c.clone(context, R.layout.view_trait)
        this.setConstraintSet(c)
        title.id = R.id.view_trait_title
        xp.id = R.id.view_trait_xp
        this.addView(title)
        this.addView(xp)
        this.background = ResourcesCompat.getDrawable(resources, R.drawable.simple_border, null)
    }

    suspend fun loadTrait(trait_id: Int) {
        var act = context as MainActivity
        var sql = "SELECT min_rank, max_rank, txt FROM traits WHERE id = $trait_id"
        var trait: Cursor = act.db.rawQuery(sql, null)
        if (trait.count > 0) {
            trait.moveToFirst()
            trait_txt = trait.getString(2)
        }

        Log.d("Info", trait_txt)
    }

    fun toggle_details() {

    }
}