package com.epfl.esl.endlessapi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.epfl.esl.endlessapi.FragmentViewModel.SearchSymptomsViewModel
import com.epfl.esl.endlessapi.databinding.ActivityMainBinding
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem

class MainActivity : AppCompatActivity(){

    var loginInfo = UserDataClass(username = "", image = null,  userKey = "", sessionID = "", numberOfSymptoms = "0")
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


    }

    override fun onBackPressed() {
        //super.onBackPressed()
        moveTaskToBack(true)
    }

}

