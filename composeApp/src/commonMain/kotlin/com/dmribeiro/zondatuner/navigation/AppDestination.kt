package com.dmribeiro.zondatuner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.dmribeiro.zondatuner.domain.usecase.GetTuningsUseCase
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUiMapper
import com.dmribeiro.zondatuner.presentation.ui.CreateTuningScreenContent
import com.dmribeiro.zondatuner.presentation.ui.HomeScreenContent
import com.dmribeiro.zondatuner.presentation.ui.SplashScreenContent
import com.dmribeiro.zondatuner.presentation.ui.TunerScreenContent
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform

sealed class AppDestination : Screen {
    abstract val topBarConfig: AppTopBarComponentState

    data object SplashScreen : AppDestination() {
        override val topBarConfig = AppTopBarComponentState()

        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow

            LaunchedEffect(Unit) {
                val getTunings: GetTuningsUseCase = KoinPlatform.getKoin().get()
                val mapper: TuningDataUiMapper = KoinPlatform.getKoin().get()

                // Splash de 1s; enquanto isso decide o destino.
                val tunings = withTimeoutOrNull(800L) {
                    getTunings().firstOrNull()
                }.orEmpty()
                delay(1000L)

                val lastUsed = tunings.filter { it.lastUsedAt > 0 }.maxByOrNull { it.lastUsedAt }
                if (lastUsed != null) {
                    // Caso comum: abre direto no afinador, com a Home no back stack.
                    navigator.replaceAll(HomeScreen)
                    navigator.push(TunerScreen(mapper.toObject(lastUsed)))
                } else {
                    navigator.replace(HomeScreen)
                }
            }

            SplashScreenContent()
        }
    }

    data object HomeScreen : AppDestination() {
        override val key: String = "HomeScreenKey"
        override val topBarConfig = AppTopBarComponentState().apply {
            showBackButton = false
            title = "Minhas Afinações"
        }

        @Composable
        override fun Content() {
            val homeScreenModel = rememberScreenModel {
                HomeScreenModel(
                    getTuningsUseCase = KoinPlatform.getKoin().get(),
                    insertTuningUseCase = KoinPlatform.getKoin().get(),
                    deleteTuningUseCase = KoinPlatform.getKoin().get(),
                    tuningDataUiMapper = KoinPlatform.getKoin().get(),
                    updateTuningUseCase = KoinPlatform.getKoin().get(),
                    seedTuningsUseCase = KoinPlatform.getKoin().get(),
                    markTuningUsedUseCase = KoinPlatform.getKoin().get(),
                )
            }

            HomeScreenContent(homeScreenModel)
        }
    }

    data class CreateTuningScreen(
        val existingTuning: TuningDataUi? = null // 🔹 Se for null, cria uma nova afinação
    ) : AppDestination() {

        override val key: String = "CreateTuningScreenKey"

        override val topBarConfig = AppTopBarComponentState().apply {
            title = if (existingTuning != null) "Editar afinação" else "Nova afinação"
            showBackButton = true
        }

        @Composable
        override fun Content() {
            val navigator = LocalNavigator.current
            val viewModel: HomeScreenModel = koinInject()

            CreateTuningScreenContent(
                existingTuning = existingTuning,
                onBack = { navigator?.pop() },
                onSave = { tuning ->
                    if (existingTuning == null) {
                        viewModel.createNewTuning(tuning)
                    } else {
                        viewModel.updateTuning(tuning) // 🔹 Agora suporta edição
                    }
                    navigator?.pop()
                }
            )
        }
    }

    data class TunerScreen(
        val tuning: TuningDataUi
    ) : AppDestination() {

        override val key: String = "TunerScreenKey"
        override val topBarConfig = AppTopBarComponentState().apply {
            showBackButton = true
            showMenuButton = true
        }

        @Composable
        override fun Content() {
            val navigator = LocalNavigator.current

            TunerScreenContent(
                onBack = { navigator?.pop() },
                tuning = tuning,
            )
        }
    }
}