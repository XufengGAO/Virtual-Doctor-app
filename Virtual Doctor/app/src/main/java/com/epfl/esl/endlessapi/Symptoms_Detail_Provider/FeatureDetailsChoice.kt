package com.epfl.esl.endlessapi.Symptoms_Detail_Provider

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeatureDetailsChoice(
    val text: String,
    val value: Double = 0.0,
    val relatedanswertag: String?
): Parcelable