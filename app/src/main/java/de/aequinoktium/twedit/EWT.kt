package de.aequinoktium.twedit

import java.time.LocalDateTime
import kotlin.random.Random

class EWT() {
    private val table = mapOf(
        1  to "#########XXXXX/",
        2  to "#######XXXXXX/O",
        3  to "#####XXXXXXX/OO",
        4  to "##XXXXXXXX//OOO",
        5  to "XXXXXXXXX//OOOO",
        6  to "XXXXXXXX//OOOOO",
        7  to "XXXXXX///OOOOOO",
        8  to "XXXXX///OOOOOOO",
        9  to "XXXX///OOOOOOOO",
        10 to "XX////OOOOOOOOO",
        11 to "X////OOOOOOOOOO",
        12 to "////OOOOOOOOOOO"
    )

    private val values = mapOf(
        "#" to 1f,
        "X" to 1f,
        "/" to 0.5f,
        "O" to 0f
    )

    fun roll(dice: Int, col: Int):Float {
        var result = 0f
        var rolls = dice
        val i = col + 7

        while (rolls > 0) {
            val roll = Random.nextInt(1,12)
            val cell:Char = table[roll]!![i]
            // if (cell == "#")
            rolls--
        }


        return result

    }

}