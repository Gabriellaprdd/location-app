package com.example.locationapp.ui

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.locationapp.*
import com.example.locationapp.databinding.FragmentLocationDetailBinding
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class LocationDetailFragment : Fragment() {

    private val TAG = "LocationDetailFragment"

    private var _binding: FragmentLocationDetailBinding? = null
    private val binding get() = _binding!!

    private val args: LocationDetailFragmentArgs by navArgs()
    private val viewModel: LocationViewModel by activityViewModels()

    private var locationId: Int = -1
    private var currentLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "Fragment created")

        locationId = args.location?.id ?: return
        Log.d(TAG, "Location ID: $locationId")

        viewModel.getLocationById(locationId).observe(viewLifecycleOwner) { location ->
            if (location != null) {
                Log.d(TAG, "Location loaded: ${location.name}")
                currentLocation = location
                updateUI(location)
            } else {
                Log.w(TAG, "Location not found for ID $locationId")
            }
        }

        binding.backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            findNavController().navigateUp()
        }

        binding.writeReviewButton.setOnClickListener {
            Log.d(TAG, "Write Review button clicked")
            AddReviewFragment(locationId = locationId)
                .show(parentFragmentManager, "AddReview")
        }

        binding.editButton.setOnClickListener {
            val currentLocation = viewModel.getLocationByIdSync(locationId)
            Log.d(TAG, "Edit button clicked for location: ${currentLocation?.name}")
            val action = LocationDetailFragmentDirections
                .actionLocationDetailFragmentToAddEditLocationFragment(currentLocation)
            findNavController().navigate(action)
        }

        binding.deleteButton.setOnClickListener {
            Log.d(TAG, "Delete button clicked")
            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this location? This action cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    currentLocation?.let {
                        Log.d(TAG, "Deleting location: ${it.name}")
                        viewModel.delete(it)
                        Toast.makeText(requireContext(), "Location deleted.", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
                .setNegativeButton("No") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    Log.d(TAG, "Deletion cancelled")
                }
                .create()

            dialog.show()
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.green)
            )
            dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red)
            )
        }

        viewModel.getReviewsForLocation(locationId).observe(viewLifecycleOwner) { reviews ->
            Log.d(TAG, "Reviews loaded: ${reviews.size} reviews")

            val avg = reviews.map { it.rating }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
            Log.d(TAG, "Average rating: $avg")

            binding.averageRatingBar.rating = avg.toFloat()
            binding.averageRatingLabel.text = String.format("%.1f", avg)
            binding.userReviewsLabel.text = "User Reviews (${reviews.size})"

            val container = binding.reviewsContainer
            container.removeAllViews()

            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a", Locale.getDefault())
            val density = resources.displayMetrics.density

            for ((index, review) in reviews.withIndex()) {
                val dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(review.timestamp), ZoneId.systemDefault()
                )

                val user = review.userName.uppercase()
                val comment = review.comment
                val timestamp = formatter.format(dateTime)

                val reviewItem = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, (8 * density).toInt(), 0, (12 * density).toInt())
                }

                val commentBuilder = SpannableStringBuilder()
                val userStart = 0
                commentBuilder.append("$user: ")
                val userEnd = commentBuilder.length

                commentBuilder.setSpan(
                    android.text.style.StyleSpan(Typeface.BOLD),
                    userStart, userEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                commentBuilder.append(comment)

                val messageView = TextView(requireContext()).apply {
                    text = commentBuilder
                    textSize = 15f
                    typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
                reviewItem.addView(messageView)

                val footerRow = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = (8 * density).toInt()
                    }
                }

                val dateView = TextView(requireContext()).apply {
                    text = timestamp
                    textSize = 12f
                    typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
                footerRow.addView(dateView)

                val starsHtml = "<font color='#F59E0B'>" + "★".repeat(review.rating.coerceIn(0, 5)) + "</font>"
                val starsView = TextView(requireContext()).apply {
                    text = HtmlCompat.fromHtml(starsHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    textSize = 14f
                    gravity = Gravity.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                footerRow.addView(starsView)

                reviewItem.addView(footerRow)

                if (index < reviews.size - 1) {
                    val divider = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (1 * density).toInt().coerceAtLeast(1)
                        ).apply {
                            topMargin = (8 * density).toInt()
                        }
                        setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_color))
                    }
                    reviewItem.addView(divider)
                }

                container.addView(reviewItem)
                Log.d(TAG, "Review by $user: $comment ($timestamp)")
            }
        }
    }

    private fun updateUI(location: Location) {
        Log.d(TAG, "Updating UI for location: ${location.name}")
        binding.nameText.text = location.name
        binding.categoryText.text = location.category
        binding.addressText.text = location.address
        binding.contactText.text = location.contact
        binding.descriptionText.text = location.description

        updateStatusText(location.openingHours)
        populateOpeningHoursTable(location.openingHours)

        Glide.with(this)
            .load(location.imageUri)
            .placeholder(R.drawable.placeholder)
            .into(binding.detailImage)
    }

    private fun updateStatusText(hours: String) {
        val today = LocalDate.now().dayOfWeek.name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        val todayLine = hours.lines().find { it.startsWith(today, ignoreCase = true) }
        val statusView = binding.statusLabel

        if (todayLine == null || todayLine.uppercase().contains("CLOSED")) {
            statusView.text = "• Closed Today"
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            Log.d(TAG, "Status for $today: Closed Today")
            return
        }

        try {
            val timeRange = todayLine.substringAfter(":").trim()
            val (openStr, closeStr) = timeRange.split("-").map { it.trim() }

            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            val openTime = LocalTime.parse(openStr, formatter)
            val closeTime = LocalTime.parse(closeStr, formatter)
            val now = LocalTime.now()

            val isOpen = now.isAfter(openTime) && now.isBefore(closeTime)

            statusView.text = if (isOpen) "• Open Now" else "• Closed"
            statusView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isOpen) R.color.green else R.color.red
                )
            )

            Log.d(TAG, "Status for $today: ${statusView.text} ($openStr - $closeStr)")
        } catch (e: Exception) {
            statusView.text = "• Invalid time format"
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            Log.e(TAG, "Failed to parse opening hours for $today", e)
        }
    }

    private fun populateOpeningHoursTable(hours: String) {
        val tableLayout = binding.openingHoursTable
        tableLayout.removeAllViews()

        val rows = hours.lines()
        for (line in rows) {
            if (!line.contains(":")) continue
            val (day, time) = line.split(":", limit = 2).map { it.trim() }

            val row = TableRow(requireContext())

            val dayView = TextView(requireContext()).apply {
                text = day.uppercase()
                textSize = 14f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                setPadding(0, 12, 16, 12)
            }

            val isClosed = time.contains("closed", ignoreCase = true)
            val timeView = TextView(requireContext()).apply {
                text = time
                textSize = 14f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isClosed) R.color.red else R.color.black
                    )
                )
                setPadding(16, 12, 0, 12)
            }

            row.addView(dayView)
            row.addView(timeView)
            tableLayout.addView(row)

            Log.d(TAG, "Opening hours row added: $day - $time")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "View destroyed")
    }
}