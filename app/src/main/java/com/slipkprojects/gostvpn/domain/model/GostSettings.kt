package com.slipkprojects.gostvpn.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GostSettings(
    val settings: String
): Parcelable