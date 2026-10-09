package com.example.locationapp.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locationapp.databinding.FragmentVisitedLocationsBinding
import com.example.locationapp.LocationViewModel
import com.example.locationapp.LocationAdapter
import com.example.locationapp.Location
import com.example.locationapp.Review

class VisitedLocationsFragment : Fragment() {

    private val TAG = "VisitedLocationsFragment"

    private var _binding: FragmentVisitedLocationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by activityViewModels()
    private lateinit var adapter: LocationAdapter

    private var visitedList: List<Location> = emptyList()
    private var allReviews: List<Review> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitedLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "VisitedLocationsFragment created")

        binding.arrowBack.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            findNavController().navigateUp()
        }

        adapter = LocationAdapter(
            onItemClick = { location ->
                Log.d(TAG, "Clicked on location: ${location.name}")
                val action = VisitedLocationsFragmentDirections
                    .actionVisitedLocationsFragmentToLocationDetailFragment(location)
                findNavController().navigate(action)
            },
            onVisitedToggle = { updatedLocation ->
                Log.d(TAG, "Visited toggle changed for: ${updatedLocation.name}, now visited=${updatedLocation.visited}")
                viewModel.update(updatedLocation)
                if (!updatedLocation.visited) {
                    visitedList = visitedList.map {
                        if (it.id == updatedLocation.id) updatedLocation else it
                    }
                    filterVisitedList()
                }
            }
        )

        binding.recyclerViewVisited.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewVisited.adapter = adapter

        viewModel.allReviews.observe(viewLifecycleOwner) { reviews ->
            allReviews = reviews
            val reviewMap = reviews.groupBy { it.locationId }
            adapter.updateReviewMap(reviewMap)
            Log.d(TAG, "Loaded ${reviews.size} reviews")
        }

        viewModel.visitedLocations.observe(viewLifecycleOwner) { list ->
            visitedList = list
            Log.d(TAG, "Loaded ${visitedList.size} visited locations")
            filterVisitedList()
        }
    }

    private fun filterVisitedList() {
        val filtered = visitedList.filter { it.visited }
        adapter.updateLocations(filtered)
        Log.d(TAG, "Filtered visited list to ${filtered.size} locations")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "View destroyed")
    }
}
