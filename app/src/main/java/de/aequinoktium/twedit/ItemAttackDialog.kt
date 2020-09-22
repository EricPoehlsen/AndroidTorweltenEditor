package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import java.util.*

/**
 * A DialogFrament that is used to modify an item damage code
 * @param item is the current item
 */
class ItemAttackDialog(val item: Item): DialogFragment() {
    internal lateinit var listener: DialogListener
    private lateinit var ll: LinearLayout
    private val skill = CharacterViewModel.Skill()
    private var attrib = 0
    private var modifier = 0
    private var rolls = arrayOf<Int>()

    var remove_skill = false
    var success = 0

    private val c: CharacterViewModel by activityViewModels()
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as DialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement DialogListener")
            )
        }
    }

    /**
     * Create the dialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater: LayoutInflater = this.layoutInflater
            val content: View = inflater.inflate(R.layout.dialog_item_attack, null)
            ll = content.findViewById(R.id.dia_item_attack)
            getSkill()
            showSkill()
            attribSelect()



            builder.setView(content)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    private fun showSkill() {
        val tv = centeredText()
        tv.text = skill.name
        tv.setOnLongClickListener { v -> removeSkill(v)}
        ll.addView(tv)
    }

    fun centeredText(): TextView {
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val tv = TextView(context)
        tv.layoutParams = lp
        tv.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        return tv
    }

    private fun attribSelect() {
        val attribs = arrayOf(
            getString(R.string.phy),
            getString(R.string.men),
            getString(R.string.soz),
            getString(R.string.nk),
            getString(R.string.fk)
        )

        val line = LinearLayout(context)
        line.orientation = LinearLayout.HORIZONTAL

        for (a in attribs) {
            val tv = button()
            tv.text = a
            tv.setOnClickListener{ v -> selectAttrib(v)}
            line.addView(tv)
        }
        ll.addView(line)
    }

    fun button():TextView {
        val margins = px(3).toInt()
        val padding = px(3).toInt()

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        lp.setMargins(margins, margins, margins, margins)
        val tv = TextView(context)
        tv.minEms = 2
        tv.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        tv.setTextColor(Color.WHITE)
        tv.setPadding(padding, padding, padding, padding)
        tv.setBackgroundColor(Color.GRAY)
        tv.layoutParams = lp

        return tv
    }


    private fun selectAttrib(view: View) {
        view as TextView
        val attrib_name = view.text.toString().toLowerCase(Locale.getDefault())
        attrib = c.attribs[attrib_name]?:0

        modifierSelect()
    }

    private fun modifierSelect() {
        ll.removeViewAt(1)
        val ranges = arrayOf(
            -6..-1,
            0..0,
            1..6
        )

        for (r in ranges) {
            val line = LinearLayout(context)
            line.orientation = LinearLayout.HORIZONTAL
            for (i in r) {
                val tv = button()
                tv.text = if (i < 1) "$i" else "+$i"
                tv.setOnClickListener { v -> selectModifier(v) }
                line.addView(tv)
            }
            ll.addView(line)
        }
    }

    fun selectModifier(view: View) {
        view as TextView
        modifier = view.text.toString().toInt()

        rollDice()
    }

    fun rollDice() {
        ll.removeViewAt(1)
        ll.removeViewAt(1)
        ll.removeViewAt(1)
        val dice = arrayOf(
            R.drawable.d12_1,
            R.drawable.d12_2,
            R.drawable.d12_3,
            R.drawable.d12_4,
            R.drawable.d12_5,
            R.drawable.d12_6,
            R.drawable.d12_7,
            R.drawable.d12_8,
            R.drawable.d12_9,
            R.drawable.d12_10,
            R.drawable.d12_11,
            R.drawable.d12_12
        )

        val line = LinearLayout(context)
        line.orientation = LinearLayout.HORIZONTAL
        for (i in 1..skill.lvl+1) {
            val roll = Random().nextInt(12)
            rolls += roll+1

            val lp = LinearLayout.LayoutParams(
                px(48).toInt(),
                px(48).toInt(),
                1f
            )
            val iv = ImageView(context)
            iv.setImageResource(dice[roll])
            iv.layoutParams = lp
            line.addView(iv)
        }
        ll.addView(line)

        showModifier()
        showResult()
    }

    private fun showModifier() {
        var text = getString(R.string.dialog_item_attack_modifier)
        val mod = if (modifier < 1) "$modifier" else "+$modifier"
        text = "$text: $mod"
        val tv = centeredText()
        tv.text = text
        ll.addView(tv)
    }

    private fun showResult() {
        val line = LinearLayout(context)

        line.orientation = LinearLayout.HORIZONTAL
        val red = ContextCompat.getColor(requireContext(), R.color.Red)
        val green = ContextCompat.getColor(requireContext(), R.color.Green)

        for (i in rolls) {
            val tv = button()
            val lp = LinearLayout.LayoutParams(tv.layoutParams)
            lp.weight = 1f
            tv.layoutParams = lp
            var result = i + modifier
            result = Math.min(result, 12)
            result = Math.max(result, 1)
            tv.text = result.toString()
            if (result <= attrib) {
                tv.setBackgroundColor(green)
                success += 1
            } else {
                tv.setBackgroundColor(red)
            }
            line.addView(tv)
        }
        ll.addView(line)

    }

    private fun removeSkill(view: View): Boolean {
        view as TextView
        view.setPaintFlags(view.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
        val red = ContextCompat.getColor(requireContext(), R.color.Red)
        view.setTextColor(red)
        remove_skill = true
        return true
    }

    fun getSkill() {
        val no_skill = CharacterViewModel.Skill().apply {
            id=0
            name = getString(R.string.dialog_item_skill_none)
        }
        val skills = arrayOf(no_skill) + c.char_skills
        for (s in skills) {
            if (item.skill == s.id) {
                skill.name = s.name
                skill.lvl = s.lvl
            }
        }
    }

    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density
}

