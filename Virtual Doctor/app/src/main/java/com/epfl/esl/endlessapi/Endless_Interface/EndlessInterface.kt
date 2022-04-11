package com.epfl.esl.endlessapi.Endless_Interface

import retrofit2.Call
import retrofit2.http.*


interface EndlessInterface {

    @GET("InitSession") // Get User ID Interface
    fun getUserID(): Call<UserIDResponse>

    @POST("AcceptTermsOfUse") // Get AcceptTerm Interface post
    fun postUserTerm(
        @Query("SessionID") SessionID: String?,
        @Query("passphrase") Passphrase: String
    ): Call<AcceptTermResponse>

    @POST("UpdateFeature")  // Upload features(Symptoms)
    fun updateFeatures(
        @Query("SessionID") sessionID: String?,
        @Query("name") name: String?,
        @Query("value") value: String?
    ): Call<BasicSuccessResponse>

    @POST("DeleteFeature")  // Delete features(Symptoms)
    fun deleteFeatures(
        @Query("SessionID") sessionID: String?,
        @Query("name") name: String?,
    ): Call<BasicSuccessResponse>

    @POST("SetUseDefaultValues")  // Delete features(Symptoms)
    fun setUseDefaultValues(
        @Query("SessionID") sessionID: String?,
        @Query("value") name: Boolean,
    ): Call<BasicSuccessResponse>

    @GET("Analyze")  // Analyze features(Symptoms)
    fun analyze(
        @Query("SessionID") sessionID: String?,
        @Query("NumberOfResults") numberOfResults: Int?,
        //@Query("ResponseFormat") responseFormat: String?,
    ): Call<AnalysisResponse>

    @GET("GetSuggestedSpecializations")  // Analyze features(Symptoms)
    fun getSuggestedSpecializations(
        @Query("SessionID") sessionID: String?,
        @Query("NumberOfResults") numberOfResults: Int?,
    ): Call<GetSuggestedSpecializationsResponse>

}