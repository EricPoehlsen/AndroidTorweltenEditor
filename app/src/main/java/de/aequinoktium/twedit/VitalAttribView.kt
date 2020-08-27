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
) : View(context, attrs, defStyleAttr){
    var max_value = 8
    var cur_value = 8f

    // positions of the boxes
    private val fields = mapOf(
        -12 to arrayOf(2,34,14,46),
        -11 to arrayOf(18,34,30,46),
        -10 to arrayOf(34,34,46,46),
        -9 to arrayOf(50,34,62,46),
        -8 to arrayOf(2,18,14,30),
        -7 to arrayOf(18,18,30,30),
        -6 to arrayOf(34,18,46,30),
        -5 to arrayOf(50,18,62,30),
        -4 to arrayOf(2,2,14,14),
        -3 to arrayOf(18,2,30,14),
        -2 to arrayOf(34,2,46,14),
        -1 to arrayOf(50,2,62,14),
        0 to arrayOf(62,2,66,22),
        1 to arrayOf(66,2,86,22),
        2 to arrayOf(90,2,110,22),
        3 to arrayOf(114,2,134,22),
        4 to arrayOf(138,2,158,22),
        5 to arrayOf(162,2,182,22),
        6 to arrayOf(186,2,206,22),
        7 to arrayOf(66,26,86,46),
        8 to arrayOf(90,26,110,46),
        9 to arrayOf(114,26,134,46),
        10 to arrayOf(138,26,158,46),
        11 to arrayOf(162,26,182,46),
        12 to arrayOf(186,26,206,46)
    )


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

        val coords = arrayOf(
            arrayOf(0,0,0,48),
            arrayOf(16,0,16,48),
            arrayOf(32,0,32,48),
            arrayOf(48,0,48,48),
            arrayOf(64,0,64,48),
            arrayOf(88,0,88,48),
            arrayOf(112,0,112,48),
            arrayOf(136,0,136,48),
            arrayOf(160,0,160,48),
            arrayOf(184,0,184,48),
            arrayOf(208,0,208,48),
            arrayOf(0,0,208,0),
            arrayOf(0,48,208,48),
            arrayOf(0,16,64,16),
            arrayOf(64,24,208,24),
            arrayOf(0,32,64,32),
            arrayOf(0,48,208,48)
            )


        for (c in coords) {
            canvas.drawLine(px(c[0]),px (c[1]), px(c[2]), px(c[3]), paint)
        }
    }

    fun box(canvas: Canvas, pos: Int) {
        val paint = Paint()
        paint.color = Color.GRAY
        paint.style = Paint.Style.FILL

        canvas.drawRect(
            px(fields[pos]!![0]),
            px(fields[pos]!![1]),
            px(fields[pos]!![2]),
            px(fields[pos]!![3]),
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

        canvas.drawLine(px(x1),px(y2),px(x2),px(y1), paint)
        if (full) canvas.drawLine(px(x1),px(y1),px(x2),px(y2), paint)
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
            canvas.drawText("-2",px(fields[1]!![0]),px(fields[1]!![3]),paint)
            canvas.drawText("-1",px(fields[2]!![0]),px(fields[2]!![3]),paint)
        } else {
            canvas.drawText("-2",px(fields[-1]!![0]),px(fields[-1]!![3]),paint)
            canvas.drawText("-1",px(fields[1]!![0]),px(fields[1]!![3]),paint)
        }
    }


    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density
}
