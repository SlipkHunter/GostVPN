package com.slipkprojects.gostvpn.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.slipkprojects.gostvpn.R
import com.slipkprojects.gostvpn.domain.model.Event
import com.slipkprojects.gostvpn.domain.usecase.GetGostSettingsUseCase
import com.slipkprojects.gostvpn.domain.usecase.UpdateGostSettingsUseCase
import com.slipkprojects.gostvpn.service.util.GostHelper
import com.slipkprojects.gostvpn.domain.model.GostSettings
import com.slipkprojects.gostvpn.service.GostInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    getGostSettingsUseCase: GetGostSettingsUseCase,
    private val updateGostSettingsUseCase: UpdateGostSettingsUseCase
): AndroidViewModel(application), LifecycleEventObserver  {
    private val gostInteractor: GostInteractor = GostInteractor(getApplication(), object : GostInteractor.Listener {
        override fun onNewIsActive(isActiveVpn: Boolean) {
            _isTunnelActive.update { isActiveVpn }
        }
    })

    private val _isTunnelActive: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val isEnabledGostService: LiveData<Boolean> = _isTunnelActive.asLiveData()
    val gostSettings: LiveData<GostSettings> = getGostSettingsUseCase().asLiveData()
    val promptMessage: MutableLiveData<Event<String>> = MutableLiveData()

    fun startOrStopGostService() = viewModelScope.launch {
        if (isEnabledGostService.value == true) {
            stopVPN()
        } else {
            val settings = gostSettings.asFlow().first()

            if (validarGostSettings(settings)) {
                clearLogs()
                GostHelper.startService(getApplication(), settings)
            }
        }
    }

    fun updateGostSettings(gostSettings: GostSettings) = viewModelScope.launch {
        updateGostSettingsUseCase(gostSettings)
    }

    private fun stopVPN() = viewModelScope.launch {
        gostInteractor.stopVpn()
    }

    private fun clearLogs() = viewModelScope.launch {
        gostInteractor.clearLogsFromService()
    }


    private fun validarGostSettings(gostSettings: GostSettings): Boolean {
        var errorMsg: Int? = null

        if (gostSettings.settings.isBlank()) {
            errorMsg = R.string.error_empty_settings
        }

        return if (errorMsg != null) {
            promptMessage.postValue(Event(getApplication<Application>().getString(errorMsg)))
            false
        } else {
            true
        }
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