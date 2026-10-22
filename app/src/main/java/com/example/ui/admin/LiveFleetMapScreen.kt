package com.example.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.ElectricMoped
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryOrder
import com.example.data.model.Rider
import com.example.data.model.RiderStatus
import com.example.data.model.VehicleType
import com.example.ui.components.LogisticsMap
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.LogisticsBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusSuccess

@Composable
fun LiveFleetMapScreen(
    riders: List<Rider>,
    orders: List<DeliveryOrder>,
    selectedRiderId: String?,
    isSimulationActive: Boolean,
    onSelectRider: (riderId: String) -> Unit,
    onToggleSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRoutes by remember { mutableStateOf(true) }
    var activeFilter by remember { mutableStateOf("ALL") } // ALL, ON_DELIVERY, AVAILABLE

    val displayRiders = remember(riders, activeFilter) {
        when (activeFilter) {
            "BUSY" -> riders.filter { it.status == RiderStatus.ON_DELIVERY }
            "AVAILABLE" -> riders.filter { it.status == RiderStatus.AVAILABLE }
            else -> riders
        }
    }

    val selectedRider = remember(riders, selectedRiderId) {
        riders.find { it.id == selectedRiderId }
    }

    val selectedRiderOrder = remember(orders, selectedRider) {
        if (selectedRider?.currentOrderId != null) {
            orders.find { it.id == selectedRider.currentOrderId }
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("live_fleet_map_screen")
    ) {
        // 1. Full Screen Interactive Fleet Map
        LogisticsMap(
            modifier = Modifier.fillMaxSize(),
            riders = displayRiders,
            selectedRiderId = selectedRiderId,
            showAllRoutes = showRoutes,
            allOrders = orders,
            activeOrder = selectedRiderOrder,
            onRiderClick = { rider ->
                onSelectRider(rider.id)
            }
        )

        // 2. Top Filter Controls Header
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xF00F172A)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter Chips: ALL, BUSY, FREE
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL", "BUSY", "AVAILABLE").forEach { filter ->
                            val isSelected = activeFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentOrange else Color(0x22FFFFFF))
                                    .clickable { activeFilter = filter }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Toggle Routes & Pause Simulation Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showRoutes = !showRoutes },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (showRoutes) DeepBlue else Color(0x22FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = "Toggle Routes",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleSimulation,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSimulationActive) StatusSuccess else Color(0xFFEF4444))
                                .testTag("toggle_live_telemetry_button")
                        ) {
                            Icon(
                                imageVector = if (isSimulationActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Simulation",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Bottom Selected Rider Telemetry Panel
        AnimatedVisibility(
            visible = selectedRider != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (selectedRider != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_rider_telemetry_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Rider Card Top
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selectedRider.status == RiderStatus.ON_DELIVERY) Color(0xFFFFEDD5)
                                            else Color(0xFFD1FAE5)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (selectedRider.vehicle) {
                                            VehicleType.E_BIKE -> Icons.Default.ElectricBike
                                            VehicleType.SCOOTER -> Icons.Default.ElectricScooter
                                            VehicleType.MOTORCYCLE -> Icons.Default.ElectricMoped
                                            VehicleType.CARGO_VAN -> Icons.Default.LocalShipping
                                        },
                                        contentDescription = null,
                                        tint = if (selectedRider.status == RiderStatus.ON_DELIVERY) AccentOrange else StatusSuccess,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = selectedRider.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = SlateTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "★ ${selectedRider.rating}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                    Text(
                                        text = "${selectedRider.vehicle.label} • #${selectedRider.id}",
                                        fontSize = 11.sp,
                                        color = SlateTextSecondary
                                    )
                                }
                            }

                            // Close inspection button
                            IconButton(
                                onClick = { onSelectRider("") },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = SlateTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Telemetry stats row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Telemetry Speed", fontSize = 10.sp, color = SlateTextMuted)
                                Text(
                                    text = if (selectedRider.status == RiderStatus.ON_DELIVERY) "24.5 km/h" else "0 km/h (idle)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedRider.status == RiderStatus.ON_DELIVERY) AccentOrange else SlateTextPrimary
                                )
                            }

                            Column {
                                Text("Battery", fontSize = 10.sp, color = SlateTextMuted)
                                Text(
                                    text = "${selectedRider.batteryPct}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSuccess
                                )
                            }

                            Column {
                                Text("Status", fontSize = 10.sp, color = SlateTextMuted)
                                Text(
                                    text = selectedRider.status.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedRider.status == RiderStatus.AVAILABLE) StatusSuccess else AccentOrange
                                )
                            }

                            Column {
                                Text("Shift Trips", fontSize = 10.sp, color = SlateTextMuted)
                                Text(
                                    text = "${selectedRider.completedToday}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBlue
                                )
                            }
                        }

                        // Order preview if on delivery
                        if (selectedRiderOrder != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Routing Order #${selectedRiderOrder.id}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepBlue
                                    )
                                    Text(
                                        text = "${selectedRiderOrder.pickupName} → ${selectedRiderOrder.dropoffName}",
                                        fontSize = 11.sp,
                                        color = SlateTextSecondary
                                    )
                                }
                                Text(
                                    text = "$${String.format("%.2f", selectedRiderOrder.payout)}",
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBlue,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Call Rider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${selectedRider.phone}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Call Driver (${selectedRider.name.split(" ").first()})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
