package com.example.listingapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.listingapp.R
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.User
import com.example.listingapp.database.UserDatabase
import com.example.listingapp.databinding.FragmentListBinding
import com.example.listingapp.viewmodel.ListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class ListFragment : Fragment(R.layout.fragment_list) {
    private lateinit var binding: FragmentListBinding
    private var _sGridLayoutManager: StaggeredGridLayoutManager? = null
    private val viewModel by viewModels<ListViewModel>()
    private var adapter: RecyclerViewAdapter? = null
    private var sList: List<User>? = null
    private val dbHelper = context?.let { UserDatabase.DatabaseBuilder.getInstance(it) }?.let {
        DatabaseHelperImpl(
            it
        )
    }
    private val lastVisibleItemPosition = 25
    private lateinit var scrollListener: RecyclerView.OnScrollListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root

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
            adapter = context?.let { it1 ->
                RecyclerViewAdapter(
                    it1, it
                ) {
                    view?.findNavController()
                        ?.navigate(R.id.action_listFragment_to_detailsFragment)
                }
            }
            binding.recyclerView.adapter = adapter
        }

    }

    override fun onStart() {
        super.onStart()
        viewModel.userDetailsResponse.observe(this, {
            lifecycleScope.launch {
                delay(3000)

            }
        })
        val dbHelper = context?.let { UserDatabase.DatabaseBuilder.getInstance(it) }?.let {
            DatabaseHelperImpl(
                it
            )
        }
        dbHelper?.let { getUserDetails() }
        dbHelper?.let { viewModel.fetchDataFromDb(it) }
        viewModel.userDetailsFromDb.observe(this, { list ->
            recyclerView(list)
//            binding.loadingIndicator.visibility = View.GONE
        })
        binding.searchView.isIconified = false
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
        scrollListener()
    }

    private fun scrollListener() {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager?.itemCount
                if (totalItemCount == lastVisibleItemPosition + 1) {
                    Toast.makeText(context, "New load Found..", Toast.LENGTH_SHORT).show()
                    binding.recyclerView.removeOnScrollListener(scrollListener)
                }
            }
        }
        binding.recyclerView.addOnScrollListener(scrollListener)
    }


    private fun getUserDetails() {
        dbHelper?.let { viewModel.getUserDetails(it) }
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<User> = ArrayList()
        sList?.let {
            for (item in it) {
                if (item.firstName?.lowercase(Locale.getDefault())
                        ?.contains(text.lowercase(Locale.getDefault())) == true
                ) {
                    filteredList.add(item)
                }
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(context, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            adapter?.filterList(filteredList)
        }
    }

}