package de.aequinoktium.twedit

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View


class ColorSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var size = 0f
        set(value) {
            field = value
            invalidate()
        }

    var h = 0f
        set(value) {
            field = value
            invalidate()
        }

    var s = 0f
        set(value) {
            field = value
            invalidate()
        }

    var v = 0f
        set(value) {
            field = value
            invalidate()
        }

    var name = ""

    private var stroke_width = 0f

    private var x0 = 0f
    private var y0 = 0f

    private val stroke = Paint()
    private val text_color = Paint()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas is Canvas) {
            drawColorRect(canvas)
            drawHueSelector(canvas)
            drawHueMarker(canvas)
            drawCrosshairs(canvas)
            drawResultRect(canvas)
            drawColorName(canvas)
        }
    }

    // the selector rect for value and saturation of the current hue
    fun drawColorRect(canvas: Canvas) {
        val width = size
        val height = (size * .75).toFloat()

        // draw border
        canvas.drawRect(
            x0,
            y0,
            x0+width,
            y0+height,
            stroke
        )

        val border = stroke_width.toInt()

        val cnt = size.toInt()
        for (x in border..cnt-border) {
            val xf = x.toFloat()
            val f = xf/height
            val paint = Paint()
            val gradient = LinearGradient(
                x0,
                y0 + height + 1 - border,
                x0,
                y0 + border,
                Color.HSVToColor(arrayOf(h,0f,0f).toFloatArray()),
                Color.HSVToColor(arrayOf(h,f,1f).toFloatArray()),
                Shader.TileMode.REPEAT
            )
            paint.shader = gradient
            canvas.drawLine(
                x0 + xf,
                y0 + border,
                x0 + xf,
                y0 + height - border,
                paint
            )
        }

    }

    // the hue selector
    fun drawHueSelector(canvas: Canvas) {
        val x = x0
        val y = (y0 + size*.75 + size/16).toFloat()
        val width = size
        val height = size/8
        val border = stroke_width

        //draw border
        canvas.drawRect(
            x,
            y,
            x+width,
            y+height,
            stroke
        )

        //draw rainbow
        canvas.drawRect(
            x+border,
            y+border,
            x+width-border,
            y+height-border,
            rainbow_gradient()
        )
    }

    // Crosshairs to mark the selected saturation/value
    fun drawCrosshairs(canvas: Canvas) {
        val saturation = s
        val value = 1-v // inverted as we want black down

        val border = stroke_width
        val top =  y0 + border
        val left = x0 + border

        val width = size - 2 * border
        val height = (size * .75 - 2 * border).toFloat()

        val x = width * saturation + left
        val y = height * value + top

        val paint = Paint().apply{
            color = Color.BLACK
            strokeWidth = px(2)
        }

        val hairs = arrayOf(
            arrayOf(1,0,4,0),
            arrayOf(-1,0,-4,0),
            arrayOf(0,1,0,4),
            arrayOf(0,-1,0,-4)
        )

        for (hair in hairs) {
            canvas.drawLine(
                x + px(hair[0]),
                y + px(hair[1]),
                x + px(hair[2]),
                y + px(hair[3]),
                paint
            )
        }
    }

    // a marker to display the selected hue on the rainbow
    fun drawHueMarker(canvas: Canvas) {
        val border = stroke_width
        val hue_width = size - 2*border
        val left = x0 + border
        val top = (y0 + (size*.75) + size/16).toFloat()
        val height = size/8
        val hue = h/360

        val x = hue * hue_width + left
        val y = top

        val paint = Paint().apply{
            strokeWidth = px(2)
            color = Color.BLACK
        }

        canvas.drawLine(
            x,
            y,
            x,
            y+height,
            paint
        )
    }

    // display the current selected color
    fun drawResultRect(canvas: Canvas) {
        val border = stroke_width
        val left = x0
        val top = (y0 + (size*.75) + size/4).toFloat()

        val width = size
        val height = size/6

        val x = left
        val y = top

        // border
        canvas.drawRect(
            x,
            y,
            x + width,
            y + height,
            stroke
        )

        val current_color = Paint().apply {
            color = Color.HSVToColor(arrayOf(h,s,v).toFloatArray())
        }

        // result rect
        canvas.drawRect(
            x + border,
            y + border,
            x + width - border,
            y + height - border,
            current_color
        )
    }

    // draw the color name centered inside the result rect
    fun drawColorName(canvas: Canvas) {
        val top = (y0 + (size*.75) + size/4).toFloat()
        val left = x0
        val box_width = size
        val box_height = size/6

        setTextColorAndSize()
        val ascent_height = -text_color.ascent()
        val text_height = text_color.descent() - text_color.ascent()
        val text_width = text_color.measureText(name)
        val baseline = (box_height - text_height)/2 + ascent_height

        val x = left + box_width/2 - text_width/2
        val y = top + baseline

        canvas.drawText(name, x, y, text_color)
    }

    // sets the text color based on the current value
    fun setTextColorAndSize() {
        text_color.textSize = size/12
        if (v < 0.666) {
            text_color.color = Color.WHITE
        } else {
            text_color.color = Color.BLACK
        }
    }

    // set hue, saturation and value based on users touch input
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event is MotionEvent) {
            val border = stroke_width
            val width = size - 2 * border
            val sv_height = (size * 0.75) - 2 * border
            val hue_height = (size / 8) - 2 * border
            val padding = size / 16 + 2 * border

            // those are the boundaries that concern us ..
            val left = (x0 + border).toInt()
            val right = (left + width).toInt()

            val sv_start = (y0 + border).toInt()
            val sv_end = (sv_start + sv_height).toInt()

            val hue_start = (sv_end + padding).toInt()
            val hue_end = (hue_start + hue_height).toInt()

            // event coordinates
            val x = event.x.toInt()
            val y = event.y.toInt()

            // check coordinates set values
            if (x in left..right) {
                 val width_percent = (x-left) / width

                 if (y in sv_start..sv_end) {
                     val height_percent = ((y-sv_start) / sv_height).toFloat()

                    s = width_percent
                    v = 1 - height_percent
                    name = colorLookup()
                } else if (y in hue_start..hue_end) {
                    h = 360 * width_percent
                    name = colorLookup()
                }
            }
        }
        performClick()
        return true
    }

    // creates the rainbow gradient for the hue selector
    fun rainbow_gradient(): Paint {
        val x = x0 + stroke_width
        val y = y0 + stroke_width
        val width = size - 2 * stroke_width

        // the rainbow array
        var colors = arrayOf<Int>().toIntArray()
        for (i in 0..12) {
            val hue = i * 30f //0-360 in 30° steps
            val color = Color.HSVToColor(arrayOf(hue, 1f, 1f).toFloatArray())
            colors += color
        }

        // create the gradient
        val gradient = LinearGradient(
            x,
            y,
            x + width,
            y,
            colors,
            null,
            Shader.TileMode.REPEAT
        )

        return Paint().apply{shader = gradient}
    }

    fun colorLookup():String {
        val c = ColorLookup(resources)
        return c.getColor(h,s,v)
    }

    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val spec_width = MeasureSpec.getSize(widthMeasureSpec)
        val spec_height = MeasureSpec.getSize(heightMeasureSpec)
        val mode_width = MeasureSpec.getMode(widthMeasureSpec)
        val mode_height = MeasureSpec.getMode(heightMeasureSpec)

        if (size == 0f) {
            size = (spec_width - paddingLeft - paddingRight).toFloat()
        }

        val sv_height = (size * .75).toFloat()
        val padding = (size/16)
        val hue_height = (size/8)
        val result_height = (size/6)

        val min_width = paddingLeft + size + paddingRight
        val min_height = (
            paddingTop
            + sv_height
            + padding
            + hue_height
            + padding
            + result_height
            + paddingBottom
        )

        val w = min_width.toInt()
        val h = min_height.toInt()

        setMeasuredDimension(w, h)

    }


    init {
        stroke_width = 0f

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ColorSelectorView,
            0, 0).apply {


            try {
                size = getDimension(R.styleable.ColorSelectorView_size, 0f)
                stroke_width = getDimension(R.styleable.ColorSelectorView_stroke_width, 2f)
                stroke.color = getColor(R.styleable.ColorSelectorView_stroke_color, 0)
            } finally {
                recycle()
            }
        }

        text_color.setTypeface(Typeface.create("sans", Typeface.BOLD))
        text_color.setAntiAlias(true)

        x0 = paddingLeft.toFloat()
        y0 = paddingTop.toFloat()
    }
}