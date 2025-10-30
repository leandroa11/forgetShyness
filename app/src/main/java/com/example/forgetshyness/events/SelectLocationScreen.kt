package com.example.forgetshyness.events

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.forgetshyness.R
import com.example.forgetshyness.data.EventSessionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun SelectLocationScreen(
    onLocationSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    previousAddress: String? = null
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    // ✅ Evitar múltiples inicializaciones de Places
    val placesClient: PlacesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, context.getString(R.string.google_maps_key))
        }
        Places.createClient(context)
    }

    // Estados
    var hasPermission by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var searchQuery by remember { mutableStateOf(previousAddress ?: "") }
    var predictions by remember { mutableStateOf(listOf<AutocompletePrediction>()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.60971, -74.08175), 14f)
    }

    // ✅ Si el usuario tenía una dirección previa, mostrarla al volver
    LaunchedEffect(previousAddress) {
        if (!previousAddress.isNullOrBlank()) searchQuery = previousAddress
    }

    // ✅ Pedir permiso de ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            scope.launch {
                try {
                    val location = fusedLocationClient.lastLocation.await()
                    location?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        selectedLocation = userLatLng
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(userLatLng, 15f)
                        if (searchQuery.isBlank()) {
                            searchQuery = getAddressFromLatLng(context, userLatLng)
                        }
                    }
                } catch (_: Exception) { }
            }
        }
    }

    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.select_location_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_description_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFC44545)
                )
            )
        }
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding)) {

            // 🗺️ Mapa
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    scope.launch {
                        searchQuery = getAddressFromLatLng(context, latLng)
                    }
                },
                properties = MapProperties(isMyLocationEnabled = hasPermission)
            ) {
                selectedLocation?.let {
                    Marker(state = MarkerState(position = it), title = stringResource(R.string.selected_location))
                }
            }

            // 🔍 Buscador
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .padding(12.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        if (query.isNotBlank()) {
                            scope.launch {
                                val request = FindAutocompletePredictionsRequest.builder()
                                    .setQuery(query)
                                    .build()
                                val response = placesClient.findAutocompletePredictions(request).await()
                                predictions = response.autocompletePredictions
                            }
                        } else predictions = emptyList()
                    },
                    placeholder = { Text(stringResource(R.string.search_location)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.DarkGray,
                        cursorColor = Color(0xFFC44545)
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                if (predictions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .heightIn(max = 200.dp)
                    ) {
                        items(predictions) { prediction ->
                            Text(
                                text = prediction.getFullText(null).toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            val placeRequest = FetchPlaceRequest.newInstance(
                                                prediction.placeId,
                                                listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
                                            )
                                            val place =
                                                placesClient.fetchPlace(placeRequest).await().place
                                            val latLng = place.latLng
                                            if (latLng != null) {
                                                selectedLocation = latLng
                                                cameraPositionState.position =
                                                    CameraPosition.fromLatLngZoom(latLng, 15f)
                                                searchQuery = place.address ?: ""
                                                predictions = emptyList()
                                            }
                                        }
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            // button ok
            Button(
                onClick = {
                    selectedLocation?.let { latLng ->
                        val address = if (searchQuery.isNotBlank()) searchQuery
                        else getAddressFromLatLng(context, latLng)
                        EventSessionManager.eventLocation = address
                        EventSessionManager.latitude = latLng.latitude
                        EventSessionManager.longitude = latLng.longitude
                        onLocationSelected(address)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C))
            ) {
                Text(stringResource(R.string.Button_ok), color = Color.Black)
            }
        }
    }
}

/**
 * 🧭 Convierte coordenadas a una dirección legible
 */
private fun getAddressFromLatLng(context: android.content.Context, latLng: LatLng): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        address?.firstOrNull()?.getAddressLine(0) ?: context.getString(R.string.no_address_found)
    } catch (e: Exception) {
        context.getString(R.string.no_address_found)
    }
}





