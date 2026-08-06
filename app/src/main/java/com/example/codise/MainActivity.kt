package com.example.codise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codise.data.City
import com.example.codise.data.User
import com.example.codise.ui.theme.*

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

    var currentScreen by remember { mutableStateOf("main") }
    var selectedCity by remember { mutableStateOf<City?>(null) }

    Scaffold(
        topBar = {
            TopBar(
                onProfileClick = { currentScreen = "profile" },
                onLogoClick = { currentScreen = "main" }
            )
        },
        bottomBar = { BottomNavBar(onHomeClick = { currentScreen = "main" }) },
        containerColor = Celeste
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "main" -> MainScreen(
                    mainViewModel = mainViewModel,
                    onCityInfoClick = { city ->
                        selectedCity = city
                        currentScreen = "city_detail"
                    }
                )
                "profile" -> ProfileContent(
                    user = user,
                    onBack = { currentScreen = "main" },
                    onSave = { updatedUser: User ->
                        profileViewModel.updateProfile(token, updatedUser)
                    },
                    profileUiState = profileUiState,
                    onLogout = onLogout
                )
                "city_detail" -> {
                    selectedCity?.let { city ->
                        CityDetailScreen(
                            city = city
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onCityInfoClick: (City) -> Unit
) {
    val cities by mainViewModel.cities
    val isLoading by mainViewModel.isLoading
    val error by mainViewModel.error

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
            onRefresh = { mainViewModel.fetchCities() },
            onInfoClick = onCityInfoClick
        )
    }
}

@Composable
fun TopBar(onProfileClick: () -> Unit, onLogoClick: () -> Unit) {
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
            modifier = Modifier.clickable { onLogoClick() }
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
                text = "Codice路",
                color = GoldColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp)
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
    onInfoClick: (City) -> Unit
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
                                onInfoClick = { onInfoClick(city) }
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
fun LocationItem(name: String, onInfoClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
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
                    contentDescription = "Información",
                    tint = AzulPetroleo,
                    modifier = Modifier.size(28.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = GoldColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun BottomNavBar(onHomeClick: () -> Unit) {
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
                .padding(horizontal = 45.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Menu, null, tint = GoldColor, modifier = Modifier.size(36.dp))
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
            onInfoClick = {}
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
