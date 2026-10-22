package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.ElectricMoped
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.GeoPoint
import com.example.data.model.Rider
import com.example.data.model.RiderStatus
import com.example.data.model.VehicleType
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.MapDarkBg
import com.example.ui.theme.MapGridLine
import com.example.ui.theme.MapRoadPrimary
import com.example.ui.theme.MapRoadSecondary
import com.example.ui.theme.MapRouteGlow
import com.example.ui.theme.MapRouteLine
import com.example.ui.theme.StatusSuccess
import kotlin.math.hypot

@Composable
fun LogisticsMap(
    modifier: Modifier = Modifier,
    activeOrder: DeliveryOrder? = null,
    riders: List<Rider> = emptyList(),
    selectedRiderId: String? = null,
    showAllRoutes: Boolean = false,
    allOrders: List<DeliveryOrder> = emptyList(),
    onRiderClick: ((Rider) -> Unit)? = null,
    onRecenterClick: (() -> Unit)? = null,
    isInteractive: Boolean = true
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        if (isInteractive) {
            scale = (scale * zoomChange).coerceIn(0.7f, 3.5f)
            offset += offsetChange
        }
    }

    // Radar pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MapDarkBg)
            .testTag("logistics_map_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .pointerInput(isInteractive, riders) {
                    if (isInteractive && onRiderClick != null) {
                        detectTapGestures { tapOffset ->
                            // Hit test riders
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            val clickedRider = riders.find { rider ->
                                val riderCanvasX = (rider.location.x / 1000f) * canvasWidth * scale + offset.x
                                val riderCanvasY = (rider.location.y / 1000f) * canvasHeight * scale + offset.y
                                val dist = hypot(tapOffset.x - riderCanvasX, tapOffset.y - riderCanvasY)
                                dist <= 40f * scale
                            }
                            if (clickedRider != null) {
                                onRiderClick(clickedRider)
                            }
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Helper coordinate conversion
            fun mapToCanvas(point: GeoPoint): Offset {
                val cx = (point.x / 1000f) * canvasW * scale + offset.x
                val cy = (point.y / 1000f) * canvasH * scale + offset.y
                return Offset(cx, cy)
            }

            // 1. Draw City Grid & Waterway
            drawCityBackground(canvasW, canvasH, scale, offset)

            // 2. Draw Delivery Routes
            if (activeOrder != null && activeOrder.routeWaypoints.isNotEmpty()) {
                drawRoutePolyline(
                    waypoints = activeOrder.routeWaypoints,
                    mapToCanvas = ::mapToCanvas,
                    isHighPriority = true
                )
            } else if (showAllRoutes) {
                allOrders.filter { it.status == DeliveryStatus.EN_ROUTE || it.status == DeliveryStatus.PICKED_UP }
                    .forEach { order ->
                        if (order.routeWaypoints.isNotEmpty()) {
                            drawRoutePolyline(
                                waypoints = order.routeWaypoints,
                                mapToCanvas = ::mapToCanvas,
                                isHighPriority = false
                            )
                        }
                    }
            }

            // 3. Draw Pickup and Dropoff Markers
            if (activeOrder != null) {
                val pickupPos = mapToCanvas(activeOrder.pickupLocation)
                val dropoffPos = mapToCanvas(activeOrder.dropoffLocation)

                // Pickup pin (Orange)
                drawMarkerPin(
                    position = pickupPos,
                    color = AccentOrange,
                    label = "PICKUP",
                    subtext = activeOrder.pickupName
                )

                // Dropoff pin (Deep Blue)
                drawMarkerPin(
                    position = dropoffPos,
                    color = DeepBlue,
                    label = "DROP-OFF",
                    subtext = activeOrder.dropoffName
                )
            } else if (showAllRoutes) {
                allOrders.filter { it.status == DeliveryStatus.UNASSIGNED || it.status == DeliveryStatus.EN_ROUTE }
                    .take(6)
                    .forEach { o ->
                        val pPos = mapToCanvas(o.pickupLocation)
                        val dPos = mapToCanvas(o.dropoffLocation)
                        drawMarkerDot(pPos, AccentOrange, "P")
                        drawMarkerDot(dPos, DeepBlue, "D")
                    }
            }

            // 4. Draw Fleet Riders with Beacons
            riders.forEach { rider ->
                val riderPos = mapToCanvas(rider.location)
                val isSelected = rider.id == selectedRiderId
                val isBusy = rider.status == RiderStatus.ON_DELIVERY

                // Radar pulse for active/selected rider
                if (isSelected || isBusy) {
                    val maxRadius = 38f * scale
                    val currentRadius = maxRadius * pulseFraction
                    val alpha = (1f - pulseFraction).coerceIn(0f, 1f) * 0.7f

                    val pulseColor = if (isSelected) AccentOrange else Color(0xFF38BDF8)
                    drawCircle(
                        color = pulseColor.copy(alpha = alpha),
                        radius = currentRadius,
                        center = riderPos
                    )
                }

                // Rider Vehicle Marker
                val markerBg = when (rider.status) {
                    RiderStatus.AVAILABLE -> StatusSuccess
                    RiderStatus.ON_DELIVERY -> AccentOrange
                    RiderStatus.OFFLINE -> Color(0xFF64748B)
                }

                // Outer halo
                drawCircle(
                    color = Color.White,
                    radius = 16f * scale,
                    center = riderPos
                )
                // Main circle
                drawCircle(
                    color = markerBg,
                    radius = 13f * scale,
                    center = riderPos
                )

                // Heading arrow indicator
                val headingRad = Math.toRadians((rider.headingDegrees - 90.0)).toFloat()
                val pointerLength = 22f * scale
                val tipX = riderPos.x + kotlin.math.cos(headingRad) * pointerLength
                val tipY = riderPos.y + kotlin.math.sin(headingRad) * pointerLength
                drawLine(
                    color = Color.White,
                    start = riderPos,
                    end = Offset(tipX, tipY),
                    strokeWidth = 3f * scale,
                    cap = StrokeCap.Round
                )

                // Selection highlight ring
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = 22f * scale,
                        center = riderPos,
                        style = Stroke(width = 3f * scale)
                    )
                }
            }
        }

        // Overlay Map Controls (Zoom +, Zoom -, Recenter, Telemetry Tag)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Live Status Tag
            Surface(
                color = Color(0xCC0F172A),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(StatusSuccess, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE GPS • 5G TELEMETRY",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Map Zoom Controls
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xDD1E293B)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3364748B))
            ) {
                Column {
                    IconButton(
                        onClick = { scale = (scale * 1.25f).coerceAtMost(3.5f) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { scale = (scale / 1.25f).coerceAtLeast(0.7f) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                            onRecenterClick?.invoke()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter Map",
                            tint = AccentOrange
                        )
                    }
                }
            }
        }

        // Map Legend at bottom
        Surface(
            color = Color(0xDD0F172A),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(AccentOrange, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pickup", color = Color(0xFFCBD5E1), fontSize = 10.sp)

                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(DeepBlue, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Drop-off", color = Color(0xFFCBD5E1), fontSize = 10.sp)

                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(StatusSuccess, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Available", color = Color(0xFFCBD5E1), fontSize = 10.sp)
            }
        }
    }
}

private fun DrawScope.drawCityBackground(width: Float, height: Float, scale: Float, offset: Offset) {
    // Waterway / Bay curve across upper right
    val waterPath = Path().apply {
        moveTo(width * 0.7f * scale + offset.x, 0f)
        cubicTo(
            width * 0.65f * scale + offset.x, height * 0.25f * scale + offset.y,
            width * 0.85f * scale + offset.x, height * 0.45f * scale + offset.y,
            width * scale + offset.x, height * 0.5f * scale + offset.y
        )
        lineTo(width * scale + offset.x, 0f)
        close()
    }
    drawPath(path = waterPath, color = Color(0xFF0F1E36))

    // City Park block
    val parkX = width * 0.15f * scale + offset.x
    val parkY = height * 0.5f * scale + offset.y
    val parkW = width * 0.18f * scale
    val parkH = height * 0.16f * scale
    drawRoundRect(
        color = Color(0xFF132A26),
        topLeft = Offset(parkX, parkY),
        size = Size(parkW, parkH),
        cornerRadius = CornerRadius(12f, 12f)
    )

    // Arterial Boulevards (wide avenues)
    val avenueColor = MapRoadSecondary
    val streetColor = MapRoadPrimary

    // Avenue 1: Diagonal
    drawLine(
        color = avenueColor,
        start = Offset(0f, height * 0.3f * scale + offset.y),
        end = Offset(width * scale + offset.x, height * 0.7f * scale + offset.y),
        strokeWidth = 14f * scale
    )

    // Avenue 2: Cross diagonal
    drawLine(
        color = avenueColor,
        start = Offset(width * 0.2f * scale + offset.x, 0f),
        end = Offset(width * 0.8f * scale + offset.x, height * scale + offset.y),
        strokeWidth = 12f * scale
    )

    // Grid streets (Horizontal)
    for (i in 1..8) {
        val y = (height * (i * 0.11f)) * scale + offset.y
        drawLine(
            color = streetColor,
            start = Offset(0f, y),
            end = Offset(width * scale + offset.x, y),
            strokeWidth = 6f * scale
        )
    }

    // Grid streets (Vertical)
    for (i in 1..8) {
        val x = (width * (i * 0.11f)) * scale + offset.x
        drawLine(
            color = streetColor,
            start = Offset(x, 0f),
            end = Offset(x, height * scale + offset.y),
            strokeWidth = 6f * scale
        )
    }
}

private fun DrawScope.drawRoutePolyline(
    waypoints: List<GeoPoint>,
    mapToCanvas: (GeoPoint) -> Offset,
    isHighPriority: Boolean
) {
    if (waypoints.size < 2) return

    val path = Path().apply {
        val first = mapToCanvas(waypoints[0])
        moveTo(first.x, first.y)
        for (i in 1 until waypoints.size) {
            val pt = mapToCanvas(waypoints[i])
            lineTo(pt.x, pt.y)
        }
    }

    // Outer Glow
    drawPath(
        path = path,
        color = if (isHighPriority) MapRouteGlow else Color(0x3338BDF8),
        style = Stroke(
            width = 16f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Core Route Line
    drawPath(
        path = path,
        color = if (isHighPriority) MapRouteLine else Color(0xFF38BDF8),
        style = Stroke(
            width = 6f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Directional dashes
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.8f),
        style = Stroke(
            width = 2.5f,
            cap = StrokeCap.Round,
            pathEffect = dashEffect
        )
    )
}

private fun DrawScope.drawMarkerPin(
    position: Offset,
    color: Color,
    label: String,
    subtext: String
) {
    // Pin base shadow
    drawCircle(
        color = Color(0x66000000),
        radius = 12f,
        center = Offset(position.x, position.y + 14f)
    )

    // Outer marker circle
    drawCircle(
        color = Color.White,
        radius = 18f,
        center = position
    )

    // Inner filled color
    drawCircle(
        color = color,
        radius = 14f,
        center = position
    )

    // Center dot
    drawCircle(
        color = Color.White,
        radius = 5f,
        center = position
    )
}

private fun DrawScope.drawMarkerDot(
    position: Offset,
    color: Color,
    text: String
) {
    drawCircle(
        color = Color.White,
        radius = 10f,
        center = position
    )
    drawCircle(
        color = color,
        radius = 8f,
        center = position
    )
}
