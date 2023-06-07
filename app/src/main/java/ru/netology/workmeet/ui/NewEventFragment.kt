package ru.netology.workmeet.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.result.contract.ActivityResultContracts
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
        var typeAtt: String? = null
        var mentionedUser: Long? = null
        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding
        binding.edit.requestFocus()

        setFragmentResultListener("user selected") { key, bundle ->
            mentionedUser = bundle.getLong("mentionedId")
            Snackbar.make(binding.root, key, Snackbar.LENGTH_SHORT).show()
        }

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
                        if (uri != null && typeAtt != null) {
                            viewModel.changeFile(uri, stream, typeAtt!!)
                        }
                    }
                }
            }

        binding.addAudio.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType("audio/*")
            typeAtt = intent.type
            pickMediaLauncher.launch(intent)
        }

        binding.addVideo.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
            }.setType("video/*")
            typeAtt = intent.type
            pickMediaLauncher.launch(intent)
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
                    typeAtt = it.type
                    pickMediaLauncher.launch(it)
                }
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
                    typeAtt = it.type
                    pickMediaLauncher.launch(it)
                }
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
        binding.addSpeaker.setOnClickListener{
            findNavController().navigate(R.id.action_newEventFragment_to_allUsersFragment)
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
                            viewModel.speakUser(mentionedUser)
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
        TimePickerDialog(requireContext(), this@NewEventFragment, hour, minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute
        dateString = "$myDay.${myMonth + 1}.$myYear $myHour:$myMinute"
        val calendar = Calendar.getInstance()
        calendar.set(myYear, myMonth, myDay, myHour, myMinute)
        date = calendar.timeInMillis
        fragmentBinding?.datetimeText?.text = dateString
    }
}