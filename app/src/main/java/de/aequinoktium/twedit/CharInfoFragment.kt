package de.aequinoktium.twedit

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CharInfoFragment(private var dataset: String) : Fragment(),
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
        val root: View

        if (dataset == "core") {
            root = inflater.inflate(
                R.layout.fragment_char_info_core,
                container,
                false
            )
        } else if (dataset == "desc") {
            root = inflater.inflate(
                R.layout.fragment_char_info_desc,
                container,
                false
            )
        } else {
            root = createLayout(c.info[dataset]!!.size)
        }

        return root
    }

    fun createLayout(size: Int): LinearLayout {
        val act = context as MainActivity

        var layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        val lp_ll = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(act.calc_dp(6),0,act.calc_dp(6), act.calc_dp(6))
        }
        layout.layoutParams = lp_ll



        val lp_label = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0,act.calc_dp(6),0,0)
        }

        val lp_data = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            weight = 1f
        }


        for (i in 1..size) {
            val tv_label = TextView(context)
            tv_label.textSize = 10f
            tv_label.layoutParams = lp_label
            tv_label.id = i * 1000

            val tv_data = TextView(context)
            tv_data.setBackgroundResource(R.color.colorPrimaryDark)
            tv_data.layoutParams = lp_data
            tv_data.setPadding(0,act.calc_dp(6),0,act.calc_dp(6))
            tv_data.id = i * 100

            layout.addView(tv_label)
            layout.addView(tv_data)
        }
        return layout
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
        } else {
            var i = 1
            for (info in c.info[dataset]!!) {
                val tv_label = view.findViewById<TextView>(i * 1000)
                val tv_data = view.findViewById<TextView>(i * 100)
                tv_data.setOnClickListener {v -> editInfo(v, info.info_id)}

                tv_label.text = info.name
                tv_data.setText(HtmlCompat.fromHtml(info.txt, HtmlCompat.FROM_HTML_MODE_COMPACT))
                i++
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