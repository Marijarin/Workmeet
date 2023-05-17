package ru.netology.workmeet.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.internal.NavigationMenu
import com.google.android.material.navigation.NavigationBarMenuView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.workmeet.R
import ru.netology.workmeet.adapter.ItemLoadingStateAdapter
import ru.netology.workmeet.adapter.LargeItemAdapter
import ru.netology.workmeet.adapter.OnInteractionListener
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.FragmentPostFeedBinding
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Post
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.ui.NewPostFragment.Companion.textArg
import ru.netology.workmeet.viewModel.AuthViewModel
import ru.netology.workmeet.viewModel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostFeedFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentPostFeedBinding.inflate(inflater, container, false)

        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.want_to_sign_in) { _, _ ->
                    findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
                }
                setNegativeButton(R.string.cancel) { _, _ ->
                    findNavController().navigateUp()
                }
            }
                .setIcon(R.drawable.ic_launcher_foreground)
                .setMessage(R.string.warn_out)
            builder.create()
        }

        fun postToJson(post: Post) = Gson().toJson(post)

        val adapter = LargeItemAdapter(object : OnInteractionListener {
            override fun onEdit(item: FeedItem) {
                if (item is Post && item.authorId == appAuth.state.value.id)
                viewModel.edit(item)
                else return
            }

            override fun onLike(item: FeedItem) {
                if (item is Post) {
                    if (!item.likedByMe) viewModel.likeById(item.id) else viewModel.unlikeById(
                        item.id
                    )
                } else return
            }

            override fun onRemove(item: FeedItem) {
                if (item is Post && item.authorId == appAuth.state.value.id)
                viewModel.removeById(item.id)
                else return
            }

            override fun onAuth() {
                alertDialog?.show()
            }

            override fun onUser(item: FeedItem) {
                if (item is Post && authViewModel.authenticated) {
                    val json = postToJson(item)
                    findNavController( ).navigate(R.id.action_postFeedFragment_to_wallFragment,
                        Bundle().apply { textArg = json })
                } else  alertDialog?.show()
            }

            override fun onPlay(item: FeedItem) {
                viewModel.playAudio(item as Post)
            }

            override fun onPause() {
                viewModel.pause()
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
            if (authViewModel.authenticated) {
                adapter.refresh()
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

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (post.id == 0L) {
                return@observe
            }
            findNavController()
                .navigate(R.id.action_postFeedFragment_to_newPostFragment, Bundle().apply {
                    textArg = post.content
                })

        }

        binding.fab.setOnClickListener {
            if (!authViewModel.authenticated) {
                alertDialog?.show()
            } else if (authViewModel.authenticated) {
            findNavController().navigate(R.id.action_postFeedFragment_to_newPostFragment)
        }
            setFragmentResultListener("signInClosed") { _, _ ->
                if (authViewModel.authenticated) {
                    findNavController().navigate(R.id.action_postFeedFragment_to_newPostFragment)
                }
            }
        }
            return binding.root
        }
}