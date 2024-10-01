package com.example.mapsappyandex

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mapsappyandex.data.model.entity.PointEntity
import com.example.mapsappyandex.data.viewmodel.PointViewModel
import com.example.mapsappyandex.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView
    private lateinit var tapListener: MapObjectTapListener
    private val pointViewModel: PointViewModel by viewModels()
    private val listVisible = mutableListOf<PointEntity>()

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
        binding.showUserLocation.setOnClickListener{
            getCurrentLocation()
        }

        mapView.mapWindow.map.addInputListener(clickProcessingOnMap)
        showAllPoints()
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

    private fun showPoint(pointEntity: PointEntity) {
        val imageProvider = ImageProvider.fromResource(this, R.drawable.point_location)
        val placemark = mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            geometry = Point(pointEntity.latitude, pointEntity.longitude)
            setIcon(imageProvider)
            setText(
                pointEntity.name,
                TextStyle().apply {
                    size = 10f
                    placement = TextStyle.Placement.RIGHT
                    offset = 5f
                },
            )
        }
        tapListener = MapObjectTapListener { _, _ ->
            showDialogPoint(pointEntity) {
                mapView.mapWindow.map.mapObjects.remove(placemark)
                deletePoint(pointEntity)
            }
            true
        }

        placemark.addTapListener(tapListener)

    }

    private val clickProcessingOnMap = object : InputListener {
        override fun onMapTap(p0: Map, p1: Point) {
            Toast.makeText(
                this@MainActivity,
                R.string.toast_create_point,
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onMapLongTap(p0: Map, p1: Point) {
            insertNewPoint(p1)
        }
    }

    private fun showDialogPointCreate(onPositive: (String) -> Unit = {}) {
        val view = layoutInflater.inflate(R.layout.dialog_menu, null)
        val editText = view.findViewById<EditText>(R.id.name_point)

       MaterialAlertDialogBuilder(this)
            .setView(view)
            .setPositiveButton(R.string.positive_button) { dialogInt, _ ->
                onPositive(editText.text.toString())
                dialogInt.dismiss()
            }.setNegativeButton(R.string.negative_button) { dialogInt, _ ->
                dialogInt.dismiss()
            }
            .show()
    }

    private fun showDialogPoint(pointEntity: PointEntity, onNegative: () -> Unit = {}) {
        val dialogWindow = MaterialAlertDialogBuilder(this)

        dialogWindow.setPositiveButton(R.string.positive_button2) { dialogInt, _ ->
            dialogInt.dismiss()
        }.setNegativeButton(R.string.negative_button2) { dialogInt, _ ->
            onNegative()
            dialogInt.dismiss()
        }.setMessage("Координаты точки: ${pointEntity.latitude} ${pointEntity.longitude}")
            .setTitle(R.string.title).create()
        dialogWindow.show()
    }

    private fun insertNewPoint(point: Point) {
        showDialogPointCreate { name ->
            pointViewModel.insertNewPoint(PointEntity(
                id = 0,
                latitude = point.latitude,
                longitude = point.longitude,
                name = name
            ))
        }
    }

    private fun deletePoint(pointEntity: PointEntity) {
        pointViewModel.deletePoint(pointEntity.id)
    }

    private fun showAllPoints() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pointViewModel.readAllPoints.collect { list ->
                    list.forEach { point ->
                        if (!listVisible.contains(point)) {
                            showPoint(point)
                            listVisible.add(point)
                        }
                    }
                }
            }
        }
    }
}