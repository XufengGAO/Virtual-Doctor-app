package com.epfl.esl.endlessapi

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.epfl.esl.endlessapi.FragmentViewModel.WearSymptomsViewModel
import com.epfl.esl.endlessapi.SubActivity.HistoryActivity
import com.epfl.esl.endlessapi.SubActivity.SymptomsSeekBarActivity
import com.epfl.esl.endlessapi.SubActivity.SymptomsSpinnerActivity
import com.epfl.esl.endlessapi.SymptomsDetailProvider.FeatureDetails
import com.epfl.esl.endlessapi.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask
//Activity()
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener, SensorEventListener,
    MessageClient.OnMessageReceivedListener {

    private val SeekBar_REQUEST = 1
    private val History_REQUEST = 2
    private val Spinner_REQUEST = 3

    private val Confirm_RESULT = "confirm"
    private val Cancel_RESULT = "cancel"
    private val HISTORY_RESULT = "save"
    private val RESULT = "result"

    private lateinit var binding: ActivityMainBinding
    var heartRate:Int = 40
    private var timer = Timer()

    private lateinit var viewModel: WearSymptomsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        viewModel = ViewModelProvider(this).get(WearSymptomsViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                "android" + ""
                        + ".permission.BODY_SENSORS"
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissions(arrayOf("android.permission.BODY_SENSORS"), 0)
        }

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)?.also { heartRate ->
            sensorManager.registerListener(this, heartRate, SensorManager.SENSOR_DELAY_UI)
        }
        
    }

    fun configureSymptoms(mode: String, symptomName: String){
        val selectedSymptom = viewModel.symptomsMap[symptomName]

        if (mode == "/AddSymptom" || mode == "/editSymptom" ) {
            if (viewModel.selectedSymptomsMap.containsKey(symptomName)){
                selectedSymptom?.default = viewModel.selectedSymptomsMap[symptomName]!!
            }
            // To Seek Bar Fragment if no choices
            if (selectedSymptom?.type == "integer" || selectedSymptom?.type == "double") {
                Log.e("symptoms map", "with seekbar")
                val intent = Intent(this, SymptomsSeekBarActivity::class.java)
                intent.putExtra("symptomDetails", selectedSymptom)
                startActivityForResult(intent, SeekBar_REQUEST)
            }
            // To spinner Fragment if there are choices
            else{
                Log.e("symptoms map", "with spinner")
                val intent = Intent(this, SymptomsSpinnerActivity::class.java)
                intent.putExtra("symptomDetails", selectedSymptom)
                startActivityForResult(intent, Spinner_REQUEST)
            }
        }

        if (mode == "/deleteSymptom") {
            viewModel.deleteSymptom(symptomName)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SeekBar_REQUEST && resultCode == RESULT_OK && data != null) {
            val received_result : String? = data.getStringExtra(RESULT)
            when(received_result) {
                Confirm_RESULT  -> {
                    Log.e("Confirm result", "with seekbar")
                    val receivedSymptoms: FeatureDetails = data.getParcelableExtra("symptomDetails")!!
                    val symptomValue: Double = data.getDoubleExtra("SymptomValue", -1.0)
                    viewModel.addSymptom(receivedSymptoms, symptomValue)
                }
                Cancel_RESULT -> {Log.e("Cancel result", "with seekbar")}
            }
        }

        if(requestCode == Spinner_REQUEST && resultCode == RESULT_OK && data != null) {
            val received_result : String? = data.getStringExtra(RESULT)
            when(received_result) {
                Confirm_RESULT  -> {
                    Log.e("Confirm result", "with spinner")
                    val receivedSymptoms: FeatureDetails = data.getParcelableExtra("symptomDetails")!!
                    val symptomIntValue: Int = data.getIntExtra("SymptomIntValue", -1)
                    val symptomChoiceString: String = data.getStringExtra("SymptomChoiceString")!!
                    viewModel.addSymptom(receivedSymptoms, symptomIntValue, symptomChoiceString)

                }
                Cancel_RESULT -> {Log.e("Cancel result", "with spinner")}
            }
        }

        if(requestCode == History_REQUEST && resultCode == RESULT_OK && data != null) {
            val received_result : String? = data.getStringExtra(RESULT)
            when(received_result) {
                HISTORY_RESULT  -> {Log.e("History", "save")}
            }
        }

    }

    fun clickHistoryButton(view: View) {
        // use HR as a symptom
        if (viewModel.selectedSymptomsMap.containsKey("HeartRate")){
            val hrSymptom = viewModel.symptomsMap["HeartRate"]
            if (hrSymptom != null) {
                viewModel.addSymptom(hrSymptom, heartRate.toDouble())

            }

        }

        val intent = Intent(this, HistoryActivity::class.java)
        intent.putExtra("symptomText", ArrayList<String>(viewModel.selectedSymptomsTextList.values.toMutableList()))
        intent.putExtra("symptomValue", ArrayList<String>(viewModel.selectedSymptomsStringValueList.values.toMutableList()))
        Log.e("Go to", "History")
        startActivityForResult(intent, History_REQUEST)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.i("wear", "Onchange")
        dataEvents
            .filter {it.dataItem.uri.path == "/userInfo" }
            .forEach { event ->
                val receivedImage: ByteArray = DataMapItem.fromDataItem(event.dataItem).dataMap.getByteArray("profileImage")
                val receivedUsername: String = DataMapItem.fromDataItem(event.dataItem).dataMap.getString("username")

                val receivedSymptomsTextList: ArrayList<String> = DataMapItem.fromDataItem(event.dataItem).dataMap.getStringArrayList("symptomsTextList")
                val receivedSymptomsStringValueList: ArrayList<String> = DataMapItem.fromDataItem(event.dataItem).dataMap.getStringArrayList("symptomsStringValueList")
                val receivedMapValueList: ArrayList<String> = DataMapItem.fromDataItem(event.dataItem).dataMap.getStringArrayList("symptomsMapValueList")
                val receivedNameList: ArrayList<String> = DataMapItem.fromDataItem(event.dataItem).dataMap.getStringArrayList("symptomsNameList")

                val receivedUsernameBitmap = BitmapFactory.decodeByteArray(receivedImage, 0, receivedImage.size)

                if (receivedSymptomsTextList.size>0){
                    for (i in 0 until (receivedSymptomsTextList.size-1)){
                        viewModel.selectedSymptomsMap[receivedNameList[i]] = receivedMapValueList[i].toDouble()
                        viewModel.selectedSymptomsTextList[receivedNameList[i]] = receivedSymptomsTextList[i]
                        viewModel.selectedSymptomsStringValueList[receivedNameList[i]] = receivedSymptomsStringValueList[i]
                    }
                }

                binding.logoView.setImageBitmap(receivedUsernameBitmap)
                binding.myText.text = receivedUsername
            }

    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if(messageEvent.path == "/command") {
            val receivedCommand: String = String(messageEvent.data)
            if (receivedCommand == "Start") {
                timer = Timer()
                timer.schedule(timerTask {
                    sendDataToMobile(heartRate)
                }, 0, 500)

                // use HR as a symptom
                val hrSymptom = viewModel.symptomsMap["HeartRate"]
                if (hrSymptom != null) {
                    viewModel.addSymptom(hrSymptom, heartRate.toDouble())
                }

            } else if (receivedCommand == "Stop") {
                timer.cancel()
                viewModel.deleteSymptom("HeartRate")
            } else if (receivedCommand == "Logout") {

                Log.e("Confirm go to", "Wear Main")
                val intent = Intent(this, MainActivity::class.java)
                Log.e("Confirm go to", "Main")
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        if(messageEvent.path == "/AddSymptom") {
            val receivedSymptomsName: String = String(messageEvent.data)
            configureSymptoms("/AddSymptom", receivedSymptomsName)
        }

        if(messageEvent.path == "/deleteSymptom") {
            val receivedSymptomsName: String = String(messageEvent.data)
            configureSymptoms("/deleteSymptom", receivedSymptomsName)
        }

        if(messageEvent.path == "/editSymptom") {
            val receivedSymptomsName: String = String(messageEvent.data)
            configureSymptoms("/editSymptom", receivedSymptomsName)

        }

    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
        Wearable.getMessageClient(this).removeListener(this)
    }

    // function to send HR to Mobile
    private fun sendDataToMobile(heartRate: Int) {
        val dataClient: DataClient = Wearable.getDataClient(this)
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/heart_rate").run {
            dataMap.putInt("HEART_RATE", heartRate)
            asPutDataRequest()
        }
        //putDataReq.setUrgent()
        dataClient.putDataItem(putDataReq)
    }


    // function to measure HR
    override fun onSensorChanged(event: SensorEvent?) {
        val heartRateReceived = event!!.values[0].toInt()
        binding.hrSensor.text = heartRateReceived.toString()
        heartRate = heartRateReceived
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}