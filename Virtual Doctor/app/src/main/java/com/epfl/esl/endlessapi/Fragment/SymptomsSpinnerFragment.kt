package com.epfl.esl.endlessapi.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.epfl.esl.endlessapi.FragmentViewModel.SearchSymptomsViewModel
import com.epfl.esl.endlessapi.MainActivity
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.Symptoms_Detail_Provider.FeatureDetails
import com.epfl.esl.endlessapi.databinding.FragmentSymptomsSpinnerBinding
import com.google.android.gms.wearable.*

class SymptomsSpinnerFragment : Fragment(), DataClient.OnDataChangedListener {

    private lateinit var binding: FragmentSymptomsSpinnerBinding
    private lateinit var confirmedSymptomsChoice: String
    private  var confirmedSymptomsValue: Int = 0
    private val sharedViewModel: SearchSymptomsViewModel by activityViewModels()
    private val choiceText: MutableList<String> = mutableListOf()
    private val choiceValue: MutableList<Int> = mutableListOf()
    private var currentIndex: Int = 0
    lateinit var result: FeatureDetails

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_symptoms_spinner, container, false)

        val args: SymptomsSpinnerFragmentArgs by navArgs()
        result = args.symptomDetails!!

        binding.SymptomName.text = result.text

        // extract the choice text and corresponding values
        result.choices?.forEach { (text, value) ->
            choiceText.add(text)
            choiceValue.add(value.toInt())
            println("text:${text} value:${value}")
        }

        // UI for spinner
        val symptomAdapter = activity?.let { ArrayAdapter<String>(
            it,R.layout.custom_spinner_adapter,choiceText) }
        binding.SymptomSpinner.adapter = symptomAdapter

        // find the index corresponding to the current choice value
        // all choice values are integers
        currentIndex = choiceValue.indexOf(result.default.toInt())

        // spinner UI
        binding.SymptomSpinner.setSelection(currentIndex)

        // find the choice text
        confirmedSymptomsChoice = choiceText[currentIndex]
        // find the choice value
        confirmedSymptomsValue = result.default.toInt()

        // click listener for spinner
        binding.SymptomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
               parent: AdapterView<*>?,
               view: View?,
               position: Int,
               id: Long
            ) {
                // find the choice text and choice value
                confirmedSymptomsChoice = choiceText[position]
                confirmedSymptomsValue = choiceValue[position]
                currentIndex = position
                Toast.makeText(context,
                    "I choose $confirmedSymptomsChoice$confirmedSymptomsValue", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // click listener for confirm button
        binding.ConfirmButton.setOnClickListener{ view: View ->
            sendConfirmedSymptomsToWear(confirmedSymptomsValue, confirmedSymptomsChoice)
            // add symptoms: choice value + choice text
            sharedViewModel.addSymptom(result, confirmedSymptomsValue, confirmedSymptomsChoice,currentIndex)
            val directions = SymptomsSpinnerFragmentDirections.actionSymptomspinnerToSearchfragment()
            view.findNavController().navigate(directions)
        }

        // click listener for cancel button
        binding.CancelButton.setOnClickListener{ view: View ->
            val directions = SymptomsSpinnerFragmentDirections.actionSymptomspinnerToSearchfragment()
            view.findNavController().navigate(directions)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(activity as MainActivity).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(activity as MainActivity).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents
            .filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == "/ConfirmedByWearInSpinner" }
            .forEach { event ->
                confirmedSymptomsValue = DataMapItem.fromDataItem(event.dataItem).dataMap.getInt("confirmedSymptomsValue")!!
                confirmedSymptomsChoice = DataMapItem.fromDataItem(event.dataItem).dataMap.getString("confirmedSymptomsChoice")!!
                currentIndex = choiceText.indexOf(confirmedSymptomsChoice)

                sharedViewModel.addSymptom(result, confirmedSymptomsValue, confirmedSymptomsChoice,currentIndex)
                val directions = SymptomsSpinnerFragmentDirections.actionSymptomspinnerToSearchfragment()
                binding.ConfirmButton.findNavController().navigate(directions)
            }
    }

    // function to send Confirmed Symptoms To Wear
    private fun sendConfirmedSymptomsToWear(confirmedSymptomsValue: Int, confirmedSymptomsChoice: String) {
        val dataClient: DataClient = Wearable.getDataClient(activity as AppCompatActivity)
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/ConfirmedByMobileInSpinner").run {
            dataMap.putInt("confirmedSymptomsValue", confirmedSymptomsValue)
            dataMap.putString("confirmedSymptomsChoice", confirmedSymptomsChoice)
            asPutDataRequest()
        }
        putDataReq.setUrgent()
        dataClient.putDataItem(putDataReq)
    }

    
}