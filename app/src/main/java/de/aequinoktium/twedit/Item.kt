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
    var equip_loc = emptyArray<String>()
    var price = 0f
    var container_name = ""
    var dmg = ""
    var chambers = 0
    var caliber = ""
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
    var dmg = ""
    var chambers = 0
    var caliber = ""
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
        var dmg = ""
        var selected = false
        var prefix = false
        var suffix = false
        var rename = false
    }
}