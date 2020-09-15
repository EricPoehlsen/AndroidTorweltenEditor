package de.aequinoktium.twedit

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View

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

    private val dmg_icons = arrayOf(
        resources.getDrawable(R.drawable.qual_broken, null),
        resources.getDrawable(R.drawable.qual_bad, null),
        resources.getDrawable(R.drawable.qual_normal, null),
        resources.getDrawable(R.drawable.qual_high, null),
        resources.getDrawable(R.drawable.qual_top, null)
    )

    private val container_icon = resources.getDrawable(R.drawable.itemview_container, null)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        this.setBackgroundColor(Color.BLUE)

        if (canvas is Canvas) {
            Log.d("info", "DRAW")
            drawBackground(canvas)
            drawQuantity(canvas)
            drawName(canvas)
            drawDamage(canvas)
            drawContainer(canvas)
            drawQuality(canvas)
        }

    }

    private fun drawBackground(canvas: Canvas) {
        val paint = Paint().apply{
            color=Color.DKGRAY
        }
        canvas.drawRect(left,top,right,bottom,paint)

    }

    private fun drawName(canvas: Canvas) {
        val paint = whiteTextColor()
        canvas.drawText(item.name, left+px(32),top+textAlignCenter(paint),paint)
    }

    private fun drawQuantity(canvas: Canvas) {
        val text = if (item.qty < 100) "${item.qty}x" else "99+"

        val paint = whiteTextColor()
        val max_width = paint.measureText("99x")
        val width = paint.measureText(text)
        val start = left + px(6) + max_width - width

        canvas.drawText(text, start,top+textAlignCenter(paint),paint)
    }

    /**
     * Displays container information
     */
    private fun drawContainer(canvas: Canvas) {
        if (item.weight_limit > 0) {
            container_icon.setBounds(
                right.toInt()-100,
                top.toInt()+5,
                right.toInt()-50,
                bottom.toInt()-5
            )
            if (item.has_contents) {
                container_icon.alpha = 255
            } else {
                container_icon.alpha = 50
            }
            container_icon.draw(canvas)
        }
    }

    /**
     * Displays the quality icon
     */
    private fun drawQuality(canvas: Canvas) {
        val icon: Drawable
        if (item.cur_qual <= 2) {
            icon = dmg_icons[0]
        } else if (item.cur_qual <= 4) {
            icon = dmg_icons[1]
        } else if (item.cur_qual <= 6) {
            icon = dmg_icons[2]
        } else if (item.cur_qual <= 9) {
            icon = dmg_icons[3]
        } else {
            icon = dmg_icons[4]
        }
        icon.setBounds(
            (right-px(100)).toInt(),
            (top+(bottom-top)/2-px(8)).toInt(),
            (right-px(84)).toInt(),
            (top+(bottom-top)/2+px(8)).toInt()
        )
        icon.draw(canvas)
    }


    /**
     * Display the damage text
     */
    private fun drawDamage(canvas: Canvas) {
        if (!item.dmg.isEmpty()) {
            val paint = Paint().apply {
                color = Color.RED
                textSize = px(14)
                isAntiAlias = true
            }
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
            val dmg = item.dmg.toString()
            val text_width = paint.measureText(dmg)
            val x = right - text_width - 6
            val y = top + textAlignCenter(paint)
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

    // text baseline
    private fun textHeight(paint: Paint): Float = paint.descent() - paint.ascent()
    private fun textAlignTop(paint: Paint):Float = -paint.ascent()
    private fun textAlignBottom(paint: Paint): Float = -paint.descent()
    private fun textAlignCenter(paint: Paint): Float {
        return (top + .5*(bottom - top) + .5*textHeight(paint) - paint.descent()).toFloat()
    }


    private fun whiteTextColor(): Paint {
        val paint = Paint().apply{
            color= Color.WHITE
            textSize = px(14)
            isAntiAlias = true
        }

        return paint
    }




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

