package com.example

import com.example.data.model.AppCategory
import com.example.data.model.AppInfo
import com.example.util.AppSearchRanker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val sampleApps = listOf(
        AppInfo(packageName = "com.android.Account", activityName = ".Main", label = "Account Manager", category = AppCategory.SYSTEM),
        AppInfo(packageName = "com.android.calculator2", activityName = ".Calc", label = "Calculator", category = AppCategory.TOOLS),
        AppInfo(packageName = "com.google.android.calendar", activityName = ".Cal", label = "Calendar", category = AppCategory.TOOLS),
        AppInfo(packageName = "com.android.camera", activityName = ".Cam", label = "Camera", category = AppCategory.MEDIA),
        AppInfo(packageName = "com.android.chrome", activityName = ".Chrome", label = "Chrome", category = AppCategory.TOOLS),
        AppInfo(packageName = "com.android.settings", activityName = ".Settings", label = "Settings", category = AppCategory.SYSTEM),
        AppInfo(packageName = "com.google.android.gms", activityName = ".GoogleSettings", label = "Google Settings", category = AppCategory.SYSTEM),
        AppInfo(packageName = "com.example.preset", activityName = ".Preset", label = "Presetting Utility", category = AppCategory.TOOLS),
        AppInfo(packageName = "com.android.dialer", activityName = ".Dialer", label = "Phone", category = AppCategory.COMMUNICATION),
        AppInfo(packageName = "com.oneplus.backuprestore", activityName = ".Clone", label = "Phone Clone", category = AppCategory.TOOLS),
        AppInfo(packageName = "com.whatsapp", activityName = ".Home", label = "WhatsApp", category = AppCategory.COMMUNICATION)
    )

    @Test
    fun singleLetterSearch_prioritizesAppsStartingWithLetterAndExcludesMidWord() {
        val results = AppSearchRanker.filterAndRank(sampleApps, "c")
        val labels = results.map { it.label }

        assertEquals(listOf("Calculator", "Calendar", "Camera", "Chrome", "Phone Clone"), labels)
        // "Account Manager" has 'c' only in the middle of the word, so single-letter 'c' must not rank it
        assertFalse(labels.contains("Account Manager"))
    }

    @Test
    fun exactMatch_ranksAbovePrefixMatch() {
        val results = AppSearchRanker.filterAndRank(sampleApps, "phone")
        assertEquals("Phone", results[0].label)
        assertEquals("Phone Clone", results[1].label)
    }

    @Test
    fun prefixMatch_ranksAboveWordPrefixAndSubstringMatch() {
        val results = AppSearchRanker.filterAndRank(sampleApps, "set")
        val labels = results.map { it.label }
        // Tier 2: "Settings" (starts with "set")
        // Tier 3: "Google Settings" (word starts with "set")
        // Tier 4: "Presetting Utility" (substring "set")
        assertEquals(listOf("Settings", "Google Settings", "Presetting Utility"), labels)
    }

    @Test
    fun caseAndWhitespaceNormalization_worksCorrectly() {
        val results = AppSearchRanker.filterAndRank(sampleApps, "   gOoGlE   sEt  ")
        assertEquals(1, results.size)
        assertEquals("Google Settings", results.first().label)
    }

    @Test
    fun emptySearchQuery_returnsFullAlphabeticalCatalogWithoutDuplicates() {
        val withDuplicates = sampleApps + sampleApps.first()
        val results = AppSearchRanker.filterAndRank(withDuplicates, "   ")
        assertEquals(sampleApps.size, results.size)
        assertEquals("Account Manager", results.first().label)
        assertEquals("WhatsApp", results.last().label)
    }

    @Test
    fun nonMatchingQuery_returnsEmptyList() {
        val results = AppSearchRanker.filterAndRank(sampleApps, "zzzz_nonexistent_app")
        assertTrue(results.isEmpty())
    }
}
