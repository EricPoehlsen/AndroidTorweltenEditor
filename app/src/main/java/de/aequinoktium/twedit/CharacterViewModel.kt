package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Primary ViewModel for anything related to the character
 * handles some database and network IO.
 */

class CharacterViewModel: ViewModel() {
    var char_id = 0
    val LOCALE = Locale.getDefault()
    lateinit var db: SQLiteDatabase
    var name: String = ""
    var attribs = mutableMapOf(
        "phy" to 0,
        "men" to 0,
        "soz" to 0,
        "nk" to 0,
        "fk" to 0,
        "lp" to 0,
        "ep" to 0,
        "mp" to 0
    )
    var vitals = mutableMapOf(
        "lp" to 0f,
        "ep" to 0f,
        "mp" to 0f
    )
    var info = mutableMapOf(
        "core" to mutableListOf<Info>(),
        "desc" to mutableListOf<Info>()
    )

    private var inv = mutableListOf<Item>()
    var xp_used: Int = 0
    var xp_total: Int = 0

    var edit_trait = 0
    var char_traits = emptyArray<Int>()

    lateinit var current_item: Item

    private var accounts = mutableListOf<Account>()

    private var deleted = false

    var string_values = mutableMapOf<String, String>()

    fun setDatabase(db: SQLiteDatabase) {
        this.db = db
    }

    suspend fun loadCharData(char_id: Int) {
        this.char_id = char_id
        var sql = "SELECT * FROM char_core WHERE id = $char_id"
        var data: Cursor = db.rawQuery(sql, null)
        if (data.count == 1) {
            data.moveToFirst()
            val attrib_names = arrayOf("phy", "men", "soz", "nk", "fk", "lp", "ep", "mp")
            for (n in attrib_names) {
                attribs[n] = data.getInt(data.getColumnIndex(n))
            }
            val vitals_names = arrayOf("lp_cur", "ep_cur", "mp_cur")
            for (n in vitals_names) {
                vitals[n] = data.getFloat(data.getColumnIndex(n))
            }
            xp_used = data.getInt(data.getColumnIndex("xp_used"))
            xp_total = data.getInt(data.getColumnIndex("xp_total"))
            name = data.getString(data.getColumnIndex("name"))
            deleted = (data.getInt(data.getColumnIndex("deleted")) == 1)
        }
        data.close()

        sql = "SELECT trait_id FROM char_traits WHERE char_id = $char_id"
        data = db.rawQuery(sql, null)
        while (data.moveToNext()) {
            char_traits += data.getInt(0)
        }

        data.close()

        loadInfo()
        loadAccounts()
        loadInventory()
    }

    /**
     * Load character info from the database
     * Initialize the core fields on first load
     */
    suspend fun loadInfo() {
        var reload = false
        var sql = """
            SELECT
                id,
                name, 
                txt, 
                dataset
            FROM
                char_info
            WHERE
                char_id = $char_id 
        """.trimIndent()
        val data = db.rawQuery(sql, null)
        while (data.moveToNext()) {
            val dataset = data.getString(3)
            val i = Info()
            i.info_id = data.getInt(0)
            i.name = data.getString(1)
            i.txt = data.getString(2)
            if (info[dataset] != null) {
                info[dataset]?.add(i)
            } else {
                info[dataset] = mutableListOf(i)
            }
        }
        data.close()

        if (info["core"]?.size == 0) {
            sql = """
                INSERT INTO
                    char_info
                    (char_id, name, dataset)
                VALUES
                    ($char_id, 'species', 'core'),
                    ($char_id, 'concept', 'core'),
                    ($char_id, 'homeworld', 'core'),
                    ($char_id, 'culture', 'core'),
                    ($char_id, 'notes', 'core')
            """.trimIndent()
            db.execSQL(sql)
            reload = true
        }

        if (info["desc"]?.size == 0) {
            sql = """
                INSERT INTO
                    char_info
                    (char_id, name, dataset)
                VALUES
                    ($char_id, 'age', 'desc'),
                    ($char_id, 'size', 'desc'),
                    ($char_id, 'weight', 'desc'),
                    ($char_id, 'sex', 'desc'),
                    ($char_id, 'build', 'desc'),
                    ($char_id, 'eyecolor', 'desc'),
                    ($char_id, 'color1', 'desc'),
                    ($char_id, 'color2', 'desc'),
                    ($char_id, 'desc', 'desc')
            """.trimIndent()
            db.execSQL(sql)
            reload = true
        }

        if (reload) {
            loadInfo()
        }
    }

    /**
     * Update an attribute in the database and the ViewModel
     * @param attr: the attribute
     * @param new_value: the new value for this attribute
     * @param xp_cost: the xp cost for the modification
     */
    fun updateAttrib(attr: String, new_value: Int, xp_cost: Int) {
        attribs[attr] = new_value
        val data = ContentValues()
        data.put(attr, new_value)

        this.viewModelScope.launch(Dispatchers.IO) {
            db.update("char_core", data, "id = $char_id", null)

            val sql = """
                UPDATE char_core 
                SET xp_used = xp_used + ${xp_cost}
                WHERE id = ${char_id}
            """.trimIndent()
            db.execSQL(sql)
        }
    }

    fun updateVital(attr: String, new_value: Float) {
        vitals[attr] = new_value
        val sql = """
            UPDATE char_core
            SET ${attr}_cur = $new_value
            WHERE id = $char_id
        """.trimIndent()
        this.viewModelScope.launch(Dispatchers.IO) {
            db.execSQL(sql)
        }
    }

    suspend fun loadInventory() {
        val items = mutableListOf<Item>()

        val sql = "SELECT * FROM char_items WHERE char_id = ${char_id}"
        val data = db.rawQuery(sql, null)

        while (data.moveToNext()) {
            val item = Item()



            item.id = data.getInt(data.getColumnIndex("id"))
            item.cls = data.getString(data.getColumnIndex("cls"))
            item.name = data.getString(data.getColumnIndex("name"))
            item.desc = data.getString(data.getColumnIndex("desc"))
            item.qty = data.getInt(data.getColumnIndex("qty"))
            item.weight = data.getInt(data.getColumnIndex("weight"))
            item.equipped = data.getInt(data.getColumnIndex("equipped"))
            item.weight_limit = data.getInt(data.getColumnIndex("weight_limit"))
            item.orig_qual = data.getInt(data.getColumnIndex("original_quality"))
            item.cur_qual = data.getInt(data.getColumnIndex("current_quality"))
            item.price = data.getFloat(data.getColumnIndex("price"))
            item.packed_into = data.getInt(data.getColumnIndex("packed_into"))

            val extra_data = data.getString(data.getColumnIndex("extra_data"))
            val datasets = extra_data.split("|")
            for (value in datasets) {
                if (value.startsWith("cnt:")) {
                    item.container_name = value.split(":")[1]
                }
                if (value.startsWith("dmg:")) {
                    item.dmg = Damage(value.split(":")[1])
                }
                if (value.startsWith("var.")) {
                    val split = value.split(":")
                    val var_name = split[0].replace("var.","")
                    if (var_name == string_values["mat"]) item.material = split[1]
                }
                if (value.startsWith("chambers:")) {
                    item.chambers = value.split(":")[1].toInt()
                }
                if (value.startsWith("chambered:")) {
                    val chambered = value.replace("chambered:", "")
                    val ammo_ids = chambered.split(".")
                    for (ammo_id in ammo_ids) {item.chambered += ammo_id.toInt()}
                }
                if(value.startsWith("caliber:")) {
                    val caliber = value.replace("caliber:", "")
                    val caliber_data = caliber.split(".")
                    if (caliber_data.size == 2) item.caliber = caliber_data.toTypedArray()
                }
                if(value.startsWith("capacity:")) {
                    item.capacity = value.split(":")[1].toInt()
                }
                if (value.startsWith("clip:")) {
                    item.clip = value.split(":")[1].toInt()
                }
            }
            items.add(item)
        }

        data.close()

        this.inv = items


        for (item in inv) {
            item.has_contents = getItemContents(item,1).size != 0
        }

    }

    /**
     * Retrieve the characters current items
     * return an array of [Item]
     */
    fun getInventory(): Array<Item> {
        return inv.toTypedArray()
    }

    /**
     * Retrieves an item by its id
     * @param id: Integer id
     * @return an [Item] from the inventory (empty item with id=0 if none found)
     */
    fun getItemById(id: Int): Item {
        var result = Item()
        for (i in getInventory()) {
            if (i.id == id) {
                result = i
                break
            }
        }
        return result
    }

    /**
     * Prepares an item for storage in the database
     * @param item: [Item] to store
     * @return [ContentValues] to write the the database
     */
    fun prepareItem(item: Item): ContentValues {
        var extra_data = ""
        if (item.container_name.length > 0) extra_data += "cnt:${item.container_name}|"
        if (!item.dmg.isEmpty()) extra_data += "dmg:${item.dmg}|"
        if (!item.color.isBlank()) extra_data += "col:${item.color}|"
        if (!item.material.isBlank()) {
            val mat_name = string_values["mat"]
            extra_data += "var.$mat_name:${item.material}|"
        }
        if (item.chambers > 0) extra_data += "chambers:${item.chambers}|"
        if (item.chambered.size > 0) {
            var chambered_rounds = "chambered:"
            for (id in item.chambered) chambered_rounds += "${id}."
            extra_data = chambered_rounds.drop(1)  + "|"
        }
        if (!item.caliber[0].isEmpty()) {
            extra_data += "caliber:${item.caliber[0]}.${item.caliber[1]}|"
        }
        if (item.capacity > 0) {
            extra_data += "capacity:${item.capacity}|"
        }
        if (item.clip > -1) {
            extra_data += "clip:${item.clip}|"
        }

        val cv = ContentValues()
        cv.put("name", item.name)
        cv.put("qty", item.qty)
        cv.put("cls", item.cls)
        cv.put("desc", item.desc)
        cv.put("equipped", item.equipped)
        cv.put("packed_into", item.packed_into)
        cv.put("current_quality", item.cur_qual)
        cv.put("original_quality", item.orig_qual)
        cv.put("weight", item.weight)
        cv.put("price", item.price)
        cv.put("weight_limit", item.weight_limit)
        cv.put("equip_loc", item.equip_loc.joinToString("."))
        cv.put("extra_data", extra_data)

        cv.put("char_id", char_id)

        return cv
    }

    /**
     * Adds an item to the character inventory.
     */
    suspend fun addToInventory(item: Item) {
        val cv = prepareItem(item)

        val row_id = db.insert("char_items",null, cv)
        item.id = row_id.toInt()
        inv.add(item)
    }

    suspend fun updateItem(item: Item) {
        val cv = prepareItem(item)
        db.update("char_items", cv, "id=${item.id}", null)
    }

    suspend fun packItem(item: Item) {
        updateItemHasContents(getItemById(item.packed_into))
        val sql = """
            UPDATE char_items 
            SET 
                packed_into=${item.packed_into},
                equipped=0
            WHERE
                id=${item.id}
        """.trimIndent()
        db.execSQL(sql)
    }

    suspend fun unpackItem(item: Item) {
        updateItemHasContents(getItemById(item.packed_into))
        val sql = """
            UPDATE char_items 
            SET 
                packed_into=0
            WHERE
                id=${item.id}
        """.trimIndent()
        db.execSQL(sql)
    }

    fun updateItemHasContents(item: Item) {
        item.has_contents = getItemContents(item,1).size != 0
    }

    suspend fun removeItem(item: Item) {
        inv.remove(item)
        val sql = """
            DELETE FROM char_items
            WHERE id = ${item.id}
        """.trimIndent()
        db.execSQL(sql)
    }

    /**
     * Equipping or unequipping an item
     * @return 0: unequipped 1: equipped
     */
    fun equipItem(item: Item): Int {
        if (item.equipped == 1) {
            item.equipped = 0
        } else {
            item.equipped = 1
        }
        this.viewModelScope.launch(Dispatchers.IO) {
            updateItem(item)
        }
        return item.equipped
    }

    /**
     * Retrieves the [Item]s content weights plus the own weight
     * @param item: The [Item] of interest
     * @return the total weight in grams
     */
    fun getItemTotalWeight(item: Item): Int = item.weight + getItemContentWeight(item)

    /**
     * Retrieve the weight of the contents packed into an item
     * @param item: the [Item] which contents are of interest to us
     * @return the weight of contents in grams
     */
    fun getItemContentWeight(item: Item): Int {
        for (i in getItemContents(item)) {
            item.weight += i.weight * i.qty
        }
        return item.weight
    }

    /**
     * retrieve an array of all packed items
     * @param levels how deep to search the tree 0 = unlimited
     * @param item the [Item] which contents are of interest
     * @return an array of [Item].
     */
    fun getItemContents(item: Item, levels: Int = 0): Array<Item> {

        var loop = 0
        var result = arrayOf<Item>()
        var look_into = arrayOf(item.id)
        while (look_into.size > 0) {

            var next_look = arrayOf<Int>()
            for (i in getInventory()) {
                if (i.packed_into in look_into) {
                    result += i
                    next_look += i.id
                }
            }
            look_into = next_look
            loop++
            if (levels in 1..loop) break
        }
        return result
    }

    /**
     * loads the characters accounts and current balances
     */
    suspend fun loadAccounts() {
        // find char accounts
        var sql = "SELECT * FROM accounts WHERE char_id=$char_id"
        val data = db.rawQuery(sql, null)
        while (data.moveToNext()) {
            val acc = Account()
            acc.nr = data.getInt(0)
            acc.name = data.getString(2)
            accounts.add(acc)
        }
        data.close()

        Log.d("info", "accounts: ${accounts.size}")

        // retrieve account balances
        for (acc in accounts) {
            sql = """
                SELECT (
                    (SELECT COALESCE(SUM(amount),0) FROM money_transfers WHERE target_acc = ${acc.nr}) 
                    - (SELECT COALESCE(SUM(amount),0) FROM money_transfers WHERE origin_acc = ${acc.nr})
                )
            """.trimIndent()
            val acc_value = db.rawQuery(sql, null)
            if (acc_value.moveToFirst()) {
                acc.balance = acc_value.getFloat(0)
                Log.d("info", "Account: ${acc.name} - Amount: ${acc.balance}")
            }
            acc_value.close()
        }

        // create primary account if necessary
        if (accounts.isEmpty()) {
            val acc = Account()
            acc.name = "primary"

            val cv = ContentValues()
            cv.put("name", acc.name)
            cv.put("char_id", char_id)

            val row_id = db.insert("accounts",null, cv)
            acc.nr = row_id.toInt()

            accounts.add(acc)
        }
    }

    fun primaryAccount(): Account {
        return accounts[0]
    }

    /**
     * Execute a money transaction between two accounts.
     * transaction is only passed to the database if either the originating
     * or targeted account belong to this character.
     * There are no checks if the second account of the transaction exists
     * or who owns it!
     * @param origin account number of the origin account
     * @param target account number of the target account
     * @param amount the amount to be transferred
     * @param purpose optional string to explain the transaction
     * @return check result of the account
     */
    suspend fun moneyTransaction(
        origin: Int,
        target: Int,
        amount: Float,
        purpose: String = ""): Boolean
    {
        var valid_account = false
        for (acc in accounts) {
            if (acc.nr == origin) {
                acc.balance -= amount
                valid_account = true
            }
            if (acc.nr == target) {
                acc.balance += amount
                valid_account = true
            }
        }
        if (valid_account) {
            val sql = """
            INSERT INTO
                money_transfers
                (origin_acc, target_acc, amount, purpose)
            VALUES
                ($origin, $target, $amount, '$purpose')
            """.trimIndent()
            db.execSQL(sql)
        }

        return valid_account
    }

    /**
     * is the character marked as deleted?
     * @return true if marked
     */
    fun isDeleted():Boolean {
        return deleted
    }

    /**
     *  Restores a character that was marked as deleted
     */
    suspend fun restore() {
        name = name.drop(1)
        deleted = false
        val sql = """
            UPDATE char_core
            SET
                name = '$name',
                deleted = 0
            WHERE
                id = $char_id
        """.trimIndent()
        db.execSQL(sql)
    }

    class Info() {
        var info_id = 0
        var name = ""
        var txt = ""
    }

    class Account() {
        var nr = 0
        var name = ""
        var balance = 0f
    }
}