package ru.netology.workmeet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.FragmentAllUsersBinding
import ru.netology.workmeet.viewModel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AllUsersFragment: Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: UserViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAllUsersBinding.inflate(inflater, container, false)
        return binding.root
    }
}