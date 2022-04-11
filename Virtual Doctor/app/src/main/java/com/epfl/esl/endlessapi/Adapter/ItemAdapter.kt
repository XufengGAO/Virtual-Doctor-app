package com.epfl.esl.endlessapi.Adapter

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.text.bold

import androidx.recyclerview.widget.RecyclerView
import com.epfl.esl.endlessapi.R


// This class contains some important functions to work with the RecyclerView
class ItemAdapter(val context: Context, val itemsText: MutableList<String>, val itemsValue: MutableList<String>) :
    RecyclerView.Adapter<ItemAdapter.MyViewHolder>(){

    // Click listener for delete button and edit buttons
    private lateinit var deleteListener : OnItemClickListener
    private lateinit var editListener : OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int, buttonView: View)
    }

    fun setOnItemClickListener(delete_listener: OnItemClickListener, edit_listener: OnItemClickListener){
        deleteListener = delete_listener
        editListener = edit_listener
    }

    // create new views
    // Inflates teh item views which is designed in xml layout file
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            // inflates the item custom row design view
            // that is used to hold list item
            LayoutInflater.from(context).inflate(
                R.layout.custom_recycleview_adapter,
                parent,
                false
            ), deleteListener, editListener
        )
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val itemsTextPosition = itemsText[position]
        val itemsValuePosition = itemsValue[position]
        // sets the text to the textview from our itemHolder class
        val symptomString = SpannableStringBuilder().bold { append(itemsTextPosition) }.append(" $itemsValuePosition")
        holder.tvItem.text = symptomString
    }

    // return the number of items in the list
    override fun getItemCount(): Int {
        return itemsText.size
    }

    // Holds the views for adding it to text items
    // define the click listener
    class MyViewHolder(itemView: View, delete_listener: OnItemClickListener,
                       edit_listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        var tvItem = itemView.findViewById<TextView>(R.id.SymptomInfo)!!

        init {
            itemView.findViewById<ImageButton>(R.id.deleteButton).setOnClickListener {
                delete_listener.onItemClick(adapterPosition, itemView.findViewById<ImageButton>(R.id.deleteButton))
            }
            itemView.findViewById<ImageButton>(R.id.editButton).setOnClickListener {
                edit_listener.onItemClick(adapterPosition,itemView.findViewById<ImageButton>(R.id.editButton))
            }
        }
    }

}