package ru.netology.workmeet.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import ru.netology.workmeet.R
import ru.netology.workmeet.databinding.FragmentNewPostBinding
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.util.AndroidUtils
import ru.netology.workmeet.util.StringArg
import ru.netology.workmeet.viewModel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()
    private var fragmentBinding: FragmentNewPostBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var type: String? = null
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.edit.requestFocus()


        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state is FeedModelState.Error) {
                Snackbar
                    .make(binding.root, R.string.error_loading, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry_loading) {
                        viewModel.save()
                    }.show()
            }
        }

        val pickMediaLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    Activity.RESULT_CANCELED -> {
                        Snackbar
                            .make(binding.root, R.string.error_loading, Snackbar.LENGTH_SHORT)
                            .show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        val stream = uri?.let { it1 ->
                            requireContext().contentResolver.openInputStream(
                                it1
                            )
                        }
                        viewModel.changeFile(uri = uri, type = type, inputStream = stream)
                    }
                }
            }
        binding.pickPhoto.setOnClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_GET_CONTENT
                }.setType( "image/*")
                type = intent.type
                pickMediaLauncher.launch(intent)
            }

        binding.addAudio.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType( "audio/*")
            type = intent.type
            pickMediaLauncher.launch(intent)
        }

        binding.addVideo.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType( "video/*")
            type = intent.type
            pickMediaLauncher.launch(intent)
        }



        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickMediaLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.changeFile(null, null, null)
        }

        viewModel.upload.observe(viewLifecycleOwner) {
            binding.photo.setImageURI(it.uri)
            binding.photoContainer.isVisible = it.uri != null
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_newPostFragment_to_postFeedFragment)
        }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentBinding?.let {
                            viewModel.changeContent(it.edit.text.toString())
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }
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