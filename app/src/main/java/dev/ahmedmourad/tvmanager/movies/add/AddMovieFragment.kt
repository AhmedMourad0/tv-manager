package dev.ahmedmourad.tvmanager.movies.add

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dev.ahmedmourad.tvmanager.R
import dev.ahmedmourad.tvmanager.common.AssistedViewModelFactory
import dev.ahmedmourad.tvmanager.common.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.tvmanager.databinding.FragmentAddMovieBinding
import dev.ahmedmourad.tvmanager.di.injector
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Provider


private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

class AddMovieFragment : Fragment(R.layout.fragment_add_movie) {

    @Inject
    lateinit var viewModelFactory: Provider<AssistedViewModelFactory<AddMoviesViewModel>>

    private val viewModel: AddMoviesViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
            this,
            viewModelFactory,
            AddMoviesViewModel.defaultArgs()
        )
    }

    private var binding: FragmentAddMovieBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddMovieBinding.bind(view)
        initializeStateObservers()
        initializeInputFields()
        initializeAddButton()
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding!!.appBar.toolbar)
            title = getString(R.string.add_movie)
        }
    }

    private fun initializeStateObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.addMovieState.collectLatest { state ->
                when (state) {
                    is AddMoviesViewModel.AddMovieState.Success -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.movie_added_successfully,
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigateUp()
                    }
                    is AddMoviesViewModel.AddMovieState.NoConnection -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.no_internet_connection,
                            Toast.LENGTH_LONG
                        ).show()
                        setAddEnabled(viewModel.canAdd.value)
                    }
                    is AddMoviesViewModel.AddMovieState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.something_went_wrong,
                            Toast.LENGTH_LONG
                        ).show()
                        setAddEnabled(viewModel.canAdd.value)
                    }
                    null -> Unit
                }
                viewModel.addMovieState.value = null
            }
        }
    }

    private fun initializeInputFields() {
        val b = binding!!

        b.title.editText!!.setText(viewModel.title.value)
        b.seasonsCount.editText!!.setText(viewModel.seasonsCount.value?.toString())

        b.title.editText!!.doOnTextChanged { text, _, _, _ ->
            viewModel.onTitleChanged(text?.toString())
        }
        b.seasonsCount.editText!!.doOnTextChanged { text, _, _, _ ->
            viewModel.onSeasonsCountChanged(text?.toString()?.toDoubleOrNull())
        }

        lifecycleScope.launchWhenStarted {
            viewModel.releaseDate.collectLatest {
                val date = viewModel.releaseDate.value?.format(dateFormat)
                b.releaseDateLabel.text = if (date != null) {
                    getString(R.string.release_date_formatted, date)
                } else {
                    getString(R.string.click_to_select_release_date)
                }
            }
        }

        b.releaseDateLabel.setOnClickListener {
            startReleaseDatePicker()
        }
        b.releaseDateIcon.setOnClickListener {
            startReleaseDatePicker()
        }
    }

    private fun initializeAddButton() {
        binding!!.addButton.setOnClickListener {
            viewModel.onAddMovie()
            setAddEnabled(false)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.canAdd.collectLatest { enable ->
                setAddEnabled(enable)
            }
        }
    }

    private fun setAddEnabled(enabled: Boolean) {
        binding!!.addButton.isEnabled = enabled
        binding!!.addButton.alpha = if (enabled) 1f else 0.7f
    }

    //TODO: disable add button and release date label and image until this finishes
    private fun startReleaseDatePicker() {
        val date = OnDateSetListener { _, year, month, dayOfMonth ->
            viewModel.onReleaseDateChanged(LocalDate.of(year, month, dayOfMonth))
        }
        val now = LocalDate.now()
        val dialog = DatePickerDialog(
            requireContext(),
            date,
            now.year,
            now.monthValue,
            now.dayOfMonth
        )
        dialog.setOnShowListener {
            val positive = dialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            positive.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.teal_700)
            )
            positive.text = getString(R.string.select)
            dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red_500)
            )
        }
        dialog.show()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
