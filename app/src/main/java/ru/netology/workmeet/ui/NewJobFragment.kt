package ru.netology.workmeet.ui

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.workmeet.R
import ru.netology.workmeet.databinding.FragmentNewJobBinding
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.ui.NewEventFragment.Companion.textArg
import ru.netology.workmeet.util.AndroidUtils
import ru.netology.workmeet.util.StringArg
import ru.netology.workmeet.viewModel.JobViewModel

@AndroidEntryPoint
class NewJobFragment : Fragment() {
    private val viewModel: JobViewModel by viewModels()
    val userId = arguments?.getLong("userId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewJobBinding.inflate(inflater, container, false)

       if (arguments?.containsKey("name") == true
           && arguments?.containsKey("position") == true
           && arguments?.containsKey("start") == true
          ){
           binding.apply {
               name.setText(arguments?.getString("name"))
               position.setText(arguments?.getString("position"))
               start.setText(arguments?.getString("start"))
               finish.setText(arguments?.getString("finish") ?: context?.getText(R.string.now))
               link.setText(arguments?.getString("link") ?: " ")
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
                        viewModel.changeContent(
                            binding.name.toString(),
                            binding.position.toString(),
                            binding.start.toString(),
                            binding.finish.toString(),
                            binding.link.toString()
                            )
                        if (userId != null) {
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
}