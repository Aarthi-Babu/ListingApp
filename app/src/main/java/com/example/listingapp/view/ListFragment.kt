package com.example.listingapp.view

//import android.annotation.SuppressLint
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.location.Location
//import android.net.Uri
//import android.os.Build
//import android.provider.Settings
//import android.util.Log
//import androidx.core.app.ActivityCompat
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val REQUEST_LOCATION = 1
    var locationManager: LocationManager? = null
    var latitude: String? = null
    var longitude: String? = null

    private val lastVisibleItemPosition = 25

    //    private var fusedLocationClient: FusedLocationProviderClient? = null
//    private var lastLocation: Location? = null
//    private var latitude = 0
//    private var longitude = 0
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
                        ?.navigate(ListFragmentDirections.actionListFragmentToDetailsFragment(it))
                }
            }
            binding.recyclerView.adapter = adapter
        }

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
            OnGPS()
        } else {
            getLocation()
        }

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
//        if (!checkPermissions()) {
//            requestPermissions()
//        } else {
//            getLastLocation()
//        }
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
        dbHelper?.let { getUserDetails(it) }
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
//        getWeatherData()
    }

    private fun scrollListener() {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                var lastCompletelyVisibleItemPosition =
                    (recyclerView.layoutManager as StaggeredGridLayoutManager?)!!.findLastCompletelyVisibleItemPositions(
                        null
                    )
                lastCompletelyVisibleItemPosition.sort()
                val totalItemCount = (recyclerView.layoutManager?.itemCount)?.minus(1)
                if (totalItemCount == lastCompletelyVisibleItemPosition[lastCompletelyVisibleItemPosition.size - 1]) {
                    Toast.makeText(context, "New load Found..", Toast.LENGTH_SHORT).show()
                    val dbHelper =
                        context?.let { UserDatabase.DatabaseBuilder.getInstance(it) }?.let {
                            DatabaseHelperImpl(
                                it
                            )
                        }
                    dbHelper?.let { getUserDetails(it) }
                    binding.recyclerView.removeOnScrollListener(scrollListener)
                }
            }
        }
        binding.recyclerView.addOnScrollListener(scrollListener)
    }


    private fun getUserDetails(dbHelper: DatabaseHelperImpl) {
        viewModel.getUserDetails(dbHelper)
        viewModel.fetchDataFromDb(dbHelper)
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


    private fun OnGPS() {
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
                latitude = lat.toString()
                longitude = longi.toString()
                getWeatherData(lat.toInt(), longi.toInt())
                Toast.makeText(
                    context,
                    "Your Location: \nLatitude: $latitude\nLongitude: $longitude",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(context, "Unable to find location.", Toast.LENGTH_SHORT).show()
            }
        }
    }


//    @SuppressLint("MissingPermission")
//    private fun getLastLocation() {
//        activity?.let {
//            fusedLocationClient?.lastLocation?.addOnCompleteListener(it) { task ->
//                if (task.isSuccessful && task.result != null) {
//                    lastLocation = task.result
//                    latitude = lastLocation!!.latitude.toInt()
//                    longitude = lastLocation!!.longitude.toInt()
//                } else {
//                    showMessage("No location detected. Make sure location is enabled on the device.")
//                }
//            }
//        }
//    }
//
//    private fun showMessage(string: String) {
//        Toast.makeText(activity, string, Toast.LENGTH_LONG).show()
//
//    }
//
//    private fun showToast(
//        mainTextStringId: String, actionStringId: String,
//        listener: View.OnClickListener
//    ) {
//        Toast.makeText(activity, mainTextStringId, Toast.LENGTH_LONG).show()
//    }
//
//    private fun checkPermissions(): Boolean {
//        val permissionState = activity?.let {
//            ActivityCompat.checkSelfPermission(
//                it,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        }
//        return permissionState == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun startLocationPermissionRequest() {
//        activity?.let {
//            ActivityCompat.requestPermissions(
//                it,
//                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
//                REQUEST_PERMISSIONS_REQUEST_CODE
//            )
//        }
//    }
//
//    private fun requestPermissions() {
//        val shouldProvideRationale = activity?.let {
//            ActivityCompat.shouldShowRequestPermissionRationale(
//                it,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        }
//        if (shouldProvideRationale == true) {
//            Log.i(TAG, "Displaying permission rationale to provide additional context.")
//            showToast("Location permission is needed for core functionality", "Okay",
//                View.OnClickListener {
//                    startLocationPermissionRequest()
//                })
//        } else {
//            Log.i(TAG, "Requesting permission")
//            startLocationPermissionRequest()
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        Log.i(TAG, "onRequestPermissionResult")
//        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
//            when {
//                grantResults.isEmpty() -> {
//                    // If user interaction was interrupted, the permission request is cancelled and you
//                    // receive empty arrays.
//                    Log.i(TAG, "User interaction was cancelled.")
//                }
//                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
//                    // Permission granted.
//                    getLastLocation()
//                }
//                else -> {
//                    showMessage("denied")
//                    showToast("Permission was denied", "Settings",
//                        View.OnClickListener {
//                            // Build intent that displays the App settings screen.
//                            val intent = Intent()
//                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                            val uri = Uri.fromParts(
//                                "package",
//                                Build.DISPLAY, null
//                            )
//                            intent.data = uri
//                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            startActivity(intent)
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//    companion object {
//        private val TAG = "LocationProvider"
//        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
//    }


    private fun getWeatherData(latitude: Int, longitude: Int) {
        viewModel.getWeatherDetails(latitude, longitude)
        viewModel.weatherModel.observe(viewLifecycleOwner, {
            Toast.makeText(context, it.wind?.deg.toString(), Toast.LENGTH_SHORT).show()
            binding.textView.text =
                (it.wind?.deg.toString() + it.name + it.weather?.firstOrNull()?.description)
//            it.name?.let { it1 -> binding.toolbar.setCity(it1) }
//            it.weather?.firstOrNull()?.description?.let { it1 -> binding.toolbar.setArea(it1) }
        })
    }
}