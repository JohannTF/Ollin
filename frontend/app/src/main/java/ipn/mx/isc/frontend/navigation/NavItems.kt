package ipn.mx.isc.frontend.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Map : NavItem("map", Icons.Outlined.Map, "Mapa")
    data object Reports : NavItem("reports", Icons.Outlined.Analytics, "Reportes")
    data object Settings : NavItem("settings", Icons.Outlined.Settings, "Ajustes")
}