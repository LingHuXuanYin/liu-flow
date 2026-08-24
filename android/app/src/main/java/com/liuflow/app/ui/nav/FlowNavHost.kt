package com.liuflow.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liuflow.app.AppContainer
import com.liuflow.app.ui.components.BottomNavBar
import com.liuflow.app.ui.focus.FocusScreen
import com.liuflow.app.ui.focus.FocusViewModel
import com.liuflow.app.ui.heatmap.HeatmapScreen
import com.liuflow.app.ui.heatmap.HeatmapViewModel
import com.liuflow.app.ui.history.HistoryScreen
import com.liuflow.app.ui.history.HistoryViewModel
import com.liuflow.app.ui.me.MeScreen
import com.liuflow.app.ui.me.MeViewModel
import com.liuflow.app.ui.rest.RestScreen
import com.liuflow.app.ui.running.RunningScreen
import com.liuflow.app.ui.running.RunningViewModel
import com.liuflow.app.ui.settings.SettingsScreen
import com.liuflow.app.ui.settings.SettingsViewModel
import com.liuflow.app.ui.stats.StatsScreen
import com.liuflow.app.ui.stats.StatsViewModel
import com.liuflow.app.ui.weekly.WeeklyScreen
import com.liuflow.app.ui.weekly.WeeklyViewModel
import com.liuflow.app.ui.flowViewModelFactory

@Composable
fun FlowNavHost(container: AppContainer) {
    val nav: NavHostController = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Routes.Focus
    val factory = remember { flowViewModelFactory(container) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = Routes.Focus,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.Focus) {
                val vm: FocusViewModel = viewModel(factory = factory)
                FocusScreen(
                    viewModel = vm,
                    onStart = { nav.navigate(Routes.Running) },
                )
            }
            composable(Routes.Running) {
                val vm: RunningViewModel = viewModel(factory = factory)
                RunningScreen(
                    viewModel = vm,
                    onCompleted = {
                        vm.startRestThen { nav.navigate(Routes.Rest) }
                    },
                    onAbandoned = {
                        nav.popBackStack(Routes.Focus, inclusive = false)
                    },
                    onRest = { nav.navigate(Routes.Rest) },
                )
            }
            composable(Routes.Rest) {
                val runningVm: RunningViewModel = viewModel(factory = factory)
                val state by runningVm.state.collectAsState()
                RestScreen(
                    timerState = state,
                    onFinish = {
                        runningVm.finishRest()
                        nav.popBackStack(Routes.Focus, inclusive = false)
                    },
                )
            }
            composable(Routes.History) {
                val vm: HistoryViewModel = viewModel(factory = factory)
                HistoryScreen(
                    viewModel = vm,
                    onOpenStats = { nav.navigate(Routes.Stats) },
                    onOpenWeekly = { nav.navigate(Routes.Weekly) },
                )
            }
            composable(Routes.Stats) {
                val vm: StatsViewModel = viewModel(factory = factory)
                StatsScreen(
                    viewModel = vm,
                    onBack = { nav.popBackStack() },
                    onOpenHeatmap = { nav.navigate(Routes.Heatmap) },
                )
            }
            composable(Routes.Heatmap) {
                val vm: HeatmapViewModel = viewModel(factory = factory)
                HeatmapScreen(
                    viewModel = vm,
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Routes.Weekly) {
                val vm: WeeklyViewModel = viewModel(factory = factory)
                WeeklyScreen(
                    viewModel = vm,
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Routes.Me) {
                val vm: MeViewModel = viewModel(factory = factory)
                MeScreen(
                    viewModel = vm,
                    onOpenSettings = { nav.navigate(Routes.Settings) },
                )
            }
            composable(Routes.Settings) {
                val vm: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(
                    viewModel = vm,
                    onBack = { nav.popBackStack() },
                )
            }
        }

        // Bottom nav only on the three tab routes.
        if (currentRoute in Routes.BottomTabs) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxSize())
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        nav.navigate(route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}
