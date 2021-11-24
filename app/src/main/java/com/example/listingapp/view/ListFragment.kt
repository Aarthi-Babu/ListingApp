package com.example.listingapp.view


import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class ListFragment : Fragment(R.layout.fragment_list) {
    private lateinit var binding: FragmentListBinding
    private var _sGridLayoutManager: StaggeredGridLayoutManager? = null
    private val viewModel by viewModels<ListViewModel>()
    private var adapter: RecyclerViewAdapter? = null
    private var sList: ArrayList<User>? = null
    private val REQUEST_LOCATION = 1
    var locationManager: LocationManager? = null
    private lateinit var scrollListener: RecyclerView.OnScrollListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
            )
        }

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!) {
            onGPS()
        } else {
            getLocation()
        }

        viewModel.userDetailsResponse.observeForever {
            lifecycleScope.launch {
                adapter?.addData(it)
//                adapter?.notifyDataSetChanged()
            }
        }
        invokeDB()

        viewModel.userDetailsFromDb.observe(this, { list ->
//            recyclerView(list)
//            adapter?.addData(list)

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
        recyclerView()
    }

    private fun recyclerView() {
        binding.recyclerView.setHasFixedSize(true)
        _sGridLayoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        binding.recyclerView.layoutManager = _sGridLayoutManager

        adapter = context?.let { it ->
            RecyclerViewAdapter(
                it
            ) {
                view?.findNavController()
                    ?.navigate(ListFragmentDirections.actionListFragmentToDetailsFragment(it))
            }
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
        viewModel.getWeatherDetails(lat, longitude)
        viewModel.weatherModel.observe(viewLifecycleOwner, {
            Toast.makeText(context, it.wind?.deg.toString(), Toast.LENGTH_SHORT).show()
            binding.toolbar.setTemperature(it.wind?.deg.toString())
            it.name?.let { it1 -> binding.toolbar.setCity(it1) }
            it.weather?.firstOrNull()?.description?.let { it1 -> binding.toolbar.setArea(it1) }
        })
    }

    private fun scrollListener() {
        scrollListener = object : RecyclerView.OnScrollListener() {
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
                val totalItemCount = (recyclerView.layoutManager?.itemCount)
                if (firstVisibleItemPosition?.let { visibleItemCount?.plus(it) }!! == totalItemCount) {
                    Toast.makeText(context, "New load Found..", Toast.LENGTH_SHORT).show()
                    invokeDB()
//                    binding.recyclerView.removeOnScrollListener(scrollListener)
                }
            }
        }
        binding.recyclerView.addOnScrollListener(scrollListener)
    }

    private fun invokeDB() {
        val dbHelper = context?.let { UserDatabase.DatabaseBuilder.getInstance(it) }?.let {
            DatabaseHelperImpl(
                it
            )
        }
        dbHelper?.let { getUserDetails(it) }
    }


    private fun getUserDetails(dbHelper: DatabaseHelperImpl) {
        viewModel.getUserDetails(dbHelper)
//        viewModel.fetchDataFromDb(dbHelper)

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


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == 34) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    getLocation()
                }
                else -> {
                    View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            Build.DISPLAY, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun onGPS() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun getLocation() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION
                )
            }
        } else {
            val locationGPS: Location? =
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (locationGPS != null) {
                val lat = locationGPS.latitude
                val longi = locationGPS.longitude
                getWeatherData(lat, longi)
                Toast.makeText(
                    context,
                    "Your Location: \nLatitude: $lat\nLongitude: $longi",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(context, "Unable to find location.", Toast.LENGTH_SHORT).show()
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
}