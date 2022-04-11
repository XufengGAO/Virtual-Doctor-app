package com.epfl.esl.endlessapi.Fragment

import android.app.Activity
import android.app.AlertDialog
import com.epfl.esl.endlessapi.R

class LoadingDialog(val mActivity: Activity) {
    private lateinit var loadDialog: AlertDialog

    fun startLoading(){
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_item,null)

        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        loadDialog = builder.create()
        loadDialog.show()
    }

    fun isDismiss(){
        loadDialog.dismiss()
    }

}