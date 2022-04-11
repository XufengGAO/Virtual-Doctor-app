package com.epfl.esl.endlessapi.FragmentViewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class UserLoginProfileViewModel : ViewModel() {
    var imageUri: Uri? = null
    var username: String = ""
    var password: String = ""
    var key: String  = ""
    var tempID: String? = null
    var sessionIDFromFirebase: String = ""
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val profileRef: DatabaseReference = database.getReference("Profiles")
    var storageRef = Firebase.storage.reference

    val symptomsTextList = ArrayList<String>()
    val symptomsStringValueList = ArrayList<String>()
    val symptomsMapValueList = ArrayList<String>()
    val symptomsNameList = ArrayList<String>()

    init{
        imageUri = null
        username = ""
        password = ""
        key = profileRef.push().key.toString()
    }

    fun reset(){
        onCleared()
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("ViewModel", "is cleared")
    }

    fun sendDataToWear(context: Context, dataClient: DataClient) {

        var imageByteArray: ByteArray? = null


        if (imageUri == null) {

            var imageRef = storageRef.child("ProfileImages/"+username+".jpg")
            imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { byteArray ->
                val image: Drawable = BitmapDrawable(BitmapFactory.decodeByteArray(byteArray,0, byteArray.size))
                val bitmap = (image as BitmapDrawable).bitmap
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                imageByteArray = stream.toByteArray()
                sendRequest(imageByteArray!!, dataClient)
            }.addOnFailureListener {
                Toast.makeText(context,"Image read was unsuccessful.", Toast.LENGTH_SHORT).show()
            }

        }
        else {
            val matrix = Matrix()

            var imageBitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
            val ratio: Float = 13F

            val imageBitmapScaled = Bitmap.createScaledBitmap(
                imageBitmap,
                (imageBitmap.width / ratio).toInt(),
                (imageBitmap.height / ratio).toInt(),
                false
            )
            imageBitmap = Bitmap.createBitmap(
                imageBitmapScaled,
                0,
                0,
                (imageBitmap.width / ratio).toInt(),
                (imageBitmap.height / ratio).toInt(),
                matrix,
                true
            )
            val stream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            imageByteArray = stream.toByteArray()
            sendRequest(imageByteArray!!, dataClient)
        }
    }

    fun sendRequest(imageByteArray: ByteArray, dataClient: DataClient){
        val request: PutDataRequest = PutDataMapRequest.create("/userInfo").run {
            dataMap.putByteArray("profileImage", imageByteArray)
            dataMap.putString("username", username)
            dataMap.putStringArrayList("symptomsTextList", symptomsTextList)
            dataMap.putStringArrayList("symptomsStringValueList", symptomsStringValueList)
            dataMap.putStringArrayList("symptomsMapValueList", symptomsMapValueList)
            dataMap.putStringArrayList("symptomsNameList", symptomsNameList)
            dataMap.putDouble("Random", Random.nextDouble())
            asPutDataRequest()
        }
        symptomsTextList.forEach { v ->
            Log.e("Text case", v)
        }
        symptomsStringValueList.forEach { v ->
            Log.e("value case", v)
        }
        if (symptomsTextList.size>0){
            for (i in 0 until (symptomsTextList.size-1)){
                println(symptomsMapValueList[i])
            }
        }
        Log.e("send data to", "wear")
        request.setUrgent()
        dataClient.putDataItem(request)
    }



}