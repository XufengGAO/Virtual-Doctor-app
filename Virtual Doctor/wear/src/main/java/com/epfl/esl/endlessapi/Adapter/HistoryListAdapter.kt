package com.epfl.esl.endlessapi.Adapter

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.epfl.esl.endlessapi.R

class HistoryListAdapter(val context: Context, val itemsText: MutableList<String>, val itemsValue: MutableList<String>) :
    RecyclerView.Adapter<HistoryListAdapter.MyViewHolder>(){

    interface OnItemClickListener {
        fun onItemClick(position: Int, buttonView: View)
    }

    fun setOnItemClickListener(delete_listener: OnItemClickListener){

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
            )
        )
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val itemsTextPosition = itemsText[position]
        val itemsValuePosition = itemsValue[position]
        // sets the text to the textview from our itemHolder class
        val symptomString = SpannableStringBuilder().bold { append(itemsTextPosition) }.append(" $itemsValuePosition")
        holder.tvItem.text = symptomString
        holder.cardView.setBackgroundColor(Color.parseColor("#e8f2b9" ))
    }

    // return the number of items in the list
    override fun getItemCount(): Int {
        return itemsText.size
    }

    // Holds the views for adding it to text items
    // define the click listener
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardView: LinearLayout = itemView.findViewById(R.id.cardView)
        var tvItem = itemView.findViewById<TextView>(R.id.SymptomInfo)!!

        init {
        }
    }

}