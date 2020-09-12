package de.aequinoktium.twedit

import android.util.Log
import java.util.*


class Item() {
    var id = 0
    var name = ""
    var desc = ""
    var qty = 1
    var weight = 0
    var packed_into = 0
    var cur_qual = 6
    var orig_qual = 6
    var weight_limit = 0
    var equipped = 0
    var equip_loc = emptyArray<String>()
    var price = 0f
    var container_name = ""
    var has_contents = false
    var dmg = Damage()
    var chambers = 0
    var chambered = arrayOf<Int>() // item.id
    var caliber = arrayOf("","")  // type, size
    var color = ""
    var material = ""
    var cls = ""


    fun copy(): Item {
        val new = Item()
        new.name = this.name
        new.desc = this.desc
        new.qty = this.qty
        new.weight = this.weight
        new.packed_into = this.packed_into
        new.cur_qual = this.cur_qual
        new.orig_qual = this.orig_qual
        new.weight_limit = this.weight_limit
        new.equipped = this.equipped
        new.price = this.price
        new.container_name = this.container_name
        new.dmg = this.dmg
        new.chambers = 0
        new.caliber = this.caliber
        new.color = this.color
        new.material = this.material
        new.cls = this.cls
        new.equip_loc = this.equip_loc

        return new
    }

    /**
     * checks if the item equals this item
     * disregards qty and id
     * @param item: The [Item] to compare
     * @return true if 'equal'
     */
    fun eq(item: Item): Boolean {
        return (
            item.name == this.name &&
            item.desc == this.desc &&
            item.weight == this.weight &&
            item.packed_into == this.packed_into &&
            item.cur_qual == this.cur_qual &&
            item.orig_qual == this.orig_qual &&
            item.weight_limit == this.weight_limit &&
            item.equipped == this.equipped &&
            item.price == this.price &&
            item.container_name == this.container_name &&
            item.dmg == this.dmg &&
            item.chambers == 0 &&
            item.caliber == this.caliber &&
            item.color == this.color &&
            item.material == this.material &&
            item.cls == this.cls
        )
    }
}

class CatalogItem() {
    var name = ""
    var desc = ""
    var weight = 0
    var avail = 0
    var weight_limit = 0
    var price = 0f
    var container_name = ""
    var dmg = Damage()
    var chambers = 0
    var caliber = arrayOf("","")
    var color = ""
    var material = ""
    var variants = mutableMapOf<String,Array<Variant>>()
    var cls = ""
    var grp = emptyArray<String>()
    var equip_loc = emptyArray<String>()

    class Variant(){
        var name = ""
        var price_factor = 1f
        var weight_factor = 1f
        var weight_limit = 0
        var dmg_mod = ""
        var dmg = Damage()
        var selected = false
        var prefix = false
        var suffix = false
        var rename = false
    }
}

/**
 * A class to store the damage value
 * Damage is written as S/D(/T) in the rules
 * @param s is "Schaden" (damage) as integer on a roll it defines the number of dice
 * @param d is "Durchschlag" (penetration) an integer -7..+7
 * @param t is damage type P=physical E=energy M=morale (empty = P)
 * @param mod Boolean - if true the damage value is meant to modify another damage value
 *
 * @param dmg_string damage as string representation
 *                   if the first char is '±', '+' or '-' it is damage modifier
 */
class Damage {
    constructor(){}
    constructor(dmg_string: String) {
        init(dmg_string)
    }
    constructor(s:Int, d:Int, t:String, mod:Boolean=false) {
        init(s,d,t,mod)
    }

    var s: Int = 0
    var d: Int = 0
    var t: String = ""
    var mod: Boolean = false

    // returns true if this Damage is 'empty'
    fun isEmpty(): Boolean = (s == 0 && d == 0)

    // returns a string representation of the damage
    override fun toString():String {
        var s_mod = ""
        var d_mod = ""
        var type = ""
        if (mod) {
            if (s == 0) s_mod = "±"
            if (s > 0) s_mod = "+"
            if (d == 0) d_mod = "±"
            if (d > 0) d_mod = "+"
        }
        if (t.length == 1) {
            type = "/$t"
        }

        return "$s_mod$s/$d_mod$d$type"
    }

    // allows adding two damage values
    operator fun plus(b: Damage):Damage {
        s += b.s
        d += b.d
        t = b.t
        return this
    }

    // init base on input string - parses the string
    fun init(input: String) {
        val dmg_elements = input.split("/")
        if (dmg_elements.size >= 2) {
            if(dmg_elements[0].length >= 1) {
                var s_string = dmg_elements[0]
                if (s_string.first() in "±+-") {
                    mod = true
                    s_string = s_string.replace("±", "")
                }
                s = s_string.toInt()
            }
            if(dmg_elements[1].matches("[+-]?\\d+".toRegex())) {
                d = dmg_elements[1].toInt()
            }
        }
        if (dmg_elements.size == 3) {
            if(dmg_elements[2].matches("[pPeEmM]".toRegex())) {
                t = dmg_elements[2].toUpperCase(Locale.getDefault())
            }
        }
    }

    fun init(s:Int, d:Int, t:String, mod:Boolean) {
        this.s = s
        this.d = d
        this.t = t
        this.mod = mod
    }
}