package com.epfl.esl.endlessapi.Endless_Interface

data class GetSuggestedSpecializationsResponse(
    val SuggestedSpecializations: List<List<String>>,
    val status: String?
)