package com.example.locationapp.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.locationapp.Location
import com.example.locationapp.LocationViewModel
import com.example.locationapp.R
import com.example.locationapp.Review
import com.example.locationapp.databinding.FragmentReviewAnalysisBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class ReviewAnalysisFragment : Fragment() {

    private val TAG = "ReviewAnalysisFragment"

    private var _binding: FragmentReviewAnalysisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocationViewModel by activityViewModels()

    private var locations = listOf<Location>()
    private var reviews = listOf<Review>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Fragment created")

        viewModel.allLocations.observe(viewLifecycleOwner) {
            locations = it
            Log.d(TAG, "Loaded ${locations.size} locations")
            updateAll()
        }

        viewModel.allReviews.observe(viewLifecycleOwner) {
            reviews = it
            Log.d(TAG, "Loaded ${reviews.size} reviews")
            updateAll()
        }

        binding.backButton.setOnClickListener {
            Log.d(TAG, "Back button pressed")
            findNavController().navigateUp()
        }
    }

    private fun updateAll() {
        Log.d(TAG, "Updating analysis UI")
        updateAverageRating()
        updateMostReviewed()
        updateHighestRated()
        updateLowestRated()
        updateVisitedStats()
        updateCategoryChart()
        updateRecentReviewStats()
    }

    private fun updateAverageRating() {
        if (reviews.isNotEmpty()) {
            val average = reviews.map { it.rating }.average()
            binding.averageRatingValue.text = String.format(Locale.getDefault(), "%.1f", average)
            binding.totalReviewsCount.text = "${reviews.size} total reviews across ${locations.size} locations"
        } else {
            binding.averageRatingValue.text = "0.0"
            binding.totalReviewsCount.text = "No reviews submitted yet"
        }
    }

    private fun updateMostReviewed() {
        val mostReviewed = reviews.groupBy { it.locationId }
            .mapNotNull { (id, list) -> locations.find { it.id == id }?.let { loc -> loc to list.size } }
            .maxByOrNull { it.second }

        if (mostReviewed != null) {
            val (location, count) = mostReviewed
            val reviewWord = if (count == 1) "review" else "reviews"
            binding.mostReviewedValue.text = "${location.name} ($count $reviewWord)"
            loadImageInto(binding.mostReviewedImage, location.imageUri)
        } else {
            binding.mostReviewedValue.text = "No reviews yet"
            binding.mostReviewedImage.setImageResource(R.drawable.placeholder)
        }
    }

    private fun updateHighestRated() {
        val highestRated = averageRatingPerLocation()?.maxByOrNull { it.second }

        if (highestRated != null) {
            val (location, avgRating) = highestRated
            binding.highestRatedName.text = location.name
            binding.highestRatedCount.text = String.format(Locale.getDefault(), "%.1f", avgRating)
            binding.highestRatedBar.rating = avgRating.toFloat()
            loadImageInto(binding.highestRatedImage, location.imageUri)
        } else {
            binding.highestRatedName.text = "No rated locations"
            binding.highestRatedCount.text = "-"
            binding.highestRatedBar.rating = 0f
            binding.highestRatedImage.setImageResource(R.drawable.placeholder)
        }
    }

    private fun updateLowestRated() {
        val lowestRated = averageRatingPerLocation()?.minByOrNull { it.second }

        if (lowestRated != null) {
            val (location, avgRating) = lowestRated
            binding.lowestRatedName.text = location.name
            binding.lowestRatedCount.text = String.format(Locale.getDefault(), "%.1f", avgRating)
            binding.lowestRatedBar.rating = avgRating.toFloat()
            loadImageInto(binding.lowestRatedImage, location.imageUri)
        } else {
            binding.lowestRatedName.text = "No rated locations"
            binding.lowestRatedCount.text = "-"
            binding.lowestRatedBar.rating = 0f
            binding.lowestRatedImage.setImageResource(R.drawable.placeholder)
        }
    }

    private fun averageRatingPerLocation(): List<Pair<Location, Double>>? {
        if (reviews.isEmpty()) return null
        return reviews.groupBy { it.locationId }
            .mapNotNull { (id, list) ->
                locations.find { it.id == id }?.let { loc ->
                    val avg = list.map { it.rating }.average()
                    loc to avg
                }
            }
            .takeIf { it.isNotEmpty() }
    }

    private fun updateVisitedStats() {
        val total = locations.size
        if (total == 0) {
            binding.visitedStatsText.text = "No locations added yet."
            return
        }

        val visited = locations.count { it.visited }
        val unvisited = total - visited
        val visitedPercent = (visited * 100.0 / total).roundToInt()
        val unvisitedPercent = 100 - visitedPercent

        val builder = SpannableStringBuilder()

        builder.append("Visited\n")
        val vStart = builder.length
        builder.append("$visited ($visitedPercent%)\n\n")
        builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD), vStart, builder.length - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        builder.append("Unvisited\n")
        val uStart = builder.length
        builder.append("$unvisited ($unvisitedPercent%)")
        builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD), uStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.visitedStatsText.text = builder
    }

    private fun updateCategoryChart() {
        val categoryCounts = locations.groupingBy { it.category }.eachCount()
        setupPieChart(binding.categoryPieChart, categoryCounts)
    }

    private fun setupPieChart(chart: PieChart, data: Map<String, Int>) {
        if (data.isEmpty()) {
            chart.clear()
            chart.setNoDataText("No location categories available")
            return
        }

        val entries = data.map { PieEntry(it.value.toFloat(), it.key) }

        val modernColors = listOf(
            Color.parseColor("#2563EB"),
            Color.parseColor("#10B981"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#8B5CF6"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#06B6D4"),
            Color.parseColor("#64748B")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = modernColors
            valueTextSize = 13f
            valueTextColor = Color.WHITE
            sliceSpace = 2.5f
        }

        chart.data = PieData(dataSet).apply {
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }

        chart.apply {
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            description.isEnabled = false
            setHoleRadius(50f)
            setTransparentCircleRadius(55f)
            setCenterText("Categories")
            setCenterTextSize(13f)
            setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            legend.apply {
                isEnabled = true
                textSize = 12f
                isWordWrapEnabled = true
                orientation = Legend.LegendOrientation.VERTICAL
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            }
            invalidate()
        }
    }

    private fun updateRecentReviewStats() {
        val now = System.currentTimeMillis()
        val count7 = reviews.count { it.timestamp >= now - TimeUnit.DAYS.toMillis(7) }
        val count30 = reviews.count { it.timestamp >= now - TimeUnit.DAYS.toMillis(30) }

        val builder = SpannableStringBuilder()

        builder.append("Past 7 Days\n")
        val start7 = builder.length
        builder.append("$count7 reviews\n\n")
        builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start7, builder.length - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        builder.append("Past 30 Days\n")
        val start30 = builder.length
        builder.append("$count30 reviews")
        builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start30, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.recentReviewStats.text = builder
    }

    private fun loadImageInto(imageView: ImageView, uri: String?) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(imageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "View destroyed")
    }
}