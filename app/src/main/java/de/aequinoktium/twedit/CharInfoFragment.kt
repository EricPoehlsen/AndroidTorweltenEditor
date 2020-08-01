package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CharInfoFragment(private var layout: Int, private var dataset: String) : Fragment(),
                         EditInfoDialog.EditInfoDialogListener{
    private val c: CharacterViewModel by activityViewModels()

    private val core_views = mapOf(
        "species" to R.id.ci_species,
        "concept" to R.id.ci_concept,
        "homeworld" to R.id.ci_homeworld,
        "culture" to R.id.ci_culture,
        "notes" to R.id.ci_notes
    )

    private val desc_views = mapOf(
        "age" to R.id.ci_age,
        "size" to R.id.ci_height,
        "weight" to R.id.ci_weight,
        "sex" to R.id.ci_sex,
        "build" to R.id.ci_build,
        "eyecolor" to R.id.ci_eyecolor,
        "color1" to R.id.ci_color1,
        "color2" to R.id.ci_color2,
        "desc" to R.id.ci_desc
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(
            layout,
            container,
            false
        )





        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (dataset == "core") {
            for (info in c.info["core"]!!) {
                val tv = view.findViewById<TextView>(core_views[info.name]!!)
                tv.setText(HtmlCompat.fromHtml(info.txt, HtmlCompat.FROM_HTML_MODE_COMPACT))
                tv.setOnClickListener{v -> editInfo(v, info.info_id)}
            }
            val tv_name = view.findViewById<TextView>(R.id.ci_name)
            tv_name.setText(c.name)
            tv_name.setOnClickListener{v -> editInfo(v, -1)}
        } else if (dataset == "desc") {
            for (info in c.info["desc"]!!) {
                val tv = view.findViewById<TextView>(desc_views[info.name]!!)
                tv.setText(HtmlCompat.fromHtml(info.txt, HtmlCompat.FROM_HTML_MODE_COMPACT))
                tv.setOnClickListener{v -> editInfo(v, info.info_id)}
            }
        }

    }


    /**
     * Trigger the EditSkillDialog on a click
     */
    fun editInfo(view: View, info_id: Int) {
        val fm = this.parentFragmentManager
        val dialog = EditInfoDialog(info_id, dataset, c, view)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    override fun onEditInfoDialogPositiveClick(dialog: EditInfoDialog) {
        val v = dialog.v as TextView
        var txt = dialog.text
        val i_id = dialog.info_id
        val ds = dialog.dataset
        if (i_id > 0) {
            c.viewModelScope.launch(Dispatchers.IO) {
                txt = storeInfo(i_id, ds, txt)
                withContext(Dispatchers.Main) {
                    v.setText(HtmlCompat.fromHtml(txt, HtmlCompat.FROM_HTML_MODE_COMPACT))
                }
            }
        } else {
            c.viewModelScope.launch(Dispatchers.IO) {
                txt = update_name(txt)
                withContext(Dispatchers.Main) {
                    v.setText(txt)
                }
            }
        }
    }


    fun storeInfo(i_id: Int, ds: String, t: String): String {
        var txt = t

        // parse markdown to html
        var bold = false
        var italic = false
        var underline = false
        while ("**" in txt) {
            if (!bold) {
                txt = txt.replaceFirst("**", "<b>")
                bold = true
            } else {
                txt = txt.replaceFirst("**", "</b>")
                bold = false
            }
        }
        while ("//" in txt) {
            if (!italic) {
                txt = txt.replaceFirst("//", "<i>")
                italic = true
            } else {
                txt = txt.replaceFirst("//", "</i>")
                italic = false
            }
        }
        while ("__" in txt) {
            if (!underline) {
                txt = txt.replaceFirst("__", "<u>")
                underline = true
            } else {
                txt = txt.replaceFirst("__", "</U>")
                underline = false
            }
        }
        txt = txt.replace("\n", "<br/>")
        txt = txt.replace("'", "\u2019")

        // update SQLite database
        var sql = """
            UPDATE 
                char_info
            SET
                txt = '$txt'
            WHERE 
                id = $i_id
        """.trimIndent()
        c.db.execSQL(sql)

        // update loaded data
        for (i in c.info[ds]!!) {
            if (i.info_id == i_id) i.txt = txt
        }

        return txt
    }

    fun update_name(n: String): String {
        var new_name = n.replace("'", "\u2019")
        c.name = new_name

        var sql = """
            UPDATE
                char_core
            SET
                name = '$new_name'
            WHERE
                id = ${c.char_id}
        """.trimIndent()
        c.db.execSQL(sql)

        return new_name
    }




}