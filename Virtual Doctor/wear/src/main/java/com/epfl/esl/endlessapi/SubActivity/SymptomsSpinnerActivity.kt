package com.epfl.esl.endlessapi.SubActivity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.epfl.esl.endlessapi.Adapter.ChoiceListAdapter
import com.epfl.esl.endlessapi.Adapter.CustomScrollingLayoutCallback
import com.epfl.esl.endlessapi.Adapter.HistoryListAdapter
import com.epfl.esl.endlessapi.MainActivity
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.SymptomsDetailProvider.FeatureDetails
import com.epfl.esl.endlessapi.databinding.ActivitySymptomsSpinnerBinding
import com.google.android.gms.wearable.*

class SymptomsSpinnerActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private val Confirm_RESULT = "confirm"
    private val Cancel_RESULT = "cancel"
    val RESULT = "result"
    private lateinit var confirmedSymptomsChoice: String
    private  var confirmedSymptomsValue: Int = 0
    lateinit var receivedSymptoms: FeatureDetails
    private val choiceText: MutableList<String> = mutableListOf()
    private val choiceValue: MutableList<Int> = mutableListOf()
    private var currentIndex: Int = 0
    private lateinit var itemAdapter: ChoiceListAdapter

    private var symptomTextChoiceList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        // Inflate the layout for this fragment
        setContentView(R.layout.activity_symptoms_spinner)


        receivedSymptoms = intent.getParcelableExtra("symptomDetails")!!
        choiceText.add(receivedSymptoms.text)
        choiceValue.add(-1)

        // extract the choice text and corresponding values
        receivedSymptoms.choices?.forEach { (text, value) ->
            choiceText.add(text)
            choiceValue.add(value.toInt())
            println("text:${text} value:${value}")
        }
        // find the index corresponding to the current choice value
        // all choice values are integers
        currentIndex = choiceValue.indexOf(receivedSymptoms.default.toInt())

        val recyclerView: WearableRecyclerView = findViewById(R.id.choice_list)
        recyclerView.setHasFixedSize(true)
        recyclerView.isEdgeItemsCenteringEnabled = true
        recyclerView.layoutManager = WearableLinearLayoutManager(this, CustomScrollingLayoutCallback())

        // Adapter class is initialized and list is passed in the param.
        itemAdapter = ChoiceListAdapter(context = this, choiceText = choiceText, currentIndex)

        recyclerView.adapter = itemAdapter

        itemAdapter.setOnItemClickListener(object : ChoiceListAdapter.OnItemClickListener{
            override fun onItemClick(position: Int, textView: View) {
                Toast.makeText(this@SymptomsSpinnerActivity, "I choose $position", Toast.LENGTH_SHORT).show()
                Log.e("Error", "Eoor")
            }
        })

    }

    fun clickSpinnerConfirmButton(view: View) {
        currentIndex = itemAdapter.returnSelectedItemPosition()
        // find the choice text
        confirmedSymptomsChoice = choiceText[currentIndex]
        // find the choice value
        confirmedSymptomsValue = choiceValue[currentIndex]
        sendConfirmedSymptomsToMobile(confirmedSymptomsValue, confirmedSymptomsChoice)
        Log.e(confirmedSymptomsChoice, confirmedSymptomsValue.toString())
        val intent = Intent(this, MainActivity::class.java)
        Log.e("Confirm go to", "Main")
        intent.putExtra(RESULT,Confirm_RESULT)
        intent.putExtra("symptomDetails", receivedSymptoms)
        intent.putExtra("SymptomIntValue", confirmedSymptomsValue)
        intent.putExtra("SymptomChoiceString", confirmedSymptomsChoice)
        setResult(RESULT_OK, intent)
        finish()
    }

    fun clickSpinnerCancelButton(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        Log.e("Cancel go to", "Main")
        intent.putExtra(RESULT,Cancel_RESULT)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents
            .filter {it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == "/ConfirmedByMobileInSpinner" }
            .forEach { event ->
                confirmedSymptomsValue = DataMapItem.fromDataItem(event.dataItem).dataMap.getInt("confirmedSymptomsValue")!!
                confirmedSymptomsChoice = DataMapItem.fromDataItem(event.dataItem).dataMap.getString("confirmedSymptomsChoice")!!
                val intent = Intent(this, MainActivity::class.java)
                Log.e("Confirmed by", "Mobile")
                intent.putExtra(RESULT,Confirm_RESULT)
                intent.putExtra("symptomDetails", receivedSymptoms)
                intent.putExtra("SymptomIntValue", confirmedSymptomsValue)
                intent.putExtra("SymptomChoiceString", confirmedSymptomsChoice)
                setResult(RESULT_OK, intent)
                finish()
            }
    }

    // function to send HR to Mobile
    private fun sendConfirmedSymptomsToMobile(confirmedSymptomsValue: Int, confirmedSymptomsChoice: String) {
        val dataClient: DataClient = Wearable.getDataClient(this)
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/ConfirmedByWearInSpinner").run {
            dataMap.putInt("confirmedSymptomsValue", confirmedSymptomsValue)
            dataMap.putString("confirmedSymptomsChoice", confirmedSymptomsChoice)
            asPutDataRequest()
        }
        putDataReq.setUrgent()
        dataClient.putDataItem(putDataReq)
    }





}