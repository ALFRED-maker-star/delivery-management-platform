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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.DeliveryStatus
import com.example.data.model.DeliveryUrgency
import com.example.data.model.DispatchStats
import com.example.data.model.Rider
import com.example.data.model.RiderStatus
import com.example.ui.components.MetricCard
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.LogisticsBg
import com.example.ui.theme.LogisticsBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    stats: DispatchStats,
    orders: List<DeliveryOrder>,
    riders: List<Rider>,
    isSimulationActive: Boolean,
    onAutoAssignAll: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToLiveMap: () -> Unit,
    onToggleSimulation: () -> Unit,
    onCreateSampleOrder: (customer: String, pickup: String, dropoff: String, item: String, urgency: DeliveryUrgency) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateOrderModal by remember { mutableStateOf(false) }

    val pendingOrders = remember(orders) {
        orders.filter { it.status == DeliveryStatus.UNASSIGNED }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LogisticsBg)
            .padding(16.dp)
            .testTag("admin_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Action Command Strip
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepBlue),
                shape = RoundedCornerShape(16.dp),
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
                                text = "FLEET DISPATCH HUB",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Central Operations Active",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Simulation live toggle
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSimulationActive) StatusSuccess else Color(0xFF64748B),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onToggleSimulation() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSimulationActive) "LIVE GPS ON" else "SIM PAUSED",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAutoAssignAll,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("auto_assign_all_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-Assign All (${pendingOrders.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showCreateOrderModal = true },
                            modifier = Modifier
                                .weight(0.7f)
                                .height(44.dp)
                                .testTag("create_order_dialog_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New Job",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 4 Primary Required Stat Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Active Deliveries",
                    value = "${stats.activeDeliveries}",
                    icon = Icons.Default.LocalShipping,
                    accentColor = AccentOrange,
                    trendText = "4 in transit now",
                    modifier = Modifier.weight(1f),
                    testTag = "card_active_deliveries"
                )
                MetricCard(
                    title = "Available Riders",
                    value = "${stats.availableRiders}",
                    icon = Icons.Default.TwoWheeler,
                    accentColor = StatusSuccess,
                    trendText = "${stats.totalRiders} total registered",
                    modifier = Modifier.weight(1f),
                    testTag = "card_available_riders"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Avg Delivery Time",
                    value = "${stats.avgDeliveryTimeMinutes}m",
                    icon = Icons.Default.Timer,
                    accentColor = DeepBlue,
                    trendText = "-3.2m vs yesterday",
                    isPositiveTrend = true,
                    modifier = Modifier.weight(1f),
                    testTag = "card_avg_delivery_time"
                )
                MetricCard(
                    title = "Orders Pending",
                    value = "${stats.pendingOrdersCount}",
                    icon = Icons.Default.PendingActions,
                    accentColor = if (stats.pendingOrdersCount > 0) Color(0xFFEF4444) else StatusSuccess,
                    trendText = if (stats.pendingOrdersCount > 0) "Needs assignment" else "Queue clear",
                    isPositiveTrend = stats.pendingOrdersCount == 0,
                    modifier = Modifier.weight(1f),
                    testTag = "card_orders_pending"
                )
            }
        }

        // Live Radar Map preview teaser
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LogisticsBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLiveMap() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Live Fleet Radar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "${riders.filter { it.status == RiderStatus.ON_DELIVERY }.size} active routes streaming live",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "View Map",
                        tint = DeepBlue
                    )
                }
            }
        }

        // Orders Pending Assignment preview list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Awaiting Rider Assignment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateTextPrimary
                )
                TextButton(onClick = onNavigateToQueue) {
                    Text("View All Queue", color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (pendingOrders.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("All orders are currently dispatched!", color = StatusSuccess, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(pendingOrders.take(3).size) { idx ->
                val order = pendingOrders[idx]
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LogisticsBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToQueue() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${order.id}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DeepBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFFEDD5))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = order.urgency.label,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentOrange
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${order.pickupName} → ${order.dropoffName}",
                                fontSize = 12.sp,
                                color = SlateTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${String.format("%.2f", order.payout)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "Unassigned",
                                fontSize = 11.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Modal to create sample order
    if (showCreateOrderModal) {
        var custName by remember { mutableStateOf("David Miller") }
        var pickup by remember { mutableStateOf("Roast & Co Cafe") }
        var dropoff by remember { mutableStateOf("Tech Campus Building 4") }
        var itemType by remember { mutableStateOf("Hot Meals & Beverages") }
        var urgency by remember { mutableStateOf(DeliveryUrgency.EXPRESS) }

        AlertDialog(
            onDismissRequest = { showCreateOrderModal = false },
            title = {
                Text(
                    text = "Dispatch New Delivery Job",
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = custName,
                        onValueChange = { custName = it },
                        label = { Text("Recipient Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pickup,
                        onValueChange = { pickup = it },
                        label = { Text("Pickup Location") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dropoff,
                        onValueChange = { dropoff = it },
                        label = { Text("Drop-off Destination") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = itemType,
                        onValueChange = { itemType = it },
                        label = { Text("Cargo Description") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateSampleOrder(custName, pickup, dropoff, itemType, urgency)
                        showCreateOrderModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Queue Job")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateOrderModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
