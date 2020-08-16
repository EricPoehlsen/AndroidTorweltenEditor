package de.aequinoktium.twedit

/**
 * a class to hold the data of a trait
 */
class TraitData {
    var id = 0
    var name = ""
    var xp = 0
    var txt = ""
    var min_rank = 0
    var max_rank = 0
    var cur_rank = 0
    var rank_xp = 0
    var total_xp = 0
    var cls = 0
    var grp = 0
    var effects = ""
    var variants: MutableMap<String, MutableMap<Int, TraitVariant>> = mutableMapOf()
    var complex = false
}

/**
 * a class to keep the data of a trait variant ...
 */
class TraitVariant {
    var var_id = 0
    var grp = ""
    var xp_factor = 0f
    var oper = 0
    var name = ""
    var txt = ""
    var selected = false
}

/**
 * a class to keep the characters variant of the trait
 */
class CharTrait {
    var id = 0
    var trait_id = 0
    var rank = 0
    var variants = ""
    var xp_cost = 0
    var xp_old = 0
    var name = ""
    var txt = ""
    var reduced = 0
    var effects = ""
}