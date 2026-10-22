package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.ElectricMoped
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Rider
import com.example.data.model.RiderStatus
import com.example.data.model.VehicleType
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.LogisticsBg
import com.example.ui.theme.LogisticsBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusSuccess

@Composable
fun RidersListScreen(
    riders: List<Rider>,
    onViewRiderOnMap: (riderId: String) -> Unit,
    onToggleStatus: (riderId: String, newStatus: RiderStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var statusFilter by remember { mutableStateOf<RiderStatus?>(null) } // null = All

    val filteredRiders = remember(riders, statusFilter) {
        if (statusFilter == null) riders else riders.filter { it.status == statusFilter }
    }

    val availableCount = riders.count { it.status == RiderStatus.AVAILABLE }
    val onDeliveryCount = riders.count { it.status == RiderStatus.ON_DELIVERY }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LogisticsBg)
            .testTag("riders_list_screen")
    ) {
        // Fleet Header Bar
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fleet Roster & Telemetry",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = "$availableCount Available • $onDeliveryCount On Delivery",
                            fontSize = 12.sp,
                            color = SlateTextSecondary
                        )
                    }

                    // Fleet health indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFD1FAE5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "FLEET 96% OK",
                            color = Color(0xFF065F46),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = statusFilter == null,
                            onClick = { statusFilter = null },
                            label = { Text("All Drivers (${riders.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == RiderStatus.AVAILABLE,
                            onClick = { statusFilter = RiderStatus.AVAILABLE },
                            label = { Text("Available ($availableCount)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusSuccess,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == RiderStatus.ON_DELIVERY,
                            onClick = { statusFilter = RiderStatus.ON_DELIVERY },
                            label = { Text("On Delivery ($onDeliveryCount)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == RiderStatus.OFFLINE,
                            onClick = { statusFilter = RiderStatus.OFFLINE },
                            label = { Text("Offline", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF64748B),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Fleet List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredRiders, key = { it.id }) { rider ->
                RiderItemCard(
                    rider = rider,
                    onViewOnMap = { onViewRiderOnMap(rider.id) },
                    onToggleStatus = {
                        val newStatus = if (rider.status == RiderStatus.AVAILABLE) RiderStatus.OFFLINE else RiderStatus.AVAILABLE
                        onToggleStatus(rider.id, newStatus)
                    }
                )
            }
        }
    }
}

@Composable
fun RiderItemCard(
    rider: Rider,
    onViewOnMap: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LogisticsBorder, RoundedCornerShape(16.dp))
            .testTag("rider_card_${rider.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Vehicle Icon, Name, Rating, Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (rider.status) {
                                    RiderStatus.AVAILABLE -> Color(0xFFD1FAE5)
                                    RiderStatus.ON_DELIVERY -> Color(0xFFFFEDD5)
                                    RiderStatus.OFFLINE -> Color(0xFFF1F5F9)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (rider.vehicle) {
                                VehicleType.E_BIKE -> Icons.Default.ElectricBike
                                VehicleType.SCOOTER -> Icons.Default.ElectricScooter
                                VehicleType.MOTORCYCLE -> Icons.Default.ElectricMoped
                                VehicleType.CARGO_VAN -> Icons.Default.LocalShipping
                            },
                            contentDescription = null,
                            tint = when (rider.status) {
                                RiderStatus.AVAILABLE -> StatusSuccess
                                RiderStatus.ON_DELIVERY -> AccentOrange
                                RiderStatus.OFFLINE -> Color(0xFF64748B)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rider.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SlateTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "★ ${rider.rating}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                        Text(
                            text = "${rider.vehicle.label} • #${rider.id}",
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }
                }

                // Status Pill
                val (statusBg, statusFg) = when (rider.status) {
                    RiderStatus.AVAILABLE -> Pair(Color(0xFFD1FAE5), StatusSuccess)
                    RiderStatus.ON_DELIVERY -> Pair(Color(0xFFFFEDD5), AccentOrange)
                    RiderStatus.OFFLINE -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = rider.status.label.uppercase(),
                        color = statusFg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Table / Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Current Location", fontSize = 10.sp, color = SlateTextMuted)
                    Text("Grid (${rider.location.x.toInt()}, ${rider.location.y.toInt()})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
                }

                Column {
                    Text("Completed Today", fontSize = 10.sp, color = SlateTextMuted)
                    Text("${rider.completedToday} trips", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }

                Column {
                    Text("Battery / Fuel", fontSize = 10.sp, color = SlateTextMuted)
                    Text("${rider.batteryPct}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (rider.batteryPct < 30) Color(0xFFEF4444) else StatusSuccess)
                }

                Column {
                    Text("Active Load", fontSize = 10.sp, color = SlateTextMuted)
                    Text("${rider.currentLoadCount}/${rider.vehicle.maxLoad}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
                }
            }

            // Current Active Order info if busy
            if (rider.currentOrderId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Job: #${rider.currentOrderId}",
                        fontSize = 11.sp,
                        color = DeepBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "En route to delivery",
                        fontSize = 11.sp,
                        color = DeepBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions: View on Live Map & Status Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewOnMap,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("view_rider_map_${rider.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = DeepBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View on Map", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }

                if (rider.status != RiderStatus.ON_DELIVERY) {
                    Button(
                        onClick = onToggleStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (rider.status == RiderStatus.AVAILABLE) Color(0xFF64748B) else StatusSuccess
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (rider.status == RiderStatus.AVAILABLE) "Set Offline" else "Set Online",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
