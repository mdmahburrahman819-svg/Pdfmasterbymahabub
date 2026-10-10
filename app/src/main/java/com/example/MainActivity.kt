package com.example

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.editor.PdfEditorScreen
import com.example.ui.files.FilesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.MainTab
import com.example.ui.navigation.PdfMasterBottomNav
import com.example.ui.reader.PdfReaderScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.ThemeMode
import com.example.ui.tools.ToolsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PdfViewModel

enum class AppDestination {
    MAIN,
    READER,
    EDITOR
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("pdf_master_prefs", Context.MODE_PRIVATE)
        val savedThemeStr = sharedPrefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val initialTheme = try {
            ThemeMode.valueOf(savedThemeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }

        setContent {
            var themeMode by remember { mutableStateOf(initialTheme) }
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PdfAppNavHost(
                        intentUri = intent?.data,
                        themeMode = themeMode,
                        onThemeModeChanged = { newMode ->
                            themeMode = newMode
                            sharedPrefs.edit().putString("theme_mode", newMode.name).apply()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PdfAppNavHost(
    intentUri: Uri? = null,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    viewModel: PdfViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentDestination by remember { mutableStateOf(AppDestination.MAIN) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    // Handle incoming file intent if app was opened via file manager
    LaunchedEffect(intentUri) {
        if (intentUri != null) {
            viewModel.importPdfFromUri(context, intentUri) { success, _ ->
                if (success) {
                    currentDestination = AppDestination.READER
                }
            }
        }
    }

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "AppDestinationTransition"
    ) { destination ->
        when (destination) {
            AppDestination.MAIN -> {
                Scaffold(
                    bottomBar = {
                        PdfMasterBottomNav(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            MainTab.HOME -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onOpenReader = { currentDestination = AppDestination.READER },
                                    onOpenEditor = { currentDestination = AppDestination.EDITOR },
                                    onNavigateToTools = { selectedTab = MainTab.TOOLS }
                                )
                            }
                            MainTab.FILES -> {
                                FilesScreen(
                                    viewModel = viewModel,
                                    onOpenReader = { currentDestination = AppDestination.READER },
                                    onOpenEditor = { currentDestination = AppDestination.EDITOR }
                                )
                            }
                            MainTab.TOOLS -> {
                                ToolsScreen(
                                    viewModel = viewModel,
                                    onOpenReader = { currentDestination = AppDestination.READER },
                                    onOpenEditor = { currentDestination = AppDestination.EDITOR }
                                )
                            }
                            MainTab.SETTINGS -> {
                                SettingsScreen(
                                    currentThemeMode = themeMode,
                                    onThemeModeChanged = onThemeModeChanged
                                )
                            }
                        }
                    }
                }
            }
            AppDestination.READER -> {
                PdfReaderScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        currentDestination = AppDestination.MAIN
                    },
                    onNavigateToEditor = {
                        currentDestination = AppDestination.EDITOR
                    }
                )
            }
            AppDestination.EDITOR -> {
                PdfEditorScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        currentDestination = AppDestination.READER
                    }
                )
            }
        }
    }
}
