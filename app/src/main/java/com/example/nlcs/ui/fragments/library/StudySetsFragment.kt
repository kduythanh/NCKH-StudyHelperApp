package com.example.nlcs.ui.fragments.library

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.R
import com.example.nlcs.adapter.flashcard.SetCopyAdapter
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.FragmentStudySetsBinding
import com.example.nlcs.ui.activities.create.CreateSetActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudySetsFragment : Fragment() {
    // Use a backing property for non-nullable binding
    private var _binding: FragmentStudySetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth


    private var flashCards: ArrayList<FlashCard> = ArrayList()
    private lateinit var flashCardDAO: FlashCardDAO
    private var setsAdapter: SetCopyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flashCardDAO = FlashCardDAO(requireActivity())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudySetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            setupView()
        }


    }

    private suspend fun setupView() {
        binding.createSetBtn.setOnClickListener {
            startActivity(
                Intent(
                    requireActivity(), CreateSetActivity::class.java
                )
            )
        }
        firebaseAuth = Firebase.auth
        val userId = firebaseAuth.currentUser?.uid ?: ""
        flashCards = flashCardDAO.getAllFlashCardByUserId(userId)
        updateVisibility()
        setupRecyclerView()
    }

    private fun updateVisibility() {
        if (flashCards.isEmpty()) {
            binding.setsCl.visibility = View.VISIBLE
            binding.setsRv.visibility = View.GONE
        } else {
            binding.setsCl.visibility = View.GONE
            binding.setsRv.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView() {
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.setsRv.layoutManager = linearLayoutManager
        setsAdapter = SetCopyAdapter(requireContext(), flashCards)
        binding.setsRv.adapter = setsAdapter
        setsAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        // Use lifecycleScope to launch coroutines tied to the fragment's lifecycle
        refreshData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshData() {
        CoroutineScope(Dispatchers.Main).launch {
            // Initialize firebaseAuth if needed
            firebaseAuth = Firebase.auth
            val userId = firebaseAuth.currentUser?.uid ?: ""

            // Ensure flashCards is initialized before using it
            flashCards = flashCards ?: ArrayList() // Initialize if not already initialized

            flashCards.clear() // Clear current data
            flashCardDAO.getAllFlashCardByUserId(userId)?.let {
                flashCards.addAll(it) // Add new data
            }

            setsAdapter?.notifyDataSetChanged()
            updateVisibility() // Make sure this method doesn't depend on uninitialized properties
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks by nullifying the binding
        _binding = null
    }
}
