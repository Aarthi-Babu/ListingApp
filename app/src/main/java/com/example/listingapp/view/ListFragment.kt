package com.example.listingapp.view


import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.listingapp.R
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.User
import com.example.listingapp.database.UserDatabase
import com.example.listingapp.databinding.FragmentListBinding
import com.example.listingapp.di.AppModule
import com.example.listingapp.utils.ProgressDialog
import com.example.listingapp.utils.Resource
import com.example.listingapp.utils.isNetworkAvailable
import com.example.listingapp.utils.setStatusBarColor
import com.example.listingapp.viewmodel.ListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val viewModel: ListViewModel by activityViewModels()
    private var adapter: RecyclerViewAdapter? = null
    private var sList: ArrayList<User>? = null
    private var locationManager: LocationManager? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var progressDialogFactory: AppModule.ProgressDialogFactory
    private val progressDialog: ProgressDialog by lazy { progressDialogFactory.create(this.context) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onStart()
        activity?.setStatusBarColor(R.color.purple_500)
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!) {
            onGPS()
        } else {
            getLocation()
        }
        invokeDB()
        loadData()
        recyclerView()
        viewListener()
//        val permissionsResultCallback = registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) {
//            when (it) {
//                true -> {
//                    println("Permission has been granted by user")
//                }
//                false -> {
//                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
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
        if (isNetworkAvailable(context)) {
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
                    progressDialog.hideLoading()
                    resource?.data?.let {
                        Toast.makeText(context, it.wind?.deg.toString(), Toast.LENGTH_SHORT).show()
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

    private fun viewListener() {
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
                    Toast.makeText(context, "New load Found..", Toast.LENGTH_SHORT).show()
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
        } else {
            adapter?.filterList(filteredList)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Toast.makeText(context, "on request invoked. $requestCode", Toast.LENGTH_SHORT).show()
        if (requestCode == 1) {
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
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton(
            "Yes"
        ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "No"
            ) { dialog, _ -> dialog.cancel() }
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
                    1
                )
            }
        } else {
            val locationGPS: Location? =
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (locationGPS != null) {
                val lat = locationGPS.latitude
                val longitude = locationGPS.longitude
                getWeatherData(lat, longitude)
                Toast.makeText(
                    context,
                    "Your Location: \nLatitude: $lat\nLongitude: $longitude",
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}