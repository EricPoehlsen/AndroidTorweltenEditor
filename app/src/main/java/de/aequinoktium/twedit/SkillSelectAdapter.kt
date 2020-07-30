package de.aequinoktium.twedit

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SkillSelectAdapter(
    private var data: Array<SkillSelectFragment.SkillData>,
    private val c: CharacterViewModel) :
        RecyclerView.Adapter<SkillSelectAdapter.ViewHolder>() {

    private var col_select = 0
    private var col_standard = 0

    class ViewHolder(val sv: SkillSelectView) : RecyclerView.ViewHolder(sv)

    /**
     * Create a new view holder
     * use this call to access the colors for styling
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val sv = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_skill_select, parent, false) as SkillSelectView

        if (col_select == 0) {
            col_select = ContextCompat.getColor(parent.context, R.color.colorBlue)
        }
        if (col_standard == 0) {
            col_standard = ContextCompat.getColor(parent.context, R.color.colorLite)
        }
        return ViewHolder(sv)
    }

    /**
     * When binding a value to a view assign the correct formatting and data
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.sv.text = data[position].name
        holder.sv.skill_id = data[position].id
        holder.sv.spec = data[position].spec
            if (data[position].spec == 1) holder.sv.setTypeface(null, Typeface.BOLD)
            if (data[position].spec == 3) holder.sv.setTypeface(null, Typeface.ITALIC)

        holder.sv.is_active = (data[position].is_active == 1)

        if (data[position].activated) {
            holder.sv.is_activated = (data[position].activated)
            holder.sv.setTextColor(col_select)
        }
        if (data[position].has_level) {
            val act = holder.sv.context as MainActivity // we need calc_dp() below
            holder.sv.has_lvl = data[position].has_level
            holder.sv.setTextColor(col_select)
            val lock = holder.sv.context.resources.getDrawable(R.drawable.lock_blue, null)
            lock.setBounds(0,0,act.calc_dp(16),act.calc_dp(16))
            holder.sv.setCompoundDrawables(lock, null, null, null);
        }

        holder.sv.setOnClickListener { v -> updateSkills(v) }

    }

    /**
     * Once a view is scrolled out of bounds and is recycled make sure that it is
     * returned to pristine conditions before it is reused ...
     */
    override fun onViewRecycled(holder: ViewHolder) {
        holder.sv.text = ""
        holder.sv.skill_id = 0
        holder.sv.spec = 0
        holder.sv.setTypeface(null, Typeface.NORMAL)
        holder.sv.setTextColor(col_standard)
        holder.sv.setCompoundDrawables(null, null, null, null)
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * Update the characters skills after clicking an entry
     */
    fun updateSkills(v: View) {
        val sv = v as SkillSelectView
        if (sv.is_activated) {
            if (!sv.has_lvl) {
                sv.setTextColor(col_standard)
                sv.is_activated = false
                c.viewModelScope.launch(Dispatchers.IO) {
                    val sql = """
                        DELETE FROM 
                            char_skills 
                        WHERE 
                            skill_id = ${sv.skill_id}
                            AND
                            char_id = ${c.char_id}
                    """.trimIndent()
                    c.db.execSQL(sql)
                }
            }
        } else {
            sv.setTextColor(col_select)
            sv.is_activated = true
            c.viewModelScope.launch(Dispatchers.IO) {
                val sql = """
                        INSERT INTO 
                            char_skills 
                            (char_id, skill_id)
                        VALUES
                            (${c.char_id}, ${sv.skill_id})
                    """.trimIndent()
                c.db.execSQL(sql)
            }
        }
    }

    /**
     * Update the dataset from which the views are generated
     */
    fun updateData(data: Array<SkillSelectFragment.SkillData>) {
        this.data = data
        this.notifyDataSetChanged()
    }

}