package com.metehanyl.dilogrenme.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.metehanyl.dilogrenme.ui.conversation.ConversationScreen
import com.metehanyl.dilogrenme.ui.home.HomeScreen
import com.metehanyl.dilogrenme.ui.level.LevelScreen
import com.metehanyl.dilogrenme.ui.listening.ListeningScreen
import com.metehanyl.dilogrenme.ui.onboarding.OnboardingScreen
import com.metehanyl.dilogrenme.ui.quiz.QuizScreen
import com.metehanyl.dilogrenme.ui.settings.SettingsScreen

private object Routes {
    const val ONBOARDING = "onboarding"
    const val LEVEL = "level"
    const val HOME = "home"
    const val QUIZ = "quiz"
    const val LISTENING = "listening"
    const val CONVERSATION = "conversation"
    const val SETTINGS = "settings"
}

@Composable
fun DilOgrenmeNavGraph(viewModel: MainViewModel = viewModel()) {
    val navController: NavHostController = rememberNavController()
    val settings by viewModel.settings.collectAsState()
    val wordBank = viewModel.wordBank

    val startDestination = when {
        !settings.onboarded -> Routes.ONBOARDING
        !settings.levelChosen -> Routes.LEVEL
        else -> Routes.HOME
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                languages = wordBank.languages,
                onFinished = { native, target ->
                    viewModel.saveLanguages(native, target)
                    navController.navigate(Routes.LEVEL) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LEVEL) {
            val canGoBack = navController.previousBackStackEntry != null
            LevelScreen(
                strings = viewModel.uiStrings,
                wordBank = wordBank,
                nativeLang = settings.nativeLang ?: "en",
                targetLang = settings.targetLang ?: "en",
                currentTier = settings.selectedTier,
                onTierChosen = { tier ->
                    viewModel.saveSelectedTier(tier)
                    val popped = navController.popBackStack(Routes.HOME, false)
                    if (!popped) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LEVEL) { inclusive = true }
                        }
                    }
                },
                onBack = if (canGoBack) ({ navController.popBackStack() }) else null
            )
        }

        composable(Routes.HOME) {
            val strings = viewModel.uiStrings
            val nativeLang = remember(settings.nativeLang, wordBank) {
                wordBank.languages.firstOrNull { it.code == settings.nativeLang }
            }
            val targetLang = remember(settings.targetLang, wordBank) {
                wordBank.languages.firstOrNull { it.code == settings.targetLang }
            }
            HomeScreen(
                strings = strings,
                settings = settings,
                nativeLang = nativeLang,
                targetLang = targetLang,
                onOpenQuiz = { navController.navigate(Routes.QUIZ) },
                onOpenListening = { navController.navigate(Routes.LISTENING) },
                onOpenConversation = { navController.navigate(Routes.CONVERSATION) },
                onOpenLevel = { navController.navigate(Routes.LEVEL) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.QUIZ) {
            QuizScreen(
                strings = viewModel.uiStrings,
                wordBank = wordBank,
                nativeLang = settings.nativeLang ?: "en",
                targetLang = settings.targetLang ?: "en",
                startTier = settings.selectedTier,
                onTierReached = { tier -> viewModel.saveHighestTier(tier) },
                onXpEarned = { amount -> viewModel.addXp(amount) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LISTENING) {
            ListeningScreen(
                strings = viewModel.uiStrings,
                wordBank = wordBank,
                nativeLang = settings.nativeLang ?: "en",
                targetLang = settings.targetLang ?: "en",
                startTier = settings.selectedTier,
                onTierReached = { tier -> viewModel.saveHighestTier(tier) },
                onXpEarned = { amount -> viewModel.addXp(amount) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CONVERSATION) {
            val nativeLang = remember(settings.nativeLang, wordBank) {
                wordBank.languages.firstOrNull { it.code == settings.nativeLang }
                    ?: wordBank.languages.first()
            }
            val targetLang = remember(settings.targetLang, wordBank) {
                wordBank.languages.firstOrNull { it.code == settings.targetLang }
                    ?: wordBank.languages.first()
            }
            ConversationScreen(
                strings = viewModel.uiStrings,
                nativeLang = nativeLang,
                targetLang = targetLang,
                apiKey = viewModel.apiKey(),
                apiBaseUrl = settings.apiBaseUrl,
                apiModel = settings.apiModel,
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                strings = viewModel.uiStrings,
                settings = settings,
                languages = wordBank.languages,
                initialApiKey = viewModel.apiKey(),
                onBack = { navController.popBackStack() },
                onSaveLanguages = { native, target -> viewModel.saveLanguages(native, target) },
                onSaveApiConfig = { baseUrl, model, apiKey ->
                    viewModel.setApiConfig(baseUrl, model)
                    viewModel.saveApiKey(apiKey)
                },
                onResetProgress = { viewModel.resetProgress() }
            )
        }
    }
}
