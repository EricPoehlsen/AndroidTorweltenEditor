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
    var char_id: Int = 0
    val LOCALE = Locale.GERMAN
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
            xp_used = data.getInt(data.getColumnIndex("xp_used"))
            xp_total = data.getInt(data.getColumnIndex("xp_total"))
            name = data.getString(data.getColumnIndex("name"))
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

    suspend fun loadInventory() {
        val items = mutableListOf<Item>()

        val sql = "SELECT * FROM char_items WHERE char_id = ${char_id}"
        val data = db.rawQuery(sql, null)

        while (data.moveToNext()) {
            var item = Item()

            val cls = data.getString(data.getColumnIndex("cls"))

            item.id = data.getInt(data.getColumnIndex("id"))
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
            val datasets = extra_data.split(",")
            for (value in datasets) {
                if (value.startsWith("cnt:")) {
                    item.container_name = value.split(":")[1]
                }
                if (value.startsWith("dmg:")) {
                    item.dmg = value.split(":")[1]
                }
                if (value.startsWith("dmg_mod:")) {
                    item.dmg_mod = value.split(":")[1]
                }

            }


            items.add(item)
        }

        data.close()
        this.inv = items
    }

    /**
     * Retrieve the characters current items
     * return an array of [Item]
     */
    fun getInventory(): Array<Item> {
        return inv.toTypedArray()
    }

    fun prepareItem(item: Item): ContentValues {
        var extra_data = ""
        if (item.container_name.length > 0) extra_data += "cnt:${item.container_name},"
        if (!item.dmg.isBlank()) extra_data += "dmg:${item.dmg},"
        if (!item.dmg_mod.isBlank()) extra_data += "dmg_mod:${item.dmg_mod},"


        val cv = ContentValues()
        cv.put("name", item.name)
        cv.put("qty", item.qty)
        cv.put("desc", item.desc)
        cv.put("current_quality", item.cur_qual)
        cv.put("original_quality", item.orig_qual)
        cv.put("weight", item.weight)
        cv.put("price", item.price)
        cv.put("weight_limit", item.weight_limit)
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
        val sql = """
            UPDATE char_items 
            SET 
                packed_into=0
            WHERE
                id=${item.id}
        """.trimIndent()
        db.execSQL(sql)
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
     * View Model is destroyed. Clean up the database connection.
     */
    override fun onCleared() {
        db.close()
        super.onCleared()
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