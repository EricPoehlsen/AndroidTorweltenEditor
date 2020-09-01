package de.aequinoktium.twedit

import android.content.res.Resources
import android.util.Log
import java.util.*

class ColorLookup(val resources: Resources) {
    private val colors = arrayOf(
        arrayOf(0,0,0,R.string.color_black),
        arrayOf(0,0,66,R.string.color_darkgray),
        arrayOf(0,0,41,R.string.color_dimgray),
        arrayOf(0,0,86,R.string.color_gainsboro),
        arrayOf(0,0,83,R.string.color_lightgray),
        arrayOf(0,0,75,R.string.color_silver),
        arrayOf(0,0,100,R.string.color_white),
        arrayOf(0,0,96,R.string.color_whitesmoke),
        arrayOf(0,2,100,R.string.color_snow),
        arrayOf(240,3,100,R.string.color_ghostwhite),
        arrayOf(150,4,100,R.string.color_mintcream),
        arrayOf(40,6,100,R.string.color_floralwhite),
        arrayOf(60,6,100,R.string.color_ivory),
        arrayOf(120,6,100,R.string.color_honeydew),
        arrayOf(180,6,100,R.string.color_azure),
        arrayOf(208,6,100,R.string.color_aliceblue),
        arrayOf(340,6,100,R.string.color_lavenderblush),
        arrayOf(25,7,100,R.string.color_seashell),
        arrayOf(30,8,98,R.string.color_linen),
        arrayOf(240,8,98,R.string.color_lavender),
        arrayOf(39,9,99,R.string.color_oldlace),
        arrayOf(60,10,96,R.string.color_beige),
        arrayOf(6,12,100,R.string.color_mistyrose),
        arrayOf(60,12,100,R.string.color_lightyellow),
        arrayOf(180,12,100,R.string.color_lightcyan),
        arrayOf(300,12,85,R.string.color_thistle),
        arrayOf(34,14,98,R.string.color_antiquewhite),
        arrayOf(48,14,100,R.string.color_cornsilk),
        arrayOf(37,16,100,R.string.color_papayawhip),
        arrayOf(60,16,98,R.string.color_lightgoldenrodyellow),
        arrayOf(36,20,100,R.string.color_blanchedalmond),
        arrayOf(54,20,100,R.string.color_lemonchiffon),
        arrayOf(214,21,87,R.string.color_lightsteelblue),
        arrayOf(210,22,60,R.string.color_lightslategray),
        arrayOf(210,22,56,R.string.color_slategray),
        arrayOf(33,23,100,R.string.color_bisque),
        arrayOf(187,23,90,R.string.color_powderblue),
        arrayOf(0,24,74,R.string.color_rosybrown),
        arrayOf(120,24,74,R.string.color_darkseagreen),
        arrayOf(195,25,90,R.string.color_lightblue),
        arrayOf(350,25,100,R.string.color_pink),
        arrayOf(180,26,93,R.string.color_paleturquoise),
        arrayOf(28,27,100,R.string.color_peachpuff),
        arrayOf(39,27,96,R.string.color_wheat),
        arrayOf(300,28,87,R.string.color_plum),
        arrayOf(38,29,100,R.string.color_moccasin),
        arrayOf(55,29,93,R.string.color_palegoldenrod),
        arrayOf(351,29,100,R.string.color_lightpink),
        arrayOf(36,32,100,R.string.color_navajowhite),
        arrayOf(34,33,82,R.string.color_tan),
        arrayOf(34,39,87,R.string.color_burlywood),
        arrayOf(120,39,93,R.string.color_lightgreen),
        arrayOf(120,39,98,R.string.color_palegreen),
        arrayOf(180,41,31,R.string.color_darkslategray),
        arrayOf(182,41,63,R.string.color_cadetblue),
        arrayOf(54,42,94,R.string.color_khaki),
        arrayOf(56,43,74,R.string.color_darkkhaki),
        arrayOf(197,43,92,R.string.color_skyblue),
        arrayOf(300,45,93,R.string.color_violet),
        arrayOf(203,46,98,R.string.color_lightskyblue),
        arrayOf(0,47,94,R.string.color_lightcoral),
        arrayOf(15,48,91,R.string.color_darksalmon),
        arrayOf(260,49,86,R.string.color_mediumpurple),
        arrayOf(302,49,85,R.string.color_orchid),
        arrayOf(340,49,86,R.string.color_palevioletred),
        arrayOf(160,50,100,R.string.color_aquamarine),
        arrayOf(160,50,80,R.string.color_mediumaquamarine),
        arrayOf(17,52,100,R.string.color_lightsalmon),
        arrayOf(6,54,98,R.string.color_salmon),
        arrayOf(0,55,80,R.string.color_indianred),
        arrayOf(82,56,42,R.string.color_darkolivegreen),
        arrayOf(248,56,55,R.string.color_darkslateblue),
        arrayOf(248,56,80,R.string.color_slateblue),
        arrayOf(249,56,93,R.string.color_mediumslateblue),
        arrayOf(219,58,93,R.string.color_cornflowerblue),
        arrayOf(330,59,100,R.string.color_hotpink),
        arrayOf(288,60,83,R.string.color_mediumorchid),
        arrayOf(28,61,96,R.string.color_sandybrown),
        arrayOf(207,61,71,R.string.color_steelblue),
        arrayOf(147,66,70,R.string.color_mediumseagreen),
        arrayOf(178,66,82,R.string.color_mediumturquoise),
        arrayOf(146,67,55,R.string.color_seagreen),
        arrayOf(16,69,100,R.string.color_coral),
        arrayOf(30,69,80,R.string.color_peru),
        arrayOf(174,71,88,R.string.color_turquoise),
        arrayOf(225,71,88,R.string.color_royalblue),
        arrayOf(9,72,100,R.string.color_tomato),
        arrayOf(19,72,63,R.string.color_sienna),
        arrayOf(0,75,65,R.string.color_brown),
        arrayOf(80,75,56,R.string.color_olivedrab),
        arrayOf(280,75,80,R.string.color_darkorchid),
        arrayOf(80,76,80,R.string.color_yellowgreen),
        arrayOf(120,76,55,R.string.color_forestgreen),
        arrayOf(120,76,80,R.string.color_limegreen),
        arrayOf(240,78,44,R.string.color_midnightblue),
        arrayOf(0,81,70,R.string.color_firebrick),
        arrayOf(271,81,89,R.string.color_blueviolet),
        arrayOf(84,82,100,R.string.color_greenyellow),
        arrayOf(177,82,70,R.string.color_lightseagreen),
        arrayOf(43,85,85,R.string.color_goldenrod),
        arrayOf(25,86,82,R.string.color_chocolate),
        arrayOf(25,86,55,R.string.color_saddlebrown),
        arrayOf(210,88,100,R.string.color_dodgerblue),
        arrayOf(322,89,78,R.string.color_mediumvioletred),
        arrayOf(348,91,86,R.string.color_crimson),
        arrayOf(330,100,100,R.string.color_deeppink),
        arrayOf(43,94,72,R.string.color_darkgoldenrod),
        arrayOf(0,100,55,R.string.color_darkred),
        arrayOf(0,100,50,R.string.color_maroon),
        arrayOf(0,100,100,R.string.color_red),
        arrayOf(16,100,100,R.string.color_orangered),
        arrayOf(33,100,100,R.string.color_darkorange),
        arrayOf(39,100,100,R.string.color_orange),
        arrayOf(51,100,100,R.string.color_gold),
        arrayOf(60,100,50,R.string.color_olive),
        arrayOf(60,100,100,R.string.color_yellow),
        arrayOf(90,100,100,R.string.color_lawngreen),
        arrayOf(120,100,39,R.string.color_darkgreen),
        arrayOf(120,100,50,R.string.color_green),
        arrayOf(120,100,100,R.string.color_lime),
        arrayOf(150,100,100,R.string.color_springgreen),
        arrayOf(157,100,100,R.string.color_mediumspringgreen),
        arrayOf(180,100,100,R.string.color_cyan),
        arrayOf(180,100,55,R.string.color_darkcyan),
        arrayOf(180,100,50,R.string.color_gray),
        arrayOf(180,100,50,R.string.color_teal),
        arrayOf(181,100,82,R.string.color_darkturquoise),
        arrayOf(195,100,100,R.string.color_deepskyblue),
        arrayOf(240,100,100,R.string.color_blue),
        arrayOf(240,100,55,R.string.color_darkblue),
        arrayOf(240,100,80,R.string.color_mediumblue),
        arrayOf(240,100,50,R.string.color_navy),
        arrayOf(275,100,51,R.string.color_indigo),
        arrayOf(282,100,83,R.string.color_darkviolet),
        arrayOf(300,100,55,R.string.color_darkmagenta),
        arrayOf(300,100,100,R.string.color_magenta),
        arrayOf(300,100,50,R.string.color_purple)
    )

    fun getColor(h: Float, s: Float, v: Float): String{
        var result = ""
        var darkness = ""
        var lightness = ""

        val hue = h.toInt()
        val sat = (s*100).toInt()
        val value = (v*100).toInt()

        var checked_colors = arrayOf<Array<Int>>()


        for (col in colors) {
            // calculate hue diff .. hue wraps at 360°
            var hue_diff = col[0] - hue
            if (hue_diff > 180) hue_diff -=360
            if (hue_diff < -180) hue_diff += 360

            val sat_diff = sat - col[1]
            val val_diff = value - col[2]

            // calculate the total spread (hue difference is doubled)
            val spread = (2*Math.abs(hue_diff)) + Math.abs(sat_diff) + Math.abs(val_diff)

            checked_colors += arrayOf(spread,hue_diff,sat_diff,val_diff) + col
        }

        // sort the colors by least differenc
        fun selector(a: Array<Int>): Int = a[0]
        checked_colors.sortBy({selector(it)})

        val selected_color = checked_colors[0]
        val diff = selected_color[0]
        val hue_diff = selected_color[1]
        val sat_diff = selected_color[2]
        val val_diff = selected_color[3]
        var name = resources.getString(selected_color[7])

        val dark = resources.getString(R.string.color_dark)
        val light = resources.getString(R.string.color_light)

        val check_dark = dark.dropLast(dark.length-4)
        val check_light = light.dropLast(light.length-4)


        if (!(  name.toLowerCase(Locale.getDefault()).startsWith(check_dark)
             || name.toLowerCase(Locale.getDefault()).startsWith(check_light)
             || name.contains(" ")))
        {
            // darker colors
            if (val_diff < -10) darkness = resources.getString(R.string.color_dark)
            // brighter colors
            if (val_diff > 10) {
                darkness = resources.getString(R.string.color_light)
                if (sat_diff > 10) darkness = resources.getString(R.string.color_bright)
            }
        }

        // handle grays ...
        if (sat <= 5) {
            name = resources.getString(R.string.color_gray)
            if (value in 6..33) darkness = resources.getString(R.string.color_dark)
            if (value in 66..95) darkness = resources.getString(R.string.color_light)
            if (value in 95..100) name = resources.getString(R.string.color_white)
        }
        // ... and black
        if (value <= 5) {
            name = resources.getString(R.string.color_black)
            darkness = ""
        }

        return "$darkness $name"
    }


}