package com.example.nlcs.ui.fragments.library

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.adapter.flashcard.SetCopyAdapter
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.FragmentStudySetsBinding
import com.example.nlcs.ui.activities.create.CreateSetActivity

class StudySetsFragment : Fragment() {
    private var binding: FragmentStudySetsBinding? = null
    private var flashCards: ArrayList<FlashCard>? = null
    private var flashCardDAO: FlashCardDAO? = null
    private var setsAdapter: SetCopyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flashCardDAO = FlashCardDAO(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudySetsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    private fun setupView() {
        binding!!.createSetBtn.setOnClickListener { view1: View? ->
            startActivity(
                Intent(
                    activity, CreateSetActivity::class.java
                )
            )
        }
        flashCards = flashCardDAO.getAllFlashCards()
        updateVisibility()
        setupRecyclerView()
    }

    private fun updateVisibility() {
        if (flashCards!!.isEmpty()) {
            binding!!.setsCl.visibility = View.VISIBLE
            binding!!.setsRv.visibility = View.GONE
        } else {
            binding!!.setsCl.visibility = View.GONE
            binding!!.setsRv.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView() {
        val linearLayoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding!!.setsRv.layoutManager = linearLayoutManager
        setsAdapter = SetCopyAdapter(requireActivity(), flashCards!!)
        binding!!.setsRv.adapter = setsAdapter
        setsAdapter!!.notifyDataSetChanged()
    }

    override suspend fun onResume() {
        super.onResume()
        refreshData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun refreshData() {
        flashCards!!.clear()
        flashCards!!.addAll(flashCardDAO.getAllFlashCards())
        setsAdapter!!.notifyDataSetChanged()
        updateVisibility()
    }
}