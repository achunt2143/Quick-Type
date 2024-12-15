package com.achunt.justtype

import android.net.Uri

data class Contact(
    val name: String,
    val number: String,
    val photoUri: Uri?
)
