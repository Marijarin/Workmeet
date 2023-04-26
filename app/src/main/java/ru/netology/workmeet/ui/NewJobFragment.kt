package ru.netology.workmeet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.netology.workmeet.databinding.FragmentNewJobBinding
import ru.netology.workmeet.ui.NewEventFragment.Companion.textArg
import ru.netology.workmeet.util.StringArg
import ru.netology.workmeet.viewModel.JobViewModel

class NewJobFragment: Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }
    private val viewModel: JobViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewJobBinding.inflate(inflater, container, false )

        /*arguments?.textArg
            ?.let(binding.start::setText)*/


        return binding.root
    }
}