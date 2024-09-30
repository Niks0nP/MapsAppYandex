package com.example.mapsappyandex

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mapsappyandex.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.traffic.TrafficLayer
import com.yandex.runtime.image.ImageProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapKitFactory.setApiKey(BuildConfig.API_KEY)
        MapKitFactory.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        mapView = binding.mapView
        val mapKit = MapKitFactory.getInstance()
        val trafficLayer = mapKit.createTrafficLayer(mapView.mapWindow)

        binding.button.setOnClickListener{
            showTraffic(trafficLayer, binding)
        }

        binding.showUserLocation.setOnClickListener{
            getCurrentLocation()
        }

        mapView.mapWindow.map.addInputListener(clickProcessingOnMap())


    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
                val mapKit = MapKitFactory.getInstance()
                showUserOnMap(mapKit)
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            } else -> {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show()
        }
        }
    }

    private fun showUserOnMap(mapKit: MapKit) {
        val locationOnMapUser = mapKit.createUserLocationLayer(mapView.mapWindow)
        locationOnMapUser.isVisible = true
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                1)
        }
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLocation = Point(location.latitude, location.longitude)

                mapView.mapWindow.map.move(
                    CameraPosition(currentLocation, 15.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 1f), null
                )
            } else
                Toast.makeText(
                    this,
                    R.string.location_button_off,
                    Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun showTraffic(probity: TrafficLayer, binding: ActivityMainBinding) {
        probity.isTrafficVisible = !probity.isTrafficVisible

        binding.button.text = if (probity.isTrafficVisible) {
            "Скрыть пробки"
        } else {
            "Показать пробки"
        }
    }

    private fun showPoint(point: Point) {
        val imageProvider = ImageProvider.fromResource(this, R.drawable.point_location)
        val placemark = mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            geometry = Point(point.latitude, point.longitude)
            setIcon(imageProvider)
            Toast.makeText(this@MainActivity, "Точка добавлена", Toast.LENGTH_SHORT).show()
        }

        val tapListener = MapObjectTapListener { _, _ ->
            Toast.makeText(
                this@MainActivity,
                "Tapped the point (${point.longitude}, ${point.latitude})",
                Toast.LENGTH_SHORT).show()
            true
        }

        placemark.addTapListener(tapListener)
    }

    private fun clickProcessingOnMap()  = object : InputListener {
            override fun onMapTap(p0: Map, p1: Point) {
                TODO("Not yet implemented")
            }

            override fun onMapLongTap(p0: Map, p1: Point) {
                showPoint(p1)
            }

    }
}