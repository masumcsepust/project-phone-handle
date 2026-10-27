package com.nurani.qaida.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.nurani.qaida.util.toBengaliDigits
import com.nurani.qaida.ui.theme.PrimaryDark
import com.nurani.qaida.ui.theme.PrimaryLight
import com.nurani.qaida.ui.theme.SecondaryDark
import com.nurani.qaida.ui.theme.SurfaceWhite
import com.nurani.qaida.ui.theme.TextSecondary

object Routes {
    const val HOME = "home"
    const val LETTERS = "letters"
    const val AI_ASSIST = "ai_assist"
    const val HUJUR = "hujur"
    const val PROFILE = "profile"
    const val QUIZ = "quiz"
}

enum class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "হোম", Icons.Filled.Mosque, Icons.Outlined.Mosque),
    LETTERS(Routes.LETTERS, "হরফ", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    AI(Routes.AI_ASSIST, "এআই অ্যাসিস্ট", Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
    HUJUR(Routes.HUJUR, "হুজুর যাচাই", Icons.Filled.Verified, Icons.Outlined.Verified),
    PROFILE(Routes.PROFILE, "প্রোফাইল", Icons.Filled.Person, Icons.Outlined.Person),
}

/** Switches tabs the Material way: single top, saved/restored state, shallow back stack. */
fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination?.isTabSelected(tab: BottomTab): Boolean {
    val routes = this?.hierarchy?.mapNotNull { it.route }?.toList().orEmpty()
    // The quiz is launched from Home, so keep Home highlighted while it is open.
    return tab.route in routes || (tab == BottomTab.HOME && Routes.QUIZ in routes)
}

@Composable
fun QaidaBottomBar(
    currentDestination: NavDestination?,
    hujurBadgeCount: Int,
    onTabSelected: (BottomTab) -> Unit,
) {
    NavigationBar(containerColor = SurfaceWhite, tonalElevation = 0.dp) {
        BottomTab.entries.forEach { tab ->
            val selected = currentDestination.isTabSelected(tab)
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    val icon = if (selected) tab.selectedIcon else tab.icon
                    if (tab == BottomTab.HUJUR && hujurBadgeCount > 0) {
                        BadgedBox(badge = {
                            Badge(containerColor = SecondaryDark) { Text(hujurBadgeCount.toBengaliDigits()) }
                        }) { Icon(icon, contentDescription = null) }
                    } else {
                        Icon(icon, contentDescription = null)
                    }
                },
                label = {
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryDark,
                    selectedTextColor = PrimaryDark,
                    indicatorColor = PrimaryLight.copy(alpha = 0.35f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                ),
            )
        }
    }
}
