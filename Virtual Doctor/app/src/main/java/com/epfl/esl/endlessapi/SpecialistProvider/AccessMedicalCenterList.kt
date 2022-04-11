package com.epfl.esl.endlessapi.SpecialistProvider

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.epfl.esl.endlessapi.Symptoms_Detail_Provider.FeatureDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class AccessMedicalCenterList: SpecialistInfoInterface, AppCompatActivity() {


    override fun getSpecialistDetails(context: Context?): MutableMap<String, SpecailistDetailsItem> {

        val result: MutableMap<String, SpecailistDetailsItem> = mutableMapOf()
        try {
            val jsonFileString = read(context, "MedicalCenterList.json")
            if (jsonFileString != null) {
                Log.i("data", jsonFileString.length.toString())
            } else {
                Log.e("json file", "null")
            }
            val gson = Gson()
            val listFeatureType = object : TypeToken<List<SpecailistDetailsItem>>() {}.type //Extract by FeatureDetails
            val details: List<SpecailistDetailsItem> = gson.fromJson(
                jsonFileString,
                listFeatureType
            )
            for (detail in details) {
                result[detail.name] = detail //Storing NAME as array header, Ex: Age=FeatureDetails(text...)
            }

        } catch (e: IOException) {
            Log.e("Error opening json file", "")
        }
        return result
    }

    // Read function for reading Json File by using bufferedReader
    private fun read(context: Context?, file: String): String? {
        var jsonString: String? = null
        try {
            if (context != null) {
                jsonString = context.assets.open(file).bufferedReader().use { it.readText() }
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

}