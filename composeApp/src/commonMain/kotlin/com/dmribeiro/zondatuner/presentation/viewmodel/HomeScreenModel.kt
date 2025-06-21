package com.dmribeiro.zondatuner.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.dmribeiro.zondatuner.domain.usecase.DeleteTuningUseCase
import com.dmribeiro.zondatuner.domain.usecase.GetTuningsUseCase
import com.dmribeiro.zondatuner.domain.usecase.InsertTuningUseCase
import com.dmribeiro.zondatuner.domain.usecase.SeedTuningsUseCase
import com.dmribeiro.zondatuner.domain.usecase.UpdateTuningUseCase
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUi
import com.dmribeiro.zondatuner.presentation.dataui.TuningDataUiMapper
import com.dmribeiro.zondatuner.presentation.dataui.TuningStateUI
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getTuningsUseCase: GetTuningsUseCase,
    private val insertTuningUseCase: InsertTuningUseCase,
    private val deleteTuningUseCase: DeleteTuningUseCase,
    private val tuningDataUiMapper: TuningDataUiMapper,
    private val updateTuningUseCase: UpdateTuningUseCase,
    private val seedTuningsUseCase: SeedTuningsUseCase
) : ScreenModel {

    val tuningState = TuningStateUI()

    init {
        screenModelScope.launch {
            seedTuningsUseCase()
            loadTunings()
        }
    }

    private fun loadTunings() {
        screenModelScope.launch {
            tuningState.loading = true
            tuningState.error = null
            try {
                getTuningsUseCase().collectLatest { tuningList ->
                    if (tuningList.isNotEmpty()) {
                        val uiList = tuningList.map { tuningDataUiMapper.toObject(it) }
                        tuningState.listState.value = uiList
                    }
                    tuningState.loading = false
                }
            } catch (e: Exception) {
                tuningState.error = e
                tuningState.loading = false
            }
        }
    }

    fun createNewTuning(tuning: TuningDataUi) {
        screenModelScope.launch {
            try {
                insertTuningUseCase(tuningDataUiMapper.fromObject(tuning))
                loadTunings()
            } catch (e: Exception) {
                tuningState.error = e
            }
        }
    }

    fun updateTuning(tuning: TuningDataUi) {
        screenModelScope.launch {
            try {
                updateTuningUseCase(tuningDataUiMapper.fromObject(tuning)) // 🔹 Mesmo use case, mas sobrescreve o existente
                loadTunings()
            } catch (e: Exception) {
                tuningState.error = e
            }
        }
    }

    fun removeTuning(id: Long) {
        screenModelScope.launch {
            try {
                deleteTuningUseCase(id)
                loadTunings()
            } catch (e: Exception) {
                tuningState.error = e
            }
        }
    }
}