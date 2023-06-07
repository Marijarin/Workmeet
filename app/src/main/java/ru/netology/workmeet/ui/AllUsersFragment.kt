package ru.netology.workmeet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.workmeet.R
import ru.netology.workmeet.adapter.OnUserInteractionListener
import ru.netology.workmeet.adapter.UserAdapter
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.FragmentAllUsersBinding
import ru.netology.workmeet.viewModel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AllUsersFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: UserViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAllUsersBinding.inflate(inflater, container, false)

        val adapter = UserAdapter(object: OnUserInteractionListener {
            override fun onAvatar(userId: Long) {
                findNavController().navigate(
                        R.id.action_allUsersFragment_to_wallFragment,
                        bundleOf("userId" to userId)
                    )
            }

            override fun onPick(userId: Long) {
                                    findNavController().navigateUp()
                   setFragmentResult("user selected", bundleOf("mentionedId" to userId))
                }
            })
        binding.list.adapter = adapter
        lifecycleScope.launchWhenCreated {
        viewModel.data.collectLatest(adapter::submitList)
        }
        return binding.root
    }
}