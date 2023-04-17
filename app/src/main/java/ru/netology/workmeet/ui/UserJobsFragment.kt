package ru.netology.workmeet.ui

import android.app.AlertDialog
import android.app.ProgressDialog.show
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.workmeet.R
import ru.netology.workmeet.adapter.JobAdapter
import ru.netology.workmeet.adapter.LargeItemAdapter
import ru.netology.workmeet.adapter.OnInteractionListener
import ru.netology.workmeet.adapter.OnInteractionSmallListener
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.FragmentUserJobsBinding
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.viewModel.AuthViewModel
import ru.netology.workmeet.viewModel.JobViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserJobsFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: JobViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentUserJobsBinding.inflate(inflater, container, false)

        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.want_to_sign_in) { _, _ ->
                    findNavController().navigate(R.id.action_userJobsFragment_to_signInFragment)
                }
                setNegativeButton(R.string.cancel) { _, _ ->
                    findNavController().navigate(R.id.nav_host_fragment)
                }
            }
                .setIcon(R.drawable.ic_launcher_foreground)
                .setMessage(R.string.warn_out)
            builder.create()
        }

        val adapter = JobAdapter(object : OnInteractionSmallListener {
            override fun onEdit(job: Job) {
                super.onEdit(job)
            }

            override fun onRemove(job: Job) {
                //not implemented
            }


        }, appAuth)

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                it.asReversed()
            }
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            if (authViewModel.authenticated) {
                //todo
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is FeedModelState.Loading
            if (state is FeedModelState.Error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { //todo
                         }
                    .show()
            }
        }


        binding.fab.setOnClickListener {
            if (!authViewModel.authenticated) {
                alertDialog?.show()
            }
            setFragmentResultListener("signInClosed") { _, _ ->
                if (authViewModel.authenticated) findNavController().navigate(R.id.action_userJobsFragment_to_newJobFragment)
            }
        }
        return binding.root
    }
}
