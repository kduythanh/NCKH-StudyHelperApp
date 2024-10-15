package com.example.nlcs.ui.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.transition.Slide
import com.example.nlcs.databinding.FragmentCreateBinding
import com.example.nlcs.preferen.UserSharePreferences
import com.example.nlcs.ui.activities.create.CreateFolderActivity
import com.example.nlcs.ui.activities.create.CreateSetActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CreateFragment : BottomSheetDialogFragment() {
    private var binding: FragmentCreateBinding? = null
    var userSharePreferences: UserSharePreferences? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the entered transition animation
        enterTransition = CustomEnterTransition().setDuration(500)
        // Set the exit transition animation
        exitTransition = CustomExitTransition().setDuration(500)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userSharePreferences = UserSharePreferences(requireActivity())


        binding!!.llCreateFolder.setOnClickListener { v: View? ->
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(),
                binding!!.llCreateFolder,
                "transition"
            )
            startActivity(
                Intent(requireContext(), CreateFolderActivity::class.java),
                options.toBundle()
            )
            dismiss()
        }

        binding!!.llCreateSet.setOnClickListener { v: View? ->
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(),
                binding!!.llCreateSet,
                "transition"
            )
            startActivity(
                Intent(requireContext(), CreateSetActivity::class.java),
                options.toBundle()
            )
            dismiss()
        }
    }

    // Define your custom enter transition class
    private class CustomEnterTransition : Slide() {
        init {
            slideEdge = Gravity.BOTTOM
        }
    }

    // Define your custom exit transition class
    private class CustomExitTransition : Slide() {
        init {
            slideEdge = Gravity.BOTTOM
        }
    }
}
