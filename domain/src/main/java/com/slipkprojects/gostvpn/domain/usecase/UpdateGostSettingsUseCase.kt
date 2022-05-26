package com.slipkprojects.gostvpn.domain.usecase

import com.slipkprojects.gostvpn.domain.LocalRepository
import com.slipkprojects.gostvpn.domain.model.GostSettings
import javax.inject.Inject

class UpdateGostSettingsUseCase @Inject constructor(private val localRepository: LocalRepository) {
    suspend operator fun invoke(gostSettings: GostSettings) = localRepository.updateGostSettings(gostSettings)
}