package com.example.mapsappyandex

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mapsappyandex.databinding.ActivityMainBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.traffic.TrafficLayer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey(BuildConfig.API_KEY)
        MapKitFactory.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        mapView = binding.mapView
        val mapKit = MapKitFactory.getInstance()
        defaultLocation(mapView, mapKit)

        locationPermission()
        val trafficLayer = mapKit.createTrafficLayer(mapView.mapWindow)

        binding.button.setOnClickListener{
            showTraffic(trafficLayer, binding)
        }
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

    private fun defaultLocation(mapView: MapView, mapKit: MapKit) {
        val locationOnMapUser = mapKit.createUserLocationLayer(mapView.mapWindow)
        locationOnMapUser.isVisible = true

        mapView.mapWindow.map.move(
            CameraPosition(Point(55.753995, 37.620470), 12.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f), null
        )

    }

    private fun showTraffic(probity: TrafficLayer, binding: ActivityMainBinding) {
        probity.isTrafficVisible = !probity.isTrafficVisible

        binding.button.text = if (probity.isTrafficVisible) {
            "Скрыть пробки"
        } else {
            "Показать пробки"
        }
    }

    private fun locationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                0)
            return
        }
    }
}