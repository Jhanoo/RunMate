package com.D107.runmate.presentation.group.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.D107.runmate.domain.model.group.Place
import com.D107.runmate.presentation.R

class PlaceAdapter : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    private val places = mutableListOf<Place>()
    var onItemClickListener: ((Place) -> Unit)? = null

    fun updatePlaces(newPlaces: List<Place>) {
        places.clear()
        places.addAll(newPlaces)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount(): Int = places.size

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPlaceName: TextView = itemView.findViewById(R.id.tv_place_name)
        private val tvPlaceAddress: TextView = itemView.findViewById(R.id.tv_place_address)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(places[position])
                }
            }
        }

        fun bind(place: Place) {
            tvPlaceName.text = place.name
            tvPlaceAddress.text = place.address
        }
    }
}