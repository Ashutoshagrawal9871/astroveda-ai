package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.astro.VedicEngine
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
    assertEquals("Jyotish AI", appName)
  }

  @Test
  fun `verify vedic kundli calculation and ashtakoota matching`() {
    val kundli = VedicEngine.calculateKundli(
      name = "Arjun",
      birthDate = "1995-10-24",
      birthTime = "06:45",
      place = "New Delhi, India",
      isTimeApproximate = false
    )

    assertNotNull(kundli)
    val safeKundli = kundli!!
    assertEquals("Arjun", safeKundli.personName)
    assertEquals(9, safeKundli.planets.size)
    assertEquals(12, safeKundli.houses.size)

    val compatibility = VedicEngine.calculateCompatibility(
      boyName = "Arjun",
      boyMoonSignId = 1,
      boyNakshatraIndex = 0,
      girlName = "Priya",
      girlMoonSignId = 5,
      girlNakshatraIndex = 9
    )

    assertNotNull(compatibility)
    assertTrue(compatibility.totalScore in 0.0..36.0)
    assertEquals(8, compatibility.gunaScores.size)
  }
}
