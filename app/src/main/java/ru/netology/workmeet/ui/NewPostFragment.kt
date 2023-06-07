package ru.netology.workmeet.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var type: String? = null
        var mentionedUser: Long? = null

        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding
        setFragmentResultListener("user selected") { key, bundle ->
            mentionedUser = bundle.getLong("mentionedId")
            Snackbar.make(binding.root, key, Snackbar.LENGTH_SHORT).show()
        }
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

                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        val stream = uri?.let { yourUri ->
                            requireContext().contentResolver.openInputStream(
                                yourUri
                            )
                        }
                        viewModel.changeFile(
                            uri = uri,
                            type = type,
                            inputStream = stream
                        )
                    }
                }
            }
        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .createIntent {
                    it.apply {
                        action = Intent.ACTION_GET_CONTENT
                    }.type = "image/*"
                    type = it.type
                    pickMediaLauncher.launch(it)
                }
        }

        binding.addAudio.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType("audio/*")
            type = intent.type
            pickMediaLauncher.launch(intent)
        }

        binding.addVideo.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType("video/*")
            type = intent.type
            pickMediaLauncher.launch(intent)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent {
                    it.apply {
                        action = Intent.ACTION_GET_CONTENT
                    }.type = "image/*"
                    type = it.type
                    pickMediaLauncher.launch(it)
                }
        }

        binding.removePhoto.setOnClickListener {
            viewModel.changeFile(null, null, null)
        }

        binding.addMentioned.setOnClickListener {
            findNavController().navigate(R.id.action_newPostFragment_to_allUsersFragment)
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
                        viewModel.mentionUser(mentionedUser)
                        viewModel.changeContent(binding.edit.text.toString())
                        viewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
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