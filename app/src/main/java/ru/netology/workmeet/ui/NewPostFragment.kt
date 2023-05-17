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
        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changeAttachment(uri, uri?.toFile())
                    }
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
                        if (uri != null && stream!=null) {
                            viewModel.changeFile(uri, stream)


                        }
                    }
                }
            }

        binding.addAudio.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType( "audio/*")
            pickMediaLauncher.launch(intent)
        }

        binding.addVideo.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType( "video/*")
            pickMediaLauncher.launch(intent)
        }


        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.changeAttachment(null, null)
        }

        viewModel.media.observe(viewLifecycleOwner) {
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