package ipn.mx.isc.frontend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ipn.mx.isc.frontend.navigation.NavItem
import ipn.mx.isc.frontend.ui.components.BottomSheetCard
import ipn.mx.isc.frontend.ui.components.OllinBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    var currentRoute by remember { mutableStateOf(NavItem.Map.route) }
    val sheetState = rememberModalBottomSheetState()
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            OllinBottomNavigation(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    currentRoute = route
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Aquí irá el contenido principal (mapa, etc.)
            
            BottomSheetCard(
                onDetailsClick = {
                    // Implementar navegación a detalles
                }
            )
        }
    }
}