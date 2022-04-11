package com.epfl.esl.endlessapi

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserDataClass(var username: String?, var image: Uri?, var userKey: String?, var sessionID: String?, var numberOfSymptoms: String?): Parcelable
