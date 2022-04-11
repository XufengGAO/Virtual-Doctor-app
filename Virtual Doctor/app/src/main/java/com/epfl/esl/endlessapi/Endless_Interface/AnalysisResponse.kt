package com.epfl.esl.endlessapi.Endless_Interface

data class AnalysisResponse(
    val status: String,
    var Diseases: List<Map<String, String>>
)
