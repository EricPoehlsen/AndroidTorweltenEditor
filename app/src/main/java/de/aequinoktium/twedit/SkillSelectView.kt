package de.aequinoktium.twedit

import android.content.Context
import android.util.AttributeSet


class SkillSelectView: androidx.appcompat.widget.AppCompatCheckBox {
    constructor(context: Context?): super(context)
    constructor(context: Context?, attrs: AttributeSet):
            super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr)
    var skill_id: Int = 0
    var is_base = false
    var spec = 0
    var is_active = true
}