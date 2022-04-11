package com.epfl.esl.endlessapi.FragmentViewModel

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.epfl.esl.endlessapi.Endless_Interface.*
import com.epfl.esl.endlessapi.MainActivity
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.Symptoms_Detail_Provider.AccessSymptomsOutput
import com.epfl.esl.endlessapi.Symptoms_Detail_Provider.FeatureDetails
import com.epfl.esl.endlessapi.Symptoms_Detail_Provider.FeatureDetailsChoice
import com.google.android.gms.wearable.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import kotlin.jvm.java as java

class SearchSymptomsViewModel(application: Application) : AndroidViewModel(application), DataClient.OnDataChangedListener{

    // HR Part
    private val _heartRate = MutableLiveData<Int>()
    val heartRate: LiveData<Int>
        get() = _heartRate

    // TODO: if HR is below/above the limit, do something
    val MIN_HR = 50
    val MAX_HR = 120

    // Get key from UserLoginProfileViewModel
    var key: String  = ""

    // Symptom List in Firebase
    var arraylist : MutableList<String> = mutableListOf()
    val base: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = base.getReference("Profiles")


    // Symptoms Part
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    // Initial Variable for Endless
    val BASE_URL: String = "http://api.endlessmedical.com/v1/dx/"
    var sessionID: String? = null
    var userImage: Drawable? = null
//    var sessionID: String = "cgjUZslTVchvGoBs"
    var setUseDefaultValuesCheckValue: Boolean = false

    private val retrofitBuilder: Retrofit = Retrofit.Builder().addConverterFactory(
        GsonConverterFactory.create())
        .baseUrl(BASE_URL).build()

    // Object of AccessSymptomsOutput for extracting symptoms' name
    val selectedSymptomsListText: MutableMap<String, String> = mutableMapOf()
    val selectedSymptomsListValue: MutableMap<String, String> = mutableMapOf()
    val selectedSymptomsListIsPatientProvided: MutableMap<String, Boolean> = mutableMapOf()
    private val selectedSymptomsListRelatedAnswerTag: MutableMap<String, String?> = mutableMapOf()
    private val selectedSymptomType: MutableMap<String, String> = mutableMapOf()
    private val selectedSymptomChoiceIndex: MutableMap<String, String> = mutableMapOf()

    // For storing all Symptoms Data
    val symptomsMap: MutableMap<String, FeatureDetails>
    val symptomsTextList: MutableList<String> = mutableListOf()
    val symptomsNameList: MutableList<String> = mutableListOf()

    // For analyzing Output List
    val diseaseNameList: MutableList<String> = mutableListOf()
    val diseaseProbabilityList: MutableList<String> = mutableListOf()
    val diseaseNameAndProbabilityList: MutableList<String> = mutableListOf()
    var diseaseNameCleanList: String = ""

    var assessmentList:MutableList<String> = mutableListOf()

    // For GetSuggestedSpecializations List
    val suggestedSpecializationsList: MutableList<String> = mutableListOf()

    val selectedSymptomsMap: MutableMap<String, Double> = mutableMapOf()

    var acceptFlag: Boolean
    var hrCheckBoxFlag: Boolean

    init {
        Log.d("Hint:", "SearchViewModel created!")
        _heartRate.value = 0

        // Get all symptoms
        symptomsMap = getSymptomList()
        symptomsMap.forEach { (_, featureContent) ->
            symptomsTextList.add(featureContent.text)
            symptomsNameList.add(featureContent.name)
        }
        acceptFlag = false
        hrCheckBoxFlag = false
        //getUserIDData()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("Hint:", "SearchViewModel destroyed!")
    }

    // function to add symptoms in seekBar fragment
    fun addSymptom(result: FeatureDetails, value: Double) {
        selectedSymptomChoiceIndex[result.name] = "null"
        selectedSymptomsMap[result.name] = value
        selectedSymptomsListText[result.name] = result.text
        selectedSymptomsListIsPatientProvided[result.name] = symptomsMap[result.name]!!.IsPatientProvided
        selectedSymptomsListRelatedAnswerTag[result.name]  = symptomsMap[result.name]?.choices?.get(value.toInt())?.relatedanswertag
        if (result.type == "double"){
            selectedSymptomsListValue[result.name] = String.format("%.1f", value)
            updateFeature(result.name, value)
            selectedSymptomType[result.name] = "seekBar"
        } else {
            selectedSymptomsListValue[result.name] = value.toInt().toString()
            selectedSymptomType[result.name] = "seekBar"
            updateFeature(result.name, value.toInt())
        }
    }

    // function to add symptoms in spinner fragment
    fun addSymptom(result: FeatureDetails, value: Int, choiceString: String, choiceIndex: Int) {
        selectedSymptomChoiceIndex[result.name] = choiceIndex.toString()
        selectedSymptomType[result.name] = "spinner"
        selectedSymptomsMap[result.name] = value.toDouble()
        selectedSymptomsListText[result.name] = result.text
        selectedSymptomsListValue[result.name] = choiceString
        selectedSymptomsListIsPatientProvided[result.name] = symptomsMap[result.name]!!.IsPatientProvided
        selectedSymptomsListRelatedAnswerTag[result.name]  = symptomsMap[result.name]?.choices?.get(choiceIndex)?.relatedanswertag
        updateFeature(result.name, value)
    }

    // function to delete symptoms
    fun deleteSymptom(name: String) {
        selectedSymptomsMap.remove(name)
        selectedSymptomsListText.remove(name)
        selectedSymptomsListValue.remove(name)
        selectedSymptomsListIsPatientProvided.remove(name)
        selectedSymptomsListRelatedAnswerTag.remove(name)
        deleteFeatures(name)
    }

    // Get Symptoms List
    fun getSymptomList(): MutableMap<String, FeatureDetails> {

        val symptomsProvider = AccessSymptomsOutput()

        // Access Data from getSymptomsDetails
        val featureDetails: MutableMap<String, FeatureDetails> = symptomsProvider.getSymtomsDetails(context)

        // Finding feature without choices
        featureDetails.forEach { (array_name, featureContent) ->
            if(featureContent.step == 0.0 && featureContent.choices == null) {
                featureContent.step = 1.0
            }
            if(featureContent.choices == null) {
                println("key:$array_name,Choices:${featureContent.choices.toString()}")
            }
        }
        return featureDetails
    }

    // function to accept api terms (api call)
    fun acceptTerms() {
        if (sessionID == null) {
            Log.e("Invalid Session ID","")
            return
        }
        val jsonAPI = retrofitBuilder.create(EndlessInterface::class.java)
        val passphrase = "I have read, understood and I accept and agree to comply with the Terms of Use of EndlessMedicalAPI and Endless Medical services. The Terms of Use are available on endlessmedical.com"
        val retrofitData = jsonAPI.postUserTerm(SessionID = sessionID,Passphrase = passphrase)

        retrofitData.enqueue(object : Callback<AcceptTermResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<AcceptTermResponse>, response: Response<AcceptTermResponse>) {
                if (!response.isSuccessful){
                    Log.e("User No Accept","Code: "+response.code())
                    return
                }
                val responseBody = response.body()!!
                Log.e("User Accept",responseBody.toString())
            }
            override fun onFailure(call: Call<AcceptTermResponse>, t: Throwable) {
                Toast.makeText(context?.applicationContext, "Reading API Data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // function to update features (api call)
    @JvmName("IntegerValue")
    fun updateFeature(featureName: String, value: Int){
        return updateFeature(featureName, value.toString())
    }
    @JvmName("DoubleValue")
    fun updateFeature(featureName: String, value: Double){
        return  updateFeature(featureName, value.toString())
    }
    @JvmName("StringValue")
    fun updateFeature(featureName: String, value: String) {

        if (sessionID == null) {
            Log.e("Invalid Session ID","")
            return
        }

        val jsonAPI = retrofitBuilder.create(EndlessInterface::class.java)

        val updateResponse = jsonAPI.updateFeatures(sessionID,featureName,value)

        updateResponse.enqueue(object : Callback<BasicSuccessResponse> {
            override fun onResponse(
                call: Call<BasicSuccessResponse?>,
                response: Response<BasicSuccessResponse?>
            ) {
                if (!response.isSuccessful){
                    Log.e("Failed","update one feature: "+response.code())
                    return
                }
                Log.e("Successful", "update one feature")
            }
            override fun onFailure(call: Call<BasicSuccessResponse?>, t: Throwable) {

            }
        })
    }

    // function to delete features (api call)
    fun deleteFeatures(featureName: String){

        if (sessionID == null) {
            Log.e("Invalid Session ID","")
            return
        }
        val jsonAPI = retrofitBuilder.create(EndlessInterface::class.java)

        val deleteResponse = jsonAPI.deleteFeatures(sessionID, featureName)

        deleteResponse.enqueue(object : Callback<BasicSuccessResponse> {
            override fun onResponse(
                call: Call<BasicSuccessResponse>,
                response: Response<BasicSuccessResponse>
            ) {
                if (!response.isSuccessful){
                    Log.e("Failed","delete feature: "+response.code())
                    return
                }
                //val responseBody = response.body()!!
                Log.e("Successful", "delete feature")
            }

            override fun onFailure(call: Call<BasicSuccessResponse>, t: Throwable) {
            }
        })
    }

    // function to set default values for unselected symptoms (api call)
    fun setUseDefaultValues(value: Boolean){

        if (sessionID == null) {
            Log.e("Invalid Session ID","")
            return
        }
        val jsonAPI = retrofitBuilder.create(EndlessInterface::class.java)

        val setUseDefaultResponse = jsonAPI.setUseDefaultValues(sessionID, value)

        setUseDefaultResponse.enqueue(object : Callback<BasicSuccessResponse> {
            override fun onResponse(
                call: Call<BasicSuccessResponse>,
                response: Response<BasicSuccessResponse>
            ) {
                if (!response.isSuccessful){
                    Log.e("Failed","setUseDefault: "+response.code())
                    return
                }
                //val responseBody = response.body()!!
                Log.e("Successful", "setUseDefault")
            }

            override fun onFailure(call: Call<BasicSuccessResponse>, t: Throwable) {
            }
        })
    }

    // function to analyze potential diseases (api call)
    fun analyzeDisease() {
        if (sessionID == null) {
            Log.e("Invalid Session ID","")
            return
        }
        val jsonAPI = retrofitBuilder.create(EndlessInterface::class.java)

        val analyzeResponse = jsonAPI.analyze(sessionID, 10)


        analyzeResponse.enqueue(object : Callback<AnalysisResponse?> {
            override fun onResponse(
                call: Call<AnalysisResponse?>,
                response: Response<AnalysisResponse?>
            ) {
                if (!response.isSuccessful){
                    Log.e("Failed","analyze: "+response.code())
                    return
                }
                Log.e("Successful", "analyze")
                val responseBody = response.body()!!

                //Initialize data
                diseaseNameList.clear()
                diseaseProbabilityList.clear()
                diseaseNameAndProbabilityList.clear()
                diseaseNameCleanList = ""

                // Disease list for list view
                for (item in responseBody.Diseases){
                    item.forEach{(disease_name,disease_prob)->
                        diseaseNameList.add(disease_name)
                        diseaseProbabilityList.add(disease_prob)
                        diseaseNameAndProbabilityList.add( ("$disease_name:  " + String.format("%.1f", (disease_prob.toDouble()*100)) +"%") )
                        println("Name: $disease_name Value: $disease_prob")
                    }
                }

                if (diseaseNameList.isNotEmpty()) {
                    diseaseNameCleanList = diseaseNameList.subList(0, 5).toString().replace("[", "")
                        .replace("]", "").replace(", ", "\n")
                }

            }

            override fun onFailure(call: Call<AnalysisResponse?>, t: Throwable) {}
        })

    }

    // function to analyze suggested specializations (api call)
    fun suggestedSpecializations () {

        if (sessionID == null) {
            Log.e("Invalid Session ID","")
            return
        }
        val jsonAPI = retrofitBuilder.create(EndlessInterface::class.java)
        val suggestedSpecializationsResponse = jsonAPI.getSuggestedSpecializations(sessionID, 10)


        suggestedSpecializationsResponse.enqueue(object : Callback<GetSuggestedSpecializationsResponse?> {
            override fun onResponse(
                call: Call<GetSuggestedSpecializationsResponse?>,
                response: Response<GetSuggestedSpecializationsResponse?>
            ) {
                if (!response.isSuccessful){
                    Log.e("Failed","SuggestedSpecializations: "+response.code())
                    return
                }
                Log.e("Successful", "SuggestedSpecializations")
                val responseBody = response.body()!!

                if(responseBody.status == null) {
                    Log.e("Status", "error")
                }

                // Initialize data
                suggestedSpecializationsList.clear()

                // suggestedSpecializations list for list view
                if (responseBody.SuggestedSpecializations.isNotEmpty()) {
                    for (idx in 0..4) {
                        suggestedSpecializationsList.add(responseBody.SuggestedSpecializations[idx][0])
                    }
                }
            }

            override fun onFailure(call: Call<GetSuggestedSpecializationsResponse?>, t: Throwable) {}
        })

    }



    // function to receive HR value
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents
            .filter { it.type == DataEvent.TYPE_CHANGED &&
                      it.dataItem.uri.path == "/heart_rate" }
            .forEach { event ->
                val heartRateReceived: Int =
                    DataMapItem.fromDataItem(event.dataItem).dataMap.getInt(
                        "HEART_RATE"
                    )
                Log.i("UserMainMenuFragment", "Heart-rate $heartRateReceived")
                _heartRate.value = heartRateReceived

            }
    }

    //    Send symptoms to Firebase
    fun uploadList(){
        ref.child(key).child("symptomList").removeValue()
        selectedSymptomsListText.forEach{ (array_name, name) ->
            ref.child(key).child("symptomList").child(array_name).child("ListValue").setValue(selectedSymptomsListValue[array_name])
            ref.child(key).child("symptomList").child(array_name).child("MapValue").setValue(selectedSymptomsMap[array_name])
            ref.child(key).child("symptomList").child(array_name).child("ValueType").setValue(selectedSymptomType[array_name])
            ref.child(key).child("symptomList").child(array_name).child("ChoiceIndex").setValue(selectedSymptomChoiceIndex[array_name])
        }

    }

    // read symptoms from Firebase

    fun sendFlagToFirebase(value: String){
        ref.child(key).child("acceptFlag").setValue(value) // not in the right place
    }

}
