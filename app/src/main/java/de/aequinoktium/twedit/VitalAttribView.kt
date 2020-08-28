package de.aequinoktium.twedit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View


class VitalAttribView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var max_value:Int = 8
        set(value){
            field = value
            invalidate()
        }

    var cur_value:Float = 0f
        set(value){
            field = value
            invalidate()
        }

    private var x0 = 0f
    private var y0 = 0f
    private var s_bx:Float = px(16)
    private var l_bx:Float = (s_bx * 1.5).toFloat()
    private var bx_p:Float = 2f

    // shadow variable to set both the small and large box sizes
    var box_size:Float = px(16)
    set(value){
        field = value
        s_bx = value
        l_bx = (value * 1.5).toFloat()
        invalidate()
    }

    var box_color = Color.GRAY
        set(value){
            field = value
            invalidate()
        }

    var line_color = Color.GRAY
        set(value){
            field = value
            invalidate()
        }

    var tick_color = Color.RED
        set(value){
            field = value
            invalidate()
        }

    var tick_width = px(2)
        set(value){
            field = value
            invalidate()
        }

    var variant = 0
        set(value){
            field = value
            invalidate()
        }

    // positions of the boxes
    private var fields = mutableMapOf<Int, Array<Float>>()


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val min_width = (paddingLeft + 4 * s_bx + 6 * l_bx + paddingRight).toInt()
        val w: Int = resolveSizeAndState(min_width, widthMeasureSpec, 1)

        val min_height = (paddingTop + 2 * l_bx + paddingBottom).toInt()
        val h: Int = resolveSizeAndState(min_height, heightMeasureSpec, 1)

        setMeasuredDimension(w, h)
    }


    // Called when the view should render its content.
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas as Canvas
        grid(canvas)

        for (i in max_value+1..12) box(canvas, i)
        for (i in -12..-max_value-1) box(canvas, i)

        txt(canvas,variant)

        cur_dmg(canvas)

    }

    fun grid(canvas: Canvas) {
        val paint = Paint()
        paint.color = line_color
        paint.strokeWidth = px(1)
        paint.style = Paint.Style.STROKE

        var coords = arrayOf<Array<Float>>()
        // vertical lines for small boxes
        for (i in 0..4) {
            val vs = arrayOf(
                x0 + i * s_bx,
                y0,
                x0 + i * s_bx,
                y0 + 2 * l_bx
            )
            coords += vs
        }
        // vertical lines for the large boxes
        for (i in 0..6) {
            val vl = arrayOf(
                x0 + 4 * s_bx + i * l_bx,
                y0,
                x0 + 4 * s_bx + i * l_bx,
                y0 + 2 * l_bx
            )
            coords += vl
        }
        // horizontal lines for small boxes
        for (i in 0..3) {
            val hs = arrayOf(
                x0,
                y0 + i * s_bx,
                x0 + 4 * s_bx,
                y0 + i * s_bx
            )
            coords += hs
        }
        // horizontal lines for large boxes
        for (i in 0..2) {
            val hs = arrayOf(
                x0 + 4 * s_bx,
                y0 + i * l_bx,
                x0 + 4 * s_bx + 6 * l_bx,
                y0 + i * l_bx
            )
            coords += hs
        }

        for (c in coords) {
            canvas.drawLine(c[0], c[1], c[2], c[3], paint)
        }
    }

    /**
     * draws boxes into the fields that are outside the range of
     */
    fun box(canvas: Canvas, pos: Int) {
        val paint = Paint()
        paint.color = box_color
        paint.style = Paint.Style.FILL

        canvas.drawRect(
            fields[pos]!![0],
            fields[pos]!![1],
            fields[pos]!![2],
            fields[pos]!![3],
            paint
        )
    }

    fun dmg(canvas: Canvas, pos: Int, full:Boolean=true) {
        val x1 = fields[pos]!![0] + tick_width
        val y1 = fields[pos]!![1] + tick_width
        val x2 = fields[pos]!![2] - tick_width
        val y2 = fields[pos]!![3] - tick_width

        val paint = Paint()
        paint.color = tick_color
        paint.strokeWidth = tick_width
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.STROKE

        canvas.drawLine(x1, y2, x2, y1, paint)
        if (full) canvas.drawLine(x1, y1, x2, y2, paint)
    }

    fun cur_dmg(canvas: Canvas) {
        if (cur_value > 0) {
            for (i in 1..max_value) {
                val f_i = i.toFloat()

                if (f_i - cur_value in 0.1f..0.9f) {
                    dmg(canvas,i,false)
                } else if (f_i > cur_value) {
                    dmg(canvas,i)
                }
            }
        } else {
            for (i in 1..max_value) dmg(canvas, i)
            for (i in -12..-1) {
                val f_i = i.toFloat()
                if (cur_value - f_i-1 in -0.9f..-0.1f) {
                    dmg(canvas,i,false)
                } else if (f_i+1 > cur_value) {
                    dmg(canvas,i)
                }
            }
        }

    }

    fun txt(canvas: Canvas, variant: Int=0) {
        val paint = Paint()
        paint.textSize = s_bx -2
        paint.color = Color.GRAY
        if (variant == 0) {
            canvas.drawText("-2", fields[1]!![0], fields[1]!![3], paint)
            canvas.drawText("-1", fields[2]!![0], fields[2]!![3], paint)
        } else {
            canvas.drawText("-2", fields[-1]!![0] , fields[-1]!![3], paint)
            canvas.drawText("-1", fields[1]!![0] , fields[1]!![3], paint)
        }
    }

    /**
     * sets up the coordinates map for the fields
     */
    fun prepFields() {
        // setting the fields for the small boxes
        var row = 2
        var col = 0
        for (i in -12..-1) {
            fields[i] = arrayOf(
                x0 + col * s_bx + bx_p,
                y0 + row * s_bx + bx_p,
                x0 + col * s_bx + s_bx - bx_p,
                y0 + row * s_bx + s_bx - bx_p
            )
            col++
            if (col > 3) {
                col = 0
                row--
            }
        }

        // setting the fields for the large boxes
        row = 0
        col = 0
        val _x = x0 + 4 * s_bx
        for (i in 1..12) {
            fields[i] = arrayOf(
                (_x + col * l_bx + bx_p).toFloat(),
                (y0 + row * l_bx + bx_p).toFloat(),
                (_x + col * l_bx + l_bx - bx_p).toFloat(),
                (y0 + row * l_bx + l_bx - bx_p).toFloat()
            )
            col++
            if (col > 5) {
                col = 0
                row++
            }
        }

        // contingency
        fields[0] = arrayOf(0f,0f,0f,0f)
    }

    fun readAttribSet(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.VitalAttribView,
            0,
            0).apply {
            try {
                box_size = getDimension(R.styleable.VitalAttribView_box_size, px(16))
                max_value = getInteger(R.styleable.VitalAttribView_max_value, 4)
                cur_value = getFloat(R.styleable.VitalAttribView_cur_value, 2.5f)
                tick_color = getColor(R.styleable.VitalAttribView_tick_color, Color.RED)
                box_color = getColor(R.styleable.VitalAttribView_box_color, Color.GRAY)
                line_color = getColor(R.styleable.VitalAttribView_line_color, Color.GRAY)
                tick_width = getDimension(R.styleable.VitalAttribView_tick_width, px(2))
                variant = getInteger(R.styleable.VitalAttribView_variant, 0)
                bx_p = getDimension(R.styleable.VitalAttribView_box_padding, px(2))
            } finally {
                recycle()
            }
        }
    }

    fun handlePadding() {
        x0 = paddingLeft.toFloat()
        y0 = paddingTop.toFloat()
    }

    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density



    init {
        handlePadding()
        if (attrs != null) readAttribSet(attrs)
        prepFields()

    }
}
