package ru.netology.workmeet.ui

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.workmeet.R
import ru.netology.workmeet.databinding.FragmentNewJobBinding
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.util.AndroidUtils
import ru.netology.workmeet.util.AndroidUtils.dateForServer
import ru.netology.workmeet.util.AndroidUtils.toDateFromLong
import ru.netology.workmeet.viewModel.JobViewModel

@AndroidEntryPoint
class NewJobFragment : Fragment() {
    private val viewModel: JobViewModel by activityViewModels()
    private var fragmentBinding: FragmentNewJobBinding? = null
    companion object {
        var startDate: Long = 0
        var endDate: Long? = null
        lateinit var startDateString: String
        lateinit var endDateString: String
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(inflater, container, false)

        fragmentBinding = binding

        val userId = arguments?.getLong("userId")


        if (arguments?.containsKey("name") == true
            && arguments?.containsKey("position") == true
            && arguments?.containsKey("start") == true
        ) {
            binding.apply {
                name.setText(arguments?.getString("name"))
                position.setText(arguments?.getString("position"))
                link.setText(arguments?.getString("link") ?: " ")
            }

        }
        binding.selectRange.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText(R.string.select_dates)
                .build()
            dateRangePicker.show(requireActivity().supportFragmentManager, "job dates")
            dateRangePicker.addOnPositiveButtonClickListener { datePicked ->
                startDate = datePicked.first
                endDate = datePicked.second
                if(startDate == endDate) endDate = null
                startDateString = toDateFromLong(startDate)
                endDateString =
                    when (endDate) {
                        null -> "now"
                        else -> toDateFromLong(endDate!!)
                    }
                binding.start.text = startDateString
                binding.finish.text = endDateString
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state is FeedModelState.Error) {
                Snackbar
                    .make(binding.root, R.string.error_loading, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry_loading) {
                        if (userId != null) {
                            viewModel.save(userId)
                        }
                    }.show()
            }
        }
        viewModel.jobCreated.observe(viewLifecycleOwner) {
            if (userId != null) {
                viewModel.loadJobs(userId)
            }
            findNavController().navigateUp()
        }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        if (userId != null) {
                            viewModel.changeContent(
                                name = binding.name.text.toString(),
                                position = binding.position.text.toString(),
                                start = dateForServer(startDate),
                                finish = endDate?.let { dateForServer(it)},
                                link = binding.link.text.toString().ifBlank { null }
                            )
                            viewModel.save(userId)
                        }
                        AndroidUtils.hideKeyboard(requireView())
                        true
                    }

                    else -> false
                }
            }

        }, viewLifecycleOwner)

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }


}