package com.example.locationapp.ui

import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.locationapp.*
import com.example.locationapp.databinding.FragmentAddEditLocationBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddEditLocationFragment : Fragment() {

    private val TAG = "AddEditLocationFrag"

    private var _binding: FragmentAddEditLocationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by activityViewModels()

    private var existingLocation: Location? = null
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.placeholder)
                .into(binding.imagePreview)
            Log.d(TAG, "Image selected: $it")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        existingLocation = getPassedLocation()

        setupTopBar()
        setupCategories()
        setupTimePickers()
        setupImagePicker()

        if (existingLocation != null) {
            binding.pageTitle.text = "Edit Location"
            populateFields(existingLocation!!)
        } else {
            binding.pageTitle.text = getString(R.string.add_location_button)
            resetOpeningHoursToDefault()
        }

        binding.saveButton.setOnClickListener { saveLocation() }
        binding.cancelButton.setOnClickListener { findNavController().navigateUp() }
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
    }

    private fun getPassedLocation(): Location? {
        val bundle = arguments ?: return null
        return try {
            if (bundle.containsKey("location")) {
                @Suppress("DEPRECATION")
                bundle.getParcelable<Location>("location")
                    ?: (bundle.getSerializable("location") as? Location)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse location argument: ${e.message}")
            null
        }
    }

    private fun setupTopBar() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCategories() {
        val categories = listOf("Restaurant", "Cafe", "Park", "Museum", "Shopping", "Historical", "Campus")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.categoryText.setAdapter(adapter)
    }

    private fun setupImagePicker() {
        binding.selectImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun setupTimePickers() {
        val buttons = listOf(
            binding.mondayOpenButton, binding.mondayCloseButton,
            binding.tuesdayOpenButton, binding.tuesdayCloseButton,
            binding.wednesdayOpenButton, binding.wednesdayCloseButton,
            binding.thursdayOpenButton, binding.thursdayCloseButton,
            binding.fridayOpenButton, binding.fridayCloseButton,
            binding.saturdayOpenButton, binding.saturdayCloseButton,
            binding.sundayOpenButton, binding.sundayCloseButton
        )

        for (btn in buttons) {
            btn.setOnClickListener { showTimePicker(btn) }
        }
    }

    private fun showTimePicker(button: Button) {
        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        val currentText = button.text.toString().trim()
        try {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            val parsed = LocalTime.parse(currentText, formatter)
            hour = parsed.hour
            minute = parsed.minute
        } catch (_: Exception) {
        }

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val time = LocalTime.of(selectedHour, selectedMinute)
            val formatted = time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
            button.text = formatted
        }, hour, minute, false).show()
    }

    private fun populateFields(loc: Location) {
        binding.nameInput.setText(loc.name)
        binding.addressInput.setText(loc.address)
        binding.categoryText.setText(loc.category, false)
        binding.contactInput.setText(loc.contact)
        binding.descriptionInput.setText(loc.description)

        if (!loc.imageUri.isNullOrEmpty()) {
            selectedImageUri = Uri.parse(loc.imageUri)
            Glide.with(this)
                .load(selectedImageUri)
                .placeholder(R.drawable.placeholder)
                .into(binding.imagePreview)
        }

        parseAndSetOpeningHours(loc.openingHours)
    }

    private fun parseAndSetOpeningHours(hours: String) {
        val lines = hours.lines()
        val dayMap = mapOf(
            "Monday" to Pair(binding.mondayOpenButton, binding.mondayCloseButton),
            "Tuesday" to Pair(binding.tuesdayOpenButton, binding.tuesdayCloseButton),
            "Wednesday" to Pair(binding.wednesdayOpenButton, binding.wednesdayCloseButton),
            "Thursday" to Pair(binding.thursdayOpenButton, binding.thursdayCloseButton),
            "Friday" to Pair(binding.fridayOpenButton, binding.fridayCloseButton),
            "Saturday" to Pair(binding.saturdayOpenButton, binding.saturdayCloseButton),
            "Sunday" to Pair(binding.sundayOpenButton, binding.sundayCloseButton)
        )

        for (line in lines) {
            if (!line.contains(":")) continue
            val parts = line.split(":", limit = 2).map { it.trim() }
            val dayName = parts[0]
            val timeRange = parts[1]

            val pair = dayMap.entries.find { it.key.equals(dayName, ignoreCase = true) }?.value
            if (pair != null) {
                if (timeRange.contains("-")) {
                    val (open, close) = timeRange.split("-").map { it.trim() }
                    pair.first.text = open
                    pair.second.text = close
                } else if (timeRange.contains("Closed", ignoreCase = true)) {
                    pair.first.text = "Closed"
                    pair.second.text = "Closed"
                }
            }
        }
    }

    private fun resetOpeningHoursToDefault() {
        val dayPairs = listOf(
            Pair(binding.mondayOpenButton, binding.mondayCloseButton),
            Pair(binding.tuesdayOpenButton, binding.tuesdayCloseButton),
            Pair(binding.wednesdayOpenButton, binding.wednesdayCloseButton),
            Pair(binding.thursdayOpenButton, binding.thursdayCloseButton),
            Pair(binding.fridayOpenButton, binding.fridayCloseButton),
            Pair(binding.saturdayOpenButton, binding.saturdayCloseButton),
            Pair(binding.sundayOpenButton, binding.sundayCloseButton)
        )

        for (pair in dayPairs) {
            pair.first.text = getString(R.string.open)
            pair.second.text = getString(R.string.close)
        }
    }

    private fun buildOpeningHoursString(): String {
        val days = listOf(
            "Monday" to Pair(binding.mondayOpenButton.text.toString(), binding.mondayCloseButton.text.toString()),
            "Tuesday" to Pair(binding.tuesdayOpenButton.text.toString(), binding.tuesdayCloseButton.text.toString()),
            "Wednesday" to Pair(binding.wednesdayOpenButton.text.toString(), binding.wednesdayCloseButton.text.toString()),
            "Thursday" to Pair(binding.thursdayOpenButton.text.toString(), binding.thursdayCloseButton.text.toString()),
            "Friday" to Pair(binding.fridayOpenButton.text.toString(), binding.fridayCloseButton.text.toString()),
            "Saturday" to Pair(binding.saturdayOpenButton.text.toString(), binding.saturdayCloseButton.text.toString()),
            "Sunday" to Pair(binding.sundayOpenButton.text.toString(), binding.sundayCloseButton.text.toString())
        )

        return days.joinToString("\n") { (day, times) ->
            val (open, close) = times
            if (open.equals("Closed", ignoreCase = true) || close.equals("Closed", ignoreCase = true)) {
                "$day: Closed"
            } else if (open.equals("Open", ignoreCase = true) || close.equals("Close", ignoreCase = true)) {
                "$day: Closed"
            } else {
                "$day: $open - $close"
            }
        }
    }

    private fun saveLocation() {
        val name = binding.nameInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()
        val category = binding.categoryText.text.toString().trim()
        val contact = binding.contactInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameInput.error = "Name is required"
            binding.nameInput.requestFocus()
            return
        }

        if (category.isEmpty()) {
            binding.categoryText.error = "Category is required"
            return
        }

        val openingHours = buildOpeningHoursString()
        val imageUriString = selectedImageUri?.toString() ?: existingLocation?.imageUri

        if (existingLocation != null) {
            val updated = existingLocation!!.copy(
                name = name,
                category = category,
                address = address,
                contact = contact,
                description = description,
                openingHours = openingHours,
                imageUri = imageUriString
            )
            viewModel.update(updated)
            Toast.makeText(requireContext(), "Location updated successfully!", Toast.LENGTH_SHORT).show()
        } else {
            val newLocation = Location(
                name = name,
                category = category,
                address = address,
                contact = contact,
                description = description,
                openingHours = openingHours,
                imageUri = imageUriString,
                visited = false
            )
            viewModel.insert(newLocation)
            Toast.makeText(requireContext(), "Location added successfully!", Toast.LENGTH_SHORT).show()
        }

        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}