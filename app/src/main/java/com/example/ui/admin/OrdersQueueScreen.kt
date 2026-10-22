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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.DeliveryStatus
import com.example.data.model.DeliveryUrgency
import com.example.data.model.Rider
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.LogisticsBg
import com.example.ui.theme.LogisticsBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusSuccess

@Composable
fun OrdersQueueScreen(
    orders: List<DeliveryOrder>,
    riders: List<Rider>,
    onManualAssign: (orderId: String) -> Unit,
    onAutoAssignOrder: (orderId: String) -> Unit,
    onAutoAssignAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<DeliveryStatus?>(null) } // null = All

    val filteredOrders = remember(orders, searchQuery, statusFilter) {
        orders.filter { order ->
            val matchesFilter = when (statusFilter) {
                null -> true
                DeliveryStatus.UNASSIGNED -> order.status == DeliveryStatus.UNASSIGNED
                DeliveryStatus.EN_ROUTE -> order.status == DeliveryStatus.EN_ROUTE || order.status == DeliveryStatus.PICKED_UP || order.status == DeliveryStatus.ASSIGNED
                DeliveryStatus.DELIVERED -> order.status == DeliveryStatus.DELIVERED
                else -> order.status == statusFilter
            }
            val matchesSearch = searchQuery.isBlank() ||
                order.id.contains(searchQuery, ignoreCase = true) ||
                order.customerName.contains(searchQuery, ignoreCase = true) ||
                order.pickupName.contains(searchQuery, ignoreCase = true) ||
                order.dropoffName.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    val unassignedCount = orders.count { it.status == DeliveryStatus.UNASSIGNED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LogisticsBg)
            .testTag("orders_queue_screen")
    ) {
        // Top Queue Action Header
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
                            text = "Orders Dispatch Queue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = "$unassignedCount orders awaiting rider assignment",
                            fontSize = 12.sp,
                            color = if (unassignedCount > 0) AccentOrange else StatusSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (unassignedCount > 0) {
                        Button(
                            onClick = onAutoAssignAll,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("batch_auto_assign_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-Assign All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Order ID, merchant, customer...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = SlateTextMuted)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("orders_queue_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = statusFilter == null,
                            onClick = { statusFilter = null },
                            label = { Text("All Orders (${orders.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == DeliveryStatus.UNASSIGNED,
                            onClick = { statusFilter = DeliveryStatus.UNASSIGNED },
                            label = { Text("Pending (${unassignedCount})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == DeliveryStatus.EN_ROUTE,
                            onClick = { statusFilter = DeliveryStatus.EN_ROUTE },
                            label = { Text("In-Transit", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == DeliveryStatus.DELIVERED,
                            onClick = { statusFilter = DeliveryStatus.DELIVERED },
                            label = { Text("Delivered", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusSuccess,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Orders List
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No orders match the current filters.", color = SlateTextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    QueueOrderCard(
                        order = order,
                        onManualAssign = { onManualAssign(order.id) },
                        onAutoAssign = { onAutoAssignOrder(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun QueueOrderCard(
    order: DeliveryOrder,
    onManualAssign: () -> Unit,
    onAutoAssign: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LogisticsBorder, RoundedCornerShape(16.dp))
            .testTag("queue_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order ID, Waiting Time, Urgency Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.id}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = DeepBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (order.urgency) {
                                    DeliveryUrgency.EXPRESS -> Color(0xFFFFEDD5)
                                    DeliveryUrgency.PRIORITY -> Color(0xFFFEF3C7)
                                    DeliveryUrgency.STANDARD -> Color(0xFFF1F5F9)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.urgency.label.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (order.urgency) {
                                DeliveryUrgency.EXPRESS -> AccentOrange
                                DeliveryUrgency.PRIORITY -> Color(0xFFD97706)
                                DeliveryUrgency.STANDARD -> SlateTextSecondary
                            }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = SlateTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${order.createdAtMinutesAgo}m ago",
                        fontSize = 11.sp,
                        color = SlateTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pickup & Destination info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).background(AccentOrange, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.pickupName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SlateTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).background(DeepBlue, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${order.dropoffName} (${order.customerName})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SlateTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row (Distance, Payout, Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${order.distanceKm} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextSecondary
                    )
                    Text(
                        text = "$${String.format("%.2f", order.payout)} payout",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue
                    )
                }

                // Rider Assignment Status
                if (order.assignedRiderName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(StatusSuccess, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rider: ${order.assignedRiderName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                    }
                } else {
                    Text(
                        text = "● UNASSIGNED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            // Action Buttons for Unassigned Orders
            if (order.status == DeliveryStatus.UNASSIGNED) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAutoAssign,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("auto_assign_btn_${order.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-Assign",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentOrange
                        )
                    }

                    Button(
                        onClick = onManualAssign,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("manual_assign_btn_${order.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentInd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Assign Rider",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
