package com.example.locationapp.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.locationapp.LocationViewModel
import com.example.locationapp.Review
import com.example.locationapp.databinding.FragmentAddReviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddReviewFragment(
    private val locationId: Int
) : DialogFragment() {

    private val TAG = "AddReviewFragment"

    private var _binding: FragmentAddReviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentAddReviewBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setupListeners()
        Log.d(TAG, "Dialog created for locationId = $locationId")
        return dialog
    }

    private fun setupListeners() {
        binding.submitReviewButton.setOnClickListener {
            val name = binding.reviewerName.text.toString().trim()
            val comment = binding.reviewComment.text.toString().trim()
            val rating = binding.ratingBar.rating.toInt()

            Log.d(TAG, "Submit clicked: name='$name', comment='$comment', rating=$rating")

            if (rating == 0) {
                Toast.makeText(requireContext(), "Please select a star rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!validateName(name)) {
                Toast.makeText(requireContext(), "Please enter a valid name (letters only)", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Invalid name: $name")
                return@setOnClickListener
            }

            if (comment.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a review comment", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Empty comment")
                return@setOnClickListener
            }

            val review = Review(
                locationId = locationId,
                userName = name,
                comment = comment,
                rating = rating
            )

            Log.d(TAG, "Submitting review: $review")

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.insertReview(review)
                Log.d(TAG, "Review inserted in DB (IO thread)")
                launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Review submitted!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Review submitted and dialog dismissed")
                    dismiss()
                }
            }
        }

        binding.cancelReviewButton.setOnClickListener {
            Log.d(TAG, "Cancel clicked")
            dismiss()
        }
    }

    private fun validateName(name: String): Boolean {
        val nameRegex = Regex("^[A-Za-z ]+$")
        val isValid = name.isNotEmpty() && nameRegex.matches(name)
        Log.d(TAG, "Validating name: '$name' -> $isValid")
        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "View destroyed")
    }
}