package com.carepulse.app.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TabInsetsTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `withoutBottom keeps top and sides and drops the bottom`() {
        lateinit var result: PaddingValues
        compose.setContent {
            result = PaddingValues(start = 1.dp, top = 2.dp, end = 3.dp, bottom = 4.dp).withoutBottom()
        }
        compose.runOnIdle {
            assertEquals(1.dp, result.calculateStartPadding(LayoutDirection.Ltr))
            assertEquals(2.dp, result.calculateTopPadding())
            assertEquals(3.dp, result.calculateEndPadding(LayoutDirection.Ltr))
            assertEquals(0.dp, result.calculateBottomPadding())
        }
    }

    @Test
    fun `withoutBottom respects right-to-left layouts`() {
        lateinit var result: PaddingValues
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                result = PaddingValues(start = 1.dp, end = 3.dp).withoutBottom()
            }
        }
        compose.runOnIdle {
            assertEquals(1.dp, result.calculateStartPadding(LayoutDirection.Rtl))
            assertEquals(3.dp, result.calculateEndPadding(LayoutDirection.Rtl))
        }
    }

    @Test
    fun `tab insets use the bottom nav clearance as the bottom edge`() {
        var bottomPx = -1
        var expectedPx = -1
        compose.setContent {
            CompositionLocalProvider(LocalBottomNavClearance provides 90.dp) {
                val density = LocalDensity.current
                bottomPx = tabContentWindowInsets().getBottom(density)
                expectedPx = with(density) { 90.dp.roundToPx() }
            }
        }
        compose.runOnIdle { assertEquals(expectedPx, bottomPx) }
    }

    @Test
    fun `clearance defaults to zero outside the tab host`() {
        var clearance = (-1).dp
        compose.setContent { clearance = LocalBottomNavClearance.current }
        compose.runOnIdle { assertEquals(0.dp, clearance) }
    }
}
