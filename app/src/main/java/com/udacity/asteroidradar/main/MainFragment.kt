package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.adapter.AsteroidAdapter
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(requireActivity().application)).get(MainViewModel::class.java)
    }

    private lateinit var adapter: AsteroidAdapter
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        setHasOptionsMenu(true)
        adapter = AsteroidAdapter(AsteroidAdapter.OnClickListener {
            findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
        })

        binding.asteroidRecycler.adapter = adapter
        viewModel.asteroids.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.status.observe(viewLifecycleOwner) { status ->
            when (status) {
                MainViewModel.NasaApiStatus.LOADING -> {
                    binding.apply {
                        statusLoadingWheel.visibility = View.VISIBLE
                    }
                }

                MainViewModel.NasaApiStatus.DONE -> {
                    binding.statusLoadingWheel.visibility = View.GONE
                }

                else -> {
                    binding.statusLoadingWheel.visibility = View.GONE
                    binding.activityMainImageOfTheDay.setImageResource(R.drawable.ic_broken_image)
                }
            }
        }


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_all_menu -> {
                viewModel.getWeekAsteroid().observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                }
            }
            R.id.show_rent_menu -> {
                viewModel.getTodayAsteroids().observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                }
            }
            R.id.show_buy_menu -> {
                viewModel.asteroids.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                }
            }
        }
        return true
    }
}
