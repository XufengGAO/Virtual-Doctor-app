package com.epfl.esl.endlessapi.SymptomsDetailProvider

import android.content.Context

interface SymptomsDetails {
    fun getSymtomsDetails(context: Context?): Map<String, FeatureDetails>
}