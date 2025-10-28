package ipn.mx.isc.frontend.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Map : NavItem("map", Icons.Outlined.Map, "Mapa")
    data object Alerts : NavItem("alerts", Icons.Outlined.Notifications, "Alertas")
    data object Reports : NavItem("reports", Icons.Outlined.Analytics, "Reportes")
    data object Settings : NavItem("settings", Icons.Outlined.Settings, "Ajustes")

    companion object {
        fun fromRoute(route: String?): NavItem {
            return when (route) {
                Map.route -> Map
                Alerts.route -> Alerts
                Reports.route -> Reports
                Settings.route -> Settings
                else -> Map
            }
        }
    }
}