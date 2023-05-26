package ru.netology.workmeet.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
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
import ru.netology.workmeet.adapter.LargeItemAdapter
import ru.netology.workmeet.adapter.ItemLoadingStateAdapter
import ru.netology.workmeet.adapter.OnInteractionListener
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.FragmentEventFeedBinding
import ru.netology.workmeet.dto.Event
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Post
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.ui.NewPostFragment.Companion.textArg
import ru.netology.workmeet.viewModel.AuthViewModel
import ru.netology.workmeet.viewModel.EventViewModel
import javax.inject.Inject


    @AndroidEntryPoint
    class EventFeedFragment : Fragment() {
        @Inject
        lateinit var appAuth: AppAuth
        private val viewModel: EventViewModel by activityViewModels()
        private val authViewModel: AuthViewModel by activityViewModels()
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {

            val binding = FragmentEventFeedBinding.inflate(inflater, container, false)

            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton(R.string.want_to_sign_in) { _, _ ->
                        findNavController().navigate(R.id.action_eventFeedFragment_to_signInFragment)
                    }
                    setNegativeButton(R.string.cancel) { _, _ ->
                        findNavController().navigateUp()
                    }
                }
                    .setIcon(R.drawable.ic_launcher_foreground)
                    .setMessage(R.string.warn_out)
                builder.create()
            }

            val adapter = LargeItemAdapter(object : OnInteractionListener {
                override fun onEdit(item: FeedItem) {
                    if (item is Event)
                        viewModel.edit(item)
                    else return
                }

                override fun onLike(item: FeedItem) {
                    if (item is Event) {
                        if (!item.likedByMe) viewModel.likeById(item.id) else viewModel.unlikeById(item.id)
                    } else return
                }

                override fun onRemove(item: FeedItem) {
                    if (item is Event)
                        viewModel.removeById(item.id)
                    else return
                }
                override fun onPlay(item: FeedItem) {
                    viewModel.playMedia(item as Event)
                }

                override fun onPause() {
                    viewModel.pause()
                }

                override fun onAuth() {
                    alertDialog?.show()
                }
            }, appAuth)

            binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
                header = ItemLoadingStateAdapter {
                    adapter.retry()
                },
                footer = ItemLoadingStateAdapter {
                    adapter.retry()
                }
            )

            lifecycleScope.launchWhenCreated {
                viewModel.data.collectLatest(adapter::submitData)
            }

            authViewModel.data.observe(viewLifecycleOwner) {
                setFragmentResultListener("signInClosed") { _, _ ->
                    if (authViewModel.authenticated) {
                        adapter.refresh()
                    }
                }
            }

            viewModel.dataState.observe(viewLifecycleOwner) { state ->
                binding.progress.isVisible = state is FeedModelState.Loading
                if (state is FeedModelState.Error) {
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { adapter.refresh() }
                        .show()
                }
            }

            viewModel.edited.observe(viewLifecycleOwner) { event ->
                if (event.id == 0L) {
                    return@observe
                }
                findNavController()
                    .navigate(R.id.action_eventFeedFragment_to_newEventFragment, bundleOf(
                        "content" to event.content,
                        "type" to event.type,
                        "datetime" to event.datetime
                    ))
            }

            binding.fabE.setOnClickListener {
                if (!authViewModel.authenticated) {
                    alertDialog?.show()
                } else if (authViewModel.authenticated) {
                    findNavController().navigate(R.id.action_eventFeedFragment_to_newEventFragment)
                }
                setFragmentResultListener("signInClosed") { _, _ ->
                    if (authViewModel.authenticated) {
                        findNavController().navigate(R.id.action_eventFeedFragment_to_newEventFragment)
                    }
                }
            }
            return binding.root
        }
    }
