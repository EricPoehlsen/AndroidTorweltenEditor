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

    private val x0 = 0f
    private val y0 = 0f
    private var s_bx:Float = px(16)
    private var l_bx:Float = (s_bx * 1.5).toFloat()
    private val bx_p = 2




    var max_value = 8
    var cur_value = 8f

    // positions of the boxes
    private var fields = mutableMapOf<Int, Array<Float>>()


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val min_width = (4 * s_bx + 6 * l_bx).toInt()
        val w: Int = resolveSizeAndState(min_width, widthMeasureSpec, 1)

        val min_height = (2 * l_bx).toInt()
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

        txt(canvas,1)

        cur_dmg(canvas)

    }

    fun grid(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.GRAY
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

    fun box(canvas: Canvas, pos: Int) {
        val paint = Paint()
        paint.color = Color.GRAY
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
        val x1 = fields[pos]!![0]
        val y1 = fields[pos]!![1]
        val x2 = fields[pos]!![2]
        val y2 = fields[pos]!![3]

        val paint = Paint()
        paint.color = Color.WHITE
        paint.strokeWidth = px(2)
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
                } else if (f_i > cur_value) {
                    dmg(canvas,i)
                }
            }
        }

    }

    fun txt(canvas: Canvas, variant: Int=0) {
        val paint = Paint()
        paint.textSize = px(12)
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
        var row = 0
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
                row++
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
                val size = getDimension(R.styleable.VitalAttribView_box_size, 16f)

                Log.d("info", "Size: $size")
                s_bx = size
                l_bx = (size * 1.5).toFloat()
            } finally {
                recycle()
            }
        }
    }

    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density



    init {
        if (attrs != null) readAttribSet(attrs)
        prepFields()

    }
}
