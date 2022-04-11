package com.epfl.esl.endlessapi.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.epfl.esl.endlessapi.Endless_Interface.EndlessInterface
import com.epfl.esl.endlessapi.Endless_Interface.UserIDResponse
import com.epfl.esl.endlessapi.FragmentViewModel.SearchSymptomsViewModel
import com.epfl.esl.endlessapi.FragmentViewModel.UserLoginProfileViewModel
import com.epfl.esl.endlessapi.MainActivity
import com.epfl.esl.endlessapi.R
import com.epfl.esl.endlessapi.UserDataClass
import com.epfl.esl.endlessapi.databinding.FragmentUserLoginProfileBinding
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream


class UserLoginProfileFragment : Fragment() {

    private lateinit var viewModel: UserLoginProfileViewModel
    private lateinit var binding: FragmentUserLoginProfileBinding
    private val sharedViewModel: SearchSymptomsViewModel by activityViewModels()
    var userProfile = UserDataClass(username = "", image = null,  userKey = "", sessionID = "", numberOfSymptoms = "0")

    // Initial Variable for Endless
    val BASE_URL: String = "http://api.endlessmedical.com/v1/dx/"
    private val retrofitBuilder: Retrofit = Retrofit.Builder().addConverterFactory(
        GsonConverterFactory.create())
        .baseUrl(BASE_URL).build()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_login_profile,
            container, false)

        // get a temp ID
        getUserIDData()

        // Link the ViewModel to the fragment
        viewModel = ViewModelProvider(this).get(UserLoginProfileViewModel::class.java)

        // Generate a random key every time we write new data for Firebase
        viewModel.key = viewModel.profileRef.push().key.toString()



        // Checking whether there's image at local or not
        if (viewModel.imageUri != null) {
            binding.Userimage.setImageURI(viewModel.imageUri)
        }

        // Button
        // Select Image Click
        binding.Userimage.setOnClickListener {
            val imgIntent = Intent(Intent.ACTION_GET_CONTENT)
            imgIntent.type = "image/*"
            resultLauncher.launch(imgIntent)
        }

        // Button
        // Sign up Button -> Checking all values are valid
        binding.SignUpButton.setOnClickListener { view: View ->
            viewModel.username = binding.Username.text.toString()
            viewModel.password = binding.Password.text.toString()

            if (viewModel.username == "") {
                Toast.makeText(context,"Enter username", Toast.LENGTH_SHORT).show()
            }
            else if (viewModel.password == "") {
                Toast.makeText(context,"Enter password", Toast.LENGTH_SHORT).show()
            }
            else if (viewModel.imageUri == null) {
                Toast.makeText(context,"Pick an image", Toast.LENGTH_SHORT).show()
            }
            else {

                viewModel.profileRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.e("SignUpButton ", "event")

                        var isRegistered = false

                        for (user in dataSnapshot.children) {
                            val usernameDatabase = user.child("username").value
                            if (viewModel.username == usernameDatabase) {
                                Toast.makeText(context,"This username is already registered to Firebase. Use another name", Toast.LENGTH_SHORT).show()
                                isRegistered = true
                                break
                            }
                        }
                        if (!isRegistered) {
                            // to get a new session ID from the Endless API
                            Toast.makeText(context,"Your are registered to Firebase, please click Sign In", Toast.LENGTH_SHORT).show()
                            // to update all information to firebase
                            sendDataToFireBase()
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
        }

        // Button
        // Sign In Button -> Read from realtime database
        binding.SignInButton.setOnClickListener { view: View ->
            viewModel.profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
                // change the data
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    viewModel.username = binding.Username.text.toString()
                    viewModel.password = binding.Password.text.toString()
                    var correctUsername = false
                    var correctPassword = false

                    // search for all keys
                    for (user in dataSnapshot.children) {
                        val usernameDatabase = user.child("username").value.toString()

                        if (viewModel.username == usernameDatabase) {
                            userProfile.numberOfSymptoms = user.child("symptomList").childrenCount.toString()
                            Log.e("size is ", userProfile.numberOfSymptoms.toString())
                            val passwordDatabase = user.child("password").value.toString()
                            val sessionIDDatabase = user.child("sessionID").value.toString()
                            Log.e("sign in ", usernameDatabase)
                            Log.e("password ", passwordDatabase)
                            Log.e("ID from fire", sessionIDDatabase)
                            if (viewModel.password == passwordDatabase) {
                                correctUsername = true
                                correctPassword = true
                                viewModel.key = user.key.toString()
                                sharedViewModel.key = user.key.toString()
                                viewModel.sessionIDFromFirebase = sessionIDDatabase
                                sharedViewModel.sessionID = sessionIDDatabase
                                break
                            } else {
                                correctUsername = true
                                correctPassword = false
                                Toast.makeText(
                                    context, "Wrong password.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    if (!correctUsername) {
                        Toast.makeText(
                            context, "You should register first.", Toast.LENGTH_LONG).show()
                    }
                    if (correctUsername && correctPassword) {


                        userProfile.username = viewModel.username
                        userProfile.image = viewModel.imageUri
                        userProfile.userKey = viewModel.key
                        userProfile.sessionID = viewModel.sessionIDFromFirebase

                        viewModel.profileRef.child(viewModel.key).addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {

                                sharedViewModel.acceptFlag = snapshot.child("acceptFlag").value.toString() == "true"


                                for (symptoms in snapshot.child("symptomList").children){
                                    val symptomName = symptoms.key
                                    val symptomListValue = symptoms.child("ListValue").value.toString() // string value to show list
                                    val symptomMapValue = symptoms.child("MapValue").value.toString()  // real value to update api
                                    val symptomValueType = symptoms.child("ValueType").value.toString() // value type
                                    val symptomValueChoiceIndex = symptoms.child("ChoiceIndex").value.toString() // value type

                                    Log.e("The list in", "database")
                                    Log.e(symptomName, symptomListValue)
                                    Log.e(symptomName, symptomMapValue)
                                    Log.e(symptomName, symptomValueType)
                                    Log.e(symptomName, symptomValueChoiceIndex)

                                    val symptomDetails = sharedViewModel.symptomsMap[symptomName]
                                    viewModel.symptomsNameList.add(symptomName!!)
                                    viewModel.symptomsStringValueList.add(symptomListValue)
                                    viewModel.symptomsMapValueList.add(symptomMapValue)
                                    viewModel.symptomsTextList.add(symptomDetails!!.text)

                                    when (symptomValueType) {
                                        "seekBar" -> {
                                            sharedViewModel.addSymptom(symptomDetails, symptomMapValue.toDouble())
                                        }
                                        "spinner" -> {
                                            sharedViewModel.addSymptom(symptomDetails, symptomMapValue.toInt(), symptomListValue, symptomValueChoiceIndex.toInt())
                                        }
                                        else -> {
                                            Log.e("This type is", "error")
                                        }
                                    }
                                }

                            }
                            override fun onCancelled(error: DatabaseError) {
                            }

                        })


                        // send image and username to wear
                        val dataClient: DataClient = Wearable.getDataClient(activity as AppCompatActivity)
                        viewModel.sendDataToWear(activity!!.applicationContext, dataClient)

                        (activity as MainActivity).loginInfo = userProfile
                        val directions = UserLoginProfileFragmentDirections.actionUserlgoinprofilefragmentToUsermainmenu(userProfile)
                        view.findNavController().navigate(directions)
                    }
                }
                // Cancel the change in data
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        return binding.root
    }

    // Function for getting new user ID
    private fun getUserIDData() {
        val jsonAPI = retrofitBuilder.create(EndlessInterface::class.java)
        val retrofitData = jsonAPI.getUserID()

        retrofitData.enqueue(object : Callback<UserIDResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<UserIDResponse>, response: Response<UserIDResponse>) {
                if (!response.isSuccessful){
                    Log.e("Userid","Code: "+response.code())
                    return
                }
                val responseBody = response.body()!! // !!: not safety operator
                Log.e("Successful ", "Getting sessionID")

                viewModel.tempID = responseBody.SessionID
                Log.e("Temp ID is ", viewModel.tempID.toString())
            }
            override fun onFailure(call: Call<UserIDResponse>, t: Throwable) {
                Toast.makeText(context?.applicationContext, "Reading API Data Fail", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function: Sending username, password, Endless sessionID, imageUri to Firebase
    fun sendDataToFireBase(){
        viewModel.profileRef.child(viewModel.key).child("username").setValue(viewModel.username)
        viewModel.profileRef.child(viewModel.key).child("password").setValue(viewModel.password)
        viewModel.profileRef.child(viewModel.key).child("sessionID").setValue(viewModel.tempID)
        viewModel.profileRef.child(viewModel.key).child("acceptFlag").setValue("false")

        // create the reference to the location where to store the data
        var profileImageRef = viewModel.storageRef.child("ProfileImages/"+ viewModel.username +".jpg")

        // convert the image to a ByteArray and upload it to Firebase storage
        val matrix = Matrix()
//        matrix.postRotate(90F)
        var imageBitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver,
            viewModel.imageUri)
        val ratio:Float = 13F
        val imageBitmapScaled = Bitmap.createScaledBitmap(imageBitmap,
            (imageBitmap.width / ratio).toInt(), (imageBitmap.height / ratio).toInt(), false)
        imageBitmap = Bitmap.createBitmap(imageBitmapScaled, 0, 0,
            (imageBitmap.width / ratio).toInt(), (imageBitmap.height / ratio).toInt(),
            matrix, true)
        val stream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageByteArray = stream.toByteArray()
        var uploadProfileImage = profileImageRef.putBytes(imageByteArray)

        // listen to upload outcome; when successful, add photo URL field to the profile in the Real Time Database
        uploadProfileImage.addOnFailureListener {
            Toast.makeText(context,"Profile image upload to firebase was failed.", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            viewModel.profileRef.child(viewModel.key).child("photo URL").
            setValue((FirebaseStorage.getInstance().getReference()).toString()
                    +"ProfileImages/"+ viewModel.username +".jpg")
        }
    }

    // Launcher for imageUri
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                viewModel.imageUri = imageUri
                binding.Userimage.setImageURI(imageUri)
            }
        }




}