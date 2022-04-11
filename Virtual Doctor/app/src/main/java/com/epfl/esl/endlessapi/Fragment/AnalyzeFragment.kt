package com.epfl.esl.endlessapi.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.epfl.esl.endlessapi.Endless_Interface.AnalysisResponse
import com.epfl.esl.endlessapi.Endless_Interface.EndlessInterface
import com.epfl.esl.endlessapi.FragmentViewModel.SearchSymptomsViewModel
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.databinding.FragmentAnalyzeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AnalyzeFragment : Fragment() {

    private lateinit var binding: FragmentAnalyzeBinding

    // Initial Variable for Endless
    private val sharedViewModel: SearchSymptomsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_analyze, container, false)


        if (sharedViewModel.diseaseNameAndProbabilityList.isEmpty()
            || sharedViewModel.diseaseProbabilityList[0].toDouble()==0.0 )
            {
            binding.diseaseslist.visibility = View.GONE
            binding.analyzeText.visibility = View.VISIBLE
            binding.DocumentationButton.isEnabled = false
        } else {
            // Function of extracting analyze result
            val symptomAdapter = activity?.let {
                ArrayAdapter(it, android.R.layout.simple_list_item_1,
                    sharedViewModel.diseaseNameAndProbabilityList
                )
            }
            binding.diseaseslist.adapter = symptomAdapter
            binding.diseaseslist.visibility = View.VISIBLE
            binding.analyzeText.visibility = View.GONE
            binding.DocumentationButton.isEnabled = true
        }

        // Button
        // Return to Main Menu
        binding.FinishButton.setOnClickListener { view: View->
            val directions = AnalyzeFragmentDirections.actionAnalyzefragmentToUsermainmenu()
            view.findNavController().navigate(directions)
        }

        // Button
        // Direct to Documentation
        binding.DocumentationButton.setOnClickListener { view: View->
            val directions = AnalyzeFragmentDirections.actionAnalyzefragmentToPatientdocumentationfragment()
            view.findNavController().navigate(directions)
        }

        return binding.root
    }




}