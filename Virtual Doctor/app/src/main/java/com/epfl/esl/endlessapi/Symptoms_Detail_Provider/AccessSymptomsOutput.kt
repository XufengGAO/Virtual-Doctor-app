package com.epfl.esl.endlessapi.Symptoms_Detail_Provider

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class AccessSymptomsOutput: SymptomsDetails, AppCompatActivity() {

    // get symptoms feature by parsing
    override fun getSymtomsDetails(context: Context?): MutableMap<String, FeatureDetails> {
        val result: MutableMap<String, FeatureDetails> = mutableMapOf()
        try {
            val jsonFileString = read(context, "SymptomsOutput.json")
            if (jsonFileString != null) {
                Log.i("data", jsonFileString.length.toString())
            } else {
                Log.e("json file", "null")
            }
            val gson = Gson()
            val listFeatureType = object : TypeToken<List<FeatureDetails>>() {}.type //Extract by FeatureDetails
            val details: List<FeatureDetails> = gson.fromJson(
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

