package com.example.listingapp.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.listingapp.R
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.UserDatabase
import com.example.listingapp.databinding.FragmentUserDetailsBinding
import com.example.listingapp.viewmodel.ListViewModel


class DetailsFragment : Fragment(R.layout.fragment_user_details) {
    private lateinit var binding: FragmentUserDetailsBinding
    private val viewModel by viewModels<ListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
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
            binding.firstName.text = list[position].firstName + " " + list[position].lastName
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
        })
    }

}