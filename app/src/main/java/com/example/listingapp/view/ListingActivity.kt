package com.example.listingapp.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.User
import com.example.listingapp.database.UserDatabase
import com.example.listingapp.databinding.ActivityListingBinding
import com.example.listingapp.viewmodel.ListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ListingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListingBinding
    private var _sGridLayoutManager: StaggeredGridLayoutManager? = null
    private val viewModel by viewModels<ListViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.searchView.isIconified = false


    }

    private fun recyclerView(list: List<User>) {
        binding.recyclerView.setHasFixedSize(true)
        _sGridLayoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        binding.recyclerView.layoutManager = _sGridLayoutManager
        val sList: List<User> = list
        val rcAdapter = RecyclerViewAdapter(
            this, sList
        )
        binding.recyclerView.adapter = rcAdapter
    }


    override fun onStart() {
        super.onStart()
        viewModel.userDetailsResponse.observe(this, {
            lifecycleScope.launch {
                delay(3000)
//                    view?.findNavController()
//                        ?.navigate(R.id.action_splashFragment_to_userListFragment)
            }
        })
        val dbHelper = DatabaseHelperImpl(
            this.let { UserDatabase.DatabaseBuilder.getInstance(it) }
        )
        getUserDetails(dbHelper)
        viewModel.fetchDataFromDb(dbHelper)
        viewModel.userDetailsFromDb.observe(this, { list ->
            recyclerView(list)
        })
    }

    private fun getUserDetails(dbHelper: DatabaseHelperImpl) {
        viewModel.getUserDetails(dbHelper)
    }


}