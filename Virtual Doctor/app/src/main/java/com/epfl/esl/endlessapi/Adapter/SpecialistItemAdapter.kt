package com.epfl.esl.endlessapi.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.bold

import androidx.recyclerview.widget.RecyclerView
import com.epfl.esl.endlessapi.Fragment.RecommendedSpecialistFragment
import com.epfl.esl.endlessapi.R


// This class contains some important functions to work with the RecyclerView
class SpecialistItemAdapter(
    val context: Context, val itemsName: MutableList<String>, val itemsAddress: MutableList<String>, val itemsURL: MutableList<String>,
    val itemsImage: MutableList<Int>, private var listener: RecommendedSpecialistFragment
) :
    RecyclerView.Adapter<SpecialistItemAdapter.MyViewHolder>(){


    // create new views
    // Inflates teh item views which is designed in xml layout file
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.specialist_recycleview_adapter, parent, false)
        )
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val itemsNamePosition = itemsName[position]
        val itemsAddressPosition = itemsAddress[position]
        val itemsURLPosition = itemsURL[position]
        val itemsImagePosition = itemsImage[position]
        holder.tvItem.text = itemsNamePosition
        holder.tvItem_2.text = itemsAddressPosition
        holder.tvItem_3.text = itemsURLPosition
        holder.tvItem_4.setImageResource(itemsImagePosition)
    }

    // return the number of items in the list
    override fun getItemCount(): Int {
        return itemsName.size
    }

    // Holds the views for adding it to text items
    // define the click listener
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var tvItem = itemView.findViewById<TextView>(R.id.specialistNameText)!!
        var tvItem_2 = itemView.findViewById<TextView>(R.id.addressText)!!
        var tvItem_3 = itemView.findViewById<TextView>(R.id.URLText)!!
        var tvItem_4 = itemView.findViewById<ImageView>(R.id.specialistImage)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(adapterPosition)
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


}