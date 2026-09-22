package com.achunt.justtype

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val requestContactsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            lifecycleScope.launch(Dispatchers.IO) {
                ContactRepository.getContacts(applicationContext, forceReload = true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.preference.PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        setContentView(R.layout.activity_main)

        // Request contacts permission seamlessly if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestContactsLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, JustType())
                .commit()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Forward hardware keyboard strokes directly to the search input if not already focused
        if (event != null && event.isPrintingKey) {
            val searchInput = findViewById<EditText>(R.id.jtInput)
            if (searchInput != null && !searchInput.isFocused) {
                searchInput.requestFocus()
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}