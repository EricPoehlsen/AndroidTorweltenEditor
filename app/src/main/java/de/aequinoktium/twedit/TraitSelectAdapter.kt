package de.aequinoktium.twedit

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TraitSelectAdapter(
    private var data: Array<TraitData>,
    private val c: CharacterViewModel) :
        RecyclerView.Adapter<TraitSelectAdapter.ViewHolder>() {

    private var col_select = 0
    private var col_standard = 0

    class ViewHolder(val tv: TraitView) : RecyclerView.ViewHolder(tv)

    /**
     * Create a new view holder
     * use this call to access the colors for styling
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val tv = TraitView(parent.context)

        if (col_select == 0) {
            col_select = ContextCompat.getColor(parent.context, R.color.Blue)
        }
        if (col_standard == 0) {
            col_standard = ContextCompat.getColor(parent.context, R.color.Grey)
        }
        return ViewHolder(tv)
    }

    /**
     * When binding a value to a view assign the correct formatting and data
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trait = data[position]
        val view = holder.tv

        view.c = c
        view.setTrait(trait)
    }

    /**
     * Once a view is scrolled out of bounds and is recycled make sure that it is
     * returned to pristine conditions before it is reused ...
     */
    override fun onViewRecycled(holder: ViewHolder) {
        holder.tv.resetView()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * Update the dataset from which the views are generated
     */
    fun updateData(data: Array<TraitData>) {
        this.data = data
        this.notifyDataSetChanged()
    }

}