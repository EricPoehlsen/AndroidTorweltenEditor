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
        rainbow = rainbow_gradient()
        text_color.textSize = size/12
        invalidate()
    }

    private var h = 0f
    private var s = 0f
    private var v = 0f

    var name = ""

    private var stroke_width = 0f

    private var x0 = 0f
    private var y0 = 0f
    private var rainbow = Paint()

    private val stroke = Paint()
    private val text_color = Paint()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas as Canvas

        col_rect(canvas)
        hue_select(canvas)
        selected_hue(canvas)
        selected_sv(canvas)
        result_rect(canvas)
        color_name(canvas)
    }

    // the selector rect for value and saturation of the current hue
    fun col_rect(canvas: Canvas) {
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
    fun hue_select(canvas: Canvas) {
        val xa = x0
        val ya = (y0 + size*.75 + size/16).toFloat()
        val xb = xa + size
        val yb = ya + size/8
        val b = stroke_width
        canvas.drawRect(xa,ya,xb,yb,stroke)
        canvas.drawRect(xa+b,ya+b,xb-b,yb-b,rainbow)
    }

    // a marker showing the current selected saturation and value on the sv_rect
    fun selected_sv(canvas: Canvas) {
        val b = stroke_width
        val xa = x0 + b + size * s
        val ya = (y0 + b + (size*.75) * (1-v)).toFloat()
        val p = Paint()
        p.color = Color.BLACK
        p.strokeWidth = px(2)

        val dirs = arrayOf(
            arrayOf(1,0,4,0),
            arrayOf(-1,0,-4,0),
            arrayOf(0,1,0,4),
            arrayOf(0,-1,0,-4)
        )

        for (d in dirs) {
            canvas.drawLine(
                xa + px(d[0]),
                ya + px(d[1]),
                xa + px(d[2]),
                ya + px(d[3]),
                p
            )
        }
    }

    // a marker to display the selected hue on the rainbow
    fun selected_hue(canvas: Canvas) {
        val b = stroke_width
        val xa = x0 + b + (h/360 * (size-2*b))
        val ya = (y0 + (size*.75) + size/16).toFloat()
        val yb = ya + size/8
        val p = Paint()
        p.strokeWidth = px(2)
        p.color = Color.BLACK
        canvas.drawLine(xa,ya,xa,yb,p)
    }

    // display the current selected color
    fun result_rect(canvas: Canvas) {
        val b = stroke_width
        val xa = x0
        val ya = (y0 + (size*.75) + size/4).toFloat()
        val xb = xa + size
        val yb = ya + size/6
        canvas.drawRect(xa,ya,xb,yb,stroke)
        val col = Paint().apply {color=Color.HSVToColor(arrayOf(h,s,v).toFloatArray())}
        canvas.drawRect(xa+b,ya+b,xb-b,yb-b,col)
    }

    fun color_name(canvas: Canvas) {
        val text_width = text_color.measureText(name)
        val xa = x0 + (size/2) - (text_width/2)

        val text_height = text_color.descent() - text_color.ascent()
        val baseline = ((size / 6 - text_height) / 2) + (-text_color.ascent())
        val y_top = (y0 + (size*.75) + size/4).toFloat()
        val ya = (y_top + baseline)

        if (v < 0.666) {
            text_color.color = Color.WHITE
        } else {
            text_color.color = Color.BLACK
        }
        canvas.drawText(name, xa, ya, text_color)
    }


    override fun onMeasure(w_spec: Int, h_spec: Int) {
        val min_width = paddingLeft + size + paddingRight
        val min_height = paddingTop + size + size/4 + size/8 + paddingBottom

        val w = Math.max(w_spec, min_width.toInt())
        val h = Math.max(h_spec, min_height.toInt())

        setMeasuredDimension(w, h)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event is MotionEvent) {

            val b = stroke_width
            val x = event.x.toInt()
            val y = event.y.toInt()
            val min_x = (paddingLeft + b).toInt()
            val max_x = (min_x + size - 2*b).toInt()
            val sv_start = (paddingTop + b).toInt()
            val sv_end = (sv_start - 2*b + (size*0.75)).toInt()
            val hue_start = (sv_end + size/16).toInt()
            val hue_end = (hue_start + size/8).toInt()

            val xf = ((x-min_x) / size).toFloat()
            val yf = ((y-sv_start) / ((size*.75) -2*b )).toFloat()

            if (x in min_x..max_x) {
                if (y in sv_start..sv_end) {
                    s = xf
                    v = 1 - yf
                    colorLookup()
                } else if (y in hue_start..hue_end) {
                    h = 360 * xf
                    colorLookup()
                }
                invalidate()
            }
        }
        performClick()
        return true
    }

    // creates the rainbow gradient for the hue selector
    fun rainbow_gradient(): Paint {
        val paint = Paint()

        val gradient = LinearGradient(
            x0 + stroke_width,
            y0,
            x0 + size - stroke_width,
            y0,
            arrayOf(
                Color.HSVToColor(arrayOf(0f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(30f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(60f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(90f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(120f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(150f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(180f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(210f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(240f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(270f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(300f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(330f, 1f, 1f).toFloatArray()),
                Color.HSVToColor(arrayOf(360f, 1f, 1f).toFloatArray())
            ).toIntArray(),
            null,
            Shader.TileMode.REPEAT
        )
        paint.shader = gradient
        return paint
    }

    fun colorLookup() {
        val c = ColorLookup(resources)
        name = c.getColor(h,s,v)
    }

    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density


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