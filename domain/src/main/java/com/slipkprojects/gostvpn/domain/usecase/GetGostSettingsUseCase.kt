package com.slipkprojects.gostvpn.domain.usecase

import com.slipkprojects.gostvpn.domain.LocalRepository
import javax.inject.Inject

class GetGostSettingsUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    operator fun invoke() = repository.getGostSettings()
}