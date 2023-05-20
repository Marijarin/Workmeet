package ru.netology.workmeet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.workmeet.R
import ru.netology.workmeet.adapter.JobAdapter
import ru.netology.workmeet.adapter.OnInteractionSmallListener
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.FragmentUserJobsBinding
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.viewModel.JobViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserJobsFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: JobViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentUserJobsBinding.inflate(inflater, container, false)

        val userId = requireArguments().getLong("userId")

        userId.let {
            viewModel.loadJobs(it)
        }


        val adapter = JobAdapter(object : OnInteractionSmallListener {
            override fun onEdit(job: Job) {
                viewModel.edit(job)
            }

            override fun onRemove(job: Job) {
                viewModel.removeById(job.id)
            }


        }, appAuth)
        binding.list.adapter = adapter
        lifecycleScope.launchWhenCreated {
            viewModel.setUserId(userId)
            if(userId != appAuth.state.value.id) {
                viewModel.data.collectLatest {
                    adapter.submitList(it)
                }
            } else {
                viewModel.myData.collectLatest {
                    adapter.submitList(it)
                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is FeedModelState.Loading
            if (state is FeedModelState.Error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadJobs(userId)
                    }
                    .show()
            }
        }
        viewModel.edited.observe(viewLifecycleOwner) { job ->
            if (job.id == 0L) {
                return@observe
            }
            findNavController()
                .navigate(
                    R.id.action_userJobsFragment_to_newJobFragment, bundleOf(
                        "name" to job.name,
                        "position" to job.position,
                        "start" to job.start,
                        "finish" to job.finish,
                        "link" to job.link
                    )
                )

        }
        binding.toPosts.setOnClickListener {
            findNavController().navigateUp()
        }

        if (userId!=appAuth.state.value.id){
            binding.fabJ.visibility = View.GONE
        }

        binding.fabJ.setOnClickListener {
            findNavController().navigate(
                R.id.action_userJobsFragment_to_newJobFragment,
                bundleOf("userId" to appAuth.state.value.id)
            )

        }
        return binding.root
    }
}
