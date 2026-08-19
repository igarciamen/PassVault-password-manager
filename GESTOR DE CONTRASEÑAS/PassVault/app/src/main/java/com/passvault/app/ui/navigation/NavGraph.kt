package com.passvault.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.passvault.app.security.MasterPasswordManager
import com.passvault.app.ui.AppLockViewModel
import com.passvault.app.ui.CreateMasterPasswordScreen
import com.passvault.app.ui.ExportScreen
import com.passvault.app.ui.ImportScreen
import com.passvault.app.ui.LoginScreen
import com.passvault.app.ui.PasswordDetailScreen
import com.passvault.app.ui.PasswordEditScreen
import com.passvault.app.ui.PasswordListScreen
import com.passvault.app.ui.RecoverAccessScreen
import com.passvault.app.ui.ResetMasterPasswordScreen
import com.passvault.app.ui.SecurityAuditScreen
import com.passvault.app.ui.SettingsScreen
import com.passvault.app.ui.ShowRecoveryCodeScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthGateViewModel @Inject constructor(
    masterPasswordManager: MasterPasswordManager
) : ViewModel() {
    val startDestination: String = if (masterPasswordManager.isMasterPasswordSet()) {
        PassVaultDestinations.LOGIN_ROUTE
    } else {
        PassVaultDestinations.CREATE_MASTER_PASSWORD_ROUTE
    }
}

private val PROTECTED_ROUTES = setOf(
    PassVaultDestinations.LIST_ROUTE,
    PassVaultDestinations.SETTINGS_ROUTE,
    PassVaultDestinations.DETAIL_ROUTE,
    PassVaultDestinations.EDIT_ROUTE,
    PassVaultDestinations.SECURITY_AUDIT_ROUTE,
    PassVaultDestinations.EXPORT_ROUTE,
    PassVaultDestinations.IMPORT_ROUTE
)

@Composable
fun PassVaultNavGraph(navController: NavHostController = rememberNavController()) {
    val authGateViewModel: AuthGateViewModel = hiltViewModel()

    val appLockViewModel: AppLockViewModel = hiltViewModel()
    val isUnlocked by appLockViewModel.isUnlocked.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(isUnlocked, currentBackStackEntry) {
        val currentRoute = currentBackStackEntry?.destination?.route
        val isOnProtectedRoute = currentRoute != null && currentRoute in PROTECTED_ROUTES

        android.util.Log.d("PassVaultTest", "NavGraph check: isUnlocked=$isUnlocked route=$currentRoute") // AÑADIDO

        if (!isUnlocked && isOnProtectedRoute) {
            android.util.Log.d("PassVaultTest", "Redirigiendo a login!") // AÑADIDO

            navController.navigate(PassVaultDestinations.LOGIN_ROUTE) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = authGateViewModel.startDestination) {

        composable(PassVaultDestinations.CREATE_MASTER_PASSWORD_ROUTE) {
            CreateMasterPasswordScreen(
                onCreated = { navController.navigate(PassVaultDestinations.SHOW_RECOVERY_CODE_ROUTE) }
            )
        }

        composable(PassVaultDestinations.SHOW_RECOVERY_CODE_ROUTE) {
            ShowRecoveryCodeScreen(
                onDone = {
                    navController.navigate(PassVaultDestinations.LIST_ROUTE) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
            )
        }

        composable(PassVaultDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onSuccess = {
                    navController.navigate(PassVaultDestinations.LIST_ROUTE) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                },
                onRecoverAccess = { navController.navigate(PassVaultDestinations.RECOVER_ACCESS_ROUTE) }
            )
        }

        composable(PassVaultDestinations.RECOVER_ACCESS_ROUTE) {
            RecoverAccessScreen(
                onRecovered = { navController.navigate(PassVaultDestinations.RESET_MASTER_PASSWORD_ROUTE) }
            )
        }

        composable(PassVaultDestinations.RESET_MASTER_PASSWORD_ROUTE) {
            ResetMasterPasswordScreen(
                onDone = {
                    navController.navigate(PassVaultDestinations.LIST_ROUTE) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
            )
        }

        composable(PassVaultDestinations.LIST_ROUTE) {
            PasswordListScreen(
                onEntryClick = { id -> navController.navigate(PassVaultDestinations.detail(id)) },
                onAddClick = { navController.navigate(PassVaultDestinations.editNew()) },
                onSettingsClick = { navController.navigate(PassVaultDestinations.SETTINGS_ROUTE) }
            )
        }

        composable(
            route = PassVaultDestinations.DETAIL_ROUTE,
            arguments = listOf(navArgument(PassVaultDestinations.ARG_ENTRY_ID) { type = NavType.LongType })
        ) {
            PasswordDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(PassVaultDestinations.editExisting(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(
            route = PassVaultDestinations.EDIT_ROUTE,
            arguments = listOf(
                navArgument(PassVaultDestinations.ARG_ENTRY_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            PasswordEditScreen(
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(PassVaultDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenSecurityAudit = { navController.navigate(PassVaultDestinations.SECURITY_AUDIT_ROUTE) },
                onOpenExport = { navController.navigate(PassVaultDestinations.EXPORT_ROUTE) },
                onOpenImport = { navController.navigate(PassVaultDestinations.IMPORT_ROUTE) }
            )
        }

        composable(PassVaultDestinations.SECURITY_AUDIT_ROUTE) {
            SecurityAuditScreen(onBack = { navController.popBackStack() })
        }

        composable(PassVaultDestinations.EXPORT_ROUTE) {
            ExportScreen(onBack = { navController.popBackStack() })
        }

        composable(PassVaultDestinations.IMPORT_ROUTE) {
            ImportScreen(onBack = { navController.popBackStack() })
        }
    }
}