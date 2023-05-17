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
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.workmeet.R
import ru.netology.workmeet.adapter.ItemLoadingStateAdapter
import ru.netology.workmeet.adapter.OnInteractionListener
import ru.netology.workmeet.adapter.WallAdapter
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.FragmentWallBinding
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Post
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.ui.NewPostFragment.Companion.textArg
import ru.netology.workmeet.viewModel.AuthViewModel
import ru.netology.workmeet.viewModel.WallViewModel
import javax.inject.Inject

@AndroidEntryPoint
class WallFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: WallViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentWallBinding.inflate(inflater, container, false)

        fun postFromJson(json: String) = Gson().fromJson(json, Post::class.java)
        val post = arguments?.textArg?.let { postFromJson(it) }



        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.want_to_sign_in) { _, _ ->
                    findNavController().navigate(R.id.action_wallFragment_to_signInFragment)
                }
                setNegativeButton(R.string.cancel) { _, _ ->
                    findNavController().navigateUp()
                }
            }
                .setIcon(R.drawable.ic_launcher_foreground)
                .setMessage(R.string.warn_out)
            builder.create()
        }

        val adapter = WallAdapter(object : OnInteractionListener {
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
            if (post!=null ) {
                viewModel.setUserId(post.authorId)
                binding.apply {
                    when (post.authorAvatar) {
                        null -> Glide.with(avatar)
                            .load(R.drawable.avatar3)
                            .circleCrop()
                            .error(R.drawable.twotone_error_outline_24)
                            .into(avatar)
                        else -> Glide.with(avatar)
                            .load("${post.authorAvatar}")
                            .circleCrop()
                            .placeholder(R.drawable.avatar3)
                            .error(R.drawable.twotone_error_outline_24)
                            .timeout(10_000)
                            .into(avatar)
                    }
                    author.text = post.author
                    authorJob.text = post.authorJob
                }
                viewModel.uData.collectLatest(adapter::submitData)
            } else {
                viewModel.getUserById(appAuth.state.value.id)
                viewModel.setUserId(appAuth.state.value.id)
                viewModel.user.collectLatest {
                    binding.apply {
                        when (it.avatar) {
                            null -> Glide.with(avatar)
                                .load(R.drawable.avatar3)
                                .circleCrop()
                                .error(R.drawable.twotone_error_outline_24)
                                .into(avatar)
                            else -> Glide.with(avatar)
                                .load("${it.avatar}")
                                .circleCrop()
                                .placeholder(R.drawable.avatar3)
                                .error(R.drawable.twotone_error_outline_24)
                                .timeout(10_000)
                                .into(avatar)
                        }
                        author.text = it.name
                    }
                }
                viewModel.myData.collectLatest (adapter::submitData)
            }
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

        viewModel.edited.observe(viewLifecycleOwner) { postEd ->
            if (postEd.id == 0L) {
                return@observe
            }
            findNavController()
                .navigate(R.id.action_postFeedFragment_to_newPostFragment, Bundle().apply {
                    textArg = postEd.content
                })

        }
        binding.toJobs.setOnClickListener {
            if (!authViewModel.authenticated) {
                alertDialog?.show()
            } else if (authViewModel.authenticated) {
                findNavController().navigate(R.id.action_wallFragment_to_userJobsFragment, bundleOf( "userId" to viewModel.userId.value))
            }
            setFragmentResultListener("signInClosed") { _, _ ->
                if (authViewModel.authenticated) {
                    findNavController().navigate(R.id.action_wallFragment_to_userJobsFragment, bundleOf( "userId" to viewModel.userId.value))
                }
            }
        }

        if (viewModel.userId.value!=appAuth.state.value.id){
            binding.fabW.visibility = View.GONE
            binding.message.visibility = View.VISIBLE
            binding.invite.visibility = View.VISIBLE
        }
        binding.fabW.setOnClickListener {
            findNavController().navigate(R.id.action_postFeedFragment_to_newPostFragment)
        }
        return binding.root
    }
}