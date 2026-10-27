package com.example.util

import com.example.data.model.AppCategory
import com.example.data.model.AppInfo
import java.text.Normalizer
import java.util.Locale

/**
 * Deterministic, multi-tier application search and ranking engine for Bharat OS 2.0.
 *
 * Ranking Order:
 *  Tier 1: Exact application name match (e.g., query "Phone" == label "Phone")
 *  Tier 2: Application name prefix match (e.g., query "ca" -> "Calculator", "Calendar", "Camera")
 *  Tier 3: Word prefix match inside the application name (e.g., query "set" -> "Google Settings")
 *  Tier 4: Substring match inside the application name (e.g., query "tube" -> "YouTube")
 *  Tier 5: Package name / metadata match (only when query length >= 2, e.g., "whatsapp" -> com.whatsapp)
 *
 * Ties within any tier are broken alphabetically by normalized application label, then by packageName.
 */
object AppSearchRanker {

    private val WHITESPACE_REGEX = Regex("\\s+")
    private val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
    private val WORD_SPLIT_REGEX = Regex("[\\s._\\-/]+")

    fun normalize(input: String): String {
        if (input.isBlank()) return ""
        val trimmed = input.trim().replace(WHITESPACE_REGEX, " ")
        val decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
        return DIACRITICS_REGEX.replace(decomposed, "").lowercase(Locale.ROOT)
    }

    /**
     * Filters and ranks [apps] by [rawQuery], [selectedCategory], and [favoritePackages].
     * Guarantees no duplicate entries (by packageName#activityName) and deterministic ordering.
     */
    fun filterAndRank(
        apps: List<AppInfo>,
        rawQuery: String,
        selectedCategory: AppCategory = AppCategory.ALL,
        favoritePackages: Set<String> = emptySet()
    ): List<AppInfo> {
        if (apps.isEmpty()) return emptyList()

        val normalizedQuery = normalize(rawQuery)

        // Step 1: Deduplicate and apply category filter
        val seenKeys = HashSet<String>(apps.size)
        val categoryFiltered = ArrayList<AppInfo>(apps.size)

        for (app in apps) {
            val uniqueKey = "${app.packageName}#${app.activityName}"
            if (!seenKeys.add(uniqueKey)) continue

            val matchesCategory = when (selectedCategory) {
                AppCategory.ALL -> true
                AppCategory.FAVORITES -> app.isFavorite || favoritePackages.contains(app.packageName)
                else -> app.category == selectedCategory
            }
            if (matchesCategory) {
                categoryFiltered.add(app)
            }
        }

        // Fast path: Empty query -> sort alphabetically by normalized label
        if (normalizedQuery.isEmpty()) {
            return categoryFiltered.sortedWith(
                compareBy<AppInfo> { normalize(it.label) }
                    .thenBy { it.packageName }
                    .thenBy { it.activityName }
            )
        }

        // Step 2: Score each app against the query
        data class ScoredApp(
            val app: AppInfo,
            val tier: Int,
            val matchIndex: Int,
            val normalizedLabel: String
        )

        val scoredResults = ArrayList<ScoredApp>()

        for (app in categoryFiltered) {
            val normLabel = normalize(app.label)
            val normPkg = app.packageName.lowercase(Locale.ROOT)

            val tierAndIndex: Pair<Int, Int>? = when {
                // Tier 1: Exact label match
                normLabel == normalizedQuery -> Pair(1, 0)

                // Tier 2: Label prefix match (starts with query)
                normLabel.startsWith(normalizedQuery) -> Pair(2, 0)

                // Tier 3: Word-boundary prefix match inside label
                else -> {
                    val words = normLabel.split(WORD_SPLIT_REGEX)
                    val wordIndex = words.indexOfFirst { it.isNotEmpty() && it.startsWith(normalizedQuery) }
                    if (wordIndex >= 0) {
                        Pair(3, wordIndex)
                    } else {
                        // Tier 4: Substring match inside label (only for queries >= 2 chars so 1-letter queries strictly show apps/words starting with that letter!)
                        val subIndex = if (normalizedQuery.length >= 2) normLabel.indexOf(normalizedQuery) else -1
                        if (subIndex >= 0) {
                            Pair(4, subIndex)
                        } else if (normalizedQuery.length >= 2 && normPkg.contains(normalizedQuery)) {
                            // Tier 5: Package name match (only when query has 2+ chars)
                            Pair(5, normPkg.indexOf(normalizedQuery))
                        } else {
                            null
                        }
                    }
                }
            }

            if (tierAndIndex != null) {
                scoredResults.add(
                    ScoredApp(
                        app = app,
                        tier = tierAndIndex.first,
                        matchIndex = tierAndIndex.second,
                        normalizedLabel = normLabel
                    )
                )
            }
        }

        // Step 3: Sort by Tier -> Match Index -> Alphabetical Label -> Package Name
        scoredResults.sortWith(
            compareBy<ScoredApp> { it.tier }
                .thenBy { it.matchIndex }
                .thenBy { it.normalizedLabel }
                .thenBy { it.app.packageName }
        )

        return scoredResults.map { it.app }
    }
}
