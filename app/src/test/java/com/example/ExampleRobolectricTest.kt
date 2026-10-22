package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DeliveryStatus
import com.example.data.model.RiderStatus
import com.example.data.repository.DispatchRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DispatchGo", appName)
  }

  @Test
  fun `verify initial dispatch orders and riders`() {
    val repo = DispatchRepository.getInstance()
    val orders = repo.orders.value
    val riders = repo.riders.value

    assertTrue("Orders should be initialized", orders.isNotEmpty())
    assertTrue("Riders should be initialized", riders.isNotEmpty())

    val unassigned = orders.filter { it.status == DeliveryStatus.UNASSIGNED }
    assertTrue("Should have unassigned orders available", unassigned.isNotEmpty())
  }

  @Test
  fun `rider accept order workflow`() {
    val repo = DispatchRepository.getInstance()
    val unassignedOrder = repo.orders.value.firstOrNull { it.status == DeliveryStatus.UNASSIGNED }
    assertNotNull("Must have an unassigned order", unassignedOrder)

    val targetId = unassignedOrder!!.id
    val accepted = repo.acceptOrder(targetId, "RDR-101")
    assertTrue("Order accept must return true", accepted)

    val updatedOrder = repo.orders.value.find { it.id == targetId }
    assertEquals(DeliveryStatus.ASSIGNED, updatedOrder?.status)
    assertEquals("RDR-101", updatedOrder?.assignedRiderId)
  }
}
