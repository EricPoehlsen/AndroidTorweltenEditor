package de.aequinoktium.twedit

import kotlinx.coroutines.channels.consumesAll

open class Item(private var c: CharacterViewModel) {
    var id = 0
    var name = ""
    var desc = ""
    var qty = 1
    var weight = 0
    var packed_into = 0
    var cur_qual = 7
    var orig_qual = 7
    var weight_limit = 0
    var price = 0f

    fun pack(item: Item) {
        this.packed_into = item.id
    }

    fun unpack(item: Item) {

    }


    fun getTotalWeight(): Int {
        var weight = this.weight
        for (i in getContents()) {
            weight += i.weight * i.qty
        }
        return weight
    }

    /**
     * retrieve an array of all packed items
     * @param levels how deep to search the tree 0 = unlimited
     * @return an array of [Item].
     */
    fun getContents(levels: Int = 0): Array<Item> {
        var loop = 0
        var result = arrayOf<Item>()
        var look_into = arrayOf(this.id)
        while (look_into.size > 0) {
            var next_look = arrayOf<Int>()
            for (item in c.getInventory()) {
                if (item.packed_into in look_into) {
                    result += item
                    next_look += item.id
                }
            }
            look_into = next_look
            loop++
            if (levels in 1..loop) break
        }
        return result
    }


}

class Clothing(c: CharacterViewModel): Item(c) {

}

class Tool(c: CharacterViewModel): Item(c) {

}

open class Weapon(c: CharacterViewModel): Item(c) {
    var damage = 0
    var penetration = 0
}

class MeleeWeapon(c: CharacterViewModel): Weapon(c) {

}

class RangedWeapon(c: CharacterViewModel): Weapon(c) {

}
