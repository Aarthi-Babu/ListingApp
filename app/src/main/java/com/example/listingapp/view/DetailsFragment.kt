package com.example.listingapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.listingapp.database.User
import com.example.listingapp.databinding.FragmentUserDetailsBinding
import com.example.listingapp.di.AppModule
import com.example.listingapp.utils.ProgressDialog
import com.example.listingapp.utils.Resource
import com.example.listingapp.utils.isNetworkConnected
import com.example.listingapp.viewmodel.ListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment : Fragment() {
    private var _binding: FragmentUserDetailsBinding? = null
    private val viewModel: ListViewModel by activityViewModels()
    private val binding get() = _binding!!

    @Inject
    lateinit var progressDialogFactory: AppModule.ProgressDialogFactory

    private val progressDialog: ProgressDialog by lazy { progressDialogFactory.create(this.context) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onStart()
        loadData()
    }

    private fun loadData() {
        val position = DetailsFragmentArgs.fromBundle(requireArguments()).position
        if (context?.isNetworkConnected() == true) {
            viewModel.userDetailsResponse.value?.let {
                populateData(it, position)
            }
        } else {
            viewModel.userDetailsFromDb.value?.let { list ->
                populateData(list, position)
            }
        }
    }

    private fun populateData(
        list: ArrayList<User>,
        position: Int
    ) {
        val name = list[position].firstName + " " + list[position].lastName
        binding.firstName.text = name
        binding.ageVal.text = list[position].age
        binding.genderVal.text = list[position].gender
        binding.cityVal.text = list[position].city
        binding.phoneVal.text = list[position].phone
        binding.emailVal.text = list[position].email
        context?.let {
            Glide.with(it)
                .load(list[position].thumbnail)
                .centerCrop()
                .into(binding.profileImg)
        }
        list[position].longitude?.let { longitude ->
            list[position].latitude?.let { lat ->
                getWeatherData(
                    lat,
                    longitude
                )
            }
        }
    }

    private fun getWeatherData(lat: Double, longitude: Double) {
        viewModel.getWeatherDetails(lat, longitude).observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    progressDialog.hideLoading()
                    resource?.data?.let {
                        binding.toolbar.setTemperature(it.wind?.deg.toString())
                        it.name?.let { it1 -> binding.toolbar.setCity(it1) }
                        it.weather?.firstOrNull()?.description?.let { it1 ->
                            binding.toolbar.setArea(
                                it1
                            )
                        }
                    } ?: Toast.makeText(context, "no location found.", Toast.LENGTH_SHORT).show()

                }
                Resource.Status.ERROR -> {
                    progressDialog.hideLoading()
                    Toast.makeText(context, "weather api error.", Toast.LENGTH_SHORT).show()
                }
                Resource.Status.LOADING -> {
                    progressDialog.showLoading()
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}