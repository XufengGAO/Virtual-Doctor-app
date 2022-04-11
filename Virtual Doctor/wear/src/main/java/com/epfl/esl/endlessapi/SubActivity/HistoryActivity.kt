package com.epfl.esl.endlessapi.SubActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.epfl.esl.endlessapi.Adapter.CustomScrollingLayoutCallback
import com.epfl.esl.endlessapi.Adapter.HistoryListAdapter
import com.epfl.esl.endlessapi.MainActivity

class HistoryActivity : AppCompatActivity() {

    private var receivedSymptomsText: MutableList<String> = mutableListOf()
    private var receivedSymptomsValue: MutableList<String> = mutableListOf()
    private val HISTORY_RESULT = "save"
    private val RESULT = "result"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(com.epfl.esl.endlessapi.R.layout.activity_history)

        receivedSymptomsText = intent.getStringArrayListExtra("symptomText")!!.toMutableList()
        receivedSymptomsValue = intent.getStringArrayListExtra("symptomValue")!!.toMutableList()

        val recyclerView: WearableRecyclerView = findViewById(com.epfl.esl.endlessapi.R.id.history_list)
        recyclerView.setHasFixedSize(true)
        recyclerView.isEdgeItemsCenteringEnabled = true
        recyclerView.layoutManager = WearableLinearLayoutManager(this, CustomScrollingLayoutCallback())

        // Adapter class is initialized and list is passed in the param.
        val itemAdapter = HistoryListAdapter(context = this,
            itemsText = receivedSymptomsText, itemsValue = receivedSymptomsValue)

        recyclerView.adapter = itemAdapter
    }

    fun clickSaveButton(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        Log.e("History go to", "Main")
        intent.putExtra(RESULT,HISTORY_RESULT)
        setResult(RESULT_OK, intent)
        finish()
    }
}