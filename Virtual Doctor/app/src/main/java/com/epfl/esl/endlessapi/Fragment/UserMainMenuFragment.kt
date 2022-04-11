package com.epfl.esl.endlessapi.Fragment

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.epfl.esl.endlessapi.FragmentViewModel.SearchSymptomsViewModel
import com.epfl.esl.endlessapi.MainActivity
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.UserDataClass
import com.epfl.esl.endlessapi.databinding.FragmentUserMainMenuBinding
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class UserMainMenuFragment : Fragment() {

    private val sharedViewModel: SearchSymptomsViewModel by activityViewModels()
    private lateinit var binding: FragmentUserMainMenuBinding

    var storageRef = Firebase.storage.reference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for UserLogIn fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_main_menu, container, false
        )



        val args: UserMainMenuFragmentArgs by navArgs()
        if (args.profile == null) {
            binding.userName.text = (activity as MainActivity).loginInfo.username
            binding.numberOfFeature.text = sharedViewModel.selectedSymptomsMap.size.toString()

            if (sharedViewModel.userImage == null) {
                var imageRef = storageRef.child("ProfileImages/" + (activity as MainActivity).loginInfo.username + ".jpg")
                imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { byteArray ->
                    val image: Drawable = BitmapDrawable(
                        resources,
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    )
                    sharedViewModel.userImage = image
                    binding.userImageView.setImageDrawable(image)
                }.addOnFailureListener {
                    Toast.makeText(context, "Image read was unsuccessful.", Toast.LENGTH_SHORT)
                        .show()
                }
            } else{
                binding.userImageView.setImageDrawable(sharedViewModel.userImage)
            }
        } else {
            binding.userName.text = args.profile?.username
            binding.numberOfFeature.text = args.profile?.numberOfSymptoms

            if (sharedViewModel.userImage == null) {
                var imageRef = storageRef.child("ProfileImages/" + args.profile?.username + ".jpg")
                imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { byteArray ->
                    val image: Drawable = BitmapDrawable(
                        resources,
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    )
                    sharedViewModel.userImage = image
                    binding.userImageView.setImageDrawable(image)
                }.addOnFailureListener {
                    Toast.makeText(context, "Image read was unsuccessful.", Toast.LENGTH_SHORT)
                        .show()
                }
            }else{
                binding.userImageView.setImageDrawable(sharedViewModel.userImage)
            }
        }



        // Heart rate check box and collection
        if (sharedViewModel.selectedSymptomsMap.containsKey("HeartRate")){
            binding.HRCheckBox.isChecked = true

        } else {
            binding.HRCheckBox.isChecked = sharedViewModel.hrCheckBoxFlag
        }
        binding.HRCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                createNotificationChannel()
                sharedViewModel.hrCheckBoxFlag = isChecked
                sendCommandToWear("Start")
            } else {
                sharedViewModel.hrCheckBoxFlag = isChecked
                binding.HRText.text = "Unavailable"
                sendCommandToWear("Stop")
            }
        }
        sharedViewModel.heartRate.observeForever(Observer { newHR ->

            if(binding.HRCheckBox.isChecked) {
                if (newHR > sharedViewModel.MAX_HR || newHR < sharedViewModel.MIN_HR) {
                    sendNotification(newHR)
                    val intent = Intent(this.activity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }

            if (binding.HRCheckBox.isChecked) {
                // use HR as a symptom
                val hrSymptom = sharedViewModel.symptomsMap["HeartRate"]
                if (hrSymptom != null) {
                    sharedViewModel.addSymptom(hrSymptom, newHR.toDouble())
                }
                binding.HRText.text = newHR.toString()
            } else {
                binding.HRText.text = "Unavailable"
            }
        })

        // Check box
        binding.setDefaultCheckBox.isChecked = sharedViewModel.setUseDefaultValuesCheckValue
        binding.setDefaultCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setUseDefaultValues(isChecked)
            sharedViewModel.setUseDefaultValuesCheckValue = isChecked
        }

        // click listener for search button
        binding.symptomSearchButton.setOnClickListener { view: View ->
            if (!sharedViewModel.acceptFlag) {
                acceptTermsAlert(view)
            } else {
                val directions = UserMainMenuFragmentDirections.actionUsermainmenuToSearchfragment()
                view.findNavController().navigate(directions)
            }
        }


        // Analyze button
        binding.analysisButton.isEnabled = (activity as MainActivity).loginInfo.numberOfSymptoms != "0"
                || (sharedViewModel.selectedSymptomsMap.isNotEmpty())
        binding.analysisButton.setOnClickListener { view: View ->
            GlobalScope.launch {
                async { sharedViewModel.analyzeDisease() }
                async { sharedViewModel.suggestedSpecializations() }
            }

            val loading = LoadingDialog(this.activity!!)
            loading.startLoading()
            val handler = Handler()
            handler.postDelayed(object :Runnable{
                override fun run() {
                    Wearable.getDataClient(activity as MainActivity).removeListener(sharedViewModel)
                    val directions = UserMainMenuFragmentDirections.actionUsermainmenuToAnalyzefragment()
                    view.findNavController().navigate(directions)
                    loading.isDismiss()
                }
            },5000)
        }

        // Sign out button
        binding.signOutButton.setOnClickListener { view:View ->
            viewModelStore.clear()

            val intent = Intent(activity, MainActivity::class.java)
            Log.e("Confirm go to", "Main")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            Wearable.getDataClient(activity as MainActivity).removeListener(sharedViewModel)

            (activity as MainActivity).loginInfo = UserDataClass(username = "", image = null,  userKey = "", sessionID = "", numberOfSymptoms = "0")
            val directions = UserMainMenuFragmentDirections.actionUsermainmenuToUserlgoinprofilefragment()
            view.findNavController().navigate(directions)

            Log.e("send logout to", "wear")
            sendCommandToWear("Logout")
        }

        return binding.root
    }

    // Alert dialog for accepting terms
    fun acceptTermsAlert(view: View) {

        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Accept The Terms")
        val linkTextView = layoutInflater.inflate(R.layout.alertdialog, null)
        alertDialogBuilder.setView(linkTextView)
        val acceptTermsLink = linkTextView.findViewById<TextView>(R.id.AcceptTermsLink)
        acceptTermsLink.movementMethod = LinkMovementMethod.getInstance()
        alertDialogBuilder.setPositiveButton("Accept") { dialogInterface, which ->
            sharedViewModel.acceptFlag = true
            sharedViewModel.sendFlagToFirebase("true")
            sharedViewModel.acceptTerms()

            val directions = UserMainMenuFragmentDirections.actionUsermainmenuToSearchfragment()
            view.findNavController().navigate(directions)
        }
        alertDialogBuilder.setNegativeButton("Decline") { dialogInterface, which ->
            sharedViewModel.acceptFlag = false
            sharedViewModel.sendFlagToFirebase("false")
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(activity as MainActivity).addListener(sharedViewModel)

    }

    override fun onPause() {
        super.onPause()
//        Wearable.getDataClient(activity as MainActivity).removeListener(sharedViewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(activity as MainActivity).removeListener(sharedViewModel)
    }


    private fun sendCommandToWear(command: String){
        Thread(Runnable {
            val connectedNodes: List<String> =
                Tasks.await(Wearable.getNodeClient(activity as MainActivity).connectedNodes)
                    .map { it.id }
            connectedNodes.forEach {
                val messageClient: MessageClient =
                    Wearable.getMessageClient(activity as MainActivity)
                messageClient.sendMessage(it, "/command", command.toByteArray())
            }
        }).start()
            Log.e("Send message: ", command)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("HR_Note",name,importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = (activity as MainActivity).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.i("Notification","create")
            }
    }

    private fun sendNotification(HR: Int){


        val builder = NotificationCompat.Builder(context!!,"HR_Note")
            .setSmallIcon(R.drawable.vd_logo)
            .setContentTitle("Heart Rate Warning!")
            .setContentText("Your heart rate is $HR, which is abnormal right now, please check the APP")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context!!)){
            notify(1,builder.build())
        }

    }
}