package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DeliveryStatus
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.admin.AdminLoginScreen
import com.example.ui.admin.AssignRiderScreen
import com.example.ui.admin.LiveFleetMapScreen
import com.example.ui.admin.OrdersQueueScreen
import com.example.ui.admin.RidersListScreen
import com.example.ui.components.AppTopBar
import com.example.ui.rider.ActiveDeliveryScreen
import com.example.ui.rider.AvailableDeliveriesScreen
import com.example.ui.rider.DeliveryDetailScreen
import com.example.ui.rider.RiderEarningsScreen
import com.example.ui.rider.RiderLoginScreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.LogisticsBg
import com.example.ui.theme.LogisticsBorder

@Composable
fun DispatchGoApp(
    viewModel: DispatchViewModel = viewModel()
) {
    val appRole by viewModel.appRole.collectAsState()
    val isRiderLoggedIn by viewModel.isRiderLoggedIn.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val currentRiderNav by viewModel.currentRiderNav.collectAsState()
    val currentAdminNav by viewModel.currentAdminNav.collectAsState()

    val orders by viewModel.orders.collectAsState()
    val riders by viewModel.riders.collectAsState()
    val currentRider by viewModel.currentRider.collectAsState()
    val activeRiderDelivery by viewModel.currentRiderActiveOrder.collectAsState()
    val availableDeliveries by viewModel.availableDeliveries.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()
    val selectedMapRiderId by viewModel.selectedMapRiderId.collectAsState()
    val isSimulationActive by viewModel.isLiveSimulationActive.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // Top bar shown when logged in or for role toggle
                AppTopBar(
                    currentRole = appRole,
                    onSwitchRole = {
                        val nextRole = if (appRole == AppRole.RIDER) AppRole.ADMIN else AppRole.RIDER
                        viewModel.switchRole(nextRole)
                    },
                    title = if (appRole == AppRole.RIDER) currentRiderNav.title else currentAdminNav.title,
                    subtitle = if (appRole == AppRole.RIDER) {
                        currentRider?.let { "${it.name} • ${it.vehicle.label}" }
                    } else "Central Command Station"
                )
            },
            bottomBar = {
                // Bottom Bar for Mobile Layouts
                if (!isWideScreen) {
                    if (appRole == AppRole.RIDER && isRiderLoggedIn) {
                        RiderBottomBar(
                            currentNav = currentRiderNav,
                            onNavChange = { viewModel.navigateRider(it) },
                            availableCount = availableDeliveries.size,
                            hasActiveDelivery = activeRiderDelivery != null,
                            onLogout = { viewModel.logoutRider() }
                        )
                    } else if (appRole == AppRole.ADMIN && isAdminLoggedIn) {
                        AdminBottomBar(
                            currentNav = currentAdminNav,
                            onNavChange = { viewModel.navigateAdmin(it) },
                            pendingCount = orders.count { it.status == DeliveryStatus.UNASSIGNED }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(LogisticsBg)
            ) {
                if (isWideScreen && appRole == AppRole.ADMIN && isAdminLoggedIn) {
                    // Desktop-First Adaptive Layout with Navigation Rail
                    Row(modifier = Modifier.fillMaxSize()) {
                        AdminNavigationRail(
                            currentNav = currentAdminNav,
                            onNavChange = { viewModel.navigateAdmin(it) },
                            pendingCount = orders.count { it.status == DeliveryStatus.UNASSIGNED },
                            onLogout = { viewModel.logoutAdmin() }
                        )
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            AdminContent(
                                currentAdminNav = currentAdminNav,
                                viewModel = viewModel,
                                orders = orders,
                                riders = riders,
                                selectedOrder = selectedOrder,
                                selectedMapRiderId = selectedMapRiderId,
                                isSimulationActive = isSimulationActive
                            )
                        }
                    }
                } else {
                    // Standard Screen routing
                    if (appRole == AppRole.RIDER) {
                        if (!isRiderLoggedIn) {
                            RiderLoginScreen(onLoginSuccess = { viewModel.loginRider(it) })
                        } else {
                            RiderContent(
                                currentRiderNav = currentRiderNav,
                                viewModel = viewModel,
                                currentRider = currentRider,
                                availableDeliveries = availableDeliveries,
                                activeOrder = activeRiderDelivery ?: selectedOrder,
                                selectedOrder = selectedOrder,
                                allOrders = orders
                            )
                        }
                    } else {
                        if (!isAdminLoggedIn) {
                            AdminLoginScreen(onLoginSuccess = { viewModel.loginAdmin(it) })
                        } else {
                            AdminContent(
                                currentAdminNav = currentAdminNav,
                                viewModel = viewModel,
                                orders = orders,
                                riders = riders,
                                selectedOrder = selectedOrder,
                                selectedMapRiderId = selectedMapRiderId,
                                isSimulationActive = isSimulationActive
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiderContent(
    currentRiderNav: RiderNavScreen,
    viewModel: DispatchViewModel,
    currentRider: com.example.data.model.Rider?,
    availableDeliveries: List<com.example.data.model.DeliveryOrder>,
    activeOrder: com.example.data.model.DeliveryOrder?,
    selectedOrder: com.example.data.model.DeliveryOrder?,
    allOrders: List<com.example.data.model.DeliveryOrder>
) {
    when (currentRiderNav) {
        RiderNavScreen.LOGIN -> RiderLoginScreen(onLoginSuccess = { viewModel.loginRider(it) })
        RiderNavScreen.AVAILABLE -> AvailableDeliveriesScreen(
            deliveries = availableDeliveries,
            currentRider = currentRider,
            onAcceptDelivery = { viewModel.acceptOrder(it) },
            onViewDetail = { viewModel.selectOrder(it, navigateToDetail = true) }
        )
        RiderNavScreen.DETAIL -> DeliveryDetailScreen(
            order = selectedOrder,
            onStartDelivery = { viewModel.startDelivery(it) },
            onBack = { viewModel.navigateRider(RiderNavScreen.AVAILABLE) }
        )
        RiderNavScreen.ACTIVE -> ActiveDeliveryScreen(
            order = activeOrder,
            currentRider = currentRider,
            onAdvanceStatus = { id, st -> viewModel.advanceActiveDeliveryStatus(id, st) },
            onBack = { viewModel.navigateRider(RiderNavScreen.AVAILABLE) }
        )
        RiderNavScreen.EARNINGS -> RiderEarningsScreen(
            currentRider = currentRider,
            allOrders = allOrders,
            weeklyEarnings = viewModel.getWeeklyEarnings()
        )
    }
}

@Composable
fun AdminContent(
    currentAdminNav: AdminNavScreen,
    viewModel: DispatchViewModel,
    orders: List<com.example.data.model.DeliveryOrder>,
    riders: List<com.example.data.model.Rider>,
    selectedOrder: com.example.data.model.DeliveryOrder?,
    selectedMapRiderId: String?,
    isSimulationActive: Boolean
) {
    when (currentAdminNav) {
        AdminNavScreen.LOGIN -> AdminLoginScreen(onLoginSuccess = { viewModel.loginAdmin(it) })
        AdminNavScreen.DASHBOARD -> AdminDashboardScreen(
            stats = viewModel.getDispatchStats(),
            orders = orders,
            riders = riders,
            isSimulationActive = isSimulationActive,
            onAutoAssignAll = { viewModel.autoAssignOrders() },
            onNavigateToQueue = { viewModel.navigateAdmin(AdminNavScreen.QUEUE) },
            onNavigateToLiveMap = { viewModel.navigateAdmin(AdminNavScreen.LIVE_MAP) },
            onToggleSimulation = { viewModel.toggleSimulation() },
            onCreateSampleOrder = { c, p, d, i, u -> viewModel.createSampleOrder(c, p, d, i, u) }
        )
        AdminNavScreen.QUEUE -> OrdersQueueScreen(
            orders = orders,
            riders = riders,
            onManualAssign = { orderId ->
                viewModel.selectOrder(orderId)
                viewModel.navigateAdmin(AdminNavScreen.ASSIGN)
            },
            onAutoAssignOrder = { orderId ->
                viewModel.autoAssignOrders()
            },
            onAutoAssignAll = { viewModel.autoAssignOrders() }
        )
        AdminNavScreen.ASSIGN -> AssignRiderScreen(
            order = selectedOrder,
            riders = riders,
            onConfirmAssignment = { oId, rId -> viewModel.assignRider(oId, rId) },
            onBack = { viewModel.navigateAdmin(AdminNavScreen.QUEUE) }
        )
        AdminNavScreen.RIDERS -> RidersListScreen(
            riders = riders,
            onViewRiderOnMap = { rId ->
                viewModel.selectMapRider(rId)
                viewModel.navigateAdmin(AdminNavScreen.LIVE_MAP)
            },
            onToggleStatus = { rId, status -> viewModel.setRiderStatus(rId, status) }
        )
        AdminNavScreen.LIVE_MAP -> LiveFleetMapScreen(
            riders = riders,
            orders = orders,
            selectedRiderId = selectedMapRiderId,
            isSimulationActive = isSimulationActive,
            onSelectRider = { viewModel.selectMapRider(it) },
            onToggleSimulation = { viewModel.toggleSimulation() }
        )
    }
}

@Composable
fun RiderBottomBar(
    currentNav: RiderNavScreen,
    onNavChange: (RiderNavScreen) -> Unit,
    availableCount: Int,
    hasActiveDelivery: Boolean,
    onLogout: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = DeepBlue,
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("rider_bottom_navigation")
    ) {
        NavigationBarItem(
            selected = currentNav == RiderNavScreen.AVAILABLE,
            onClick = { onNavChange(RiderNavScreen.AVAILABLE) },
            icon = {
                if (availableCount > 0) {
                    BadgedBox(badge = { Badge { Text("$availableCount") } }) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = "Available")
                    }
                } else {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "Available")
                }
            },
            label = { Text("Available", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentOrange,
                selectedTextColor = AccentOrange,
                indicatorColor = Color(0xFFFFEDD5)
            )
        )

        NavigationBarItem(
            selected = currentNav == RiderNavScreen.ACTIVE || currentNav == RiderNavScreen.DETAIL,
            onClick = { onNavChange(RiderNavScreen.ACTIVE) },
            icon = {
                if (hasActiveDelivery) {
                    BadgedBox(badge = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AccentOrange, CircleShape)
                        )
                    }) {
                        Icon(Icons.Default.Navigation, contentDescription = "Active")
                    }
                } else {
                    Icon(Icons.Default.Navigation, contentDescription = "Active")
                }
            },
            label = { Text("Active Trip", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentOrange,
                selectedTextColor = AccentOrange,
                indicatorColor = Color(0xFFFFEDD5)
            )
        )

        NavigationBarItem(
            selected = currentNav == RiderNavScreen.EARNINGS,
            onClick = { onNavChange(RiderNavScreen.EARNINGS) },
            icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Earnings") },
            label = { Text("Earnings", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepBlue,
                selectedTextColor = DeepBlue,
                indicatorColor = Color(0xFFDBEAFE)
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out") },
            label = { Text("Sign Out", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            )
        )
    }
}

@Composable
fun AdminBottomBar(
    currentNav: AdminNavScreen,
    onNavChange: (AdminNavScreen) -> Unit,
    pendingCount: Int
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = DeepBlue,
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("admin_bottom_navigation")
    ) {
        NavigationBarItem(
            selected = currentNav == AdminNavScreen.DASHBOARD,
            onClick = { onNavChange(AdminNavScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepBlue,
                selectedTextColor = DeepBlue,
                indicatorColor = Color(0xFFDBEAFE)
            )
        )

        NavigationBarItem(
            selected = currentNav == AdminNavScreen.QUEUE || currentNav == AdminNavScreen.ASSIGN,
            onClick = { onNavChange(AdminNavScreen.QUEUE) },
            icon = {
                if (pendingCount > 0) {
                    BadgedBox(badge = {
                        Badge(containerColor = AccentOrange) { Text("$pendingCount") }
                    }) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = "Orders")
                    }
                } else {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "Orders")
                }
            },
            label = { Text("Orders", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepBlue,
                selectedTextColor = DeepBlue,
                indicatorColor = Color(0xFFDBEAFE)
            )
        )

        NavigationBarItem(
            selected = currentNav == AdminNavScreen.RIDERS,
            onClick = { onNavChange(AdminNavScreen.RIDERS) },
            icon = { Icon(Icons.Default.People, contentDescription = "Fleet") },
            label = { Text("Fleet", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepBlue,
                selectedTextColor = DeepBlue,
                indicatorColor = Color(0xFFDBEAFE)
            )
        )

        NavigationBarItem(
            selected = currentNav == AdminNavScreen.LIVE_MAP,
            onClick = { onNavChange(AdminNavScreen.LIVE_MAP) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Live Map") },
            label = { Text("Live Map", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentOrange,
                selectedTextColor = AccentOrange,
                indicatorColor = Color(0xFFFFEDD5)
            )
        )
    }
}

@Composable
fun AdminNavigationRail(
    currentNav: AdminNavScreen,
    onNavChange: (AdminNavScreen) -> Unit,
    pendingCount: Int,
    onLogout: () -> Unit
) {
    NavigationRail(
        containerColor = Color.White,
        contentColor = DeepBlue,
        modifier = Modifier.testTag("admin_navigation_rail")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        NavigationRailItem(
            selected = currentNav == AdminNavScreen.DASHBOARD,
            onClick = { onNavChange(AdminNavScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontSize = 10.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = DeepBlue,
                indicatorColor = Color(0xFFDBEAFE)
            )
        )

        NavigationRailItem(
            selected = currentNav == AdminNavScreen.QUEUE || currentNav == AdminNavScreen.ASSIGN,
            onClick = { onNavChange(AdminNavScreen.QUEUE) },
            icon = {
                if (pendingCount > 0) {
                    BadgedBox(badge = { Badge(containerColor = AccentOrange) { Text("$pendingCount") } }) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = "Queue")
                    }
                } else {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "Queue")
                }
            },
            label = { Text("Orders", fontSize = 10.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = DeepBlue,
                indicatorColor = Color(0xFFDBEAFE)
            )
        )

        NavigationRailItem(
            selected = currentNav == AdminNavScreen.RIDERS,
            onClick = { onNavChange(AdminNavScreen.RIDERS) },
            icon = { Icon(Icons.Default.People, contentDescription = "Fleet") },
            label = { Text("Fleet", fontSize = 10.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = DeepBlue,
                indicatorColor = Color(0xFFDBEAFE)
            )
        )

        NavigationRailItem(
            selected = currentNav == AdminNavScreen.LIVE_MAP,
            onClick = { onNavChange(AdminNavScreen.LIVE_MAP) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Live Map") },
            label = { Text("Live Map", fontSize = 10.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = AccentOrange,
                indicatorColor = Color(0xFFFFEDD5)
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationRailItem(
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out") },
            label = { Text("Exit", fontSize = 10.sp) }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}
