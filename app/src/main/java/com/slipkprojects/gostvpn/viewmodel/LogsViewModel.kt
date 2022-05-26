package com.slipkprojects.gostvpn.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.slipkprojects.gostvpn.service.GostInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application), LifecycleEventObserver {
    private val gostInteractor: GostInteractor =
        GostInteractor(
            getApplication(),
            object : GostInteractor.Listener {
                override fun onNewLog(logItem: String) {
                    logsCached.add(logItem)
                    _logsGostService.update { logsCached.toTypedArray() }
                }

                override fun onLogsCached(logsList: Array<String>) {
                    logsCached.clear()
                    logsCached.addAll(logsList)
                    _logsGostService.update { logsCached.toTypedArray() }
                }

                override fun onLogsCleared() {
                    logsCached.clear()
                    _logsGostService.update { logsCached.toTypedArray() }
                }
            })
    private val logsCached: ArrayList<String> = arrayListOf()
    private val _logsGostService: MutableStateFlow<Array<String>> = MutableStateFlow(arrayOf())

    val logsGostService: LiveData<Array<String>> = _logsGostService.asLiveData()

    fun clearLogs() = viewModelScope.launch {
        gostInteractor.clearLogsFromService()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                gostInteractor.startBound()
            }
            Lifecycle.Event.ON_STOP -> {
                gostInteractor.stopBound()
            }
            else -> {}
        }
    }
}