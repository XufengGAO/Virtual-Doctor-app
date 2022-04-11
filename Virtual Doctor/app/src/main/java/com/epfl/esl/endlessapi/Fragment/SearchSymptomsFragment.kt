package com.epfl.esl.endlessapi.Fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.epfl.esl.endlessapi.R
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.epfl.esl.endlessapi.Adapter.ItemAdapter
import com.epfl.esl.endlessapi.FragmentViewModel.SearchSymptomsViewModel
import com.epfl.esl.endlessapi.MainActivity
import com.epfl.esl.endlessapi.Symptoms_Detail_Provider.FeatureDetails
import com.epfl.esl.endlessapi.Symptoms_Detail_Provider.FeatureDetailsChoice
import com.epfl.esl.endlessapi.databinding.FragmentSearchBinding
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*


class SearchSymptomsFragment : Fragment() {


    private lateinit var binding: FragmentSearchBinding
    private val sharedViewModel: SearchSymptomsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for SearchSymptoms fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search,container,false)

        // To load recyclerview
        binding.selectedSymptomList.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL, false)

        // UI for search symptom list
        val symptomAdapter = activity?.let { ArrayAdapter<String>(
            it,android.R.layout.simple_list_item_1,sharedViewModel.symptomsTextList) }
        binding.searchSymptomList.adapter = symptomAdapter

        // Click listener for Save button
        if (!sharedViewModel.selectedSymptomsMap.isNotEmpty()){
            binding.saveButton.text = "BACK to Main"
        } else {
            binding.saveButton.text = "Save"
        }
        binding.saveButton.setOnClickListener { view: View ->
            sharedViewModel.uploadList()
            val directions = SearchSymptomsFragmentDirections.actionSearchfragmentToUsermainmenu()
            view.findNavController().navigate((directions))
        }

        // Search Bar and Searching function create
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchBar.clearFocus()
                if (sharedViewModel.symptomsTextList.contains(query)){
                    symptomAdapter?.filter?.filter(query)
                    Toast.makeText(context, "I choose $query", Toast.LENGTH_SHORT).show()
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                symptomAdapter?.filter?.filter(newText)
                return false
            }
        })


        // Click search symptom list
        binding.searchSymptomList.setOnItemClickListener { parent, view, position, id ->
            // extract the selected symptom
            val selectedSymptom = sharedViewModel.symptomsMap.getValue(
                sharedViewModel.symptomsNameList[sharedViewModel.symptomsTextList.indexOf(
                    parent.getItemAtPosition(position))])

            // user may click the selected symptom from symptom list instead of the selected symptom list
            if (sharedViewModel.selectedSymptomsMap.isNotEmpty()){
                if (sharedViewModel.selectedSymptomsMap.containsKey(selectedSymptom.name)){
                    selectedSymptom.default = sharedViewModel.selectedSymptomsMap[selectedSymptom.name]!!
                }
            }

            // send selected symptoms to smartwatch
            //val dataClient: DataClient = Wearable.getDataClient(activity as AppCompatActivity)
            //selectSymptomsInWear(dataClient, selectedSymptom.name, "/AddSymptom")
            editSymptomsToWear("/AddSymptom", selectedSymptom.name)

            // To Seek Bar Fragment if no choices
            if (selectedSymptom.type == "integer" || selectedSymptom.type == "double") {
                val directions = SearchSymptomsFragmentDirections.actionSearchfragmentToSymptomseekbar(selectedSymptom)
                view.findNavController().navigate((directions))
                Log.e("symptoms map", "with seekbar")
            }

            // To spinner Fragment if there are choices
            else{
                val directions = SearchSymptomsFragmentDirections.actionSearchfragmentToSymptomspiner(selectedSymptom)
                view.findNavController().navigate((directions))
                Log.e("symptoms map", "with spinner")
            }
        }


        // Conclusion list for selected symptoms
        // extract all current selected symptoms
        val newSelectedSymptomsStringListText = sharedViewModel.selectedSymptomsListText.values.toMutableList()
        val newSelectedSymptomsStringListValue = sharedViewModel.selectedSymptomsListValue.values.toMutableList()

        // adjust the title
        if (newSelectedSymptomsStringListText.size == 0){
            binding.selectedSymptomListTitle.text = "No Selected Features Yet..."
        } else {
            binding.selectedSymptomListTitle.text = "The Selected Features"
        }

        // Adapter class is initialized and list is passed in the param.
        val itemAdapter = context?.let { ItemAdapter(context = it,
            itemsText = newSelectedSymptomsStringListText, itemsValue = newSelectedSymptomsStringListValue) }

        // Adapter instance is set to the recyclerview to inflate the items.
        binding.selectedSymptomList.adapter = itemAdapter

        // Provide self-defined click-listener functions for buttons
        itemAdapter?.setOnItemClickListener(object : ItemAdapter.OnItemClickListener{
            // function for delete button
            override fun onItemClick(position: Int, buttonView: View) {
                //Toast.makeText(context, "I choose delete button $position", Toast.LENGTH_SHORT).show()

                // Remove the selected symptoms from recyclerview UI
                val alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder.setTitle("Delete Symptom")
                alertDialogBuilder.setMessage("Are you sure you want to delete the symptom?")
                alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, which ->
                    itemAdapter.itemsText.removeAt(position)
                    itemAdapter.itemsValue.removeAt(position)
                    itemAdapter.notifyItemRemoved(position)

                    // Remove the selected symptoms from viewModel database
                    val newSelectedSymptomsMap = sharedViewModel.selectedSymptomsMap.keys.toMutableList()
                    sharedViewModel.deleteSymptom(newSelectedSymptomsMap[position])

                    // delete selected symptoms to smartwatch
                    //val dataClient: DataClient = Wearable.getDataClient(activity as AppCompatActivity)
                    //selectSymptomsInWear(dataClient, newSelectedSymptomsMap[position], "/deleteSymptom")
                    editSymptomsToWear("/deleteSymptom", newSelectedSymptomsMap[position])

                    if (itemAdapter.itemCount == 0){
                        binding.selectedSymptomListTitle.text = "No Selected Features Yet..."
                    }
                }
                alertDialogBuilder.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int -> })
                val alertDialog = alertDialogBuilder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()

            } }, object : ItemAdapter.OnItemClickListener{
            // function for edit button
            override fun onItemClick(position: Int, buttonView: View) {
                // extract the selected symptom
                val selectedSymptom = sharedViewModel.symptomsMap.getValue(
                    sharedViewModel.selectedSymptomsMap.keys.toList()[position])

                // update the default value with current user-set value
                selectedSymptom.default = sharedViewModel.selectedSymptomsMap.values.toList()[position]

                editSymptomsToWear("/editSymptom", selectedSymptom.name)

                // navigation
                if (selectedSymptom.choices == null) {
                    val directions = SearchSymptomsFragmentDirections.actionSearchfragmentToSymptomseekbar(selectedSymptom)
                    buttonView.findNavController().navigate((directions))
                }
                else{
                    val directions = SearchSymptomsFragmentDirections.actionSearchfragmentToSymptomspiner(selectedSymptom)
                    buttonView.findNavController().navigate((directions))
                }
            }
            })


        return binding.root
    }

    fun selectSymptomsInWear(dataClient: DataClient, selectedSymptomName: String, mode: String) {
        Log.e("The wear mode is", mode)
        val request: PutDataRequest = PutDataMapRequest.create(mode).run {
            dataMap.putString("name",selectedSymptomName)
            asPutDataRequest()
        }
        request.setUrgent()
        dataClient.putDataItem(request)
    }

    private fun editSymptomsToWear(command: String, selectedSymptomName: String){
        Log.e(command, selectedSymptomName)
        Thread(Runnable {
            val connectedNodes: List<String> =
                Tasks.await(Wearable.getNodeClient(activity as MainActivity).connectedNodes)
                    .map { it.id }

            connectedNodes.forEach {
                val messageClient: MessageClient =
                    Wearable.getMessageClient(activity as MainActivity)
                messageClient.sendMessage(it, command, selectedSymptomName.toByteArray())
                Log.e(selectedSymptomName, command)
            }

        }).start()
    }

}