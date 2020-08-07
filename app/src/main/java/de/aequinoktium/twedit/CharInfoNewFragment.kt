package de.aequinoktium.twedit


import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import de.aequinoktium.twedit.CharacterViewModel.Info

/**
 * This [Fragment] is used to create a new CharInfo Dataset
 */
class CharInfoNewFragment(var adapter: CharInfoHostFragment.CharInfoFragmentAdapter) :
    Fragment(),
    TextWatcher
{
    private val c: CharacterViewModel by activityViewModels()
    private lateinit var ll: LinearLayout
    private lateinit var grp: EditText
    private lateinit var bt_add: Button
    private lateinit var bt_del: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View

           // Inflate the layout for this fragment
            root = inflater.inflate(
                R.layout.fragment_char_info_new,
                container,
                false
            )

        return root
    }

    /**
     * Setting up the Listeners ...
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ll = view.findViewById(R.id.cin_container)
        grp = view.findViewById(R.id.cin_grp_name)
        grp.addTextChangedListener(this)

        bt_add = view.findViewById(R.id.cin_add_field)
        bt_add.setOnClickListener { addField() }

        val bt_save = view.findViewById<Button>(R.id.cin_save)
        bt_save.setOnClickListener { saveGroup() }

        bt_del = view.findViewById(R.id.cin_delete)
        bt_del.setOnClickListener { deleteGroup() }
    }

    /**
     * The onClickListener that creates a new [EditText] in which
     * the user can enter the name for a field.
     */
    fun addField() {
        if (ll.getChildAt(0)?.id == R.id.cin_info) {
            ll.removeViewAt(0)
        }
        val act = activity as MainActivity
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            act.calc_dp(48)
        )

        val et = EditText(context)
        et.layoutParams = lp
        et.setHint(R.string.ci_new_field)
        ll.addView(et)

        if (ll.childCount >= 8) bt_add.isEnabled = false
    }

    /**
     * The onClickListener to save a group.
     * Prepares the data for database transaction and
     * calls the appropriate methods
     */
    fun saveGroup() {
        var dataset = ""
        var fields = arrayOf<String>()

        if (grp.text.length > 0) {
            dataset = grp.text.toString()
            dataset = dataset.replace("'", "\u2019")
        }

        for (v in ll.children) {
            var name = ""
            v as EditText
            if (v.text.length > 0) {
                name = v.text.toString()
                name = name.replace("'", "\u2019")
                fields += name
            }
        }

        c.viewModelScope.launch(Dispatchers.IO) {
            val result = writeData(dataset, fields)
            withContext(Dispatchers.Main) {
                displaySaved(result, dataset)
                val new_fragment = CharInfoFragment(dataset)
                adapter.addFragment(new_fragment)
            }
        }
    }

    /**
     * Writing a dataset into the SQL database after checks
     * @param dataset Name of the dataset
     * @param field an Array of the names for the fields
     * @return an integer error code
     *         0 = success
     *        -1 = no name given
     *        -2 = dataset exists
     *        -3 = empty fields array
     */
    suspend fun writeData(dataset: String, fields: Array<String>): Int {
        var result = 0
        if (dataset.length == 0) {
            result = -1
        } else if (dataset in c.info.keys) {
            result = -2
        } else if (fields.size == 0) {
            result = -3
        } else {
            c.info[dataset] = mutableListOf<Info>()
            var sql = """
                INSERT INTO 
                    char_info
                    (char_id, dataset, name)
                VALUES 
            """.trimIndent()
            for (f in fields) {
                val i = Info()
                i.name = f
                c.info[dataset]?.add(i)
                sql += " (${c.char_id}, '$dataset', '$f'),"
            }
            sql = sql.dropLast(1)
            c.db.execSQL(sql)

        }

        return result
    }

    /**
     * The onClickListener to handle the removal of a dataset
     */
    fun deleteGroup() {
        var dataset = grp.text.toString()
        dataset = dataset.replace("'", "\u2019")

        c.viewModelScope.launch(Dispatchers.IO) {
            val result = removeData(dataset)
            withContext(Dispatchers.Main) {
                displayRemoved(result, dataset)
                adapter.fragments = adapter.initialFragments()
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Removing a dataset from the SQLite database
     * @param dataset Name of the dataset
     * @return integer result
     *          0 = success
     *         -1 = no dataset name given
     *         -2 = tried to remove 'core' or 'desc' dataset
     */
    suspend fun removeData(dataset: String): Int {
        var result = 0
        if (dataset.length == 0) {
            result = -1
        } else if (dataset in arrayOf("core", "desc")) {
            result = -2
        } else {
            val sql = """
                DELETE FROM 
                    char_info
                WHERE
                    char_id = ${c.char_id}
                    AND
                    dataset = '$dataset'
            """.trimIndent()
            c.db.execSQL(sql)
            c.info.remove(dataset)
        }
        return result

    }


    /**
     * Create a [Snackbar] informing the user about the attempt to create a new dataset
     * @param result Integer return of the database operation 0 = success
     * @param dataset Name of the dataset
     */
    fun displaySaved(result: Int, dataset: String) {
        var info = ""
        var color = Color.RED
        when (result) {
            0 -> {
                info = resources.getString(R.string.ci_new_success_create, dataset)
                color = ContextCompat.getColor(this.requireContext(), R.color.colorBlue)
                ll.removeAllViews()
                grp.setText("")
                bt_add.isEnabled = true
            }
            -1 -> {
                info = resources.getString(R.string.ci_new_error_noname)
            }
            -2 -> {
                info = resources.getString(R.string.ci_new_error_exists)
            }
            -3 -> {
                info = resources.getString(R.string.ci_new_error_nofields)
            }
        }
        val sb = Snackbar.make(ll, info, Snackbar.LENGTH_LONG)
        sb.setTextColor(color)
        sb.show()
    }

    /**
     * Create a [Snackbar] informing the user about the attempt to delete a dataset
     * @param result Integer return of the database operation 0 = success
     * @param dataset Name of the dataset
     */
    fun displayRemoved(result: Int, dataset: String) {
        var info = ""
        var color = Color.RED
        when (result) {
            0 -> {
                info = resources.getString(R.string.ci_new_success_delete, dataset)
                color = ContextCompat.getColor(this.requireContext(), R.color.colorBlue)
                ll.removeAllViews()
                grp.setText("")
                bt_add.isEnabled = true
            }
            -1 -> {
                info = resources.getString(R.string.ci_new_error_noname)
            }
            -2 -> {
                info = resources.getString(R.string.ci_new_error_core)
            }
        }
        val sb = Snackbar.make(ll, info, Snackbar.LENGTH_LONG)
        sb.setTextColor(color)
        sb.show()
    }

    /**
     * Implementation of the [TextWatcher]
     * Writes the current value into the [skill] class
     */

        override fun afterTextChanged(text: Editable?) {
            val value = text.toString()
            if (value in c.info.keys) {
                grp.setTextColor(resources.getColor(R.color.colorBlue))

            } else {
                grp.setTextColor(Color.WHITE)
            }
        }

        // necessary implementations for TextWatcher
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

}