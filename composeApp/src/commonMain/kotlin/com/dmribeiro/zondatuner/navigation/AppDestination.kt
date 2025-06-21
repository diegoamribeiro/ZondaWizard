package com.dmribeiro.zondatuner.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.ui.CreateTuningScreenContent
import com.dmribeiro.zondatuner.presentation.ui.HomeScreenContent
import com.dmribeiro.zondatuner.presentation.ui.TunerScreenContent
import com.dmribeiro.zondatuner.presentation.viewmodel.HomeScreenModel
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform

sealed class AppDestination : Screen {
    abstract val topBarConfig: AppTopBarComponentState

    data object HomeScreen : AppDestination() {
        override val key: String = "HomeScreenKey"
        override val topBarConfig = AppTopBarComponentState().apply {
            showBackButton = false
            title = "Home"
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
                    seedTuningsUseCase = KoinPlatform.getKoin().get()
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
            title = tuning.name
            showBackButton = true
        }

        @Composable
        override fun Content() {
            val navigator = LocalNavigator.current
            TunerScreenContent(
                onBack = { navigator?.pop() },
                tuning = tuning
            )
        }
    }
}