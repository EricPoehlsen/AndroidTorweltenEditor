package de.aequinoktium.twedit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.E

class ItemView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var item: Item = Item()

    private var layout_width = 0f
    private var layout_height = 0f
    private var top = 0f
    private var left = 0f
    private var right = 0f
    private var bottom = 0f





    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        this.setBackgroundColor(Color.BLUE)

        if (canvas is Canvas) {
            Log.d("info", "DRAW")
            drawBackgrund(canvas)
            drawName(canvas)
            drawDamage(canvas)
        }

    }

    fun drawBackgrund(canvas: Canvas) {
        val paint = Paint().apply{
            color=Color.DKGRAY
        }
        canvas.drawRect(left,top,right,bottom,paint)

    }

    fun drawName(canvas: Canvas) {
        val paint = Paint().apply{
            color= Color.WHITE
            textSize = px(14)
            isAntiAlias = true
        }
        canvas.drawText(item.name, left,top+baseline(paint),paint)
    }

    fun drawDamage(canvas: Canvas) {
        if (!item.dmg.isEmpty()) {
            val paint = Paint().apply {
                color = Color.RED
                textSize = px(14)
                isAntiAlias = true
            }
            val dmg = item.dmg.toString()
            val text_width = paint.measureText(dmg)
            val x = right - text_width
            val y = bottom - paint.descent()
            canvas.drawText(dmg, x, y, paint)
        }
    }

    override fun onMeasure(width_measure_spec: Int, height_measure_spec: Int) {
        val measured_width = MeasureSpec.getSize(width_measure_spec)
        val measured_height = MeasureSpec.getSize(height_measure_spec)

        val min_width = px(128)
        val min_height = px(32)

        if (layout_width == 0f) {
            layout_width = measured_width.toFloat()
            if (layout_width < min_width) layout_width = min_width
        }
        if (layout_height == 0f) {
            layout_height = measured_height.toFloat()
            if (layout_height < min_height) layout_height = min_height
        }
        top = paddingTop.toFloat()
        left = paddingLeft.toFloat()
        right = layout_width - paddingRight.toFloat()
        bottom = layout_height - paddingBottom.toFloat()

        val width = layout_width.toInt()
        val height = layout_height.toInt()

        setMeasuredDimension(width, height)
    }

    fun baseline(paint: Paint):Float = -paint.ascent()






    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density

    init {
        if (context is Context)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ColorSelectorView,
            0, 0).apply {


            try {
                layout_width = getDimension(R.styleable.ItemView_android_layout_width,0f)
                layout_height = getDimension(R.styleable.ItemView_android_layout_height,0f)
            } finally {
                recycle()
            }
        }

//         text_color.setTypeface(Typeface.create("sans", Typeface.BOLD))
        //      text_color.setAntiAlias(true)

        // x0 = paddingLeft.toFloat()
        // y0 = paddingTop.toFloat()
    }
}

