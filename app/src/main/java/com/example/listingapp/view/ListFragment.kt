package com.example.listingapp.view


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.User
import com.example.listingapp.database.UserDatabase
import com.example.listingapp.databinding.FragmentListBinding
import com.example.listingapp.utils.Resource
import com.example.listingapp.utils.isNetworkConnected
import com.example.listingapp.viewmodel.ListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val viewModel: ListViewModel by activityViewModels()
    private var adapter: RecyclerViewAdapter? = null
    private var sList: ArrayList<User>? = null
    private var locationManager: LocationManager? = null
    private val binding get() = _binding!!
    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    getLocation()
                } else {
                    onGPS()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onStart()
        invokeDB()
        loadData()
        recyclerView()
        viewListener()
        getLocation()

    }

    private fun invokeDB() {
        val dbHelper =
            context?.let { DatabaseHelperImpl(UserDatabase.DatabaseBuilder.getInstance(it)) }
        dbHelper?.let {
            viewModel.getUserDetails(it).observe(viewLifecycleOwner) { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        binding.progressBar.isVisible = false
                    }
                    Resource.Status.ERROR -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(context, "user api error.", Toast.LENGTH_SHORT).show()
                    }
                    Resource.Status.LOADING -> {
                        binding.progressBar.isVisible = true
                    }
                }
            }
        }
        dbHelper?.let { viewModel.getUserDetails(it) }
    }

    private fun loadData() {
        if (context?.isNetworkConnected() == true) {
            viewModel.userDetailsResponse.observeForever {
                lifecycleScope.launch {
                    adapter?.addData(it)
                    sList = it
                }
            }
        } else {
            viewModel.userDetailsFromDb.value?.let { list ->
                adapter?.addData(list)
                sList = list

            }
        }
    }

    private fun recyclerView() {
        binding.recyclerView.setHasFixedSize(true)
        val sGridLayoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        binding.recyclerView.layoutManager = sGridLayoutManager

        adapter = RecyclerViewAdapter {
            view?.findNavController()
                ?.navigate(ListFragmentDirections.actionListFragmentToDetailsFragment(it))
        }

        val animate = TranslateAnimation(
            0F,
            0F,
            0F,
            0F
        ).apply {
            duration = 1000
            fillAfter = true
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.startAnimation(animate)

    }

    private fun getWeatherData(lat: Double, longitude: Double) {
        viewModel.getWeatherDetails(lat, longitude).observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
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
                    Toast.makeText(context, "weather api error.", Toast.LENGTH_SHORT).show()
                }
                Resource.Status.LOADING -> {
                    //
                }
            }
        }
    }

    private fun viewListener() {
        binding.searchView.isIconified = false
        binding.searchView.clearFocus()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val visibleItemCount = recyclerView.layoutManager?.childCount
                val firstVisibleItemPosition =
                    (recyclerView.layoutManager as StaggeredGridLayoutManager?)!!.findFirstVisibleItemPositions(
                        null
                    )?.let {
                        getFirstVisibleItem(
                            it
                        )
                    }
                val totalItemCount = sList?.size
                if (firstVisibleItemPosition?.let { visibleItemCount?.plus(it) }!! == totalItemCount) {
                    invokeDB()
                }
            }
        }
        binding.recyclerView.addOnScrollListener(scrollListener)
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
            adapter?.filterList(filteredList)
        } else {
            adapter?.filterList(filteredList)
        }
    }

    private fun openAppInfo() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package",
            context?.packageName, null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun onGPS() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("Enable Location it will help you to have better experience")
            .setCancelable(false).setPositiveButton(
                "Yes"
            ) { _, _ -> openAppInfo() }
            .setNegativeButton(
                "No"
            ) { dialog, _ -> dialog.cancel() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun getLocation() {
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        when {
            context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED -> {
                val locationGPS: Location? =
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (locationGPS != null) {
                    val lat = locationGPS.latitude
                    val longitude = locationGPS.longitude
                    getWeatherData(lat, longitude)
                } else {
                    Toast.makeText(context, "Unable to find location.", Toast.LENGTH_SHORT).show()
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    context,
                    "please allow location for better experience.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                requestPermissionLauncher?.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    fun getFirstVisibleItem(firstVisibleItemPositions: IntArray): Int {
        var minSize = 0
        if (firstVisibleItemPositions.isNotEmpty()) {
            minSize = firstVisibleItemPositions[0]
            for (position in firstVisibleItemPositions) {
                if (position < minSize) {
                    minSize = position
                }
            }
        }
        return minSize
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}