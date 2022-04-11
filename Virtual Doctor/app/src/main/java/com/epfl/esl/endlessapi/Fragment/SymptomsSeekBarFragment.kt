package com.epfl.esl.endlessapi.Fragment

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.epfl.esl.endlessapi.FragmentViewModel.SearchSymptomsViewModel
import com.epfl.esl.endlessapi.MainActivity
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.Symptoms_Detail_Provider.FeatureDetails
import com.epfl.esl.endlessapi.databinding.FragmentSymptomsSeekBarBinding
import com.google.android.gms.wearable.*

class SymptomsSeekBarFragment : Fragment(), DataClient.OnDataChangedListener {

    private lateinit var binding: FragmentSymptomsSeekBarBinding
    private val sharedViewModel: SearchSymptomsViewModel by activityViewModels()
    private var currentProgress: Double = 0.0
    lateinit var result: FeatureDetails
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_symptoms_seek_bar, container, false)

        val args: SymptomsSeekBarFragmentArgs by navArgs()
        result = args.symptomDetails!!

        binding.SymptomName.text = result.text

        // set default as current progress
        currentProgress = result.default

        // check the type
        if (result.type == "double"){
            binding.SymptomValue.setText(currentProgress.toString())
        } else {
            binding.SymptomValue.setText(currentProgress.toInt().toString())
        }

        // Max & min value setting for seek bar
        binding.valueSeekBar.max = ((result.max - result.min)/result.step).toInt()
        binding.valueSeekBar.progress = ((result.default - result.min)/result.step).toInt()

        // Seek bar change Listener
        binding.valueSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // calculate current progress
                currentProgress = progress * result.step + result.min
                // check the type
                if (result.type == "double"){
                    binding.SymptomValue.setText(String.format("%.1f", currentProgress))
                } else {
                    binding.SymptomValue.setText(currentProgress.toInt().toString())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Confirm Button
        binding.ConfirmButton.setOnClickListener{ view: View ->
            sendConfirmedSymptomsToWear(currentProgress)
            sharedViewModel.addSymptom(result, currentProgress)
            val directions = SymptomsSeekBarFragmentDirections.actionSymptomseekbarToSearchfragment()
            view.findNavController().navigate(directions)
        }

        // Cancel Button
        binding.CancelButton.setOnClickListener { view: View ->



            val directions = SymptomsSeekBarFragmentDirections.actionSymptomseekbarToSearchfragment()
            view.findNavController().navigate((directions))
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
            .filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == "/ConfirmedByWearInSeekBar" }
            .forEach { event ->
                currentProgress = DataMapItem.fromDataItem(event.dataItem).dataMap.getDouble("currentProgress")

                sharedViewModel.addSymptom(result, currentProgress)
                val directions = SymptomsSeekBarFragmentDirections.actionSymptomseekbarToSearchfragment()
                binding.ConfirmButton.findNavController().navigate(directions)
            }
    }

    // function to send HR to Mobile
    private fun sendConfirmedSymptomsToWear(currentProgress: Double) {
        val dataClient: DataClient = Wearable.getDataClient(activity as AppCompatActivity)
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/ConfirmedByMobileInSeekBar").run {
            dataMap.putDouble("currentProgress", currentProgress)
            asPutDataRequest()
        }
        putDataReq.setUrgent()
        dataClient.putDataItem(putDataReq)
    }


}