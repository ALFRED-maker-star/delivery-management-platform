package com.example.ui.rider

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.DailyEarning
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.Rider
import com.example.ui.components.MetricCard
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.LogisticsBg
import com.example.ui.theme.LogisticsBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusSuccess

@Composable
fun RiderEarningsScreen(
    currentRider: Rider?,
    allOrders: List<DeliveryOrder>,
    weeklyEarnings: List<DailyEarning>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Daily, 1: Weekly

    val completedDeliveries = remember(allOrders, currentRider) {
        allOrders.filter { it.status == DeliveryStatus.DELIVERED }
    }

    val totalEarnings = if (selectedTab == 0) {
        currentRider?.todayEarnings ?: 158.50
    } else {
        weeklyEarnings.sumOf { it.amount }
    }

    val completedCount = if (selectedTab == 0) {
        currentRider?.completedToday ?: 9
    } else {
        weeklyEarnings.sumOf { it.tripCount }
    }

    val basePay = totalEarnings * 0.74
    val tips = totalEarnings * 0.18
    val bonuses = totalEarnings * 0.08

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LogisticsBg)
            .padding(16.dp)
            .testTag("rider_earnings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily / Weekly Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = DeepBlue,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, LogisticsBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Today's Summary",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_daily_earnings")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Weekly Breakdown",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_weekly_earnings")
                )
            }
        }

        // Hero Earnings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepBlue),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (selectedTab == 0) "TOTAL NET EARNINGS (TODAY)" else "WEEKLY NET EARNINGS",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$${String.format("%.2f", totalEarnings)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Breakdown pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Base Rate", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("$${String.format("%.2f", basePay)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Tips", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("$${String.format("%.2f", tips)}", color = StatusSuccess, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Rush Bonus", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("$${String.format("%.2f", bonuses)}", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Key Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Trips Finished",
                    value = "$completedCount",
                    icon = Icons.Default.CheckCircle,
                    accentColor = DeepBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Active Online",
                    value = if (selectedTab == 0) "5.8 hrs" else "38.2 hrs",
                    icon = Icons.Default.Timer,
                    accentColor = AccentOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly Bar Chart (shown in Weekly tab)
        if (selectedTab == 1) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LogisticsBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Daily Trend (Mon - Sun)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlateTextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val maxAmount = weeklyEarnings.maxOfOrNull { it.amount } ?: 220.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyEarnings.forEach { day ->
                                val barHeightFraction = (day.amount / maxAmount).toFloat().coerceIn(0.15f, 1f)
                                val isToday = day.dayLabel.contains("Sun")

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "$${day.amount.toInt()}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) AccentOrange else SlateTextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .fillMaxHeight(barHeightFraction)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(if (isToday) AccentOrange else DeepBlue)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = day.dayLabel.take(3),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isToday) AccentOrange else SlateTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Completed Deliveries List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completed Deliveries",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateTextPrimary
                )
                Text(
                    text = "${completedDeliveries.size} records",
                    fontSize = 12.sp,
                    color = SlateTextMuted
                )
            }
        }

        // Completed Deliveries items
        items(completedDeliveries, key = { it.id }) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LogisticsBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1FAE5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "#${order.id} • ${order.dropoffName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "${order.pickupName} → ${order.distanceKm} km",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+$${String.format("%.2f", order.totalEarnings)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = StatusSuccess
                        )
                        Text(
                            text = "Delivered",
                            fontSize = 10.sp,
                            color = SlateTextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
