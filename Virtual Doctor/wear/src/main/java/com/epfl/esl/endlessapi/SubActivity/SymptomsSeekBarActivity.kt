package com.epfl.esl.endlessapi.SubActivity

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.epfl.esl.endlessapi.Adapter.ChoiceListAdapter
import com.epfl.esl.endlessapi.Adapter.CustomScrollingLayoutCallback
import com.epfl.esl.endlessapi.MainActivity
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.SymptomsDetailProvider.FeatureDetails
import com.epfl.esl.endlessapi.databinding.ActivitySymptomsSeekBarBinding
import com.google.android.gms.wearable.*

class SymptomsSeekBarActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private val Confirm_RESULT = "confirm"
    private val Cancel_RESULT = "cancel"
    private val RESULT = "result"

    lateinit var receivedSymptoms: FeatureDetails
    private var currentProgress: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        // Inflate the layout for this fragment
        setContentView(R.layout.activity_symptoms_seek_bar)

        receivedSymptoms = intent.getParcelableExtra("symptomDetails")!!

        val recyclerView: WearableRecyclerView = findViewById(R.id.SymptomName)
        recyclerView.setHasFixedSize(true)
        recyclerView.isEdgeItemsCenteringEnabled = true
        recyclerView.layoutManager = WearableLinearLayoutManager(this, CustomScrollingLayoutCallback())
        val choiceText: MutableList<String> = mutableListOf(receivedSymptoms.text)
        val itemAdapter = ChoiceListAdapter(context = this, choiceText = choiceText, 0)
        recyclerView.adapter = itemAdapter

        itemAdapter.setOnItemClickListener(object : ChoiceListAdapter.OnItemClickListener{
            override fun onItemClick(position: Int, textView: View) {
            }
        })

        // set default as current progress
        currentProgress = receivedSymptoms.default


        val symptomValue: TextView =  findViewById(R.id.SymptomValue)
        // check the type
        if (receivedSymptoms.type == "double"){
            symptomValue.text = currentProgress.toString()
        } else {
            symptomValue.text = currentProgress.toInt().toString()
        }

        val valueSeekBar: SeekBar =  findViewById(R.id.valueSeekBar)
        // Max & min value setting for seek bar
        valueSeekBar.max = ((receivedSymptoms.max - receivedSymptoms.min)/receivedSymptoms.step).toInt()
        valueSeekBar.progress = ((receivedSymptoms.default - receivedSymptoms.min)/receivedSymptoms.step).toInt()

        // Seek bar change Listener
        valueSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // calculate current progress
                currentProgress = progress * receivedSymptoms.step + receivedSymptoms.min
                // check the type
                if (receivedSymptoms?.type == "double"){
                    symptomValue.text = String.format("%.1f", currentProgress)
                } else {
                    symptomValue.text = currentProgress.toInt().toString()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    fun clickSeekBarConfirmButton(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        Log.e("Confirm go to", "Main")
        sendConfirmedSymptomsToMobile(currentProgress)
        intent.putExtra(RESULT,Confirm_RESULT)
        intent.putExtra("symptomDetails", receivedSymptoms)
        intent.putExtra("SymptomValue", currentProgress)
        setResult(RESULT_OK, intent)

        finish()
        //startActivityForResult(intentMainActivity, Confirm_RESULT)
    }

    fun clickSeekBarCancelButton(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        Log.e("Cancel go to", "Main")
        intent.putExtra(RESULT,Cancel_RESULT)
        setResult(RESULT_OK, intent)
        finish()
        //startActivityForResult(intentMainActivity, Cancel_RESULT)
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
            .filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == "/ConfirmedByMobileInSeekBar" }
            .forEach { event ->
                currentProgress = DataMapItem.fromDataItem(event.dataItem).dataMap.getDouble("currentProgress")

                val intent = Intent(this, MainActivity::class.java)
                Log.e("Confirm go to", "Main")

                intent.putExtra(RESULT,Confirm_RESULT)
                intent.putExtra("symptomDetails", receivedSymptoms)
                intent.putExtra("SymptomValue", currentProgress)
                setResult(RESULT_OK, intent)

                finish()
            }
    }

    // function to send HR to Mobile
    private fun sendConfirmedSymptomsToMobile(currentProgress: Double) {
        val dataClient: DataClient = Wearable.getDataClient(this)
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/ConfirmedByWearInSeekBar").run {
            dataMap.putDouble("currentProgress", currentProgress)
            asPutDataRequest()
        }
        putDataReq.setUrgent()
        dataClient.putDataItem(putDataReq)
    }


}