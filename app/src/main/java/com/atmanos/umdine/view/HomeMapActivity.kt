package com.atmanos.umdine.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.atmanos.umdine.R
import com.atmanos.umdine.model.DiningHall
import com.atmanos.umdine.model.DiningHallId
import com.atmanos.umdine.model.Model
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

/**
 * Aiden Manos
 * Home page with map and banner ad
 */
class HomeMapActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var model: Model
    private var map: GoogleMap? = null
    private var halls: List<DiningHall> = emptyList()
    private lateinit var adView: AdView
    private lateinit var flpc: FusedLocationProviderClient
    private var userLocation: Location? = null
    private val markers = mutableMapOf<String, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_map)

        model = Model(this)
        flpc = LocationServices.getFusedLocationProviderClient(this)

        setupAd()
        setupButtons()
        loadDiningHalls()
        setupMap()
        requestLocation()
    }

    private fun setupAd() {
        adView = AdView(this)
        val adSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT)
        adView.setAdSize(adSize)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        val builder = AdRequest.Builder()
        builder.addKeyword("food")
        builder.addKeyword("maryland")
        val request = builder.build()
        val adLayout = findViewById<LinearLayout>(R.id.ad_view)
        adLayout.addView(adView)
        adView.loadAd(request)
    }

    private fun loadDiningHalls() {
        model.getDiningHalls { fetchedHalls ->
            halls = fetchedHalls
            updateButtonUI()
            updateMap()
        }
    }

    private fun setupMap() {
        val fragment = supportFragmentManager.findFragmentById(R.id.map)
        (fragment as SupportMapFragment).getMapAsync(this)
    }

    private fun requestLocation() {
        val launcher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) requestLocation()
            }
        val fine = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fine) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        map?.isMyLocationEnabled = true
        flpc.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = location
                updateButtonUI()
                showClosestHallInfo()
            }
        }
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnSouthCampus).setOnClickListener {
            hall = DiningHallId.SOUTH_CAMPUS
            startActivity(Intent(this, DiningHallActivity::class.java))
        }
        findViewById<Button>(R.id.btnYahentamitsi).setOnClickListener {
            hall = DiningHallId.YAHENTAMITSI
            startActivity(Intent(this, DiningHallActivity::class.java))
        }
        findViewById<Button>(R.id.btn251North).setOnClickListener {
            hall = DiningHallId.NORTH_251
            startActivity(Intent(this, DiningHallActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnPreferences).setOnClickListener {
            startActivity(Intent(this, PreferencesActivity::class.java))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val currentMap = map ?: return
        currentMap.setMapStyle(
            com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
        )
        currentMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
        currentMap.setInfoWindowAdapter(createInfoWindowAdapter())

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            currentMap.isMyLocationEnabled = true
        }

        updateMap()
    }

    private fun createInfoWindowAdapter(): GoogleMap.InfoWindowAdapter {
        return object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? = null

            override fun getInfoContents(marker: Marker): View {
                val hallItem = halls.find { it.name == marker.title } ?: return View(this@HomeMapActivity)
                val view = layoutInflater.inflate(R.layout.map_info_window, null)
                view.findViewById<TextView>(R.id.infoTitle).text = marker.title

                view.findViewById<RatingBar>(R.id.infoRating).rating = averageRatingFor(hallItem)
                return view
            }
        }
    }

    private fun averageRatingFor(hall: DiningHall): Float {
        val menuItems = hall.menu.values
        val totalWeighted = menuItems.sumOf { it.getAvgRating().toDouble() * it.numRatings }
        val totalRatings = menuItems.sumOf { it.numRatings }
        return if (totalRatings == 0) 0f else (totalWeighted / totalRatings).toFloat()
    }

    private fun hallColors(): Map<String, Int> {
        val rankedHalls = halls.sortedByDescending { it.totalReviews() }
        val colors = listOf(
            Color.rgb(255, 187, 187),
            Color.rgb(255, 255, 224),
            Color.rgb(152, 251, 152)
        )

        return rankedHalls.mapIndexed { index, hallItem ->
            hallItem.id to colors.getOrElse(index) { colors.last() }
        }.toMap()
    }

    private fun updateButtonUI() {
        val hallColors = hallColors()

        halls.forEach { hallItem ->
            val buttonId = when (hallItem.id) {
                DiningHallId.YAHENTAMITSI.id -> R.id.btnYahentamitsi
                DiningHallId.SOUTH_CAMPUS.id -> R.id.btnSouthCampus
                DiningHallId.NORTH_251.id -> R.id.btn251North
                else -> null
            }
            buttonId?.let { id ->
                val button = findViewById<Button>(id)
                val color = hallColors[hallItem.id] ?: return@let
                button.backgroundTintList = ColorStateList.valueOf(color)
                val distance = userLocation?.let { loc ->
                    val hallLoc = Location("").apply {
                        latitude = hallItem.latitude
                        longitude = hallItem.longitude
                    }
                    val meters = loc.distanceTo(hallLoc)
                    val miles = meters / 1609.3
                    String.format(Locale.getDefault(), "%.1fmi away", miles)
                }
                val displayName = DiningHallId.fromId(hallItem.id)?.displayName ?: hallItem.name
                button.text = if (distance != null) "$displayName ($distance)" else displayName
            }
        }
    }

    private fun updateMap() {
        val currentMap = map ?: return
        if (halls.isEmpty()) return

        currentMap.clear()
        markers.clear()

        val boundsBuilder = LatLngBounds.Builder()
        var hasPoints = false
        val hallColors = hallColors()

        halls.forEach { hallItem ->
            val color = hallColors[hallItem.id] ?: Color.rgb(152, 251, 152)
            addHallMarker(currentMap, hallItem, color, boundsBuilder)
            hasPoints = true
        }

        if (hasPoints) {
            val bounds = boundsBuilder.build()
            currentMap.setOnMapLoadedCallback {
                val padding = 260
                currentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                showClosestHallInfo()
            }
        }
    }

    private fun addHallMarker(
        currentMap: GoogleMap,
        hallItem: DiningHall,
        color: Int,
        boundsBuilder: LatLngBounds.Builder
    ) {
        val loc = LatLng(hallItem.latitude, hallItem.longitude)
        currentMap.addCircle(
            CircleOptions()
                .center(loc)
                .radius(25.0)
                .fillColor(color)
                .strokeColor(Color.BLACK)
                .strokeWidth(3f)
        )
        val marker = currentMap.addMarker(
            MarkerOptions()
                .position(loc)
                .title(hallItem.name)
                .alpha(0f)
        )
        if (marker != null) {
            markers[hallItem.id] = marker
        }
        boundsBuilder.include(loc)
    }
    private fun showClosestHallInfo() {
        val loc = userLocation ?: return
        val curMap = map ?: return
        if (halls.isEmpty() || markers.isEmpty()) return
        val closestHall = halls.minByOrNull { hallItem ->
            val hallLoc = Location("").apply {
                latitude = hallItem.latitude
                longitude = hallItem.longitude
            }
            loc.distanceTo(hallLoc)
        }
        closestHall?.let {
            markers[it.id]?.showInfoWindow()
        }
    }

    companion object {
        var hall: DiningHallId = DiningHallId.SOUTH_CAMPUS
    }
}
