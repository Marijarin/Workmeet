package ru.netology.workmeet.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.workmeet.R
import ru.netology.workmeet.databinding.FragmentNewEventBinding
import ru.netology.workmeet.dto.EventType
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.util.AndroidUtils
import ru.netology.workmeet.viewModel.EventViewModel
import java.util.*

@AndroidEntryPoint
class NewEventFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private val viewModel: EventViewModel by activityViewModels()
    private var fragmentBinding: FragmentNewEventBinding? = null

    companion object {
        var date = 0L
        var dateString: String = " "
        var type = EventType.OFFLINE
        var day = 0
        var month: Int = 0
        var year: Int = 0
        var hour: Int = 0
        var minute: Int = 0
        var myDay = 0
        var myMonth: Int = 0
        var myYear: Int = 0
        var myHour: Int = 0
        var myMinute: Int = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding
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
        binding.datetime.setOnClickListener {
            getDateTimeCalendar()
            val dialog = DatePickerDialog(requireContext(), this, year, month, day)
            dialog.show()
            binding.datetimeText.text = dateString
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
                        viewModel.changePhoto(uri, uri?.toFile())
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
                        if (uri != null && stream != null) {
                            viewModel.changeFile(uri, stream)


                        }
                    }
                }
            }

        binding.addAudio.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType("audio/*")
            pickMediaLauncher.launch(intent)
        }

        binding.addVideo.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType("video/*")
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
            viewModel.changePhoto(null, null)
        }

        viewModel.media.observe(viewLifecycleOwner) {
            binding.photo.setImageURI(it.uri)
            binding.photoContainer.isVisible = it.uri != null
        }



        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            type = if (checkedId == R.id.radioOff) {
                EventType.OFFLINE
            } else EventType.ONLINE
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            viewModel.loadEvents()
            findNavController().navigateUp()
        }
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.save -> {
                            viewModel.changeContent(
                                content = binding.edit.text.toString(),
                                type = type,
                                datetime = AndroidUtils.dateForServer(date)
                            )
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                            true
                        }

                        else -> false
                    }
                }

            }, viewLifecycleOwner
        )
        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }

    private fun getDateTimeCalendar() {
        val calendar: Calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        myDay = dayOfMonth
        myYear = year
        myMonth = month
        getDateTimeCalendar()
        TimePickerDialog(requireContext(), this@NewEventFragment, hour, minute,true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute
        dateString = "$myDay.${myMonth+1}.$myYear $myHour:$myMinute"
        val calendar = Calendar.getInstance()
        calendar.set(myYear, myMonth, myDay, myHour, myMinute)
        date = calendar.timeInMillis
    }
}