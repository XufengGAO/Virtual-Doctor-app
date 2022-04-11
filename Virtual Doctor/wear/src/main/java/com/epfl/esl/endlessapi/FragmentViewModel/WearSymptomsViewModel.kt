package com.epfl.esl.endlessapi.FragmentViewModel
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
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
import androidx.lifecycle.AndroidViewModel
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.SymptomsDetailProvider.AccessSymptomsOutput
import com.epfl.esl.endlessapi.SymptomsDetailProvider.FeatureDetails
import com.epfl.esl.endlessapi.SymptomsDetailProvider.FeatureDetailsChoice
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import java.io.ByteArrayOutputStream

class WearSymptomsViewModel(application: Application) : AndroidViewModel(application){

    private val context = getApplication<Application>().applicationContext
    // Object of AccessSymptomsOutput for extracting symptoms' name
    val selectedSymptomsTextList: MutableMap<String, String> = mutableMapOf()
    val selectedSymptomsStringValueList: MutableMap<String, String> = mutableMapOf()

    // symptom text + choice value (api updated)
    val selectedSymptomsMap: MutableMap<String, Double> = mutableMapOf()

    // For storing all Symptoms Data
    val symptomsMap: MutableMap<String, FeatureDetails>


    init {
        Log.d("SearchFragment", "SearchViewModel created!")
        symptomsMap = getSymptomList() // Get symptoms' name

    }

    override fun onCleared() {
        super.onCleared()
        Log.d("SearchFragment", "SearchViewModel destroyed!")
    }

    // function to add symptoms from seekBar fragment
    fun addSymptom(result: FeatureDetails, value: Double) {
        selectedSymptomsMap[result.name] = value
        selectedSymptomsTextList[result.name] = result.text
        if (result.type == "double"){
            selectedSymptomsStringValueList[result.name] = String.format("%.1f", value)
        } else {
            selectedSymptomsStringValueList[result.name] = value.toInt().toString()
        }
    }

    // function to add symptoms from spinner fragment
    fun addSymptom(result: FeatureDetails, value: Int, choiceString: String) {
        selectedSymptomsMap[result.name] = value.toDouble()
        selectedSymptomsTextList[result.name] = result.text
        selectedSymptomsStringValueList[result.name] = choiceString
    }

    fun deleteSymptom(name: String) {
        selectedSymptomsMap.remove(name)
        selectedSymptomsTextList.remove(name)
        selectedSymptomsStringValueList.remove(name)
    }

    // Get Symptoms List
    fun getSymptomList(): MutableMap<String, FeatureDetails> {

        val symptomsProvider = AccessSymptomsOutput()

        // Access Data from getSymptomsDetails
        val featureDetails: MutableMap<String, FeatureDetails> = symptomsProvider.getSymtomsDetails(context)

        // Finding feature without choices
        featureDetails.forEach { (_, featureContent) ->
            if(featureContent.step == 0.0 && featureContent.choices == null) {
                featureContent.step = 1.0
            }
        }
        return featureDetails
    }


}
