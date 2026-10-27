package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ClockStyle
import com.example.data.model.CulturalGreeting
import com.example.data.model.FontChoice
import com.example.data.model.GridSize
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import com.example.data.model.LauncherLanguage
import com.example.data.model.LauncherTheme
import com.example.data.model.WidgetType
import com.example.data.repository.AppRepository
import com.example.data.repository.LauncherPreferencesRepository
import com.example.ui.theme.TypeHelper
import com.example.util.DiagnosticsTracker
import com.example.util.LauncherManager
import com.example.util.Localization
import com.example.util.MediaSessionTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("bharat_launcher_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        DiagnosticsTracker.clearLogs()
    }

    @Test
    fun testAppNameResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Bharat OS", appName)
    }

    @Test
    fun testLocalizationGreetings() {
        val hindiGreeting = Localization.getGreeting(LauncherLanguage.HINDI, 9)
        assertEquals("सुप्रभात", hindiGreeting)

        val bengaliGreeting = Localization.getGreeting(LauncherLanguage.BENGALI, 9)
        assertEquals("শুভ সকাল", bengaliGreeting)

        val englishGreeting = Localization.getGreeting(LauncherLanguage.ENGLISH, 9)
        assertEquals("Good Morning", englishGreeting)
    }

    @Test
    fun testLauncherPreferencesPersistence() {
        val repo = LauncherPreferencesRepository(context)

        repo.updateGridSize(GridSize.FIVE_BY_FIVE)
        assertEquals(GridSize.FIVE_BY_FIVE, repo.settings.value.gridSize)

        repo.updateIconSize(IconSize.LARGE)
        assertEquals(IconSize.LARGE, repo.settings.value.iconSize)

        repo.updateIconShape(IconShape.CIRCLE)
        assertEquals(IconShape.CIRCLE, repo.settings.value.iconShape)

        repo.updateFontChoice(FontChoice.MODERN_POPPINS)
        assertEquals(FontChoice.MODERN_POPPINS, repo.settings.value.fontChoice)

        repo.updateClockStyle(ClockStyle.ANALOG_GLASS)
        assertEquals(ClockStyle.ANALOG_GLASS, repo.settings.value.clockStyle)

        repo.updateCulturalGreeting(CulturalGreeting.JAI_SHRI_RAM)
        assertEquals(CulturalGreeting.JAI_SHRI_RAM, repo.settings.value.greetingChoice)

        repo.updateTheme(LauncherTheme.DARK)
        assertEquals(LauncherTheme.DARK, repo.settings.value.theme)

        repo.updateLanguage(LauncherLanguage.HINDI)
        assertEquals(LauncherLanguage.HINDI, repo.settings.value.language)

        repo.updateGlassSettings(0.35f, 0.6f, true, reducedEffects = true)
        assertEquals(0.35f, repo.settings.value.glassSettings.opacity, 0.01f)
        assertTrue(repo.settings.value.glassSettings.enableGlassIconContainers)
        assertTrue(repo.settings.value.glassSettings.reducedEffects)

        repo.addHomeShortcut("com.example.testapp")
        assertTrue(repo.settings.value.homeShortcutPackages.contains("com.example.testapp"))
    }

    @Test
    fun testWidgetResizingAndPersistenceAcrossRestart() {
        val repo1 = LauncherPreferencesRepository(context)
        val clockWidget = repo1.settings.value.activeWidgets.first { it.type == WidgetType.CLOCK }

        // Resize clock widget to 3 columns x 3 rows
        val resized = repo1.resizeWidget(clockWidget.id, newColSpan = 3, newRowSpan = 3)
        assertTrue(resized)

        val updatedClock = repo1.settings.value.activeWidgets.first { it.id == clockWidget.id }
        assertEquals(3, updatedClock.colSpan)
        assertEquals(3, updatedClock.rowSpan)

        // Instantiate a second repository to simulate launcher process restart
        val repo2 = LauncherPreferencesRepository(context)
        val reloadedClock = repo2.settings.value.activeWidgets.first { it.id == clockWidget.id }
        assertEquals(3, reloadedClock.colSpan)
        assertEquals(3, reloadedClock.rowSpan)
    }

    @Test
    fun testWidgetDeletionNeverResurrectsAfterRestart() {
        val repo1 = LauncherPreferencesRepository(context)
        val initialIds = repo1.settings.value.activeWidgets.map { it.id }
        assertTrue(initialIds.isNotEmpty())

        // Remove single widget
        val firstId = initialIds.first()
        assertTrue(repo1.removeWidgetById(firstId))
        assertFalse(repo1.settings.value.activeWidgets.any { it.id == firstId })

        // Remove all remaining widgets
        for (id in initialIds.drop(1)) {
            repo1.removeWidgetById(id)
        }
        assertTrue(repo1.settings.value.activeWidgets.isEmpty())

        // Simulate process restart: deleted widgets must NOT come back!
        val repoAfterRestart = LauncherPreferencesRepository(context)
        assertTrue(repoAfterRestart.settings.value.activeWidgets.isEmpty())
    }

    @Test
    fun testAppLaunchTelemetryAndDiagnosticsTracker() {
        val appRepo = AppRepository(context)
        val launched = appRepo.launchApp("com.nonexistent.invalid.pkg")
        assertFalse(launched)
        assertTrue(DiagnosticsTracker.state.value.launchFailuresCount >= 1)
        assertTrue(DiagnosticsTracker.state.value.logs.isNotEmpty())

        DiagnosticsTracker.clearLogs()
        assertEquals(0, DiagnosticsTracker.state.value.launchFailuresCount)
    }

    @Test
    fun testMediaSessionTrackerSafety() {
        MediaSessionTracker.refreshSessions(context)
        val state = MediaSessionTracker.mediaState.value
        assertNotNull(state)
    }

    @Test
    fun testTypeHelperFontResolution() {
        assertNotNull(TypeHelper.getFontFamily(FontChoice.MODERN_POPPINS))
        assertNotNull(TypeHelper.getFontFamily(FontChoice.ELEGANT_CINZEL))
        assertNotNull(TypeHelper.getFontFamily(FontChoice.DIGITAL_MONO))
        assertNotNull(TypeHelper.getFontFamily(FontChoice.SYSTEM_SANS))
    }

    @Test
    fun testLauncherManagerAndSystemControlsSafety() {
        assertNotNull(LauncherManager.isDefaultLauncher(context))
        val diagnostics = com.example.util.SystemControlsManager.getSystemDiagnostics(context)
        assertTrue(diagnostics.jvmHeapMaxMb > 0)
        assertTrue(diagnostics.cpuCores >= 1)

        val battery = com.example.util.SystemControlsManager.getBatteryStatus(context)
        assertTrue(battery.levelPercent in 0..100)

        val torchResult = com.example.util.SystemControlsManager.toggleTorch(context)
        assertNotNull(torchResult)
    }
}
