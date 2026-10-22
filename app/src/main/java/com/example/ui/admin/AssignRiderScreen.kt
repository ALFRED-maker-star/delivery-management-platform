package com.example.ui.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.ElectricMoped
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.data.model.DeliveryOrder
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
import kotlin.math.hypot

@Composable
fun AssignRiderScreen(
    order: DeliveryOrder?,
    riders: List<Rider>,
    onConfirmAssignment: (orderId: String, riderId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRiderId by remember { mutableStateOf(riders.firstOrNull { it.status == RiderStatus.AVAILABLE }?.id ?: riders.firstOrNull()?.id) }

    if (order == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(LogisticsBg),
            contentAlignment = Alignment.Center
        ) {
            Text("No order selected for assignment", color = SlateTextMuted)
        }
        return
    }

    // Rank riders by distance to order pickup
    val rankedRiders = remember(riders, order) {
        riders.sortedBy { r ->
            val dx = r.location.x - order.pickupLocation.x
            val dy = r.location.y - order.pickupLocation.y
            hypot(dx, dy)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LogisticsBg)
            .testTag("assign_rider_screen")
    ) {
        // Header
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SlateTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Assign Rider to #${order.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = "Select closest available driver for pickup",
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Order Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LogisticsBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ORDER DETAILS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepBlue,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$${String.format("%.2f", order.payout)} payout",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = SlateTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Pickup: ${order.pickupName} (${order.pickupAddress})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Drop-off: ${order.dropoffName} (${order.dropoffAddress})",
                            fontSize = 13.sp,
                            color = SlateTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Cargo: ${order.itemType}",
                                fontSize = 11.sp,
                                color = SlateTextMuted
                            )
                            Text(
                                text = "Distance: ${order.distanceKm} km",
                                fontSize = 11.sp,
                                color = SlateTextMuted
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Nearby Available Fleet (${rankedRiders.size} drivers)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SlateTextPrimary
                )
            }

            // Riders list
            items(rankedRiders, key = { it.id }) { rider ->
                val isSelected = selectedRiderId == rider.id
                val distanceEst = remember(rider, order) {
                    val dist = hypot(rider.location.x - order.pickupLocation.x, rider.location.y - order.pickupLocation.y) / 140.0
                    String.format("%.1f", dist.coerceAtLeast(0.4))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedRiderId = rider.id }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) DeepBlue else LogisticsBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .testTag("rider_candidate_${rider.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedRiderId = rider.id },
                            colors = RadioButtonDefaults.colors(selectedColor = DeepBlue)
                        )

                        // Vehicle Icon Badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) DeepBlue else Color(0xFFF1F5F9)),
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
                                tint = if (isSelected) Color.White else DeepBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = rider.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
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

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${rider.vehicle.label} • $distanceEst km to pickup",
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Load: ${rider.currentLoadCount}/${rider.vehicle.maxLoad}",
                                    fontSize = 11.sp,
                                    color = if (rider.currentLoadCount >= rider.vehicle.maxLoad) Color(0xFFEF4444) else SlateTextMuted
                                )
                                Text(
                                    text = "Battery: ${rider.batteryPct}%",
                                    fontSize = 11.sp,
                                    color = SlateTextMuted
                                )
                                Text(
                                    text = rider.status.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rider.status == RiderStatus.AVAILABLE) StatusSuccess else AccentOrange
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Confirmation Action Bar
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val rId = selectedRiderId
                        if (rId != null) {
                            onConfirmAssignment(order.id, rId)
                        }
                    },
                    enabled = selectedRiderId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_assign_rider_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm Assignment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
