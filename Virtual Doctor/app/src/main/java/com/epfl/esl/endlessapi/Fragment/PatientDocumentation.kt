package com.epfl.esl.endlessapi.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.epfl.esl.endlessapi.Endless_Interface.AnalysisResponse
import com.epfl.esl.endlessapi.Endless_Interface.EndlessInterface
import com.epfl.esl.endlessapi.FragmentViewModel.SearchSymptomsViewModel
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.databinding.FragmentPatientDocumentationBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PatientDocumentation : Fragment() {

    lateinit var binding: FragmentPatientDocumentationBinding
    private val sharedViewModel: SearchSymptomsViewModel by activityViewModels()

    private val LocalTrueIsPatientProvided: MutableMap<String, Boolean> = mutableMapOf()
    private val LocalClinicalData: MutableList<String> = mutableListOf()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_patient_documentation,container,false)

        // Show All Data
        // Chief Complaint Text
        // Checking True Complaint
        sharedViewModel.selectedSymptomsListIsPatientProvided.forEach{ (array_name, booleanValue) ->
            LocalTrueIsPatientProvided[array_name] = booleanValue
            if (booleanValue){
                var FalseComplaint:Boolean = false
                FalseComplaint = sharedViewModel.selectedSymptomsListValue[array_name]!!.contains("No")||
                        sharedViewModel.selectedSymptomsListValue[array_name]!!.contains("0")||
                        sharedViewModel.selectedSymptomsListValue[array_name]!!.contains("Data unavailable")
                if (FalseComplaint){
                    LocalTrueIsPatientProvided[array_name] = false
                }
            }
        }
        // Show Chief Complaint: Expression about Age Gender Race
        val userAge: String? = sharedViewModel.selectedSymptomsListValue["Age"]?.replace(".0","")
        var userGender: String? = sharedViewModel.selectedSymptomsListValue["Gender"]?.replace("I am ","")
        if ( userGender?.contains("Data unavailable") == true ) userGender = null
        var userRace: String? = sharedViewModel.selectedSymptomsListValue["Race"]
        if ( (userRace?.contains("Data unavailable") == true) or (userRace == "Other") ) userRace = null
        val complaintCounts = LocalTrueIsPatientProvided.count{ it.value }
        // Condition for showing Chief Complaint
        if (complaintCounts > 1){
            if (userAge!=null){
                binding.ChiefComplaintText.text =
                    "$userAge years old" +
                            userGender.orEmpty().replaceFirst(""," ") + userRace.orEmpty()+
                            "patient has more than one complaints."
            }else{
                binding.ChiefComplaintText.text =
                    "The" +
                            userGender.orEmpty().replaceFirst(""," ") + userRace.orEmpty()+
                            "patient has more than one complaints."
            }
        }
        else if (complaintCounts == 1){
            if (userAge!=null){
                binding.ChiefComplaintText.text =
                    "$userAge years old" +
                            userGender.orEmpty().replaceFirst(""," ") + userRace.orEmpty()+
                            "patient has one complaints."
            }else{
                binding.ChiefComplaintText.text =
                    "The" + userGender.orEmpty().replaceFirst(""," ")+ userRace.orEmpty()+
                            "patient has one complaints."
            }
        }
        else {
            if (userAge!=null){
                binding.ChiefComplaintText.text =
                    "$userAge years old" +
                            userGender.orEmpty().replaceFirst(""," ") + userRace.orEmpty()+
                            "patient didn't have any specify complaint."
            }else{
                binding.ChiefComplaintText.text =
                    "The" +
                            userGender.orEmpty().replaceFirst(""," ") + userRace.orEmpty()+
                            "patient didn't have any specify complaint."
            }
        }

        // Assessment Text
        // Show Assessment
        sharedViewModel.assessmentList.clear()
        sharedViewModel.selectedSymptomsListIsPatientProvided.forEach{ (array_name, booleanValue) ->
            if (booleanValue){
                if (LocalTrueIsPatientProvided[array_name] == true){
                    sharedViewModel.assessmentList.add("Has " + sharedViewModel.symptomsMap[array_name]!!.name.replace(Regex("(.)([A-Z])"), "$1 $2" ) )
                }else{
                    sharedViewModel.assessmentList.add("No " + sharedViewModel.symptomsMap[array_name]!!.name.replace(Regex("(.)([A-Z])"), "$1 $2"  ) )
                }
            }
        }
        if (sharedViewModel.assessmentList.isEmpty()){
            binding.AssessmentText.text = "No assessment is provided"
        }
        else{
            binding.AssessmentText.text = sharedViewModel.assessmentList.toString()
                .replace("[","").replace("]","").replace(", ", "\n")
        }

        // Problem Text
        // Show Disease
        binding.ProblemsText.text = sharedViewModel.diseaseNameCleanList

        // Recommended Specialist
        // Show Specialist
        binding.RecommendedSpecialistText.text = sharedViewModel.suggestedSpecializationsList.toString()
            .replace("[","").replace("]","").replace(", ", "\n")

        // Clinical Data Provided by Patient Text
        // Show Clinical Data
        LocalClinicalData.clear()
        sharedViewModel.selectedSymptomsListIsPatientProvided.forEach { (array_name, booleanValue) ->
            if ((!booleanValue) and
                (sharedViewModel.symptomsMap[array_name]!!.type == "integer" || sharedViewModel.symptomsMap[array_name]!!.type == "double")
            ) {
                if (sharedViewModel.symptomsMap[array_name]!!.name !="Age") {
                    LocalClinicalData.add(sharedViewModel.symptomsMap[array_name]!!.name + ": " + sharedViewModel.selectedSymptomsListValue[array_name])
                }
            }
        }
        binding.InfoProvideByPatientText.text = LocalClinicalData.toString()
            .replace("[","").replace("]","").replace(", ", "\n")

        Log.e("symptomsNames", sharedViewModel.selectedSymptomsListText.toString())
        Log.e("symptomsValue", sharedViewModel.selectedSymptomsListValue.toString())
        Log.e("RecommendedSpecial",sharedViewModel.suggestedSpecializationsList.toString())


        // Button
        // Return to Analyze Frag
        binding.backtoanalyzzbutton.setOnClickListener { view: View ->
            val directions = PatientDocumentationDirections.actionPatientdocumentationfragmentToAnalyzefragment()
            view.findNavController().navigate(directions)
        }

        // Specialist Recommendation
        binding.specialListToLausanneButton.setOnClickListener { view:View ->
            val direction = PatientDocumentationDirections.actionPatientdocumentationfragmentToSpecialistRecommendFragment()
            view.findNavController().navigate(direction)
        }

        return binding.root
    }
}