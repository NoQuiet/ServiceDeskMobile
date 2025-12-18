package com.example.servicedeskmobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.servicedeskmobile.ui.screen.*
import com.example.servicedeskmobile.ui.viewmodel.AuthViewModel
import com.example.servicedeskmobile.ui.viewmodel.CommentViewModel
import com.example.servicedeskmobile.ui.viewmodel.TicketViewModel
import com.example.servicedeskmobile.ui.viewmodel.UserViewModel
import com.example.servicedeskmobile.ui.viewmodel.ThemeViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")
    object TicketList : Screen("ticket_list")
    object CreateTicket : Screen("create_ticket")
    object TicketDetail : Screen("ticket_detail/{ticketId}") {
        fun createRoute(ticketId: Int) = "ticket_detail/$ticketId"
    }
    object Profile : Screen("profile")
    object Archive : Screen("archive")
    object Admin : Screen("admin")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    ticketViewModel: TicketViewModel,
    commentViewModel: CommentViewModel,
    userViewModel: UserViewModel,
    themeViewModel: ThemeViewModel,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val userRole by authViewModel.userRole.collectAsState(initial = null)
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.TicketList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onGuestLogin = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.TicketList.route) {
            TicketListScreen(
                ticketViewModel = ticketViewModel,
                userRole = userRole,
                onTicketClick = { ticketId ->
                    navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                },
                onCreateTicket = {
                    navController.navigate(Screen.CreateTicket.route)
                }
            )
        }
        
        composable(Screen.CreateTicket.route) {
            CreateTicketScreen(
                ticketViewModel = ticketViewModel,
                onTicketCreated = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.TicketDetail.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: 0
            val currentUser by authViewModel.currentUser.collectAsState()
            TicketDetailScreen(
                ticketId = ticketId,
                ticketViewModel = ticketViewModel,
                commentViewModel = commentViewModel,
                userRole = userRole,
                userId = currentUser?.id,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                userViewModel = userViewModel,
                themeViewModel = themeViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Archive.route) {
            ArchiveScreen(
                ticketViewModel = ticketViewModel,
                onTicketClick = { ticketId ->
                    navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Admin.route) {
            AdminScreen(
                userViewModel = userViewModel,
                ticketViewModel = ticketViewModel
            )
        }
    }
}
