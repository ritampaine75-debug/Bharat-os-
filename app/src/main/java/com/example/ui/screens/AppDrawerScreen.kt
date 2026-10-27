package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCategory
import com.example.ui.components.AppItemView
import com.example.ui.components.BharatSearchBar
import com.example.ui.theme.TypeHelper
import com.example.ui.viewmodel.LauncherScreen
import com.example.ui.viewmodel.LauncherUiState
import com.example.ui.viewmodel.LauncherViewModel
import com.example.util.Localization
import kotlinx.coroutines.launch

private val DrawerCategories = listOf(
    AppCategory.ALL,
    AppCategory.FAVORITES,
    AppCategory.COMMUNICATION,
    AppCategory.TOOLS,
    AppCategory.PRODUCTIVITY,
    AppCategory.MEDIA,
    AppCategory.SYSTEM
)

private val AlphabetLetters = ('A'..'Z').toList()

private val LiquidDrawerBgBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F172A).copy(alpha = 0.88f),
        Color(0xFF090D16).copy(alpha = 0.94f)
    )
)

private val LiquidScrollerBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.30f),
        Color(0xFFFF9800).copy(alpha = 0.25f),
        Color.White.copy(alpha = 0.12f)
    )
)

@Composable
fun AppDrawerScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    // BackHandler clears active search query first, or returns to Home screen
    BackHandler {
        if (uiState.searchQuery.isNotEmpty()) {
            viewModel.setSearchQuery("")
        } else {
            viewModel.navigateTo(LauncherScreen.HOME)
        }
    }

    val settings = uiState.settings
    val language = settings.language
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = uiState.drawerScrollIndex,
        initialFirstVisibleItemScrollOffset = uiState.drawerScrollOffset
    )
    val coroutineScope = rememberCoroutineScope()
    val fontFamily = remember(settings.fontChoice) { TypeHelper.getFontFamily(settings.fontChoice) }

    // Scroll-aware lightweight rendering flag: only changes when scroll starts/stops, never per-pixel!
    val isScrolling by remember {
        derivedStateOf { gridState.isScrollInProgress }
    }

    // Persist scroll position when leaving the drawer or when scroll settles
    DisposableEffect(gridState) {
        onDispose {
            viewModel.saveDrawerScrollPosition(
                index = gridState.firstVisibleItemIndex,
                offset = gridState.firstVisibleItemScrollOffset
            )
        }
    }

    // When user actively types a search query, scroll to top so Tier-1 / Tier-2 prefix matches are visible immediately
    LaunchedEffect(uiState.searchQuery, uiState.selectedCategory) {
        if (uiState.searchQuery.isNotEmpty() && gridState.firstVisibleItemIndex > 0) {
            gridState.scrollToItem(0)
        }
    }

    // Liquid Glass frosted container for App Drawer
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = LiquidDrawerBgBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("app_drawer_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Back button + Search bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            viewModel.setSearchQuery("")
                        }
                        viewModel.navigateTo(LauncherScreen.HOME)
                    },
                    modifier = Modifier.testTag("drawer_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    BharatSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        language = language,
                        onSearchSubmit = {
                            val first = uiState.filteredApps.firstOrNull()
                            if (first != null) {
                                viewModel.openApp(first)
                            }
                        }
                    )
                }
            }

            // Category Filter Chips + Match count badge when searching
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = DrawerCategories,
                        key = { it.id }
                    ) { category ->
                        val isSelected = uiState.selectedCategory == category
                        val label = when (category) {
                            AppCategory.ALL -> Localization.getText("all_apps", language)
                            AppCategory.FAVORITES -> Localization.getText("favorites", language)
                            else -> category.name.lowercase().replaceFirstChar { it.uppercase() }
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE65100),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.12f),
                                labelColor = Color.White.copy(alpha = 0.85f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("category_chip_${category.id}")
                        )
                    }
                }

                if (uiState.searchQuery.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE65100).copy(alpha = 0.25f),
                        modifier = Modifier.padding(end = 14.dp)
                    ) {
                        Text(
                            text = "${uiState.filteredApps.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFCC80),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Grid + Alphabet Index
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (uiState.filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = Localization.getText("empty_apps", language),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            if (uiState.searchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No apps match \"${uiState.searchQuery.trim()}\"",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(settings.gridSize.columns),
                        state = gridState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .testTag("drawer_grid")
                    ) {
                        items(
                            items = uiState.filteredApps,
                            key = { it.compositeKey },
                            contentType = { "app_item" }
                        ) { app ->
                            AppItemView(
                                app = app,
                                iconSize = settings.iconSize,
                                showLabel = true,
                                iconShape = settings.iconShape,
                                labelColor = Color.White,
                                fontFamily = fontFamily,
                                useGlassContainer = settings.glassSettings.enableGlassIconContainers,
                                isScrolling = isScrolling || settings.glassSettings.reducedEffects,
                                onClick = { viewModel.openApp(app) },
                                onLongClick = { viewModel.setContextMenuApp(app) }
                            )
                        }
                    }
                }

                // Liquid Glass Fast Alphabet Scroller (A-Z)
                if (uiState.searchQuery.isEmpty() && uiState.selectedCategory == AppCategory.ALL && uiState.filteredApps.isNotEmpty()) {
                    AlphabetIndexScroller(
                        onLetterSelected = { letter ->
                            val lowerChar = letter.lowercaseChar()
                            val index = uiState.filteredApps.indexOfFirst {
                                it.normalizedLabel.startsWith(lowerChar)
                            }
                            if (index in 0 until uiState.filteredApps.size) {
                                coroutineScope.launch {
                                    gridState.scrollToItem(index)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlphabetIndexScroller(
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(end = 6.dp, top = 6.dp, bottom = 6.dp)
            .width(22.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(0.8.dp, LiquidScrollerBorderBrush, RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        AlphabetLetters.forEach { char ->
            Text(
                text = char.toString(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.72f),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onLetterSelected(char) }
                    .padding(vertical = 1.dp, horizontal = 3.dp)
                    .testTag("alphabet_letter_$char")
            )
        }
    }
}
