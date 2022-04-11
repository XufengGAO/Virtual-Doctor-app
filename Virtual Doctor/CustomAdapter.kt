package com.epfl.esl.endlessapi.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.Symptoms_Detail_Provider.Model
import com.epfl.esl.sportstracker.ItemAdapter

class CustomAdapter(val context: Context, val featureList: MutableList<String>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>(){

    // create new views
    // Inflates teh item views which is designed in xml layout file
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        return CustomAdapter.ViewHolder(
            // inflates the item custom row design view
            // that is used to hold list item
            LayoutInflater.from(parent.context).inflate(
                R.layout.custom_recycleview_adapter,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //(holder as ViewHolder).bind(modelList.get(position));
        val item_position = featureList.get(position)

        holder.tvItem.text = item_position
    }
    // binds the list items to a view
    /*override fun onBindViewHolder(holder: ItemAdapter.ViewHolder, position: Int) {
        val item_position = modelList.get(position)
        // sets the text to the textview from our itemHolder class
        holder.tvItem.text = item_position
    }*/



    // return the number of items in the list
    override fun getItemCount(): Int {
        return featureList.size
    }

    fun deleteItem(position: Int) {
        featureList.removeAt(position)
        notifyDataSetChanged()
    }

    fun editItem(position: Int) {
        featureList.removeAt(position)
        notifyDataSetChanged()
    }

    // Holds the views for adding it to text items
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvItem = itemView.findViewById<TextView>(R.id.SymptomInfo)
        //fun bind(model: Model): Unit {

            /*
            itemView.SymptomInfo.text = model.name
            itemView.sub_txt.text = model.version
            val id = context.resources.getIdentifier(
                model.name.toLowerCase(),
                "drawable",
                context.packageName
            )
            itemView.img.setBackgroundResource(id)
            */
        //}

    }

}