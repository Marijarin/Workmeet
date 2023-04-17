package ru.netology.workmeet.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.netology.workmeet.util.StringArg
import ru.netology.workmeet.viewModel.PostViewModel

class NewPostFragment: Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels()
}