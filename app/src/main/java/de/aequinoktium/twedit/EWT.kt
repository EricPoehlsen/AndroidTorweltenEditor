package de.aequinoktium.twedit

import java.time.LocalDateTime
import kotlin.random.Random

class EWT() {
    //                   overflow(     EWT    )overflow
    private val table = "###########XXXX////OOOOOOOOOOO"

    private val values = mapOf(
        "#" to 1f,
        "X" to 1f,
        "/" to 0.5f,
        "O" to 0f
    )

    fun roll(dmg: Damage): Array<Any> {
        return roll(dmg.s, dmg.d)
    }

    fun roll(dice: Int, modifier: Int):Array<Any> {
        var rolled = arrayOf<Int>()
        var result = 0f
        var rolls = dice
        while (rolls > 0) {
            val roll = Random.nextInt(0,12)
            val check = roll + modifier + 9
            val cell = table[check].toString()
            if (cell == "#") rolls++
            result += values[cell]!!
            rolled += roll+1
            rolls--
        }

        return arrayOf(result, rolled)
    }

}