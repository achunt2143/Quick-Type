package com.achunt.justtype

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable? = null,
    var launchCount: Int = 0
)
