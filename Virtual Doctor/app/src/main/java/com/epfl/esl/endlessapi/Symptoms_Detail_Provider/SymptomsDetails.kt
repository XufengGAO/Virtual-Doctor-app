package com.epfl.esl.endlessapi.Symptoms_Detail_Provider

import android.content.Context

interface SymptomsDetails {
    fun getSymtomsDetails(context: Context?): Map<String, FeatureDetails>
}