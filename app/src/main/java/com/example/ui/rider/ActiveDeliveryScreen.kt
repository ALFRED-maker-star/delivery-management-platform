package com.example.ui.rider

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.Rider
import com.example.ui.components.LogisticsMap
import com.example.ui.components.StatusStepper
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.DeepBlueDark
import com.example.ui.theme.LogisticsBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusSuccess

@Composable
fun ActiveDeliveryScreen(
    order: DeliveryOrder?,
    currentRider: Rider?,
    onAdvanceStatus: (orderId: String, currentStatus: DeliveryStatus) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeliveryConfirmDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    if (order == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No active delivery in progress",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("View Available Deliveries")
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_delivery_screen")
    ) {
        // 1. Full Screen Interactive Map
        LogisticsMap(
            modifier = Modifier.fillMaxSize(),
            activeOrder = order,
            riders = if (currentRider != null) listOf(currentRider) else emptyList(),
            selectedRiderId = currentRider?.id
        )

        // 2. Top Navigation Guidance Card
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
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (order.status == DeliveryStatus.EN_ROUTE) "In 180m, Turn Right onto Bay Plaza"
                                   else "Proceed to ${order.pickupName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ETA: ${order.estimatedMinutes} mins",
                                color = AccentOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  Speed: 23 km/h",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Call customer quick icon
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${order.customerPhone}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DeepBlue)
                            .size(36.dp)
                            .testTag("active_call_customer_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Customer",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 3. Bottom Persistent Logistics Sheet
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Stepper: Picked Up → En Route → Delivered
                StatusStepper(currentStatus = order.status)

                Spacer(modifier = Modifier.height(14.dp))

                // Target Destination Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (order.status == DeliveryStatus.PICKED_UP || order.status == DeliveryStatus.ASSIGNED)
                                "PICKUP: ${order.pickupName}"
                                else "DELIVERY: ${order.dropoffName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = if (order.status == DeliveryStatus.PICKED_UP || order.status == DeliveryStatus.ASSIGNED)
                                order.pickupAddress else order.dropoffAddress,
                            fontSize = 12.sp,
                            color = SlateTextSecondary,
                            maxLines = 1
                        )
                    }

                    // Customer Call Icon Button with name
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${order.customerPhone}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF))
                            .size(40.dp)
                            .testTag("call_customer_button_bottom")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Customer",
                            tint = DeepBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Report Issue Button
                    OutlinedButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier
                            .weight(0.35f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = "Report",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Primary Step Transition Button
                    val (actionText, actionColor) = when (order.status) {
                        DeliveryStatus.ASSIGNED -> Pair("Confirm Picked Up", AccentOrange)
                        DeliveryStatus.PICKED_UP -> Pair("Start Transit (En Route)", DeepBlue)
                        DeliveryStatus.EN_ROUTE -> Pair("Mark Delivered", StatusSuccess)
                        DeliveryStatus.DELIVERED -> Pair("Completed", StatusSuccess)
                        else -> Pair("Next Status", AccentOrange)
                    }

                    Button(
                        onClick = {
                            if (order.status == DeliveryStatus.EN_ROUTE) {
                                showDeliveryConfirmDialog = true
                            } else {
                                onAdvanceStatus(order.id, order.status)
                            }
                        },
                        modifier = Modifier
                            .weight(0.65f)
                            .height(48.dp)
                            .testTag("mark_delivered_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = actionColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = actionText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Delivery confirmation dialog
    if (showDeliveryConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeliveryConfirmDialog = false },
            title = {
                Text(
                    text = "Confirm Delivery Handoff",
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure package #${order.id} has been delivered to ${order.customerName} at ${order.dropoffAddress}?",
                        fontSize = 13.sp,
                        color = SlateTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Payout: $${String.format("%.2f", order.totalEarnings)} will be credited to your account.",
                        fontWeight = FontWeight.Bold,
                        color = StatusSuccess,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeliveryConfirmDialog = false
                        onAdvanceStatus(order.id, order.status)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                    modifier = Modifier.testTag("confirm_delivery_modal_button")
                ) {
                    Text("Confirm Delivered")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Report delay dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Transit Issue") },
            text = {
                Text(
                    "Notify Central Dispatch of heavy traffic, road closure, or recipient unreachable for order #${order.id}."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showReportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Alert Dispatch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}
