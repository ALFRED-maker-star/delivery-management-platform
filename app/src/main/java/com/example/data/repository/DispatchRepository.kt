package com.example.data.repository

import com.example.data.model.DailyEarning
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.DeliveryUrgency
import com.example.data.model.DispatchStats
import com.example.data.model.GeoPoint
import com.example.data.model.Rider
import com.example.data.model.RiderStatus
import com.example.data.model.VehicleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.hypot

class DispatchRepository private constructor() {

    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    private val _orders = MutableStateFlow<List<DeliveryOrder>>(emptyList())
    val orders: StateFlow<List<DeliveryOrder>> = _orders.asStateFlow()

    private val _riders = MutableStateFlow<List<Rider>>(emptyList())
    val riders: StateFlow<List<Rider>> = _riders.asStateFlow()

    private val _activeRiderId = MutableStateFlow("RDR-101")
    val activeRiderId: StateFlow<String> = _activeRiderId.asStateFlow()

    private val _isLiveSimulationActive = MutableStateFlow(true)
    val isLiveSimulationActive: StateFlow<Boolean> = _isLiveSimulationActive.asStateFlow()

    init {
        seedInitialData()
        startLiveMovementSimulation()
    }

    private fun seedInitialData() {
        val initialRiders = listOf(
            Rider(
                id = "RDR-101",
                name = "Marco Santos",
                phone = "+1 (555) 234-5678",
                vehicle = VehicleType.E_BIKE,
                status = RiderStatus.AVAILABLE,
                location = GeoPoint(320f, 410f),
                headingDegrees = 45f,
                batteryPct = 94,
                completedToday = 9,
                weeklyCompleted = 52,
                rating = 4.95,
                todayEarnings = 158.50,
                currentLoadCount = 0
            ),
            Rider(
                id = "RDR-102",
                name = "Aisha Chen",
                phone = "+1 (555) 876-5432",
                vehicle = VehicleType.SCOOTER,
                status = RiderStatus.ON_DELIVERY,
                location = GeoPoint(580f, 320f),
                headingDegrees = 135f,
                batteryPct = 78,
                completedToday = 7,
                weeklyCompleted = 41,
                rating = 4.88,
                currentOrderId = "ORD-4001",
                todayEarnings = 124.00,
                currentLoadCount = 1
            ),
            Rider(
                id = "RDR-103",
                name = "Liam O'Connor",
                phone = "+1 (555) 345-6789",
                vehicle = VehicleType.MOTORCYCLE,
                status = RiderStatus.ON_DELIVERY,
                location = GeoPoint(450f, 650f),
                headingDegrees = 270f,
                batteryPct = 85,
                completedToday = 11,
                weeklyCompleted = 64,
                rating = 4.92,
                currentOrderId = "ORD-4002",
                todayEarnings = 196.25,
                currentLoadCount = 1
            ),
            Rider(
                id = "RDR-104",
                name = "Elena Rostova",
                phone = "+1 (555) 456-7890",
                vehicle = VehicleType.CARGO_VAN,
                status = RiderStatus.AVAILABLE,
                location = GeoPoint(210f, 260f),
                headingDegrees = 90f,
                batteryPct = 91,
                completedToday = 6,
                weeklyCompleted = 38,
                rating = 4.97,
                todayEarnings = 145.00,
                currentLoadCount = 0
            ),
            Rider(
                id = "RDR-105",
                name = "Tariq Mansoor",
                phone = "+1 (555) 567-8901",
                vehicle = VehicleType.E_BIKE,
                status = RiderStatus.AVAILABLE,
                location = GeoPoint(680f, 540f),
                headingDegrees = 180f,
                batteryPct = 82,
                completedToday = 8,
                weeklyCompleted = 49,
                rating = 4.85,
                todayEarnings = 138.00,
                currentLoadCount = 0
            ),
            Rider(
                id = "RDR-106",
                name = "Sofia Ramirez",
                phone = "+1 (555) 678-9012",
                vehicle = VehicleType.SCOOTER,
                status = RiderStatus.OFFLINE,
                location = GeoPoint(150f, 750f),
                headingDegrees = 0f,
                batteryPct = 45,
                completedToday = 4,
                weeklyCompleted = 29,
                rating = 4.79,
                todayEarnings = 68.00,
                currentLoadCount = 0
            )
        )

        val initialOrders = listOf(
            // Unassigned jobs
            DeliveryOrder(
                id = "ORD-4003",
                customerName = "Sarah Jenkins",
                customerPhone = "+1 (555) 912-3041",
                pickupName = "Artisan Bakery & Cafe",
                pickupAddress = "442 Market St, Downtown",
                pickupLocation = GeoPoint(350f, 380f),
                dropoffName = "Apex Technology Tower",
                dropoffAddress = "820 Bay Plaza, Suite 14B",
                dropoffLocation = GeoPoint(620f, 220f),
                distanceKm = 2.4,
                estimatedMinutes = 14,
                payout = 16.50,
                tip = 4.00,
                itemType = "Warm Pastries & Coffee Box",
                itemWeightKg = 3.2,
                specialInstructions = "Please use service freight elevator on arrival. Contact receptionist at front desk.",
                urgency = DeliveryUrgency.EXPRESS,
                status = DeliveryStatus.UNASSIGNED,
                createdAtMinutesAgo = 4,
                routeWaypoints = generateWaypoints(GeoPoint(350f, 380f), GeoPoint(620f, 220f))
            ),
            DeliveryOrder(
                id = "ORD-4004",
                customerName = "David Kim",
                customerPhone = "+1 (555) 432-8765",
                pickupName = "MedRx Specialty Pharmacy",
                pickupAddress = "110 Pine Blvd",
                pickupLocation = GeoPoint(280f, 490f),
                dropoffName = "Pinecrest Medical Clinic",
                dropoffAddress = "550 4th Ave, 3rd Floor",
                dropoffLocation = GeoPoint(490f, 680f),
                distanceKm = 3.1,
                estimatedMinutes = 18,
                payout = 21.00,
                tip = 5.50,
                itemType = "Temperature-Sensitive Pharmaceuticals",
                itemWeightKg = 1.8,
                specialInstructions = "Temperature-controlled bag required. Direct handoff to Dr. Kim only.",
                urgency = DeliveryUrgency.EXPRESS,
                status = DeliveryStatus.UNASSIGNED,
                createdAtMinutesAgo = 7,
                routeWaypoints = generateWaypoints(GeoPoint(280f, 490f), GeoPoint(490f, 680f))
            ),
            DeliveryOrder(
                id = "ORD-4005",
                customerName = "Rachel Green",
                customerPhone = "+1 (555) 321-9876",
                pickupName = "Green Leaf Organic Mart",
                pickupAddress = "705 Valencia Way",
                pickupLocation = GeoPoint(180f, 340f),
                dropoffName = "Harborview Condominiums",
                dropoffAddress = "21 Marina Blvd, Apt 402",
                dropoffLocation = GeoPoint(340f, 160f),
                distanceKm = 1.8,
                estimatedMinutes = 11,
                payout = 14.00,
                tip = 3.00,
                itemType = "Fresh Produce & Pantry",
                itemWeightKg = 6.4,
                specialInstructions = "Gate code: #4092. Leave at front door if no answer.",
                urgency = DeliveryUrgency.STANDARD,
                status = DeliveryStatus.UNASSIGNED,
                createdAtMinutesAgo = 12,
                routeWaypoints = generateWaypoints(GeoPoint(180f, 340f), GeoPoint(340f, 160f))
            ),
            DeliveryOrder(
                id = "ORD-4006",
                customerName = "Marcus Vance",
                customerPhone = "+1 (555) 789-0123",
                pickupName = "TechCore Hardware Lab",
                pickupAddress = "930 Mission Boulevard",
                pickupLocation = GeoPoint(510f, 480f),
                dropoffName = "Vance Architecture Studio",
                dropoffAddress = "1200 Howard St, Studio 6",
                dropoffLocation = GeoPoint(720f, 390f),
                distanceKm = 2.9,
                estimatedMinutes = 16,
                payout = 19.50,
                tip = 4.50,
                itemType = "Fragile Laser Sensor Prototypes",
                itemWeightKg = 2.5,
                specialInstructions = "Handle with extreme care. Keep package upright.",
                urgency = DeliveryUrgency.PRIORITY,
                status = DeliveryStatus.UNASSIGNED,
                createdAtMinutesAgo = 15,
                routeWaypoints = generateWaypoints(GeoPoint(510f, 480f), GeoPoint(720f, 390f))
            ),

            // Active in-transit orders
            DeliveryOrder(
                id = "ORD-4001",
                customerName = "Emily Watson",
                customerPhone = "+1 (555) 654-3210",
                pickupName = "Bistro Lumiere",
                pickupAddress = "234 Sutter Street",
                pickupLocation = GeoPoint(480f, 260f),
                dropoffName = "Financial Tower",
                dropoffAddress = "500 California St, 28th Fl",
                dropoffLocation = GeoPoint(680f, 390f),
                distanceKm = 2.1,
                estimatedMinutes = 9,
                payout = 18.00,
                tip = 5.00,
                itemType = "Catering Lunch Package",
                itemWeightKg = 4.1,
                specialInstructions = "Call when arriving at lobby loading zone.",
                urgency = DeliveryUrgency.PRIORITY,
                status = DeliveryStatus.EN_ROUTE,
                assignedRiderId = "RDR-102",
                assignedRiderName = "Aisha Chen",
                createdAtMinutesAgo = 22,
                routeWaypoints = generateWaypoints(GeoPoint(480f, 260f), GeoPoint(680f, 390f)),
                currentRouteStepIndex = 4
            ),
            DeliveryOrder(
                id = "ORD-4002",
                customerName = "James Bennett",
                customerPhone = "+1 (555) 987-6543",
                pickupName = "RapidPrint Express",
                pickupAddress = "610 Folsom Street",
                pickupLocation = GeoPoint(380f, 620f),
                dropoffName = "Horizon Event Hall",
                dropoffAddress = "740 3rd St, Main Entrance",
                dropoffLocation = GeoPoint(560f, 720f),
                distanceKm = 3.6,
                estimatedMinutes = 12,
                payout = 24.50,
                tip = 6.00,
                itemType = "Conference Banner Displays",
                itemWeightKg = 7.8,
                specialInstructions = "Urgent event setup. Hand off directly to James at stage doors.",
                urgency = DeliveryUrgency.EXPRESS,
                status = DeliveryStatus.PICKED_UP,
                assignedRiderId = "RDR-103",
                assignedRiderName = "Liam O'Connor",
                createdAtMinutesAgo = 18,
                routeWaypoints = generateWaypoints(GeoPoint(380f, 620f), GeoPoint(560f, 720f)),
                currentRouteStepIndex = 2
            ),

            // Completed orders for history & earnings
            DeliveryOrder(
                id = "ORD-3998",
                customerName = "Alex Rivera",
                customerPhone = "+1 (555) 111-2233",
                pickupName = "Central Roastery",
                pickupAddress = "150 2nd St",
                pickupLocation = GeoPoint(300f, 400f),
                dropoffName = "Salesforce East",
                dropoffAddress = "350 Mission St",
                dropoffLocation = GeoPoint(420f, 440f),
                distanceKm = 1.4,
                estimatedMinutes = 8,
                payout = 13.50,
                tip = 3.50,
                itemType = "Cold Brew Keg & Snacks",
                itemWeightKg = 5.0,
                specialInstructions = "Left with security desk as requested.",
                urgency = DeliveryUrgency.STANDARD,
                status = DeliveryStatus.DELIVERED,
                assignedRiderId = "RDR-101",
                assignedRiderName = "Marco Santos",
                createdAtMinutesAgo = 55
            ),
            DeliveryOrder(
                id = "ORD-3995",
                customerName = "Chloe Bennett",
                customerPhone = "+1 (555) 222-3344",
                pickupName = "Gourmet Deli",
                pickupAddress = "88 5th St",
                pickupLocation = GeoPoint(260f, 380f),
                dropoffName = "Union Square Lofts",
                dropoffAddress = "310 Geary St",
                dropoffLocation = GeoPoint(340f, 300f),
                distanceKm = 1.9,
                estimatedMinutes = 12,
                payout = 15.00,
                tip = 4.00,
                itemType = "Artisan Sandwiches",
                itemWeightKg = 2.2,
                specialInstructions = "Delivered to door.",
                urgency = DeliveryUrgency.STANDARD,
                status = DeliveryStatus.DELIVERED,
                assignedRiderId = "RDR-101",
                assignedRiderName = "Marco Santos",
                createdAtMinutesAgo = 110
            )
        )

        _riders.value = initialRiders
        _orders.value = initialOrders
    }

    private fun generateWaypoints(start: GeoPoint, end: GeoPoint): List<GeoPoint> {
        val points = mutableListOf<GeoPoint>()
        points.add(start)
        val steps = 8
        for (i in 1 until steps) {
            val fraction = i.toFloat() / steps
            // Add slight dogleg turns simulating city blocks
            val jitterX = if (i % 2 == 0) 15f else -15f
            val jitterY = if (i % 3 == 0) -12f else 12f
            val x = start.x + (end.x - start.x) * fraction + jitterX
            val y = start.y + (end.y - start.y) * fraction + jitterY
            points.add(GeoPoint(x, y))
        }
        points.add(end)
        return points
    }

    fun setActiveRider(riderId: String) {
        _activeRiderId.value = riderId
    }

    fun toggleSimulation() {
        _isLiveSimulationActive.update { !it }
    }

    fun acceptOrder(orderId: String, riderId: String = _activeRiderId.value): Boolean {
        var success = false
        val targetRider = _riders.value.find { it.id == riderId } ?: return false

        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId && order.status == DeliveryStatus.UNASSIGNED) {
                    success = true
                    order.copy(
                        status = DeliveryStatus.ASSIGNED,
                        assignedRiderId = riderId,
                        assignedRiderName = targetRider.name
                    )
                } else {
                    order
                }
            }
        }

        if (success) {
            _riders.update { list ->
                list.map { rider ->
                    if (rider.id == riderId) {
                        rider.copy(
                            status = RiderStatus.ON_DELIVERY,
                            currentOrderId = orderId,
                            currentLoadCount = rider.currentLoadCount + 1
                        )
                    } else rider
                }
            }
        }
        return success
    }

    fun updateOrderStatus(orderId: String, newStatus: DeliveryStatus) {
        val order = _orders.value.find { it.id == orderId } ?: return
        val riderId = order.assignedRiderId

        _orders.update { list ->
            list.map { o ->
                if (o.id == orderId) {
                    o.copy(status = newStatus)
                } else o
            }
        }

        if (newStatus == DeliveryStatus.DELIVERED && riderId != null) {
            _riders.update { list ->
                list.map { rider ->
                    if (rider.id == riderId) {
                        rider.copy(
                            status = RiderStatus.AVAILABLE,
                            currentOrderId = null,
                            completedToday = rider.completedToday + 1,
                            todayEarnings = rider.todayEarnings + order.totalEarnings,
                            currentLoadCount = (rider.currentLoadCount - 1).coerceAtLeast(0)
                        )
                    } else rider
                }
            }
        }
    }

    fun assignRiderToOrder(orderId: String, riderId: String) {
        val rider = _riders.value.find { it.id == riderId } ?: return
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    order.copy(
                        status = DeliveryStatus.ASSIGNED,
                        assignedRiderId = riderId,
                        assignedRiderName = rider.name
                    )
                } else order
            }
        }
        _riders.update { list ->
            list.map { r ->
                if (r.id == riderId) {
                    r.copy(
                        status = RiderStatus.ON_DELIVERY,
                        currentOrderId = orderId,
                        currentLoadCount = r.currentLoadCount + 1
                    )
                } else r
            }
        }
    }

    fun autoAssignPendingOrders(): Int {
        var assignedCount = 0
        val pending = _orders.value.filter { it.status == DeliveryStatus.UNASSIGNED }
        val availableRiders = _riders.value
            .filter { it.status == RiderStatus.AVAILABLE && it.currentLoadCount < it.vehicle.maxLoad }
            .toMutableList()

        if (availableRiders.isEmpty() || pending.isEmpty()) return 0

        pending.forEach { order ->
            if (availableRiders.isNotEmpty()) {
                // Find nearest rider
                val bestRider = availableRiders.minByOrNull { rider ->
                    val dx = rider.location.x - order.pickupLocation.x
                    val dy = rider.location.y - order.pickupLocation.y
                    hypot(dx, dy)
                }
                if (bestRider != null) {
                    assignRiderToOrder(order.id, bestRider.id)
                    assignedCount++
                    availableRiders.remove(bestRider)
                }
            }
        }
        return assignedCount
    }

    fun setRiderStatus(riderId: String, newStatus: RiderStatus) {
        _riders.update { list ->
            list.map { if (it.id == riderId) it.copy(status = newStatus) else it }
        }
    }

    fun createOrder(
        customerName: String,
        customerPhone: String,
        pickupName: String,
        pickupAddress: String,
        dropoffName: String,
        dropoffAddress: String,
        itemType: String,
        urgency: DeliveryUrgency,
        distanceKm: Double = 2.5,
        payout: Double = 18.00
    ) {
        val nextId = "ORD-${4000 + _orders.value.size + 1}"
        val pickup = GeoPoint((200..600).random().toFloat(), (200..600).random().toFloat())
        val dropoff = GeoPoint((200..700).random().toFloat(), (200..700).random().toFloat())
        val newOrder = DeliveryOrder(
            id = nextId,
            customerName = customerName,
            customerPhone = customerPhone,
            pickupName = pickupName,
            pickupAddress = pickupAddress,
            pickupLocation = pickup,
            dropoffName = dropoffName,
            dropoffAddress = dropoffAddress,
            dropoffLocation = dropoff,
            distanceKm = distanceKm,
            estimatedMinutes = (distanceKm * 5).toInt() + 4,
            payout = payout,
            tip = 3.50,
            itemType = itemType,
            itemWeightKg = 2.5,
            specialInstructions = "Standard logistics delivery dispatch.",
            urgency = urgency,
            status = DeliveryStatus.UNASSIGNED,
            createdAtMinutesAgo = 1,
            routeWaypoints = generateWaypoints(pickup, dropoff)
        )
        _orders.update { listOf(newOrder) + it }
    }

    fun getDispatchStats(): DispatchStats {
        val allOrders = _orders.value
        val allRiders = _riders.value

        val activeDeliveries = allOrders.count {
            it.status == DeliveryStatus.ASSIGNED ||
            it.status == DeliveryStatus.PICKED_UP ||
            it.status == DeliveryStatus.EN_ROUTE
        }
        val availableRiders = allRiders.count { it.status == RiderStatus.AVAILABLE }
        val pendingCount = allOrders.count { it.status == DeliveryStatus.UNASSIGNED }
        val grossVolume = allOrders.sumOf { it.totalEarnings } + 3420.0

        return DispatchStats(
            activeDeliveries = activeDeliveries,
            availableRiders = availableRiders,
            totalRiders = allRiders.size,
            avgDeliveryTimeMinutes = 21.4,
            pendingOrdersCount = pendingCount,
            onTimeRatePercent = 97.2,
            todayGrossVolume = grossVolume
        )
    }

    fun getWeeklyEarnings(): List<DailyEarning> {
        return listOf(
            DailyEarning("Mon", 145.00, 8),
            DailyEarning("Tue", 168.50, 10),
            DailyEarning("Wed", 152.00, 9),
            DailyEarning("Thu", 188.00, 11),
            DailyEarning("Fri", 215.50, 13),
            DailyEarning("Sat", 192.00, 12),
            DailyEarning("Sun (Today)", 158.50, 9)
        )
    }

    private fun startLiveMovementSimulation() {
        repositoryScope.launch {
            while (true) {
                delay(2400)
                if (_isLiveSimulationActive.value) {
                    simulateStep()
                }
            }
        }
    }

    private fun simulateStep() {
        val currentOrders = _orders.value
        _riders.update { riderList ->
            riderList.map { rider ->
                if (rider.status == RiderStatus.ON_DELIVERY && rider.currentOrderId != null) {
                    val order = currentOrders.find { it.id == rider.currentOrderId }
                    if (order != null && order.routeWaypoints.isNotEmpty()) {
                        val currentIdx = order.currentRouteStepIndex
                        val nextIdx = (currentIdx + 1) % order.routeWaypoints.size
                        val nextPoint = order.routeWaypoints[nextIdx]

                        // Calculate heading angle
                        val dx = nextPoint.x - rider.location.x
                        val dy = nextPoint.y - rider.location.y
                        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

                        rider.copy(
                            location = nextPoint,
                            headingDegrees = (angle + 360f) % 360f
                        )
                    } else rider
                } else if (rider.status == RiderStatus.AVAILABLE) {
                    // Small idle drift around current block
                    val driftX = (-3..3).random().toFloat()
                    val driftY = (-3..3).random().toFloat()
                    val newX = (rider.location.x + driftX).coerceIn(100f, 900f)
                    val newY = (rider.location.y + driftY).coerceIn(100f, 900f)
                    rider.copy(location = GeoPoint(newX, newY))
                } else rider
            }
        }

        // Advance route step indexes on active orders
        _orders.update { orderList ->
            orderList.map { order ->
                if (order.status == DeliveryStatus.EN_ROUTE || order.status == DeliveryStatus.PICKED_UP) {
                    val nextStep = (order.currentRouteStepIndex + 1) % (order.routeWaypoints.size.coerceAtLeast(1))
                    order.copy(currentRouteStepIndex = nextStep)
                } else order
            }
        }
    }

    companion object {
        @Volatile
        private var instance: DispatchRepository? = null

        fun getInstance(): DispatchRepository {
            return instance ?: synchronized(this) {
                instance ?: DispatchRepository().also { instance = it }
            }
        }
    }
}
