package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DailyEarning
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.DeliveryUrgency
import com.example.data.model.DispatchStats
import com.example.data.model.Rider
import com.example.data.model.RiderStatus
import com.example.data.repository.DispatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppRole(val label: String) {
    RIDER("Rider Experience"),
    ADMIN("Admin / Dispatcher")
}

enum class RiderNavScreen(val title: String) {
    LOGIN("Rider Login"),
    AVAILABLE("Available Deliveries"),
    DETAIL("Delivery Detail"),
    ACTIVE("Active Delivery"),
    EARNINGS("Earnings & History")
}

enum class AdminNavScreen(val title: String) {
    LOGIN("Admin Login"),
    DASHBOARD("Dashboard"),
    QUEUE("Orders Queue"),
    ASSIGN("Assign Rider"),
    RIDERS("Riders Fleet"),
    LIVE_MAP("Live Map")
}

class DispatchViewModel(
    private val repository: DispatchRepository = DispatchRepository.getInstance()
) : ViewModel() {

    private val _appRole = MutableStateFlow(AppRole.RIDER)
    val appRole: StateFlow<AppRole> = _appRole.asStateFlow()

    private val _isRiderLoggedIn = MutableStateFlow(true)
    val isRiderLoggedIn: StateFlow<Boolean> = _isRiderLoggedIn.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(true)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _currentRiderNav = MutableStateFlow(RiderNavScreen.AVAILABLE)
    val currentRiderNav: StateFlow<RiderNavScreen> = _currentRiderNav.asStateFlow()

    private val _currentAdminNav = MutableStateFlow(AdminNavScreen.DASHBOARD)
    val currentAdminNav: StateFlow<AdminNavScreen> = _currentAdminNav.asStateFlow()

    val orders: StateFlow<List<DeliveryOrder>> = repository.orders
    val riders: StateFlow<List<Rider>> = repository.riders
    val activeRiderId: StateFlow<String> = repository.activeRiderId
    val isLiveSimulationActive: StateFlow<Boolean> = repository.isLiveSimulationActive

    private val _selectedOrderId = MutableStateFlow<String?>("ORD-4003")
    val selectedOrderId: StateFlow<String?> = _selectedOrderId.asStateFlow()

    private val _selectedMapRiderId = MutableStateFlow<String?>("RDR-101")
    val selectedMapRiderId: StateFlow<String?> = _selectedMapRiderId.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Filters
    private val _orderQueueFilter = MutableStateFlow<DeliveryStatus?>(null)
    val orderQueueFilter: StateFlow<DeliveryStatus?> = _orderQueueFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Current active rider object
    val currentRider: StateFlow<Rider?> = combine(riders, activeRiderId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current active delivery for rider
    val currentRiderActiveOrder: StateFlow<DeliveryOrder?> = combine(orders, activeRiderId) { orderList, rId ->
        orderList.find {
            it.assignedRiderId == rId &&
            (it.status == DeliveryStatus.ASSIGNED ||
             it.status == DeliveryStatus.PICKED_UP ||
             it.status == DeliveryStatus.EN_ROUTE)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Unassigned deliveries for Rider screen 2
    val availableDeliveries: StateFlow<List<DeliveryOrder>> = orders.map { list ->
        list.filter { it.status == DeliveryStatus.UNASSIGNED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected order object
    val selectedOrder: StateFlow<DeliveryOrder?> = combine(orders, selectedOrderId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun switchRole(role: AppRole) {
        _appRole.value = role
        _userMessage.value = "Switched to ${role.label}"
    }

    fun loginRider(riderId: String = "RDR-101") {
        repository.setActiveRider(riderId)
        _isRiderLoggedIn.value = true
        _currentRiderNav.value = RiderNavScreen.AVAILABLE
        _userMessage.value = "Welcome back, Rider!"
    }

    fun logoutRider() {
        _isRiderLoggedIn.value = false
        _currentRiderNav.value = RiderNavScreen.LOGIN
    }

    fun loginAdmin(adminName: String = "HQ Controller") {
        _isAdminLoggedIn.value = true
        _currentAdminNav.value = AdminNavScreen.DASHBOARD
        _userMessage.value = "Admin session verified for $adminName"
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _currentAdminNav.value = AdminNavScreen.LOGIN
    }

    fun navigateRider(screen: RiderNavScreen) {
        _currentRiderNav.value = screen
    }

    fun navigateAdmin(screen: AdminNavScreen) {
        _currentAdminNav.value = screen
    }

    fun selectOrder(orderId: String, navigateToDetail: Boolean = false) {
        _selectedOrderId.value = orderId
        if (navigateToDetail) {
            _currentRiderNav.value = RiderNavScreen.DETAIL
        }
    }

    fun selectMapRider(riderId: String) {
        _selectedMapRiderId.value = riderId
    }

    fun acceptOrder(orderId: String) {
        val riderId = activeRiderId.value
        val success = repository.acceptOrder(orderId, riderId)
        if (success) {
            _selectedOrderId.value = orderId
            _currentRiderNav.value = RiderNavScreen.DETAIL
            _userMessage.value = "Job Accepted! Review details to start delivery."
        } else {
            _userMessage.value = "Unable to accept job."
        }
    }

    fun startDelivery(orderId: String) {
        repository.updateOrderStatus(orderId, DeliveryStatus.PICKED_UP)
        _selectedOrderId.value = orderId
        _currentRiderNav.value = RiderNavScreen.ACTIVE
        _userMessage.value = "Package Picked Up! Navigating to drop-off."
    }

    fun advanceActiveDeliveryStatus(orderId: String, currentStatus: DeliveryStatus) {
        when (currentStatus) {
            DeliveryStatus.ASSIGNED -> {
                repository.updateOrderStatus(orderId, DeliveryStatus.PICKED_UP)
                _userMessage.value = "Status updated: Picked Up!"
            }
            DeliveryStatus.PICKED_UP -> {
                repository.updateOrderStatus(orderId, DeliveryStatus.EN_ROUTE)
                _userMessage.value = "Status updated: En Route to customer!"
            }
            DeliveryStatus.EN_ROUTE -> {
                repository.updateOrderStatus(orderId, DeliveryStatus.DELIVERED)
                _userMessage.value = "🎉 Delivery marked as Completed! Payout added to earnings."
                _currentRiderNav.value = RiderNavScreen.EARNINGS
            }
            else -> {}
        }
    }

    fun assignRider(orderId: String, riderId: String) {
        repository.assignRiderToOrder(orderId, riderId)
        _userMessage.value = "Order $orderId assigned successfully!"
        _currentAdminNav.value = AdminNavScreen.QUEUE
    }

    fun autoAssignOrders() {
        val count = repository.autoAssignPendingOrders()
        if (count > 0) {
            _userMessage.value = "Auto-assigned $count orders based on distance & capacity!"
        } else {
            _userMessage.value = "No pending orders or available riders."
        }
    }

    fun toggleSimulation() {
        repository.toggleSimulation()
        val state = if (isLiveSimulationActive.value) "resumed" else "paused"
        _userMessage.value = "Live GPS simulation $state"
    }

    fun setOrderFilter(status: DeliveryStatus?) {
        _orderQueueFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRiderStatus(riderId: String, status: RiderStatus) {
        repository.setRiderStatus(riderId, status)
        _userMessage.value = "Rider status changed to ${status.label}"
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun getDispatchStats(): DispatchStats = repository.getDispatchStats()

    fun getWeeklyEarnings(): List<DailyEarning> = repository.getWeeklyEarnings()

    fun createSampleOrder(customer: String, pickup: String, dropoff: String, item: String, urgency: DeliveryUrgency) {
        repository.createOrder(
            customerName = customer,
            customerPhone = "+1 (555) 777-8899",
            pickupName = pickup,
            pickupAddress = "$pickup, Metro Zone",
            dropoffName = dropoff,
            dropoffAddress = "$dropoff, Metro Zone",
            itemType = item,
            urgency = urgency
        )
        _userMessage.value = "New delivery order created and queued!"
    }
}
