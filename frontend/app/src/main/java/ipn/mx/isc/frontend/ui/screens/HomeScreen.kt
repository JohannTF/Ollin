package ipn.mx.isc.frontend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ipn.mx.isc.frontend.navigation.NavItem
import ipn.mx.isc.frontend.ui.components.OllinBottomNavigation

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    var currentRoute by remember { mutableStateOf(NavItem.Map.route) }
    
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
            when (currentRoute) {
                NavItem.Map.route -> MapScreen()
                NavItem.Alerts.route -> AlertsScreen()
                NavItem.Reports.route -> ReportsScreen()
                NavItem.Settings.route -> SettingsScreen()
            }
        }
    }
}