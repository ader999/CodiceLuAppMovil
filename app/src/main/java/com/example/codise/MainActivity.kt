package com.example.codise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codise.data.City
import com.example.codise.data.User
import com.example.codise.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Codise路Theme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val loginViewModel: LoginViewModel = viewModel()
    val uiState by loginViewModel.uiState.collectAsState()

    if (uiState is LoginUiState.Success) {
        val response = (uiState as LoginUiState.Success).response
        AuthenticatedApp(
            user = response.user,
            token = response.tokens.access,
            onLogout = { loginViewModel.logout() }
        )
    } else {
        LoginScreen(loginViewModel)
    }
}

@Composable
fun AuthenticatedApp(user: User, token: String, onLogout: () -> Unit) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val mainViewModel: MainViewModel = viewModel()
    val eventsViewModel: EventsViewModel = viewModel()
    val visitedPoiIds by mainViewModel.visitedPoiIds.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentScreen by remember { mutableStateOf("main") }
    val selectedCity = mainViewModel.selectedCity
    var selectedTab by remember { mutableIntStateOf(0) }

    var poiIdToMarkAsVisited by remember { mutableStateOf<Int?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            poiIdToMarkAsVisited?.let { poiId ->
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).addOnSuccessListener { location ->
                        mainViewModel.toggleVisited(poiId, location?.latitude, location?.longitude)
                    }.addOnFailureListener {
                        mainViewModel.toggleVisited(poiId)
                    }
                } catch (e: SecurityException) {
                    mainViewModel.toggleVisited(poiId)
                }
            }
        } else {
            // Permission denied, mark as visited locally without cloud sync (or at least without GPS)
            poiIdToMarkAsVisited?.let { mainViewModel.toggleVisited(it) }
        }
        poiIdToMarkAsVisited = null
    }

    val requestLocationAndMark = { poiId: Int ->
        if (visitedPoiIds.contains(poiId)) {
            // Already visited, just unmark locally
            mainViewModel.toggleVisited(poiId)
        } else {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).addOnSuccessListener { location ->
                        mainViewModel.toggleVisited(poiId, location?.latitude, location?.longitude)
                    }.addOnFailureListener {
                        mainViewModel.toggleVisited(poiId)
                    }
                } catch (e: SecurityException) {
                    mainViewModel.toggleVisited(poiId)
                }
            } else {
                poiIdToMarkAsVisited = poiId
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    // Auto-refresh when returning to foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mainViewModel.fetchCities()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = null,
                onProfileClick = { currentScreen = "profile" },
                onLogoClick = { currentScreen = "main" }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentScreen = currentScreen,
                selectedTab = selectedTab,
                onHomeClick = { currentScreen = "main" },
                onTabSelected = { 
                    selectedTab = it
                    if (it == 2) {
                        currentScreen = "events"
                    }
                },
                onBackClick = {
                    if (currentScreen == "circuit_detail") {
                        currentScreen = "circuits_and_poi"
                    }
                }
            )
        },
        containerColor = Celeste
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "main" -> MainScreen(
                    mainViewModel = mainViewModel,
                    onCityInfoClick = { city ->
                        mainViewModel.selectCity(city.id)
                        currentScreen = "city_detail"
                    },
                    onCityPinClick = { city ->
                        mainViewModel.selectCity(city.id)
                        selectedTab = 0
                        currentScreen = "circuits_and_poi"
                    },
                    onCityClick = { city ->
                        mainViewModel.selectCity(city.id)
                        currentScreen = "city_detail"
                    }
                )
                "profile" -> {
                    val businessUiState by profileViewModel.businessUiState.collectAsState()
                    val cities by mainViewModel.cities
                    ProfileContent(
                        user = user,
                        token = token,
                        onBack = { currentScreen = "main" },
                        onSave = { updatedUser: User ->
                            profileViewModel.updateProfile(token, updatedUser)
                        },
                        profileUiState = profileUiState,
                        businessUiState = businessUiState,
                        onRegisterBusiness = { t, b -> profileViewModel.registerBusiness(t, b) },
                        cities = cities,
                        onLogout = onLogout
                    )
                }
                "circuits_and_poi" -> {
                    selectedCity?.let { city ->
                        CircuitsAndPoiScreen(
                            city = city,
                            selectedTab = selectedTab,
                            onVerMasClick = { circuit ->
                                mainViewModel.selectCircuit(circuit.id)
                                currentScreen = "circuit_detail"
                            }
                        )
                    }
                }
                "circuit_detail" -> {
                    val circuit = mainViewModel.selectedCircuit
                    if (circuit != null) {
                        val visitedPois by mainViewModel.visitedPois.collectAsState()
                        CircuitDetailScreen(
                            circuit = circuit,
                            visitedPois = visitedPois,
                            onToggleVisited = { poiId -> requestLocationAndMark(poiId) }
                        )
                    } else {
                        currentScreen = "circuits_and_poi"
                    }
                }
                "city_detail" -> {
                    selectedCity?.let { city ->
                        CityDetailScreen(
                            city = city,
                            onBack = { currentScreen = "main" }
                        )
                    }
                }
                "events" -> {
                    EventsScreen(
                        viewModel = eventsViewModel,
                        canUpload = user.esProtagonista,
                        onUploadClick = { currentScreen = "upload_event" }
                    )
                }
                "upload_event" -> {
                    val cities by mainViewModel.cities
                    val isUploading by eventsViewModel.isUploading
                    val uploadSuccess by eventsViewModel.uploadSuccess
                    UploadEventScreen(
                        cities = cities,
                        onBack = { 
                            currentScreen = "events"
                            eventsViewModel.resetUploadState()
                        },
                        onUpload = { eventsViewModel.uploadEvent(it) },
                        isUploading = isUploading,
                        uploadSuccess = uploadSuccess
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onCityInfoClick: (City) -> Unit,
    onCityPinClick: (City) -> Unit,
    onCityClick: (City) -> Unit
) {
    val cities by mainViewModel.cities
    val isLoading by mainViewModel.isLoading
    val error by mainViewModel.error

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { mainViewModel.fetchCities(force = true) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainCard(
                cities = cities,
                isLoading = isLoading,
                error = error,
                onRefresh = { mainViewModel.fetchCities(force = true) },
                onInfoClick = onCityInfoClick,
                onPinClick = onCityPinClick,
                onCityClick = onCityClick
            )
        }
    }
}

@Composable
fun TopBar(title: String? = null, onProfileClick: () -> Unit, onLogoClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AzulPetroleo)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onLogoClick() }.weight(1f)
        ) {
            // Stylized Logo Triangle
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .clip(GenericShape { size, _ ->
                        moveTo(size.width * 0.2f, size.height)
                        lineTo(size.width, size.height * 0.1f)
                        lineTo(0f, size.height * 0.5f)
                        close()
                    })
                    .background(GoldColor)
            )
            Text(
                text = title ?: "Codice路",
                color = GoldColor,
                fontSize = if (title != null) 20.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            tint = GoldColor,
            modifier = Modifier
                .size(36.dp)
                .clickable { onProfileClick() }
        )
    }
}

@Composable
fun MainCard(
    cities: List<City>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onInfoClick: (City) -> Unit,
    onPinClick: (City) -> Unit,
    onCityClick: (City) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoBase),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Map Placeholder Area
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = Celeste.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize(0.8f)
                )
                Text(
                    "NICARAGUA",
                    color = AzulPetroleo.copy(alpha = 0.2f),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = GrisClaro)
            
            Box(modifier = Modifier.weight(0.65f)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldColor)
                } else if (error != null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                        Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = AzulPetroleo)) {
                            Text("Reintentar")
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(cities) { city ->
                            LocationItem(
                                name = city.nombre,
                                onInfoClick = { onInfoClick(city) },
                                onPinClick = { onPinClick(city) },
                                onCityClick = { onCityClick(city) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = GrisClaro)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationItem(name: String, onInfoClick: () -> Unit, onPinClick: () -> Unit, onCityClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable { onCityClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = GoldColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = name,
                fontSize = 22.sp,
                color = NegroPuro,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Información y Datos Históricos",
                    tint = AzulPetroleo,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = onPinClick) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Circuitos y Puntos de Interés",
                    tint = GoldColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: String,
    selectedTab: Int,
    onHomeClick: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onBackClick: () -> Unit = {}
) {
    val threeMoundsShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val waveBaseY = 30f // Starting Y for the wave
        val wavePeakY = -20f // Highest point of the wave
        
        moveTo(0f, waveBaseY)
        // Left mound
        quadraticTo(w * 0.16f, waveBaseY - 20f, w * 0.33f, waveBaseY)
        // Middle mound (higher)
        quadraticTo(w * 0.5f, wavePeakY, w * 0.67f, waveBaseY)
        // Right mound
        quadraticTo(w * 0.84f, waveBaseY - 20f, w, waveBaseY)
        
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(threeMoundsShape)
            .background(AzulPetroleo)
            .padding(bottom = 26.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentScreen == "circuits_and_poi") {
                // Circuitos Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(0) }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Menu,
                        null,
                        tint = if (selectedTab == 0) GoldColor else GoldColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        "Circuitos",
                        color = if (selectedTab == 0) GoldColor else GoldColor.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    Icons.Default.Home,
                    null,
                    tint = GoldColor,
                    modifier = Modifier
                        .size(42.dp)
                        .padding(bottom = 12.dp)
                        .clickable { onHomeClick() }
                )

                // Puntos Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(1) }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Explore,
                        null,
                        tint = if (selectedTab == 1) GoldColor else GoldColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        "Puntos",
                        color = if (selectedTab == 1) GoldColor else GoldColor.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Default Navigation
                Icon(
                    imageVector = if (currentScreen == "circuit_detail") {
                        Icons.AutoMirrored.Filled.ArrowBack
                    } else if (currentScreen == "events") {
                        Icons.Default.Event
                    } else {
                        Icons.Default.Event
                    },
                    contentDescription = if (currentScreen == "circuit_detail") "Regresar" else "Eventos",
                    tint = if (currentScreen == "events") GoldColor else GoldColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            if (currentScreen == "circuit_detail") {
                                onBackClick()
                            } else {
                                onTabSelected(2) // Events tab
                            }
                        }
                )
                Icon(
                    Icons.Default.Home,
                    null,
                    tint = GoldColor,
                    modifier = Modifier
                        .size(42.dp)
                        .padding(bottom = 12.dp)
                        .clickable { onHomeClick() }
                )
                Icon(Icons.Default.Explore, null, tint = GoldColor, modifier = Modifier.size(36.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainCardPreview() {
    Codise路Theme {
        MainCard(
            cities = listOf(
                City(1, "León", "Ciudad universitaria", null, 0.0, 0.0, emptyList(), emptyList(), emptyList()),
                City(2, "Granada", "La Gran Sultana", null, 0.0, 0.0, emptyList(), emptyList(), emptyList())
            ),
            isLoading = false,
            error = null,
            onRefresh = {},
            onInfoClick = {},
            onPinClick = {},
            onCityClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Codise路Theme {
        MainApp()
    }
}
