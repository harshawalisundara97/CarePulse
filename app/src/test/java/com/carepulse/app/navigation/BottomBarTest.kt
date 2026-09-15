package com.carepulse.app.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.ui.theme.CarePulseTheme
import com.carepulse.app.ui.theme.LocalGlassEnabled
import dev.chrisbanes.haze.HazeState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BottomBarTest {

    @get:Rule
    val compose = createComposeRule()

    private val tabRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun setBar(role: UserRole?, currentRoute: String?, onTab: (String) -> Unit = {}) {
        compose.setContent {
            CarePulseTheme {
                // Blur is covered on-device; the opaque fallback keeps Robolectric off RenderEffect.
                CompositionLocalProvider(LocalGlassEnabled provides false) {
                    BottomBar(
                        currentRoute = currentRoute,
                        tabs = tabsFor(role),
                        hazeState = HazeState(),
                        onTabSelected = onTab
                    )
                }
            }
        }
    }

    @Test
    fun `family and caregiver share the five standard tabs`() {
        val expected = listOf("Home", "Pulse", "Messages", "Activity", "Settings")
        assertEquals(expected, tabsFor(UserRole.CUSTOMER).map { it.label })
        assertEquals(expected, tabsFor(UserRole.CAREGIVER).map { it.label })
        assertEquals(expected, tabsFor(null).map { it.label })
    }

    @Test
    fun `agency relabels tabs but keeps the same routes`() {
        val agency = tabsFor(UserRole.AGENCY)
        assertEquals(
            listOf("Dashboard", "Caregivers", "Requests", "Billing", "Settings"),
            agency.map { it.label }
        )
        assertEquals(tabsFor(UserRole.CUSTOMER).map { it.route }, agency.map { it.route })
    }

    @Test
    fun `renders five accessible tabs with the current one selected`() {
        setBar(UserRole.CUSTOMER, Routes.Pulse)

        compose.onAllNodes(tabRole).assertCountEquals(5)
        compose.onNodeWithContentDescription("Pulse").assertIsSelected()
        compose.onNodeWithContentDescription("Home").assertIsNotSelected()
        compose.onAllNodesWithText("PULSE").assertCountEquals(1)
    }

    @Test
    fun `tabs meet the 48dp minimum touch target`() {
        setBar(UserRole.CUSTOMER, Routes.Home)

        listOf("Home", "Pulse", "Messages", "Activity", "Settings").forEach {
            compose.onNodeWithContentDescription(it)
                .assertWidthIsAtLeast(48.dp)
                .assertHeightIsAtLeast(48.dp)
        }
    }

    @Test
    fun `tapping another tab reports its route`() {
        val selected = mutableListOf<String>()
        setBar(UserRole.CUSTOMER, Routes.Home) { selected += it }

        compose.onNodeWithContentDescription("Messages").performClick()

        assertEquals(listOf(Routes.Messages), selected)
    }

    @Test
    fun `tapping the current tab does nothing`() {
        val selected = mutableListOf<String>()
        setBar(UserRole.AGENCY, Routes.Activity) { selected += it }

        compose.onNodeWithContentDescription("Billing").performClick()

        assertEquals(emptyList<String>(), selected)
    }
}
