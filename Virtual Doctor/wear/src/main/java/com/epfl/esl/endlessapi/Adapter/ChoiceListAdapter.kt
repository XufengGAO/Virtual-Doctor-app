package com.epfl.esl.endlessapi.Adapter

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.epfl.esl.endlessapi.R

class ChoiceListAdapter(val context: Context, val choiceText: MutableList<String>, val defaultPosition: Int) :
    RecyclerView.Adapter<ChoiceListAdapter.MyViewHolder>() {

    // Click listener for delete button and edit buttons
    private lateinit var setColorListener : OnItemClickListener
    private var selectedItemPosition: Int = defaultPosition

    interface OnItemClickListener {
        fun onItemClick(position: Int, textView: View)
    }

    fun setOnItemClickListener(setColor_listener: OnItemClickListener){
        setColorListener = setColor_listener
    }

    // create new views
    // Inflates teh item views which is designed in xml layout file
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            // inflates the item custom row design view
            // that is used to hold list item
            LayoutInflater.from(context).inflate(
                R.layout.history_item_adapter,
                parent,
                false
            ), setColorListener
        )
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val itemsTextPosition = choiceText[position]
        // sets the text to the textview from our itemHolder class
        if (position == 0) {
            holder.tvItem.text = SpannableStringBuilder().bold { append(itemsTextPosition) }
            holder.cardView.setBackgroundColor(Color.parseColor("#FF03DAC5"))
        } else {
            holder.tvItem.text = itemsTextPosition
        }
        holder.itemView.setOnClickListener {
            if(position != 0){
                selectedItemPosition = position
                Log.e(selectedItemPosition.toString(), choiceText[position])
                notifyDataSetChanged()
            }
        }

        if (position != 0) {
            if (selectedItemPosition == position){
                holder.cardView.setBackgroundColor(Color.parseColor("#DC746C"))
            } else {
                holder.cardView.setBackgroundColor(Color.parseColor("#E49B83"))
            }
        }
    }


    // return the number of items in the list
    override fun getItemCount(): Int {
        return choiceText.size
    }

    fun returnSelectedItemPosition(): Int {
        return  selectedItemPosition
    }

    // Holds the views for adding it to text items
    // define the click listener
    class MyViewHolder(itemView: View, setColor_listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        var tvItem = itemView.findViewById<TextView>(R.id.SymptomInfo)!!
        var cardView: LinearLayout = itemView.findViewById(R.id.cardView)
        init {
            itemView.setOnClickListener {
                setColor_listener.onItemClick(adapterPosition, itemView)
            }
        }
    }
}