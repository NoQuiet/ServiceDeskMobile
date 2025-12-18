package com.example.servicedeskmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.servicedeskmobile.navigation.NavGraph
import com.example.servicedeskmobile.navigation.Screen
import com.example.servicedeskmobile.ui.theme.ServiceDeskMobileTheme
import com.example.servicedeskmobile.ui.viewmodel.AuthViewModel
import com.example.servicedeskmobile.ui.viewmodel.CommentViewModel
import com.example.servicedeskmobile.ui.viewmodel.TicketViewModel
import com.example.servicedeskmobile.ui.viewmodel.UserViewModel
import com.example.servicedeskmobile.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var ticketViewModel: TicketViewModel
    private lateinit var commentViewModel: CommentViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var themeViewModel: ThemeViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        authViewModel = AuthViewModel(applicationContext)
        ticketViewModel = TicketViewModel()
        commentViewModel = CommentViewModel()
        userViewModel = UserViewModel()
        themeViewModel = ThemeViewModel(applicationContext)
        
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            
            ServiceDeskMobileTheme(darkTheme = isDarkTheme) {
                ServiceDeskApp(
                    authViewModel = authViewModel,
                    ticketViewModel = ticketViewModel,
                    commentViewModel = commentViewModel,
                    userViewModel = userViewModel,
                    themeViewModel = themeViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDeskApp(
    authViewModel: AuthViewModel,
    ticketViewModel: TicketViewModel,
    commentViewModel: CommentViewModel,
    userViewModel: UserViewModel,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userRole by authViewModel.userRole.collectAsState(initial = null)
    
    val startDestination = if (userRole != null) Screen.TicketList.route else Screen.Login.route
    
    val showBottomBar = currentRoute in listOf(
        Screen.TicketList.route,
        Screen.Archive.route,
        Screen.Profile.route,
        Screen.Admin.route
    )
    
    Scaffold(
        bottomBar = {
            if (showBottomBar && userRole != null) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Заявки") },
                        label = { Text("Заявки") },
                        selected = currentRoute == Screen.TicketList.route,
                        onClick = {
                            navController.navigate(Screen.TicketList.route) {
                                popUpTo(Screen.TicketList.route) { inclusive = true }
                            }
                        }
                    )
                    
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Archive, contentDescription = "Архив") },
                        label = { Text("Архив") },
                        selected = currentRoute == Screen.Archive.route,
                        onClick = {
                            navController.navigate(Screen.Archive.route)
                        }
                    )
                    
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                        label = { Text("Профиль") },
                        selected = currentRoute == Screen.Profile.route,
                        onClick = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                    
                    if (userRole == "admin") {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Админ") },
                            label = { Text("Админ") },
                            selected = currentRoute == Screen.Admin.route,
                            onClick = {
                                navController.navigate(Screen.Admin.route)
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavGraph(
            navController = navController,
            authViewModel = authViewModel,
            ticketViewModel = ticketViewModel,
            commentViewModel = commentViewModel,
            userViewModel = userViewModel,
            themeViewModel = themeViewModel,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        )
    }
}