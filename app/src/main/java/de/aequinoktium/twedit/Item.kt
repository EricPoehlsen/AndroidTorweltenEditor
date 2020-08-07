package de.aequinoktium.twedit

import kotlinx.coroutines.channels.consumesAll

interface Wearable {
    fun dress() { }
    fun undress() {}


}


open class Item() {
    var id = 0
    var name = ""
    var desc = ""
    var weight = 0
    var volume = 0
    var packed = 0
    var quality = 0
    var attachable = false
    var packable = false
    var inner_capacity = 0
    var outer_capacity = 0
    var contents = mutableListOf<Item>()
    var attachments = mutableListOf<Item>()

    fun pack(item: Item) {

    }

    fun unpack(item: Item) {

    }

    /**
     * Retrieve all [Item]s that are attached to or packed inside this Item
     * @return all the items as a mutable list.
     */
    fun getContainedItems(): MutableList<Item> {
        val contained_items = mutableListOf<Item>()
        var has_content = ((contents.size + attachments.size) > 0)
        var cur_level = mutableListOf<Item>()
        for (item in contents) {
            cur_level.add(item)
        }
        for (item in attachments) {
            cur_level.add(item)
        }

        while (has_content) {
            val next_level = mutableListOf<Item>()
            for (item in cur_level) {
                contained_items.add(item)
                for (i in item.contents) {
                    next_level.add(i)
                }
                for (i in item.attachments) {
                    next_level.add(i)
                }
            }
            cur_level = next_level
            has_content = (cur_level.size > 0)
        }

        return contained_items
    }

    fun getTotalWeight(): Int {
        var weight = this.weight
        for (i in getContainedItems()) {
            weight += i.weight
        }
        return weight
    }

}



class Clothing() : Item(), Wearable {

}

fun test() {
    var a = Clothing()
    a.dress()

}