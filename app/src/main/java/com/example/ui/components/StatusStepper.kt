package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryStatus
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.StatusSuccess

@Composable
fun StatusStepper(
    currentStatus: DeliveryStatus,
    modifier: Modifier = Modifier
) {
    // Steps: 1. Picked Up, 2. En Route, 3. Delivered
    val steps = listOf(
        Pair(DeliveryStatus.PICKED_UP, "Picked Up"),
        Pair(DeliveryStatus.EN_ROUTE, "En Route"),
        Pair(DeliveryStatus.DELIVERED, "Delivered")
    )

    val currentStepIndex = when (currentStatus) {
        DeliveryStatus.UNASSIGNED, DeliveryStatus.ASSIGNED -> 0
        DeliveryStatus.PICKED_UP -> 0
        DeliveryStatus.EN_ROUTE -> 1
        DeliveryStatus.DELIVERED -> 2
        DeliveryStatus.CANCELLED -> -1
    }

    val isStepCompleted: (Int) -> Boolean = { idx ->
        when (currentStatus) {
            DeliveryStatus.DELIVERED -> true
            DeliveryStatus.EN_ROUTE -> idx <= 0
            else -> false
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_stepper"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, (status, label) ->
            val completed = isStepCompleted(index)
            val isCurrent = currentStepIndex == index && !completed

            val circleColor by animateColorAsState(
                targetValue = when {
                    completed -> StatusSuccess
                    isCurrent -> AccentOrange
                    else -> Color(0xFFE2E8F0)
                },
                label = "stepColor"
            )

            val textColor = when {
                completed -> StatusSuccess
                isCurrent -> AccentOrange
                else -> Color(0xFF94A3B8)
            }

            // Step item
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(circleColor)
                        .then(
                            if (isCurrent) Modifier.border(2.dp, Color.White, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = if (isCurrent) Color.White else Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isCurrent || completed) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }

            // Connecting line between steps
            if (index < steps.size - 1) {
                val lineColor = if (completed || (isCurrent && index < currentStepIndex)) {
                    AccentOrange
                } else {
                    Color(0xFFE2E8F0)
                }
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .weight(0.8f)
                        .padding(bottom = 16.dp)
                        .background(lineColor, CircleShape)
                )
            }
        }
    }
}
