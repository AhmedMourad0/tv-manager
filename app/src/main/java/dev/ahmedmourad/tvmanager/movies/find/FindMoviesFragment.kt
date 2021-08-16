package dev.ahmedmourad.tvmanager.movies.find

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import dev.ahmedmourad.tvmanager.R
import dev.ahmedmourad.tvmanager.common.AssistedViewModelFactory
import dev.ahmedmourad.tvmanager.common.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.tvmanager.databinding.FragmentFindMoviesBinding
import dev.ahmedmourad.tvmanager.di.injector
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import javax.inject.Provider

class FindMoviesFragment : Fragment(R.layout.fragment_find_movies) {

    @Inject
    lateinit var viewModelFactory: Provider<AssistedViewModelFactory<FindMoviesViewModel>>

    private val viewModel: FindMoviesViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
            this,
            viewModelFactory,
            FindMoviesViewModel.defaultArgs()
        )
    }

    private var binding: FragmentFindMoviesBinding? = null

    private lateinit var adapter: MoviesRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFindMoviesBinding.bind(view)
        initializePostsList()
        initializeStateObservers()
        initializeAddMovieFab()
        initializeSwipeToRefresh()
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding!!.appBar.toolbar)
            title = getString(R.string.app_name)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_find_movies, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                val action = FindMoviesFragmentDirections
                    .actionFindMoviesFragmentToAddMovieFragment()
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializePostsList() {
        adapter = MoviesRecyclerAdapter(requireContext())
        adapter.stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding!!.let { b ->
            b.recycler.adapter = adapter
            b.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            b.recycler.isVerticalScrollBarEnabled = true
        }
    }

    private fun initializeAddMovieFab() {
        binding!!.newerDataFab.setOnClickListener {
            binding!!.newerDataFab.hide()
            viewModel.enforceNewerData()
        }
    }

    private fun initializeSwipeToRefresh() {
        binding!!.swipeToRefresh.setColorSchemeColors(Color.BLACK)
        binding!!.swipeToRefresh.setOnRefreshListener {
            viewModel.onRefresh()
        }
    }

    private fun initializeStateObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is FindMoviesViewModel.State.Data -> {
                        itemsMode()
                        adapter.submitData(state.data)
                    }
                    FindMoviesViewModel.State.NoConnection -> {
                        if (adapter.itemCount > 0) {
                            Toast.makeText(
                                requireContext(),
                                R.string.no_internet_connection,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            noConnectionMode()
                        }
                    }
                    is FindMoviesViewModel.State.Error -> {
                        if (adapter.itemCount > 0) {
                            Toast.makeText(
                                requireContext(),
                                R.string.something_went_wrong,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            errorMode()
                        }
                    }
                    FindMoviesViewModel.State.Loading -> {
                        loadingMode()
                        adapter.submitData(PagingData.empty())
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isNewerDataAvailable.collectLatest { isAvailable ->
                if (isAvailable) {
                    binding!!.newerDataFab.show()
                } else {
                    binding!!.newerDataFab.hide()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.endRefresh.collectLatest {
                binding!!.swipeToRefresh.isRefreshing = false
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.clearList.collectLatest {
                adapter.submitData(PagingData.empty())
            }
        }
    }

    private fun itemsMode() {
        binding!!.let { b ->
            b.recycler.visibility = VISIBLE
            b.error.root.visibility = GONE
            b.loading.root.visibility = GONE
            b.swipeToRefresh.isRefreshing = false
        }
    }

    private fun errorMode() {
        binding!!.let { b ->
            b.newerDataFab.hide()
            b.recycler.visibility = GONE
            b.loading.root.visibility = GONE
            b.swipeToRefresh.isRefreshing = false
            b.error.root.visibility = VISIBLE
            b.error.errorMessage.setText(R.string.something_went_wrong)
            b.error.errorIcon.setImageResource(R.drawable.ic_error)
        }
    }

    private fun noConnectionMode() {
        binding!!.let { b ->
            b.newerDataFab.hide()
            b.recycler.visibility = GONE
            b.loading.root.visibility = GONE
            b.swipeToRefresh.isRefreshing = false
            b.error.root.visibility = VISIBLE
            b.error.errorMessage.setText(R.string.no_internet_connection)
            b.error.errorIcon.setImageResource(R.drawable.ic_no_internet)
        }
    }

    private fun loadingMode() {
        binding!!.let { b ->
            b.newerDataFab.hide()
            b.recycler.visibility = GONE
            b.error.root.visibility = GONE
            b.loading.root.visibility = VISIBLE
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
