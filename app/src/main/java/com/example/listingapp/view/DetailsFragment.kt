package com.example.listingapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.listingapp.R
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.UserDatabase
import com.example.listingapp.databinding.FragmentUserDetailsBinding
import com.example.listingapp.viewmodel.ListViewModel


class DetailsFragment : Fragment(R.layout.fragment_user_details) {
    private var _binding: FragmentUserDetailsBinding? = null
    private val viewModel by viewModels<ListViewModel>()
    private val binding get() = this._binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun loadData() {
                val position = DetailsFragmentArgs.fromBundle(requireArguments()).position
        val dbHelper = context?.let { UserDatabase.DatabaseBuilder.getInstance(it) }?.let {
            DatabaseHelperImpl(
                it
            )
        }
        if (dbHelper != null) {
            viewModel.fetchDataFromDb(dbHelper)
        }
        viewModel.userDetailsFromDb.observe(viewLifecycleOwner, { list ->
            this.binding.firstName.text = list[position].firstName + " " + list[position].lastName
            this.binding.ageVal.text = list[position].age
            this.binding.genderVal.text = list[position].gender
            this.binding.cityVal.text = list[position].city
            this.binding.phoneVal.text = list[position].phone
            this.binding.emailVal.text = list[position].email
            context?.let {
                Glide.with(it)
                    .load(list[position].thumbnail)
                    .centerCrop()
                    .into(this.binding.profileImg)
            }
            list[position].longitude?.let {
                list[position].latitude?.let { it1 ->
                    getWeatherData(
                        it1,
                        it
                    )
                }
            }
        })
    }

    private fun getWeatherData(lat: Double, longitude: Double) {
        viewModel.getWeatherDetails(lat, longitude)
        viewModel.weatherModel.observe(viewLifecycleOwner, {
            Toast.makeText(context, it.wind?.deg.toString(), Toast.LENGTH_SHORT).show()
            this.binding.toolbar.setTemperature(it.wind?.deg.toString())
            it.name?.let { it1 -> this.binding.toolbar.setCity(it1) }
            it.weather?.firstOrNull()?.description?.let { it1 -> this.binding.toolbar.setArea(it1) }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}