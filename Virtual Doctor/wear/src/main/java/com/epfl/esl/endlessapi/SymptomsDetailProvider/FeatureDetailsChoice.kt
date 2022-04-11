package com.epfl.esl.endlessapi.SymptomsDetailProvider

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeatureDetailsChoice(
    val text: String,
    val value: Double = 0.0,
    val relatedanswertag: String?): Parcelable