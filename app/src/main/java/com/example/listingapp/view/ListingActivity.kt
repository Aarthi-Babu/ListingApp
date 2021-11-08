package com.example.listingapp.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.listingapp.R
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.User
import com.example.listingapp.database.UserDatabase
import com.example.listingapp.databinding.ActivityListingBinding
import com.example.listingapp.viewmodel.ListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class ListingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListingBinding
    private var _sGridLayoutManager: StaggeredGridLayoutManager? = null
    private val viewModel by viewModels<ListViewModel>()
    private var adapter: RecyclerViewAdapter? = null
    private var sList: List<User>? = null

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
        sList = list
        sList?.let {
            adapter = RecyclerViewAdapter(
                this, it
            )
            binding.recyclerView.adapter = adapter
        }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem: MenuItem = menu.findItem(R.id.actionSearch)
        val searchView = searchItem.actionView as SearchView
        searchView.setIconifiedByDefault(false)
        searchView.onActionViewExpanded()


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
        return true
    }

    private fun filter(text: String) {
        val filteredlist: ArrayList<User> = ArrayList()
        sList?.let {
            for (item in it) {
                if (item.firstName?.lowercase(Locale.getDefault())
                        ?.contains(text.lowercase(Locale.getDefault())) == true
                ) {
                    filteredlist.add(item)
                }
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            adapter?.filterList(filteredlist)
        }
    }


}