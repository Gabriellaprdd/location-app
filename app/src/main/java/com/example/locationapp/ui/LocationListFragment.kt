package com.example.locationapp.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locationapp.*
import com.example.locationapp.databinding.FragmentLocationListBinding

class LocationListFragment : Fragment() {

    private val TAG = "LocationListFragment"

    private var _binding: FragmentLocationListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocationViewModel by activityViewModels()

    private lateinit var adapter: LocationAdapter

    private var locationList: List<Location> = emptyList()
    private var allReviews: List<Review> = emptyList()
    private var selectedCategory: String = "All"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLocationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "Fragment created")

        adapter = LocationAdapter(
            onItemClick = { location ->
                Log.d(TAG, "Navigating to details of: ${location.name}")
                val action = LocationListFragmentDirections
                    .actionLocationListFragmentToLocationDetailFragment(location)
                findNavController().navigate(action)
            },
            onVisitedToggle = { updatedLocation ->
                Log.d(TAG, "Toggling visited: ${updatedLocation.name} -> ${updatedLocation.visited}")
                viewModel.update(updatedLocation)
                locationList = locationList.map {
                    if (it.id == updatedLocation.id) updatedLocation else it
                }
                filterList()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.allReviews.observe(viewLifecycleOwner) { reviews ->
            allReviews = reviews
            val reviewMap = reviews.groupBy { it.locationId }
            adapter.updateReviewMap(reviewMap)
            Log.d(TAG, "Loaded ${reviews.size} reviews")
        }

        viewModel.allLocations.observe(viewLifecycleOwner) { list ->
            locationList = list
            Log.d(TAG, "Loaded ${list.size} locations")
            filterList()
            setupFilterMenu(list)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "Search text changed: $newText")
                filterList()
                return true
            }
        })

        binding.addLocationButton.setOnClickListener {
            Log.d(TAG, "Navigating to Add Location")
            try {
                val action = LocationListFragmentDirections
                    .actionLocationListFragmentToAddEditLocationFragment(null)
                findNavController().navigate(action)
            } catch (_: Exception) {
                findNavController().navigate(R.id.action_locationListFragment_to_addEditLocationFragment)
            }
        }

        binding.viewVisitedButton.setOnClickListener {
            Log.d(TAG, "Navigating to Visited Locations")
            findNavController().navigate(R.id.action_locationListFragment_to_visitedLocationsFragment)
        }

        binding.analysisButton.setOnClickListener {
            Log.d(TAG, "Navigating to Review Analysis")
            findNavController().navigate(R.id.action_locationListFragment_to_reviewAnalysis)
        }
    }

    private fun setupFilterMenu(locations: List<Location>) {
        val categories = locations.map { it.category }.distinct().sorted()
        val options = listOf("All") + categories

        Log.d(TAG, "Filter categories available: $options")

        binding.filterButton.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            options.forEachIndexed { index, category ->
                popup.menu.add(0, index, index, category)
            }
            popup.setOnMenuItemClickListener { item ->
                selectedCategory = options[item.itemId]
                Log.d(TAG, "Selected category filter: $selectedCategory")
                filterList()
                true
            }
            popup.show()
        }
    }

    private fun filterList() {
        val query = binding.searchView.query?.toString()?.trim() ?: ""
        val unvisited = locationList.filter { !it.visited }

        val filteredByCategory = if (selectedCategory == "All") {
            unvisited
        } else {
            unvisited.filter { it.category == selectedCategory }
        }

        val filtered = if (query.isEmpty()) {
            filteredByCategory
        } else {
            filteredByCategory.filter {
                it.name.startsWith(query, ignoreCase = true)
            }
        }

        adapter.submitList(filtered)
        binding.noSearchResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE

        Log.d(TAG, "Filtered list size: ${filtered.size} (query='$query', category='$selectedCategory')")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "View destroyed")
    }
}