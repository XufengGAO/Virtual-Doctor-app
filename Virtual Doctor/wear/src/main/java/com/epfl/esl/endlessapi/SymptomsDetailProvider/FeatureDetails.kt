package com.epfl.esl.endlessapi.SymptomsDetailProvider

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeatureDetails(
    val text: String,
    val layText: String?,
    val name: String,
    val type: String,
    val min: Double,
    val max: Double,
    var default: Double,
    var step: Double,
    var choices: List<FeatureDetailsChoice>?,
    var IsPatientProvided: Boolean
): Parcelable