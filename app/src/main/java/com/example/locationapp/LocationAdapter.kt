package com.example.locationapp

import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide

class LocationAdapter(
    private val onItemClick: (Location) -> Unit,
    private val onVisitedToggle: (Location) -> Unit,
    private var reviewMap: Map<Int, List<Review>> = emptyMap()
) : ListAdapter<Location, LocationAdapter.LocationViewHolder>(DIFF) {

    private val TAG = "LocationAdapter"

    private var displayedList: MutableList<Location> = mutableListOf()

    init {
        submitList(emptyList())
        Log.d(TAG, "Adapter initialized with empty list")
    }

    fun updateReviewMap(newMap: Map<Int, List<Review>>) {
        reviewMap = newMap
        notifyDataSetChanged()
        Log.d(TAG, "Review map updated. Total locations with reviews: ${reviewMap.size}")
    }

    fun updateLocations(locations: List<Location>) {
        displayedList = locations.toMutableList()
        submitList(displayedList.toList())
        Log.d(TAG, "Locations updated. New size: ${displayedList.size}")
    }

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.locationName)
        private val category: TextView = itemView.findViewById(R.id.locationCategory)
        private val thumb: ImageView = itemView.findViewById(R.id.locationImage)
        private val ratingText: TextView = itemView.findViewById(R.id.locationRating)
        private val reviewCountText: TextView = itemView.findViewById(R.id.reviewCount)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val visitedSwitch: SwitchCompat = itemView.findViewById(R.id.visitedSwitch)

        fun bind(loc: Location) {
            name.text = loc.name
            category.text = loc.category

            Glide.with(itemView)
                .load(loc.imageUri)
                .placeholder(R.drawable.placeholder)
                .into(thumb)

            val reviews = reviewMap[loc.id].orEmpty()
            val avgRating = if (reviews.isNotEmpty()) {
                reviews.map { it.rating }.average().toFloat()
            } else 0f

            ratingBar.rating = avgRating
            ratingText.text = String.format("%.1f", avgRating)
            reviewCountText.text = "(${reviews.size} Reviews)"

            visitedSwitch.setOnCheckedChangeListener(null)
            visitedSwitch.isChecked = loc.visited

            visitedSwitch.setOnCheckedChangeListener { _, isChecked ->
                val updated = loc.copy(visited = isChecked)
                Log.d(TAG, "Visited toggled for '${loc.name}': $isChecked")
                onVisitedToggle(updated)

                if (!isChecked) {
                    val index = displayedList.indexOfFirst { it.id == loc.id }
                    if (index != -1) {
                        displayedList.removeAt(index)
                        submitList(displayedList.toList())
                        Log.d(TAG, "Removed '${loc.name}' from list due to unvisited status")
                    }
                }
            }

            itemView.setOnClickListener {
                Log.d(TAG, "Clicked on '${loc.name}'")
                onItemClick(loc)
            }

            Log.d(TAG, "Bound location '${loc.name}' with ${reviews.size} reviews, avgRating=$avgRating")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = getItem(position)
        Log.d(TAG, "onBindViewHolder for position $position: ${location.name}")
        holder.bind(location)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Location>() {
            override fun areItemsTheSame(a: Location, b: Location) = a.id == b.id
            override fun areContentsTheSame(a: Location, b: Location) = a == b
        }
    }
}
