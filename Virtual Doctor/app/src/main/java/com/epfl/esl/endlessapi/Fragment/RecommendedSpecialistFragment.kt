package com.epfl.esl.endlessapi.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.epfl.esl.endlessapi.Adapter.SpecialistItemAdapter
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.SpecialistProvider.AccessMedicalCenterList
import com.epfl.esl.endlessapi.SpecialistProvider.SpecailistDetailsItem
import com.epfl.esl.endlessapi.databinding.FragmentRecommendedSpecialistBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.IOException
import java.util.*
import java.util.jar.Attributes

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RecommendedSpecialistFragment : Fragment(), OnMapReadyCallback, SpecialistItemAdapter.OnItemClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentRecommendedSpecialistBinding
    var SpecialistInfomation: Map<String, SpecailistDetailsItem> = mutableMapOf()
    val LOCATION_REQUEST_CODE = 1
    val SpecialistName: MutableList<String> = mutableListOf()
    val SpecialistAdress: MutableList<String> = mutableListOf()
    val SpecialistURL: MutableList<String> = mutableListOf()
    val SpecialistImage: MutableList<Int> = mutableListOf()
    var mapMarker: MutableMap<String, Marker> = mutableMapOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recommended_specialist,
            container, false)

        SpecialistInfomation = getSpecialistInfo()
        SpecialistInfomation.forEach{ (name,details)->
            SpecialistName.add(name)
            SpecialistAdress.add(details.address)
            SpecialistURL.add(details.url)
            val identifier = resources.getIdentifier(details.imageName, "drawable", requireActivity().packageName)
            SpecialistImage.add(identifier)
        }

        binding.specialistList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val SpecialistRecycleViewAdapter = context?.let { SpecialistItemAdapter(
            context = it,
            itemsName = SpecialistName,
            itemsAddress = SpecialistAdress,
            itemsURL = SpecialistURL,
            itemsImage = SpecialistImage,
        this) }
        binding.specialistList.adapter = SpecialistRecycleViewAdapter


        // Google map manager
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Button
        // Back to Summary
        binding.returnButton.setOnClickListener { view:View ->
            val directions = RecommendedSpecialistFragmentDirections.actionSpecialistRecommendFragmentToPatientdocumentationfragment()
            view.findNavController().navigate(directions)
        }

        // Inflate the layout for this fragment
        return binding.root
    }





    // Google map -> create map object and map setting
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val permission: Boolean = ActivityCompat.checkSelfPermission(
            this.requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this.requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permission) {
            mMap.isMyLocationEnabled = true
            getLastLocation()
        } else {
            ActivityCompat.requestPermissions(
                this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE
            ) }
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.uiSettings.isZoomControlsEnabled = true

        SpecialistInfomation.forEach{ (name,details)->
             mapMarker[name] = mMap.addMarker(MarkerOptions()
                    .position(LatLng(details.latitude.toDouble(), details.longitude.toDouble()))
                    .title(name)
                    .snippet(details.address)
             )
        }


    }

    //Asking for permission for position
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this.requireActivity(),
                        "Unable to show location - permission required", Toast.LENGTH_LONG).show()
                }
                else {
                    val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync (this)
                }
            }
        }
    }

    // Asking for the current location
    @SuppressLint("MissingPermission", "VisibleForTests")
    private fun getLastLocation() {

        val fusedLocationProviderClient = FusedLocationProviderClient( this.requireActivity())

        fusedLocationProviderClient.lastLocation.addOnCompleteListener(this.requireActivity()) { task ->
            if (task.isSuccessful && task.result != null)
            {
                // Initializing the position
                val mLastLocation = task.result
                var address = "No known address"
                val gcd = Geocoder(this.requireActivity(), Locale.getDefault())
                val addresses: List<Address>
                try
                {
                    addresses = gcd.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)
                    if (addresses.isNotEmpty())
                    {
                        address = addresses[0].getAddressLine(0)
                    }
                } catch (e: IOException)
                {
                    e.printStackTrace()
                }
                // Call the drawable icon for pinning
                val icon = BitmapDescriptorFactory.fromBitmap( BitmapFactory.decodeResource(this.resources,
                    R.drawable.ic_pickup)
                )
                // Map position setting
                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                        .title("Current Location")
                        .snippet(address)
                        .icon(icon)
                )
                // camera moving setting
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                    .zoom(13f)
                    .build()
                // Apply the camera position
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
            else {
                Toast.makeText( this.requireActivity(),
                    "No current location found", Toast.LENGTH_LONG
                ).show()
            }
        }
        return
    }

    fun getSpecialistInfo(): MutableMap<String, SpecailistDetailsItem> {

        val SpecialistInfoProvider = AccessMedicalCenterList()
        val SpecialistInfo: MutableMap<String, SpecailistDetailsItem> = SpecialistInfoProvider.getSpecialistDetails(context)

        return SpecialistInfo
    }

    override fun onItemClick(position: Int) {
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(SpecialistInfomation[SpecialistName[position]]!!.latitude.toDouble(),
                SpecialistInfomation[SpecialistName[position]]!!.longitude.toDouble()))
            .zoom(15f)
            .build()

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        mapMarker[SpecialistName[position]]!!.showInfoWindow()
    }


}