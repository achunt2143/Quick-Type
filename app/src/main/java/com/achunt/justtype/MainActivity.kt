package com.achunt.justtype

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            checkPermission(Manifest.permission.READ_CONTACTS, 1)
        }
        if (savedInstanceState == null) {
            val fragment = JustType()
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }

    }
    private fun checkPermission(permission: String, requestCode: Int) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT)
                .show()
        }
    }
    @Override
    override fun onStart() {
        super.onStart()
    }
    @Override
    override fun onResume() {
        super.onResume()
    }
    @Override
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
    }
    @Override
    override fun onStop() {
        super.onStop()
    }
    @Override
    override fun onPause() {
        super.onPause()
        onStop()
    }
    @Override
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
    }
    @Override
    override fun onRestart() {
        super.onRestart()
    }
    @Override
    override fun onDestroy() {
        super.onDestroy()
    }
}