package com.epfl.esl.endlessapi.SpecialistProvider

import android.content.Context

interface SpecialistInfoInterface {
    fun getSpecialistDetails(context: Context?): Map<String, SpecailistDetailsItem>
}