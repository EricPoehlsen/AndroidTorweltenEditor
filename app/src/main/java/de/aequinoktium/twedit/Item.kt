package de.aequinoktium.twedit


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
    var price = 0f
    var container_name = ""
    var dmg = ""
    var dmg_mod = ""
    var chambers = 0
    var caliber = ""
    val color = ""
    var material = ""


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
        new.dmg_mod = this.dmg_mod

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
            item.dmg_mod == this.dmg_mod
        )
    }
}
