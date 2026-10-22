package com.example.data.model

data class GeoPoint(
    val x: Float, // Normalized 0f..1000f map coordinate
    val y: Float
)

enum class DeliveryUrgency(val label: String, val level: Int) {
    STANDARD("Standard", 1),
    PRIORITY("Priority", 2),
    EXPRESS("Express (Urgent)", 3)
}

enum class DeliveryStatus(val label: String) {
    UNASSIGNED("Unassigned"),
    ASSIGNED("Assigned"),
    PICKED_UP("Picked Up"),
    EN_ROUTE("En Route"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

enum class VehicleType(val label: String, val maxLoad: Int) {
    E_BIKE("E-Cargo Bike", 3),
    SCOOTER("Electric Scooter", 2),
    MOTORCYCLE("Motorcycle", 4),
    CARGO_VAN("Electric Van", 8)
}

enum class RiderStatus(val label: String) {
    AVAILABLE("Available"),
    ON_DELIVERY("On Delivery"),
    OFFLINE("Offline")
}

data class DeliveryOrder(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val pickupName: String,
    val pickupAddress: String,
    val pickupLocation: GeoPoint,
    val dropoffName: String,
    val dropoffAddress: String,
    val dropoffLocation: GeoPoint,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val payout: Double,
    val tip: Double = 0.0,
    val itemType: String,
    val itemWeightKg: Double,
    val specialInstructions: String,
    val urgency: DeliveryUrgency = DeliveryUrgency.STANDARD,
    val status: DeliveryStatus = DeliveryStatus.UNASSIGNED,
    val assignedRiderId: String? = null,
    val assignedRiderName: String? = null,
    val createdAtMinutesAgo: Int = 5,
    val routeWaypoints: List<GeoPoint> = emptyList(),
    val currentRouteStepIndex: Int = 0
) {
    val totalEarnings: Double get() = payout + tip
}

data class Rider(
    val id: String,
    val name: String,
    val phone: String,
    val vehicle: VehicleType,
    val status: RiderStatus,
    val location: GeoPoint,
    val headingDegrees: Float = 0f,
    val batteryPct: Int = 92,
    val completedToday: Int = 8,
    val weeklyCompleted: Int = 46,
    val rating: Double = 4.9,
    val currentOrderId: String? = null,
    val todayEarnings: Double = 142.50,
    val currentLoadCount: Int = 0
)

data class DailyEarning(
    val dayLabel: String,
    val amount: Double,
    val tripCount: Int
)

data class DispatchStats(
    val activeDeliveries: Int,
    val availableRiders: Int,
    val totalRiders: Int,
    val avgDeliveryTimeMinutes: Double,
    val pendingOrdersCount: Int,
    val onTimeRatePercent: Double,
    val todayGrossVolume: Double
)
